package bkit.solutions.springbootstudy.controllers;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bkit.solutions.springbootstudy.BaseApplicationIntegrationTests;
import bkit.solutions.springbootstudy.constants.TransactionApiEndpoints;
import bkit.solutions.springbootstudy.entities.AccountEntity;
import bkit.solutions.springbootstudy.repositories.AccountRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class TransactionControllerTests extends BaseApplicationIntegrationTests {

  private static final String TRANSACTION_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.TRANSFER_V1;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private MockMvc mockMvc;

  @ParameterizedTest
  @DisplayName("transferV1 with valid account & balance should be successful")
  @MethodSource // use transferV1ShouldBeSuccessful() below
  void transferV1ShouldBeSuccessful(String requestPayload, AccountEntity sendingAccount,
      AccountEntity receivingAccount, BigDecimal expectedBalance) throws Exception {
    accountRepository.save(sendingAccount);
    accountRepository.save(receivingAccount);

    mockMvc
        .perform(post(TRANSACTION_V1_PATH)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestPayload)
        )
        .andExpect(
            status().isOk()
        )
        .andExpect(jsonPath("$.balance", comparesEqualTo(expectedBalance), BigDecimal.class));
  }

  private static Stream<Arguments> transferV1ShouldBeSuccessful() throws IOException {
    final File transferV1Payload = new ClassPathResource(
        "controllers/transaction-v1-request-payload.json").getFile();
    final DocumentContext document = JsonPath.parse(transferV1Payload);

    final String sendingAccountNumber = document.read("$.sendingAccountNumber");
    final String receivingAccountNumber = document.read("$.receivingAccountNumber");

    final BigDecimal oneThousand = new BigDecimal("1000");

    return Stream.of(
        Arguments.of(
            JsonPath.parse(transferV1Payload)
                .set("$.amount", oneThousand)
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
        )
    );
  }

}
