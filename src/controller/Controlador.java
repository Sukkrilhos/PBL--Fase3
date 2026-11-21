package controller;

import controller.temporada.TemporadaCinco;
import controller.temporada.TemporadaDois;
import controller.temporada.TemporadaQuatro;
import controller.temporada.TemporadaTres;
import controller.temporada.TemporadaUm;
import model.Personagem;
import model.Progresso;
import model.persistence.GerenciadorDeSave;
import model.service.PersonagemService;
import view.Dialogos;
import view.InterrupcaoDoJogoException;
import view.Narrativa;

import java.util.List;

public class Controlador {
    private final Dialogos dialogos;
    private final Narrativa narrativa;
    private final PersonagemService personagemService;
    private final GerenciadorDeSave gerenciadorDeSave;
    private final GerenciadorDeSessao gerenciadorDeSessao;

    private final TemporadaUm temporadaUm;
    private final TemporadaDois temporadaDois;
    private final TemporadaTres temporadaTres;
    private final TemporadaQuatro temporadaQuatro;
    private final TemporadaCinco temporadaCinco;

    public Controlador(Dialogos dialogos, Narrativa narrativa, PersonagemService personagemService,
                       GerenciadorDeSave gerenciadorDeSave) {
        this.dialogos = dialogos;
        this.narrativa = narrativa;
        this.personagemService = personagemService;
        this.gerenciadorDeSave = gerenciadorDeSave;
        this.gerenciadorDeSessao = new GerenciadorDeSessao(dialogos, gerenciadorDeSave);

        this.temporadaUm = new TemporadaUm(dialogos, narrativa, personagemService);
        this.temporadaDois = new TemporadaDois(dialogos, narrativa, personagemService);
        this.temporadaTres = new TemporadaTres(dialogos, narrativa, personagemService);
        this.temporadaQuatro = new TemporadaQuatro(dialogos, narrativa, personagemService);
        this.temporadaCinco = new TemporadaCinco(dialogos, narrativa, personagemService);
    }

    public void iniciarSistema() {
        int opcao;
        do {
            dialogos.mostrarMenuPrincipal();
            opcao = dialogos.lerInteiroSeguro();

            switch (opcao) {
                case 1:
                    tentarIniciarJogo();
                    break;

                case 2:
                    tentarCarregarJogo();
                    break;

                case 3:
                    dialogos.mostrarInstrucoes();
                    break;

                case 4:
                    dialogos.mostrarCreditos();
                    break;

                case 5:
                    dialogos.mostrarGaleriaConquistas(personagemService::possuiConquista);
                    break;

                case 6:
                    dialogos.mostrarDespedida();
                    dialogos.limparTela();
                    break;

                default:
                    dialogos.mostrarOpcaoInvalida();
            }
        } while (opcao != 6);
    }

    // Tenta iniciar um novo jogo pedindo o nome do save
    private void tentarIniciarJogo() {
        boolean jogando = true;
        while (jogando) {
            try {
                // ANTES DE INICIAR O JOGO, PEDE O NOME DO SAVE
                String nomeSave = dialogos.pedirNomeSave();
                gerenciadorDeSave.setNomeSaveAtual(nomeSave);

                iniciarJogo();
                jogando = false; // Se chegar ao fim sem exceções, sai do ciclo
            } catch (ReiniciarPartidaException e) {
                dialogos.limparTela();
                dialogos.mostrarReinicioDoSistema();
                // Repete o ciclo, pedindo o nome do save novamente para a nova tentativa
            } catch (InterrupcaoDoJogoException pausa) {
                dialogos.limparTela();
                if (pausa.getAcao() == InterrupcaoDoJogoException.Acao.REINICIAR) {
                    dialogos.mostrarReinicioDoSistema();   // o menu de pausa pediu uma partida nova: repete o ciclo
                } else {
                    dialogos.mostrarRetornoAoMenu();
                    jogando = false;
                }
            }
        }
    }

    // Tenta carregar um jogo a partir da lista
    private void tentarCarregarJogo() {
        try {
            carregarJogoSalvo();
        } catch (ReiniciarPartidaException e) {
            dialogos.limparTela();
            dialogos.mostrarReinicioDoSistema();
            tentarIniciarJogo(); // Volta para o Novo Jogo limpo
        } catch (InterrupcaoDoJogoException pausa) {
            dialogos.limparTela();
            if (pausa.getAcao() == InterrupcaoDoJogoException.Acao.REINICIAR) {
                dialogos.mostrarReinicioDoSistema();
                tentarIniciarJogo();
            } else {
                dialogos.mostrarRetornoAoMenu();
            }
        }
    }

    private void iniciarJogo() {
        // Fluxo de Personalização
        String nomePersonagem = dialogos.pedirNomePersonagem();
        int opcaoAntecedente = dialogos.escolherAntecedente();

        Personagem jogador = personagemService.inicializarPersonagemCustomizado(nomePersonagem, opcaoAntecedente);

        // Desde já o botão de pausa pode salvar: o ponto de retomada é o começo da campanha
        personagemService.registrarCheckpoint(1, 0, 0);

        dialogos.mostrarInicioDeJogo(jogador.getNome(), jogador.getAntecedente());
        dialogos.pausarParaLeitura();

        // Introdução: quem é o protagonista, de acordo com a origem escolhida
        narrativa.mostrarIntroducaoParte1(jogador.getNome(), jogador.getAntecedente());
        dialogos.pausarParaLeitura();
        narrativa.mostrarIntroducaoParte2(jogador.getNome(), jogador.getAntecedente());
        dialogos.pausarParaLeitura();

        executarCampanha(1, 0, 0);
    }

