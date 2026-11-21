package view.fx.jfx;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import view.fx.AcoesDePausa;
import view.fx.OpcaoFx;
import view.fx.Pergunta;
import view.fx.Personagens;
import view.fx.PerguntaDeTexto;
import view.fx.StatusDoPersonagem;
import view.fx.TelaFx;
import view.fx.Trecho;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A janela do jogo em JavaFX. Só desenha: quem decide o que mostrar e quando é o DialogosFx,
 * que chama estes métodos na thread da janela. O visual vem de assets/tema.css e assets/fundos.
 *
 * Camadas, de baixo para cima: imagem de fundo, véu escuro (legibilidade), jogo (atributos, texto da
 * história e escolhas), menus/painéis e avisos.
 */
public class JavaFxTela implements TelaFx {
    private static final double LARGURA = 1280;
    private static final double ALTURA = 720;

    private final Stage palco;
    private final Fundos fundos = new Fundos();
    private final Retratos retratos = new Retratos();

    private final StackPane raiz = new StackPane();
    private final Region fundo = new Region();
    private final Region veu = new Region();

    // Camada do jogo
    private final BorderPane jogo = new BorderPane();
    private final VBox hud = new VBox(6);
    private final ImageView hudRetrato = new ImageView();
    private final Label hudNome = new Label();
    private final Label hudDinheiro = new Label();
    private final Label hudInventario = new Label();
    private final Label hudSaude = new Label();
    private final Label hudMoral = new Label();
    private final Label hudRelacao = new Label();
    private final ProgressBar barraSaude = new ProgressBar(1);
    private final ProgressBar barraMoral = new ProgressBar(1);
    private final ProgressBar barraRelacao = new ProgressBar(1);
    private final VBox pagina = new VBox(14);
    private final ScrollPane rolagem = new ScrollPane(pagina);
    private final VBox escolhas = new VBox(10);

    // Camada de menus/painéis e avisos
    private final StackPane camada = new StackPane();
    private final VBox avisos = new VBox(8);

    private String cenarioAtual;
    // Pausa: o botão fica no canto superior esquerdo do painel de atributos e abre um menu sobre toda a tela
    private final Button botaoPausa = new Button();
    private final StackPane camadaPausa = new StackPane();
    private AcoesDePausa acoesDePausa;
    private boolean pausado;
    private boolean pausaPermitida = true;   // some na tela de Game Over / fim da campanha

    private boolean emJogo;
    private Consumer<KeyEvent> aoTeclar;

    public JavaFxTela(Stage palco) {
        this.palco = palco;
        montarJogo();
        montarRaiz();

        Scene cena = new Scene(raiz, LARGURA, ALTURA);
        File tema = new File("assets", "tema.css");
        if (tema.isFile()) {
            cena.getStylesheets().add(tema.toURI().toString());
        }
        cena.setOnKeyPressed(evento -> {
            if (evento.getCode() == KeyCode.F11) {
                palco.setFullScreen(!palco.isFullScreen());
            } else if (evento.getCode() == KeyCode.ESCAPE) {
                if (pausado) fecharPausa(); else abrirPausa();
            } else if (!pausado && aoTeclar != null) {   // com a pausa aberta, os números não escolhem nada
                aoTeclar.accept(evento);
            }
        });

        palco.setTitle("Ponto de Ebulição");
        palco.setMinWidth(960);
        palco.setMinHeight(600);
        palco.setScene(cena);
    }

    public void mostrar() {
        palco.show();
    }

    // =====================================================================
    // Montagem da janela
    // =====================================================================

