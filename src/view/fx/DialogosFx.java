package view.fx;

import model.Personagem;
import model.Progresso;
import view.Cena;
import view.Conteudo;
import view.Dialogos;
import view.GerenciadorDeAudio;
import view.InterrupcaoDoJogoException;
import view.Saida;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Interface gráfica do jogo, sem depender de JavaFX: traduz cada chamada dos controllers em páginas e
 * perguntas para uma {@link TelaFx}. Também recebe as cenas da história (é a {@link Saida} da Narrativa).
 *
 * O jogo roda numa thread própria e fica bloqueado esperando a resposta, como no console; quem desenha
 * é a TelaFx, na thread da janela, ligada a esta classe pela {@link PonteDeInterface}.
 */
public class DialogosFx implements Dialogos, Saida, AcoesDePausa {
    private static final String SOM_CLIQUE = "sons/menu.wav";

    private final TelaFx tela;
    private final PonteDeInterface ponte;
    private final GerenciadorDeAudio audio;
    private final Supplier<Personagem> personagemAtual;

    private volatile BooleanSupplier salvarPonto = () -> false;
    private boolean limparNaProximaPagina = true;
    private List<OpcaoFx> opcoesDaCena = List.of();
    private CompletableFuture<Integer> respostaPendente;

    public DialogosFx(TelaFx tela, PonteDeInterface ponte, GerenciadorDeAudio audio, Supplier<Personagem> personagemAtual) {
        this.tela = tela;
        this.ponte = ponte;
        this.audio = audio;
        this.personagemAtual = personagemAtual;
        tela.definirAcoesDePausa(this);
    }

    // =====================================================================
    // Menu de pausa (chamado na thread da janela)
    // =====================================================================

    /** Quem grava o ponto de retomada quando o jogador escolhe "Salvar progresso" na pausa (devolve se gravou). */
    public void aoSalvar(BooleanSupplier salvarPonto) {
        this.salvarPonto = salvarPonto;
    }

    @Override
    public void salvar() {
        avisar(salvarPonto.getAsBoolean()
                ? "Progresso salvo no ponto em que você pausou."
                : "Ainda não há progresso para salvar.");
    }

    @Override
    public void reiniciar() {
        ponte.interromper(new InterrupcaoDoJogoException(InterrupcaoDoJogoException.Acao.REINICIAR));
    }

    @Override
    public void sair() {
        ponte.interromper(new InterrupcaoDoJogoException(InterrupcaoDoJogoException.Acao.SAIR));
    }

    // =====================================================================
    // Cenas da história (Saida)
    // =====================================================================

    @Override
    public void exibir(Cena cena) {
        if (cena.estaVazia()) return;

        List<OpcaoFx> opcoes = new ArrayList<>();
        for (Cena.Item opcao : cena.getOpcoes()) {
            opcoes.add(OpcaoFx.de(opcao.numero(), opcao.texto(), opcao.disponivel()));
        }
        if (!opcoes.isEmpty()) opcoesDaCena = opcoes;

        exibirTrechos(cena.getCenario(), comRetratos(FormatadorDeCena.formatar(cena)));
    }

    @Override
    public int lerOpcao(int min, int max) {
        List<OpcaoFx> escolhas = new ArrayList<>();
        for (int valor = min; valor <= max; valor++) {
            escolhas.add(opcaoDaCena(valor));
        }
        opcoesDaCena = List.of();
        return perguntar(new Pergunta(Pergunta.Modo.CENA, null, List.of(), escolhas), true);
    }

    // Cada fala ganha o rosto de quem fala (o do protagonista depende da origem escolhida)
    private List<Trecho> comRetratos(List<Trecho> trechos) {
        StatusDoPersonagem status = statusAtual();
        List<Trecho> resultado = new ArrayList<>();
        for (Trecho trecho : trechos) {
            resultado.add(trecho.estilo() == Estilo.FALA
                    ? trecho.comRetrato(Personagens.retratoDoOrador(trecho.orador(), status)) : trecho);
        }
        return resultado;
    }

