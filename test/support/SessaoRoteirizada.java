package support;

import controller.Controlador;
import model.persistence.GerenciadorDeConquistas;
import model.persistence.GerenciadorDeSave;
import model.service.PersonagemService;
import view.Dialogos;
import view.DialogosConsole;
import view.GerenciadorDeAudio;
import view.Narrativa;
import view.SaidaConsole;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

/**
 * Joga de verdade pelo Controlador com a entrada do teclado roteirizada (uma linha por resposta).
 * Cada chamada a jogar() é uma "sessão": objetos novos, mesma pasta de saves, como se o jogador
 * tivesse fechado e aberto o jogo. Chame restaurar() no @AfterEach para devolver System.in/System.out.
 */
public class SessaoRoteirizada {
    private final Path pasta;
    private final InputStream entradaOriginal = System.in;
    private final PrintStream saidaOriginal = System.out;

    public SessaoRoteirizada(Path pasta) {
        this.pasta = pasta;
    }

    public static String[] juntar(String[]... partes) {
        List<String> todas = new ArrayList<>();
        for (String[] parte : partes) todas.addAll(Arrays.asList(parte));
        return todas.toArray(new String[0]);
    }

    /** Roda uma sessão e devolve tudo o que o jogo imprimiu. Quando a entrada acaba, o jogador "fechou o jogo". */
    public String jogar(String... linhas) {
        return jogar((audio, service) -> new DialogosConsole(audio), linhas);
    }

    /** Igual a jogar(), mas com o Dialogos que o teste quiser (por exemplo, um que "aperta a pausa" no meio do jogo). */
    public String jogar(BiFunction<GerenciadorDeAudio, PersonagemService, Dialogos> fabricaDeDialogos, String... linhas) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setIn(new ByteArrayInputStream((String.join("\n", linhas) + "\n").getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            GerenciadorDeAudio audio = GerenciadorDeAudio.mudo();
            Narrativa narrativa = new Narrativa(audio, new SaidaConsole());
            GerenciadorDeSave save = new GerenciadorDeSave(pasta.resolve("saves"), null);
            GerenciadorDeConquistas conquistas = new GerenciadorDeConquistas(
                    pasta.resolve("saves").resolve(GerenciadorDeConquistas.NOME_ARQUIVO), null);
            PersonagemService service = new PersonagemService(save, conquistas);
            Dialogos dialogos = fabricaDeDialogos.apply(audio, service);
            new Controlador(dialogos, narrativa, service, save).iniciarSistema();
        } catch (NoSuchElementException fimDaEntrada) {
            // o roteiro acabou no meio do jogo
        } finally {
            System.setOut(saidaOriginal);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    public void restaurar() {
        System.setIn(entradaOriginal);
        System.setOut(saidaOriginal);
    }

    /** Captura o que um trecho de código imprime no console. */
    public static String capturarSaida(Runnable trecho) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            trecho.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
