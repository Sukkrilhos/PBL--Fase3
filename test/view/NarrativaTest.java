package view;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Percorre TODAS as cenas da Narrativa e confere o que as interfaces (console e JavaFX) assumem:
 * toda cena diz em qual cenário acontece e as opções são numeradas em sequência, a partir de 1.
 */
public class NarrativaTest {

    private static List<String> montarTodasAsCenas(List<Cena> destino) throws Exception {
        return montarTodasAsCenas(destino, model.Antecedentes.PROFESSOR_TRADICIONAL);
    }

    // Em toda cena que depende da origem o primeiro texto é o nome e o último é a origem do protagonista
    private static void invocar(Narrativa narrativa, Method metodo, int variante, String antecedente) throws Exception {
        Object[] args = new Object[metodo.getParameterCount()];
        Class<?>[] tipos = metodo.getParameterTypes();
        long textos = java.util.Arrays.stream(tipos).filter(t -> t == String.class).count();
        int vistos = 0;
        for (int i = 0; i < tipos.length; i++) {
            if (tipos[i] == String.class) {
                vistos++;
                args[i] = (textos >= 2 && vistos == textos) ? antecedente : "Valter";
            } else {
                args[i] = tipos[i] == int.class ? variante : tipos[i] == boolean.class ? (variante % 2 == 1) : null;
            }
        }
        metodo.invoke(narrativa, args);
    }