    private OpcaoFx opcaoDaCena(int valor) {
        for (OpcaoFx opcao : opcoesDaCena) {
            if (opcao.valor() == valor) return opcao;
        }
        return OpcaoFx.de(valor, "Opção " + valor);
    }

    // =====================================================================
    // Menus e perguntas
    // =====================================================================

    @Override
    public void mostrarMenuPrincipal() {
        List<OpcaoFx> opcoes = new ArrayList<>();
        for (int i = 0; i < Conteudo.MENU_PRINCIPAL.size(); i++) {
            opcoes.add(OpcaoFx.de(i + 1, Conteudo.MENU_PRINCIPAL.get(i)));
        }
        respostaPendente = iniciarPergunta(new Pergunta(Pergunta.Modo.MENU,
                Conteudo.NOME_DO_JOGO.toUpperCase(), List.of("Ponto Zero"), opcoes), false);
    }

    @Override
    public void mostrarOpcoesFimDeJogo() {
        // Modo FIM: a mensagem de Game Over / fim da campanha continua na tela e os botões ficam no canto inferior
        respostaPendente = iniciarPergunta(new Pergunta(Pergunta.Modo.FIM, null, List.of(),
                List.of(OpcaoFx.de(1, "Reiniciar partida"), OpcaoFx.de(2, "Voltar ao menu principal"))), true);
    }

    /** Devolve a resposta da pergunta aberta por mostrarMenuPrincipal() ou mostrarOpcoesFimDeJogo(). */
    @Override
    public int lerInteiroSeguro() {
        if (respostaPendente == null) {
            throw new IllegalStateException("Nenhuma pergunta aberta para ler a resposta");
        }
        CompletableFuture<Integer> resposta = respostaPendente;
        respostaPendente = null;
        return concluir(resposta);
    }

    @Override
    public String pedirNomeSave() {
        String texto = "";
        while (texto.isBlank()) {
            texto = perguntarTexto(new PerguntaDeTexto("NOVO JOGO",
                    "Dê um nome ao seu save (ex.: 'Playthrough 1', 'Rota Maligna')", "")).strip();
        }
        return texto;
    }

    @Override
    public String pedirNomePersonagem() {
        String texto = perguntarTexto(new PerguntaDeTexto("PERSONALIZAÇÃO DO PROTAGONISTA",
                "Nome do protagonista (deixe vazio para 'Valter')", "Valter")).strip();
        return texto.isEmpty() ? "Valter" : texto;
    }

    @Override
    public int escolherAntecedente() {
        List<OpcaoFx> opcoes = new ArrayList<>();
        for (int i = 0; i < Conteudo.ORIGENS.size(); i++) {
            Conteudo.Origem origem = Conteudo.ORIGENS.get(i);
            opcoes.add(new OpcaoFx(i + 1, origem.titulo(), origem.descricao(), Personagens.retratoDoProtagonista(origem.titulo())));
        }
        return perguntar(new Pergunta(Pergunta.Modo.PAINEL, "ORIGEM DO PERSONAGEM",
                List.of("Escolha o perfil de origem do seu personagem:"), opcoes), false);
    }

    @Override
    public Progresso mostrarMenuDeSaves(List<Progresso> saves) {
        if (saves.isEmpty()) {
            perguntar(new Pergunta(Pergunta.Modo.PAINEL, "JOGOS SALVOS", List.of("Nenhum jogo salvo encontrado!"),
                    List.of(OpcaoFx.de(0, "Voltar ao menu principal"))), false);
            return null;
        }

        List<OpcaoFx> opcoes = new ArrayList<>();
        for (int i = 0; i < saves.size(); i++) {
            Progresso save = saves.get(i);
            opcoes.add(new OpcaoFx(i + 1, save.getNomeSave(),
                    "Temporada " + save.getTemporadaAtual() + "  ·  último save: " + save.getDataHora()));
        }
        opcoes.add(OpcaoFx.de(0, "Voltar ao menu principal"));

        int escolha = perguntar(new Pergunta(Pergunta.Modo.PAINEL, "JOGOS SALVOS",
                List.of("Escolha o save que deseja carregar:"), opcoes), false);
        return escolha == 0 ? null : saves.get(escolha - 1);
    }

