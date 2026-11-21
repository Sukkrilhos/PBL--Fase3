package view.fx.jfx;

import controller.Controlador;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import model.persistence.GerenciadorDeConquistas;
import model.persistence.GerenciadorDeSave;
import model.service.PersonagemService;
import view.GerenciadorDeAudio;
import view.Narrativa;
import view.fx.DialogosFx;
import view.fx.JogoEncerradoException;
import view.fx.PonteDeInterface;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Aplicação JavaFX: monta o mesmo Model e o mesmo Controller do console, troca só a interface
 * (DialogosFx + JavaFxTela) e roda o jogo numa thread própria, enquanto a janela fica livre.
 */
public class JanelaFx extends Application {
    private PonteDeInterface ponte;

    public static void iniciar(String[] args) {
        launch(JanelaFx.class, args);
    }

    @Override
    public void start(Stage palco) {
        JavaFxTela tela = new JavaFxTela(palco);
        ponte = new PonteDeInterface(Platform::runLater);
        palco.setOnCloseRequest(evento -> ponte.encerrar());

        GerenciadorDeAudio audio = new GerenciadorDeAudio();

        // Falhas de gravação/leitura viram avisos na janela (o Model nunca escreve no console)
        AtomicReference<DialogosFx> dialogosPronto = new AtomicReference<>();
        Consumer<String> aoOcorrerErro = mensagem -> {
            DialogosFx dialogos = dialogosPronto.get();
            if (dialogos != null) dialogos.mostrarMensagem(mensagem);
            else System.err.println(mensagem);
        };

        GerenciadorDeSave gerenciadorDeSave = new GerenciadorDeSave(aoOcorrerErro);
        GerenciadorDeConquistas gerenciadorDeConquistas = new GerenciadorDeConquistas(aoOcorrerErro);
        PersonagemService personagemService = new PersonagemService(gerenciadorDeSave, gerenciadorDeConquistas);

        DialogosFx dialogos = new DialogosFx(tela, ponte, audio, personagemService::getPersonagem);
        dialogosPronto.set(dialogos);
        // "Salvar progresso" no menu de pausa grava o ponto de retomada do capítulo em que o jogador está
        dialogos.aoSalvar(personagemService::salvarCheckpoint);
        Narrativa narrativa = new Narrativa(audio, dialogos);
        Controlador controlador = new Controlador(dialogos, narrativa, personagemService, gerenciadorDeSave);

        tela.mostrar();

        Thread jogo = new Thread(() -> {
            try {
                controlador.iniciarSistema();
            } catch (JogoEncerradoException janelaFechada) {
                // o jogador fechou a janela no meio da partida: é o fim esperado
            } catch (RuntimeException erro) {
                erro.printStackTrace();
            } finally {
                Platform.exit();
            }
        }, "thread-do-jogo");
        jogo.setDaemon(true);
        jogo.start();
    }

    @Override
    public void stop() {
        if (ponte != null) ponte.encerrar();
    }
}
