package view;

/** Exibe as cenas no console, exatamente como o jogo sempre imprimiu. */
public class SaidaConsole implements Saida {
    @Override
    public void exibir(Cena cena) {
        for (Cena.Item item : cena.getItens()) {
            switch (item.tipo()) {
                case LINHA:
                    System.out.println(item.texto());
                    break;
                case OPCAO:
                    System.out.println(item.numero() + " - " + item.texto());
                    break;
                case PROMPT:
                    System.out.print(item.texto());
                    break;
            }
        }
    }
}
