/*
 * =====================================================================
 * ConexSom — Recomendação Musical por Similaridade entre Artistas
 * Arquivo : Conexidade.java
 *
 * Integrantes:
 *   Enrique Cipolla Martins ........ RA 10427834
 *   Henrique Ferreira Marciano ..... RA 10439797
 *   Pedro Henrique Saraiva Arruda .. RA 10437747
 *
 * Síntese:
 *   Opção i) do menu. Grafo não orientado: conexo/desconexo (+ componentes).
 *   Grafo orientado: categoria C0..C3 e grafo reduzido via FCONEX.
 *   O ConexSom é tipo 2 (não orientado), mas a parte orientada é exigida
 *   pelo enunciado para a aplicação funcionar com qualquer grafo.txt.
 *
 * Histórico de alterações (data — autor — descrição):
 *   dd/mm/aaaa — Fulano — Versão inicial
 * =====================================================================
 */

public class Conexidade {

    /** Ponto de entrada da opção i) (já pronto — chama os métodos abaixo). */
    public static void apresentar(TGrafo g) {
        if (!g.isOrientado()) {
            int[] comp = componentesConexas(g);
            int qtd = contarComponentes(comp);
            System.out.println(qtd == 1 ? "Grafo CONEXO." : "Grafo DESCONEXO (" + qtd + " componentes).");
            if (qtd> 1) {
                listarComponentes(g, comp, qtd);
            }
            // TODO(12) Se desconexo, listar os artistas de cada componente.
            //   No relatório isso vira análise: "ilhas" de artistas sem
            //   ligação com o resto => a recomendação não consegue sair delas.
        } else {
            int c = categoria(g);
            System.out.println("Categoria de conexidade: C" + c);
            int[] comp = fconex(g);
            TGrafo reduzido = grafoReduzido(g, comp);
            System.out.println("Grafo reduzido (" + reduzido.getN() + " vértices):");
            reduzido.show();
        }
    }
    private static void listarComponentes(TGrafo g, int[] comp, int qtd) {
        for (int c = 0; c < qtd; c++) {
            StringBuilder sb = new StringBuilder("  Componente " + c + ": ");
            boolean primeiro = true;
            for (int v = 0; v < comp.length; v++) {
                if (comp[v] == c) {
                    if (!primeiro) sb.append(", ");
                    sb.append(g.getRotulo(v));
                    primeiro = false;
                }
            }
            System.out.println(sb);
        }
    }

    /**
     * Grafo NÃO orientado: devolve comp[v] = id da componente de v (0, 1, 2...).
     *
     * TODO(13) Implementar com busca em largura (fila) ou profundidade:
     *   - comp[] começa com -1.
     *   - Para cada v com comp[v] == -1, fazer BFS/DFS a partir de v
     *     marcando todos os alcançados com o id atual; id++.
     *   Conexo <=> só existe a componente 0.
     */
    public static int[] componentesConexas(TGrafo g) {
        int n = g.getN();
        int[] comp = new int[n];
        java.util.Arrays.fill(comp, -1); // -1 = ainda não visitado

        int id = 0;
        for (int inicio = 0; inicio < n; inicio++) {
            if (comp[inicio] != -1) {
                continue; // já pertence a uma componente encontrada antes
            }
            java.util.Queue<Integer> fila = new java.util.LinkedList<>();
            fila.add(inicio);
            comp[inicio] = id;
            while (!fila.isEmpty()) {
                int v = fila.poll();
                for (int w = 0; w < n; w++) {
                    if (g.existeAresta(v, w) && comp[w] == -1) {
                    comp[w] = id;
                        fila.add(w);
                    }
                }   
            }
            id++; // essa componente acabou; a próxima leva o próximo id
        }
        return comp;
    }

    public static int contarComponentes(int[] comp) {
        int max = -1;
        for (int c : comp) max = Math.max(max, c);
        return max + 1;
    }

