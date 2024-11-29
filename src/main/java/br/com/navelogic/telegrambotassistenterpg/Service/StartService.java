package br.com.navelogic.telegrambotassistenterpg.Service;

import org.springframework.stereotype.Service;

@Service
public class StartService {
    public String start() {
        return "Olá, eu sou o Assistente de RPG de uso geral criado pelo Navelogic!\n\n" +
                "Estou aqui para te ajudar por esse mundo.\n\n" +
                "Ainda estou em desenvolvimento, mas já tenho uma funcionalidade implementada! Logo vou ter mais algumas funções bem legais por aqui.\n\n" +
                "Por favor, acompanhe todas as novidades e documentações pelo meu Github\n\n" +
                "https://github.com/Navelogic/telegram-bot-assistente-rpg";
    }
}
