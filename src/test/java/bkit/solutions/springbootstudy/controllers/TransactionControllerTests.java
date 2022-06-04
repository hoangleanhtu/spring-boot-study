package bkit.solutions.springbootstudy.controllers;

import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.NOT_ENOUGH_BALANCE_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE;
import static bkit.solutions.springbootstudy.utils.RestRequestBuilder.postJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bkit.solutions.springbootstudy.BaseApplicationIntegrationTests;
import bkit.solutions.springbootstudy.clients.ExternalBankClient;
import bkit.solutions.springbootstudy.constants.TransactionApiEndpoints;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.AbstractIntegerAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@MockServerTest
@TestPropertySource({"classpath:feign-timeout.properties"})
public class TransactionControllerTests extends BaseApplicationIntegrationTests {

  private static final String TRANSACTION_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.TRANSFER_V1;
  private static final String EXTERNAL_TRANSFER_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.EXTERNAL_TRANSFER_V1;
  private static final String $_SENDING_ACCOUNT_NUMBER_PATH = "$.sendingAccountNumber";
  private static final String $_RECEIVING_ACCOUNT_NUMBER_PATH = "$.receivingAccountNumber";
  private static final String $_AMOUNT_PATH = "$.amount";
  private static final String $_ERROR_CODE_PATH = "$.errorCode";
  public static final String TRANSACTIONS_TABLE_NAME = "transactions";
  public static final String TRANSFER_V1_REQUEST_PAYLOAD = "request-payloads/transaction-v1-request-payload.json";
  public static final String MOCK_RESPONSES_TRANSFER_EXTERNAL_RESPONSE_TEMPLATE_JSON = "mock-responses/transfer-external-response-template.json";

