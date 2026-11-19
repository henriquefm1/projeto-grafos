// =====================================================================
// ConexSom — Recomendação Musical por Similaridade entre Artistas
// Arquivo : ConexSom.java
//
// Integrantes:
//   Enrique Cipolla Martins ........ RA 10427834
//   Henrique Ferreira Marciano ..... RA 10439797
//   Pedro Henrique Saraiva Arruda .. RA 10437747
//
// Síntese:
//   Classe principal: interface no terminal e menu de opções. Mantém um único
//   grafo em memória e delega as operações a TGrafo, ArquivoGrafo, Conexidade,
//   Recomendacao (opção 1) e Caracteristicas (opção 2).
//
// Histórico de alterações (data — autor — descrição):
//   19/11/2026 — Henrique Ferreira Marciano — Parte 3: opção g) mostra o arquivo gravado no disco (não depende mais de grafo em memória)
//   19/11/2026 — Henrique Ferreira Marciano — Parte 3: opções 1) Investigar solução (recomendação) e 2) Características do grafo no menu
// =====================================================================

import java.util.Scanner;

public class ConexSom {

    private static final String ARQUIVO = "grafo.txt";
    private static TGrafo grafo = null;          // grafo atualmente em memória
    private static final Scanner in = new Scanner(System.in, "UTF-8");

