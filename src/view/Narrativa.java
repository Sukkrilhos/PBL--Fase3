package view;

import model.Antecedentes;
import model.Itens;

/**
 * Conteúdo da história: todo o texto narrativo (cenas, desfechos e efeitos das escolhas), montado como
 * {@link Cena} e entregue a uma {@link Saida} (console ou JavaFX). Não lê entrada nem decide nada; os
 * controllers das temporadas dizem quando cada trecho é exibido.
 */
public class Narrativa {
    private final GerenciadorDeAudio audio;
    private final Saida saida;

    public Narrativa(GerenciadorDeAudio audio, Saida saida) {
        this.audio = audio;
        this.saida = saida;
    }

    /**
     * Origem do protagonista, só para escolher o texto dos finais. Origem desconhecida (save antigo ou editado
     * à mão) cai no professor, que é o texto original de cada final.
     */
    private enum Origem {
        PROFESSOR, INDUSTRIAL, PRECAVIDO;

        static Origem de(String antecedente) {
            if (Antecedentes.EX_QUIMICO_INDUSTRIAL.equals(antecedente)) return INDUSTRIAL;
            if (Antecedentes.QUIMICO_PRECAVIDO.equals(antecedente)) return PRECAVIDO;
            return PROFESSOR;
        }
    }

