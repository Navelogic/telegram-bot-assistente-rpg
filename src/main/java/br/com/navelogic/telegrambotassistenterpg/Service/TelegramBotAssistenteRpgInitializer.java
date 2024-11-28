package br.com.navelogic.telegrambotassistenterpg.Service;

import br.com.navelogic.telegrambotassistenterpg.RPGBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@Slf4j
@Service
public class TelegramBotAssistenteRpgInitializer implements CommandLineRunner {
    private final String botToken;
    private final RPGBot rpgBot;

    public TelegramBotAssistenteRpgInitializer (
            @Value("${telegram.bot.token}") String botToken,
            RPGBot rpgBot) {
        this.botToken = botToken;
        this.rpgBot = rpgBot;
    }

    @Override
    public void run (String... args) {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, rpgBot);
            System.out.println("RPGBot está rodando...");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Erro ao iniciar o bot", e);
        }
    }
}
