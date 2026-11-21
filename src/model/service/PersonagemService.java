package model.service;

import model.Antecedentes;
import model.Itens;
import model.Personagem;
import model.Progresso;
import model.persistence.GerenciadorDeConquistas;
import model.persistence.GerenciadorDeSave;

public class PersonagemService {
    private final GerenciadorDeSave gerenciadorDeSave;
    private final GerenciadorDeConquistas conquistas;
    private Personagem personagemAtual;

    // Ponto de retomada: uma foto do personagem + onde o jogo recomeça. É o que o botão de pausa grava.
    private Personagem fotoDoCheckpoint;
    private int checkpointTemporada;
    private int checkpointCapitulo;
    private int checkpointRota;

    public PersonagemService(GerenciadorDeSave gerenciadorDeSave, GerenciadorDeConquistas conquistas) {
        this.gerenciadorDeSave = gerenciadorDeSave;
        this.conquistas = conquistas;
    }

    public Personagem inicializarPersonagemCustomizado(String nome, int opcaoAntecedente) {
        this.personagemAtual = new Personagem();
        this.personagemAtual.setNome(nome);

        switch (opcaoAntecedente) {
            case 1:
                this.personagemAtual.setAntecedente(Antecedentes.PROFESSOR_TRADICIONAL);
                break;

            case 2:
                this.personagemAtual.setAntecedente(Antecedentes.EX_QUIMICO_INDUSTRIAL);
                this.personagemAtual.alterarDinheiro(3000);
                this.personagemAtual.alterarMoral(-10);
                break;

            case 3:
                this.personagemAtual.setAntecedente(Antecedentes.QUIMICO_PRECAVIDO);
                this.personagemAtual.adicionarItem(Itens.MASCARA_DE_GAS);
                break;
        }

        limparCheckpoint();
        gerenciadorDeSave.atualizarPersonagem(this.personagemAtual);
        return this.personagemAtual;
    }

    // Regra de negócio: Atualizar atributos baseados na escolha
    public void aplicarConsequencia(int deltaDinheiro, int deltaSaude, int deltaMoral, int deltaRelacao) {
        if (this.personagemAtual == null) return;

        this.personagemAtual.alterarDinheiro(deltaDinheiro);
        this.personagemAtual.alterarSaude(deltaSaude);
        this.personagemAtual.alterarMoral(deltaMoral);
        this.personagemAtual.alterarRelacao(deltaRelacao);
        // O slot só é gravado nos pontos de retomada (fim de capítulo, de temporada ou pausa): gravar aqui
        // deixaria o save com o personagem de agora e a cena de antes, e as consequências se repetiriam ao carregar.
    }

    // Regra de negócio: a compra só é permitida se o saldo cobrir o preço
    public boolean podePagar(int preco) {
        return this.personagemAtual != null && this.personagemAtual.podePagar(preco);
    }

    // Verifica se os atributos zeraram (Game Over prematuro)
    public boolean isGameOver() {
        if (this.personagemAtual == null) return false;
        return this.personagemAtual.getSaude() <= 0 || this.personagemAtual.getRelacaoFamiliar() <= 0;
    }

    public Personagem getPersonagem() {
        return this.personagemAtual;
    }

    public void setPersonagem(Personagem personagem) {
        this.personagemAtual = personagem;
        limparCheckpoint();
    }

    // ==========================================
    // SISTEMA DE INVENTÁRIO
    // ==========================================

    public void adicionarItemInventario(String item) {
        if (this.personagemAtual != null) {
            this.personagemAtual.adicionarItem(item);
        }
    }

    public boolean verificarItem(String item) {
        return this.personagemAtual != null && this.personagemAtual.possuiItem(item);
    }

    // ==========================================
    // PONTO DE RETOMADA (botão de pausa)
    // ==========================================

    /**
     * Marca onde o jogo recomeça se for salvo agora: {@code temporada}, {@code capitulo} (0 = início da temporada;
     * o número de capítulos da temporada = temporada já concluída) e a rota escolhida (0 enquanto não houver).
     * Guarda uma foto do personagem neste instante, para o save bater com a cena.
     */
    public synchronized void registrarCheckpoint(int temporada, int capitulo, int rotaFinal) {
        if (this.personagemAtual == null) return;
        this.fotoDoCheckpoint = this.personagemAtual.copia();
        this.checkpointTemporada = temporada;
        this.checkpointCapitulo = capitulo;
        this.checkpointRota = rotaFinal;
    }

    /** Grava o último ponto de retomada no slot atual. Devolve false se não há ponto ou slot para gravar. */
    public synchronized boolean salvarCheckpoint() {
        if (this.fotoDoCheckpoint == null) return false;
        Progresso progresso = new Progresso(this.fotoDoCheckpoint.copia(), this.checkpointTemporada);
        progresso.setCapitulo(this.checkpointCapitulo);
        progresso.setRotaFinal(this.checkpointRota);
        return gerenciadorDeSave.salvarJogo(progresso);
    }

    private synchronized void limparCheckpoint() {
        this.fotoDoCheckpoint = null;
    }

    // Salvamento automático ao concluir uma temporada: grava a temporada em que o jogo deve
    // recomeçar (a próxima) e a rota já escolhida (0 se ainda não houve escolha de rota).
    public void salvarProgressoAutomatico(int proximaTemporada, int rotaFinal) {
        if (this.personagemAtual != null) {
            Progresso progresso = new Progresso(this.personagemAtual, proximaTemporada);
            progresso.setRotaFinal(rotaFinal);
            gerenciadorDeSave.salvarJogo(progresso);
        }
    }

    // ==========================================
    // CONQUISTAS
    // ==========================================

    // Registra a conquista. Devolve true se foi desbloqueada agora e false se o jogador já a possuía.
    // Avisar o jogador é responsabilidade do Controller/View (o Model não imprime nada).
    public boolean desbloquearConquista(String id) {
        if (conquistas.possuiConquista(id)) {
            return false;
        }
        conquistas.registrarConquista(id);
        return true;
    }

    public boolean possuiConquista(String id) {
        return conquistas.possuiConquista(id);
    }
}
