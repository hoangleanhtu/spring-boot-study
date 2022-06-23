package bkit.solutions.springbootstudy.controllers;

import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.NOT_ENOUGH_BALANCE_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_INACTIVE_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.RECEIVING_ACCOUNT_NOT_FOUND_ERROR_CODE;
import static bkit.solutions.springbootstudy.exceptions.ExternalTransferErrorCodes.WEEKEND_ERROR_CODE;
import static bkit.solutions.springbootstudy.utils.RestRequestBuilder.postJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
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
import java.time.Clock;
import java.time.Instant;
import org.assertj.core.api.AbstractIntegerAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource({"classpath:feign-timeout.properties"})
public class ExternalTransferControllerIT extends BaseApplicationIntegrationTests {

  private static final String EXTERNAL_TRANSFER_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.EXTERNAL_TRANSFER_V1;
  private static final String $_SENDING_ACCOUNT_NUMBER_PATH = "$.sendingAccountNumber";
  private static final String $_AMOUNT_PATH = "$.amount";
  private static final String $_ERROR_CODE_PATH = "$.errorCode";
  public static final String TRANSACTIONS_TABLE_NAME = "transactions";
  public static final String TRANSFER_V1_REQUEST_PAYLOAD = "request-payloads/transaction-v1-request-payload.json";
  public static final String MOCK_RESPONSES_TRANSFER_EXTERNAL_RESPONSE_TEMPLATE_JSON = "mock-responses/transfer-external-response-template.json";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @MockBean
  private Clock clock;

  @BeforeEach
  void setup() {
    final Clock systemUTC = Clock.systemUTC();
    when(clock.instant()).thenReturn(systemUTC.instant());
    when(clock.getZone()).thenReturn(systemUTC.getZone());
  }

  @AfterEach
  void clean() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "accounts", TRANSACTIONS_TABLE_NAME);
  }

  @ParameterizedTest
  @ValueSource(strings = {"2022-06-25T00:00:00Z", "2022-08-28T00:00:00Z"})
  void transferOnWeekendShouldFail(String mockNow) throws Exception {
    when(clock.instant()).thenReturn(Instant.parse(mockNow));

    final DocumentContext transferPayload = getTransferPayloadDocument();
    mockMvc
        .perform(postJson(EXTERNAL_TRANSFER_V1_PATH).content(transferPayload.jsonString()))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(
            jsonPath($_ERROR_CODE_PATH, is(WEEKEND_ERROR_CODE)));
  }

  @Test
  @Sql(statements = {"INSERT INTO accounts (account_number, balance) VALUES ('vinh', 1000)"})
  void transferToExternalAccountShouldFailWithErrorNotEnoughBalance() throws Exception {
    // WHEN
    final DocumentContext transferPayload = getTransferPayloadDocument();
    transferPayload.set($_SENDING_ACCOUNT_NUMBER_PATH, "vinh");
    transferPayload.set($_AMOUNT_PATH, 1001);

    mockMvc.perform(postJson(EXTERNAL_TRANSFER_V1_PATH)
            .content(transferPayload.jsonString()))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(
            jsonPath($_ERROR_CODE_PATH,
                is(NOT_ENOUGH_BALANCE_ERROR_CODE)));
    assertNumberOfTransactions().isEqualTo(0);
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
    final DocumentContext transferPayload = getTransferPayloadDocument();
    transferPayload.set($_SENDING_ACCOUNT_NUMBER_PATH, "vinh");
    transferPayload.set($_AMOUNT_PATH, 1001);
    mockMvc
        .perform(postJson(EXTERNAL_TRANSFER_V1_PATH).content(transferPayload.jsonString()))
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

    // WHEN
    final DocumentContext transferPayload = getTransferPayloadDocument();
    transferPayload.set($_SENDING_ACCOUNT_NUMBER_PATH, "vinh");
    transferPayload.set($_AMOUNT_PATH, 1001);

    mockMvc
        .perform(postJson(EXTERNAL_TRANSFER_V1_PATH).content(transferPayload.jsonString()))
        .andExpect(status().is(HttpStatus.GATEWAY_TIMEOUT.value()));

    mockServerClient.verify(mockExternalTransferRequest);
    assertNumberOfTransactions().isEqualTo(0);
  }

  @Test
  @Sql(statements = {"INSERT INTO accounts (account_number, balance) VALUES ('vinh', 1001)"})
  void transferToExternalShouldBeSuccessful() throws Exception {
    final DocumentContext transferPayload = getTransferPayloadDocument();
    final String sendingAccountNumber = transferPayload.read($_SENDING_ACCOUNT_NUMBER_PATH);

    final HttpRequest mockExternalTransferRequest = getMockExternalTransferRequest();
    mockServerClient.when(mockExternalTransferRequest)
        .respond(
            HttpResponse.response()
                .withBody("{}", MediaType.APPLICATION_JSON)
        );

    transferPayload.set($_SENDING_ACCOUNT_NUMBER_PATH, "vinh");
    transferPayload.set($_AMOUNT_PATH, 1001);

    mockMvc.perform(postJson(EXTERNAL_TRANSFER_V1_PATH).content(transferPayload.jsonString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber", is(sendingAccountNumber)))
        .andExpect(jsonPath("$.balance", comparesEqualTo(BigDecimal.ZERO), BigDecimal.class));
    assertNumberOfTransactions().isEqualTo(1);
  }

  @NotNull
  private AbstractIntegerAssert<?> assertNumberOfTransactions() {
    return assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, TRANSACTIONS_TABLE_NAME));
  }

  private DocumentContext getTransferPayloadDocument() throws IOException {
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