    private static List<String> montarTodasAsCenas(List<Cena> destino, String antecedente) throws Exception {
        Narrativa narrativa = new Narrativa(GerenciadorDeAudio.mudo(), destino::add);
        List<String> metodos = new ArrayList<>();
        for (Method metodo : Narrativa.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(metodo.getModifiers())) continue;
            for (int variante = 1; variante <= 3; variante++) {   // varia os parâmetros para passar por todos os ramos
                invocar(narrativa, metodo, variante, antecedente);
            }
            metodos.add(metodo.getName());
        }
        return metodos;
    }

    // Todo o texto (falas, narração e opções) que um método produz nas três variantes, como uma string só
    private static String textoDe(Method metodo, String antecedente) throws Exception {
        List<Cena> cenas = new ArrayList<>();
        Narrativa narrativa = new Narrativa(GerenciadorDeAudio.mudo(), cenas::add);
        for (int variante = 1; variante <= 3; variante++) invocar(narrativa, metodo, variante, antecedente);
        StringBuilder texto = new StringBuilder();
        for (Cena cena : cenas) for (Cena.Item item : cena.getItens()) texto.append(item.texto()).append('\n');
        return texto.toString();
    }

    @Test
    public void testAsFalasMudamConformeAOrigemDoProtagonista() throws Exception {
        int cenasQueDependemDaOrigem = 0;
        for (Method metodo : Narrativa.class.getDeclaredMethods()) {
            long textos = java.util.Arrays.stream(metodo.getParameterTypes()).filter(t -> t == String.class).count();
            if (!Modifier.isPublic(metodo.getModifiers()) || textos < 2) continue;   // sem origem no parâmetro

            String professor = textoDe(metodo, model.Antecedentes.PROFESSOR_TRADICIONAL);
            String industrial = textoDe(metodo, model.Antecedentes.EX_QUIMICO_INDUSTRIAL);
            String precavido = textoDe(metodo, model.Antecedentes.QUIMICO_PRECAVIDO);
            assertNotEquals(professor, industrial, metodo.getName() + ": professor e ex-químico industrial falam igual");
            assertNotEquals(professor, precavido, metodo.getName() + ": professor e químico precavido falam igual");
            assertNotEquals(industrial, precavido, metodo.getName() + ": ex-químico industrial e químico precavido falam igual");
            cenasQueDependemDaOrigem++;
        }
        assertTrue(cenasQueDependemDaOrigem >= 18, "esperava ao menos 18 cenas por origem, vieram " + cenasQueDependemDaOrigem);
    }

    @Test
    public void testOsPersonagensFalamComTodasAsOrigens() throws Exception {
        view.fx.StatusDoPersonagem status = new view.fx.StatusDoPersonagem("Valter", model.Antecedentes.PROFESSOR_TRADICIONAL, 0, 100, 100, 100, List.of());
        for (String origem : List.of(model.Antecedentes.PROFESSOR_TRADICIONAL, model.Antecedentes.EX_QUIMICO_INDUSTRIAL, model.Antecedentes.QUIMICO_PRECAVIDO)) {
            List<Cena> cenas = new ArrayList<>();
            montarTodasAsCenas(cenas, origem);
            java.util.Set<String> oradores = new java.util.TreeSet<>();
            for (Cena cena : cenas) {
                for (view.fx.Trecho trecho : view.fx.FormatadorDeCena.formatar(cena)) {
                    if (trecho.estilo() != view.fx.Estilo.FALA) continue;
                    oradores.add(trecho.orador());
                    assertNotEquals(view.fx.Personagens.DESCONHECIDO, view.fx.Personagens.retratoDoOrador(trecho.orador(), status), origem + ": " + trecho.orador());
                }
            }
            assertTrue(oradores.containsAll(List.of("MARISA", "MALUCO", "VALTER", "KADU", "HEITOR", "ENZO", "FIDALGO")), origem + ": " + oradores);
        }
    }

    @Test
    public void testOpcoesDoNovoEsconderijoRespondemAoPedidoDeDinheiroAdiantado() {
        for (String origem : List.of(model.Antecedentes.PROFESSOR_TRADICIONAL, model.Antecedentes.EX_QUIMICO_INDUSTRIAL, model.Antecedentes.QUIMICO_PRECAVIDO)) {
            List<Cena> cenas = new ArrayList<>();
            new Narrativa(GerenciadorDeAudio.mudo(), cenas::add).mostrarCenaNovoEsconderijoParte1("Valter", origem);
            Cena cena = cenas.get(0);

            String fala = cena.getItens().stream().map(Cena.Item::texto).filter(t -> t.startsWith("KADU:")).findFirst().orElseThrow();
            assertTrue(fala.contains("dinheiro adiantado"), origem + ": Kadu precisa pedir o dinheiro adiantado: " + fala);

            List<Cena.Item> opcoes = cena.getOpcoes();
            assertEquals(3, opcoes.size());
            assertTrue(opcoes.get(0).texto().contains("aluguel") && opcoes.get(0).texto().contains("radioterapia"), opcoes.get(0).texto());
            assertTrue(opcoes.get(1).texto().contains("aluguel") && opcoes.get(1).texto().contains("escola"), opcoes.get(1).texto());
            assertTrue(opcoes.get(2).texto().contains("Ricina") && opcoes.get(2).texto().contains("dono"), opcoes.get(2).texto());
        }
    }

    @Test
    public void testCompraSemDinheiroApareceDesativadaEComOPreco() {
        List<Cena> cenas = new ArrayList<>();
        Narrativa narrativa = new Narrativa(GerenciadorDeAudio.mudo(), cenas::add);

        narrativa.mostrarCenaFornada("Valter", false);
        narrativa.mostrarCenaOAtacadistaParte1("Valter", model.Antecedentes.PROFESSOR_TRADICIONAL, false);
        narrativa.mostrarCenaFundoFalsoParte1("Valter", model.Antecedentes.PROFESSOR_TRADICIONAL, false);
        int[] precos = {model.Itens.PRECO_MASCARA_DE_GAS, model.Itens.PRECO_PASSAPORTES_FALSOS, model.Itens.PRECO_COLETE_BALISTICO};

        for (int i = 0; i < 3; i++) {
            List<Cena.Item> opcoes = cenas.get(i).getOpcoes();
            Cena.Item compra = opcoes.get(2);
            assertFalse(compra.disponivel(), "a compra sem dinheiro deveria estar indisponível");
            assertTrue(compra.texto().contains("R$ " + precos[i]), compra.texto());
            assertTrue(opcoes.get(0).disponivel() && opcoes.get(1).disponivel(), "as outras opções continuam livres");
        }
    }

    @Test
    public void testCompraComDinheiroFicaDisponivel() {
        List<Cena> cenas = new ArrayList<>();
        Narrativa narrativa = new Narrativa(GerenciadorDeAudio.mudo(), cenas::add);

        narrativa.mostrarCenaFornada("Valter", true);
        narrativa.mostrarCenaOAtacadistaParte1("Valter", model.Antecedentes.PROFESSOR_TRADICIONAL, true);
        narrativa.mostrarCenaFundoFalsoParte1("Valter", model.Antecedentes.PROFESSOR_TRADICIONAL, true);

        for (Cena cena : cenas) {
            assertTrue(cena.getOpcoes().stream().allMatch(Cena.Item::disponivel));
        }
    }

    @Test
    public void testExistemCenasEElasSaoMontadasSemErro() throws Exception {
        List<Cena> cenas = new ArrayList<>();

        List<String> metodos = montarTodasAsCenas(cenas);

        assertTrue(metodos.size() >= 37, "esperava ao menos 37 métodos de cena, vieram " + metodos.size());
        assertFalse(cenas.isEmpty());
    }

    @Test
    public void testTodaCenaTemCenarioEAsOpcoesSaoSequenciaisAPartirDeUm() throws Exception {
        List<Cena> cenas = new ArrayList<>();
        montarTodasAsCenas(cenas);

        for (Cena cena : cenas) {
            assertNotNull(cena.getCenario(), "cena sem cenário");
            assertFalse(cena.getCenario().isBlank(), "cena com cenário vazio");

            List<Cena.Item> opcoes = cena.getOpcoes();
            for (int i = 0; i < opcoes.size(); i++) {
                assertEquals(i + 1, opcoes.get(i).numero(), "opções fora de sequência no cenário " + cena.getCenario());
            }
            if (!opcoes.isEmpty()) {
                boolean temPrompt = cena.getItens().stream().anyMatch(item -> item.tipo() == Cena.Tipo.PROMPT);
                assertTrue(temPrompt, "cena com opções mas sem prompt de escolha, no cenário " + cena.getCenario());
            }
        }
    }

    @Test
    public void testTodoCenarioUsadoTemImagemOuCaiNoGenerico() throws Exception {
        List<Cena> cenas = new ArrayList<>();
        montarTodasAsCenas(cenas);
        java.nio.file.Path fundos = AssetsDeTeste.pastaDeFundos();

        assertTrue(java.nio.file.Files.isRegularFile(fundos.resolve("fundo_generico.jpg")), "falta o fundo genérico");
        for (Cena cena : cenas) {
            assertTrue(cena.getCenario().matches("[a-z_]+"), "nome de cenário inesperado: " + cena.getCenario());
        }
    }

    @Test
    public void testTodoCenarioDaHistoriaTemArtePropria() throws Exception {
        List<Cena> cenas = new ArrayList<>();
        montarTodasAsCenas(cenas);
        java.nio.file.Path fundos = AssetsDeTeste.pastaDeFundos();

        for (Cena cena : cenas) {
            assertTrue(java.nio.file.Files.isRegularFile(fundos.resolve("fundo_" + cena.getCenario() + ".jpg")),
                    "o cenário \"" + cena.getCenario() + "\" ainda usa o fundo genérico: rode tools/gerar_fundos.py com ele");
        }
    }

    @Test
    public void testTodoOradorDaHistoriaTemRostoDesenhado() throws Exception {
        List<Cena> cenas = new ArrayList<>();
        montarTodasAsCenas(cenas);
        view.fx.StatusDoPersonagem status = new view.fx.StatusDoPersonagem("Valter", model.Antecedentes.PROFESSOR_TRADICIONAL, 0, 100, 100, 100, List.of());

        java.util.Set<String> oradores = new java.util.TreeSet<>();
        for (Cena cena : cenas) {
            for (view.fx.Trecho trecho : view.fx.FormatadorDeCena.formatar(cena)) {
                if (trecho.estilo() != view.fx.Estilo.FALA) continue;
                oradores.add(trecho.orador());
                assertNotEquals(view.fx.Personagens.DESCONHECIDO, view.fx.Personagens.retratoDoOrador(trecho.orador(), status),
                        "o orador \"" + trecho.orador() + "\" fala na história mas não tem rosto: desenhe um e cadastre em Personagens");
            }
        }
        assertTrue(oradores.containsAll(java.util.List.of("MARISA", "MALUCO", "VALTER", "KADU", "HEITOR", "ENZO", "FIDALGO")), "oradores encontrados: " + oradores);
    }
}
