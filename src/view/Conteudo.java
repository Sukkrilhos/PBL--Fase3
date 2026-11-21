package view;

import model.Conquistas;

import java.util.List;

/** Textos fixos do jogo compartilhados pelas interfaces (console e JavaFX), para não duplicá-los. */
public final class Conteudo {
    private Conteudo() { }

    public static final String NOME_DO_JOGO = "Ponto de Ebulição";

    public static final List<String> MENU_PRINCIPAL = List.of(
            "Novo Jogo", "Carregar Jogo", "Instruções", "Créditos", "Galeria de Conquistas", "Sair");

    public static final List<String> INSTRUCOES = List.of(
            "Suas escolhas moldam quatro atributos essenciais:",
            "- Dinheiro | Saúde | Moral/Ética | Relação Familiar",
            "CUIDADO: Se a Saúde ou Relação Familiar chegarem a zero,",
            "seu jogo terminará imediatamente (Game Over).");

    public static final List<String> CREDITOS = List.of(
            "Projeto PBL - EXA863",
            "Arquitetura MVC baseada em Java",
            "Desenvolvido por:\n Matheus de Almeida Muniz Rodrigues\n Gustavo Silva Ribeiro\n Camili Carneiro de Oliveira");

    /** Uma origem que o jogador pode escolher (título e o que ela muda no jogo). */
    public record Origem(String titulo, String descricao) { }

    public static final List<Origem> ORIGENS = List.of(
            new Origem("Professor Tradicional", "Equilibrado: Status Padrão"),
            new Origem("Ex-Químico Industrial", "Começa com R$ 3.000 extras, mas -10 Moral"),
            new Origem("Químico Precavido", "Começa com o item 'Máscara de Gás' no inventário"));

    /** Uma conquista da galeria: o ID gravado no save e a descrição mostrada quando desbloqueada. */
    public record ConquistaInfo(String id, String descricao) { }

    public static final List<ConquistaInfo> CONQUISTAS = List.of(
            new ConquistaInfo(Conquistas.ROTA_A, "O Homem Honesto (Desistiu do crime no início)"),
            new ConquistaInfo(Conquistas.ROTA_B_MORTE, "A Queda (Morreu durante a emboscada na fuga)"),
            new ConquistaInfo(Conquistas.ROTA_B_FUGA, "O Desaparecimento (Fugiu com identidades falsas)"),
            new ConquistaInfo(Conquistas.ROTA_B_PRISAO, "Atrás das Grades (Sobreviveu com colete, mas foi preso)"),
            new ConquistaInfo(Conquistas.ROTA_C_KAMIKAZE, "Reação em Cadeia (Explodiu o laboratório)"),
            new ConquistaInfo(Conquistas.ROTA_C_RICINA, "O Toque Silencioso (Envenenou todos com Ricina)"),
            new ConquistaInfo(Conquistas.ROTA_D_DELACAO, "O Acordo (Entregou o Cartel à polícia)"));
}