    public static void main(String[] args) {
        char opcao;
        do {
            mostrarMenu();
            String linha = in.nextLine().trim().toLowerCase();
            opcao = linha.isEmpty() ? ' ' : linha.charAt(0);
            System.out.println();

            try {
                switch (opcao) {
                    case 'a': lerArquivo(); break;
                    case 'b': gravarArquivo(); break;
                    case 'c': inserirVertice(); break;
                    case 'd': inserirAresta(); break;
                    case 'e': removerVertice(); break;
                    case 'f': removerAresta(); break;
                    case 'g': ArquivoGrafo.mostrarConteudoArquivo(ARQUIVO, grafo); break;
                    case 'h': if (temGrafo()) grafo.show(); break;
                    case 'i': if (temGrafo()) Conexidade.apresentar(grafo); break;
                    case '1': if (temGrafo()) investigarSolucaoProblema(); break;
                    case '2': if (temGrafo()) analisarCaracteristicasGrafo(); break;
                    case 'j': System.out.println("Encerrando o ConexSom. Até mais!"); break;
                    default:  System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                // nenhuma entrada errada deve derrubar o programa.
                System.out.println("Erro: " + e.getMessage());
            }
        } while (opcao != 'j');
    }

    // o enunciado exige um TÍTULO coerente com o problema acima do menu.
    private static void mostrarMenu() {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println("   ConexSom — Recomendação Musical por Similaridade de Artistas");
        System.out.println("==============================================================");
        System.out.println(" a) Ler dados do arquivo grafo.txt");
        System.out.println(" b) Gravar dados no arquivo grafo.txt");
        System.out.println(" c) Inserir vértice (artista)");
        System.out.println(" d) Inserir aresta (similaridade)");
        System.out.println(" e) Remover vértice (artista)");
        System.out.println(" f) Remover aresta (similaridade)");
        System.out.println(" g) Mostrar conteúdo do arquivo grafo.txt (como está gravado)");
        System.out.println(" h) Mostrar grafo (matriz de adjacência)");
        System.out.println(" i) Apresentar a conexidade do grafo e o reduzido");
        System.out.println(" 1) Investigar solução: recomendar artistas a partir de um artista");
        System.out.println(" 2) Descobrir características do grafo (graus, coloração, Euler, Hamilton)");
        System.out.println(" j) Encerrar a aplicação");
        System.out.print("Escolha: ");
    }

    // ------------------------------------------------------------------
    // ações do menu
    // ------------------------------------------------------------------

    private static void lerArquivo() throws Exception {
        grafo = ArquivoGrafo.ler(ARQUIVO);
        System.out.printf("Grafo carregado: %d artistas, %d similaridades.%n",
                grafo.getN(), grafo.getM());
    }

    private static void gravarArquivo() throws Exception {
        if (!temGrafo()) return;
        ArquivoGrafo.gravar(grafo, ARQUIVO);
        System.out.printf("Grafo gravado em %s (%d artistas, %d similaridades).%n",
                ARQUIVO, grafo.getN(), grafo.getM());
    }

    private static void inserirVertice() {
        if (!temGrafo()) return;
        String nome = lerTexto("Nome do artista: ");
        float peso = 0f;
        if (grafo.temPesoVertice()) peso = lerFloat("Peso do vértice: ");
        int v = grafo.insereV(nome, peso);
        System.out.println(v >= 0 ? "Artista inserido com índice " + v + "."
                                  : "Não foi possível inserir o artista.");
    }

    private static void inserirAresta() {
        if (!temGrafo()) return;
        int v = lerVertice("Artista 1 (índice ou nome): ");
        int w = lerVertice("Artista 2 (índice ou nome): ");
        float peso = 1f;
        if (grafo.temPesoAresta()) {
            peso = lerFloat("Similaridade (0 a 1): ");
            // no modelo ConexSom a similaridade fica em (0, 1]
            if (!(peso > 0f && peso <= 1f)) {
                System.out.println("Similaridade inválida: use um valor maior que 0 e até 1.");
                return;
            }
        }
        System.out.println(grafo.insereA(v, w, peso) ? "Aresta inserida."
                : "Não foi possível inserir (vértice inválido, laço ou aresta já existente).");
    }

    private static void removerVertice() {
        if (!temGrafo()) return;
        int v = lerVertice("Artista a remover (índice ou nome): ");
        if (!grafo.verticeValido(v)) {
            System.out.println("Vértice inválido.");
            return;
        }
        String nome = grafo.getRotulo(v);
        int ultimo = grafo.getN() - 1;
        grafo.removeV(v);
        System.out.println("Artista \"" + nome + "\" removido (e suas arestas).");
        if (v < ultimo) {
            // a matriz é compactada, então os vértices depois de v andam uma posição
            System.out.println("Atenção: os artistas de índice " + (v + 1) + " a " + ultimo
                    + " passaram a ter o índice anterior (" + v + " a " + (ultimo - 1) + ").");
        }
    }

    private static void removerAresta() {
        if (!temGrafo()) return;
        int v = lerVertice("Artista 1 (índice ou nome): ");
        int w = lerVertice("Artista 2 (índice ou nome): ");
        System.out.println(grafo.removeA(v, w) ? "Aresta removida." : "Aresta inexistente.");
    }

    // opção 1) — item 1 da Parte 3: recomendação por similaridade
    // (vizinhos mais similares, descobertas por vizinhos de vizinhos e caminho de maior similaridade).
    private static void investigarSolucaoProblema() {
        System.out.println("--- Investigação de solução: recomendação de artistas ---");
        Recomendacao.apresentar(grafo, in);
    }

    // opção 2) — item 2 da Parte 3: graus, coloração, Euler e Hamilton.
    private static void analisarCaracteristicasGrafo() {
        System.out.println("--- Características do grafo ---");
        Caracteristicas.apresentar(grafo);
    }

    // ------------------------------------------------------------------
    // leitura de entrada do usuário
    // ------------------------------------------------------------------

    private static boolean temGrafo() {
        if (grafo == null) {
            System.out.println("Nenhum grafo em memória. Use a opção a) primeiro.");
            return false;
        }
        return true;
    }

    private static String lerTexto(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    private static float lerFloat(String msg) {
        return ArquivoGrafo.parseFloat(lerTexto(msg));
    }

    // aceita número do vértice OU nome do artista.
    private static int lerVertice(String msg) {
        String s = lerTexto(msg);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return grafo.buscaPorRotulo(s);
        }
    }
}
