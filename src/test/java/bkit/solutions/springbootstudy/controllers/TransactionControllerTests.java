package bkit.solutions.springbootstudy.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bkit.solutions.springbootstudy.BaseApplicationIntegrationTests;
import bkit.solutions.springbootstudy.constants.TransactionApiEndpoints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class TransactionControllerTests extends BaseApplicationIntegrationTests {

  private static final String TRANSACTION_V1_PATH =
      TransactionApiEndpoints.PREFIX + TransactionApiEndpoints.TRANSFER_V1;

  @Autowired
  private MockMvc mockMvc;
  @Value("classpath:/controllers/transaction-v1-request-payload.json")
  private Resource transferV1Payload;

  @Test
  @DisplayName("transferV1 with valid account & balance should be successful")
  void transferV1ShouldBeSuccessful() throws Exception {

    mockMvc
        .perform(post(TRANSACTION_V1_PATH)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(transferV1Payload.getInputStream().readAllBytes())
        )
        .andExpect(
            status().isOk()
        );
  }
}