    @Override
    public int mostrarOpcoesDoSave(String nomeSave) {
        return perguntar(new Pergunta(Pergunta.Modo.PAINEL, "SAVE: " + nomeSave, List.of(),
                List.of(OpcaoFx.de(1, "Carregar partida"), OpcaoFx.de(2, "Apagar save"),
                        OpcaoFx.de(0, "Voltar à lista"))), false);
    }

    @Override
    public void pausarParaLeitura() {
        perguntar(new Pergunta(Pergunta.Modo.CONTINUAR, null, List.of(), List.of(OpcaoFx.de(1, "Continuar"))), true);
    }

    // =====================================================================
    // Telas informativas
    // =====================================================================

    @Override
    public void mostrarGaleriaConquistas(Predicate<String> possuiConquista) {
        List<String> paragrafos = new ArrayList<>();
        for (Conteudo.ConquistaInfo conquista : Conteudo.CONQUISTAS) {
            paragrafos.add(possuiConquista.test(conquista.id()) ? "●  " + conquista.descricao() : "○  ???");
        }
        informar("GALERIA DE CONQUISTAS", paragrafos);
    }

    @Override
    public void mostrarInstrucoes() {
        informar("INSTRUÇÕES", Conteudo.INSTRUCOES);
    }

    @Override
    public void mostrarCreditos() {
        List<String> paragrafos = new ArrayList<>();
        for (String bloco : Conteudo.CREDITOS) {
            for (String linha : bloco.split("\n")) {
                if (!linha.isBlank()) paragrafos.add(linha.strip());
            }
        }
        informar("CRÉDITOS", paragrafos);
    }

    private void informar(String titulo, List<String> paragrafos) {
        perguntar(new Pergunta(Pergunta.Modo.PAINEL, titulo, paragrafos, List.of(OpcaoFx.de(0, "Voltar"))), false);
    }

    @Override
    public void mostrarStatus(Personagem p) {
        exibirTrechos("status", List.of(
                Trecho.de(Estilo.TITULO_CAPITULO, "STATUS DE " + p.getNome().toUpperCase()),
                Trecho.de(Estilo.SISTEMA, "Dinheiro: R$ " + p.getDinheiro()),
                Trecho.de(Estilo.SISTEMA, "Saúde: " + p.getSaude() + "%"),
                Trecho.de(Estilo.SISTEMA, "Moral/Ética: " + p.getMoralEtica() + "%"),
                Trecho.de(Estilo.SISTEMA, "Relação Familiar: " + p.getRelacaoFamiliar() + "%")));
    }

    // =====================================================================
    // Fluxo
    // =====================================================================

    @Override
    public void limparTela() {
        limparNaProximaPagina = true;
    }

    @Override
    public void mostrarMensagem(String mensagem) {
        avisar(mensagem);
    }

    @Override
    public void mostrarConquistaDesbloqueada() {
        avisar("Conquista desbloqueada! Veja a Galeria no menu principal.");
    }

    @Override
    public void mostrarDespedida() { avisar("Encerrando o sistema. Até a próxima!"); }

    @Override
    public void mostrarOpcaoInvalida() { avisar("Opção inválida!"); }

    @Override
    public void mostrarReinicioDoSistema() { avisar("Reiniciando o sistema a partir do zero..."); }

    @Override
    public void mostrarReinicioDaJornada() { avisar("Limpando o progresso anterior e reiniciando a jornada..."); }

    @Override
    public void mostrarRetornoAoMenu() { avisar("Retornando ao Menu Principal..."); }

