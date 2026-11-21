package view;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class GerenciadorDeAudio {
    private boolean indisponivel;

    public GerenciadorDeAudio() {
        this(false);
    }

    private GerenciadorDeAudio(boolean mudo) {
        this.indisponivel = mudo;
    }

    // Instância que nunca toca nada (usada nos testes, para não tocar som de verdade)
    public static GerenciadorDeAudio mudo() {
        return new GerenciadorDeAudio(true);
    }

    // Toca um som curto (efeitos sonoros) em segundo plano
    public void tocarSom(String caminhoArquivo) {
        if (indisponivel) return;

        File arquivoAudio = new File(caminhoArquivo);
        if (!arquivoAudio.exists()) return; // Sem o arquivo, o jogo segue em silêncio

        try (AudioInputStream audioInput = AudioSystem.getAudioInputStream(arquivoAudio)) {
            Clip clip = AudioSystem.getClip();
            // Libera o recurso de áudio quando o som termina (antes cada clipe ficava aberto para sempre)
            clip.addLineListener(evento -> {
                if (evento.getType() == LineEvent.Type.STOP) {
                    evento.getLine().close();
                }
            });
            clip.open(audioInput);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException e) {
            System.out.println("Erro ao reproduzir áudio: " + e.getMessage());
        } catch (LineUnavailableException | IllegalArgumentException e) {
            // Máquina sem dispositivo de som: avisa uma única vez e desliga o áudio em vez de travar o jogo
            indisponivel = true;
            System.out.println("Áudio indisponível neste dispositivo; o jogo continuará sem som. (" + e.getMessage() + ")");
        }
    }
}
