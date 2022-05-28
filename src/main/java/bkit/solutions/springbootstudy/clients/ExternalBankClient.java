package bkit.solutions.springbootstudy.clients;

import bkit.solutions.springbootstudy.clients.dtos.PostExternalTransferRequest;
import bkit.solutions.springbootstudy.clients.dtos.PostExternalTransferResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(url = "${external-bank.url}")
public interface ExternalBankClient {

  @PostMapping("/transfer/external")
  PostExternalTransferResponse transfer(@RequestBody PostExternalTransferRequest request);
}
