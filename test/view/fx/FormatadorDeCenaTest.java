package view.fx;

import org.junit.jupiter.api.Test;
import view.Cena;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FormatadorDeCenaTest {

    private static List<Trecho> formatar(String... linhas) {
        Cena cena = new Cena("teste");
        for (String linha : linhas) cena.linha(linha);
        return FormatadorDeCena.formatar(cena);
    }

    @Test
    public void testMolduraELinhasVaziasSomem() {
        assertTrue(formatar("========================================================", "", "   ").isEmpty());
    }

    @Test
    public void testTituloDeCapituloComIguais() {
        Trecho t = formatar("\n=== CAPÍTULO 1: A Escolha ===").get(0);
        assertEquals(Estilo.TITULO_CAPITULO, t.estilo());
        assertEquals("CAPÍTULO 1: A Escolha", t.texto());
    }

    @Test
    public void testTituloDeCapituloSemIguais() {
        assertEquals(Estilo.TITULO_CAPITULO, formatar("CAPÍTULO FINAL: O Homem Honesto (ROTA A)").get(0).estilo());
    }

    @Test
    public void testTituloDeTemporadaIgnoraOsEspacosDeAlinhamento() {
        Trecho t = formatar("          TEMPORADA 1: A QUÍMICA DA NECESSIDADE").get(0);
        assertEquals(Estilo.TITULO_TEMPORADA, t.estilo());
        assertEquals("TEMPORADA 1: A QUÍMICA DA NECESSIDADE", t.texto());
    }

    @Test
    public void testFalaSeparaOOradorDoTexto() {
        Trecho t = formatar("MARISA: — Nós vamos dar um jeito. Nós sempre damos.").get(0);
        assertEquals(Estilo.FALA, t.estilo());
        assertEquals("MARISA", t.orador());
        assertEquals("— Nós vamos dar um jeito. Nós sempre damos.", t.texto());
    }

    @Test
    public void testEfeitosFicamSemColchetes() {
        Trecho t = formatar("\n[Efeito: Moral -10 | Relação Familiar -10]").get(0);
        assertEquals(Estilo.EFEITO, t.estilo());
        assertEquals("Efeito: Moral -10 | Relação Familiar -10", t.texto());
    }

    @Test
    public void testFinsDeRotaEAvisosDeRotaSaoDestacados() {
        assertEquals(Estilo.FIM, formatar("\n[FIM DA ROTA D (RENDIÇÃO): Você sacrificou o império.]").get(0).estilo());
        assertEquals(Estilo.FIM, formatar("\nROTA B/C INICIADA: Você aceitou a proposta de Kadu.").get(0).estilo());
        assertEquals(Estilo.FIM, formatar("GAME OVER: acabou.").get(0).estilo());
    }

    @Test
    public void testPerguntaCurtaEDestacadaENarracaoComum() {
        assertEquals(Estilo.PERGUNTA, formatar("O que você responde?").get(0).estilo());
        assertEquals(Estilo.NARRACAO, formatar("A fumaça do laboratório está destruindo seus pulmões.").get(0).estilo());
    }

    @Test
    public void testOpcoesEPromptNaoViramTexto() {
        Cena cena = new Cena("teste").linha("Uma frase.").opcao(1, "Recusar").prompt("Sua escolha: ");
        List<Trecho> trechos = FormatadorDeCena.formatar(cena);
        assertEquals(1, trechos.size());
        assertEquals("Uma frase.", trechos.get(0).texto());
    }

    @Test
    public void testLinhaQuebradaNoMeioDaFraseViraUmSoParagrafo() {
        List<Trecho> trechos = formatar(
                "Você encontrou-se com Heitor. Na mesma noite, a",
                "Polícia Federal desmantelou o império.",
                "Você detonou o galpão, aniquilando os inimigos de uma",
                "só vez.");
        assertEquals(2, trechos.size());
        assertEquals("Você encontrou-se com Heitor. Na mesma noite, a Polícia Federal desmantelou o império.", trechos.get(0).texto());
        assertEquals("Você detonou o galpão, aniquilando os inimigos de uma só vez.", trechos.get(1).texto());
    }

    @Test
    public void testFrasesCompletasContinuamEmParagrafosSeparados() {
        assertEquals(2, formatar("Primeira frase completa.", "Segunda frase completa.").size());
    }
}
