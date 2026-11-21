package view;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Localiza a pasta assets/fundos subindo a partir da pasta de trabalho (a IDE roda os testes na raiz do projeto). */
final class AssetsDeTeste {
    private AssetsDeTeste() { }

    static Path pastaDePersonagens() {
        return pastaDeFundos().getParent().resolve("personagens");
    }

    static Path pastaDeFundos() {
        Path atual = Paths.get("").toAbsolutePath();
        while (atual != null) {
            Path candidata = atual.resolve("assets").resolve("fundos");
            if (Files.isDirectory(candidata)) return candidata;
            atual = atual.getParent();
        }
        throw new AssertionError("não achei assets/fundos a partir de " + Paths.get("").toAbsolutePath());
    }
}
