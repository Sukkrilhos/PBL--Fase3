package controller;

import model.Antecedentes;
import model.Personagem;
import model.Progresso;
import model.persistence.GerenciadorDeSave;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;
import support.SessaoRoteirizada;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** A introdução que abre o jogo (uma por origem) e a mensagem de Game Over em qualquer temporada. */
public class IntroducaoEGameOverTest {

    private static final String MARCA_PROFESSOR = "Nem um giz a mais";
    private static final String MARCA_INDUSTRIAL = "fábrica de solventes";
    private static final String MARCA_PRECAVIDO = "confere duas vezes";

    private Path pasta;
    private SessaoRoteirizada sessao;

    @BeforeEach
    public void setUp() throws IOException {
        pasta = Arquivos.criarPastaTemporaria();
        sessao = new SessaoRoteirizada(pasta);
    }

    @AfterEach
    public void tearDown() throws IOException {
        sessao.restaurar();
        Arquivos.apagarRecursivamente(pasta);
    }

    // Novo jogo com a origem, a introdução inteira (duas telas) e o roteiro acaba na primeira pergunta do jantar
    private String abrirJogoComOrigem(int origem) {
        return sessao.jogar("1", "slot", "", String.valueOf(origem), "", "", "");
    }

    @Test
    public void testCadaOrigemTemASuaIntroducao() {
        String professor = abrirJogoComOrigem(1);
        String industrial = abrirJogoComOrigem(2);
        String precavido = abrirJogoComOrigem(3);

        assertTrue(professor.contains(MARCA_PROFESSOR));
        assertTrue(industrial.contains(MARCA_INDUSTRIAL));
        assertTrue(precavido.contains(MARCA_PRECAVIDO));
        assertFalse(professor.contains(MARCA_INDUSTRIAL) || professor.contains(MARCA_PRECAVIDO));
        assertFalse(industrial.contains(MARCA_PROFESSOR) || industrial.contains(MARCA_PRECAVIDO));
        assertFalse(precavido.contains(MARCA_PROFESSOR) || precavido.contains(MARCA_INDUSTRIAL));
    }

    @Test
    public void testAIntroducaoVemAntesDoJantarEContaODiagnostico() {
        String saida = abrirJogoComOrigem(1);

        int prologo = saida.indexOf("PRÓLOGO: Um Homem Comum");
        int diagnostico = saida.indexOf("PRÓLOGO: O Diagnóstico");
        int temporada1 = saida.indexOf("TEMPORADA 1:");
        assertTrue(prologo >= 0 && diagnostico > prologo && temporada1 > diagnostico,
                "ordem esperada: prólogo, diagnóstico, Temporada 1");
        assertTrue(saida.contains("Câncer de pulmão"));
    }

    // Carrega um save no capítulo da temporada dado, com saúde baixa, e escolhe a opção que custa saúde
    private String perderPorSaudeNaTemporada(int temporada, int capitulo, String... escolhas) {
        GerenciadorDeSave save = new GerenciadorDeSave(pasta.resolve("saves"), null);
        save.setNomeSaveAtual("frágil");
        Personagem frágil = new Personagem();
        frágil.setAntecedente(Antecedentes.PROFESSOR_TRADICIONAL);
        frágil.setSaude(10);
        Progresso progresso = new Progresso(frágil, temporada);
        progresso.setCapitulo(capitulo);
        save.salvarJogo(progresso);
        return sessao.jogar(SessaoRoteirizada.juntar(new String[]{"2", "1", "1", ""}, escolhas));
    }

    @Test
    public void testGameOverNaTemporada2MostraAMensagemEAsOpcoes() {
        // Sangue na Pia (capítulo 1 da Temporada 2), opção 2: -15 de saúde. Depois do Game Over: "2" volta ao menu e "6" sai.
        String saida = perderPorSaudeNaTemporada(2, 1, "2", "2", "6");

        assertTrue(saida.contains("GAME OVER"), "o jogador precisa ver a mensagem de Game Over");
        assertTrue(saida.contains("Retornando ao Menu Principal"));
        assertFalse(saida.contains("TEMPORADA 3:"), "a partida acabou: a campanha não segue");
    }

    @Test
    public void testGameOverNaTemporada3MostraAMensagemEAsOpcoes() {
        // Esconderijo, opção 1 (reservas da radioterapia): -25 de saúde
        String saida = perderPorSaudeNaTemporada(3, 0, "1", "2", "6");

        assertTrue(saida.contains("GAME OVER"));
        assertFalse(saida.contains("TEMPORADA 4:"));
    }

    @Test
    public void testGameOverPermiteReiniciarOJogo() {
        // "1" no fim de jogo = reiniciar: pede de novo o nome do save (aqui o roteiro acaba)
        String saida = perderPorSaudeNaTemporada(2, 1, "2", "1");

        assertTrue(saida.contains("GAME OVER"));
        assertTrue(saida.contains("REINICIANDO SISTEMA"));
    }
}
