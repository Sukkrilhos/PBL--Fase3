package view;

import model.Personagem;
import model.Progresso;

import java.util.List;
import java.util.function.Predicate;

/**
 * Tudo o que os controllers pedem à interface do jogador: menus, perguntas, pausas e mensagens de fluxo.
 * Implementações: {@link DialogosConsole} (texto no terminal) e a de JavaFX (view.fx.DialogosFx).
 * O texto da história não passa por aqui: vem de {@link Narrativa}, via {@link Saida}.
 * Qualquer método que espere uma resposta pode lançar {@link InterrupcaoDoJogoException}, se o jogador
 * reiniciar ou sair pelo menu de pausa da janela.
 */
public interface Dialogos {
    // --- Menus e perguntas (devolvem a resposta do jogador) ---
    void mostrarMenuPrincipal();
    int lerInteiroSeguro();
    int lerOpcao(int min, int max);
    String pedirNomeSave();
    String pedirNomePersonagem();
    int escolherAntecedente();
    Progresso mostrarMenuDeSaves(List<Progresso> saves);
    int mostrarOpcoesDoSave(String nomeSave);
    void mostrarOpcoesFimDeJogo();

    // --- Telas informativas ---
    void mostrarGaleriaConquistas(Predicate<String> possuiConquista);
    void mostrarInstrucoes();
    void mostrarCreditos();
    void mostrarStatus(Personagem personagem);

    // --- Fluxo ---
    void pausarParaLeitura();
    void limparTela();
    void mostrarMensagem(String mensagem);
    void mostrarConquistaDesbloqueada();
    void mostrarDespedida();
    void mostrarOpcaoInvalida();
    void mostrarReinicioDoSistema();
    void mostrarInicioDeJogo(String nome, String antecedente);
    void mostrarJogoCarregado(String nomeSave, int temporada);
    void mostrarSaveApagado(String nomeSave);
    void mostrarProgressoSalvoAutomaticamente(int temporada);
    void mostrarGameOver();
    void mostrarFimDaCampanha();
    void mostrarReinicioDaJornada();
    void mostrarRetornoAoMenu();
}