    private void montarJogo() {
        hud.getStyleClass().add("hud");
        hudNome.getStyleClass().add("hud-nome");
        hudDinheiro.getStyleClass().add("hud-dinheiro");
        hudInventario.getStyleClass().add("hud-inventario");
        barraSaude.getStyleClass().add("barra-saude");
        barraMoral.getStyleClass().add("barra-moral");
        barraRelacao.getStyleClass().add("barra-relacao");
        for (Label rotulo : List.of(hudSaude, hudMoral, hudRelacao)) rotulo.getStyleClass().add("hud-rotulo");

        hudRetrato.setFitHeight(54);
        hudRetrato.setPreserveRatio(true);
        hudRetrato.setSmooth(true);
        hudRetrato.setVisible(false);
        hudRetrato.setManaged(false);

        HBox barrasDaPausa = new HBox(4, barraDaPausa(), barraDaPausa());
        barrasDaPausa.setAlignment(Pos.CENTER);
        botaoPausa.setGraphic(barrasDaPausa);
        botaoPausa.getStyleClass().add("botao-pausa");
        botaoPausa.setFocusTraversable(false);
        botaoPausa.setTooltip(new Tooltip("Pausar (Esc)"));
        botaoPausa.setOnAction(evento -> abrirPausa());

        HBox linhaPrincipal = new HBox(28, botaoPausa, hudRetrato, hudNome, hudDinheiro,
                coluna(hudSaude, barraSaude), coluna(hudMoral, barraMoral), coluna(hudRelacao, barraRelacao));
        linhaPrincipal.setAlignment(Pos.CENTER_LEFT);
        hud.getChildren().addAll(linhaPrincipal, hudInventario);
        hud.setVisible(false);
        hud.setManaged(false);

        pagina.getStyleClass().add("pagina");
        rolagem.setFitToWidth(true);
        rolagem.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rolagem.getStyleClass().add("rolagem");

        // O cartão se ajusta ao texto (até 46% da altura da janela, depois rola), para a arte do cenário aparecer acima dele
        rolagem.prefHeightProperty().bind(pagina.heightProperty().add(4));
        rolagem.maxHeightProperty().bind(raiz.heightProperty().multiply(0.46));
        VBox cartao = new VBox(rolagem);
        cartao.getStyleClass().add("cartao-texto");
        cartao.setMaxWidth(980);

        escolhas.getStyleClass().add("area-de-escolhas");
        escolhas.setMaxWidth(980);

        VBox centro = new VBox(12, cartao, escolhas);
        centro.setAlignment(Pos.BOTTOM_CENTER);
        centro.setPadding(new Insets(10, 40, 30, 40));

        jogo.setTop(hud);
        jogo.setCenter(centro);
        jogo.setVisible(false);
    }

    private VBox coluna(Label rotulo, ProgressBar barra) {
        barra.setPrefWidth(140);
        VBox caixa = new VBox(3, rotulo, barra);
        caixa.setAlignment(Pos.CENTER_LEFT);
        return caixa;
    }

    private void montarRaiz() {
        veu.getStyleClass().add("veu");
        camada.getStyleClass().add("camada");
        camada.setVisible(false);
        camadaPausa.getStyleClass().add("camada-pausa");   // com fundo: nada por baixo recebe clique enquanto pausado
        camadaPausa.setVisible(false);
        avisos.setAlignment(Pos.TOP_RIGHT);
        avisos.setMouseTransparent(true);
        avisos.setPickOnBounds(false);
        avisos.setMaxWidth(420);
        avisos.setPadding(new Insets(70, 24, 0, 0));
        StackPane.setAlignment(avisos, Pos.TOP_RIGHT);

        raiz.getStyleClass().add("raiz");
        raiz.getChildren().addAll(fundo, veu, jogo, camada, camadaPausa, avisos);
    }

    // =====================================================================
    // TelaFx
    // =====================================================================

    @Override
    public void definirAcoesDePausa(AcoesDePausa acoes) {
        this.acoesDePausa = acoes;
    }

    @Override
    public void atualizarStatus(StatusDoPersonagem status) {
        emJogo = status != null;
        hud.setVisible(emJogo);
        hud.setManaged(emJogo);
        atualizarBotaoPausa();
        if (status == null) {
            fecharPausa();   // saiu da partida: nada de menu de pausa solto sobre o menu principal
            return;
        }

        Image rosto = retratos.obter(Personagens.retratoDoProtagonista(status.antecedente()));
        hudRetrato.setImage(rosto);
        hudRetrato.setVisible(rosto != null);
        hudRetrato.setManaged(rosto != null);
        hudNome.setText(status.nome() + "  ·  " + status.antecedente());
        hudDinheiro.setText("R$ " + status.dinheiro());
        atualizarBarra(hudSaude, barraSaude, "Saúde", status.saude());
        atualizarBarra(hudMoral, barraMoral, "Moral", status.moralEtica());
        atualizarBarra(hudRelacao, barraRelacao, "Família", status.relacaoFamiliar());
        hudInventario.setText(status.inventario().isEmpty()
                ? "Inventário vazio" : "Inventário: " + String.join(", ", status.inventario()));
    }

