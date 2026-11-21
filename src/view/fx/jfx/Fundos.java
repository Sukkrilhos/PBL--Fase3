package view.fx.jfx;

import javafx.scene.image.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Carrega as imagens de fundo de assets/fundos (fundo_&lt;cenario&gt;.jpg). Cenário sem imagem usa o genérico,
 * e se nem esse existir a janela fica só com a cor escura do tema: o jogo nunca quebra por falta de arte.
 */
final class Fundos {
    private static final Path PASTA = Paths.get("assets", "fundos");
    private static final String GENERICO = "generico";

    private final Map<String, Image> cache = new HashMap<>();

    /** Devolve a imagem do cenário, ou null se não houver nenhuma disponível. */
    Image obter(String cenario) {
        String chave = (cenario == null || cenario.isBlank()) ? GENERICO : cenario;
        Image imagem = carregar(chave);
        return imagem != null ? imagem : carregar(GENERICO);
    }

    private Image carregar(String chave) {
        if (cache.containsKey(chave)) return cache.get(chave);

        Image imagem = null;
        Path arquivo = PASTA.resolve("fundo_" + chave + ".jpg");
        if (Files.isRegularFile(arquivo)) {
            Image candidata = new Image(arquivo.toUri().toString());
            if (!candidata.isError()) imagem = candidata;
        }
        cache.put(chave, imagem);
        return imagem;
    }
}
