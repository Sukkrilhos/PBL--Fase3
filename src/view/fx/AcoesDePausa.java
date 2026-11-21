package view.fx;

/**
 * O que o menu de pausa da janela pede ao jogo. Os métodos são chamados na thread da janela
 * (a thread do jogo está parada esperando a resposta de sempre) e implementados pelo {@link DialogosFx}.
 */
public interface AcoesDePausa {
    /** Salva o ponto em que o jogo está e avisa o jogador do resultado. */
    void salvar();

    /** Abandona a partida e começa outra do zero (o progresso salvo não é apagado). */
    void reiniciar();

    /** Abandona a partida e volta ao menu principal. */
    void sair();
}
