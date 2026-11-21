package view.fx;

/**
 * Um parágrafo da história já classificado. {@code orador} e {@code retrato} só existem nas falas;
 * retrato é o id do rosto a mostrar ao lado do texto (ver {@link Personagens}).
 */
public record Trecho(Estilo estilo, String orador, String texto, String retrato) {
    public Trecho(Estilo estilo, String orador, String texto) {
        this(estilo, orador, texto, null);
    }

    public static Trecho de(Estilo estilo, String texto) {
        return new Trecho(estilo, null, texto, null);
    }

    public Trecho comRetrato(String novoRetrato) {
        return new Trecho(estilo, orador, texto, novoRetrato);
    }
}
