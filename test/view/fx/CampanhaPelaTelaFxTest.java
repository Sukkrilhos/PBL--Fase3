package view.fx;

import controller.Controlador;
import model.persistence.GerenciadorDeConquistas;
import model.persistence.GerenciadorDeSave;
import model.service.PersonagemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;
import support.TelaFalsa;
import view.GerenciadorDeAudio;
import view.Narrativa;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Joga o jogo de verdade (Controlador + Model reais) pela interface gráfica, trocando só a janela por uma
 * TelaFalsa que responde como um jogador. Prova que toda cena chega à tela com os botões certos.
 */
public class CampanhaPelaTelaFxTest {

    private Path pasta;
    private TelaFalsa tela;
    private int menusVistos;
    private boolean recusarOCrime;
    private int origemEscolhida;

    @BeforeEach
    public void setUp() throws IOException {
        pasta = Arquivos.criarPastaTemporaria();
        tela = new TelaFalsa();
        menusVistos = 0;
        recusarOCrime = false;
        origemEscolhida = 1;
        tela.politica = this::jogador;
        tela.politicaDeTexto = pergunta -> pergunta.titulo().equals("NOVO JOGO") ? "slot-fx" : "Walter";
    }

    @AfterEach
    public void tearDown() throws IOException {
        Arquivos.apagarRecursivamente(pasta);
    }

    // O "jogador": entra no jogo, sai no segundo menu, nunca salva à mão e escolhe sempre a 1ª opção
    // (ou "Recusar" quando o teste quer o final da Rota A)
    private int jogador(Pergunta pergunta) {
        switch (pergunta.modo()) {
            case MENU:
                return menusVistos++ == 0 ? 1 : 6;
            case CONTINUAR:
                return 1;
            case FIM:
                return 2;   // Game Over / fim da campanha: "Voltar ao menu principal"
            case CENA:
                for (OpcaoFx opcao : pergunta.opcoes()) {
                    if (recusarOCrime && opcao.rotulo().startsWith("Recusar")) return opcao.valor();
                    // "Aceitar." (com ponto) é a proposta de Kadu na Temporada 1; "Aceitar o fim..." é outra escolha, na Temporada 4
                    if (!recusarOCrime && opcao.rotulo().startsWith("Aceitar.")) return opcao.valor();
                }
                return pergunta.opcoes().get(0).valor();
            default:
                switch (pergunta.titulo()) {
                    case "ORIGEM DO PERSONAGEM": return origemEscolhida;
                    case "PONTO DE DESCANSO": return 2;
                    default: return 0;
                }
        }
    }

    private PersonagemService jogar() {
        PonteDeInterface ponte = new PonteDeInterface(Runnable::run);
        GerenciadorDeSave save = new GerenciadorDeSave(pasta.resolve("saves"), null);
        GerenciadorDeConquistas conquistas = new GerenciadorDeConquistas(
                pasta.resolve("saves").resolve(GerenciadorDeConquistas.NOME_ARQUIVO), null);
        PersonagemService service = new PersonagemService(save, conquistas);

        DialogosFx dialogos = new DialogosFx(tela, ponte, GerenciadorDeAudio.mudo(), service::getPersonagem);
        Narrativa narrativa = new Narrativa(GerenciadorDeAudio.mudo(), dialogos);
        new Controlador(dialogos, narrativa, service, save).iniciarSistema();
        return service;
    }

    @Test
    public void testCampanhaCompletaDaTemporada1AoFim() {
        jogar();

        String texto = tela.textoExibido();
        int anterior = -1;
        for (int t = 1; t <= 5; t++) {
            int posicao = texto.indexOf("TEMPORADA " + t + ":");
            assertTrue(posicao > anterior, "a Temporada " + t + " deveria aparecer, depois da anterior");
            anterior = posicao;
        }
        assertTrue(texto.contains("FIM DA ROTA B (MORTE)"));
        assertTrue(texto.contains("Fim da Campanha"));
        assertEquals("Encerrando o sistema. Até a próxima!", tela.avisos.get(tela.avisos.size() - 1));
    }