    // Escolhe a versão da fala conforme a origem; o professor é o texto original e o padrão
    private static String por(String antecedente, String professor, String industrial, String precavido) {
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL: return industrial;
            case PRECAVIDO: return precavido;
            default: return professor;
        }
    }

    // Opção que compra um item: sem dinheiro ela aparece desativada e diz quanto custa
    private static void opcaoDeCompra(Cena cena, int numero, String texto, int preco, boolean podeComprar) {
        if (podeComprar) {
            cena.opcao(numero, texto);
        } else {
            cena.opcaoIndisponivel(numero, "[SEM DINHEIRO: custa R$ " + preco + "] " + texto);
        }
    }

    // =========================================================================
    // TEMPORADA 1: DIÁLOGOS E INTERAÇÕES
    // =========================================================================

    public void mostrarCenaJantarParte1(String nome, String antecedente) {
        Cena cena = new Cena("jantar");
        cena.linha("\n=== CAPÍTULO 1: O Jantar em Família ===");
        cena.linha("Marisa coloca os pratos na mesa, mas evita olhar nos seus olhos. O silêncio é pesado.");
        cena.linha("MARISA: — " + nome + "... " + por(antecedente,
                "a secretária do Dr. Nogueira ligou. Ela disse que você saiu do consultório apressado. O que aconteceu?",
                "a secretária do Dr. Nogueira ligou. Disse que você saiu apressado, sem remarcar nada. Logo você, que sempre cumpriu horário como se ainda estivesse na linha de produção. O que aconteceu?",
                "a secretária do Dr. Nogueira ligou. Disse que você saiu do consultório apressado. Logo você, que confere tudo duas vezes antes de sair de qualquer lugar. O que aconteceu?"));
        cena.linha("\nO que você responde?");
        cena.opcao(1, "'Não foi nada, Marisa. Só uma confusão com os horários.'");
        cena.opcao(2, "'Ele... ele pediu mais exames. Apenas isso.'");
        cena.prompt("Sua resposta: ");
        saida.exibir(cena);
    }

    public void mostrarCenaJantarParte2(String nome, int respostaAnterior, String antecedente) {
        Cena cena = new Cena("jantar");
        if (respostaAnterior == 1) {
            cena.linha("\nMARISA: (Suspirando irritada) — " + por(antecedente,
                    "Você sempre diz que 'não é nada'. Acha que eu sou cega para não ver você tossindo a noite toda?",
                    "Você sempre diz que 'não é nada'. Anos respirando solvente naquela fábrica e agora essa tosse a noite toda. Acha que eu sou cega?",
                    "Você sempre diz que 'não é nada'. Você, que lê bula de remédio até o fim! Acha que eu não vejo você tossindo a noite toda?"));
        } else {
            cena.linha("\nMARISA: (Parando de servir a comida, assustada) — Mais exames? " + nome + ", " + por(antecedente,
                    "você está me deixando em pânico. É algo grave?",
                    "depois de tudo o que o ar daquela fábrica já te fez, eu estou em pânico. É algo grave?",
                    "você nunca deixa nada para depois e agora esconde o resultado? Eu estou em pânico. É algo grave?"));
        }

        cena.linha("\nEla senta à mesa e segura sua mão. O diagnóstico de câncer de pulmão queima no seu bolso.");
        cena.linha("\nCOMO VOCÊ ENCERRA O ASSUNTO? (Escolha Crítica)");
        cena.opcao(1, "Puxar a mão, inventar que está com problemas na escola e mudar de assunto bruscamente.");
        cena.opcao(2, "Apertar a mão dela, segurar o choro e dizer que 'não foi um bom dia, mas vamos resolver juntos'.");
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }

    public void mostrarCenaAEscolha(String nome, String antecedente) {
        Cena cena = new Cena("rua");
        cena.linha("\n=== CAPÍTULO 1: A Escolha ===");
        cena.linha("Após acompanhar uma operação policial com seu cunhado Heitor, " + nome + " encurrala o ex-aluno Kadu.");
        cena.linha("HEITOR: — Essa gente nova não larga o osso, " + nome + ". " + por(antecedente,
                "E o nome desse moleque, o Kadu, aparece em tudo que a gente investiga. Fique longe dele.",
                "O tal do Kadu aparece em tudo que a gente investiga. Você passou anos numa fábrica e sabe o estrago que produto mal feito causa. Fique longe dele.",
                "O tal do Kadu aparece em tudo que a gente investiga. Você é o mais cuidadoso da família, então me escute: fique longe dele."));
        cena.linha("KADU: — Professor?! O senhor aqui? Se veio me passar sermão, perdeu a viagem. Meu produto é fraco, todo mundo sabe. " + por(antecedente,
                "Se o senhor entende de química como entendia na sala de aula, a gente manda nessa cidade.",
                "Mas dizem que o senhor já trabalhou em fábrica, produzindo em escala. Se for verdade, a gente manda nessa cidade.",
                "Mas o senhor era o único que nunca errava a conta no laboratório, sempre de óculos e máscara. Com o senhor, a gente manda nessa cidade."));
        cena.linha("Você tem a genialidade química. Ele tem as ruas. Você pode fabricar metanfetamina para salvar sua família.");
        cena.opcao(1, "Recusar. 'Não. Isso não sou eu.'");
        cena.opcao(2, "Aceitar. 'Tenho uma proposta melhor do que você imagina.'");
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }

    public void mostrarCenaFornada(String nome, boolean podeComprar) {
        Cena cena = new Cena("laboratorio");
        cena.linha("\n=== CAPÍTULO 2: A Primeira Fornada ===");
        cena.linha("A fumaça do laboratório improvisado no furgão está destruindo seus pulmões já debilitados.");
        cena.opcao(1, "Pausar a produção para respirar (Ganha menos dinheiro, mas poupa saúde).");
        cena.opcao(2, "Ignorar a dor e forçar a produção máxima (Ganha muito dinheiro, mas derruba a saúde e corrompe a moral).");
        opcaoDeCompra(cena, 3, "Investir R$ " + Itens.PRECO_MASCARA_DE_GAS + " numa Máscara de Gás profissional (Perde dinheiro, ganha item 'Máscara de Gás' e poupa saúde).",
                Itens.PRECO_MASCARA_DE_GAS, podeComprar);
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }

    // O final de quem recusa o crime depende da origem escolhida no início do jogo.
    // Origem desconhecida (save antigo ou editado à mão) cai no final do professor, que é o original.
    public void mostrarDesfechoRotaA(String nome, String antecedente) {
        Cena cena = new Cena("desfecho");

        cena.linha("\n" + LINHA);
        cena.linha("CAPÍTULO FINAL: O Homem Honesto (ROTA A)");
        cena.linha(LINHA);
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha("Você olhou nos olhos de Kadu e balançou a cabeça. Anos de indústria química já lhe mostraram até onde um homem desce por dinheiro.");
                cena.linha(nome + " gastou os R$ 3.000 guardados nas primeiras sessões do tratamento e passou a aceitar bicos como consultor de segurança de reagentes.");
                cena.linha("Infelizmente, o câncer avançou implacavelmente e as dívidas engoliram sua família.");
                cena.linha("Você morreu sem um tostão, ainda carregando o peso do seu passado na fábrica, mas sem somar o crime a ele. Marisa e Enzo nunca souberam do que você quase se tornou.");
                break;
            case PRECAVIDO:
                cena.linha("Você olhou nos olhos de Kadu e balançou a cabeça. Precavido por natureza, você sabia que aquele mundo não perdoa o menor erro, e que você não teria como errar.");
                cena.linha(nome + " guardou a máscara de gás numa gaveta e seguiu fazendo bicos de química para pagar as contas.");
                cena.linha("Infelizmente, o câncer avançou implacavelmente e as dívidas engoliram sua família.");
                cena.linha("Você morreu cauteloso até o fim, com a consciência limpa e a máscara de gás esquecida no fundo da gaveta. Marisa e Enzo nunca souberam do que você quase se tornou.");
                break;
            default: // Professor Tradicional
                cena.linha("Você olhou nos olhos de Kadu e balançou a cabeça. O crime não era o seu caminho.");
                cena.linha(nome + " continuou dando aulas de química e fazendo bicos para pagar as contas.");
                cena.linha("Infelizmente, o câncer avançou implacavelmente e as dívidas engoliram sua família.");
                cena.linha("Você morreu pobre, mas com a consciência limpa. Marisa e Enzo nunca souberam do que você quase se tornou.");
        }
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    // =========================================================================
    // TEMPORADA 2: DIÁLOGOS E INTERAÇÕES
    // =========================================================================

    public void mostrarCenaFurgaoEncurraladoParte1(String nome, boolean temMascara, String antecedente) {
        Cena cena = new Cena("furgao");
        cena.linha("\n=== CAPÍTULO 3: O Furgão Encurralado ===");
        cena.linha("Dois carros pretos fecham a saída do beco escuro.");
        cena.linha("O traficante 'Maluco' arranca Kadu de dentro do furgão e aponta uma arma para a cabeça dele.");
        cena.linha("MALUCO: — " + por(antecedente,
                "Quem é o químico que tá invadindo a minha praça? Fala ou o moleque morre aqui!",
                "Disseram que tem um ex-fábrica cozinhando na minha praça. Fala ou o moleque morre aqui!",
                "Disseram que tem um químico cheio de cuidados, de máscara e óculos, invadindo a minha praça. Fala ou o moleque morre aqui!"));
        cena.linha("KADU: — " + por(antecedente,
                "Professor, faz alguma coisa! Ele vai me matar!",
                "Professor, o senhor sabe lidar com pressão, já trabalhou em fábrica! Faz alguma coisa, ele vai me matar!",
                "Professor, o senhor sempre tem um plano B! Faz alguma coisa, ele vai me matar!"));
        cena.linha("\nO que você faz?");
        cena.opcao(1, "Descer com calma e negociar uma porcentagem dos lucros com ele.");
        cena.opcao(2, "Jogar reagentes químicos na chapa para criar um gás de fuga e escapar correndo.");
        if (temMascara) {
            cena.opcao(3, "[OPÇÃO SECRETA: MÁSCARA DE GÁS] Trancar o furgão, inundá-lo com gás mostarda, colocar a máscara e assistir os inimigos sufocarem.");
        }
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }

    public void mostrarCenaFurgaoEncurraladoParte2(String nome, int escolhaAnterior, String antecedente) {
        Cena cena = new Cena("furgao");
        if (escolhaAnterior == 1) {
            cena.linha("\nMaluco aceitou a proposta, mas deixou claro que agora você trabalha sob o comando dele.");
            cena.linha("MALUCO: — " + por(antecedente,
                    "Você tem lábia, professor. Mas quem manda aqui sou eu.",
                    "Gosto de quem sabe produzir em escala. Mas quem manda aqui sou eu, " + nome + ".",
                    "Gostei da sua calma, doutor. Mas quem manda aqui sou eu."));
        } else if (escolhaAnterior == 2) {
            cena.linha("\nA fumaça química sufocou os capangas temporariamente. Vocês escaparam correndo no meio do caos!");
            cena.linha("KADU: — " + por(antecedente,
                    "Corre, professor, corre! Eu não enxergo nada!",
                    "Corre, professor! O senhor deve ter feito isso mil vezes na fábrica, né? Eu não enxergo nada!",
                    "Corre, professor! Eu sabia que o senhor tinha um plano! Eu não enxergo nada!"));
        } else if (escolhaAnterior == 3) {
            cena.linha("\nCompletamente imune graças à máscara, você sai do furgão caminhando por cima dos corpos asfixiados. Você até saqueou os bolsos deles.");
            cena.linha("KADU: — " + por(antecedente,
                    "O senhor... o senhor matou todo mundo? Meu Deus, professor.",
                    "O senhor matou todo mundo com o que aprendeu na fábrica? Meu Deus, professor.",
                    "O senhor planejou isso desde o começo, não foi? A máscara... meu Deus, professor."));
        }
        saida.exibir(cena);
    }

    public void mostrarCenaSanguePiaParte1(String nome, String antecedente) {
        Cena cena = new Cena("pia");
        cena.linha("\n=== CAPÍTULO 4: O Sangue na Pia ===");
        cena.linha("Você chega em casa de madrugada e tem uma forte crise de tosse, cuspindo sangue na pia do banheiro.");
        cena.linha("A porta se abre num estrondo. Marisa vê o sangue e entra em pânico completo.");
        cena.linha("\nMARISA: — " + nome + "! " + por(antecedente,
                "Por que você está sangrando assim?!",
                "É esse maldito ar da fábrica, não é? Por que você está sangrando assim?!",
                "Você, que se cuida tanto... por que está sangrando assim?!"));
        cena.linha("ENZO: (sonolento, da porta do corredor) — Pai? Mãe? Que barulho foi esse?" + por(antecedente,
                "",
                " De novo esse cheiro de solvente nas suas roupas...",
                " Você sempre diz que tudo é perigoso e agora está assim..."));
        cena.linha("\nO que você diz a ela?");
        cena.opcao(1, "Revelar parcialmente o câncer, mentindo que conseguiu um empréstimo para o tratamento.");
        cena.opcao(2, "Brigar com ela, dizer que é só uma pneumonia da escola e trancar a porta.");
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }

    public void mostrarCenaSanguePiaParte2(String nome, int escolhaAnterior, String antecedente) {
        Cena cena = new Cena("pia");
        if (escolhaAnterior == 1) {
            cena.linha("\nMarisa chora nos seus braços, mas alivia-se sabendo que a clínica está paga.");
            cena.linha("ENZO: — Pai, você tá doente? " + por(antecedente,
                    "",
                    "É por causa da fábrica? ",
                    "Mas você sempre foi o mais cuidadoso! ") + "Por que ninguém nunca me conta nada nessa casa?");
        } else {
            cena.linha("\nVocê a expulsou do banheiro de forma ríspida. O clima em casa virou um campo minado.");
            cena.linha("ENZO: — " + por(antecedente,
                    "Pai! Não fala assim com a mãe!",
                    "Pai! Você nunca gritou assim nem quando chegava exausto da fábrica! Não fala assim com a mãe!",
                    "Pai! Você nunca perde a calma! Não fala assim com a mãe!"));
        }
        saida.exibir(cena);
    }

    // =========================================================================
    // TEMPORADA 3: DIÁLOGOS E INTERAÇÕES
    // =========================================================================

    public void mostrarCenaNovoEsconderijoParte1(String nome, String antecedente) {
        Cena cena = new Cena("esconderijo");
        cena.linha("\n=== CAPÍTULO 5: O Novo Esconderijo ===");
        cena.linha("O furgão não é mais seguro para cozinhar em grande escala.");
        cena.linha("Kadu encontrou o porão de uma lavanderia abandonada, mas custa caro.");
        cena.linha("KADU: — Achei o lugar perfeito, professor. " + por(antecedente,
                "Ninguém passa por lá, e o dono só quer saber de dinheiro adiantado.",
                "Dá pra montar uma linha em escala, igual o senhor conhece de fábrica. Só que o dono quer dinheiro adiantado.",
                "Tem ventilação, saída dupla, tudo o que o senhor ia exigir. Só que o dono quer dinheiro adiantado."));
        cena.linha("O dono exige três meses de aluguel adiantados, em dinheiro vivo, e não aceita calote.");
        cena.linha("\nComo você consegue o porão?");
        cena.opcao(1, "Pagar o aluguel adiantado com as reservas de dinheiro da radioterapia.");
        cena.opcao(2, "Pagar o aluguel com reagentes desviados da escola, que o dono revende no mercado paralelo.");
        cena.opcao(3, "[CRIAR ITEM] Não pagar nada: sintetizar 'Ricina' com os reagentes que restam e usar a ameaça de um veneno indetectável para o dono ceder o porão calado (Custa Saúde e Moral, mas ganha uma arma secreta).");
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }

    public void mostrarCenaOAtacadistaParte1(String nome, String antecedente, boolean podeComprar) {
        Cena cena = new Cena("atacadista");
        cena.linha("\n=== CAPÍTULO 6: O Atacadista ===");
        cena.linha("Um distribuidor conhecido como 'O Fidalgo' exige uma cota de 50 quilos por semana.");
        cena.linha("FIDALGO: — Meu pessoal só fala bem do seu produto, " + nome + ". " + por(antecedente,
                "Admiro um homem de ciência. Cinquenta quilos por semana. Não é um pedido.",
                "Dizem que o senhor já produziu em escala de fábrica. Então não há desculpa: cinquenta quilos por semana. Não é um pedido.",
                "Dizem que o senhor confere cada lote duas vezes. Gosto de cautela, mas ela não paga a minha cota: cinquenta quilos por semana. Não é um pedido."));
        cena.linha("\nComo você responde?");
        cena.opcao(1, "Aceitar a cota monstruosa (Sacrifica saúde e relação familiar).");
        cena.opcao(2, "Bater o pé e impor um limite seguro de 20 quilos.");
        opcaoDeCompra(cena, 3, "[COMPRAR ITEM] Subornar o braço direito dele por R$ " + Itens.PRECO_PASSAPORTES_FALSOS + " para encomendar 'Passaportes Falsos' para a sua família.",
                Itens.PRECO_PASSAPORTES_FALSOS, podeComprar);
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }


    // =========================================================================
    // TEMPORADA 4: DIÁLOGOS E INTERAÇÕES
    // =========================================================================

    public void mostrarCenaFundoFalsoParte1(String nome, String antecedente, boolean podeComprar) {
        Cena cena = new Cena("fundo_falso");
        cena.linha("\n=== CAPÍTULO 7: O Fundo Falso ===");
        cena.linha("Enquanto arrumava o armário do quarto, Marisa encontrou uma tábua solta no piso.");
        cena.linha("Lá dentro, há uma mochila abarrotada de notas de dinheiro vivo e uma arma de fogo.");
        cena.linha("\nMARISA: — " + nome + "! " + por(antecedente,
                "O que é isso?! Dinheiro sujo? Uma arma?! Com quem você está se metendo?!",
                "O que é isso?! Dinheiro sujo? Uma arma?! Eu aguentei o cheiro e os turnos da fábrica, mas isto eu não aguento! Com quem você está se metendo?!",
                "O que é isso?! Você vive dizendo que cautela é tudo e esconde isto debaixo do nosso piso?! Com quem você está se metendo?!"));
        cena.linha("\nComo você lida com o flagrante?");
        cena.opcao(1, "Tentar manipular a situação, dizendo que é dinheiro legal.");
        cena.opcao(2, "Desabar parcialmente, confessando e exigindo silêncio absoluto.");
        opcaoDeCompra(cena, 3, "[COMPRAR ITEM] Ignorar as lágrimas, gastar R$ " + Itens.PRECO_COLETE_BALISTICO + " do fundo com um contato obscuro e encomendar um 'Colete Balístico' para a guerra inevitável.",
                Itens.PRECO_COLETE_BALISTICO, podeComprar);
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }

    public void mostrarCenaFundoFalsoParte2(String nome, int escolhaAnterior, String antecedente) {
        Cena cena = new Cena("fundo_falso");
        if (escolhaAnterior == 1) {
            cena.linha("\nMarisa percebe que você está mentindo na cara dura. A confiança morre aqui.");
            cena.linha("MARISA: — " + por(antecedente,
                    "Dinheiro legal? Você nunca soube mentir, " + nome + ".",
                    "Dinheiro legal? Anos de fábrica e você nunca trouxe nem metade disso para casa.",
                    "Dinheiro legal? Você guarda até recibo de farmácia, " + nome + ". Cadê a nota disso tudo?"));
        } else if (escolhaAnterior == 2) {
            cena.linha("\nEla chora copiosamente, horrorizada com o monstro em que você se transformou.");
            cena.linha("MARISA: — " + por(antecedente,
                    "Eu não consigo mais olhar para você.",
                    "Eu esperava a doença, a fábrica, qualquer coisa. Isso não. Eu não consigo mais olhar para você.",
                    "Você sempre teve medo de machucar alguém. Olha no que virou. Eu não consigo mais olhar para você."));
        } else if (escolhaAnterior == 3) {
            cena.linha("\nVocê veste o colete e tranca o dinheiro. Marisa arruma as malas e sai de casa aterrorizada.");
            cena.linha("MARISA: — " + por(antecedente,
                    "Um colete?! Você vai me deixar sozinha com isso?",
                    "Um colete? Na fábrica era para proteger a sua vida. Agora é para proteger o quê, " + nome + "?",
                    "Um colete?! Seu plano de sempre ter uma proteção, e agora é contra bala?!"));
        }
        saida.exibir(cena);
    }

    public void mostrarCenaEncruzilhadaParte1(String nome, String antecedente) {
        Cena cena = new Cena("encruzilhada");
        cena.linha("\n=== CAPÍTULO 8: O Cerco ===");
        cena.linha("O cerco final se fechou. Heitor está na cola das evidências na Polícia Federal,");
        cena.linha("e os capangas de 'O Fidalgo' descobriram a localização do porão e querem eliminar você.");
        cena.linha("Uma mensagem de voz acaba de chegar ao seu celular.");
        cena.linha("FIDALGO: — Eu sei onde o senhor cozinha, " + nome + ". " + por(antecedente,
                "Tem até amanhã para reconsiderar a nossa sociedade.",
                "Um homem de fábrica sabe quando uma operação passou do limite de segurança. Tem até amanhã para reconsiderar a nossa sociedade.",
                "Para um homem tão cauteloso, deixou o porão bem exposto. Tem até amanhã para reconsiderar a nossa sociedade."));
        cena.linha("\n" + nome.toUpperCase() + ": — " + por(antecedente,
                "Acabou o tempo. Não dá mais para esconder de ninguém.",
                "Acabou o tempo. Já vi muita fábrica estourar por ignorar o alarme, e eu ignorei o meu.",
                "Acabou o tempo. Eu ensaiei todos os planos, menos este."));
        cena.linha("\nQual será o seu último passo antes do colapso?");
        cena.opcao(1, "Tentar empacotar o que sobrou do dinheiro, comprar identidades falsas e fugir do país.");
        cena.opcao(2, "Aceitar o fim, ficar para trás e armar um plano explosivo para destruir o cartel de vez.");
        cena.opcao(3, "[CRIAR ITEM] Reunir provas bancárias e rotas num 'Dossiê do Cartel' para tentar um acordo com a Polícia (NOVA ROTA D).");
        cena.prompt("Sua escolha: ");
        saida.exibir(cena);
    }

    public void mostrarCenaEncruzilhadaParte2(String nome, int escolhaAnterior, String antecedente) {
        Cena cena = new Cena("encruzilhada");
        if (escolhaAnterior == 1) {
            cena.linha("\nVocê corre contra o tempo para arrumar as malas. A rota da fuga está traçada.");
        } else if (escolhaAnterior == 2) {
            cena.linha("\nVocê acende um cigarro com as mãos firmes. Não há mais fuga. É hora da guerra.");
        } else if (escolhaAnterior == 3) {
            cena.linha("\nVocê passa a noite a imprimir documentos e liga para o telemóvel do seu cunhado Heitor.");
            cena.linha("HEITOR: — " + por(antecedente,
                    "Três da manhã, cunhado? Se isso for o que eu estou pensando, não diga mais nada por telefone. Me encontre onde a gente combinou.",
                    "Três da manhã, cunhado? Você nunca ligou fora de turno. Se isso for o que eu estou pensando, não diga mais nada por telefone. Me encontre onde a gente combinou.",
                    "Três da manhã, cunhado? Logo você, que nunca age sem planejar. Se isso for o que eu estou pensando, não diga mais nada por telefone. Me encontre onde a gente combinou."));
        }
        saida.exibir(cena);
    }

    // =========================================================================
    // TEMPORADA 5: DESFECHOS FINAIS
    // =========================================================================

    // Cada final abaixo tem um texto por origem. O do professor é o original e também o padrão
    // para origem desconhecida; as outras duas origens trocam só as linhas que mudam o tom do final.

    public void mostrarCenaFuga(String nome, String antecedente) {
        Cena cena = new Cena("fuga");
        audio.tocarSom("sons/gameover.wav"); // Efeito sonoro de derrota
        cena.linha("\n" + LINHA);
        cena.linha("CAPÍTULO 10: Sangue no Tapete (ROTA B - A QUEDA)");
        cena.linha(LINHA);
        cena.linha("Você tentou fugir, mas foi interceptado. Os assassinos do Cartel invadiram a casa.");
        cena.linha("Tentando proteger o dinheiro, você levou um tiro fatal no abdômen.");
        cena.linha("Heitor arromba a porta com a polícia apenas para encontrar você sangrando no chão da sala.");
        cena.linha("HEITOR: — Aguenta firme, cunhado! " + por(antecedente,
                "A ambulância tá chegando! Fala comigo!",
                "Você sobreviveu a anos de fábrica, não vai ser agora! Fala comigo!",
                "Você sempre se cuidou tanto, não vai ser agora! Fala comigo!"));
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha("Nos últimos segundos você pensa na fábrica: já tinha visto homens caírem no concreto por muito menos, e jurou que nunca seria um deles.");
                cena.linha("Sua família sobreviveu, mas perdeu tudo: o dinheiro, a honra e você.");
                cena.linha("O crime não compensou. Nem para quem sempre soube o preço que ele cobra.");
                break;
            case PRECAVIDO:
                cena.linha("Você havia ensaiado cada saída e cada plano reserva. Só não ensaiou o que fazer quando todos falhassem ao mesmo tempo.");
                cena.linha("Sua família sobreviveu, mas perdeu tudo: o dinheiro, a honra e você.");
                cena.linha("O crime não compensou. Nem para quem passou a vida se precavendo.");
                break;
            default:
                cena.linha("Sua família sobreviveu, mas perdeu tudo: o dinheiro, a honra e você.");
                cena.linha("O crime não compensou.");
        }
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    public void mostrarCenaFugaSucesso(String nome, String antecedente) {
        Cena cena = new Cena("praia");
        audio.tocarSom("sons/sucesso.wav"); // Efeito sonoro de vitória
        cena.linha("\n" + LINHA);
        cena.linha("CAPÍTULO 10: O Desaparecimento (ROTA B - FINAL SECRETO)");
        cena.linha(LINHA);
        cena.linha("Quando os assassinos do Cartel invadiram a sua casa, você já não estava lá.");
        cena.linha("Graças aos Passaportes Falsos que comprou, " + nome + ", Marisa e Enzo cruzaram a fronteira.");
        cena.linha("Heitor encontrou apenas uma casa vazia. O seu império ruiu, mas a sua família sobreviveu,");
        cena.linha("escondida numa praia isolada da América Central, a viver do dinheiro que restou.");
        cena.linha("ENZO: — Pai, a gente vai poder ficar aqui?");
        String eu = nome.toUpperCase();
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha(eu + ": — Enquanto a gente souber onde ficam as saídas, filho.");
                cena.linha("Foi o velho hábito da fábrica que salvou vocês: você nunca entrava num lugar sem antes saber onde ficava a saída de emergência. A casa tinha três.");
                cena.linha("Você perdeu o poder, mas salvou a sua vida. Pela primeira vez em anos, o cheiro que chegava até você era o de sal, e não o de solvente.");
                break;
            case PRECAVIDO:
                cena.linha(eu + ": — Enquanto eu conferir todas as portas antes de dormir, sim.");
                cena.linha("Cada documento estava duplicado, cada rota tinha um plano B e um plano C. Foi a mania de uma vida inteira que tirou a sua família de lá.");
                cena.linha("Você perdeu o poder, mas salvou a sua vida. A máscara de gás virou lembrança numa gaveta, e o único ar que você respira agora é limpo.");
                break;
            default:
                cena.linha(eu + ": — Hoje, sim, filho. Para sempre é muito tempo.");
                cena.linha("Você perdeu o poder, mas salvou a sua vida.");
        }
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    public void mostrarCenaAtaque(String nome, String antecedente) {
        Cena cena = new Cena("ataque");
        audio.tocarSom("sons/explosao.wav"); // Efeito sonoro de explosão
        cena.linha("\n" + LINHA);
        cena.linha("CAPÍTULO 10: Reação em Cadeia (ROTA C - A VINGANÇA)");
        cena.linha(LINHA);
        cena.linha("FIDALGO: — O senhor veio sozinho, " + nome + "? " + por(antecedente,
                "Que coragem. Ou que tolice.",
                "Um homem de fábrica devia saber medir o tamanho do perigo. Que coragem. Ou que tolice.",
                "Logo o senhor, tão cauteloso? Que coragem. Ou que tolice."));
        cena.linha("Você usou seu gênio químico uma última vez. Com um explosivo no porta-malas,");
        cena.linha("você detonou o galpão inteiro, aniquilando todos os seus inimigos de uma só vez.");
        cena.linha("Gravemente ferido por estilhaços, você caminha pelo laboratório destruído,");
        cena.linha("toca no maquinário perfeito, sorri e dá seu último suspiro antes da polícia chegar.");
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha("Na fábrica, você passou anos aprendendo a evitar exatamente aquilo que agora provocou. Conhecia o preço de um erro, e dessa vez escolheu pagá-lo.");
                break;
            case PRECAVIDO:
                cena.linha("A máscara de gás ficou no banco do passageiro. Pela primeira vez na vida, você não planejou como sair.");
                break;
            default:
        }
        cena.linha("O dinheiro está seguro para Marisa e Enzo. Você venceu. Mas perdeu sua alma.");
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    public void mostrarCenaPrisao(String nome, String antecedente) {
        Cena cena = new Cena("prisao");
        audio.tocarSom("sons/gameover.wav");
        cena.linha("\n" + LINHA);
        cena.linha("CAPÍTULO 10: Atrás das Grades (ROTA B - FINAL ALTERNATIVO)");
        cena.linha(LINHA);
        cena.linha("Os assassinos do Cartel invadiram a sua casa. O tiro que deveria ser fatal no seu");
        cena.linha("abdômen foi parado pelo Colete Balístico que você comprou. Você sobreviveu à emboscada.");
        cena.linha("Minutos depois, Heitor arromba a porta. Você é algemado no chão da sua própria sala.");
        cena.linha("HEITOR: — Acabou, " + nome + ". " + por(antecedente,
                "",
                "Anos de fábrica sem uma multa e olha onde você veio parar. ",
                "O mais cuidadoso da família... ") + "Você tem o direito de permanecer calado.");
        cena.linha("Sua família está segura, mas você passará o resto dos seus dias numa cela de segurança máxima.");
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha("No pátio, você reconhece o ritmo de uma linha de produção: sirene, fila, turno. Já viveu isso uma vez, na fábrica. Só que agora não há salário nem portão de saída.");
                break;
            case PRECAVIDO:
                cena.linha("Você conferiu a cela como conferia tudo na vida: grades, trancas, saídas. Nenhuma. Pela primeira vez, a sua cautela não tinha para onde ir.");
                break;
            default:
        }
        cena.linha("Você está vivo. Mas perdeu a sua liberdade para sempre.");
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    public void mostrarCenaDelacao(String nome, String antecedente) {
        Cena cena = new Cena("delacao");
        audio.tocarSom("sons/sucesso.wav");
        cena.linha("\n" + LINHA);
        cena.linha("CAPÍTULO 10: O Acordo (ROTA D - A DELAÇÃO)");
        cena.linha(LINHA);
        cena.linha("Você encontrou-se com Heitor e entregou o Dossiê do Cartel.");
        cena.linha("HEITOR: — Isso derruba o Fidalgo inteiro. " + por(antecedente,
                "Você sabe que não tem volta, não sabe?",
                "Um homem de fábrica sabe que, depois do alarme, não tem volta. Sabe, não sabe?",
                "Você pesou cada detalhe, não pesou? Depois disso não tem volta."));
        cena.linha(nome.toUpperCase() + ": — Eu sei. Só quero que eles continuem vivos.");
        cena.linha("Na mesma noite, a Polícia Federal desmantelou todo o império de 'O Fidalgo' através das suas provas.");
        cena.linha("Você não é mais o poderoso " + nome + ". Você e a sua família entraram no Programa de");
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha("Proteção a Testemunhas. A viver no anonimato, sem dinheiro e a trabalhar como operador de empilhadeira num armazém.");
                cena.linha("Você nunca mais chegou perto de um reagente, e ainda assim a mão treme quando sente cheiro de solvente.");
                break;
            case PRECAVIDO:
                cena.linha("Proteção a Testemunhas. A viver no anonimato, sem dinheiro e a trabalhar como fiscal de segurança de um shopping.");
                cena.linha("Conferir extintores e saídas de emergência é o único emprego em que a sua mania de prevenir é elogiada.");
                break;
            default:
                cena.linha("Proteção a Testemunhas. A viver no anonimato, sem dinheiro e a trabalhar como");
                cena.linha("caixa num supermercado.");
        }
        cena.linha("ENZO: — Pai, por que o nosso sobrenome mudou?");
        cena.linha("A honra e o poder desapareceram. Mas todos sobreviveram.");
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    // =========================================================================
    // TÍTULOS E DESFECHOS DAS ESCOLHAS (antes espalhados pelos controllers)
    // =========================================================================

    private static final String LINHA = "========================================================";
    private static final String[] TITULOS_TEMPORADAS = {
            "          TEMPORADA 1: A QUÍMICA DA NECESSIDADE",
            "       TEMPORADA 2: A AMEAÇA NO RETROVISOR",
            "      TEMPORADA 3: A EXPANSÃO DO IMPÉRIO",
            "     TEMPORADA 4: O FALSO EQUILÍBRIO E O CERCO",
            "          TEMPORADA 5: O FIM DA LINHA"
    };

    // =========================================================================
    // INTRODUÇÃO: quem é o protagonista, conforme a origem escolhida
    // =========================================================================

    public void mostrarIntroducaoParte1(String nome, String antecedente) {
        Cena cena = new Cena("introducao");
        cena.linha("\n" + LINHA);
        cena.linha("PRÓLOGO: Um Homem Comum");
        cena.linha(LINHA);
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha("Durante quinze anos, " + nome + " foi químico numa fábrica de solventes nos arredores da cidade: turnos de doze horas, cheiro de produto no cabelo e a certeza de que sabia como cada reação se comporta quando ninguém está olhando.");
                cena.linha("Então a fábrica fechou. A rescisão durou menos do que ele imaginava. Sobrou uma vaga de professor de química no colégio, um salário de professor e o hábito de procurar as saídas de emergência em qualquer sala em que entra.");
                cena.linha("Ele guarda R$ 3000 do que restou da rescisão numa lata de café, no fundo do armário. E guarda também uma culpa antiga: o dia em que um vazamento mandou dois colegas para o hospital. Ele havia avisado. Ninguém o culpou. Ele se culpa até hoje.");
                cena.linha("Os anos respirando vapor deixaram um presente que não passa: uma tosse seca, que piora de madrugada.");
                cena.linha("MARISA: — Você promete que vai ao Dr. Nogueira amanhã? Essa tosse começou na fábrica, " + nome + ", e nunca mais foi embora.");
                break;
            case PRECAVIDO:
                cena.linha(nome + " sempre foi o homem que confere duas vezes: a validade do extintor, a tranca da porta, a etiqueta de cada frasco, a apólice do seguro. Professor de química há muitos anos, não deixa um aluno entrar no laboratório sem óculos, e guarda em casa uma máscara de gás, \"por precaução\".");
                cena.linha("A vida inteira ele economizou riscos como outros economizam dinheiro. Nunca arriscou nada e nunca perdeu nada. Também nunca teve quase nada: o salário é curto, e o futuro de Enzo, o filho adolescente, depende de um plano que não fecha.");
                cena.linha("Marisa brinca que ele é o único homem do mundo que lê a bula do remédio e o manual do liquidificador. Ele ri. Mas, em silêncio, conta os passos até a saída em todo lugar em que entra.");
                cena.linha("Há semanas, uma tosse seca chegou sem avisar. Foi a única coisa que ele não previu.");
                cena.linha("MARISA: — Já marcou aquele exame, " + nome + "? Logo você, que nunca deixa nada para depois.");
                break;
            default:
                cena.linha("Há vinte e dois anos, " + nome + " dá aulas de química no mesmo colégio público da cidade. Sabe a tabela periódica de cor, corrige provas de madrugada e acredita, com a teimosia dos homens honestos, que o conhecimento ainda salva alguém.");
                cena.linha("Mas os alunos olham para o celular, o salário atrasa e o diretor já não lembra o seu nome sem consultar a lista.");
                cena.linha("Em casa, Marisa faz as contas do mês com um lápis e uma calculadora. Enzo, o filho adolescente, precisa de um aparelho nos dentes e de um futuro que ninguém sabe como pagar. Heitor, o cunhado, é policial e o único da família com a vida resolvida.");
                cena.linha(nome + " nunca pegou nada que não fosse seu. Nem um giz a mais.");
                cena.linha("MARISA: — Amanhã você passa no consultório do Dr. Nogueira, tá? Essa tosse não é de hoje.");
        }
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    public void mostrarIntroducaoParte2(String nome, String antecedente) {
        Cena cena = new Cena("consultorio");
        cena.linha("\n" + LINHA);
        cena.linha("PRÓLOGO: O Diagnóstico");
        cena.linha(LINHA);
        cena.linha("No consultório do Dr. Nogueira, a chapa de tórax brilha na caixa de luz. Há uma mancha onde não deveria haver nada.");
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha("O médico fala em exposição prolongada a solventes. \"Anos de fábrica\", diz, e " + nome + " reconhece as palavras como quem reconhece uma assinatura.");
                break;
            case PRECAVIDO:
                cena.linha("O médico diz que não havia como prever. Para um homem que passou a vida prevendo, a frase soa como uma segunda sentença.");
                break;
            default:
                cena.linha("O médico fala em radioterapia, em quimioterapia, em valores. Cada número parece uma anotação de aula que " + nome + " não consegue copiar.");
        }
        cena.linha("Câncer de pulmão, em estágio avançado. Com tratamento, talvez dois anos. Sem ele, bem menos.");
        cena.linha(por(antecedente,
                nome.toUpperCase() + ": — Vinte e dois anos ensinando o que a vida faz com a matéria. E agora é comigo.",
                nome.toUpperCase() + ": — A fábrica me pagou durante quinze anos para respirar aquilo. Agora cobra o resto.",
                nome.toUpperCase() + ": — Eu conferi tudo. Tudo. E mesmo assim não vi isto chegando."));
        cena.linha("O plano de saúde cobre uma fração do tratamento. O resto custa mais do que ele ganha em um ano, e o que sobraria para Marisa e Enzo depois dele?");
        cena.linha(nome + " se levanta, agradece por educação e sai sem remarcar nada. Pela primeira vez, faz uma conta que o assusta mais do que a doença: quanto vale uma vida honesta, e quanto vale a de quem ele deixa para trás.");
        cena.linha("Naquela noite, a mesa do jantar o espera.");
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    public void mostrarTituloTemporada(int numero) {
        Cena cena = new Cena("titulo");
        cena.linha("\n" + LINHA);
        cena.linha(TITULOS_TEMPORADAS[numero - 1]);
        cena.linha(LINHA);
        saida.exibir(cena);
    }

    // --- Temporada 1 ---

    public void mostrarReacaoJantar(String nome, int escolha, String antecedente) {
        Cena cena = new Cena("jantar");
        if (escolha == 1) {
            cena.linha("\nMARISA: — " + por(antecedente,
                    "Você está mentindo pra mim de novo. (Ela levanta e sai da mesa).",
                    "Você está mentindo pra mim de novo. Na fábrica, pelo menos, você admitia quando algo dava errado. (Ela levanta e sai da mesa).",
                    "Você está mentindo pra mim de novo. Logo você, que nunca escondeu nada de ninguém. (Ela levanta e sai da mesa)."));
            cena.linha("[Efeito: Moral -10 | Relação Familiar -10]");
        } else {
            cena.linha("\nMARISA: — " + por(antecedente,
                    "Nós vamos dar um jeito. Nós sempre damos. (Ela te abraça).",
                    "Nós vamos dar um jeito. Sobrevivemos à fábrica, aos turnos e às dívidas. Vamos sobreviver a isso. (Ela te abraça).",
                    "Nós vamos dar um jeito. Você sempre tem um plano, " + nome + ". (Ela te abraça)."));
            cena.linha("[Efeito: Relação Familiar +15]");
        }
        saida.exibir(cena);
    }

    public void mostrarAceiteDaProposta() {
        Cena cena = new Cena("rua");
        cena.linha("\nROTA B/C INICIADA: Você aceitou a proposta de Kadu.");
        cena.linha("[Efeito: Dinheiro +5000 | Moral -30]");
        saida.exibir(cena);
    }

    public void mostrarResultadoFornada(int escolha) {
        Cena cena = new Cena("laboratorio");
        switch (escolha) {
            case 1:
                cena.linha("\n[Efeito: Dinheiro -500 | Saúde Preservada]");
                break;
            case 2:
                cena.linha("\n[Efeito: Dinheiro +3000 | Saúde -20 | Moral -10]");
                break;
            case 3:
                cena.linha("\n[Efeito: Dinheiro -1000 | ITEM ADQUIRIDO: Máscara de Gás | Saúde Preservada]");
                break;
        }
        saida.exibir(cena);
    }

    // --- Temporada 2 ---

    public void mostrarResultadoFurgao(int escolha) {
        Cena cena = new Cena("furgao");
        switch (escolha) {
            case 1:
                cena.linha("\n[Efeito: Dinheiro -2000 | Moral -10]");
                break;
            case 2:
                cena.linha("\n[Efeito: Moral -20 | Fuga arriscada bem-sucedida]");
                break;
            case 3:
                cena.linha("\n[Efeito: Dinheiro +1000 (Saque) | Moral -40 | Fuga Impecável]");
                break;
        }
        saida.exibir(cena);
    }

    public void mostrarResultadoSanguePia(int escolha) {
        Cena cena = new Cena("pia");
        if (escolha == 1) {
            cena.linha("\n[Efeito: Saúde +10 | Relação Familiar +10]");
        } else {
            cena.linha("\n[Efeito: Saúde -15 | Relação Familiar -25]");
        }
        saida.exibir(cena);
    }

    // --- Temporada 3 ---

    public void mostrarResultadoEsconderijo(int escolha) {
        Cena cena = new Cena("esconderijo");
        switch (escolha) {
            case 1:
                cena.linha("\n[Efeito: Saúde -25 | Você pagou o aluguel adiantado com o dinheiro da radioterapia.]");
                break;
            case 2:
                cena.linha("\n[Efeito: Moral -20 | Você pagou o aluguel com reagentes roubados da escola na madruga.]");
                break;
            case 3:
                cena.linha("\n[Efeito: Saúde -15 | Moral -15 | ITEM ADQUIRIDO: Ricina | O dono, apavorado, cedeu o porão de graça.]");
                break;
        }
        saida.exibir(cena);
    }

    public void mostrarResultadoAtacadista(String nome, int escolha, String antecedente) {
        Cena cena = new Cena("atacadista");
        switch (escolha) {
            case 1:
                cena.linha("\n[Efeito: Dinheiro +10000 | Saúde -30 | Moral -15 | Relação Familiar -15]");
                break;
            case 2:
                cena.linha("\nFIDALGO: — " + por(antecedente,
                        "Vinte quilos? Então que sejam vinte. Por enquanto.",
                        "Vinte quilos? O senhor sabe que a fábrica daria conta de mais. Mas que sejam vinte. Por enquanto.",
                        "Vinte quilos? Cautela outra vez, " + nome + ". Que sejam vinte. Por enquanto."));
                cena.linha("[Efeito: Dinheiro +3000 | Você irritou um distribuidor perigoso de alto escalão.]");
                break;
            case 3:
                cena.linha("\n[Efeito: Dinheiro -8000 | ITEM ADQUIRIDO: Passaportes Falsos]");
                break;
        }
        saida.exibir(cena);
    }

    // --- Temporada 4 ---

    public void mostrarResultadoFundoFalso(int escolha) {
        Cena cena = new Cena("fundo_falso");
        switch (escolha) {
            case 1:
                cena.linha("\n[Efeito: Moral -20 | Relação Familiar despencou -35]");
                break;
            case 2:
                cena.linha("\n[Efeito: Relação Familiar -50]");
                break;
            case 3:
                cena.linha("\n[Efeito: Dinheiro -5000 | Relação Familiar -70 | ITEM ADQUIRIDO: Colete Balístico]");
                break;
        }
        saida.exibir(cena);
    }

    public void mostrarRotaEscolhida(int escolha) {
        Cena cena = new Cena("encruzilhada");
        switch (escolha) {
            case 1:
                cena.linha("\n[ROTA B ESCOLHIDA: Você tentará a fuga desesperada com a família.]");
                break;
            case 2:
                cena.linha("\n[ROTA C ESCOLHIDA: Você escolheu o caminho da vingança e do confronto final.]");
                break;
            case 3:
                cena.linha("\n[ROTA D ESCOLHIDA: Você escolheu entregar o seu império à polícia. ITEM: Dossiê do Cartel]");
                break;
        }
        saida.exibir(cena);
    }

    // --- Temporada 5 ---

    public void mostrarCenaRicina(String nome, String antecedente) {
        Cena cena = new Cena("ricina");
        cena.linha("\n" + LINHA);
        cena.linha("CAPÍTULO 10: O Toque Silencioso (ROTA C - FINAL SECRETO)");
        cena.linha(LINHA);
        cena.linha("Em vez de explodir tudo e morrer no processo, você envenenou o ar do galpão com Ricina.");
        cena.linha(("FIDALGO: — O que o senhor fez com o ar, " + nome + "? " + por(antecedente,
                "",
                "Isso é trabalho de quem lidou com química a vida inteira.",
                "E essa máscara... o senhor planejou isso desde o início.")).stripTrailing());
        cena.linha("Você caminhou calmamente por entre os corpos intactos dos capangas e do Fidalgo.");
        switch (Origem.de(antecedente)) {
            case INDUSTRIAL:
                cena.linha("Era o método frio das linhas de produção: cada etapa no seu tempo, nenhuma emoção. A fábrica ensinou você a tratar o perigo como um processo, e você o levou até o fim.");
                break;
            case PRECAVIDO:
                cena.linha("Máscara no rosto, respiração controlada, cada passo contado. Todas as precauções de uma vida inteira finalmente serviram para alguma coisa.");
                break;
            default:
        }
        cena.linha("Recuperou o seu dinheiro e fugiu na calada da noite. Frio e calculista.");
        cena.linha(LINHA + "\n");
        saida.exibir(cena);
    }

    public void mostrarFimRotaBSucesso() {
        Cena cena = new Cena("fim");
        cena.linha("\n[FIM DA ROTA B (SUCESSO): Fuga perfeita. Você usou os seus itens com maestria!]");
        saida.exibir(cena);
    }

    public void mostrarFimRotaBSobrevivencia() {
        Cena cena = new Cena("fim");
        cena.linha("\n[FIM DA ROTA B (SOBREVIVÊNCIA): O colete salvou a sua vida, mas a liberdade acabou.]");
        saida.exibir(cena);
    }

    public void mostrarFimRotaBMorte() {
        Cena cena = new Cena("fim");
        cena.linha("\n[FIM DA ROTA B (MORTE): Desastre total. Devia ter preparado uma rota de fuga segura...]");
        saida.exibir(cena);
    }

    public void mostrarFimRotaCSucesso() {
        Cena cena = new Cena("fim");
        cena.linha("\n[FIM DA ROTA C (SUCESSO): Vitória limpa. Heisenberg puro.]");
        saida.exibir(cena);
    }

    public void mostrarFimRotaCKamikaze() {
        Cena cena = new Cena("fim");
        cena.linha("\n[FIM DA ROTA C (KAMIKAZE): O último ato de química e redenção. O Cartel foi aniquilado com explosivos.]");
        saida.exibir(cena);
    }

    public void mostrarFimRotaDRendicao() {
        Cena cena = new Cena("fim");
        cena.linha("\n[FIM DA ROTA D (RENDIÇÃO): Você sacrificou o império para proteger a família.]");
        saida.exibir(cena);
    }
}
