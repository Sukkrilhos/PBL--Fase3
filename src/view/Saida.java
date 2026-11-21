package view;

/** Destino das cenas da história: cada interface (console, JavaFX) decide como exibi-las. */
public interface Saida {
    void exibir(Cena cena);
}
