package view.fx;

/**
 * Um botão: o valor devolvido ao jogo, o rótulo, uma descrição opcional e o id de um retrato opcional
 * (usado, por exemplo, para mostrar o rosto de cada origem). Descrição e retrato podem ser null.
 * {@code disponivel} false desenha o botão desativado: o jogador vê a opção, mas não consegue escolhê-la.
 */
public record OpcaoFx(int valor, String rotulo, String descricao, String retrato, boolean disponivel) {
    public OpcaoFx(int valor, String rotulo, String descricao, String retrato) {
        this(valor, rotulo, descricao, retrato, true);
    }

    public OpcaoFx(int valor, String rotulo, String descricao) {
        this(valor, rotulo, descricao, null, true);
    }

    public static OpcaoFx de(int valor, String rotulo) {
        return new OpcaoFx(valor, rotulo, null, null, true);
    }

    public static OpcaoFx de(int valor, String rotulo, boolean disponivel) {
        return new OpcaoFx(valor, rotulo, null, null, disponivel);
    }
}
