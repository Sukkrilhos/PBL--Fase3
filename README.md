# Ponto Zero (PBL Fase 3)

Jogo de aventura em texto, em Java, organizado em **MVC**, com duas interfaces sobre o mesmo jogo:
**console** (`Main`) e **janela gráfica JavaFX** (`Jogar`). Salva o progresso em JSON (Gson) e toca efeitos sonoros.

## Estrutura

```
src/
├── Main.java                      Console: monta as peças (composição) e inicia o Controlador
├── Jogar.java                   Janela JavaFX: ponto de entrada (precisa do JavaFX, veja abaixo)
├── model/                         REGRAS e DADOS — não conhece View nem Controller
│   ├── Personagem.java            Estado do jogador (atributos 0-100, dinheiro, inventário)
│   ├── Progresso.java             Um save: personagem + temporada + capítulo + rota + slot + data
│   ├── Antecedentes.java, Itens.java, Conquistas.java   Constantes (origens, itens, IDs de conquistas)
│   ├── service/
│   │   └── PersonagemService.java Regras de negócio (consequências, game over, itens, conquistas)
│   └── persistence/
│       ├── GerenciadorDeSave.java        Saves em saves/<slot>.json
│       └── GerenciadorDeConquistas.java  Conquistas em saves/conquistas.json
├── view/                          TUDO que o jogador lê ou digita — não decide regras
│   ├── Dialogos.java              Interface: menus, perguntas, pausas e mensagens de fluxo
│   ├── DialogosConsole.java       Implementação em texto (teclado e terminal)
│   ├── Narrativa.java             Conteúdo da história: monta cada trecho como uma Cena (dados)
│   ├── Cena.java, Saida.java      Cena = linhas + opções + cenário; Saida = quem a exibe
│   ├── SaidaConsole.java          Exibe as cenas no terminal
│   ├── Conteudo.java              Textos fixos compartilhados (menu, instruções, créditos, conquistas)
│   ├── GerenciadorDeAudio.java    Efeitos sonoros (sons/*.wav)
│   └── fx/                        Interface gráfica
│       ├── DialogosFx.java        Implementa Dialogos e Saida para a janela (sem importar JavaFX)
│       ├── PonteDeInterface.java  Liga a thread do jogo (que espera respostas) à thread da janela
│       ├── FormatadorDeCena.java  Classifica o texto (título, fala, efeito...) para a janela
│       ├── Personagens.java       Qual rosto mostrar para cada pessoa (o do protagonista depende da origem)
│       ├── TelaFx.java, Pergunta.java, OpcaoFx.java, Trecho.java, ...   Contrato entre DialogosFx e a janela
│       └── jfx/                   ÚNICO pacote que usa JavaFX
│           ├── JanelaFx.java      Application: monta Model + Controller e roda o jogo numa thread própria
│           ├── JavaFxTela.java    A janela: HUD de atributos, texto da história, botões, menus, avisos
│           ├── Fundos.java        Carrega assets/fundos/fundo_<cenario>.jpg
│           └── Retratos.java      Carrega assets/personagens/<id>.png
└── controller/                    FLUXO do jogo — liga Model e View
    ├── Controlador.java           Menu principal, novo jogo, carregar/apagar saves, campanha
    ├── GerenciadorDeSessao.java   Game over, fim de campanha, reiniciar partida
    ├── ReiniciarPartidaException.java
    └── temporada/
        ├── Temporada.java         Base comum (conquistas, compras, ponto de retomada)
        └── TemporadaUm … TemporadaCinco.java   Escolhas e consequências de cada temporada

assets/                            tema.css (visual), fundos/ (cenários 1280x720), personagens/ (rostos 280x350)
tools/                             Geradores (Python + Pillow) dos fundos e dos retratos; não são necessários para jogar
test/                              JUnit 5 (espelha os pacotes de src/)
saves/  sons/  lib/                Dados em execução, áudio e dependência (gson)
```