    private void atualizarBarra(Label rotulo, ProgressBar barra, String nome, int valor) {
        rotulo.setText(nome + " " + valor + "%");
        barra.setProgress(Math.max(0, Math.min(100, valor)) / 100.0);
    }

    @Override
    public void exibirPagina(String cenario, List<Trecho> trechos, boolean limpar) {
        definirFundo(cenario);
        veu.setOpacity(1.0);
        camada.setVisible(false);
        jogo.setVisible(true);
        if (limpar) {
            pagina.getChildren().clear();
            escolhas.getChildren().clear();
        }

        int ordem = 0;
        for (Trecho trecho : trechos) {
            Node no = noDoTrecho(trecho);
            pagina.getChildren().add(no);
            surgir(no, 140 * ordem++);
        }
        rolarParaOFim();
    }

    @Override
    public void perguntar(Pergunta pergunta, CompletableFuture<Integer> resposta) {
        pausaPermitida = pergunta.modo() != Pergunta.Modo.FIM;
        atualizarBotaoPausa();
        switch (pergunta.modo()) {
            case MENU:
                definirFundo("menu");
                jogo.setVisible(false);
                mostrarNaCamada(telaDeMenu(pergunta, resposta), Pos.CENTER_LEFT);   // o frasco da arte fica livre à direita
                break;
            case PAINEL:
                if (!emJogo) {
                    definirFundo("menu");
                    jogo.setVisible(false);
                }
                mostrarNaCamada(painel(pergunta, resposta), Pos.CENTER);
                break;
            case CENA:
            case CONTINUAR:
                camada.setVisible(false);
                jogo.setVisible(true);
                escolhas.getChildren().clear();
                escolhas.setAlignment(Pos.TOP_LEFT);
                for (OpcaoFx opcao : pergunta.opcoes()) {
                    String texto = pergunta.modo() == Pergunta.Modo.CENA
                            ? opcao.valor() + "    " + opcao.rotulo() : opcao.rotulo();
                    Button botao = new Button(texto);
                    botao.getStyleClass().add(pergunta.modo() == Pergunta.Modo.CENA ? "opcao" : "continuar");
                    configurarBotao(botao);
                    botao.setDisable(!opcao.disponivel());   // opção sem dinheiro: aparece, mas não clica
                    botao.setOnAction(evento -> completar(resposta, opcao.valor()));
                    escolhas.getChildren().add(botao);
                    surgir(botao, 200);
                }
                break;
            case FIM:
                // A mensagem de Game Over / fim da campanha fica na página; os botões vão para o canto inferior direito
                camada.setVisible(false);
                jogo.setVisible(true);
                escolhas.getChildren().clear();
                escolhas.setAlignment(Pos.BOTTOM_RIGHT);
                HBox acoes = new HBox(12);
                acoes.setAlignment(Pos.CENTER_RIGHT);
                for (OpcaoFx opcao : pergunta.opcoes()) {
                    Button botao = new Button(opcao.valor() + "    " + opcao.rotulo());
                    botao.getStyleClass().add("opcao-fim");
                    botao.setFocusTraversable(false);
                    botao.setOnAction(evento -> completar(resposta, opcao.valor()));
                    acoes.getChildren().add(botao);
                }
                escolhas.getChildren().add(acoes);
                surgir(acoes, 400);
                break;
        }
        definirTeclas(pergunta, resposta);
    }

    @Override
    public void perguntarTexto(PerguntaDeTexto pergunta, CompletableFuture<String> resposta) {
        definirFundo("menu");
        jogo.setVisible(false);
        aoTeclar = null;

        Label titulo = new Label(pergunta.titulo());
        titulo.getStyleClass().add("painel-titulo");
        Label dica = new Label(pergunta.dica());
        dica.setWrapText(true);
        dica.getStyleClass().add("painel-texto");

        TextField campo = new TextField(pergunta.padrao());
        campo.getStyleClass().add("campo");
        Button confirmar = new Button("Confirmar");
        confirmar.getStyleClass().add("opcao");
        configurarBotao(confirmar);

        Runnable enviar = () -> {
            String texto = campo.getText() == null ? "" : campo.getText().strip();
            // Com padrão, vazio é aceito (o DialogosFx aplica o padrão); sem padrão, o texto é obrigatório
            if (texto.isEmpty() && pergunta.padrao().isEmpty()) return;
            if (resposta.complete(texto)) esconderCamada();
        };
        campo.setOnAction(evento -> enviar.run());
        confirmar.setOnAction(evento -> enviar.run());

        VBox cartao = new VBox(16, titulo, dica, campo, confirmar);
        cartao.getStyleClass().add("painel");
        cartao.setMaxWidth(640);
        mostrarNaCamada(cartao, Pos.CENTER);
        Platform.runLater(campo::requestFocus);
    }

