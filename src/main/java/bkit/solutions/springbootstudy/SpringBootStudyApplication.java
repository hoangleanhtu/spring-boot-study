package bkit.solutions.springbootstudy;

import bkit.solutions.springbootstudy.config.InitData;
import bkit.solutions.springbootstudy.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class SpringBootStudyApplication implements CommandLineRunner {

	private final AccountService accountService;
	private final InitData initData;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootStudyApplication.class, args);
	}

	@Override
	public void run(String... args) {
		initData.getAccounts().forEach(it -> accountService.create(it.getAccountNumber(), it.getDepositAmount()));
	}
}
