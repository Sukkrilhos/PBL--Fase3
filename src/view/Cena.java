package view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Um trecho da história como DADO (linhas, opções de escolha e prompt), sem decidir como aparece:
 * o console imprime, a janela JavaFX desenha. O cenário é só uma dica de arte de fundo.
 */
public final class Cena {
    public enum Tipo { LINHA, OPCAO, PROMPT }

    /** {@code disponivel} é false numa opção que o jogador não pode escolher agora (ex.: compra sem dinheiro). */
    public record Item(Tipo tipo, int numero, String texto, boolean disponivel) {
        public Item(Tipo tipo, int numero, String texto) {
            this(tipo, numero, texto, true);
        }
    }

    private final String cenario;
    private final List<Item> itens = new ArrayList<>();

    public Cena(String cenario) {
        this.cenario = cenario;
    }

    public Cena linha(String texto) {
        itens.add(new Item(Tipo.LINHA, 0, texto));
        return this;
    }

    public Cena opcao(int numero, String texto) {
        itens.add(new Item(Tipo.OPCAO, numero, texto));
        return this;
    }

    /** Opção que aparece, mas não pode ser escolhida (a interface a mostra desativada). */
    public Cena opcaoIndisponivel(int numero, String texto) {
        itens.add(new Item(Tipo.OPCAO, numero, texto, false));
        return this;
    }

    public Cena prompt(String texto) {
        itens.add(new Item(Tipo.PROMPT, 0, texto));
        return this;
    }

    public String getCenario() { return cenario; }

    public List<Item> getItens() { return Collections.unmodifiableList(itens); }

    public boolean estaVazia() { return itens.isEmpty(); }

    public List<Item> getOpcoes() {
        List<Item> opcoes = new ArrayList<>();
        for (Item item : itens) {
            if (item.tipo() == Tipo.OPCAO) opcoes.add(item);
        }
        return opcoes;
    }
}
