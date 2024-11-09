package br.com.navelogic.telegrambotassistenterpg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@SpringBootApplication
public class TelegramBotAssistenteRpgApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramBotAssistenteRpgApplication.class, args);
	}
}
@Component
class TelegramBotAssistenteRpgInitializer implements CommandLineRunner {
	@Value("${telegram.bot.token}")
	private String botToken;
	@Override
	public void run(String... args) throws Exception {
		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			botsApplication.registerBot(botToken, new RPGBot(botToken));
			System.out.println("RPGBot está rodando...");
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
