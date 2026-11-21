package support;

import view.fx.AcoesDePausa;
import view.fx.Estilo;
import view.fx.Pergunta;
import view.fx.PerguntaDeTexto;
import view.fx.StatusDoPersonagem;
import view.fx.TelaFx;
import view.fx.Trecho;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** Tela de mentira para testes: registra tudo o que recebe e responde às perguntas como um jogador roteirizado. */
public class TelaFalsa implements TelaFx {
    public record Pagina(String cenario, List<Trecho> trechos, boolean limpar) { }

    public final List<Pergunta> perguntas = new ArrayList<>();
    public final List<PerguntaDeTexto> perguntasDeTexto = new ArrayList<>();
    public final List<Pagina> paginas = new ArrayList<>();
    public final List<String> avisos = new ArrayList<>();
    public final List<StatusDoPersonagem> statuses = new ArrayList<>();

    public AcoesDePausa acoesDePausa;   // o menu de pausa que o DialogosFx registrou
    public boolean naoResponder;        // deixa a pergunta pendente, como um jogador que ainda não clicou

    public Function<Pergunta, Integer> politica = pergunta -> pergunta.opcoes().get(0).valor();
    public Function<PerguntaDeTexto, String> politicaDeTexto = pergunta -> pergunta.padrao();

    @Override
    public void definirAcoesDePausa(AcoesDePausa acoes) {
        this.acoesDePausa = acoes;
    }

    @Override
    public void atualizarStatus(StatusDoPersonagem status) {
        statuses.add(status);
    }

    @Override
    public void exibirPagina(String cenario, List<Trecho> trechos, boolean limpar) {
        paginas.add(new Pagina(cenario, trechos, limpar));
    }

    @Override
    public void perguntar(Pergunta pergunta, CompletableFuture<Integer> resposta) {
        perguntas.add(pergunta);
        if (naoResponder) return;
        resposta.complete(politica.apply(pergunta));
    }

    @Override
    public void perguntarTexto(PerguntaDeTexto pergunta, CompletableFuture<String> resposta) {
        perguntasDeTexto.add(pergunta);
        resposta.complete(politicaDeTexto.apply(pergunta));
    }

    @Override
    public void mostrarAviso(String texto) {
        avisos.add(texto);
    }

    /** Todo o texto exibido nas páginas, em ordem, uma linha por trecho. */
    public String textoExibido() {
        StringBuilder sb = new StringBuilder();
        for (Pagina pagina : paginas) {
            for (Trecho trecho : pagina.trechos()) sb.append(trecho.texto()).append('\n');
        }
        return sb.toString();
    }

    public List<Trecho> trechosDeEstilo(Estilo estilo) {
        List<Trecho> achados = new ArrayList<>();
        for (Pagina pagina : paginas) {
            for (Trecho trecho : pagina.trechos()) if (trecho.estilo() == estilo) achados.add(trecho);
        }
        return achados;
    }
}
