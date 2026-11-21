package model.service;

import model.Conquistas;
import model.Itens;
import model.Personagem;
import model.Progresso;
import model.persistence.GerenciadorDeConquistas;
import model.persistence.GerenciadorDeSave;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersonagemServiceTest {

    private Path pasta;
    private GerenciadorDeSave gerenciadorDeSave;
    private PersonagemService personagemService;

    @BeforeEach
    public void setUp() throws IOException {
        // Tudo acontece numa pasta temporária: os testes nunca tocam nos saves/conquistas reais do jogo
        pasta = Arquivos.criarPastaTemporaria();
        gerenciadorDeSave = new GerenciadorDeSave(pasta.resolve("saves"), null);
        GerenciadorDeConquistas conquistas = new GerenciadorDeConquistas(
                pasta.resolve("saves").resolve(GerenciadorDeConquistas.NOME_ARQUIVO), null);
        personagemService = new PersonagemService(gerenciadorDeSave, conquistas);

        // Inicializa o personagem com o perfil padrão (Nome: Valter, Antecedente 1: Professor Tradicional)
        personagemService.inicializarPersonagemCustomizado("Valter", 1);
    }

    @Test
    public void testPodePagarSoComSaldoSuficiente() {
        assertFalse(personagemService.podePagar(Itens.PRECO_MASCARA_DE_GAS), "o professor começa sem dinheiro");

        personagemService.aplicarConsequencia(Itens.PRECO_PASSAPORTES_FALSOS, 0, 0, 0);

        assertTrue(personagemService.podePagar(Itens.PRECO_PASSAPORTES_FALSOS));
        assertFalse(personagemService.podePagar(Itens.PRECO_PASSAPORTES_FALSOS + 1));
    }

    @AfterEach
    public void tearDown() throws IOException {
        Arquivos.apagarRecursivamente(pasta);
    }

    @Test
    public void testAplicarConsequenciaCorretamente() {
        // Simula uma escolha que dá dinheiro mas penaliza saúde e moral
        personagemService.aplicarConsequencia(5000, -20, -15, -5);

        Personagem valter = personagemService.getPersonagem();

        assertEquals(5000, valter.getDinheiro(), "O dinheiro deve ser 5000");
        assertEquals(80, valter.getSaude(), "A saúde deve cair para 80");
        assertEquals(85, valter.getMoralEtica(), "A moral deve cair para 85");
        assertEquals(95, valter.getRelacaoFamiliar(), "A relação familiar deve cair para 95");
    }

    @Test
    public void testGameOverPorSaudeZero() {
        // Remove 100 de saúde
        personagemService.aplicarConsequencia(0, -100, 0, 0);

        assertTrue(personagemService.isGameOver(), "O jogo deve identificar Game Over se a saúde zerar");
    }

    @Test
    public void testGameOverPorRelacaoFamiliarZero() {
        // Remove 100 de relação familiar
        personagemService.aplicarConsequencia(0, 0, 0, -100);

        assertTrue(personagemService.isGameOver(), "O jogo deve identificar Game Over se a relação familiar zerar");
    }

    @Test
    public void testContinuidadeComAtributosPositivos() {
        // Penalizações leves que não zeram os atributos vitais
        personagemService.aplicarConsequencia(1000, -50, -90, -50);

        assertFalse(personagemService.isGameOver(), "O jogo não deve acabar se saúde e relação forem maiores que zero");
    }

    @Test
    public void testAdicionarEVerificarItemInventario() {
        String item = Itens.COLETE_BALISTICO;

        assertFalse(personagemService.verificarItem(item), "O personagem não deve ter o item inicialmente");

        personagemService.adicionarItemInventario(item);

        assertTrue(personagemService.verificarItem(item), "O inventário deve confirmar a posse do item após a adição");
    }

    @Test
    public void testDesbloquearEVerificarConquista() {
        personagemService.desbloquearConquista(Conquistas.ROTA_D_DELACAO);

        assertTrue(personagemService.possuiConquista(Conquistas.ROTA_D_DELACAO),
                "O sistema deve validar a conquista recém-desbloqueada");
    }

    @Test
    public void testDesbloquearConquistaSoAvisaNaPrimeiraVez() {
        assertTrue(personagemService.desbloquearConquista(Conquistas.ROTA_A), "Primeira vez: conquista nova");
        assertFalse(personagemService.desbloquearConquista(Conquistas.ROTA_A), "Segunda vez: já possuía");
    }

    @Test
    public void testInicializacaoCustomizadaComAntecedenteIndustrial() {
        personagemService.inicializarPersonagemCustomizado("Heisenberg", 2);
        Personagem p = personagemService.getPersonagem();

        assertEquals("Heisenberg", p.getNome());
        assertEquals("Ex-Químico Industrial", p.getAntecedente());
        assertEquals(3000, p.getDinheiro());
        assertEquals(90, p.getMoralEtica());
    }

    @Test
    public void testInicializacaoCustomizadaComItemPrecavido() {
        personagemService.inicializarPersonagemCustomizado("Valter", 3);
        Personagem p = personagemService.getPersonagem();

        assertEquals("Químico Precavido", p.getAntecedente());
        assertTrue(p.possuiItem(Itens.MASCARA_DE_GAS));
    }

    @Test
    public void testSalvarCheckpointGravaAFotoDoMomentoEOPontoDeRetomada() {
        gerenciadorDeSave.setNomeSaveAtual("slot");
        personagemService.inicializarPersonagemCustomizado("Valter", 1);
        personagemService.aplicarConsequencia(1000, 0, 0, 0);
        personagemService.registrarCheckpoint(3, 1, 0);
        personagemService.aplicarConsequencia(500, -20, 0, 0);   // o jogo segue depois do ponto marcado

        assertTrue(personagemService.salvarCheckpoint());

        Progresso salvo = gerenciadorDeSave.listarSaves().get(0);
        assertEquals(3, salvo.getTemporadaAtual());
        assertEquals(1, salvo.getCapitulo());
        assertEquals(1000, salvo.getPersonagem().getDinheiro(), "o save tem o personagem do ponto marcado, não o de agora");
        assertEquals(100, salvo.getPersonagem().getSaude());
    }

    @Test
    public void testCheckpointGuardaARotaEOInventarioDaquelaHora() {
        gerenciadorDeSave.setNomeSaveAtual("slot");
        personagemService.inicializarPersonagemCustomizado("Valter", 1);
        personagemService.adicionarItemInventario(Itens.RICINA);
        personagemService.registrarCheckpoint(4, 2, 3);
        personagemService.adicionarItemInventario(Itens.COLETE_BALISTICO);

        personagemService.salvarCheckpoint();

        Progresso salvo = gerenciadorDeSave.listarSaves().get(0);
        assertEquals(3, salvo.getRotaFinal());
        assertTrue(salvo.getPersonagem().getInventario().contains(Itens.RICINA));
        assertFalse(salvo.getPersonagem().getInventario().contains(Itens.COLETE_BALISTICO));
    }

    @Test
    public void testSalvarCheckpointSemPontoOuSemSlotNaoGrava() {
        assertFalse(personagemService.salvarCheckpoint(), "sem personagem não há o que salvar");

        personagemService.inicializarPersonagemCustomizado("Valter", 1);   // sem slot ativo
        personagemService.registrarCheckpoint(1, 0, 0);
        assertFalse(personagemService.salvarCheckpoint(), "sem slot não há onde gravar");
    }

    @Test
    public void testConsequenciasNaoGravamNoSlotAteOProximoPontoDeRetomada() {
        gerenciadorDeSave.setNomeSaveAtual("slot");
        personagemService.inicializarPersonagemCustomizado("Valter", 1);

        personagemService.aplicarConsequencia(5000, -30, 0, 0);

        Progresso salvo = gerenciadorDeSave.listarSaves().get(0);
        assertEquals(0, salvo.getPersonagem().getDinheiro(), "o slot não pode ficar com o efeito de uma cena que será repetida");
        assertEquals(100, salvo.getPersonagem().getSaude());
    }

    @Test
    public void testSalvarProgressoAutomaticoGravaAProximaTemporadaEARota() {
        gerenciadorDeSave.setNomeSaveAtual("slot");
        personagemService.salvarProgressoAutomatico(5, 3);

        Progresso salvo = gerenciadorDeSave.listarSaves().get(0);

        assertEquals(5, salvo.getTemporadaAtual());
        assertEquals(3, salvo.getRotaFinal());
    }
}
