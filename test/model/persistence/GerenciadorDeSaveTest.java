package model.persistence;

import model.Itens;
import model.Personagem;
import model.Progresso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciadorDeSaveTest {

    private Path raiz;
    private Path pastaDeSaves;
    private List<String> erros;
    private GerenciadorDeSave gerenciador;

    @BeforeEach
    public void setUp() throws IOException {
        raiz = Arquivos.criarPastaTemporaria();
        pastaDeSaves = raiz.resolve("saves");
        erros = new ArrayList<>();
        gerenciador = new GerenciadorDeSave(pastaDeSaves, erros::add);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Arquivos.apagarRecursivamente(raiz);
    }

    private int quantidadeDeArquivos() throws IOException {
        try (Stream<Path> arquivos = Files.list(pastaDeSaves)) {
            return (int) arquivos.count();
        }
    }

    @Test
    public void testCriaAPastaDeSavesAutomaticamente() {
        assertTrue(Files.isDirectory(pastaDeSaves));
    }

    @Test
    public void testSalvarEListar() {
        gerenciador.setNomeSaveAtual("Minha Rota!");
        gerenciador.salvarJogo(new Progresso(new Personagem(), 2));

        List<Progresso> saves = gerenciador.listarSaves();

        assertEquals(1, saves.size());
        assertEquals("Minha Rota!", saves.get(0).getNomeSave());
        assertEquals(2, saves.get(0).getTemporadaAtual());
        assertNotNull(saves.get(0).getDataHora());
        // O nome do arquivo é "limpo" para não dar problema em nenhum sistema operacional
        assertTrue(Files.exists(pastaDeSaves.resolve("Minha_Rota_.json")));
    }

    @Test
    public void testPreservaAcentosNoSave() {
        Personagem p = new Personagem();
        p.setAntecedente("Químico Precavido");
        p.adicionarItem(Itens.MASCARA_DE_GAS);

        gerenciador.setNomeSaveAtual("acentos");
        gerenciador.salvarJogo(new Progresso(p, 1));

        Personagem lido = gerenciador.listarSaves().get(0).getPersonagem();
        assertEquals("Químico Precavido", lido.getAntecedente());
        assertTrue(lido.possuiItem("Máscara de Gás"));
    }

    @Test
    public void testAtualizarPersonagemMantemATemporadaJaSalva() {
        gerenciador.setNomeSaveAtual("slot");
        gerenciador.salvarJogo(new Progresso(new Personagem(), 3));

        Personagem atualizado = new Personagem();
        atualizado.setDinheiro(777);
        gerenciador.atualizarPersonagem(atualizado);

        Progresso lido = gerenciador.listarSaves().get(0);
        assertEquals(3, lido.getTemporadaAtual());
        assertEquals(777, lido.getPersonagem().getDinheiro());
    }

    @Test
    public void testAtualizarPersonagemSemSaveExistenteComecaNaTemporada1() {
        gerenciador.setNomeSaveAtual("novo");
        gerenciador.atualizarPersonagem(new Personagem());

        assertEquals(1, gerenciador.listarSaves().get(0).getTemporadaAtual());
    }

    @Test
    public void testRotaFinalEGravadaEPreservadaAoAtualizarOPersonagem() {
        Progresso progresso = new Progresso(new Personagem(), 5);
        progresso.setRotaFinal(2);
        gerenciador.setNomeSaveAtual("slot");
        gerenciador.salvarJogo(progresso);

        Personagem atualizado = new Personagem();
        atualizado.setDinheiro(42);
        gerenciador.atualizarPersonagem(atualizado); // acontece a cada consequência durante o jogo

        Progresso lido = gerenciador.listarSaves().get(0);
        assertEquals(2, lido.getRotaFinal());
        assertEquals(5, lido.getTemporadaAtual());
        assertEquals(42, lido.getPersonagem().getDinheiro());
    }

    @Test
    public void testSaveAntigoSemRotaFinalCarregaComZero() throws IOException {
        // Formato gravado pelas versões anteriores do jogo (sem o campo rotaFinal)
        String jsonAntigo = "{\"personagem\":{\"nome\":\"Valter\",\"antecedente\":\"Professor Tradicional\","
                + "\"dinheiro\":10,\"saude\":100,\"moralEtica\":100,\"relacaoFamiliar\":100,\"inventario\":[]},"
                + "\"temporadaAtual\":4,\"nomeSave\":\"antigo\",\"dataHora\":\"19/10/2026 13:39:19\"}";
        Files.write(pastaDeSaves.resolve("antigo.json"), jsonAntigo.getBytes(StandardCharsets.UTF_8));

        Progresso lido = gerenciador.listarSaves().get(0);

        assertEquals(4, lido.getTemporadaAtual());
        assertEquals(0, lido.getRotaFinal());
    }

    @Test
    public void testSemSlotAtivoNadaEGravado() throws IOException {
        gerenciador.atualizarPersonagem(new Personagem());
        gerenciador.salvarJogo(new Progresso(new Personagem(), 1));

        assertEquals(0, quantidadeDeArquivos());
        assertTrue(erros.isEmpty());
    }

    @Test
    public void testApagarSaveRemoveOArquivo() {
        gerenciador.setNomeSaveAtual("descartavel");
        gerenciador.salvarJogo(new Progresso(new Personagem(), 1));
        assertEquals(1, gerenciador.listarSaves().size());

        gerenciador.apagarSave();

        assertTrue(gerenciador.listarSaves().isEmpty());
    }

    @Test
    public void testListarIgnoraConquistasEArquivosCorrompidos() throws IOException {
        gerenciador.setNomeSaveAtual("valido");
        gerenciador.salvarJogo(new Progresso(new Personagem(), 1));

        Files.write(pastaDeSaves.resolve(GerenciadorDeConquistas.NOME_ARQUIVO), "[\"ROTA_A\"]".getBytes(StandardCharsets.UTF_8));
        Files.write(pastaDeSaves.resolve("quebrado.json"), "{ isto não fecha".getBytes(StandardCharsets.UTF_8));

        List<Progresso> saves = gerenciador.listarSaves();

        assertEquals(1, saves.size());
        assertEquals("valido", saves.get(0).getNomeSave());
    }

    @Test
    public void testFalhaDeGravacaoEntregaOErroAoOuvinteEmVezDeImprimir() throws IOException {
        // A "pasta de saves" fica dentro de um arquivo comum, então não dá para criá-la nem gravar nela
        Path arquivoComum = Files.createFile(raiz.resolve("bloqueio"));
        List<String> errosDoBloqueio = new ArrayList<>();
        GerenciadorDeSave bloqueado = new GerenciadorDeSave(arquivoComum.resolve("saves"), errosDoBloqueio::add);
        int errosAoCriar = errosDoBloqueio.size();

        bloqueado.setNomeSaveAtual("slot");
        bloqueado.salvarJogo(new Progresso(new Personagem(), 1));

        assertTrue(errosAoCriar >= 1, "criar a pasta deveria ter falhado e avisado");
        assertTrue(errosDoBloqueio.size() > errosAoCriar, "gravar o save deveria ter falhado e avisado");
        assertTrue(errosDoBloqueio.get(errosDoBloqueio.size() - 1).startsWith("Erro ao salvar o jogo"));
    }
}
