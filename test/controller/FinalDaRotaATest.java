package controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;
import support.SessaoRoteirizada;
import view.GerenciadorDeAudio;
import view.Narrativa;
import view.SaidaConsole;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Quem recusa o crime tem um final diferente para cada origem escolhida no começo do jogo. */
public class FinalDaRotaATest {

    private static final String TRECHO_PROFESSOR = "continuou dando aulas de química";
    private static final String TRECHO_EX_INDUSTRIAL = "consultor de segurança de reagentes";
    private static final String TRECHO_PRECAVIDO = "guardou a máscara de gás numa gaveta";

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

    // Novo jogo com a origem escolhida, Temporada 1 até "1 - Recusar", e sai pelo menu
    private String recusarOCrimeComOrigem(int origem) {
        return sessao.jogar("1", "slot", "", String.valueOf(origem), "", "", "",
                "1", "2", "", "1", "",
                "6");
    }

    private void assertSoEste(String saida, String esperado, String... naoEsperados) {
        assertTrue(saida.contains("CAPÍTULO FINAL: O Homem Honesto (ROTA A)"), "deveria ter chegado ao final da Rota A");
        assertTrue(saida.contains(esperado), "faltou o trecho do final desta origem: " + esperado);
        for (String outro : naoEsperados) {
            assertFalse(saida.contains(outro), "apareceu o trecho de outra origem: " + outro);
        }
    }

    @Test
    public void testProfessorTradicional() {
        assertSoEste(recusarOCrimeComOrigem(1), TRECHO_PROFESSOR, TRECHO_EX_INDUSTRIAL, TRECHO_PRECAVIDO);
    }

    @Test
    public void testExQuimicoIndustrial() {
        assertSoEste(recusarOCrimeComOrigem(2), TRECHO_EX_INDUSTRIAL, TRECHO_PROFESSOR, TRECHO_PRECAVIDO);
    }

    @Test
    public void testQuimicoPrecavido() {
        assertSoEste(recusarOCrimeComOrigem(3), TRECHO_PRECAVIDO, TRECHO_PROFESSOR, TRECHO_EX_INDUSTRIAL);
    }

    // Só o texto do final (a saída inteira já difere pelo nome da origem em "Iniciando 'PONTO ZERO'...")
    private static String soOFinal(String saida) {
        int inicio = saida.indexOf("CAPÍTULO FINAL: O Homem Honesto (ROTA A)");
        int marcaDoFim = saida.indexOf("Marisa e Enzo nunca souberam do que você quase se tornou.");
        assertTrue(inicio >= 0 && marcaDoFim > inicio, "não achei o bloco do final na saída");
        return saida.substring(inicio, marcaDoFim);
    }

    @Test
    public void testOsTresFinaisSaoTextosDiferentes() {
        String professor = soOFinal(recusarOCrimeComOrigem(1));
        String industrial = soOFinal(recusarOCrimeComOrigem(2));
        String precavido = soOFinal(recusarOCrimeComOrigem(3));

        assertFalse(professor.equals(industrial), "professor e ex-químico industrial têm o mesmo final");
        assertFalse(professor.equals(precavido), "professor e químico precavido têm o mesmo final");
        assertFalse(industrial.equals(precavido), "ex-químico industrial e químico precavido têm o mesmo final");
    }

    @Test
    public void testOrigemDesconhecidaOuAusenteCaiNoFinalDoProfessor() {
        Narrativa narrativa = new Narrativa(GerenciadorDeAudio.mudo(), new SaidaConsole());

        String desconhecida = SessaoRoteirizada.capturarSaida(() -> narrativa.mostrarDesfechoRotaA("Valter", "Origem Inventada"));
        String ausente = SessaoRoteirizada.capturarSaida(() -> narrativa.mostrarDesfechoRotaA("Valter", null));

        assertTrue(desconhecida.contains(TRECHO_PROFESSOR));
        assertTrue(ausente.contains(TRECHO_PROFESSOR));
    }
}
