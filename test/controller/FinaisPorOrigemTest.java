package controller;

import controller.temporada.TemporadaCinco;
import model.Antecedentes;
import model.Itens;
import model.Personagem;
import model.persistence.GerenciadorDeConquistas;
import model.persistence.GerenciadorDeSave;
import model.service.PersonagemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;
import support.SessaoRoteirizada;
import view.DialogosConsole;
import view.GerenciadorDeAudio;
import view.Narrativa;
import view.SaidaConsole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cada final da Temporada 5 (Rotas B, C e D) tem um texto para cada origem do protagonista. O jogo é rodado
 * pela própria TemporadaCinco, com o inventário que leva a cada final, para provar que a origem chega até a Narrativa.
 */
public class FinaisPorOrigemTest {

    // professor == null: o final do professor é só o texto comum, sem trecho próprio
    private record Final(String titulo, int rota, List<String> itens, String professor, String industrial, String precavido) { }

    private static final List<Final> FINAIS = List.of(
            new Final("Sangue no Tapete", 1, List.of(),
                    "O crime não compensou.\n", "Nem para quem sempre soube o preço", "Nem para quem passou a vida se precavendo"),
            new Final("O Desaparecimento", 1, List.of(Itens.PASSAPORTES_FALSOS),
                    "Para sempre é muito tempo", "saída de emergência", "plano B e um plano C"),
            new Final("Atrás das Grades", 1, List.of(Itens.COLETE_BALISTICO),
                    null, "linha de produção", "a sua cautela não tinha para onde ir"),
            new Final("Reação em Cadeia", 2, List.of(),
                    null, "evitar exatamente aquilo que agora provocou", "não planejou como sair"),
            new Final("O Toque Silencioso", 2, List.of(Itens.RICINA),
                    null, "tratar o perigo como um processo", "Máscara no rosto"),
            new Final("O Acordo", 3, List.of(Itens.DOSSIE_DO_CARTEL),
                    "caixa num supermercado", "operador de empilhadeira", "fiscal de segurança"));

    private Path pasta;
    private SessaoRoteirizada sessao;
    private int partidas;   // cada partida usa saves novos: o aviso de conquista só aparece na primeira vez

    @BeforeEach
    public void setUp() throws IOException {
        pasta = Arquivos.criarPastaTemporaria();
        sessao = new SessaoRoteirizada(pasta);
    }

    @AfterEach
    public void tearDown() throws IOException {
        sessao.restaurar();
        Arquivos.apagarRecursivamente(pasta);
    }

    // Joga só a Temporada 5 com um personagem da origem dada, com os itens do final, e devolve o que foi impresso
    private String jogarTemporada5(Final f, String antecedente) {
        Path saves = pasta.resolve("saves" + partidas++);
        GerenciadorDeSave save = new GerenciadorDeSave(saves, null);
        GerenciadorDeConquistas conquistas = new GerenciadorDeConquistas(saves.resolve(GerenciadorDeConquistas.NOME_ARQUIVO), null);
        PersonagemService service = new PersonagemService(save, conquistas);
        Personagem valter = new Personagem();
        valter.setAntecedente(antecedente);
        for (String item : f.itens()) valter.adicionarItem(item);
        service.setPersonagem(valter);

        java.io.InputStream entradaOriginal = System.in;
        System.setIn(new ByteArrayInputStream("\n\n\n\n".getBytes(StandardCharsets.UTF_8)));
        try {
            GerenciadorDeAudio audio = GerenciadorDeAudio.mudo();
            TemporadaCinco t5 = new TemporadaCinco(new DialogosConsole(audio),
                    new Narrativa(audio, new SaidaConsole()), service);
            return SessaoRoteirizada.capturarSaida(() -> t5.executarTemporada(valter, f.rota()));
        } finally {
            System.setIn(entradaOriginal);
        }
    }

    @Test
    public void testCadaFinalTemOTextoDaOrigemEnaoOsDasOutras() {
        for (Final f : FINAIS) {
            String professor = jogarTemporada5(f, Antecedentes.PROFESSOR_TRADICIONAL);
            String industrial = jogarTemporada5(f, Antecedentes.EX_QUIMICO_INDUSTRIAL);
            String precavido = jogarTemporada5(f, Antecedentes.QUIMICO_PRECAVIDO);

            assertTrue(professor.contains(f.titulo()), "não chegou ao final " + f.titulo());
            if (f.professor() != null) {
                assertTrue(professor.contains(f.professor()), f.titulo() + ": faltou o texto do professor");
                assertFalse(industrial.contains(f.professor()) && precavido.contains(f.professor()),
                        f.titulo() + ": o trecho do professor não deveria aparecer nas outras origens");
            }
            assertTrue(industrial.contains(f.industrial()), f.titulo() + ": faltou o texto do ex-químico industrial");
            assertTrue(precavido.contains(f.precavido()), f.titulo() + ": faltou o texto do químico precavido");

            assertFalse(professor.contains(f.industrial()) || professor.contains(f.precavido()), f.titulo() + ": professor com texto de outra origem");
            assertFalse(industrial.contains(f.precavido()), f.titulo() + ": industrial com o texto do precavido");
            assertFalse(precavido.contains(f.industrial()), f.titulo() + ": precavido com o texto do industrial");
            assertNotEquals(professor, industrial, f.titulo() + ": professor e industrial têm o mesmo final");
            assertNotEquals(professor, precavido, f.titulo() + ": professor e precavido têm o mesmo final");
            assertNotEquals(industrial, precavido, f.titulo() + ": industrial e precavido têm o mesmo final");
        }
    }

    @Test
    public void testOrigemDesconhecidaOuAusenteCaiNoFinalDoProfessor() {
        for (Final f : FINAIS) {
            String professor = jogarTemporada5(f, Antecedentes.PROFESSOR_TRADICIONAL);
            assertEquals(professor, jogarTemporada5(f, "Origem Inventada"), f.titulo());
            assertEquals(professor, jogarTemporada5(f, null), f.titulo());
        }
    }
}
