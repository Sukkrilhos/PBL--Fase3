package controller;

import model.persistence.GerenciadorDeSave;
import model.service.PersonagemService;
import view.Dialogos;

public class GerenciadorDeSessao {
    private final Dialogos dialogos;
    private final GerenciadorDeSave gerenciadorDeSave;

    public GerenciadorDeSessao(Dialogos dialogos, GerenciadorDeSave gerenciadorDeSave) {
        this.dialogos = dialogos;
        this.gerenciadorDeSave = gerenciadorDeSave;
    }

    public boolean lidarComGameOver(PersonagemService personagemService) {
        if (personagemService.isGameOver()) {
            dialogos.mostrarGameOver();
            oferecerOpcoesDeFimDeJogo();
            return true;
        }
        return false;
    }

    public void lidarComFimDeCampanha() {
        dialogos.mostrarFimDaCampanha();
        oferecerOpcoesDeFimDeJogo();
    }

    private void oferecerOpcoesDeFimDeJogo() {
        dialogos.mostrarOpcoesFimDeJogo();

        int escolha = dialogos.lerInteiroSeguro();

        if (escolha == 1) {
            reiniciarPartida();
        } else {
            dialogos.mostrarRetornoAoMenu();
        }
    }

    private void reiniciarPartida() {
        dialogos.mostrarReinicioDaJornada();
        gerenciadorDeSave.apagarSave();
        throw new ReiniciarPartidaException();
    }
}
