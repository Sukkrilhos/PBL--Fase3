package model;

public class Progresso {
    private Personagem personagem;
    private int temporadaAtual; // Temporada em que o jogo recomeça ao carregar este save
    private String nomeSave; // Nome do slot
    private String dataHora; // Data e hora do último save
    private int rotaFinal;   // Rota escolhida na Temporada 4 (1 = B, 2 = C, 3 = D); 0 enquanto não foi escolhida
    private int capitulo;    // Capítulo da temporada em que o jogo recomeça (0 = do início da temporada)

    public Progresso(Personagem personagem, int temporadaAtual) {
        this.personagem = personagem;
        this.temporadaAtual = temporadaAtual;
    }

    public Personagem getPersonagem() { return personagem; }
    public void setPersonagem(Personagem personagem) { this.personagem = personagem; }

    public int getTemporadaAtual() { return temporadaAtual; }
    public void setTemporadaAtual(int temporadaAtual) { this.temporadaAtual = temporadaAtual; }

    public String getNomeSave() { return nomeSave; }
    public void setNomeSave(String nomeSave) { this.nomeSave = nomeSave; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    // Saves antigos não têm este campo: o Gson o lê como 0 ("rota ainda não escolhida")
    public int getRotaFinal() { return rotaFinal; }
    public void setRotaFinal(int rotaFinal) { this.rotaFinal = rotaFinal; }

    // Também ausente em saves antigos (lido como 0): eles recomeçam do início da temporada, como sempre
    public int getCapitulo() { return capitulo; }
    public void setCapitulo(int capitulo) { this.capitulo = capitulo; }
}
