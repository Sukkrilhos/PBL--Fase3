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
import view.DialogosConsole;
import view.GerenciadorDeAudio;
import view.InterrupcaoDoJogoException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O menu de pausa da janela: salvar no ponto em que o jogador parou e retomar dali, reiniciar e sair.
 * No console não há botão; aqui um Dialogos de teste "aperta a pausa" quando o jogo espera a N-ésima escolha.
 */
public class PausaERetomadaTest {

    // Novo jogo (professor) até a primeira pergunta do jantar: as duas pausas da introdução já incluídas
    private static final String[] COMECO = {"1", "slot", "", "1", "", "", ""};
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

    /** Console em que, ao esperar a escolha de número {@code n} do jogo, o jogador aperta algo no menu de pausa. */
    private static class ConsoleComPausa extends DialogosConsole {
        private final int escolhaEmQueAPausaAcontece;
        private final IntConsumer aoPausar;
        private int escolhas;

        ConsoleComPausa(GerenciadorDeAudio audio, int escolhaEmQueAPausaAcontece, IntConsumer aoPausar) {
            super(audio);
            this.escolhaEmQueAPausaAcontece = escolhaEmQueAPausaAcontece;
            this.aoPausar = aoPausar;
        }

        @Override
        public int lerOpcao(int min, int max) {
            if (++escolhas == escolhaEmQueAPausaAcontece) aoPausar.accept(escolhas);
            return super.lerOpcao(min, max);
        }
    }

    private Progresso unicoSave() {
        return new GerenciadorDeSave(pasta.resolve("saves"), null).listarSaves().get(0);
    }

    @Test
    public void testSalvarPelaPausaNoMeioDaTemporadaRetomaNaCenaEmQueParou() {
        // Escolha nº 4 do jogo = "A Escolha" (a 1ª é a origem; a 2ª e a 3ª são as perguntas do jantar). O jogador pausa e salva ali.
        sessao.jogar((audio, service) -> new ConsoleComPausa(audio, 4, n -> assertTrue(service.salvarCheckpoint())),
                SessaoRoteirizada.juntar(COMECO, new String[]{"1", "1", ""}));

        Progresso salvo = unicoSave();
        assertEquals(1, salvo.getTemporadaAtual());
        assertEquals(1, salvo.getCapitulo(), "recomeça no capítulo em que a pausa aconteceu");
        assertEquals(90, salvo.getPersonagem().getMoralEtica(), "já tem o efeito do jantar, que ficou para trás");

        String retomada = sessao.jogar(SessaoRoteirizada.juntar(CARREGAR, new String[]{"2", "", "1", "", ""}));

        assertTrue(retomada.contains("encurrala o ex-aluno Kadu"), "deveria voltar para A Escolha");
        assertFalse(retomada.contains("a secretária do Dr. Nogueira ligou"), "o jantar não pode se repetir");
        assertFalse(retomada.contains("TEMPORADA 1:"), "nem o título da temporada");
        // Jantar (-10) uma só vez + aceitar a proposta (-30): se o jantar se repetisse, seriam 50
        assertEquals(60, unicoSave().getPersonagem().getMoralEtica(), "o efeito do jantar não pode ser aplicado duas vezes");
    }

    @Test
    public void testSalvarPelaPausaDepoisDeEscolherGuardaOEfeitoEnaoRepeteACena() {
        // A pausa acontece na escolha nº 5 do jogo (a Fornada), com A Escolha já resolvida (+R$ 5000, moral -30)
        sessao.jogar((audio, service) -> new ConsoleComPausa(audio, 5, n -> assertTrue(service.salvarCheckpoint())),
                SessaoRoteirizada.juntar(COMECO, new String[]{"1", "1", "", "2", ""}));

        Progresso salvo = unicoSave();
        assertEquals(2, salvo.getCapitulo(), "A Fornada é o capítulo 2");
        assertEquals(5000, salvo.getPersonagem().getDinheiro());

        String retomada = sessao.jogar(SessaoRoteirizada.juntar(CARREGAR, new String[]{"1", "", ""}));

        assertTrue(retomada.contains("A Primeira Fornada") || retomada.contains("fornada") || retomada.contains("Fornada"));
        assertFalse(retomada.contains("encurrala o ex-aluno Kadu"), "A Escolha não pode se repetir");
        assertEquals(4500, unicoSave().getPersonagem().getDinheiro(), "R$ 5000 uma vez, menos R$ 500 da fornada");
    }

    @Test
    public void testReiniciarPelaPausaComecaUmJogoNovoSemApagarOSave() {
        // O jogador salva e, na mesma pergunta (a 1ª do jantar), escolhe Reiniciar partida; o roteiro segue com um novo jogo
        String saida = sessao.jogar((audio, service) -> new ConsoleComPausa(audio, 2, n -> {
                    service.salvarCheckpoint();
                    throw new InterrupcaoDoJogoException(InterrupcaoDoJogoException.Acao.REINICIAR);
                }),
                SessaoRoteirizada.juntar(COMECO, new String[]{"1", "1", "", "slot2", "", "1", "", "", ""}));

        assertTrue(saida.contains("REINICIANDO SISTEMA"), "deve avisar que recomeça");
        assertEquals(2, saida.split("PRÓLOGO: Um Homem Comum", -1).length - 1, "a introdução do novo jogo apareceu de novo");
        assertEquals(2, new GerenciadorDeSave(pasta.resolve("saves"), null).listarSaves().size(),
                "o save feito antes de reiniciar continua lá, ao lado do novo");
    }

    @Test
    public void testSairPelaPausaVoltaAoMenuPrincipal() {
        String saida = sessao.jogar((audio, service) -> new ConsoleComPausa(audio, 2, n -> {
                    throw new InterrupcaoDoJogoException(InterrupcaoDoJogoException.Acao.SAIR);
                }),
                SessaoRoteirizada.juntar(COMECO, new String[]{"1", "6"}));

        assertTrue(saida.contains("Retornando ao Menu Principal"));
        assertEquals(2, saida.split("===== Ponto Zero =====", -1).length - 1, "o menu principal voltou a aparecer");
    }

    @Test
    public void testSemPontoDeRetomadaOBotaoSalvarNaoGravaNada() {
        // A 1ª escolha do jogo é a da origem, antes de existir personagem: salvar agora não pode gravar nada
        sessao.jogar((audio, service) -> new ConsoleComPausa(audio, 1, n -> assertFalse(service.salvarCheckpoint())),
                "1", "slot", "", "1", "");
    }

    @Test
    public void testSaveAntigoSemCapituloRecomecaNoInicioDaTemporada() {
        GerenciadorDeSave save = new GerenciadorDeSave(pasta.resolve("saves"), null);
        save.setNomeSaveAtual("antigo");
        Personagem p = new Personagem();
        p.setAntecedente(Antecedentes.PROFESSOR_TRADICIONAL);
        save.salvarJogo(new Progresso(p, 2));   // sem capítulo: lido como 0

        String saida = sessao.jogar(CARREGAR);

        assertTrue(saida.contains("Retomando a partir da Temporada 2"));
        assertTrue(saida.contains("TEMPORADA 2:"));
    }
}
