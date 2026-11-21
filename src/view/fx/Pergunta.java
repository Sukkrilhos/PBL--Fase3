package view.fx;

import java.util.List;

/**
 * Uma pergunta ao jogador, que a interface desenha como botões.
 * MENU: tela inicial; PAINEL: cartão central com título e texto; CENA: escolhas sobre a história;
 * CONTINUAR: botão único para seguir a leitura; FIM: botões no canto inferior, com a mensagem de
 * Game Over ou de fim da campanha ainda visível na tela.
 */
public record Pergunta(Modo modo, String titulo, List<String> paragrafos, List<OpcaoFx> opcoes) {
    public enum Modo { MENU, PAINEL, CENA, CONTINUAR, FIM }
}
