package model.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Arquivos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciadorDeConquistasTest {

    private Path pasta;
    private Path arquivo;
    private List<String> erros;

    @BeforeEach
    public void setUp() throws IOException {
        pasta = Arquivos.criarPastaTemporaria();
        arquivo = pasta.resolve("saves").resolve(GerenciadorDeConquistas.NOME_ARQUIVO);
        erros = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() throws IOException {
        Arquivos.apagarRecursivamente(pasta);
    }

    @Test
    public void testComecaSemConquistasQuandoOArquivoNaoExiste() {
        GerenciadorDeConquistas conquistas = new GerenciadorDeConquistas(arquivo, erros::add);

        assertFalse(conquistas.possuiConquista("ROTA_A"));
        assertTrue(erros.isEmpty());
    }

    @Test
    public void testConquistaRegistradaSobreviveAUmaNovaInstancia() {
        new GerenciadorDeConquistas(arquivo, erros::add).registrarConquista("ROTA_A");

        GerenciadorDeConquistas recarregado = new GerenciadorDeConquistas(arquivo, erros::add);

        assertTrue(recarregado.possuiConquista("ROTA_A"));
        assertFalse(recarregado.possuiConquista("ROTA_D_DELACAO"));
        assertTrue(erros.isEmpty());
    }

    @Test
    public void testRegistrarDuasVezesNaoDuplicaNemFalha() {
        GerenciadorDeConquistas conquistas = new GerenciadorDeConquistas(arquivo, erros::add);
        conquistas.registrarConquista("ROTA_A");
        conquistas.registrarConquista("ROTA_A");

        assertTrue(new GerenciadorDeConquistas(arquivo, erros::add).possuiConquista("ROTA_A"));
    }

    @Test
    public void testArquivoCorrompidoNaoDerrubaOJogoENotificaOErro() throws IOException {
        Files.createDirectories(arquivo.getParent());
        Files.write(arquivo, "isto {{ não é json".getBytes(StandardCharsets.UTF_8));

        GerenciadorDeConquistas conquistas = new GerenciadorDeConquistas(arquivo, erros::add);

        assertFalse(conquistas.possuiConquista("ROTA_A"));
        assertEquals(1, erros.size());
        assertEquals("Erro ao carregar conquistas.", erros.get(0));
    }
}