    private void carregarJogoSalvo() {
        boolean noMenuSaves = true;

        while (noMenuSaves) {
            // Busca a lista atualizada a cada ciclo
            List<Progresso> savesDisponiveis = gerenciadorDeSave.listarSaves();
            Progresso saveEscolhido = dialogos.mostrarMenuDeSaves(savesDisponiveis);

            if (saveEscolhido != null) {
                // Pergunta o que fazer com o save escolhido
                int acao = dialogos.mostrarOpcoesDoSave(saveEscolhido.getNomeSave());

                if (acao == 1) { // 1 - CARREGAR
                    gerenciadorDeSave.setNomeSaveAtual(saveEscolhido.getNomeSave());
                    personagemService.setPersonagem(saveEscolhido.getPersonagem());

                    int temporada = saveEscolhido.getTemporadaAtual();
                    int capitulo = saveEscolhido.getCapitulo();
                    int rotaFinal = saveEscolhido.getRotaFinal();
                    // Sem a rota gravada não dá para jogar a Temporada 5: volta à 4, onde a rota é escolhida
                    if (temporada >= 5 && rotaFinal == 0) {
                        temporada = 4;
                        capitulo = 0;
                    }
                    // Na Temporada 4, só se pode pular a encruzilhada (capítulo 2) se a rota dela foi gravada
                    if (temporada == 4 && capitulo >= 2 && rotaFinal == 0) {
                        capitulo = 1;
                    }

                    dialogos.mostrarJogoCarregado(saveEscolhido.getNomeSave(), temporada);
                    dialogos.pausarParaLeitura();

                    executarCampanha(temporada, capitulo, rotaFinal);
                    noMenuSaves = false; // Sai do menu e começa a jogar

                } else if (acao == 2) { // 2 - APAGAR
                    gerenciadorDeSave.setNomeSaveAtual(saveEscolhido.getNomeSave());
                    gerenciadorDeSave.apagarSave(); // Apaga o ficheiro JSON correspondente

                    dialogos.mostrarSaveApagado(saveEscolhido.getNomeSave());
                    dialogos.pausarParaLeitura();
                    // O ciclo continua e mostra a lista de saves atualizada sem o que acabou de apagar
                }
                // Se a ação for 0 (Voltar), não faz nada e o ciclo repete, mostrando a lista novamente

            } else {
                // Se o jogador digitou 0 na lista de saves, sai do menu de carregar
                noMenuSaves = false;
            }
        }
    }

    // Roda a campanha a partir do capítulo da temporada em que o jogo deve recomeçar (a Temporada 1, capítulo 0, num
    // jogo novo; o ponto gravado ao carregar um save). rotaFinalSalva só importa a partir da Temporada 4.
    private void executarCampanha(int temporadaInicial, int capituloInicial, int rotaFinalSalva) {
        Personagem valter = personagemService.getPersonagem();
        personagemService.registrarCheckpoint(temporadaInicial, capituloInicial, rotaFinalSalva);

        if (temporadaInicial <= 1) {
            if (encerrouPartida(temporadaUm.executarTemporada(valter, capituloDaTemporada(1, temporadaInicial, capituloInicial)))) return;
            concluirTemporada(1, 0);
        }
        if (temporadaInicial <= 2) {
            if (encerrouPartida(temporadaDois.executarTemporada(valter, capituloDaTemporada(2, temporadaInicial, capituloInicial)))) return;
            concluirTemporada(2, 0);
        }
        if (temporadaInicial <= 3) {
            if (encerrouPartida(temporadaTres.executarTemporada(valter, capituloDaTemporada(3, temporadaInicial, capituloInicial)))) return;
            concluirTemporada(3, 0);
        }

        int rotaFinal = rotaFinalSalva;
        if (temporadaInicial <= 4) {
            rotaFinal = temporadaQuatro.executarTemporada(valter, capituloDaTemporada(4, temporadaInicial, capituloInicial), rotaFinalSalva);
            if (gerenciadorDeSessao.lidarComGameOver(personagemService)) return;
            concluirTemporada(4, rotaFinal);
        }
        if (temporadaInicial <= 5) {
            temporadaCinco.executarTemporada(valter, rotaFinal);
            gerenciadorDeSessao.lidarComFimDeCampanha();
        }
    }

    // O capítulo inicial só vale na temporada em que o save foi feito; as seguintes começam do capítulo 0
    private static int capituloDaTemporada(int temporada, int temporadaInicial, int capituloInicial) {
        return temporada == temporadaInicial ? capituloInicial : 0;
    }

    // A temporada acabou em Game Over (a tela de fim aparece, com reiniciar ou menu) ou por um final (Rota A)
    private boolean encerrouPartida(boolean terminouNaTemporada) {
        if (gerenciadorDeSessao.lidarComGameOver(personagemService)) return true;
        return terminouNaTemporada;
    }

    // Ao concluir a temporada N, o save passa a apontar para a N+1 (e guarda a rota, se já escolhida). O aviso é
    // só uma notificação: o jogador não precisa responder nada para seguir.
    private void concluirTemporada(int temporadaConcluida, int rotaFinal) {
        mostrarStatusAtual();
        personagemService.salvarProgressoAutomatico(temporadaConcluida + 1, rotaFinal);
        personagemService.registrarCheckpoint(temporadaConcluida + 1, 0, rotaFinal);
        dialogos.mostrarProgressoSalvoAutomaticamente(temporadaConcluida);
    }

    private void mostrarStatusAtual() {
        dialogos.mostrarStatus(personagemService.getPersonagem());
        dialogos.pausarParaLeitura();
    }
}
