package view.fx;

/** Pede um texto ao jogador. Com {@code padrao} vazio o texto é obrigatório. */
public record PerguntaDeTexto(String titulo, String dica, String padrao) { }
