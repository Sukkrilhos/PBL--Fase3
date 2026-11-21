package controller.temporada;

import model.service.PersonagemService;
import view.Dialogos;
import view.Narrativa;

/**
 * Base das temporadas: guarda as dependências e os passos que se repetem em todas elas.
 * Cada temporada só decide o fluxo (escolhas, consequências, itens, conquistas);
 * todo texto é exibido pela View ({@link Dialogos} e {@link Narrativa}).
 */
public abstract class Temporada {
    protected final Dialogos dialogos;
    protected final Narrativa narrativa;
    protected final PersonagemService personagemService;

    protected Temporada(Dialogos dialogos, Narrativa narrativa, PersonagemService personagemService) {
        this.dialogos = dialogos;
        this.narrativa = narrativa;
        this.personagemService = personagemService;
    }

    /**
     * Lê a escolha de uma cena em que a opção {@code opcaoDeCompra} compra um item. Sem dinheiro para o preço, a
     * compra é recusada e a pergunta se repete: a interface já mostra a opção desativada, e esta checagem
     * garante a regra também no console, onde o jogador pode digitar qualquer número.
     */
    protected int lerOpcaoComCompra(int min, int max, int opcaoDeCompra, int preco) {
        while (true) {
            int escolha = dialogos.lerOpcao(min, max);
            if (escolha != opcaoDeCompra || personagemService.podePagar(preco)) {
                return escolha;
            }
            dialogos.mostrarMensagem("Dinheiro insuficiente: a compra custa R$ " + preco
                    + " e você tem R$ " + personagemService.getPersonagem().getDinheiro() + ". Escolha outra opção.");
        }
    }

    // Desbloqueia a conquista e, se ela for nova, avisa o jogador pela View.
    protected void desbloquearConquista(String id) {
        if (personagemService.desbloquearConquista(id)) {
            dialogos.mostrarConquistaDesbloqueada();
        }
    }

    /**
     * Marca o ponto em que o jogo recomeça se o jogador salvar pelo botão de pausa: {@code proximoCapitulo} é o
     * capítulo que vem a seguir (o número de capítulos da temporada, quando ela acabou). Chamado logo depois das
     * consequências de um capítulo, assim o save nunca repete nem perde o efeito de uma escolha.
     * Num Game Over não se marca nada: o jogador volta ao ponto anterior.
     */
    protected void registrarCheckpoint(int temporada, int proximoCapitulo, int rotaFinal) {
        if (!personagemService.isGameOver()) {
            personagemService.registrarCheckpoint(temporada, proximoCapitulo, rotaFinal);
        }
    }
}
