package controller.temporada;

import model.Itens;
import model.Personagem;
import model.service.PersonagemService;
import view.Dialogos;
import view.Narrativa;

public class TemporadaDois extends Temporada {

    public TemporadaDois(Dialogos dialogos, Narrativa narrativa, PersonagemService personagemService) {
        super(dialogos, narrativa, personagemService);
    }

    /** Capítulos: 0 = O Furgão Encurralado, 1 = O Sangue na Pia. */
    public boolean executarTemporada(Personagem valter, int capituloInicial) {
        if (capituloInicial == 0) narrativa.mostrarTituloTemporada(2);

        // --- Capítulo 0, Cena 1: O Furgão Encurralado ---
        if (capituloInicial <= 0) {
            boolean temMascara = personagemService.verificarItem(Itens.MASCARA_DE_GAS);
            narrativa.mostrarCenaFurgaoEncurraladoParte1(valter.getNome(), temMascara, valter.getAntecedente());

            int maxOpcao = temMascara ? 3 : 2;
            int escolhaFurgao = dialogos.lerOpcao(1, maxOpcao);

            narrativa.mostrarCenaFurgaoEncurraladoParte2(valter.getNome(), escolhaFurgao, valter.getAntecedente());

            if (escolhaFurgao == 1) {
                personagemService.aplicarConsequencia(-2000, 0, -10, 0);
            } else if (escolhaFurgao == 2) {
                personagemService.aplicarConsequencia(0, 0, -20, 0);
            } else if (escolhaFurgao == 3 && temMascara) {
                personagemService.aplicarConsequencia(1000, 0, -40, 0);
            }
            registrarCheckpoint(2, 1, 0);
            narrativa.mostrarResultadoFurgao(escolhaFurgao);

            if (personagemService.isGameOver()) return true;
            dialogos.pausarParaLeitura();
        }

        // --- Capítulo 1, Cena 2: O Sangue na Pia ---
        if (capituloInicial <= 1) {
            narrativa.mostrarCenaSanguePiaParte1(valter.getNome(), valter.getAntecedente());
            int escolhaPia = dialogos.lerOpcao(1, 2);

            narrativa.mostrarCenaSanguePiaParte2(valter.getNome(), escolhaPia, valter.getAntecedente());

            if (escolhaPia == 1) {
                personagemService.aplicarConsequencia(0, +10, -5, +10);
            } else {
                personagemService.aplicarConsequencia(0, -15, -10, -25);
            }
            registrarCheckpoint(2, 2, 0);
            narrativa.mostrarResultadoSanguePia(escolhaPia);

            if (personagemService.isGameOver()) return true;
            dialogos.pausarParaLeitura();
        }

        return false;
    }
}
