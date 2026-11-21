package view;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Os fundos que a janela JavaFX carrega: existem, abrem e têm o tamanho para o qual o layout foi pensado. */
public class ArtesDoJogoTest {

    // Todos os cenários do jogo têm arte própria (o genérico fica só como rede de segurança para cenário novo)
    private static final List<String> COM_ARTE_PROPRIA = List.of(
            "menu", "titulo", "generico", "status", "fim", "introducao", "consultorio",
            "jantar", "rua", "laboratorio", "desfecho",
            "furgao", "pia", "esconderijo", "atacadista", "fundo_falso", "encruzilhada",
            "fuga", "praia", "ataque", "prisao", "delacao", "ricina");

    @Test
    public void testCadaFundoExisteAbreEETem1280x720() throws Exception {
        Path fundos = AssetsDeTeste.pastaDeFundos();

        for (String cenario : COM_ARTE_PROPRIA) {
            Path arquivo = fundos.resolve("fundo_" + cenario + ".jpg");
            assertTrue(Files.isRegularFile(arquivo), "falta " + arquivo.getFileName());

            BufferedImage imagem = ImageIO.read(arquivo.toFile());
            assertNotNull(imagem, "não abriu como imagem: " + arquivo.getFileName());
            assertEquals(1280, imagem.getWidth(), "largura de " + arquivo.getFileName());
            assertEquals(720, imagem.getHeight(), "altura de " + arquivo.getFileName());
        }
    }

    @Test
    public void testOsFundosNaoSaoPesadosDemais() throws Exception {
        Path fundos = AssetsDeTeste.pastaDeFundos();
        for (String cenario : COM_ARTE_PROPRIA) {
            long bytes = Files.size(fundos.resolve("fundo_" + cenario + ".jpg"));
            assertTrue(bytes < 600_000, "fundo_" + cenario + ".jpg tem " + bytes + " bytes");
        }
    }

    @Test
    public void testTemaCssExiste() {
        Path tema = AssetsDeTeste.pastaDeFundos().getParent().resolve("tema.css");
        assertTrue(Files.isRegularFile(tema), "falta assets/tema.css");
    }

    @Test
    public void testCadaRetratoExisteAbreEETem280x350() throws Exception {
        Path pasta = AssetsDeTeste.pastaDePersonagens();

        for (String id : view.fx.Personagens.TODOS) {
            Path arquivo = pasta.resolve(id + ".png");
            assertTrue(Files.isRegularFile(arquivo), "falta o retrato " + arquivo.getFileName());

            BufferedImage imagem = ImageIO.read(arquivo.toFile());
            assertNotNull(imagem, "não abriu como imagem: " + arquivo.getFileName());
            assertEquals(280, imagem.getWidth(), "largura de " + arquivo.getFileName());
            assertEquals(350, imagem.getHeight(), "altura de " + arquivo.getFileName());
            assertTrue(Files.size(arquivo) < 150_000, arquivo.getFileName() + " está pesado demais");
        }
    }

    @Test
    public void testOsTresRostosDoProtagonistaSaoImagensDiferentes() throws Exception {
        Path pasta = AssetsDeTeste.pastaDePersonagens();
        byte[] professor = Files.readAllBytes(pasta.resolve("valter_professor.png"));
        byte[] industrial = Files.readAllBytes(pasta.resolve("valter_industrial.png"));
        byte[] precavido = Files.readAllBytes(pasta.resolve("valter_precavido.png"));

        assertFalse(java.util.Arrays.equals(professor, industrial), "professor e ex-químico têm a mesma imagem");
        assertFalse(java.util.Arrays.equals(professor, precavido), "professor e precavido têm a mesma imagem");
        assertFalse(java.util.Arrays.equals(industrial, precavido), "ex-químico e precavido têm a mesma imagem");
    }

    @Test
    public void testNenhumRetratoSobrandoSemUsoNoCadastro() throws Exception {
        try (java.util.stream.Stream<Path> arquivos = Files.list(AssetsDeTeste.pastaDePersonagens())) {
            java.util.List<String> noDisco = arquivos.map(a -> a.getFileName().toString().replace(".png", "")).sorted().toList();
            assertEquals(java.util.List.copyOf(new java.util.TreeSet<>(view.fx.Personagens.TODOS)), noDisco,
                    "os arquivos de assets/personagens devem ser exatamente os cadastrados em Personagens.TODOS");
        }
    }
}
