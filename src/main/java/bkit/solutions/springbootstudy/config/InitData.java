package bkit.solutions.springbootstudy.config;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "init-data")
public class InitData {
  private List<Account> accounts;

  public List<Account> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<Account> accounts) {
    this.accounts = accounts;
  }

  public static class Account {
    private String accountNumber;
    private BigDecimal depositAmount;

    public String getAccountNumber() {
      return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
    }

    public BigDecimal getDepositAmount() {
      return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
      this.depositAmount = depositAmount;
    }
  }
}
