import view.fx.jfx.JanelaFx;

/**
 * Ponto de entrada da versão gráfica (JavaFX). Fica separado da classe JanelaFx de propósito:
 * uma classe main que estende Application não inicia quando o JavaFX está no module path.
 * Para o console, use Main.
 */
public class Jogar {
    public static void main(String[] args) {
        JanelaFx.iniciar(args);
    }
}
