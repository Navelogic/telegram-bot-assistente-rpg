package br.com.navelogic.telegrambotassistenterpg.Model;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RolarDados {
    private final Random random = new Random();
    private static final Pattern PADRAO_DADOS = Pattern.compile(
            "^/(rolar|r)\\s+" +
                    "(?:(\\d*)?d(\\d+))?" +
                    "(?:([msM])(\\d+))?" +
                    "(?:\\s*([+\\-*/])\\s*" +
                    "(?:(?:(\\d*)?d(\\d+))|(?:(\\d+))))*"
    );

    /**
     * Nomenclatura dos modificadores:
     * m1 = manter o maior
     * mm1 = manter o menor
     * sM1 = soltar o maior
     * sm1 = soltar o menor
     */
    public ResultadoDados rolar(String comando) {
        Matcher matcher = PADRAO_DADOS.matcher(comando.toLowerCase());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Formato de comando inválido. Use por exemplo:\n" +
                            "/r 2d20m1 (rola 2d20 mantendo o maior)\n" +
                            "/r 2d20mm1 (rola 2d20 mantendo o menor)\n" +
                            "/r 2d20sM1 (rola 2d20 soltando o maior)\n" +
                            "/r 2d20sm1 (rola 2d20 soltando o menor)"
            );
        }
        return processarComando(comando);
    }

    private ResultadoDados processarComando(String comando){
        List<String> partes = new ArrayList<>();
        List<Integer> resultados = new ArrayList<>();
        int totalFinal = 0;
        StringBuilder visual = new StringBuilder();
        String mensagemCritico = "";
        boolean temD20 = false;

        String expressao = comando.substring(comando.indexOf(' ') + 1);
        String[] elementos = expressao.split("(?=[+\\-*/])");

        for (String elemento : elementos) {
            elemento = elemento.trim();
            if (elemento.contains("d")) {
                RolagemDados rolagem = processarRolagem(elemento);

                if (rolagem.lados == 20) {
                    temD20 = true;
                    if (rolagem.resultados.contains(20)) {
                        mensagemCritico = "\n🎯 CRÍTICO! Acerto Natural 20!";
                    } else if (rolagem.resultados.contains(1)) {
                        mensagemCritico = "\n💀 FALHA CRÍTICA! 1 Natural!";
                    }
                }

                if (visual.length() > 0) {
                    visual.append(" ").append(rolagem.operador).append(" ");
                }
                visual.append(rolagem.representacaoVisual);
                totalFinal = aplicarOperacao(totalFinal, rolagem.total, rolagem.operador);

            } else {
                String operador = elemento.substring(0, 1);
                int valor = Integer.parseInt(elemento.substring(1));
                if (visual.length() > 0) {
                    visual.append(" ").append(operador).append(" ");
                }
                visual.append(valor);
                totalFinal = aplicarOperacao(totalFinal, valor, operador);
            }
        }
        return new ResultadoDados(totalFinal, visual.toString(), mensagemCritico);
    }

    private RolagemDados processarRolagem(String elemento){
        String operador = elemento.startsWith("-") || elemento.startsWith("+") ||
                elemento.startsWith("*") || elemento.startsWith("/") ?
                elemento.substring(0, 1) : "+";

        if (operador.equals("+") || operador.equals("-")) {
            elemento = elemento.replaceFirst("[+\\-]", "");
        }

        String[] partes = elemento.split("d");
        int quantidade = partes[0].isEmpty() ? 1 : Integer.parseInt(partes[0]);

        String ladosStr = partes[1];
        int lados;
        boolean manterMaior = false;
        boolean manterMenor = false;
        boolean soltarMaior = false;
        boolean soltarMenor = false;
        int quantidadeManterSoltar = 0;

        if (ladosStr.contains("m") || ladosStr.contains("s")) {
            Pattern modificadorPattern = Pattern.compile("(\\d+)([msM]+)(\\d+)");
            Matcher modificadorMatcher = modificadorPattern.matcher(ladosStr);

            if (modificadorMatcher.find()) {
                lados = Integer.parseInt(modificadorMatcher.group(1));
                String mod = modificadorMatcher.group(2);
                quantidadeManterSoltar = Integer.parseInt(modificadorMatcher.group(3));

                if (mod.equals("m")) manterMaior = true;
                else if (mod.equals("mm")) manterMenor = true;
                else if (mod.equals("sM")) soltarMaior = true;
                else if (mod.equals("sm")) soltarMenor = true;
            } else {
                lados = Integer.parseInt(ladosStr);
            }
        } else {
            lados = Integer.parseInt(ladosStr);
        }

        if (quantidade > 1000) {
            throw new IllegalArgumentException("O número de dados não pode ser maior que 1000");
        }

        List<Integer> resultados = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            resultados.add(random.nextInt(lados) + 1);
        }

        if (manterMaior || manterMenor || soltarMaior || soltarMenor) {
            resultados.sort(null);

            if (manterMaior) {
                resultados = resultados.subList(resultados.size() - quantidadeManterSoltar, resultados.size());
            } else if (manterMenor) {
                resultados = resultados.subList(0, quantidadeManterSoltar);
            } else if (soltarMaior) {
                resultados = resultados.subList(0, resultados.size() - quantidadeManterSoltar);
            } else if (soltarMenor) {
                resultados = resultados.subList(quantidadeManterSoltar, resultados.size());
            }
        }

        int total = resultados.stream().mapToInt(Integer::intValue).sum();

        StringBuilder representacaoVisual = new StringBuilder("(");
        for (int i = 0; i < resultados.size(); i++) {
            if (i > 0) representacaoVisual.append(" + ");
            representacaoVisual.append(resultados.get(i));
        }
        representacaoVisual.append(")");

        return new RolagemDados(total, representacaoVisual.toString(), operador, lados, resultados);
    }

    private int aplicarOperacao(int valorAtual, int novoValor, String operador) {
        switch (operador) {
            case "+": return valorAtual + novoValor;
            case "-": return valorAtual - novoValor;
            case "*": return valorAtual * novoValor;
            case "/": return valorAtual / novoValor;
            default: return novoValor;
        }
    }
    private static class RolagemDados {
        int total;
        String representacaoVisual;
        String operador;
        int lados;
        List<Integer> resultados;

        RolagemDados(int total, String representacaoVisual, String operador, int lados, List<Integer> resultados) {
            this.total = total;
            this.representacaoVisual = representacaoVisual;
            this.operador = operador;
            this.lados = lados;
            this.resultados = resultados;
        }
    }
}
