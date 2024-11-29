/**
 * Classe de serviço responsável por lidar com comandos de rolagem de dados.
 * Este serviço processa comandos para rolar dados, aplicar modificadores e calcular resultados.
 * 
 * Exemplos de comandos:
 * - /r 2d20m1 (rola 2d20 mantendo o maior)
 * - /r 2d20mm1 (rola 2d20 mantendo o menor)
 * - /r 2d20sM1 (rola 2d20 soltando o maior)
 * - /r 2d20sm1 (rola 2d20 soltando o menor)
 * 
 * 
 * O serviço suporta várias operações e modificadores:
 * - m: Manter o maior
 * - mm: Manter o menor
 * - sM: Soltar o maior
 * - sm: Soltar o menor
 * 
 * O serviço registra as etapas de processamento e resultados usando SLF4J.
 * 
 * Métodos:
 * - rolar(String comando): Processa o comando de rolagem de dados e retorna o resultado.
 * - mensagemErro(): Retorna uma mensagem de erro para comandos inválidos.
 * - extrairExpressao(String comando): Extrai a expressão do comando.
 * - processarComando(String expressao): Processa a expressão do comando e calcula o resultado.
 * - processarRolagem(String elemento): Processa um elemento de rolagem de dados e aplica modificadores.
 * - extrairOperador(String elemento): Extrai o operador do elemento.
 * - removerOperador(String elemento, String operador): Remove o operador do elemento.
 * - validarQuantidadeDados(int quantidade): Valida a quantidade de dados.
 * - processarParametrosDados(String ladosStr): Processa os parâmetros e modificadores dos dados.
 * - rolarDados(int quantidade, int lados): Rola a quantidade especificada de dados com os lados dados.
 * - aplicarModificadores(List<Integer> resultados, DadosParametros parametros): Aplica os modificadores especificados aos resultados dos dados.
 * - criarRepresentacaoVisual(List<Integer> resultados): Cria uma representação visual dos resultados dos dados.
 * - atualizarResultado(StringBuilder visual, int totalAtual, RolagemDados rolagem): Atualiza o resultado total com a nova rolagem.
 * - processarValorNumerico(StringBuilder visual, int totalAtual, String elemento): Processa um elemento de valor numérico.
 * - aplicarOperacao(int valorAtual, int novoValor, String operador): Aplica a operação especificada aos valores atual e novo.
 * 
 * Classes Internas:
 * - DadosParametros: Contém os parâmetros para rolagem de dados, incluindo lados e modificadores.
 * - RolagemDados: Representa o resultado de uma rolagem de dados, incluindo total, representação visual e operador.
 */
package br.com.navelogic.telegrambotassistenterpg.Service;

import br.com.navelogic.telegrambotassistenterpg.Model.Modificador;
import br.com.navelogic.telegrambotassistenterpg.Model.ResultadoDados;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;

@Service
public class RolarDadosService {

    private final Random random = new Random();
    private static final int MAX_DADOS = 1000;
    private static final Logger logger = LoggerFactory.getLogger(RolarDadosService.class);

    private static final Pattern PADRAO_DADOS = Pattern.compile(
            "^/(rolar|r)\\s+" +
                    "(?:(\\d*)?d(\\d+))?" +
                    "(?:([msM]+)(\\d+))?" +
                    "(?:\\s*([+\\-*/])\\s*" +
                    "(?:(?:(\\d*)?d(\\d+))|(?:(\\d+))))*"
    );

    public ResultadoDados rolar(String comando) {
        logger.debug("Recebido comando: {}", comando);
        Matcher matcher = PADRAO_DADOS.matcher(comando.toLowerCase());
        if (!matcher.matches()) {
            logger.error("Comando inválido: {}", comando);
            throw new IllegalArgumentException(mensagemErro());
        }
        String expressao = extrairExpressao(comando);
        logger.debug("Expressão extraída: {}", expressao);
        return processarComando(expressao);
    }

