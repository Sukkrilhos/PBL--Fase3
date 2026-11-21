package view.fx;

import java.util.List;

/** Cópia imutável dos atributos do personagem, segura para ir da thread do jogo à da janela. */
public record StatusDoPersonagem(String nome, String antecedente, int dinheiro, int saude,
                                 int moralEtica, int relacaoFamiliar, List<String> inventario) { }
