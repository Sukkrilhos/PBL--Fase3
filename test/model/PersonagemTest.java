package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PersonagemTest {

    @Test
    public void testValoresIniciaisPadrao() {
        Personagem p = new Personagem();

        assertEquals("Valter", p.getNome());
        assertEquals("Professor Tradicional", p.getAntecedente());
        assertEquals(0, p.getDinheiro());
        assertEquals(100, p.getSaude());
        assertEquals(100, p.getMoralEtica());
        assertEquals(100, p.getRelacaoFamiliar());
        assertTrue(p.getInventario().isEmpty());
    }

    @Test
    public void testSoPodePagarQuandoOSaldoCobreOPreco() {
        Personagem p = new Personagem();
        p.alterarDinheiro(1000);

        assertTrue(p.podePagar(1000), "saldo igual ao preço cobre a compra");
        assertTrue(p.podePagar(0));
        assertFalse(p.podePagar(1001), "faltou R$ 1");
    }

    @Test
    public void testAtributosVitaisNuncaPassamDe100() {
        Personagem p = new Personagem();
        p.alterarSaude(+50);
        p.alterarMoral(+50);
        p.alterarRelacao(+50);

        assertEquals(100, p.getSaude());
        assertEquals(100, p.getMoralEtica());
        assertEquals(100, p.getRelacaoFamiliar());
    }

    @Test
    public void testAtributosVitaisNuncaFicamAbaixoDeZero() {
        Personagem p = new Personagem();
        p.alterarSaude(-500);
        p.alterarMoral(-500);
        p.alterarRelacao(-500);

        assertEquals(0, p.getSaude());
        assertEquals(0, p.getMoralEtica());
        assertEquals(0, p.getRelacaoFamiliar());
    }

    @Test
    public void testDinheiroNaoTemLimiteNemPisoZero() {
        Personagem p = new Personagem();
        p.alterarDinheiro(-500);
        assertEquals(-500, p.getDinheiro());

        p.alterarDinheiro(1_000_000);
        assertEquals(999_500, p.getDinheiro());
    }

    @Test
    public void testInventarioNaoDuplicaItens() {
        Personagem p = new Personagem();
        p.adicionarItem(Itens.RICINA);
        p.adicionarItem(Itens.RICINA);

        assertEquals(1, p.getInventario().size());
        assertTrue(p.possuiItem(Itens.RICINA));
        assertFalse(p.possuiItem(Itens.COLETE_BALISTICO));
    }
}
