package bkit.solutions.springbootstudy.services;

import bkit.solutions.springbootstudy.dtos.BalanceChangeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BalanceChangeHandler {

  @RabbitListener(queues = "#{amqpProperties.notifyBalanceQueue}")
  public void handleBalanceChange(BalanceChangeDto balanceChangeDto) {
    log.info("Balance change {}", balanceChangeDto);
  }
}