### Regras de dependência

- `model` não importa `view` nem `controller` e **não escreve no console**. Falhas de gravação/leitura
  são entregues a um ouvinte (`Consumer<String>`) que o `Main` liga à View; sem ouvinte vão para o log.
- `view` não importa `controller` nem os gerenciadores/serviços do Model; só lê entidades (`Personagem`,
  `Progresso`) e constantes para exibir.
- `controller` decide o que acontece e chama a View para mostrar; não usa `System.out`.
- **Duas interfaces, um jogo:** o Controller só conhece a interface `Dialogos` e a `Narrativa`. A história
  é montada como dados (`Cena`) e entregue a uma `Saida`; o console imprime, a janela desenha botões. Trocar
  de interface não muda uma linha de Model nem de Controller.
- Só `view/fx/jfx` importa JavaFX. Todo o resto da interface gráfica (`DialogosFx`, a ponte, o formatador)
  é Java puro e tem testes que rodam sem JavaFX.

## Rodar o jogo

Rode sempre **a partir da raiz do projeto** (`saves/`, `sons/` e `assets/` são caminhos relativos).

### Console (não precisa de nada além do JDK)

O código da janela depende do JavaFX, então o build só do console deixa essas classes de fora:

```bash
# Linux/macOS
mkdir -p out
javac -encoding UTF-8 -cp lib/gson-2.14.0.jar -d out \
  $(find src -name "*.java" ! -path "src/view/fx/jfx/*" ! -name "MainGui.java")
java -cp out:lib/gson-2.14.0.jar Main
```

```bat
:: Windows (cmd)
mkdir out
dir /s /b src\*.java | findstr /v /i "\\jfx\\ MainGui.java" > fontes.txt
javac -encoding UTF-8 -cp lib\gson-2.14.0.jar -d out @fontes.txt
java -cp "out;lib\gson-2.14.0.jar" Main
```

### Janela gráfica (JavaFX)

O JavaFX **não vem mais dentro do JDK**. O JavaFX 26 foi lançado junto com o JDK 26 e exige JDK 24 ou superior
(o projeto usa o 26).

1. Baixe o **JavaFX SDK 26** para o seu sistema em <https://gluonhq.com/products/javafx/> e descompacte em
   qualquer pasta (por exemplo `C:\javafx-sdk-26` ou `~/javafx-sdk-26`).
2. **IntelliJ:** *File → Project Structure → Libraries → + → Java* e escolha a pasta `lib` do SDK; depois crie
   uma configuração de execução para `MainGui` com:
   - *VM options:* `--module-path "<pasta do SDK>/lib" --add-modules javafx.controls`
   - *Working directory:* a raiz do projeto (importante: é de lá que saem `assets/`, `saves/` e `sons/`).
3. **Linha de comando:**

```bash
SDK=~/javafx-sdk-26/lib   # ajuste o caminho
mkdir -p out
javac -encoding UTF-8 --module-path $SDK --add-modules javafx.controls \
  -cp lib/gson-2.14.0.jar -d out $(find src -name "*.java")
java --module-path $SDK --add-modules javafx.controls -cp out:lib/gson-2.14.0.jar MainGui
```

Controles: mouse, ou as teclas **1, 2, 3...** para escolher; **Enter/Espaço** para continuar; **F11** alterna tela cheia.

**Arte:** todos os cenários da história têm fundo próprio: o menu (e suas telas), os títulos de temporada, as cenas
das Temporadas 1 a 5 e cada um dos finais. O fundo genérico só aparece para um cenário novo que ainda não tenha arte.

## Personagens e rostos

Cada fala mostra o **rosto de quem está falando**, ao lado do texto. O protagonista tem **um rosto por origem**,
que aparece também na barra de atributos e nos cartões de escolha da origem:

