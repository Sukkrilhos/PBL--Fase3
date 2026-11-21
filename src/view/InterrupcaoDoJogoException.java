package view;

/**
 * Lançada na thread do jogo, no lugar da resposta que ele esperava, quando o jogador escolhe no menu de pausa
 * reiniciar a partida ou sair dela. Desfaz a pilha da campanha; quem a trata é o Controlador.
 * (Fechar a janela é outra coisa: ver view.fx.JogoEncerradoException.)
 */
public class InterrupcaoDoJogoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public enum Acao { REINICIAR, SAIR }

    private final Acao acao;

    public InterrupcaoDoJogoException(Acao acao) {
        super("O jogador pediu, na pausa: " + acao);
        this.acao = acao;
    }

    public Acao getAcao() { return acao; }
}
