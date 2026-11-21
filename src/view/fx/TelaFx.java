package view.fx;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * O que a janela precisa saber fazer. Todos os métodos são chamados na thread da interface.
 * A implementação real (JavaFX) fica em view.fx.jfx; os testes usam uma tela falsa.
 */
public interface TelaFx {
    /** Atualiza o painel de atributos; null esconde o painel (menus). */
    void atualizarStatus(StatusDoPersonagem status);

    /** Mostra trechos da história sobre o cenário indicado; {@code limpar} começa uma página nova. */
    void exibirPagina(String cenario, List<Trecho> trechos, boolean limpar);

    /** Mostra os botões e completa a resposta com o valor da opção escolhida. */
    void perguntar(Pergunta pergunta, CompletableFuture<Integer> resposta);

    void perguntarTexto(PerguntaDeTexto pergunta, CompletableFuture<String> resposta);

    /** Liga o botão e o menu de pausa da janela às ações do jogo. Telas sem pausa (testes) podem ignorar. */
    default void definirAcoesDePausa(AcoesDePausa acoes) { }

    /** Aviso rápido que some sozinho (conquistas, erros de gravação...). */
    void mostrarAviso(String texto);
}
