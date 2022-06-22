package bkit.solutions.springbootstudy.controllers;

import static bkit.solutions.springbootstudy.utils.RestRequestBuilder.postJson;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bkit.solutions.springbootstudy.BaseApplicationIntegrationTests;
import bkit.solutions.springbootstudy.constants.TransactionApiEndpoints;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource({"classpath:feign-timeout.properties"})
public class TransactionControllerIT extends BaseApplicationIntegrationTests {

  private static final String TRANSACTION_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.TRANSFER_V1;
  private static final String $_SENDING_ACCOUNT_NUMBER_PATH = "$.sendingAccountNumber";
  private static final String $_RECEIVING_ACCOUNT_NUMBER_PATH = "$.receivingAccountNumber";
  private static final String $_AMOUNT_PATH = "$.amount";
  public static final String TRANSACTIONS_TABLE_NAME = "transactions";
  public static final String TRANSFER_V1_REQUEST_PAYLOAD = "request-payloads/transaction-v1-request-payload.json";

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

  @NotNull
  private static File getTransferPayloadFile() throws IOException {
    return getClasspathResource(TRANSFER_V1_REQUEST_PAYLOAD).getFile();
  }

  @NotNull
  private static ClassPathResource getClasspathResource(String path) {
    return new ClassPathResource(path);
  }
}
