package model;

/** Nomes dos itens que podem entrar no inventário do personagem. */
public final class Itens {
    public static final String MASCARA_DE_GAS = "Máscara de Gás";
    public static final String RICINA = "Ricina";
    public static final String PASSAPORTES_FALSOS = "Passaportes Falsos";
    public static final String COLETE_BALISTICO = "Colete Balístico";
    public static final String DOSSIE_DO_CARTEL = "Dossiê do Cartel";

    // Preço de cada item comprado (os demais são fabricados ou ganhos, não custam dinheiro)
    public static final int PRECO_MASCARA_DE_GAS = 1000;
    public static final int PRECO_PASSAPORTES_FALSOS = 8000;
    public static final int PRECO_COLETE_BALISTICO = 5000;

    private Itens() { }
}
