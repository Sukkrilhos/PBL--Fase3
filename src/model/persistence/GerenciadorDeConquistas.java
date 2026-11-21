package model.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Guarda as conquistas já desbloqueadas (compartilhadas por todos os saves).
 * O Model não escreve no console: falhas de leitura/gravação são entregues ao ouvinte de erros,
 * que o Main liga à View (por padrão, só vão para o log).
 */
public class GerenciadorDeConquistas {
    public static final String NOME_ARQUIVO = "conquistas.json";

    private static final Logger LOG = Logger.getLogger(GerenciadorDeConquistas.class.getName());
    private static final Type TIPO_CONQUISTAS = new TypeToken<HashSet<String>>() { }.getType();

    private final Path arquivo;
    private final Consumer<String> ouvinteDeErros;
    private final Gson gson = new Gson();
    private Set<String> conquistas = new HashSet<>();

    public GerenciadorDeConquistas() {
        this(null);
    }

    public GerenciadorDeConquistas(Consumer<String> ouvinteDeErros) {
        this(Paths.get(GerenciadorDeSave.DIRETORIO_PADRAO, NOME_ARQUIVO), ouvinteDeErros);
    }

    public GerenciadorDeConquistas(Path arquivo, Consumer<String> ouvinteDeErros) {
        this.arquivo = arquivo;
        this.ouvinteDeErros = ouvinteDeErros != null ? ouvinteDeErros : LOG::warning;
        carregarConquistas();
    }

    private void carregarConquistas() {
        if (!Files.exists(arquivo)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            Set<String> carregado = gson.fromJson(reader, TIPO_CONQUISTAS);
            if (carregado != null) {
                this.conquistas = carregado;
            }
        } catch (IOException | JsonParseException e) {
            ouvinteDeErros.accept("Erro ao carregar conquistas.");
        }
    }

    public void registrarConquista(String id) {
        if (conquistas.add(id)) {
            salvarConquistas();
        }
    }

    private void salvarConquistas() {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            try (Writer writer = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
                gson.toJson(conquistas, writer);
            }
        } catch (IOException e) {
            ouvinteDeErros.accept("Erro ao salvar conquistas.");
        }
    }

    public boolean possuiConquista(String id) {
        return conquistas.contains(id);
    }
}
