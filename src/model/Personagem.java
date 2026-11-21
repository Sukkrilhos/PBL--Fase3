package model;

import java.util.ArrayList;
import java.util.List;

public class Personagem {
    private static final int LIMITE_MINIMO = 0;
    private static final int LIMITE_MAXIMO = 100;

    private String nome;
    private String antecedente;
    private int dinheiro;
    private int saude;
    private int moralEtica;
    private int relacaoFamiliar;
    private List<String> inventario;

    public Personagem() {
        this.nome = "Valter";
        this.antecedente = Antecedentes.PROFESSOR_TRADICIONAL;
        this.dinheiro = 0;
        this.saude = 100;
        this.moralEtica = 100;
        this.relacaoFamiliar = 100;
        this.inventario = new ArrayList<>();
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getAntecedente() { return antecedente; }
    public void setAntecedente(String antecedente) { this.antecedente = antecedente; }

    public int getDinheiro() { return dinheiro; }
    public void setDinheiro(int dinheiro) { this.dinheiro = dinheiro; }

    public int getSaude() { return saude; }
    public void setSaude(int saude) { this.saude = saude; }

    public int getMoralEtica() { return moralEtica; }
    public void setMoralEtica(int moralEtica) { this.moralEtica = moralEtica; }

    public int getRelacaoFamiliar() { return relacaoFamiliar; }
    public void setRelacaoFamiliar(int relacaoFamiliar) { this.relacaoFamiliar = relacaoFamiliar; }

    public List<String> getInventario() { return inventario; }

    public void adicionarItem(String item) {
        if (!inventario.contains(item)) {
            inventario.add(item);
        }
    }

    public boolean possuiItem(String item) {
        return inventario.contains(item);
    }

    // Cópia independente (inclui o inventário): o ponto de retomada guarda uma foto do personagem, que não muda
    // quando o jogo segue em frente
    public Personagem copia() {
        Personagem outro = new Personagem();
        outro.nome = nome;
        outro.antecedente = antecedente;
        outro.dinheiro = dinheiro;
        outro.saude = saude;
        outro.moralEtica = moralEtica;
        outro.relacaoFamiliar = relacaoFamiliar;
        outro.inventario = new ArrayList<>(inventario);
        return outro;
    }

    // Só se compra o que o dinheiro cobre: o saldo precisa ser, no mínimo, o preço
    public boolean podePagar(int preco) {
        return dinheiro >= preco;
    }

    // Dinheiro não tem limite; os atributos vitais ficam sempre entre 0 e 100
    public void alterarDinheiro(int valor) { this.dinheiro += valor; }
    public void alterarSaude(int valor) { this.saude = limitar(this.saude + valor); }
    public void alterarMoral(int valor) { this.moralEtica = limitar(this.moralEtica + valor); }
    public void alterarRelacao(int valor) { this.relacaoFamiliar = limitar(this.relacaoFamiliar + valor); }

    private static int limitar(int valor) {
        return Math.min(LIMITE_MAXIMO, Math.max(LIMITE_MINIMO, valor));
    }
}
