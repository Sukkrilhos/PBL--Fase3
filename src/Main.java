import controller.Controlador;
import model.persistence.GerenciadorDeConquistas;
import model.persistence.GerenciadorDeSave;
import model.service.PersonagemService;
import view.Dialogos;
import view.DialogosConsole;
import view.GerenciadorDeAudio;
import view.Narrativa;
import view.SaidaConsole;

public class Main {
    public static void main(String[] args) {
        // View: áudio, interação e narrativa
        GerenciadorDeAudio audio = new GerenciadorDeAudio();
        Dialogos dialogos = new DialogosConsole(audio);
        Narrativa narrativa = new Narrativa(audio, new SaidaConsole());

        // Model: o Model nunca escreve no console; falhas de gravação/leitura são entregues
        // a este ouvinte, que as repassa à View
        GerenciadorDeSave gerenciadorDeSave = new GerenciadorDeSave(dialogos::mostrarMensagem);
        GerenciadorDeConquistas gerenciadorDeConquistas = new GerenciadorDeConquistas(dialogos::mostrarMensagem);
        PersonagemService personagemService = new PersonagemService(gerenciadorDeSave, gerenciadorDeConquistas);

        // Controller
        Controlador controlador = new Controlador(dialogos, narrativa, personagemService, gerenciadorDeSave);
        controlador.iniciarSistema();
    }
}
