package view.fx;

import view.Cena;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforma as linhas de uma {@link Cena} (escritas para o console) em trechos classificados
 * para a janela: some com as molduras de "=====", destaca títulos, falas e efeitos.
 */
public final class FormatadorDeCena {
    private static final Pattern MOLDURA = Pattern.compile("^=+$");
    private static final Pattern TITULO_ENTRE_IGUAIS = Pattern.compile("^=+\\s*(.+?)\\s*=+$");
    private static final Pattern CAPITULO = Pattern.compile("^CAPÍTULO\\b.*");
    private static final Pattern PROLOGO = Pattern.compile("^PRÓLOGO\\b.*");
    private static final Pattern TEMPORADA = Pattern.compile("^TEMPORADA \\d+\\s*:.*");
    private static final Pattern ENTRE_COLCHETES = Pattern.compile("^\\[(.*)]$");
    private static final Pattern FALA = Pattern.compile("^([A-ZÀ-ÖØ-Ý][A-ZÀ-ÖØ-Ý0-9 /.'-]{1,30}):\\s+(.*)$");

    private FormatadorDeCena() { }

    public static List<Trecho> formatar(Cena cena) {
        List<Trecho> trechos = new ArrayList<>();
        for (Cena.Item item : cena.getItens()) {
            if (item.tipo() != Cena.Tipo.LINHA) continue;
            for (String linha : item.texto().split("\n")) {
                Trecho trecho = classificar(linha.strip());
                if (trecho == null) continue;
                if (continuaFraseAnterior(trechos, trecho)) {
                    Trecho anterior = trechos.remove(trechos.size() - 1);
                    trechos.add(Trecho.de(Estilo.NARRACAO, anterior.texto() + " " + trecho.texto()));
                } else {
                    trechos.add(trecho);
                }
            }
        }
        return trechos;
    }

    // No console o texto foi quebrado à mão, às vezes no meio da frase; na janela isso vira um só parágrafo
    private static boolean continuaFraseAnterior(List<Trecho> trechos, Trecho atual) {
        if (trechos.isEmpty() || atual.estilo() != Estilo.NARRACAO) return false;
        Trecho anterior = trechos.get(trechos.size() - 1);
        if (anterior.estilo() != Estilo.NARRACAO) return false;

        char ultimo = anterior.texto().charAt(anterior.texto().length() - 1);
        boolean anteriorSemFim = Character.isLetterOrDigit(ultimo) || ",;:".indexOf(ultimo) >= 0;
        boolean atualComecaMinusculo = Character.isLowerCase(atual.texto().charAt(0));
        return anteriorSemFim || atualComecaMinusculo;
    }

    /** Devolve null para o que não deve aparecer (linhas vazias e molduras). */
    static Trecho classificar(String linha) {
        if (linha.isEmpty() || MOLDURA.matcher(linha).matches()) return null;

        Matcher titulo = TITULO_ENTRE_IGUAIS.matcher(linha);
        if (titulo.matches()) return Trecho.de(Estilo.TITULO_CAPITULO, titulo.group(1));
        if (CAPITULO.matcher(linha).matches() || PROLOGO.matcher(linha).matches()) return Trecho.de(Estilo.TITULO_CAPITULO, linha);
        if (TEMPORADA.matcher(linha).matches()) return Trecho.de(Estilo.TITULO_TEMPORADA, linha);

        Matcher colchetes = ENTRE_COLCHETES.matcher(linha);
        if (colchetes.matches()) {
            String interior = colchetes.group(1);
            if (interior.startsWith("Efeito") || interior.startsWith("ITEM")) return Trecho.de(Estilo.EFEITO, interior);
            if (interior.startsWith("FIM") || interior.startsWith("ROTA")) return Trecho.de(Estilo.FIM, interior);
            return Trecho.de(Estilo.SISTEMA, interior);
        }

        Matcher fala = FALA.matcher(linha);
        if (fala.matches()) {
            String orador = fala.group(1);
            if (orador.startsWith("ROTA ") || orador.equals("GAME OVER")) return Trecho.de(Estilo.FIM, linha);
            return new Trecho(Estilo.FALA, orador, fala.group(2));
        }

        if (linha.endsWith("?") && linha.length() <= 90) return Trecho.de(Estilo.PERGUNTA, linha);
        return Trecho.de(Estilo.NARRACAO, linha);
    }
}
