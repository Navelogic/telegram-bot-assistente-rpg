package br.com.navelogic.telegrambotassistenterpg.Model;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ResultadoDados {
    private final Integer total;
    private final String representacaoVisual;
    private final String mensagemCritico;
}
