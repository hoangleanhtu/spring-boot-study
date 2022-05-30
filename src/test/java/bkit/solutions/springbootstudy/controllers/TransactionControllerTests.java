package bkit.solutions.springbootstudy.controllers;

import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.NOT_ENOUGH_BALANCE_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE;
import static bkit.solutions.springbootstudy.utils.RestRequestBuilder.postJson;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bkit.solutions.springbootstudy.BaseApplicationIntegrationTests;
import bkit.solutions.springbootstudy.clients.ExternalBankClient;
import bkit.solutions.springbootstudy.constants.TransactionApiEndpoints;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

@MockServerTest
public class TransactionControllerTests extends BaseApplicationIntegrationTests {

  private static final String TRANSACTION_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.TRANSFER_V1;
  private static final String EXTERNAL_TRANSFER_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.EXTERNAL_TRANSFER_V1;
  private static final String $_SENDING_ACCOUNT_NUMBER_PATH = "$.sendingAccountNumber";
  private static final String $_RECEIVING_ACCOUNT_NUMBER_PATH = "$.receivingAccountNumber";
  private static final String $_AMOUNT_PATH = "$.amount";
  private static final String $_ERROR_CODE_PATH = "$.errorCode";

  private MockServerClient mockServerClient;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private MockMvc mockMvc;

  @AfterEach
  void clean() {
    accountRepository.deleteAll();
  }

  @ParameterizedTest
  @DisplayName("transferV1 with valid account & balance should be successful")
  @MethodSource
    // use transferV1ShouldBeSuccessful()
  void transferV1ShouldBeSuccessful(String requestPayload, AccountEntity sendingAccount,
      AccountEntity receivingAccount, BigDecimal expectedBalance) throws Exception {
    // GIVEN
    accountRepository.save(sendingAccount);
    accountRepository.save(receivingAccount);

    // WHEN
    mockMvc
        .perform(postJson(TRANSACTION_V1_PATH)
            .content(requestPayload)
        )
        .andExpect(
            status().isOk()
        )
        .andExpect(jsonPath("$.balance", comparesEqualTo(expectedBalance), BigDecimal.class));
  }

  private static Stream<Arguments> transferV1ShouldBeSuccessful() throws IOException {
    final File transferV1Payload = getTransferPayloadFile();

    final DocumentContext document = JsonPath.parse(transferV1Payload);
    final String sendingAccountNumber = document.read($_SENDING_ACCOUNT_NUMBER_PATH);
    final String receivingAccountNumber = document.read($_RECEIVING_ACCOUNT_NUMBER_PATH);

    final BigDecimal oneThousand = new BigDecimal("1000");

    final String amountPath = $_AMOUNT_PATH;
    return Stream.of(
        Arguments.of(
            JsonPath.parse(transferV1Payload)
                .set(amountPath, oneThousand)
                .jsonString(),
            AccountEntity.builder()
                .accountNumber(sendingAccountNumber)
                .balance(oneThousand)
                .build(),
            AccountEntity.builder()
                .accountNumber(receivingAccountNumber)
                .balance(BigDecimal.ZERO)
                .build(),
            BigDecimal.ZERO
        ),
        Arguments.of(
            JsonPath.parse(transferV1Payload)
                .set(amountPath, new BigDecimal("900"))
                .jsonString(),
            AccountEntity.builder()
                .accountNumber(sendingAccountNumber)
                .balance(oneThousand)
                .build(),
            AccountEntity.builder()
                .accountNumber(receivingAccountNumber)
                .balance(BigDecimal.ZERO)
                .build(),
            new BigDecimal("100")
        )
    );
  }

  @NotNull
  private static File getTransferPayloadFile() throws IOException {
    return new ClassPathResource(
        "controllers/transaction-v1-request-payload.json").getFile();
  }

  @Test
  void transferToExternalAccountShouldFailWithErrorNotEnoughBalance() throws Exception {
    final DocumentContext document = getTransferPayload();
    final String sendingAccountNumber = document.read($_SENDING_ACCOUNT_NUMBER_PATH);
    final BigDecimal amount = document.read($_AMOUNT_PATH, BigDecimal.class);

    accountRepository.save(
        AccountEntity.builder()
            .accountNumber(sendingAccountNumber)
            .balance(amount.subtract(BigDecimal.ONE))
            .build()
    );

    mockMvc.perform(postJson(EXTERNAL_TRANSFER_V1_PATH)
            .content(document.jsonString()))
        .andExpect(status().is4xxClientError())
        .andExpect(
            jsonPath($_ERROR_CODE_PATH,
                is(NOT_ENOUGH_BALANCE_ERROR_CODE)));
  }

  @Test
  void transferToNotFoundExternalAccount() throws Exception {
    final DocumentContext transferPayload = getTransferPayload();
    final String sendingAccountNumber = transferPayload.read($_SENDING_ACCOUNT_NUMBER_PATH);
    final BigDecimal amount = transferPayload.read($_AMOUNT_PATH, BigDecimal.class);

    // GIVEN
    accountRepository.save(AccountEntity.builder()
        .accountNumber(sendingAccountNumber)
        .balance(amount)
        .build());

    final DocumentContext mockNotFoundResponse = JsonPath.parse(
        new ClassPathResource("mock-responses/transfer-external-response-template.json").getFile());

    mockNotFoundResponse.set($_ERROR_CODE_PATH,
        RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE);

    final HttpRequest mockExternalTransferRequest = HttpRequest.request().withMethod(HttpMethod.POST.name())
        .withPath(ExternalBankClient.TRANSFER_EXTERNAL_PATH);
    mockServerClient.when(
        mockExternalTransferRequest
    ).respond(HttpResponse.response()
        .withBody(mockNotFoundResponse.jsonString(), MediaType.APPLICATION_JSON));

    // WHEN
    mockMvc
        .perform(postJson(EXTERNAL_TRANSFER_V1_PATH).content(transferPayload.jsonString()))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(
            jsonPath($_ERROR_CODE_PATH, is(RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE)));

    mockServerClient.verify(mockExternalTransferRequest);
  }

  private DocumentContext getTransferPayload() throws IOException {
    return JsonPath.parse(getTransferPayloadFile());
  }
}