    private String mensagemErro() {
        return """
                Formato de comando inválido. Exemplos:
                /r 2d20m1 (rola 2d20 mantendo o maior)
                /r 2d20mm1 (rola 2d20 mantendo o menor)
                /r 2d20sM1 (rola 2d20 soltando o maior)
                /r 2d20sm1 (rola 2d20 soltando o menor)""";
    }

    private String extrairExpressao(String comando) {
        return comando.substring(comando.indexOf(' ') + 1).trim();
    }

    private ResultadoDados processarComando(String expressao) {
        logger.debug("Processando comando: {}", expressao);
        String[] elementos = expressao.split("(?=[+\\-*/])");
        int totalFinal = 0;
        StringBuilder visual = new StringBuilder();

        for (String elemento : elementos) {
            elemento = elemento.trim();
            logger.debug("Processando elemento: {}", elemento);
            if (elemento.contains("d")) {
                RolagemDados rolagem = processarRolagem(elemento);
                totalFinal = atualizarResultado(visual, totalFinal, rolagem);
            } else {
                totalFinal = processarValorNumerico(visual, totalFinal, elemento);
            }
        }

        logger.debug("Resultado final: total={}, visual={}", totalFinal, visual);
        return new ResultadoDados(totalFinal, visual.toString());
    }

    private RolagemDados processarRolagem(String elemento) {
        logger.debug("Processando rolagem: {}", elemento);
        String operador = extrairOperador(elemento);
        elemento = removerOperador(elemento, operador);

        String[] partes = elemento.split("d");
        int quantidade = partes[0].isEmpty() ? 1 : Integer.parseInt(partes[0]);
        validarQuantidadeDados(quantidade);

        DadosParametros parametros = processarParametrosDados(partes[1]);
        List<Integer> resultados = rolarDados(quantidade, parametros.lados);
        logger.debug("Resultados antes de modificadores: {}", resultados);
        resultados = aplicarModificadores(resultados, parametros);

        String representacaoVisual = criarRepresentacaoVisual(resultados);
        int total = resultados.stream().mapToInt(Integer::intValue).sum();
        logger.debug("Rolagem processada: total={}, visual={}", total, representacaoVisual);

        return new RolagemDados(total, representacaoVisual, operador, parametros.lados, resultados);
    }

    private String extrairOperador(String elemento) {
        return elemento.startsWith("-") || elemento.startsWith("+") || 
               elemento.startsWith("*") || elemento.startsWith("/") ? 
               elemento.substring(0, 1) : "+";
    }

    private String removerOperador(String elemento, String operador) {
        return operador.equals("+") || operador.equals("-") ? 
               elemento.replaceFirst("[+\\-]", "") : elemento;
    }

    private void validarQuantidadeDados(int quantidade) {
        if (quantidade > MAX_DADOS) {
            throw new IllegalArgumentException("O número de dados não pode ser maior que " + MAX_DADOS);
        }
    }

    private DadosParametros processarParametrosDados(String ladosStr) {
        logger.debug("Processando parâmetros dos dados: {}", ladosStr);
        Matcher matcher = Pattern.compile("(\\d+)([msM]+\\d+)?").matcher(ladosStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Formato inválido para a definição de dados: " + ladosStr);
        }
    
        int lados = Integer.parseInt(matcher.group(1));
        DadosParametros parametros = new DadosParametros(lados);
    
        String modificadorStr = matcher.group(2);
        if (modificadorStr != null) {
            Matcher modificadorMatcher = Pattern.compile("([msM]+)(\\d+)").matcher(modificadorStr);
            if (modificadorMatcher.find()) {
                String mod = modificadorMatcher.group(1);
                parametros.quantidadeModificador = Integer.parseInt(modificadorMatcher.group(2));
    
                switch (mod) {
                    case "m" -> parametros.modificador = Modificador.MANTER_MAIOR;
                    case "mm" -> parametros.modificador = Modificador.MANTER_MENOR;
                    case "sM" -> parametros.modificador = Modificador.SOLTAR_MAIOR;
                    case "sm" -> parametros.modificador = Modificador.SOLTAR_MENOR;
                    default -> {
                        logger.error("Modificador inválido: {}", mod);
                        throw new IllegalArgumentException("Modificador inválido: " + mod);
                    }
                }
            }
        }
    
        logger.debug("Parâmetros processados: lados={}, modificador={}, quantidadeModificador={}",
                lados, parametros.modificador, parametros.quantidadeModificador);
        return parametros;
    }    

