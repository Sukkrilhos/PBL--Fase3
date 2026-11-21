package controller.temporada;

import model.Itens;
import model.Personagem;
import model.service.PersonagemService;
import view.Dialogos;
import view.Narrativa;

public class TemporadaTres extends Temporada {

    public TemporadaTres(Dialogos dialogos, Narrativa narrativa, PersonagemService personagemService) {
        super(dialogos, narrativa, personagemService);
    }

    /** Capítulos: 0 = O Novo Esconderijo, 1 = O Atacadista. */
    public boolean executarTemporada(Personagem valter, int capituloInicial) {
        if (capituloInicial == 0) narrativa.mostrarTituloTemporada(3);

        // --- Capítulo 0, CAPÍTULO 5: O Novo Esconderijo ---
        if (capituloInicial <= 0) {
            narrativa.mostrarCenaNovoEsconderijoParte1(valter.getNome(), valter.getAntecedente());
            int escolhaEsconderijo = dialogos.lerOpcao(1, 3);

            if (escolhaEsconderijo == 1) {
                personagemService.aplicarConsequencia(0, -25, -5, 0);
            } else if (escolhaEsconderijo == 2) {
                personagemService.aplicarConsequencia(0, 0, -20, 0);
            } else if (escolhaEsconderijo == 3) {
                personagemService.aplicarConsequencia(0, -15, -15, 0);
                personagemService.adicionarItemInventario(Itens.RICINA);
            }
            registrarCheckpoint(3, 1, 0);
            narrativa.mostrarResultadoEsconderijo(escolhaEsconderijo);

            if (personagemService.isGameOver()) return true;
            dialogos.pausarParaLeitura();
        }

        // --- Capítulo 1, CAPÍTULO 6: O Atacadista ---
        if (capituloInicial <= 1) {
            narrativa.mostrarCenaOAtacadistaParte1(valter.getNome(), valter.getAntecedente(),
                    personagemService.podePagar(Itens.PRECO_PASSAPORTES_FALSOS));
            int escolhaAtacadista = lerOpcaoComCompra(1, 3, 3, Itens.PRECO_PASSAPORTES_FALSOS);

            if (escolhaAtacadista == 1) {
                personagemService.aplicarConsequencia(10000, -30, -15, -15);
            } else if (escolhaAtacadista == 2) {
                personagemService.aplicarConsequencia(3000, 0, -10, 0);
            } else if (escolhaAtacadista == 3) {
                personagemService.aplicarConsequencia(-Itens.PRECO_PASSAPORTES_FALSOS, 0, 0, 0);
                personagemService.adicionarItemInventario(Itens.PASSAPORTES_FALSOS);
            }
            registrarCheckpoint(3, 2, 0);
            narrativa.mostrarResultadoAtacadista(valter.getNome(), escolhaAtacadista, valter.getAntecedente());

            if (personagemService.isGameOver()) return true;
            dialogos.pausarParaLeitura();
        }

        return false;
    }
}
