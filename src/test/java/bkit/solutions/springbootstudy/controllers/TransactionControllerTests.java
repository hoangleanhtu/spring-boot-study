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
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class TransactionControllerTests extends BaseApplicationIntegrationTests {

  private static final String TRANSACTION_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.TRANSFER_V1;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("transferV1 with valid account & balance should be successful")
  // TODO tu.hoang should improve tests for reader aware the values of each tests
  void transferV1ShouldBeSuccessful() throws Exception {
    final Resource transferV1Payload = new ClassPathResource(
        "controllers/transaction-v1-request-payload.json");
    final DocumentContext document = JsonPath.parse(transferV1Payload.getInputStream());

    final String sendingAccountNumber = document.read("$.sendingAccountNumber");
    final BigDecimal amount = document.read("$.amount", BigDecimal.class);
    accountRepository.save(AccountEntity.builder()
        .accountNumber(sendingAccountNumber)
        .balance(amount)
        .build()
    );

    final String receivingAccountNumber = document.read("$.receivingAccountNumber");
    accountRepository.save(
        AccountEntity.builder()
            .balance(BigDecimal.ZERO)
            .accountNumber(receivingAccountNumber)
            .build()
    );

    mockMvc
        .perform(post(TRANSACTION_V1_PATH)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(document.jsonString())
        )
        .andExpect(
            status().isOk()
        )
        .andExpect(jsonPath("$.balance", comparesEqualTo(BigDecimal.ZERO), BigDecimal.class));
  }

}
