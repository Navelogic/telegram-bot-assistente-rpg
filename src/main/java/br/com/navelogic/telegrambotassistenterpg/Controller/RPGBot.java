package br.com.navelogic.telegrambotassistenterpg.Controller;

import br.com.navelogic.telegrambotassistenterpg.Service.RolarDadosService;
import br.com.navelogic.telegrambotassistenterpg.Service.StartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Bot de Telegram de uso geral.
 *
 * Gerencia comandos pelo Telegram, processando
 * e retornando dados para os usuários.
 */
@Slf4j
@Component
public class RPGBot implements LongPollingSingleThreadUpdateConsumer {
    // Comandos
    private static final String COMANDO_START = "/start";
    private static final String COMANDO_COMANDOS = "/comandos";
    private static final String COMANDO_C = "/c";
    private static final String COMANDO_ROLAR = "/rolar";
    private static final String COMANDO_R = "/r";

    // Clientes e serviços
    private final TelegramClient telegramClient;
    private final StartService startService;
    private final RolarDadosService rolarDadosService;

    /**
     * Construtor que inicializa o bot com um token de autenticação.
     *
     * @param botToken Token de autenticação do Telegram
     */
    public RPGBot(@Value("${telegram.bot.token}") String botToken, 
    StartService startService, 
    RolarDadosService rolarDadosService) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.startService = new StartService();
        this.rolarDadosService = new RolarDadosService();
    }

    /**
     * Processa as atualizações recebidas do Telegram.
     *
     * @param update Atualização recebida do Telegram
     */
    @Override
    public void consume(Update update) {
        processarComando(update.getMessage());
    }

    /**
     * Processa o comando.
     *
     * @param message Mensagem contendo o comando
     */
    private void processarComando(Message message) {
        if (message.getEntities() != null) {
            for (MessageEntity entity : message.getEntities()) {
                if ("bot_command".equals(entity.getType())) {
                    String comando = message.getText().substring(entity.getOffset(), entity.getOffset() + entity.getLength());
                    try {
                        switch (comando) {
                            case COMANDO_START:
                                enviarMensagem(message, startService.start());
                                break;
                            case COMANDO_ROLAR:
                            case COMANDO_R:
                                log.info("Comando detectado: {}", comando);
                                enviarMensagem(message, rolarDadosService.rolar(message.getText()).toString());
                                break;
                            default:
                                log.warn("Comando desconhecido: {}", comando);
                        }
                    } catch (IllegalArgumentException e) {
                        enviarMensagemErro(message, e.getMessage());
                    } catch (Exception e) {
                        enviarMensagemErro(message, "Erro ao processar o comando.");
                    }
                    return;
                }
            }
        }
    }


    /**
     * Envia mensagem de erro para o usuário.
     *
     * @param message Mensagem original
     * @param erro Mensagem de erro
     */
    private void enviarMensagemErro(Message message, String erro) {
        String nomeUsuario = obterNomeExibicao(message);
        String textoErro = String.format(
                "%s, aconteceu um erro interno...%n%s",
                nomeUsuario,
                erro
        );
        enviarMensagem(message, textoErro);
    }

    /**
     * Obtém o nome de exibição do usuário.
     *
     * @param message Mensagem do usuário
     * @return Nome de exibição
     */
    private String obterNomeExibicao(Message message) {
        String userName = message.getFrom().getUserName();
        return (userName != null) ? "@" + userName : message.getFrom().getFirstName();
    }

    /**
     * Envia mensagem para o chat do Telegram.
     *
     * @param message Mensagem original
     * @param texto Texto a ser enviado
     */
    private void enviarMensagem(Message message, String texto) {
        SendMessage response = SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text(texto)
                .parseMode("HTML")
                .build();

        try {
            telegramClient.execute(response);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem", e);
        }
    }
}