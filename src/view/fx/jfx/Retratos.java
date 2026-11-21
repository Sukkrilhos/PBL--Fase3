package view.fx.jfx;

import javafx.scene.image.Image;
import view.fx.Personagens;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Carrega os rostos de assets/personagens/&lt;id&gt;.png. Id sem imagem usa o retrato "desconhecido", e se nem
 * esse existir devolve null: a janela mostra a fala sem rosto, mas nunca quebra por falta de arte.
 */
final class Retratos {
    private static final Path PASTA = Paths.get("assets", "personagens");

    private final Map<String, Image> cache = new HashMap<>();

    /** Devolve o rosto pedido, o "desconhecido" na falta dele, ou null se não houver nenhuma imagem. */
    Image obter(String id) {
        Image imagem = id == null ? null : carregar(id);
        return imagem != null ? imagem : carregar(Personagens.DESCONHECIDO);
    }

    private Image carregar(String id) {
        if (cache.containsKey(id)) return cache.get(id);

        Image imagem = null;
        Path arquivo = PASTA.resolve(id + ".png");
        if (Files.isRegularFile(arquivo)) {
            Image candidata = new Image(arquivo.toUri().toString());
            if (!candidata.isError()) imagem = candidata;
        }
        cache.put(id, imagem);
        return imagem;
    }
}