    @Override
    public void mostrarInicioDeJogo(String nome, String antecedente) {
        exibirTrechos("titulo", List.of(
                Trecho.de(Estilo.TITULO_CAPITULO, "PONTO ZERO"),
                Trecho.de(Estilo.SISTEMA, "Iniciando com " + nome + " [" + antecedente + "]...")));
    }

    @Override
    public void mostrarJogoCarregado(String nomeSave, int temporada) {
        exibirTrechos("titulo", List.of(
                Trecho.de(Estilo.SISTEMA, "Jogo '" + nomeSave + "' carregado com sucesso!"),
                Trecho.de(Estilo.SISTEMA, "Retomando a partir da Temporada " + temporada)));
    }

    @Override
    public void mostrarSaveApagado(String nomeSave) {
        exibirTrechos("titulo", List.of(Trecho.de(Estilo.SISTEMA, "Save '" + nomeSave + "' apagado permanentemente!")));
    }

    // Só um aviso que some sozinho: não interrompe a leitura nem pede resposta
    @Override
    public void mostrarProgressoSalvoAutomaticamente(int temporada) {
        avisar("Progresso guardado automaticamente após a Temporada " + temporada);
    }

    @Override
    public void mostrarGameOver() {
        exibirTrechos("fim", List.of(Trecho.de(Estilo.FIM,
                "GAME OVER: O seu corpo cedeu ao cansaço ou a sua família abandonou-o.")));
    }

    @Override
    public void mostrarFimDaCampanha() {
        exibirTrechos("fim", List.of(Trecho.de(Estilo.FIM,
                "Fim da Campanha. Obrigado por jogar '" + Conteudo.NOME_DO_JOGO + "'!")));
    }

    // =====================================================================
    // Mecânica interna: páginas, perguntas e a ponte com a janela
    // =====================================================================

    private void exibirTrechos(String cenario, List<Trecho> trechos) {
        if (trechos.isEmpty()) return;
        boolean limpar = limparNaProximaPagina;
        limparNaProximaPagina = false;
        StatusDoPersonagem status = statusAtual();
        ponte.executar(() -> {
            tela.atualizarStatus(status);
            tela.exibirPagina(cenario, trechos, limpar);
        });
    }

    private void avisar(String texto) {
        ponte.executar(() -> tela.mostrarAviso(texto));
    }

    private int perguntar(Pergunta pergunta, boolean comStatus) {
        return concluir(iniciarPergunta(pergunta, comStatus));
    }

    /** Mostra a pergunta e já devolve, sem esperar; quem chama aguarda depois com concluir(). */
    private CompletableFuture<Integer> iniciarPergunta(Pergunta pergunta, boolean comStatus) {
        CompletableFuture<Integer> resposta = ponte.novaResposta();
        StatusDoPersonagem status = comStatus ? statusAtual() : null;
        ponte.executar(() -> {
            tela.atualizarStatus(status);
            tela.perguntar(pergunta, resposta);
        });
        return resposta;
    }

    private int concluir(CompletableFuture<Integer> resposta) {
        int valor = ponte.aguardar(resposta);
        audio.tocarSom(SOM_CLIQUE);
        limparNaProximaPagina = true;
        return valor;
    }

    private String perguntarTexto(PerguntaDeTexto pergunta) {
        CompletableFuture<String> resposta = ponte.novaResposta();
        ponte.executar(() -> {
            tela.atualizarStatus(null);
            tela.perguntarTexto(pergunta, resposta);
        });
        String texto = ponte.aguardar(resposta);
        audio.tocarSom(SOM_CLIQUE);
        limparNaProximaPagina = true;
        return texto;
    }

    private StatusDoPersonagem statusAtual() {
        Personagem p = personagemAtual.get();
        if (p == null) return null;
        return new StatusDoPersonagem(p.getNome(), p.getAntecedente(), p.getDinheiro(), p.getSaude(),
                p.getMoralEtica(), p.getRelacaoFamiliar(), List.copyOf(p.getInventario()));
    }
}
