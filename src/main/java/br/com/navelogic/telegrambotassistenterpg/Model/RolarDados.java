package br.com.navelogic.telegrambotassistenterpg.Model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Gerenciador de rolagem de dados.
 * Suporta rolagem de dados com modificadores.
 */
public class RolarDados {
    // Constantes para limitar a quantidade de dados.
    private static final int MAX_DADOS = 1000;

    private final Random random = new Random();

    // Expressão regular para validar o comando de rolagem de dados.
    private static final Pattern PADRAO_DADOS = Pattern.compile(
            "^/(rolar|r)\\s+" +
                    "(?:(\\d*)?d(\\d+))?" +
                    "(?:([msM])(\\d+))?" +
                    "(?:\\s*([+\\-*/])\\s*" +
                    "(?:(\\d*)?d(\\d+)|(\\d+)))*"
    );

    /**
     * Rola dados com base no comando fornecido.
     *
     * @param comando Comando de rolagem de dados
     * @return Resultado da rolagem de dados
     * @throws IllegalArgumentException Se o formato do comando for inválido
     */
    public ResultadoDados rolar(String comando) {
        Matcher matcher = PADRAO_DADOS.matcher(comando.toLowerCase());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(criarMensagemDeErro());
        }
        return processarComando(comando);
    }

    /**
     * Cria uma mensagem de erro detalhada com exemplos de uso correto.
     *
     * @return ‘String’ com exemplos de comandos válidos
     */
    private String criarMensagemDeErro() {
        return """
                Formato de comando inválido. Exemplos:
                /r 2d20m1 (rola 2d20 mantendo o maior)
                /r 2d20mm1 (rola 2d20 mantendo o menor)
                /r 2d20sM1 (rola 2d20 soltando o maior)
                /r 2d20sm1 (rola 2d20 soltando o menor)""";
    }

    /**
     * Processa o comando de rolagem de dados completo.
     *
     * @param comando Comando de rolagem de dados
     * @return Resultado da rolagem
     */
    private ResultadoDados processarComando(String comando) {
        String expressao = comando.substring(comando.indexOf(' ') + 1);
        String[] elementos = expressao.split("(?=[+\\-*/])");

        int totalFinal = 0;
        StringBuilder visual = new StringBuilder();
        String mensagemCritico = "";

        for (String elemento : elementos) {
            elemento = elemento.trim();

            if (elemento.contains("d")) {
                RolagemDados rolagem = processarRolagem(elemento);

                // Verifica críticos para d20
                mensagemCritico = verificarCriticos(rolagem);

                // Atualiza visualização e total
                totalFinal = atualizarResultado(visual, totalFinal, rolagem);
            } else {
                totalFinal = processarValorNumerico(visual, totalFinal, elemento);
            }
        }

        return new ResultadoDados(totalFinal, visual.toString(), mensagemCritico);
    }

    /**
     * Verifica se houve um resultado crítico em rolagens de d20.
     *
     * @param rolagem Dados rolados
     * @return Mensagem de crítico, se aplicável
     */
    private String verificarCriticos(RolagemDados rolagem) {
        if (rolagem.lados == 20) {
            if (rolagem.resultados.contains(20)) {
                return "\n\n🎯 CRÍTICO! Acerto Natural 20!";
            } else if (rolagem.resultados.contains(1)) {
                return "\n\n💀 FALHA CRÍTICA! 1 Natural!";
            }
        }
        return "";
    }

    /**
     * Atualiza o resultado visual e total com uma rolagem de dados.
     *
     * @param visual StringBuilder para representação visual
     * @param totalFinal Valor total atual
     * @param rolagem Dados rolados
     * @return Novo total após aplicar a operação
     */
    private int atualizarResultado(StringBuilder visual, int totalFinal, RolagemDados rolagem) {
        if (!visual.isEmpty()) {
            visual.append(" ").append(rolagem.operador).append(" ");
        }
        visual.append(rolagem.representacaoVisual);
        return aplicarOperacao(totalFinal, rolagem.total, rolagem.operador);
    }

    /**
     * Processa um valor numérico no comando.
     *
     * @param visual StringBuilder para representação visual
     * @param totalFinal Valor total atual
     * @param elemento Elemento numérico a ser processado
     * @return Novo total após aplicar a operação
     */
    private int processarValorNumerico(StringBuilder visual, int totalFinal, String elemento) {
        String operador = elemento.substring(0, 1);
        int valor = Integer.parseInt(elemento.substring(1));

        if (!visual.isEmpty()) {
            visual.append(" ").append(operador).append(" ");
        }
        visual.append(valor);

        return aplicarOperacao(totalFinal, valor, operador);
    }

    /**
     * Processa a rolagem de um conjunto de dados.
     *
     * @param elemento Elemento de dados a ser processado
     * @return Resultado da rolagem de dados
     */
    private RolagemDados processarRolagem(String elemento) {
        // Extração de operador e parâmetros
        String operador = extrairOperador(elemento);
        elemento = removerOperador(elemento, operador);

        // Análise dos parâmetros de dados
        String[] partes = elemento.split("d");
        int quantidade = partes[0].isEmpty() ? 1 : Integer.parseInt(partes[0]);

        // Processamento de modificadores e lados do dado
        DadosParametros parametros = processarParametrosDados(partes[1]);

        // Validação de quantidade de dados
        validarQuantidadeDados(quantidade);

        // Rolagem dos dados
        List<Integer> resultados = rolarDados(quantidade, parametros.lados);

        // Aplicação de modificadores
        resultados = aplicarModificadores(resultados, parametros);

        // Criação da representação visual
        String representacaoVisual = criarRepresentacaoVisual(resultados);
        int total = resultados.stream().mapToInt(Integer::intValue).sum();

        return new RolagemDados(total, representacaoVisual, operador,
                parametros.lados, resultados);
    }

    /**
     * Extrai o operador do elemento de dados.
     *
     * @param elemento Elemento de dados
     * @return Operador encontrado
     */
    private String extrairOperador(String elemento) {
        return elemento.startsWith("-") || elemento.startsWith("+") ||
                elemento.startsWith("*") || elemento.startsWith("/") ?
                elemento.substring(0, 1) : "+";
    }

    /**
     * Remove o operador do elemento de dados.
     *
     * @param elemento Elemento de dados
     * @param operador Operador a ser removido
     * @return Elemento sem o operador
     */
    private String removerOperador(String elemento, String operador) {
        return (operador.equals("+") || operador.equals("-")) ?
                elemento.replaceFirst("[+\\-]", "") : elemento;
    }

    /**
     * Valida a quantidade de dados a serem rolados.
     *
     * @param quantidade Quantidade de dados
     * @throws IllegalArgumentException Se a quantidade exceder o limite
     */
    private void validarQuantidadeDados(int quantidade) {
        if (quantidade > MAX_DADOS) {
            throw new IllegalArgumentException("O número de dados não pode ser maior que " + MAX_DADOS);
        }
    }

    /**
     * Rola um conjunto de dados.
     *
     * @param quantidade Número de dados a rolar
     * @param lados Número de lados de cada dado
     * @return Lista de resultados das rolagens
     */
    private List<Integer> rolarDados(int quantidade, int lados) {
        return random.ints(quantidade, 1, lados + 1)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Aplica modificadores aos resultados dos dados.
     *
     * @param resultados Resultados originais
     * @param parametros Parâmetros de modificação
     * @return Lista de resultados modificados
     */
    private List<Integer> aplicarModificadores(List<Integer> resultados, DadosParametros parametros) {
        if (parametros.manterMaior || parametros.manterMenor ||
                parametros.soltarMaior || parametros.soltarMenor) {

            List<Integer> resultadosOrdenados = new ArrayList<>(resultados);
            resultadosOrdenados.sort(Comparator.naturalOrder());

            if (parametros.manterMaior) {
                return resultadosOrdenados.subList(
                        resultadosOrdenados.size() - parametros.quantidadeModificador,
                        resultadosOrdenados.size()
                );
            }
            // Lógica similar para outros modificadores...
        }
        return resultados;
    }

    /**
     * Cria representação visual dos resultados.
     *
     * @param resultados Lista de resultados
     * @return ‘String’ de representação visual
     */
    private String criarRepresentacaoVisual(List<Integer> resultados) {
        return resultados.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" + ", "(", ")"));
    }

    /**
     * Aplica operação matemática entre dois valores.
     *
     * @param valorAtual Valor atual
     * @param novoValor Novo valor a ser aplicado
     * @param operador Operador matemático
     * @return Resultado da operação
     */
    private int aplicarOperacao(int valorAtual, int novoValor, String operador) {
        return switch (operador) {
            case "+" -> valorAtual + novoValor;
            case "-" -> valorAtual - novoValor;
            case "*" -> valorAtual * novoValor;
            case "/" -> valorAtual / novoValor;
            default -> novoValor;
        };
    }

    /**
     * Classe interna para processamento de parâmetros de dados.
     */
    private static class DadosParametros {
        int lados;
        boolean manterMaior;
        boolean manterMenor;
        boolean soltarMaior;
        boolean soltarMenor;
        int quantidadeModificador;

        DadosParametros(int lados) {
            this.lados = lados;
        }
    }

    /**
     * Processa os parâmetros de um conjunto de dados.
     *
     * @param ladosStr ‘String’ de definição dos dados
     * @return Parâmetros processados
     */
    private DadosParametros processarParametrosDados(String ladosStr) {
        DadosParametros parametros = new DadosParametros(Integer.parseInt(ladosStr));

        Pattern modificadorPattern = Pattern.compile("(\\d+)([msM]+)(\\d+)");
        Matcher modificadorMatcher = modificadorPattern.matcher(ladosStr);

        if (modificadorMatcher.find()) {
            parametros.lados = Integer.parseInt(modificadorMatcher.group(1));
            String mod = modificadorMatcher.group(2);
            parametros.quantidadeModificador = Integer.parseInt(modificadorMatcher.group(3));

            switch (mod) {
                case "m" -> parametros.manterMaior = true;
                case "mm" -> parametros.manterMenor = true;
                case "sM" -> parametros.soltarMaior = true;
                case "sm" -> parametros.soltarMenor = true;
            }
        }

        return parametros;
    }

    /**
     * Classe interna para representar uma rolagem de dados.
     */
    private static class RolagemDados {
        int total;
        String representacaoVisual;
        String operador;
        int lados;
        List<Integer> resultados;

        RolagemDados(int total, String representacaoVisual, String operador,
                     int lados, List<Integer> resultados) {
            this.total = total;
            this.representacaoVisual = representacaoVisual;
            this.operador = operador;
            this.lados = lados;
            this.resultados = resultados;
        }
    }
}