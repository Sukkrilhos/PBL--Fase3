package controller.temporada;

import model.Itens;
import model.Personagem;
import model.service.PersonagemService;
import view.Dialogos;
import view.Narrativa;

public class TemporadaQuatro extends Temporada {

    public TemporadaQuatro(Dialogos dialogos, Narrativa narrativa, PersonagemService personagemService) {
        super(dialogos, narrativa, personagemService);
    }

    /**
     * Capítulos: 0 = O Fundo Falso, 1 = A Encruzilhada. {@code rotaSalva} é a rota já escolhida, para quando o save
     * foi feito depois da encruzilhada. Devolve a rota (1 = B, 2 = C, 3 = D) ou 0 se o jogo acabou antes.
     */
    public int executarTemporada(Personagem valter, int capituloInicial, int rotaSalva) {
        if (capituloInicial == 0) {
            narrativa.mostrarTituloTemporada(4);
            dialogos.pausarParaLeitura();
        }

        // --- Capítulo 0, CAPÍTULO 7: O Fundo Falso ---
        if (capituloInicial <= 0) {
            narrativa.mostrarCenaFundoFalsoParte1(valter.getNome(), valter.getAntecedente(),
                    personagemService.podePagar(Itens.PRECO_COLETE_BALISTICO));
            int escolhaFundo = lerOpcaoComCompra(1, 3, 3, Itens.PRECO_COLETE_BALISTICO);

            narrativa.mostrarCenaFundoFalsoParte2(valter.getNome(), escolhaFundo, valter.getAntecedente());

            if (escolhaFundo == 1) {
                personagemService.aplicarConsequencia(0, 0, -20, -35);
            } else if (escolhaFundo == 2) {
                personagemService.aplicarConsequencia(0, 0, -10, -50);
            } else if (escolhaFundo == 3) {
                personagemService.aplicarConsequencia(-Itens.PRECO_COLETE_BALISTICO, 0, -30, -70);
                personagemService.adicionarItemInventario(Itens.COLETE_BALISTICO);
            }
            registrarCheckpoint(4, 1, 0);
            narrativa.mostrarResultadoFundoFalso(escolhaFundo);

            if (personagemService.isGameOver()) return 0;
            dialogos.pausarParaLeitura();
        }

        // --- Capítulo 1, CAPÍTULO 8: A Encruzilhada / O Cerco ---
        int rota = rotaSalva;
        if (capituloInicial <= 1) {
            narrativa.mostrarCenaEncruzilhadaParte1(valter.getNome(), valter.getAntecedente());
            int escolhaEncruzilhada = dialogos.lerOpcao(1, 3);

            narrativa.mostrarCenaEncruzilhadaParte2(valter.getNome(), escolhaEncruzilhada, valter.getAntecedente());

            if (escolhaEncruzilhada == 3) {
                personagemService.aplicarConsequencia(0, 0, 10, 0); // Ganha moral por fazer a coisa certa
                personagemService.adicionarItemInventario(Itens.DOSSIE_DO_CARTEL);
            }
            rota = escolhaEncruzilhada;
            registrarCheckpoint(4, 2, rota);
            narrativa.mostrarRotaEscolhida(escolhaEncruzilhada);

            dialogos.pausarParaLeitura();
        }
        return rota; // 1, 2 ou 3, para a Temporada 5
    }
}