    @Override
    public void mostrarAviso(String texto) {
        Label aviso = new Label(texto);
        aviso.setWrapText(true);
        aviso.getStyleClass().add("aviso");
        avisos.getChildren().add(aviso);
        surgir(aviso, 0);

        PauseTransition espera = new PauseTransition(Duration.seconds(3.5));
        espera.setOnFinished(evento -> avisos.getChildren().remove(aviso));
        espera.play();
    }

    // =====================================================================
    // Telas e componentes
    // =====================================================================

    private Node telaDeMenu(Pergunta pergunta, CompletableFuture<Integer> resposta) {
        Label titulo = new Label(pergunta.titulo());
        titulo.setWrapText(true);
        titulo.setMaxWidth(520);
        titulo.getStyleClass().add("menu-titulo");
        VBox caixa = new VBox(14, titulo);
        caixa.setAlignment(Pos.CENTER_LEFT);
        caixa.setMaxWidth(520);
        for (String subtitulo : pergunta.paragrafos()) {
            Label rotulo = new Label(subtitulo);
            rotulo.getStyleClass().add("menu-subtitulo");
            caixa.getChildren().add(rotulo);
        }

        VBox botoes = new VBox(10);
        botoes.setAlignment(Pos.CENTER_LEFT);
        botoes.setMaxWidth(340);
        botoes.setPadding(new Insets(26, 0, 0, 0));
        for (OpcaoFx opcao : pergunta.opcoes()) {
            Button botao = new Button(opcao.rotulo());
            botao.getStyleClass().add("botao-menu");
            botao.setMaxWidth(Double.MAX_VALUE);
            botao.setFocusTraversable(false);
            botao.setOnAction(evento -> completar(resposta, opcao.valor()));
            botoes.getChildren().add(botao);
        }
        caixa.getChildren().add(botoes);
        surgir(caixa, 0);
        return caixa;
    }

    private Node painel(Pergunta pergunta, CompletableFuture<Integer> resposta) {
        VBox cartao = new VBox(14);
        cartao.getStyleClass().add("painel");
        cartao.setMaxWidth(720);

        if (pergunta.titulo() != null) {
            Label titulo = new Label(pergunta.titulo());
            titulo.getStyleClass().add("painel-titulo");
            cartao.getChildren().add(titulo);
        }
        for (String paragrafo : pergunta.paragrafos()) {
            Label texto = new Label(paragrafo);
            texto.setWrapText(true);
            texto.getStyleClass().add("painel-texto");
            cartao.getChildren().add(texto);
        }
        for (OpcaoFx opcao : pergunta.opcoes()) {
            Button botao = new Button();
            Label rotulo = new Label(opcao.rotulo());
            rotulo.getStyleClass().add("opcao-titulo");
            VBox conteudo = new VBox(2, rotulo);
            if (opcao.descricao() != null) {
                Label descricao = new Label(opcao.descricao());
                descricao.setWrapText(true);
                descricao.getStyleClass().add("opcao-descricao");
                conteudo.getChildren().add(descricao);
            }
            Image rostoDaOpcao = opcao.retrato() == null ? null : retratos.obter(opcao.retrato());
            if (rostoDaOpcao != null) {
                HBox comRosto = new HBox(16, miniatura(rostoDaOpcao, 96), conteudo);
                comRosto.setAlignment(Pos.CENTER_LEFT);
                botao.setGraphic(comRosto);
            } else {
                botao.setGraphic(conteudo);
            }
            botao.getStyleClass().add("opcao");
            configurarBotao(botao);
            botao.setOnAction(evento -> completar(resposta, opcao.valor()));
            cartao.getChildren().add(botao);
        }
        surgir(cartao, 0);
        return cartao;
    }