  private MockServerClient mockServerClient;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @AfterEach
  void clean() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "accounts", TRANSACTIONS_TABLE_NAME);
  }

  @ParameterizedTest
  @DisplayName("transferV1 with valid account & balance should be successful")
  @MethodSource
    // use transferV1ShouldBeSuccessful()
  void transferV1ShouldBeSuccessful(String requestPayload, String insertSendingAccountSql,
      String insertReceivingAccountSql, BigDecimal expectedBalance) throws Exception {
    // GIVEN
    jdbcTemplate.execute(insertSendingAccountSql);
    jdbcTemplate.execute(insertReceivingAccountSql);

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

    final String insertAccountSql = "INSERT INTO accounts (account_number, balance) VALUES ('%s', %d)";
    final String insertSendingAccountSql = String.format(insertAccountSql, sendingAccountNumber, 1000);
    final String insertReceivingAccountSql = String.format(insertAccountSql, receivingAccountNumber, 0);
    return Stream.of(
        Arguments.of(
            JsonPath.parse(transferV1Payload)
                .set(amountPath, oneThousand)
                .jsonString(),
            insertSendingAccountSql,
            insertReceivingAccountSql,
            BigDecimal.ZERO
        ),
        Arguments.of(
            JsonPath.parse(transferV1Payload)
                .set(amountPath, new BigDecimal("900"))
                .jsonString(),
            insertSendingAccountSql,
            insertReceivingAccountSql,
            new BigDecimal("100")
        )
    );
  }

  @Test
  @Sql(statements = {"INSERT INTO accounts (account_number, balance) VALUES ('vinh', 1000)"})
  void transferToExternalAccountShouldFailWithErrorNotEnoughBalance() throws Exception {
    // WHEN
    mockMvc.perform(postJson(EXTERNAL_TRANSFER_V1_PATH)
            .content(getTransferPayloadBytes()))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(
            jsonPath($_ERROR_CODE_PATH,
                is(NOT_ENOUGH_BALANCE_ERROR_CODE)));
    assertNumberOfTransactions().isEqualTo(0);
  }

  private byte[] getTransferPayloadBytes() throws IOException {
    return IOUtils.toByteArray(getClasspathResource(TRANSFER_V1_REQUEST_PAYLOAD).getInputStream());
  }

  @ParameterizedTest
  @ValueSource(strings = {RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE,
      RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE})
  @Sql(statements = {"INSERT INTO accounts (account_number, balance) VALUES ('vinh', 1001)"})
  void transferToExternalAccountRespondsErrorCode(final String errorCode) throws Exception {
    // GIVEN
    final DocumentContext mockNotFoundResponse = JsonPath.parse(
        getClasspathResource(MOCK_RESPONSES_TRANSFER_EXTERNAL_RESPONSE_TEMPLATE_JSON).getFile());

    mockNotFoundResponse.set($_ERROR_CODE_PATH,
        errorCode);

    final HttpRequest mockExternalTransferRequest = getMockExternalTransferRequest();
    mockServerClient.when(mockExternalTransferRequest)
        .respond(HttpResponse.response()
            .withBody(mockNotFoundResponse.jsonString(), MediaType.APPLICATION_JSON));

    // WHEN
    mockMvc
        .perform(postJson(EXTERNAL_TRANSFER_V1_PATH).content(getTransferPayloadBytes()))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(
            jsonPath($_ERROR_CODE_PATH, is(errorCode)));

    mockServerClient.verify(mockExternalTransferRequest);
    assertNumberOfTransactions().isEqualTo(0);
  }


  @Test
  @Sql(statements = {"INSERT INTO accounts (account_number, balance) VALUES ('vinh', 1001)"})
  void transferToExternalAccountTimeout() throws Exception {
    // GIVEN
    final HttpRequest mockExternalTransferRequest = getMockExternalTransferRequest();
    mockServerClient.when(mockExternalTransferRequest)
        .respond(
            HttpResponse.response()
                .withBody("{}", MediaType.APPLICATION_JSON)
                .withDelay(Delay.seconds(1))
        );

    mockMvc
        .perform(postJson(EXTERNAL_TRANSFER_V1_PATH).content(getTransferPayloadBytes()))
        .andExpect(status().is(HttpStatus.GATEWAY_TIMEOUT.value()));

    mockServerClient.verify(mockExternalTransferRequest);
    assertNumberOfTransactions().isEqualTo(0);
  }

  @Test
  @Sql(statements = {"INSERT INTO accounts (account_number, balance) VALUES ('vinh', 1001)"})
  void transferToExternalShouldBeSuccessful() throws Exception {
    final DocumentContext transferPayload = getTransferPayload();
    final String sendingAccountNumber = transferPayload.read($_SENDING_ACCOUNT_NUMBER_PATH);

    final HttpRequest mockExternalTransferRequest = getMockExternalTransferRequest();
    mockServerClient.when(mockExternalTransferRequest)
        .respond(
            HttpResponse.response()
                .withBody("{}", MediaType.APPLICATION_JSON)
        );

    mockMvc.perform(postJson(EXTERNAL_TRANSFER_V1_PATH).content(getTransferPayloadBytes()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber", is(sendingAccountNumber)))
        .andExpect(jsonPath("$.balance", comparesEqualTo(BigDecimal.ZERO), BigDecimal.class));
    assertNumberOfTransactions().isEqualTo(1);
  }

  @NotNull
  private AbstractIntegerAssert<?> assertNumberOfTransactions() {
    return assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, TRANSACTIONS_TABLE_NAME));
  }

  private DocumentContext getTransferPayload() throws IOException {
    return JsonPath.parse(getTransferPayloadFile());
  }

  private HttpRequest getMockExternalTransferRequest() {
    return HttpRequest.request()
        .withMethod(HttpMethod.POST.name())
        .withPath(ExternalBankClient.TRANSFER_EXTERNAL_PATH);
  }

  @NotNull
  private static File getTransferPayloadFile() throws IOException {
    return getClasspathResource(TRANSFER_V1_REQUEST_PAYLOAD).getFile();
  }

  @NotNull
  private static ClassPathResource getClasspathResource(String path) {
    return new ClassPathResource(path);
  }
}
