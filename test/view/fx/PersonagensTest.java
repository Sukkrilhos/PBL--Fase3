package view.fx;

import model.Antecedentes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersonagensTest {

    private static StatusDoPersonagem status(String nome, String antecedente) {
        return new StatusDoPersonagem(nome, antecedente, 0, 100, 100, 100, List.of());
    }

    @Test
    public void testCadaOrigemTemUmRostoDiferente() {
        assertEquals("valter_professor", Personagens.retratoDoProtagonista(Antecedentes.PROFESSOR_TRADICIONAL));
        assertEquals("valter_industrial", Personagens.retratoDoProtagonista(Antecedentes.EX_QUIMICO_INDUSTRIAL));
        assertEquals("valter_precavido", Personagens.retratoDoProtagonista(Antecedentes.QUIMICO_PRECAVIDO));
    }

    @Test
    public void testOrigemDesconhecidaOuAusenteUsaORostoDoProfessor() {
        assertEquals("valter_professor", Personagens.retratoDoProtagonista("Origem Inventada"));
        assertEquals("valter_professor", Personagens.retratoDoProtagonista(null));
    }

    @Test
    public void testPersonagensFixosIgnoramMaiusculasEAcentos() {
        assertEquals("marisa", Personagens.retratoDoOrador("MARISA", null));
        assertEquals("marisa", Personagens.retratoDoOrador("  Marisa ", null));
        assertEquals("maluco", Personagens.retratoDoOrador("MALUCO", null));
        assertEquals("fidalgo", Personagens.retratoDoOrador("O FIDALGO", null));
        assertEquals("fidalgo", Personagens.retratoDoOrador("Fidalgo", null));
        assertEquals("kadu", Personagens.retratoDoOrador("KADU", null));
        assertEquals("heitor", Personagens.retratoDoOrador("HEITOR", null));
        assertEquals("enzo", Personagens.retratoDoOrador("ENZO", null));
    }

    @Test
    public void testProtagonistaUsaORostoDaOrigemEscolhida() {
        assertEquals("valter_industrial", Personagens.retratoDoOrador("VALTER", status("Valter", Antecedentes.EX_QUIMICO_INDUSTRIAL)));
        assertEquals("valter_precavido", Personagens.retratoDoOrador("VALTER", status("Valter", Antecedentes.QUIMICO_PRECAVIDO)));
    }

    @Test
    public void testProtagonistaRenomeadoTambemEReconhecido() {
        StatusDoPersonagem heisenberg = status("Heisenberg", Antecedentes.QUIMICO_PRECAVIDO);

        assertEquals("valter_precavido", Personagens.retratoDoOrador("HEISENBERG", heisenberg));
        assertEquals("valter_precavido", Personagens.retratoDoOrador("VALTER", heisenberg));
    }

    @Test
    public void testNomeComAcentoDoProtagonistaCasaComOOrador() {
        assertEquals("valter_industrial", Personagens.retratoDoOrador("JOSÉ", status("José", Antecedentes.EX_QUIMICO_INDUSTRIAL)));
        assertEquals("valter_industrial", Personagens.retratoDoOrador("JOSE", status("José", Antecedentes.EX_QUIMICO_INDUSTRIAL)));
    }

    @Test
    public void testPersonagemFixoTemPrioridadeSobreONomeEscolhidoPeloJogador() {
        // um jogador que se chama "Marisa" não pode transformar as falas da esposa no rosto do protagonista
        assertEquals("marisa", Personagens.retratoDoOrador("MARISA", status("Marisa", Antecedentes.EX_QUIMICO_INDUSTRIAL)));
    }

    @Test
    public void testOradorSemRostoCaiNoDesconhecido() {
        assertEquals(Personagens.DESCONHECIDO, Personagens.retratoDoOrador("POLICIAL", status("Valter", Antecedentes.PROFESSOR_TRADICIONAL)));
        assertEquals(Personagens.DESCONHECIDO, Personagens.retratoDoOrador(null, null));
    }

    @Test
    public void testListaDeRetratosNaoTemRepetidos() {
        assertEquals(Personagens.TODOS.size(), (int) Personagens.TODOS.stream().distinct().count());
        assertTrue(Personagens.TODOS.contains(Personagens.DESCONHECIDO));
    }
}