    private Node noDoTrecho(Trecho trecho) {
        switch (trecho.estilo()) {
            case TITULO_TEMPORADA:
                return rotulo(trecho.texto(), "titulo-temporada");
            case TITULO_CAPITULO:
                return rotulo(trecho.texto(), "titulo-capitulo");
            case FALA:
                Label orador = new Label(trecho.orador());
                orador.getStyleClass().add("orador");
                VBox textos = new VBox(2, orador, rotulo(trecho.texto(), "fala"));
                textos.setMaxWidth(Double.MAX_VALUE);
                Image rostoDaFala = retratos.obter(trecho.retrato());
                if (rostoDaFala == null) {
                    textos.getStyleClass().add("caixa-fala");
                    return textos;
                }
                HBox fala = new HBox(16, miniatura(rostoDaFala, 112), textos);
                fala.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(textos, Priority.ALWAYS);
                fala.getStyleClass().add("caixa-fala");
                return fala;
            case PERGUNTA:
                return rotulo(trecho.texto(), "pergunta");
            case EFEITO:
                return rotulo(trecho.texto(), "efeito");
            case FIM:
                return rotulo(trecho.texto(), "fim");
            case SISTEMA:
                return rotulo(trecho.texto(), "sistema");
            default:
                return rotulo(trecho.texto(), "narracao");
        }
    }

    private ImageView miniatura(Image imagem, double altura) {
        ImageView vista = new ImageView(imagem);
        vista.setFitHeight(altura);
        vista.setPreserveRatio(true);
        vista.setSmooth(true);
        return vista;
    }

    private Label rotulo(String texto, String classe) {
        Label rotulo = new Label(texto);
        rotulo.setWrapText(true);
        rotulo.setMaxWidth(Double.MAX_VALUE);
        rotulo.getStyleClass().add(classe);
        return rotulo;
    }

    // =====================================================================
    // Menu de pausa
    // =====================================================================

    private Region barraDaPausa() {
        Region barra = new Region();
        barra.getStyleClass().add("barra-pausa");
        barra.setPrefSize(5, 16);
        barra.setMinSize(5, 16);
        barra.setMaxSize(5, 16);
        return barra;
    }

    private void atualizarBotaoPausa() {
        boolean visivel = emJogo && pausaPermitida;
        botaoPausa.setVisible(visivel);
        botaoPausa.setManaged(visivel);
        if (!visivel && pausado) fecharPausa();
    }

    private void abrirPausa() {
        if (!emJogo || !pausaPermitida || pausado) return;
        pausado = true;
        mostrarNaPausa(menuDaPausa());
    }

    private void fecharPausa() {
        pausado = false;
        camadaPausa.getChildren().clear();
        camadaPausa.setVisible(false);
    }

    private void mostrarNaPausa(Node conteudo) {
        camadaPausa.getChildren().setAll(conteudo);
        StackPane.setAlignment(conteudo, Pos.CENTER);
        camadaPausa.setVisible(true);
    }

    private VBox cartaoDaPausa(String titulo) {
        VBox cartao = new VBox(14);
        cartao.getStyleClass().add("painel");
        cartao.setMaxWidth(520);
        cartao.setMaxHeight(Region.USE_PREF_SIZE);
        Label rotuloDoTitulo = new Label(titulo);
        rotuloDoTitulo.getStyleClass().add("painel-titulo");
        cartao.getChildren().add(rotuloDoTitulo);
        return cartao;
    }

    private Button botaoDaPausa(String titulo, String descricao, Runnable acao) {
        Label rotulo = new Label(titulo);
        rotulo.getStyleClass().add("opcao-titulo");
        Label detalhe = new Label(descricao);
        detalhe.setWrapText(true);
        detalhe.getStyleClass().add("opcao-descricao");
        Button botao = new Button();
        botao.setGraphic(new VBox(2, rotulo, detalhe));
        botao.getStyleClass().add("opcao");
        configurarBotao(botao);
        botao.setOnAction(evento -> acao.run());
        return botao;
    }

    private Node menuDaPausa() {
        VBox cartao = cartaoDaPausa("PAUSA");
        cartao.getChildren().addAll(
                botaoDaPausa("Continuar", "Volta exatamente de onde você parou.", this::fecharPausa),
                botaoDaPausa("Reiniciar partida", "Começa uma partida nova, do zero.",
                        () -> mostrarNaPausa(confirmacaoDaPausa("Reiniciar a partida?",
                                "O progresso que você não salvou será perdido.",
                                () -> abandonarPartida(true)))),
                botaoDaPausa("Salvar progresso", "Grava o ponto em que você pausou.",
                        () -> { if (acoesDePausa != null) acoesDePausa.salvar(); }),
                botaoDaPausa("Sair", "Volta ao menu principal.",
                        () -> mostrarNaPausa(confirmacaoDaPausa("Sair da partida?",
                                "O progresso que você não salvou será perdido.",
                                () -> abandonarPartida(false)))));
        surgir(cartao, 0);
        return cartao;
    }

