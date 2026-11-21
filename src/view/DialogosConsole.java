package view;

import model.Personagem;
import model.Progresso;

import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Interface de console: lê o teclado e imprime menus, status e mensagens de fluxo.
 * O texto da história vem de {@link Narrativa}, exibido por {@link SaidaConsole}.
 */
public class DialogosConsole implements Dialogos {
    private Scanner scanner;
    private GerenciadorDeAudio audio;

    public DialogosConsole(GerenciadorDeAudio audio) {
        this.scanner = new Scanner(System.in);
        this.audio = audio;
    }

    public void mostrarMenuPrincipal() {
        System.out.println("\n===== Ponto Zero =====");
        for (int i = 0; i < Conteudo.MENU_PRINCIPAL.size(); i++) {
            System.out.println((i + 1) + ". " + Conteudo.MENU_PRINCIPAL.get(i));
        }
        System.out.print("Escolha uma opção: ");
    }
    public String pedirNomePersonagem() {
        System.out.println("\n========================================================");
        System.out.println("             PERSONALIZAÇÃO DO PROTAGONISTA");
        System.out.println("========================================================");
        System.out.print("Digite o nome do protagonista (Pressione Enter para 'Valter'): ");

        String entrada = scanner.nextLine().trim();
        if (entrada.isEmpty()) {
            entrada = "Valter";
        }
        audio.tocarSom("sons/menu.wav");
        return entrada;
    }

    public int escolherAntecedente() {
        System.out.println("\nEscolha a Origem / Perfil de " + "origem do seu personagem:");
        for (int i = 0; i < Conteudo.ORIGENS.size(); i++) {
            Conteudo.Origem origem = Conteudo.ORIGENS.get(i);
            System.out.println((i + 1) + " - " + origem.titulo() + " (" + origem.descricao() + ")");
        }
        System.out.print("Sua escolha: ");
        return lerOpcao(1, 3);
    }
    public void mostrarGaleriaConquistas(Predicate<String> possuiConquista) {
        audio.tocarSom("sons/menu.wav");
        System.out.println("\n========================================================");
        System.out.println("                GALERIA DE CONQUISTAS");
        System.out.println("========================================================");

        for (Conteudo.ConquistaInfo conquista : Conteudo.CONQUISTAS) {
            imprimirConquista(conquista.id(), conquista.descricao(), possuiConquista);
        }

        System.out.println("========================================================\n");
        pausarParaLeitura();
    }

    private void imprimirConquista(String id, String descricao, Predicate<String> possuiConquista) {
        if (possuiConquista.test(id)) {
            System.out.println("[DESBLOQUEADO] \uD83C\uDFC6 " + descricao); // Mostra um troféu!
        } else {
            System.out.println("[BLOQUEADO]    ???");
        }
    }

    public void mostrarInstrucoes() {
        System.out.println("\n========================================================");
        System.out.println("                      INSTRUÇÕES");
        System.out.println("========================================================");
        for (String linha : Conteudo.INSTRUCOES) {
            System.out.println(linha);
        }
        System.out.println("========================================================\n");
    }

