package br.com.navelogic.telegrambotassistenterpg;

import br.com.navelogic.telegrambotassistenterpg.Model.ResultadoDados;
import br.com.navelogic.telegrambotassistenterpg.Model.RolarDados;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class RPGBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final RolarDados rolarDados;

    public RPGBot(@Value("${telegram.bot.token}") String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.rolarDados = new RolarDados();
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            if (messageText.startsWith("/rolar") || messageText.startsWith("/r")) {
                rolarDados(update.getMessage(), messageText);
            }
        }
    }

    private void rolarDados(Message message, String comando) {
        try {
            ResultadoDados resultado = rolarDados.rolar(comando);
            mandarResultadoDados(message, resultado);
        } catch (IllegalArgumentException e) {
            mandarMensagemErro(message, e.getMessage());
        } catch (Exception e) {
            mandarMensagemErro(message, "Erro ao processar o comando.");
        }
    }

    private void mandarResultadoDados(Message message, ResultadoDados resultado) {
        String responseText = String.format(
                "@%s rolou: \n%s = %d%s",
                message.getFrom().getUserName(),
                resultado.getRepresentacaoVisual(),
                resultado.getTotal(),
                resultado.getMensagemCritico() != null ? resultado.getMensagemCritico() : ""
        );

        SendMessage response = SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text(responseText)
                .parseMode("HTML")
                .build();

        try {
            telegramClient.execute(response);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem", e);
        }
    }

    private void mandarMensagemErro(Message message, String erro) {
        String responseText = String.format(
                "@%s, aconteceu um erro interno...\n%s",
                message.getFrom().getUserName(),
                erro
        );

        SendMessage response = SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text(responseText)
                .parseMode("HTML")
                .build();

        try {
            telegramClient.execute(response);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem", e);
        }
    }
}
