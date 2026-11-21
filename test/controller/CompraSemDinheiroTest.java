package controller;

import controller.temporada.TemporadaQuatro;
import controller.temporada.TemporadaTres;
import controller.temporada.TemporadaUm;
import model.Itens;
import model.Personagem;
import model.persistence.GerenciadorDeConquistas;
import model.persistence.GerenciadorDeSave;
import model.service.PersonagemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;
import support.SessaoRoteirizada;
import view.DialogosConsole;
import view.GerenciadorDeAudio;
import view.Narrativa;
import view.SaidaConsole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/** Só se compra o que o dinheiro cobre: a máscara (T1), os passaportes (T3) e o colete (T4). */
public class CompraSemDinheiroTest {

    private Path pasta;
    private PersonagemService service;
    private Personagem valter;
    private Narrativa narrativa;
    private DialogosConsole dialogos;
    private InputStream entradaOriginal;
    private SessaoRoteirizada sessao;

    @BeforeEach
    public void setUp() throws IOException {
        pasta = Arquivos.criarPastaTemporaria();
        sessao = new SessaoRoteirizada(pasta);
        GerenciadorDeSave save = new GerenciadorDeSave(pasta.resolve("saves"), null);
        GerenciadorDeConquistas conquistas = new GerenciadorDeConquistas(
                pasta.resolve("saves").resolve(GerenciadorDeConquistas.NOME_ARQUIVO), null);
        service = new PersonagemService(save, conquistas);
        valter = service.inicializarPersonagemCustomizado("Valter", 1);   // professor: começa com R$ 0
        entradaOriginal = System.in;
    }

    @AfterEach
    public void tearDown() throws IOException {
        System.setIn(entradaOriginal);
        sessao.restaurar();
        Arquivos.apagarRecursivamente(pasta);
    }

    // Joga a parte da temporada com as respostas dadas (cada linha é uma tecla Enter ou número) e devolve o texto impresso
    private String jogar(String respostas, Consumer<Narrativa> cena, Runnable temporada) {
        System.setIn(new ByteArrayInputStream(respostas.getBytes(StandardCharsets.UTF_8)));
        GerenciadorDeAudio audio = GerenciadorDeAudio.mudo();
        dialogos = new DialogosConsole(audio);
        narrativa = new Narrativa(audio, new SaidaConsole());
        return SessaoRoteirizada.capturarSaida(temporada);
    }

    @Test
    public void testPassaportesSemDinheiroSaoRecusadosEJogadorEscolheOutraOpcao() {
        String saida = jogar("1\n\n3\n1\n\n", null,
                () -> new TemporadaTres(dialogos, narrativa, service).executarTemporada(valter, 0));

        assertTrue(saida.contains("[SEM DINHEIRO: custa R$ 8000]"), "a opção deveria avisar que está sem dinheiro");
        assertTrue(saida.contains("Dinheiro insuficiente"), "a tentativa de comprar deveria ser recusada");
        assertFalse(service.verificarItem(Itens.PASSAPORTES_FALSOS), "sem dinheiro não pode sair com o item");
        assertEquals(10000, valter.getDinheiro(), "ficou com a opção 1 (aceitar a cota): +R$ 10000, sem descontar a compra");
    }

    @Test
    public void testPassaportesComDinheiroSaoComprados() {
        valter.alterarDinheiro(Itens.PRECO_PASSAPORTES_FALSOS);

        String saida = jogar("1\n\n3\n\n", null,
                () -> new TemporadaTres(dialogos, narrativa, service).executarTemporada(valter, 0));

        assertFalse(saida.contains("SEM DINHEIRO"));
        assertTrue(service.verificarItem(Itens.PASSAPORTES_FALSOS));
        assertEquals(0, valter.getDinheiro(), "pagou exatamente o preço");
    }

    @Test
    public void testColeteSemDinheiroSaoRecusado() {
        valter.alterarDinheiro(Itens.PRECO_COLETE_BALISTICO - 1);

        String saida = jogar("\n3\n1\n\n1\n\n", null,
                () -> new TemporadaQuatro(dialogos, narrativa, service).executarTemporada(valter, 0, 0));

        assertTrue(saida.contains("Dinheiro insuficiente"));
        assertFalse(service.verificarItem(Itens.COLETE_BALISTICO));
        assertEquals(Itens.PRECO_COLETE_BALISTICO - 1, valter.getDinheiro(), "nada foi cobrado");
    }

    @Test
    public void testMascaraSemDinheiroSaoRecusada() {
        valter.alterarDinheiro(-5000);   // a proposta do Kadu paga R$ 5000; assim o saldo na fornada é R$ 0
        // jantar (1, 1), pausa, rua (2 = aceitar), pausa, fornada (3 recusada, depois 1), pausa
        String saida = jogar("1\n1\n\n2\n\n3\n1\n\n", null,
                () -> new TemporadaUm(dialogos, narrativa, service).executarTemporada(valter, 0));

        assertTrue(saida.contains("Dinheiro insuficiente"));
        assertFalse(service.verificarItem(Itens.MASCARA_DE_GAS));
    }
}
