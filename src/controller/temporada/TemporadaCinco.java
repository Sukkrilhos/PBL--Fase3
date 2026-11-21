package controller.temporada;

import model.Conquistas;
import model.Itens;
import model.Personagem;
import model.service.PersonagemService;
import view.Dialogos;
import view.Narrativa;

public class TemporadaCinco extends Temporada {

    public TemporadaCinco(Dialogos dialogos, Narrativa narrativa, PersonagemService personagemService) {
        super(dialogos, narrativa, personagemService);
    }

    public void executarTemporada(Personagem valter, int escolhaRotaFinal) {
        narrativa.mostrarTituloTemporada(5);
        dialogos.pausarParaLeitura();

        if (escolhaRotaFinal == 1) {
            // --- ROTA B: A QUEDA OU O DESAPARECIMENTO ---
            if (personagemService.verificarItem(Itens.PASSAPORTES_FALSOS)) {
                desbloquearConquista(Conquistas.ROTA_B_FUGA);
                narrativa.mostrarCenaFugaSucesso(valter.getNome(), valter.getAntecedente());
                narrativa.mostrarFimRotaBSucesso();
            } else if (personagemService.verificarItem(Itens.COLETE_BALISTICO)) {
                desbloquearConquista(Conquistas.ROTA_B_PRISAO);
                narrativa.mostrarCenaPrisao(valter.getNome(), valter.getAntecedente());
                personagemService.aplicarConsequencia(-valter.getDinheiro(), -50, 0, -50);
                narrativa.mostrarFimRotaBSobrevivencia();
            } else {
                desbloquearConquista(Conquistas.ROTA_B_MORTE);
                narrativa.mostrarCenaFuga(valter.getNome(), valter.getAntecedente());
                personagemService.aplicarConsequencia(-valter.getDinheiro(), -valter.getSaude(), 0, -valter.getRelacaoFamiliar());
                narrativa.mostrarFimRotaBMorte();
            }

        } else if (escolhaRotaFinal == 2) {
            // --- ROTA C: A VINGANÇA EXPLOSIVA OU SILENCIOSA ---
            if (personagemService.verificarItem(Itens.RICINA)) {
                desbloquearConquista(Conquistas.ROTA_C_RICINA);
                narrativa.mostrarCenaRicina(valter.getNome(), valter.getAntecedente());
                narrativa.mostrarFimRotaCSucesso();
            } else {
                desbloquearConquista(Conquistas.ROTA_C_KAMIKAZE);
                narrativa.mostrarCenaAtaque(valter.getNome(), valter.getAntecedente());
                narrativa.mostrarFimRotaCKamikaze();
            }

        } else if (escolhaRotaFinal == 3) {
            // --- ROTA D: A DELAÇÃO PREMIADA ---
            if (personagemService.verificarItem(Itens.DOSSIE_DO_CARTEL)) {
                desbloquearConquista(Conquistas.ROTA_D_DELACAO);
                narrativa.mostrarCenaDelacao(valter.getNome(), valter.getAntecedente());
                personagemService.aplicarConsequencia(-valter.getDinheiro(), 0, +50, +20); // Perde o dinheiro, ganha moral e relação
                narrativa.mostrarFimRotaDRendicao();
            }
        }

        dialogos.pausarParaLeitura();
    }
}
