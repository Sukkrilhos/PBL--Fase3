package view.fx;

import model.Antecedentes;

import java.text.Normalizer;
import java.util.List;

/**
 * Decide qual rosto (arquivo assets/personagens/&lt;id&gt;.png) mostrar para cada pessoa da história.
 * O protagonista tem um rosto por origem; os demais têm um rosto fixo. Quem ainda não tem rosto
 * recebe o retrato "desconhecido", para a janela nunca ficar sem imagem na fala.
 */
public final class Personagens {
    public static final String DESCONHECIDO = "desconhecido";

    /** Todos os retratos que o jogo espera encontrar em assets/personagens. */
    public static final List<String> TODOS = List.of(
            "valter_professor", "valter_industrial", "valter_precavido",
            "marisa", "kadu", "heitor", "enzo", "fidalgo", "maluco", DESCONHECIDO);

    private Personagens() { }

    /** O rosto do protagonista depende da origem escolhida; origem desconhecida usa o do professor. */
    public static String retratoDoProtagonista(String antecedente) {
        if (Antecedentes.EX_QUIMICO_INDUSTRIAL.equals(antecedente)) return "valter_industrial";
        if (Antecedentes.QUIMICO_PRECAVIDO.equals(antecedente)) return "valter_precavido";
        return "valter_professor";
    }

    /**
     * Rosto de quem fala. Os personagens fixos têm prioridade sobre o nome do protagonista (que o jogador pode
     * trocar): assim, um jogador chamado "Marisa" não vira a esposa. O protagonista é "VALTER" ou o nome escolhido.
     */
    public static String retratoDoOrador(String orador, StatusDoPersonagem status) {
        String nome = normalizar(orador);
        switch (nome) {
            case "MARISA": return "marisa";
            case "KADU": return "kadu";
            case "HEITOR": return "heitor";
            case "ENZO": return "enzo";
            case "FIDALGO":
            case "O FIDALGO": return "fidalgo";
            case "MALUCO": return "maluco";
            default:
        }
        boolean protagonista = nome.equals("VALTER") || (status != null && nome.equals(normalizar(status.nome())));
        if (protagonista) return retratoDoProtagonista(status == null ? null : status.antecedente());
        return DESCONHECIDO;
    }

    static String normalizar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.strip().toUpperCase();
    }
}