| Rosto | Aparência |
|---|---|
| Valter, Professor Tradicional | grisalho, óculos redondos, bigode, colete mostarda e gravata verde |
| Valter, Ex-Químico Industrial | capacete amarelo, barba por fazer, cicatriz, colete refletivo laranja |
| Valter, Químico Precavido | óculos de proteção na testa, jaleco, máscara de gás no peito, olhar apreensivo |
| Marisa | cabelo castanho avermelhado comprido, cardigã verde-azulado, brincos, olhar preocupado |
| Kadu | gorro vermelho, moletom cinza, corrente dourada, cavanhaque, sorriso de lado |
| Heitor | cabelo curto escuro, bigode grosso, polo azul-marinho com crachá de policial |
| Enzo | cabelo bagunçado, camiseta azul, rosto jovem e bochechas coradas |
| O Fidalgo | cabelo prateado penteado para trás, óculos sem aro, cavanhaque, terno cinza e gravata bordô |
| Maluco | careca, barba cheia, tatuagem no pescoço, regata vermelha, brinco e dente de ouro |
| Desconhecido | silhueta com "?", usada quando alguém fala sem ter rosto cadastrado |

**Quem fala:** todos os personagens com rosto têm falas na história. O Kadu aparece na rua, no furgão e no porão;
o Heitor na rua, na encruzilhada e nos finais de prisão, queda e delação; o Enzo na pia, na praia e na proteção a
testemunhas; o Fidalgo no atacado, no cerco e nos finais da Rota C.

**Adicionar uma fala:** escreva a linha na `Narrativa` no formato `NOME: — texto`; o rosto aparece sozinho.
**Adicionar um personagem novo:** gere o retrato (veja `tools/gerar_retratos.py`), salve em `assets/personagens/<id>.png`
(280x350), acrescente o id em `Personagens.TODOS` e a regra em `Personagens.retratoDoOrador`. O teste
`NarrativaTest` falha com uma mensagem clara se alguém falar na história sem ter rosto.

**Adicionar arte a um cenário:** salve uma imagem JPEG de 1280x720 como `assets/fundos/fundo_<cenario>.jpg`
(ou acrescente uma função em `tools/gerar_fundos.py` e rode `python tools/gerar_fundos.py <cenario>`).
Cenário sem imagem usa `fundo_generico.jpg`, então nada quebra, mas o `NarrativaTest` avisa. Cenários usados:
`menu`, `titulo`, `generico`, `jantar`, `rua`, `laboratorio`, `desfecho` (Temporada 1), `furgao`, `pia` (T2),
`esconderijo`, `atacadista` (T3), `fundo_falso`, `encruzilhada` (T4), `fuga`, `praia`, `ataque`, `prisao`,
`delacao`, `ricina` (finais), `fim` (epílogos e game over) e `status` (ficha do personagem).
Para mudar cores e fontes, edite `assets/tema.css`.

## Testar

Na IntelliJ, clique direito em `test` → *Run All Tests* (JUnit 5.14 já está no módulo). Os testes usam pastas
temporárias e **não** alteram `saves/`. Nenhum teste precisa do JavaFX: a lógica da janela é testada com uma
tela falsa, incluindo uma campanha completa (Temporadas 1 a 5) jogada pela interface gráfica.

## Origens e finais

A origem escolhida no começo (Professor Tradicional, Ex-Químico Industrial ou Químico Precavido) muda o texto
de **todos** os finais: a recusa da Rota A, a queda, o desaparecimento e a prisão da Rota B, a explosão e a ricina da
Rota C e a delação da Rota D. As conquistas são as mesmas para as três origens. O texto do professor é o original de
cada final e também o padrão para origem desconhecida. Para ajustar um final, edite o `switch (Origem.de(...))` do
método correspondente na `Narrativa`; o `FinaisPorOrigemTest` confere que as três versões são diferentes.

**Introdução:** o jogo novo abre com um prólogo em duas telas (`Narrativa.mostrarIntroducaoParte1/2`, fundos
`introducao` e `consultorio`), diferente para cada origem: quem o protagonista é, a vida em casa e o diagnóstico.

