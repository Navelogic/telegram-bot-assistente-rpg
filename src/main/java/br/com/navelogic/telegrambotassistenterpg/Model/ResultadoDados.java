package br.com.navelogic.telegrambotassistenterpg.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Uma classe que representa o resultado de uma rolagem de dados em um jogo de RPG.
 * Ela contém o valor total da rolagem, uma representação visual da rolagem,
 * e uma descrição opcional de acerto crítico.
 * 
 * <p>Esta classe fornece construtores para criar um objeto ResultadoDados com
 * o total e a representação visual especificados, e opcionalmente uma descrição de acerto crítico.
 * Também sobrescreve o método toString para fornecer uma representação de string formatada
 * do resultado.</p>
 * 
 * <p>Exemplo de uso:</p>
 * <pre>
 * {@code
 * ResultadoDados resultado = new ResultadoDados(15, "(5+5+5)");
 * System.out.println(resultado);
 * }
 * </pre>
 * 
 * <p>Saída:</p>
 * <pre>
 * 🎲 Total: 15
 * 
 * Dados: 5+5+5
 * </pre>
 * 
 * <p>Se uma descrição de acerto crítico for fornecida, ela será incluída na saída.</p>
 * 
 * <p>Exemplo de uso com acerto crítico:</p>
 * <pre>
 * {@code
 * ResultadoDados resultado = new ResultadoDados(20, "(10+10)", "Acerto Crítico!");
 * System.out.println(resultado);
 * }
 * </pre>
 * 
 * <p>Saída:</p>
 * <pre>
 * 🎲 Total: 20
 * 
 * Dados: 10+10
 * 
 * Acerto Crítico!
 * </pre>
 * 
 * @param total  o valor total da rolagem de dados
 * @param visual a representação visual da rolagem de dados
 * @param critico a descrição de um acerto crítico (opcional)
 */
@Data
@AllArgsConstructor
public class ResultadoDados {
    private final Integer total;
    private final String visual;
    private final String critico;

    public ResultadoDados(int total, String visual) {
        this.total = total;
        this.visual = visual;
        this.critico = "";
    }

        @Override
        public String toString() {
            String resultado = String.format("🎲 Total: %d\n\nDados: %s\n", 
                total, 
                visual.replaceAll("[()]", "")
            );
            
            if (!critico.isEmpty()) {
                resultado += "\n" + critico;
            }
            
            return resultado;
        }
    }