    private Node confirmacaoDaPausa(String titulo, String texto, Runnable confirmar) {
        VBox cartao = cartaoDaPausa(titulo);
        Label aviso = new Label(texto);
        aviso.setWrapText(true);
        aviso.getStyleClass().add("painel-texto");
        cartao.getChildren().addAll(aviso,
                botaoDaPausa("Sim", "Tenho certeza.", confirmar),
                botaoDaPausa("Voltar", "Volta ao menu de pausa.", () -> mostrarNaPausa(menuDaPausa())));
        return cartao;
    }

    // Reiniciar ou sair: fecha a pausa e devolve ao jogo a interrupção no lugar da resposta que ele esperava
    private void abandonarPartida(boolean reiniciar) {
        fecharPausa();
        aoTeclar = null;
        escolhas.getChildren().clear();
        if (acoesDePausa == null) return;
        if (reiniciar) acoesDePausa.reiniciar(); else acoesDePausa.sair();
    }

    private void configurarBotao(Button botao) {
        botao.setWrapText(true);
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setMinHeight(Region.USE_PREF_SIZE);
        botao.setAlignment(Pos.CENTER_LEFT);
        botao.setFocusTraversable(false);
    }

    // =====================================================================
    // Mecânica
    // =====================================================================

    private void mostrarNaCamada(Node conteudo, Pos posicao) {
        boolean menu = posicao == Pos.CENTER_LEFT;
        camada.getChildren().setAll(conteudo);
        StackPane.setAlignment(conteudo, posicao);
        StackPane.setMargin(conteudo, menu ? new Insets(0, 0, 0, 110) : new Insets(0));
        camada.getStyleClass().setAll("camada", menu ? "camada-menu" : "camada-painel");
        veu.setOpacity(menu ? 0.30 : 1.0);   // no menu a arte aparece quase sem véu
        camada.setVisible(true);
    }

    private void esconderCamada() {
        camada.getChildren().clear();
        camada.setVisible(false);
    }

    private void completar(CompletableFuture<Integer> resposta, int valor) {
        if (resposta.isDone()) return; // clique duplo
        aoTeclar = null;
        escolhas.getChildren().clear();
        esconderCamada();
        resposta.complete(valor);
    }

    // Teclado: números escolhem a opção correspondente; Enter/Espaço avançam quando há uma única opção
    private void definirTeclas(Pergunta pergunta, CompletableFuture<Integer> resposta) {
        List<OpcaoFx> opcoes = pergunta.opcoes();
        aoTeclar = evento -> {
            boolean avancar = evento.getCode() == KeyCode.ENTER || evento.getCode() == KeyCode.SPACE;
            if (avancar && opcoes.size() == 1) {
                completar(resposta, opcoes.get(0).valor());
                return;
            }
            for (OpcaoFx opcao : opcoes) {
                if (opcao.disponivel() && String.valueOf(opcao.valor()).equals(evento.getText())) {
                    completar(resposta, opcao.valor());
                    return;
                }
            }
        };
    }

    private void definirFundo(String cenario) {
        String chave = cenario == null ? "" : cenario;
        if (chave.equals(cenarioAtual)) return;
        cenarioAtual = chave;

        Image imagem = fundos.obter(cenario);
        if (imagem == null) {
            fundo.setBackground(null);
            return;
        }
        fundo.setBackground(new Background(new BackgroundImage(imagem,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(1.0, 1.0, true, true, false, true))));
        surgir(fundo, 0);
    }

    private void surgir(Node no, double atrasoEmMs) {
        FadeTransition transicao = new FadeTransition(Duration.millis(380), no);
        transicao.setFromValue(0);
        transicao.setToValue(1);
        transicao.setDelay(Duration.millis(atrasoEmMs));
        transicao.play();
    }

    private void rolarParaOFim() {
        Platform.runLater(() -> rolagem.setVvalue(1.0));
    }
}
