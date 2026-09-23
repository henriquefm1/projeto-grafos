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
        int[] comp = new int[g.getN()];
        // TODO(13)
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
        // TODO(14)
        return 0;
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
        int[] comp = new int[g.getN()];
        // TODO(15)
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
        // TODO(16)
        return new TGrafo(4, 0);
    }
}