    /**
     * Grafo ORIENTADO: categoria de conexidade.
     *   C3 — fortemente conexo (todo par se alcança nos dois sentidos)
     *   C2 — semi-fortemente conexo (todo par se alcança em pelo menos um sentido)
     *   C1 — simplesmente conexo (o grafo subjacente não orientado é conexo)
     *   C0 — desconexo
     *
     * TODO(14) Implementar seguindo a definição vista em aula. Uma forma:
     *   1. Calcular a matriz de alcançabilidade (BFS de cada vértice, ou
     *      Warshall) — alc[v][w] = true se existe caminho v -> w.
     *   2. Todos os pares com alc[v][w] && alc[w][v]  => C3.
     *   3. Todos os pares com alc[v][w] || alc[w][v]  => C2.
     *   4. Ignorando o sentido das arestas, conexo     => C1.
     *   5. Senão                                      => C0.
     */
    public static int categoria(TGrafo g) {
        int n = g.getN();
        boolean[][] alc = alcancabilidade(g);

        boolean fortementeConexo = true;
        boolean semiFortementeConexo = true;
        for (int v = 0; v < n; v++) {
            for (int w = v + 1; w < n; w++) {
                boolean vAlcancaW = alc[v][w];
                boolean wAlcancaV = alc[w][v];
                if (!(vAlcancaW && wAlcancaV)) {
                    fortementeConexo = false;
                }
                if (!(vAlcancaW || wAlcancaV)) {
                    semiFortementeConexo = false;
                }
            }
        }
        if (fortementeConexo) return 3;
        if (semiFortementeConexo) return 2;
        if (conexoIgnorandoDirecao(g)) return 1;
        return 0;
    }

    /** alc[v][w] == true se existe caminho orientado de v até w. */
    private static boolean[][] alcancabilidade(TGrafo g) {
        int n = g.getN();
        boolean[][] alc = new boolean[n][n];
        for (int origem = 0; origem < n; origem++) {
            boolean[] visitado = new boolean[n];
            java.util.Queue<Integer> fila = new java.util.LinkedList<>();
            fila.add(origem);
            visitado[origem] = true;
            while (!fila.isEmpty()) {
                int v = fila.poll();
                for (int w = 0; w < n; w++) {
                    if (g.existeAresta(v, w) && !visitado[w]) {
                        visitado[w] = true;
                        fila.add(w);
                    }
                }
            }
            alc[origem] = visitado;
        }
        return alc;
    }

/** Conexo tratando cada aresta como se fosse nos dois sentidos (só para C1/C0). */
private static boolean conexoIgnorandoDirecao(TGrafo g) {
    int n = g.getN();
    if (n == 0) return true;
    boolean[] visitado = new boolean[n];
    java.util.Queue<Integer> fila = new java.util.LinkedList<>();
    fila.add(0);
    visitado[0] = true;
    int contador = 1;
    while (!fila.isEmpty()) {
        int v = fila.poll();
        for (int w = 0; w < n; w++) {
            if (!visitado[w] && (g.existeAresta(v, w) || g.existeAresta(w, v))) {
                visitado[w] = true;
                contador++;
                fila.add(w);
            }
        }
    }
    return contador == n;
}

    /**
     * FCONEX — componentes fortemente conexas. comp[v] = id da componente.
     *
     * TODO(15) Implementar o FCONEX como apresentado em aula:
     *   enquanto houver vértice v sem componente:
     *     R+ = fecho transitivo direto de v   (quem v alcança)
     *     R- = fecho transitivo inverso de v  (quem alcança v)
     *     componente de v = R+ ∩ R-  (entre os vértices ainda não marcados)
     */
    public static int[] fconex(TGrafo g) {
        int n = g.getN();
        boolean[][] alc = alcancabilidade(g);
        int[] comp = new int[n];
        java.util.Arrays.fill(comp, -1);

        int id = 0;
        for (int v = 0; v < n; v++) {
            if (comp[v] != -1) {
                continue;
            }
            for (int w = 0; w < n; w++) {
                if (comp[w] == -1 && alc[v][w] && alc[w][v]) {
                    comp[w] = id;
                }
            }
            id++;
        }
        return comp;
    }

    /**
     * Grafo reduzido: cada componente vira um vértice; existe aresta Ci -> Cj
     * se existe alguma aresta de um vértice de Ci para um vértice de Cj (i != j).
     *
     * TODO(16) Implementar:
     *   1. k = contarComponentes(comp); new TGrafo(4, k) (orientado sem peso).
     *   2. Rótulo de cada vértice: "C0", "C1"... (ou lista de artistas).
     *   3. Para cada aresta v->w com comp[v] != comp[w]: insereA(comp[v], comp[w], 1).
     */
    public static TGrafo grafoReduzido(TGrafo g, int[] comp) {
        int k = contarComponentes(comp);
        TGrafo reduzido = new TGrafo(4, k); // tipo 4: orientado, sem peso

        for (int c = 0; c < k; c++) {
            reduzido.setVertice(c, "C" + c, 0f);
        }

        int n = g.getN();
        for (int v = 0; v < n; v++) {
            for (int w = 0; w < n; w++) {
                if (g.existeAresta(v, w) && comp[v] != comp[w]) {
                    reduzido.insereA(comp[v], comp[w], 1f);
                }
            }
        }
        return reduzido;
    }
}
