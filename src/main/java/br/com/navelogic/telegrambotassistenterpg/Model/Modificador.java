package br.com.navelogic.telegrambotassistenterpg.Model;

import java.util.Comparator;

public enum Modificador {
    MANTER_MAIOR(Comparator.naturalOrder()),
    MANTER_MENOR(Comparator.naturalOrder()),
    SOLTAR_MAIOR(Comparator.reverseOrder()),
    SOLTAR_MENOR(Comparator.reverseOrder());

    private final Comparator<Integer> comparator;

    Modificador(Comparator<Integer> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Integer> getComparator() {
        return comparator;
    }
}
