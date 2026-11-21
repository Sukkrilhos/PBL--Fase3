package view.fx;

import model.Personagem;
import model.Progresso;
import org.junit.jupiter.api.Test;
import support.TelaFalsa;
import view.Cena;
import view.GerenciadorDeAudio;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class DialogosFxTest {

    private final TelaFalsa tela = new TelaFalsa();
    private final PonteDeInterface ponte = new PonteDeInterface(Runnable::run);
    private final AtomicReference<Personagem> personagem = new AtomicReference<>();
    private final DialogosFx dialogos = new DialogosFx(tela, ponte, GerenciadorDeAudio.mudo(), personagem::get);

    private static Cena cenaComDuasOpcoes() {
        return new Cena("jantar").linha("O que você responde?")
                .opcao(1, "Dizer a verdade").opcao(2, "Mentir").prompt("Sua escolha: ");
    }

    @Test
    public void testLerOpcaoUsaOsRotulosDaUltimaCena() {
        tela.politica = pergunta -> 2;
        dialogos.exibir(cenaComDuasOpcoes());

        int escolha = dialogos.lerOpcao(1, 2);

        assertEquals(2, escolha);
        Pergunta pergunta = tela.perguntas.get(0);
        assertEquals(Pergunta.Modo.CENA, pergunta.modo());
        assertEquals("Dizer a verdade", pergunta.opcoes().get(0).rotulo());
        assertEquals("Mentir", pergunta.opcoes().get(1).rotulo());
    }

    @Test
    public void testOpcaoIndisponivelChegaDesativadaNaTela() {
        dialogos.exibir(new Cena("atacadista").linha("Como você responde?")
                .opcao(1, "Aceitar").opcaoIndisponivel(2, "[SEM DINHEIRO: custa R$ 8000] Subornar").prompt("Sua escolha: "));

        dialogos.lerOpcao(1, 2);

        List<OpcaoFx> opcoes = tela.perguntas.get(0).opcoes();
        assertTrue(opcoes.get(0).disponivel());
        assertFalse(opcoes.get(1).disponivel(), "a compra sem dinheiro deveria chegar desativada");
        assertTrue(opcoes.get(1).rotulo().contains("R$ 8000"));
    }

    @Test
    public void testGameOverMantemAMensagemNaTelaEPoeOsBotoesNoCanto() {
        tela.politica = pergunta -> 2;

        dialogos.mostrarGameOver();
        int paginasAntes = tela.paginas.size();
        dialogos.mostrarOpcoesFimDeJogo();
        int escolha = dialogos.lerInteiroSeguro();

        assertEquals(2, escolha);
        assertTrue(tela.textoExibido().contains("GAME OVER"), "a mensagem de Game Over precisa ser exibida");
        assertEquals(paginasAntes, tela.paginas.size(), "os botões não podem apagar nem trocar a mensagem");
        Pergunta botoes = tela.perguntas.get(tela.perguntas.size() - 1);
        assertEquals(Pergunta.Modo.FIM, botoes.modo(), "botões de fim de jogo ficam no canto, não num painel central");
        assertEquals(List.of("Reiniciar partida", "Voltar ao menu principal"), botoes.opcoes().stream().map(OpcaoFx::rotulo).toList());
        assertEquals(List.of(1, 2), botoes.opcoes().stream().map(OpcaoFx::valor).toList());
    }

    @Test
    public void testFimDaCampanhaTambemMantemAMensagemEOferecaReiniciarOuMenu() {
        tela.politica = pergunta -> 1;

        dialogos.mostrarFimDaCampanha();
        dialogos.mostrarOpcoesFimDeJogo();
        int escolha = dialogos.lerInteiroSeguro();

        assertEquals(1, escolha);
        assertTrue(tela.textoExibido().contains("Fim da Campanha"));
        assertEquals(Pergunta.Modo.FIM, tela.perguntas.get(tela.perguntas.size() - 1).modo());
    }

    @Test
    public void testLerOpcaoSoOferecePedidoNoIntervalo() {
        dialogos.exibir(new Cena("x").linha("Texto").opcao(1, "A").opcao(2, "B").opcao(3, "C"));

        dialogos.lerOpcao(1, 2);

        assertEquals(2, tela.perguntas.get(0).opcoes().size());
    }

    @Test
    public void testLerOpcaoSemRotulosCriaBotoesGenericos() {
        dialogos.lerOpcao(1, 3);

        List<OpcaoFx> opcoes = tela.perguntas.get(0).opcoes();
        assertEquals(3, opcoes.size());
        assertEquals("Opção 2", opcoes.get(1).rotulo());
    }

    @Test
    public void testOsRotulosDeUmaCenaNaoVazamParaAProximaPergunta() {
        dialogos.exibir(cenaComDuasOpcoes());
        dialogos.lerOpcao(1, 2);

        dialogos.lerOpcao(1, 2); // sem cena nova entre as duas perguntas

        assertEquals("Opção 1", tela.perguntas.get(1).opcoes().get(0).rotulo());
    }

    @Test
    public void testPaginaNovaSoDepoisDeUmaInteracaoDoJogador() {
        dialogos.exibir(new Cena("a").linha("Primeira."));
        dialogos.exibir(new Cena("a").linha("Segunda, na mesma página."));
        dialogos.pausarParaLeitura();
        dialogos.exibir(new Cena("b").linha("Terceira, página nova."));

        assertEquals(List.of(true, false, true),
                tela.paginas.stream().map(TelaFalsa.Pagina::limpar).toList());
    }

    @Test
    public void testCadaFalaGanhaORostoDeQuemFala() {
        dialogos.exibir(new Cena("jantar")
                .linha("Marisa coloca os pratos na mesa.")
                .linha("MARISA: — Valter... o que aconteceu?")
                .linha("MALUCO: — Quem é o químico?"));

        List<Trecho> trechos = tela.paginas.get(0).trechos();
        assertNull(trechos.get(0).retrato(), "narração não tem rosto");
        assertEquals("marisa", trechos.get(1).retrato());
        assertEquals("maluco", trechos.get(2).retrato());
    }

    @Test
    public void testFalaDoProtagonistaUsaORostoDaSuaOrigem() {
        Personagem p = new Personagem();
        p.setNome("Valter");
        p.setAntecedente(model.Antecedentes.QUIMICO_PRECAVIDO);
        personagem.set(p);

        dialogos.exibir(new Cena("encruzilhada").linha("VALTER: — Acabou o tempo."));

        assertEquals("valter_precavido", tela.paginas.get(0).trechos().get(0).retrato());
    }

    @Test
    public void testEscolhaDeOrigemMostraORostoDeCadaUma() {
        dialogos.escolherAntecedente();

        List<OpcaoFx> origens = tela.perguntas.get(0).opcoes();
        assertEquals(List.of("valter_professor", "valter_industrial", "valter_precavido"),
                origens.stream().map(OpcaoFx::retrato).toList());
    }

    @Test
    public void testCenaSemTextoNaoGeraPagina() {
        dialogos.exibir(new Cena("x"));
        dialogos.exibir(new Cena("x").linha("========================================================"));

        assertTrue(tela.paginas.isEmpty());
    }

    @Test
    public void testMenuPrincipalMostraSeisOpcoesEDevolveAEscolha() {
        tela.politica = pergunta -> 4;
        dialogos.mostrarMenuPrincipal();

        int escolha = dialogos.lerInteiroSeguro();

        assertEquals(4, escolha);
        Pergunta menu = tela.perguntas.get(0);
        assertEquals(Pergunta.Modo.MENU, menu.modo());
        assertEquals(6, menu.opcoes().size());
        assertEquals("Novo Jogo", menu.opcoes().get(0).rotulo());
        assertEquals("Sair", menu.opcoes().get(5).rotulo());
    }

    @Test
    public void testLerInteiroSemPerguntaAbertaEUmErroDeProgramacao() {
        assertThrows(IllegalStateException.class, dialogos::lerInteiroSeguro);
    }

    @Test
    public void testNomeVazioDoPersonagemViraValter() {
        tela.politicaDeTexto = pergunta -> "   ";
        assertEquals("Valter", dialogos.pedirNomePersonagem());

        tela.politicaDeTexto = pergunta -> "  Heisenberg ";
        assertEquals("Heisenberg", dialogos.pedirNomePersonagem());
    }

    @Test
    public void testNomeDoSaveEObrigatorioEPerguntaDeNovoSeVier() {
        List<String> respostas = new java.util.ArrayList<>(List.of("", "   ", "minha rota"));
        tela.politicaDeTexto = pergunta -> respostas.remove(0);

        assertEquals("minha rota", dialogos.pedirNomeSave());
        assertEquals(3, tela.perguntasDeTexto.size());
    }

    @Test
    public void testMenuDeSavesDevolveOSaveEscolhidoOuNullAoVoltar() {
        Progresso a = new Progresso(new Personagem(), 2);
        a.setNomeSave("primeiro");
        a.setDataHora("01/01/2026 10:00:00");
        Progresso b = new Progresso(new Personagem(), 4);
        b.setNomeSave("segundo");
        b.setDataHora("02/01/2026 11:00:00");

        tela.politica = pergunta -> 2;
        assertSame(b, dialogos.mostrarMenuDeSaves(List.of(a, b)));

        Pergunta lista = tela.perguntas.get(0);
        assertEquals("primeiro", lista.opcoes().get(0).rotulo());
        assertTrue(lista.opcoes().get(1).descricao().contains("Temporada 4"));
        assertEquals(0, lista.opcoes().get(2).valor()); // "Voltar"

        tela.politica = pergunta -> 0;
        assertNull(dialogos.mostrarMenuDeSaves(List.of(a, b)));
    }

    @Test
    public void testMenuDeSavesVazioAvisaEDevolveNull() {
        assertNull(dialogos.mostrarMenuDeSaves(List.of()));
        assertTrue(tela.perguntas.get(0).paragrafos().get(0).contains("Nenhum jogo salvo"));
    }

    @Test
    public void testDialogosFxRegistraSeNoMenuDePausaDaTela() {
        assertSame(dialogos, tela.acoesDePausa);
    }

    @Test
    public void testSalvarPelaPausaAvisaSeGravouOuNao() {
        dialogos.aoSalvar(() -> true);
        tela.acoesDePausa.salvar();
        assertTrue(tela.avisos.get(0).contains("Progresso salvo"), tela.avisos.toString());

        dialogos.aoSalvar(() -> false);
        tela.acoesDePausa.salvar();
        assertTrue(tela.avisos.get(1).contains("Ainda não há progresso"), tela.avisos.toString());
    }

    // Joga a pergunta numa "thread do jogo", deixa ela pendente, aperta o botão da pausa e devolve o que o jogo recebeu
    private view.InterrupcaoDoJogoException perguntaInterrompidaPor(Runnable botaoDaPausa) throws Exception {
        tela.naoResponder = true;
        AtomicReference<view.InterrupcaoDoJogoException> recebida = new AtomicReference<>();
        Thread jogo = new Thread(() -> {
            try {
                dialogos.lerOpcao(1, 2);
            } catch (view.InterrupcaoDoJogoException interrupcao) {
                recebida.set(interrupcao);
            }
        });
        jogo.start();
        long limite = System.currentTimeMillis() + 3000;
        while (tela.perguntas.isEmpty() && System.currentTimeMillis() < limite) Thread.sleep(10);

        botaoDaPausa.run();
        jogo.join(3000);

        assertFalse(jogo.isAlive(), "o jogo deveria ter sido destravado pela pausa");
        return recebida.get();
    }

    @Test
    public void testReiniciarPelaPausaInterrompeAPerguntaEmQueOJogoEstaParado() throws Exception {
        view.InterrupcaoDoJogoException interrupcao = perguntaInterrompidaPor(tela.acoesDePausa::reiniciar);

        assertNotNull(interrupcao);
        assertEquals(view.InterrupcaoDoJogoException.Acao.REINICIAR, interrupcao.getAcao());
    }

    @Test
    public void testSairPelaPausaInterrompeAPerguntaEmQueOJogoEstaParado() throws Exception {
        view.InterrupcaoDoJogoException interrupcao = perguntaInterrompidaPor(tela.acoesDePausa::sair);

        assertNotNull(interrupcao);
        assertEquals(view.InterrupcaoDoJogoException.Acao.SAIR, interrupcao.getAcao());
    }

    @Test
    public void testGaleriaMostraDesbloqueadasEEscondeAsOutras() {
        dialogos.mostrarGaleriaConquistas(id -> id.equals(model.Conquistas.ROTA_A));

        List<String> linhas = tela.perguntas.get(0).paragrafos();
        assertEquals(7, linhas.size());
        assertTrue(linhas.get(0).contains("O Homem Honesto"));
        assertTrue(linhas.get(1).contains("???"));
    }

    @Test
    public void testCreditosSeparamOsNomesEmLinhas() {
        dialogos.mostrarCreditos();

        List<String> linhas = tela.perguntas.get(0).paragrafos();
        assertTrue(linhas.contains("Matheus de Almeida Muniz Rodrigues"));
        assertTrue(linhas.contains("Gustavo Silva Ribeiro"));
    }

    @Test
    public void testStatusVemDeUmaCopiaDoPersonagemEMenusNaoMostramStatus() {
        Personagem p = new Personagem();
        p.setNome("Valter");
        p.alterarDinheiro(5000);
        p.adicionarItem("Máscara de Gás");
        personagem.set(p);

        dialogos.exibir(new Cena("jantar").linha("Uma cena."));
        StatusDoPersonagem exibido = tela.statuses.get(tela.statuses.size() - 1);
        assertEquals(5000, exibido.dinheiro());
        assertEquals(List.of("Máscara de Gás"), exibido.inventario());

        p.alterarDinheiro(-1000); // mudar o personagem depois não altera o que já foi enviado à janela
        assertEquals(5000, exibido.dinheiro());

        dialogos.mostrarMenuPrincipal();
        assertNull(tela.statuses.get(tela.statuses.size() - 1));
    }

    @Test
    public void testFecharAJanelaEncerraOJogoNaProximaPergunta() {
        ponte.encerrar();

        assertThrows(JogoEncerradoException.class, () -> dialogos.lerOpcao(1, 2));
        assertThrows(JogoEncerradoException.class, dialogos::pausarParaLeitura);
    }

    @Test
    public void testConquistaDesbloqueadaViraAviso() {
        dialogos.mostrarConquistaDesbloqueada();

        assertTrue(tela.avisos.get(0).contains("Conquista desbloqueada"));
    }
}
