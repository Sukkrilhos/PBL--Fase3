package model.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import model.Personagem;
import model.Progresso;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Lê e grava os saves (um arquivo JSON por slot) dentro de uma pasta.
 * O Model não escreve no console: falhas de I/O são entregues ao ouvinte de erros,
 * que o Main liga à View (por padrão, só vão para o log).
 */
public class GerenciadorDeSave {
    public static final String DIRETORIO_PADRAO = "saves";

    private static final Logger LOG = Logger.getLogger(GerenciadorDeSave.class.getName());
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Path diretorio;
    private final Consumer<String> ouvinteDeErros;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private volatile String nomeSaveAtual; // Guarda o nome do save que está a ser jogado

    public GerenciadorDeSave() {
        this(null);
    }

    public GerenciadorDeSave(Consumer<String> ouvinteDeErros) {
        this(Paths.get(DIRETORIO_PADRAO), ouvinteDeErros);
    }

    public GerenciadorDeSave(Path diretorio, Consumer<String> ouvinteDeErros) {
        this.diretorio = diretorio;
        this.ouvinteDeErros = ouvinteDeErros != null ? ouvinteDeErros : LOG::warning;

        // Cria a pasta de saves automaticamente, se não existir
        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            notificarErro("Não foi possível criar a pasta de saves: " + e.getMessage());
        }
    }

    public void setNomeSaveAtual(String nomeSave) {
        this.nomeSaveAtual = nomeSave;
    }

    // Gera o nome do ficheiro limpando caracteres especiais para evitar erros no Windows/Linux
    private Path getCaminhoArquivoAtual() {
        String nomeArquivo = nomeSaveAtual.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        return diretorio.resolve(nomeArquivo + ".json");
    }

    private void notificarErro(String mensagem) {
        ouvinteDeErros.accept(mensagem);
    }

    // Devolve true se gravou. Synchronized: o botão de pausa salva pela thread da janela, e o jogo pela sua
    public synchronized boolean salvarJogo(Progresso progresso) {
        String slot = nomeSaveAtual;
        if (slot == null) return false; // Proteção: sem slot ativo não há onde gravar

        progresso.setNomeSave(slot);
        progresso.setDataHora(FORMATO_DATA_HORA.format(LocalDateTime.now())); // Carimba a hora exata sempre que salva

        try (Writer writer = Files.newBufferedWriter(getCaminhoArquivoAtual(), StandardCharsets.UTF_8)) {
            gson.toJson(progresso, writer);
            return true;
        } catch (IOException e) {
            notificarErro("Erro ao salvar o jogo: " + e.getMessage());
            return false;
        }
    }

    private Progresso carregarJogoAtual() {
        if (nomeSaveAtual == null) return null;
        Path arquivo = getCaminhoArquivoAtual();
        if (!Files.exists(arquivo)) return null;

        try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Progresso.class);
        } catch (IOException | JsonParseException e) {
            return null;
        }
    }

    public synchronized void atualizarPersonagem(Personagem personagem) {
        if (nomeSaveAtual == null) return; // Proteção

        Progresso progresso = carregarJogoAtual();
        if (progresso == null) {
            progresso = new Progresso(personagem, 1);
        } else {
            progresso.setPersonagem(personagem);
        }
        salvarJogo(progresso); // Grava atualizando a data e hora
    }

    public synchronized void apagarSave() {
        if (nomeSaveAtual != null) {
            try {
                Files.deleteIfExists(getCaminhoArquivoAtual());
            } catch (IOException e) {
                notificarErro("Erro ao apagar o save: " + e.getMessage());
            }
            nomeSaveAtual = null; // Limpa a referência
        }
    }

    // Lê todos os ficheiros .json da pasta de saves (exceto as conquistas) e devolve uma lista
    public List<Progresso> listarSaves() {
        List<Progresso> saves = new ArrayList<>();
        File[] arquivos = diretorio.toFile().listFiles((dir, nome) ->
                nome.toLowerCase().endsWith(".json") && !nome.equalsIgnoreCase(GerenciadorDeConquistas.NOME_ARQUIVO));

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                try (Reader reader = Files.newBufferedReader(arquivo.toPath(), StandardCharsets.UTF_8)) {
                    Progresso p = gson.fromJson(reader, Progresso.class);
                    if (p != null) {
                        saves.add(p);
                    }
                } catch (IOException | JsonParseException e) {
                    // Ignora ficheiros corrompidos ou mal formatados sem quebrar o programa
                }
            }
        }
        return saves;
    }
}