    public void mostrarCreditos() {
        System.out.println("\n========================================================");
        System.out.println("                       CRÉDITOS");
        System.out.println("========================================================");
        for (String linha : Conteudo.CREDITOS) {
            System.out.println(linha);
        }
        System.out.println("========================================================\n");
    }

    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }

    public void pausarParaLeitura() {
        System.out.println("(Pressione Enter para continuar...)");
        scanner.nextLine();
    }

    public void limparTela() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    public void mostrarStatus(Personagem p) {
        System.out.println("\n=======================================");
        System.out.println("          STATUS DE " + p.getNome().toUpperCase());
        System.out.println("=======================================");
        System.out.println("Dinheiro: R$ " + p.getDinheiro());
        System.out.println("Saúde: " + p.getSaude() + "%");
        System.out.println("Moral/Ética: " + p.getMoralEtica() + "%");
        System.out.println("Relação Familiar: " + p.getRelacaoFamiliar() + "%");
        System.out.println("=======================================\n");
    }

    public void mostrarConquistaDesbloqueada() {
        System.out.println("\n*** CONQUISTA DESBLOQUEADA! Verifique a Galeria no Menu Principal. ***");
    }

    public void mostrarOpcoesFimDeJogo() {
        System.out.println("\nO que deseja fazer agora?");
        System.out.println("1 - Reiniciar Partida (Apagar progresso atual)");
        System.out.println("2 - Voltar ao Menu Principal");
        System.out.print("Sua escolha: ");
    }

    // --- Mensagens de fluxo (antes montadas dentro dos controllers) ---

    public void mostrarDespedida() {
        System.out.println("Encerrando o sistema. Até a próxima!");
    }

    public void mostrarOpcaoInvalida() {
        System.out.println("Opção inválida!");
    }

    public void mostrarReinicioDoSistema() {
        System.out.println("\n--- REINICIANDO SISTEMA A PARTIR DO ZERO ---");
    }

    public void mostrarInicioDeJogo(String nome, String antecedente) {
        System.out.println("\nIniciando 'PONTO ZERO' com " + nome + " [" + antecedente + "]...");
    }

    public void mostrarJogoCarregado(String nomeSave, int temporada) {
        System.out.println("\nJogo '" + nomeSave + "' carregado com sucesso!");
        System.out.println("Retomando a partir da Temporada " + temporada);
    }

    public void mostrarSaveApagado(String nomeSave) {
        System.out.println("\nSave '" + nomeSave + "' apagado permanentemente!");
    }

    public void mostrarProgressoSalvoAutomaticamente(int temporada) {
        System.out.println("\n[Progresso guardado automaticamente após a Temporada " + temporada + "]");
    }

    public void mostrarGameOver() {
        System.out.println("\nGAME OVER: O seu corpo cedeu ao cansaço ou a sua família abandonou-o.");
    }

    public void mostrarFimDaCampanha() {
        System.out.println("\n[Fim da Campanha. Obrigado por jogar 'Ponto de Ebulição'!]");
    }

    public void mostrarReinicioDaJornada() {
        System.out.println("\nLimpando o progresso anterior e reiniciando a jornada...");
    }

    public void mostrarRetornoAoMenu() {
        System.out.println("Retornando ao Menu Principal...\n");
    }

    public int lerInteiroSeguro() {
        while (!scanner.hasNextInt()) {
            System.out.println("Entrada inválida! Digite o número correspondente.");
            scanner.next();
        }
        int valor = scanner.nextInt();
        scanner.nextLine();

        // Efeito sonoro de clique ao inserir um número válido
        audio.tocarSom("sons/menu.wav");

        return valor;
    }

    public int lerOpcao(int min, int max) {
        int valor;
        while (true) {
            valor = lerInteiroSeguro();
            if (valor >= min && valor <= max) {
                return valor;
            }
            System.out.print("Opção inválida! Escolha um número entre " + min + " e " + max + ": ");
        }
    }

    public String lerTexto() {
        String texto = scanner.nextLine().trim();
        while (texto.isEmpty()) {
            texto = scanner.nextLine().trim();
        }

        // Som de confirmação ao inserir texto
        audio.tocarSom("sons/menu.wav");

        return texto;
    }

    public String pedirNomeSave() {
        System.out.println("\n--- NOVO JOGO ---");
        System.out.print("Digite um nome para o seu save (ex: 'Playthrough 1', 'Rota Maligna'): ");
        return lerTexto();
    }

    public Progresso mostrarMenuDeSaves(List<Progresso> saves) {
        if (saves.isEmpty()) {
            System.out.println("\nNenhum jogo salvo encontrado!");
            return null;
        }

        System.out.println("\n=== JOGOS SALVOS ===");
        for (int i = 0; i < saves.size(); i++) {
            Progresso p = saves.get(i);
            System.out.println((i + 1) + ". " + p.getNomeSave() + " | Temp " + p.getTemporadaAtual() + " | Último save: " + p.getDataHora());
        }
        System.out.println("0. Voltar ao Menu Principal");
        System.out.print("Escolha o save que deseja carregar: ");

        int escolha;
        while (true) {
            escolha = lerInteiroSeguro();
            if (escolha >= 0 && escolha <= saves.size()) {
                break;
            }
            System.out.print("Opção inválida! Escolha um número da lista: ");
        }

        if (escolha == 0) return null;
        return saves.get(escolha - 1);
    }
    public int mostrarOpcoesDoSave(String nomeSave) {
        System.out.println("\n=== OPÇÕES: " + nomeSave + " ===");
        System.out.println("1 - Carregar Partida");
        System.out.println("2 - Apagar Save");
        System.out.println("0 - Voltar à Lista");
        System.out.print("Sua escolha: ");
        return lerOpcao(0, 2); // Aproveita o nosso validador para aceitar 0, 1 ou 2
    }

}
