package view.fx;

import view.InterrupcaoDoJogoException;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

/**
 * Liga a thread do jogo (que espera respostas, como no console) à thread da interface gráfica
 * (que nunca pode ficar bloqueada). A thread do jogo cria uma resposta, pede à interface para
 * mostrá-la e aguarda; o clique do jogador completa a resposta. Não depende de JavaFX: em produção
 * o executor é Platform::runLater, e nos testes pode ser qualquer Executor.
 */
public final class PonteDeInterface {
    private final Executor executorDaInterface;
    private final Set<CompletableFuture<?>> pendentes = ConcurrentHashMap.newKeySet();
    private volatile boolean encerrada;
    private InterrupcaoDoJogoException interrupcaoSemOuvinte;   // pedida enquanto o jogo não esperava resposta

    public PonteDeInterface(Executor executorDaInterface) {
        this.executorDaInterface = executorDaInterface;
    }

    /** Executa algo na thread da interface. */
    public void executar(Runnable acao) {
        if (encerrada) throw new JogoEncerradoException();
        executorDaInterface.execute(acao);
    }

    /** Cria uma resposta que a interface completará quando o jogador agir. */
    public synchronized <T> CompletableFuture<T> novaResposta() {
        CompletableFuture<T> resposta = new CompletableFuture<>();
        InterrupcaoDoJogoException interrupcao = pegarInterrupcaoPendente();
        if (encerrada) {
            resposta.completeExceptionally(new JogoEncerradoException());
        } else if (interrupcao != null) {
            resposta.completeExceptionally(interrupcao);
        } else {
            pendentes.add(resposta);
            resposta.whenComplete((valor, erro) -> pendentes.remove(resposta));
        }
        return resposta;
    }

    /** Bloqueia a thread do jogo até o jogador responder. */
    public <T> T aguardar(CompletableFuture<T> resposta) {
        try {
            return resposta.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JogoEncerradoException();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof JogoEncerradoException) {
                throw (JogoEncerradoException) e.getCause();
            }
            if (e.getCause() instanceof InterrupcaoDoJogoException) {
                throw (InterrupcaoDoJogoException) e.getCause();
            }
            throw new IllegalStateException("Falha ao obter a resposta do jogador", e.getCause());
        }
    }

    /**
     * Pausa -> reiniciar/sair: a pergunta em que o jogo está parado termina com a interrupção. Se, por um
     * instante, ele não esperar nenhuma, a interrupção vale para a próxima pergunta.
     */
    public synchronized void interromper(InterrupcaoDoJogoException interrupcao) {
        if (pendentes.isEmpty()) {
            interrupcaoSemOuvinte = interrupcao;
            return;
        }
        for (CompletableFuture<?> pendente : pendentes) {
            pendente.completeExceptionally(interrupcao);
        }
    }

    private synchronized InterrupcaoDoJogoException pegarInterrupcaoPendente() {
        InterrupcaoDoJogoException interrupcao = interrupcaoSemOuvinte;
        interrupcaoSemOuvinte = null;
        return interrupcao;
    }

    /** Chamada ao fechar a janela: destrava e encerra a thread do jogo, esteja ela esperando ou não. */
    public void encerrar() {
        encerrada = true;
        for (CompletableFuture<?> pendente : pendentes) {
            pendente.completeExceptionally(new JogoEncerradoException());
        }
    }
}