    private List<Integer> rolarDados(int quantidade, int lados) {
        logger.debug("Rolando {} dados de {} lados", quantidade, lados);
        if (lados <= 0) {
            logger.error("Número de lados inválido: {}", lados);
            throw new IllegalArgumentException("O número de lados deve ser maior que zero.");
        }
        List<Integer> resultados = random.ints(quantidade, 1, lados + 1)
                .boxed()
                .collect(Collectors.toList());
        logger.debug("Resultados da rolagem: {}", resultados);
        return resultados;
    }
    

    private List<Integer> aplicarModificadores(List<Integer> resultados, DadosParametros parametros) {
        if (parametros.modificador != null) {
            logger.debug("Aplicando modificador {} com limite {}", parametros.modificador, parametros.quantidadeModificador);
            resultados.sort(parametros.modificador.getComparator());
            int limite = Math.min(parametros.quantidadeModificador, resultados.size());

            switch (parametros.modificador) {
                case MANTER_MAIOR:
                    resultados = resultados.subList(resultados.size() - limite, resultados.size());
                    break;
                case MANTER_MENOR:
                    resultados = resultados.subList(0, limite);
                    break;
                case SOLTAR_MAIOR:
                    resultados = resultados.subList(0, resultados.size() - limite);
                    break;
                case SOLTAR_MENOR:
                    resultados = resultados.subList(limite, resultados.size());
                    break;
                default:
                    logger.error("Modificador inválido: {}", parametros.modificador);
                    throw new IllegalArgumentException("Modificador inválido");
            }
        }
        logger.debug("Resultados após modificadores: {}", resultados);
        return resultados;
    }

    private String criarRepresentacaoVisual(List<Integer> resultados) {
        return resultados.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" + "));
    }    

    private int atualizarResultado(StringBuilder visual, int totalAtual, RolagemDados rolagem) {
        logger.debug("Atualizando resultado: totalAtual={}, rolagem={}", totalAtual, rolagem);
        if (visual.length() > 0) {
            visual.append(" ").append(rolagem.operador).append(" ");
        }
        visual.append(rolagem.representacaoVisual);
        int novoTotal = aplicarOperacao(totalAtual, rolagem.total, rolagem.operador);
        logger.debug("Novo total após atualização: {}", novoTotal);
        return novoTotal;
    }

    private int processarValorNumerico(StringBuilder visual, int totalAtual, String elemento) {
        String operador = elemento.substring(0, 1);
        int valor = Integer.parseInt(elemento.substring(1));
        if (visual.length() > 0) {
            visual.append(" ").append(operador).append(" ");
        }
        visual.append(valor);
        return aplicarOperacao(totalAtual, valor, operador);
    }

    private int aplicarOperacao(int valorAtual, int novoValor, String operador) {
        return switch (operador) {
            case "+" -> valorAtual + novoValor;
            case "-" -> valorAtual - novoValor;
            case "*" -> valorAtual * novoValor;
            case "/" -> valorAtual / novoValor;
            default -> novoValor;
        };
    }

    private static class DadosParametros {
        int lados;
        int quantidadeModificador = 0;
        Modificador modificador = null;

        DadosParametros(int lados) {
            this.lados = lados;
        }
    }

    private static class RolagemDados {
        int total;
        String representacaoVisual;
        String operador;

        RolagemDados(int total, String representacaoVisual, String operador, int lados, List<Integer> resultados) {
            this.total = total;
            this.representacaoVisual = representacaoVisual;
            this.operador = operador;
        }
    }
}