    @Test
    public void testTodaEscolhaDaCampanhaApareceComSeusRotulosEBotoesRealmenteDistintos() {
        jogar();

        List<Pergunta> escolhasDeCena = new ArrayList<>();
        for (Pergunta p : tela.perguntas) if (p.modo() == Pergunta.Modo.CENA) escolhasDeCena.add(p);

        assertEquals(10, escolhasDeCena.size()); // 4 (T1) + 2 (T2) + 2 (T3) + 2 (T4); a T5 não tem escolhas
        for (Pergunta escolha : escolhasDeCena) {
            for (OpcaoFx opcao : escolha.opcoes()) {
                assertFalse(opcao.rotulo().startsWith("Opção "), "botão sem rótulo da história: " + opcao);
            }
        }
    }

    @Test
    public void testRecusarOCrimeMostraOFinalDaOrigemEscolhida() {
        recusarOCrime = true;
        origemEscolhida = 2;

        PersonagemService service = jogar();

        assertTrue(tela.textoExibido().contains("consultor de segurança de reagentes"));
        assertTrue(tela.avisos.stream().anyMatch(a -> a.contains("Conquista desbloqueada")));
        assertTrue(service.possuiConquista(model.Conquistas.ROTA_A));
        assertFalse(tela.textoExibido().contains("TEMPORADA 2:"), "recusar o crime encerra a campanha");
    }

    @Test
    public void testPainelDeAtributosAcompanhaOPersonagemEMenusNaoOMostram() {
        origemEscolhida = 2; // começa com R$ 3.000 e -10 de moral

        jogar();

        StatusDoPersonagem primeiro = tela.statuses.stream().filter(s -> s != null).findFirst().orElseThrow();
        assertEquals("Walter", primeiro.nome());
        assertEquals(3000, primeiro.dinheiro());
        assertEquals(90, primeiro.moralEtica());
        assertNull(tela.statuses.get(0), "o menu inicial não mostra o painel de atributos");
        assertNull(tela.statuses.get(tela.statuses.size() - 1), "o menu final também não");
    }

    @Test
    public void testFecharAJanelaNoMeioDaPartidaInterrompeOJogo() {
        tela.politica = pergunta -> {
            if (pergunta.modo() == Pergunta.Modo.CENA) throw new JogoEncerradoException(); // janela fechada ao escolher
            return jogador(pergunta);
        };

        assertThrows(JogoEncerradoException.class, this::jogar);
    }

    @Test
    public void testFalasDaCampanhaTrazemOsRostosCertosEAOrigemDefineOdoProtagonista() {
        origemEscolhida = 3; // Químico Precavido

        jogar();

        List<Trecho> falas = tela.trechosDeEstilo(Estilo.FALA);
        assertFalse(falas.isEmpty());
        java.util.Set<String> vistos = new java.util.TreeSet<>();
        for (Trecho fala : falas) {
            switch (fala.orador()) {
                case "MARISA": assertEquals("marisa", fala.retrato()); break;
                case "MALUCO": assertEquals("maluco", fala.retrato()); break;
                case "KADU": assertEquals("kadu", fala.retrato()); break;
                case "HEITOR": assertEquals("heitor", fala.retrato()); break;
                case "ENZO": assertEquals("enzo", fala.retrato()); break;
                case "FIDALGO": assertEquals("fidalgo", fala.retrato()); break;
                case "WALTER": assertEquals("valter_precavido", fala.retrato()); break;   // nome escolhido no teste
                default: fail("orador inesperado: " + fala.orador());
            }
            vistos.add(fala.orador());
        }
        assertTrue(vistos.containsAll(List.of("MARISA", "MALUCO", "KADU", "HEITOR", "ENZO", "FIDALGO", "WALTER")),
                "esperava falas de todos os personagens, vieram: " + vistos);
    }
}
