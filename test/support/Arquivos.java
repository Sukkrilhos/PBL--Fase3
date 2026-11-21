package support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/** Utilitário dos testes: pastas temporárias para que nenhum teste toque em saves/ de verdade. */
public final class Arquivos {
    private Arquivos() { }

    public static Path criarPastaTemporaria() throws IOException {
        return Files.createTempDirectory("ponto-de-ebulicao-teste");
    }

    public static void apagarRecursivamente(Path pasta) throws IOException {
        if (pasta == null || !Files.exists(pasta)) return;
        try (Stream<Path> caminhos = Files.walk(pasta)) {
            caminhos.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }
}
