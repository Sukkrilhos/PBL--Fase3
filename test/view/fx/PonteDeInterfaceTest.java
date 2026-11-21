package view.fx;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class PonteDeInterfaceTest {

    private static boolean lancouJogoEncerrado(Runnable acao) {
        try {
            acao.run();
            return false;
        } catch (JogoEncerradoException esperada) {
            return true;
        }
    }

    @Test
    public void testRespostaCompletadaEmOutraThreadDestravaOJogo() throws Exception {
        PonteDeInterface ponte = new PonteDeInterface(Runnable::run);
        CompletableFuture<Integer> resposta = ponte.novaResposta();

        Thread cliqueDoJogador = new Thread(() -> {
            try { Thread.sleep(50); } catch (InterruptedException ignorada) { }
            resposta.complete(7);
        });
        cliqueDoJogador.start();

        assertEquals(7, ponte.aguardar(resposta));
        cliqueDoJogador.join();
    }

    @Test
    public void testInterromperDestravaAPerguntaPendenteComAAcaoPedida() {
        PonteDeInterface ponte = new PonteDeInterface(Runnable::run);
        CompletableFuture<Integer> resposta = ponte.novaResposta();

        ponte.interromper(new view.InterrupcaoDoJogoException(view.InterrupcaoDoJogoException.Acao.SAIR));

        view.InterrupcaoDoJogoException lancada = assertThrows(view.InterrupcaoDoJogoException.class, () -> ponte.aguardar(resposta));
        assertEquals(view.InterrupcaoDoJogoException.Acao.SAIR, lancada.getAcao());
    }

    @Test
    public void testInterrupcaoPedidaSemPerguntaPendenteVaiParaAProxima() {
        PonteDeInterface ponte = new PonteDeInterface(Runnable::run);

        ponte.interromper(new view.InterrupcaoDoJogoException(view.InterrupcaoDoJogoException.Acao.REINICIAR));
        CompletableFuture<Integer> proxima = ponte.novaResposta();

        assertThrows(view.InterrupcaoDoJogoException.class, () -> ponte.aguardar(proxima));
        // E só vale uma vez: a pergunta seguinte volta ao normal
        CompletableFuture<Integer> seguinte = ponte.novaResposta();
        seguinte.complete(5);
        assertEquals(5, ponte.aguardar(seguinte));
    }

    @Test
    public void testExecutarUsaOExecutorDaInterface() {
        StringBuilder rastro = new StringBuilder();
        PonteDeInterface ponte = new PonteDeInterface(acao -> { rastro.append("[ui]"); acao.run(); });

        ponte.executar(() -> rastro.append("A"));

        assertEquals("[ui]A", rastro.toString());
    }

    @Test
    public void testFecharAJanelaDestravaQuemEstaEsperando() throws Exception {
        PonteDeInterface ponte = new PonteDeInterface(Runnable::run);
        CompletableFuture<Integer> resposta = ponte.novaResposta();
        boolean[] encerrou = {false};

        Thread jogo = new Thread(() -> encerrou[0] = lancouJogoEncerrado(() -> ponte.aguardar(resposta)));
        jogo.start();
        Thread.sleep(50);
        ponte.encerrar();
        jogo.join(2000);

        assertFalse(jogo.isAlive(), "a thread do jogo deveria ter terminado");
        assertTrue(encerrou[0]);
    }

    @Test
    public void testDepoisDeEncerradaNadaMaisBloqueiaNemExecuta() {
        PonteDeInterface ponte = new PonteDeInterface(Runnable::run);
        ponte.encerrar();

        assertTrue(lancouJogoEncerrado(() -> ponte.executar(() -> { })));
        assertTrue(lancouJogoEncerrado(() -> ponte.aguardar(ponte.novaResposta())));
    }

    @Test
    public void testSegundoCliqueNaMesmaRespostaEIgnorado() {
        PonteDeInterface ponte = new PonteDeInterface(Runnable::run);
        CompletableFuture<Integer> resposta = ponte.novaResposta();

        resposta.complete(1);
        resposta.complete(2); // clique duplo

        assertEquals(1, ponte.aguardar(resposta));
    }
}
