package controller;

import model.persistence.GerenciadorDeSave;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;
import support.SessaoRoteirizada;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Joga de verdade pelo Controlador (ver SessaoRoteirizada) e confere onde a campanha recomeça ao carregar o save.
 */
public class RetomadaDeCampanhaTest {

    // Menu: novo jogo -> nome do save -> nome do personagem (Enter = Valter) -> origem 1 -> Enter,
    // e mais dois Enter, um para cada parte da introdução
    private static final String[] COMECO = {"1", "slot", "", "1", "", "", ""};
    // Temporada 1 até o fim (aceita a proposta, rota B/C), mais a pausa do status que o Controlador faz
    private static final String[] TEMPORADA_1 = {"1", "2", "", "2", "", "1", "", ""};
    private static final String[] TEMPORADA_2 = {"1", "", "1", "", ""};
    private static final String[] TEMPORADA_3 = {"2", "", "2", "", ""};
    // Temporada 4 escolhendo a rota D (entregar o império), mais a pausa do status
    private static final String[] TEMPORADA_4_ROTA_D = {"", "1", "", "3", "", ""};
    // Menu: carregar jogo -> primeiro save da lista -> carregar -> Enter
    private static final String[] CARREGAR = {"2", "1", "1", ""};

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

    private String jogar(String... linhas) {
        return sessao.jogar(linhas);
    }

    @Test
    public void testAposConcluirATemporada1OJogoRetomaNaTemporada2() {
        String primeiraSessao = jogar(SessaoRoteirizada.juntar(COMECO, TEMPORADA_1));
        assertTrue(primeiraSessao.contains("Progresso guardado automaticamente após a Temporada 1"));
        assertTrue(primeiraSessao.contains("TEMPORADA 2:"), "a sessão acabou já no começo da Temporada 2");

        String segundaSessao = jogar(CARREGAR);

        assertTrue(segundaSessao.contains("Retomando a partir da Temporada 2"));
        assertTrue(segundaSessao.contains("TEMPORADA 2:"), "deve começar na Temporada 2");
        assertFalse(segundaSessao.contains("TEMPORADA 1:"), "não pode repetir a Temporada 1");
    }

    @Test
    public void testAposConcluirATemporada4OJogoRetomaNaTemporada5ComARotaEscolhida() {
        String primeiraSessao = jogar(SessaoRoteirizada.juntar(COMECO, TEMPORADA_1, TEMPORADA_2, TEMPORADA_3, TEMPORADA_4_ROTA_D));
        assertTrue(primeiraSessao.contains("Progresso guardado automaticamente após a Temporada 4"));

        // Temporada 5 tem 2 pausas; no fim, "2" volta ao menu e "6" sai
        String segundaSessao = jogar(SessaoRoteirizada.juntar(CARREGAR, new String[]{"", "", "2", "6"}));

        assertTrue(segundaSessao.contains("Retomando a partir da Temporada 5"));
        assertTrue(segundaSessao.contains("TEMPORADA 5:"));
        assertFalse(segundaSessao.contains("TEMPORADA 4:"), "não pode repetir a Temporada 4");
        assertTrue(segundaSessao.contains("[FIM DA ROTA D (RENDIÇÃO)"), "a rota D escolhida na Temporada 4 foi lembrada");
    }

    @Test
    public void testSaveNaTemporada5SemRotaGravadaVoltaParaATemporada4() {
        // Cenário de save antigo/adulterado: diz "Temporada 5", mas a rota nunca foi gravada
        GerenciadorDeSave save = new GerenciadorDeSave(pasta.resolve("saves"), null);
        save.setNomeSaveAtual("sem-rota");
        save.salvarJogo(new model.Progresso(new model.Personagem(), 5));

        String saida = jogar(CARREGAR);

        assertTrue(saida.contains("Retomando a partir da Temporada 4"));
        assertTrue(saida.contains("TEMPORADA 4:"));
        assertFalse(saida.contains("TEMPORADA 5:"));
    }
}
