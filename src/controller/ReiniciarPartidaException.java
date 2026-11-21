package controller;

/** Sinal de fluxo: interrompe a campanha em andamento e volta ao início de um novo jogo. */
public class ReiniciarPartidaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReiniciarPartidaException() {
        super("Sinal para reiniciar a partida");
    }
}
