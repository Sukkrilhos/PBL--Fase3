package view.fx;

/** Lançada na thread do jogo quando o jogador fecha a janela: desfaz a pilha e encerra a partida. */
public class JogoEncerradoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JogoEncerradoException() {
        super("A janela do jogo foi fechada");
    }
}