**Falas por origem:** não são só os finais. Quase toda fala de Marisa, Enzo, Kadu, Heitor, Maluco e Fidalgo muda
conforme a origem (o professor é o texto original; o ex-químico industrial é tratado como o homem da fábrica; o
químico precavido, como o que confere tudo duas vezes). Dentro da `Narrativa`, use o helper
`por(antecedente, professor, industrial, precavido)` para escrever as três versões de uma fala. As cenas que dependem
da origem recebem `(nome, ..., antecedente)`, e o `NarrativaTest` confere que as três versões diferem.

## Compras e fim de jogo

- **Compras:** a Máscara de Gás (R$ 1000), os Passaportes Falsos (R$ 8000) e o Colete Balístico (R$ 5000) só podem ser
  comprados com saldo suficiente. Os preços ficam em `Itens`; `PersonagemService.podePagar` decide. Sem dinheiro, a opção
  aparece **desativada** na janela, com o aviso `[SEM DINHEIRO: custa R$ ...]`; no console, digitar essa opção é recusado
  e a pergunta se repete (`Temporada.lerOpcaoComCompra`). Para criar uma compra nova, marque a opção com
  `opcaoDeCompra(...)` na `Narrativa` e leia a escolha com `lerOpcaoComCompra(...)` na temporada.
- **Game Over e fim da campanha:** na janela, a mensagem fica visível e os botões **Reiniciar partida** e **Voltar ao
  menu principal** aparecem no canto inferior direito (`Pergunta.Modo.FIM`). As teclas 1 e 2 também funcionam.

## Pausa e salvamento

**Botão de pausa** (canto superior esquerdo da janela, ou a tecla Esc) abre o menu: **Continuar** (só fecha a pausa, o
jogo fica onde estava), **Reiniciar partida**, **Salvar progresso** e **Sair** (volta ao menu principal). Reiniciar e
Sair pedem confirmação, e o botão some na tela de Game Over / fim da campanha. Não há mais as perguntas "Ponto de
descanso" no meio das temporadas.

- **Salvar progresso** grava o **ponto de retomada**: o personagem e o capítulo no estado em que estavam no início da
  cena em que o jogador pausou (`PersonagemService.registrarCheckpoint` / `salvarCheckpoint`). Escolhas já resolvidas
  ficam valendo; a cena em andamento recomeça. O ponto é marcado logo depois das consequências de cada capítulo, e
  `Progresso.capitulo` guarda onde retomar (0 = início da temporada, também para saves antigos).
- **Automático:** ao concluir a temporada N, o save passa a apontar para a **N+1** (um aviso rápido, sem pedir resposta).
  Ao concluir a Temporada 4 também grava a rota escolhida (`rotaFinal`: 1 = B, 2 = C, 3 = D).
- **Reiniciar** pela pausa não apaga o save existente. O Game Over, ao reiniciar, continua apagando o slot da partida perdida.
- Cada temporada recebe `executarTemporada(valter, capituloInicial)`; um capítulo novo precisa de um
  `if (capituloInicial <= n)` e de uma chamada a `registrarCheckpoint(...)` depois das suas consequências.
- **Saves antigos** (sem `rotaFinal` ou `capitulo`) continuam abrindo. Um save na Temporada 5 sem rota gravada volta para a 4,
  onde a rota é escolhida.
- A pausa existe só na janela. No console o salvamento é só o automático, no fim de cada temporada.

## Comportamentos conhecidos

- O slot só é gravado nos pontos de retomada (fim de temporada ou "Salvar progresso"), não a cada consequência.
  Fechar a janela sem salvar volta ao último ponto salvo, e as cenas jogadas depois dele são repetidas, sem efeitos duplicados.
- Se `sons/` não existir, o jogo roda sem som; se a máquina não tiver dispositivo de áudio, avisa uma vez e segue sem som.
