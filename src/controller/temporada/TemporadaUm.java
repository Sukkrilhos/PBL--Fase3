package controller.temporada;

import model.Conquistas;
import model.Itens;
import model.Personagem;
import model.service.PersonagemService;
import view.Dialogos;
import view.Narrativa;

public class TemporadaUm extends Temporada {

    public TemporadaUm(Dialogos dialogos, Narrativa narrativa, PersonagemService personagemService) {
        super(dialogos, narrativa, personagemService);
    }

    /**
     * Capítulos: 0 = O Jantar, 1 = A Escolha, 2 = A Primeira Fornada. {@code capituloInicial} é por onde começar
     * (0 num jogo novo; outro valor ao carregar um save feito pela pausa). Devolve true se a partida acabou aqui.
     */
    public boolean executarTemporada(Personagem valter, int capituloInicial) {
        if (capituloInicial == 0) narrativa.mostrarTituloTemporada(1);

        // --- Capítulo 0, Cena 1: O Jantar ---
        if (capituloInicial <= 0) {
            narrativa.mostrarCenaJantarParte1(valter.getNome(), valter.getAntecedente());
            int fala1 = dialogos.lerOpcao(1, 2);

            narrativa.mostrarCenaJantarParte2(valter.getNome(), fala1, valter.getAntecedente());
            int escolhaCriticaJantar = dialogos.lerOpcao(1, 2);

            if (escolhaCriticaJantar == 1) {
                personagemService.aplicarConsequencia(0, 0, -10, -10);
            } else {
                personagemService.aplicarConsequencia(0, 0, 0, +15);
            }
            registrarCheckpoint(1, 1, 0);
            narrativa.mostrarReacaoJantar(valter.getNome(), escolhaCriticaJantar, valter.getAntecedente());

            if (personagemService.isGameOver()) return true;
            dialogos.pausarParaLeitura();
        }

        // --- Capítulo 1, Cena 2: A Escolha ---
        if (capituloInicial <= 1) {
            narrativa.mostrarCenaAEscolha(valter.getNome(), valter.getAntecedente());
            int escolhaPrincipal = dialogos.lerOpcao(1, 2);

            if (escolhaPrincipal == 1) {
                desbloquearConquista(Conquistas.ROTA_A);

                narrativa.mostrarDesfechoRotaA(valter.getNome(), valter.getAntecedente());

                dialogos.pausarParaLeitura();
                return true;
            }

            personagemService.aplicarConsequencia(5000, 0, -30, 0);
            registrarCheckpoint(1, 2, 0);
            narrativa.mostrarAceiteDaProposta();

            if (personagemService.isGameOver()) return true;
            dialogos.pausarParaLeitura();
        }

        // --- Capítulo 2, Cena 3: A Primeira Fornada ---
        if (capituloInicial <= 2) {
            narrativa.mostrarCenaFornada(valter.getNome(), personagemService.podePagar(Itens.PRECO_MASCARA_DE_GAS));
            int escolhaFornada = lerOpcaoComCompra(1, 3, 3, Itens.PRECO_MASCARA_DE_GAS);

            if (escolhaFornada == 1) {
                personagemService.aplicarConsequencia(-500, 0, 0, 0);
            } else if (escolhaFornada == 2) {
                personagemService.aplicarConsequencia(3000, -20, -10, 0);
            } else if (escolhaFornada == 3) {
                personagemService.aplicarConsequencia(-Itens.PRECO_MASCARA_DE_GAS, 0, 0, 0);
                personagemService.adicionarItemInventario(Itens.MASCARA_DE_GAS);
            }
            registrarCheckpoint(1, 3, 0);
            narrativa.mostrarResultadoFornada(escolhaFornada);

            if (personagemService.isGameOver()) return true;
            dialogos.pausarParaLeitura();
        }

        return false;
    }
}
