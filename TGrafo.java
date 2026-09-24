/*
 * =====================================================================
 * ConexSom — Recomendação Musical por Similaridade entre Artistas
 * Arquivo : TGrafo.java
 *
 * Integrantes:
 *   Enrique Cipolla Martins ........ RA 10427834
 *   Henrique Ferreira Marciano ..... RA 10439797
 *   Pedro Henrique Saraiva Arruda .. RA 10437747
 *
 * Síntese:
 *   Representação do grafo por MATRIZ DE ADJACÊNCIA. Guarda o tipo do
 *   grafo (0..7), o rótulo (nome do artista) e o peso de cada vértice,
 *   e o peso de cada aresta (grau de similaridade entre dois artistas).
 *
 * Histórico de alterações (data — autor — descrição):
 *   23/09/2026 — Enrique Cipolla Martins — Versão inicial baseada na classe TGrafo de aula
 *   24/09/2026 — Enrique Cipolla Martins — insereA e removeA (TODOs 1 e 2)
 *   24/09/2026 — Henrique Ferreira Marciano — insereV, removeV, buscaPorRotulo,
 *                grau e show (TODOs 3 a 7)
 * =====================================================================
 *
 * TODO(0) IMPORTANTE: o enunciado exige que a implementação seja baseada
 *   NECESSARIAMENTE na classe apresentada em aula. Abram o TGrafo do
 *   professor e alinhem nomes de atributos/métodos (n, m, adj, insereA,
 *   removeA, show...). Este arquivo é um guia, não um substituto.
 */

import java.util.Arrays;

public class TGrafo {

    /** Valor que indica "não existe aresta" na matriz.
     *  Como o peso é uma similaridade em (0, 1], zero pode ser usado como
     *  ausência. Se algum dia um peso 0 for válido, troquem por NaN. */
    public static final float SEM_ARESTA = 0f;

    /** Acima deste número de vértices, show() usa a visão por linhas. */
    private static final int LIMITE_MATRIZ_COMPLETA = 15;

    private int tipo;               // 0..7, conforme enunciado (ConexSom = 2)
    private int n;                  // número de vértices
    private int m;                  // número de arestas
    private String[] rotulos;       // rotulos[v] = nome do artista
    private float[] pesosVertices;  // só usado nos tipos 1, 3, 5, 7
    private float[][] adj;          // adj[v][w] = peso da aresta v-w

    public TGrafo(int tipo, int n) {
        this.tipo = tipo;
        this.n = n;
        this.m = 0;
        this.rotulos = new String[n];
        this.pesosVertices = new float[n];
        this.adj = new float[n][n];   // Java já inicializa com 0 = SEM_ARESTA
    }

    // ------------------------------------------------------------------
    // Consultas simples
    // ------------------------------------------------------------------
    public int getTipo() { return tipo; }
    public int getN() { return n; }
    public int getM() { return m; }
    public String getRotulo(int v) { return rotulos[v]; }
    public float getPesoVertice(int v) { return pesosVertices[v]; }
    public float getPesoAresta(int v, int w) { return adj[v][w]; }

    public boolean isOrientado() { return tipo >= 4; }
    public boolean temPesoVertice() { return tipo == 1 || tipo == 3 || tipo == 5 || tipo == 7; }
    public boolean temPesoAresta() { return tipo == 2 || tipo == 3 || tipo == 6 || tipo == 7; }

    public boolean verticeValido(int v) { return v >= 0 && v < n; }
    public boolean existeAresta(int v, int w) { return adj[v][w] != SEM_ARESTA; }

    public void setVertice(int v, String rotulo, float peso) {
        rotulos[v] = rotulo;
        pesosVertices[v] = peso;
    }

    // ------------------------------------------------------------------
    // Operações do menu (c, d, e, f)
    // ------------------------------------------------------------------

    /**
     * Insere a aresta v-w com o peso informado.
     * Rejeita: vértice inválido, laço (artista não é similar a si mesmo),
     * aresta já existente e, nos tipos com peso na aresta, peso <= 0
     * (0 representa "sem aresta" na matriz).
     * Nos tipos sem peso na aresta, grava peso 1.
     *
     * @return true se inseriu; false caso contrário.
     */
    public boolean insereA(int v, int w, float peso) {
        // 1. Vértices válidos e sem laço
        if (!verticeValido(v) || !verticeValido(w) || v == w) {
            return false;
        }
        // 2. Peso inválido (rejeita também NaN)
        if (temPesoAresta() && !(peso > 0f)) {
            return false;
        }
        // 3. Aresta já existente: não conta m duas vezes
        if (existeAresta(v, w)) {
            return false;
        }
        // 4. Grafo sem peso na aresta: usa peso 1
        if (!temPesoAresta()) {
            peso = 1f;
        }
        // 5. Grava na matriz (nos dois sentidos se não orientado)
        adj[v][w] = peso;
        if (!isOrientado()) {
            adj[w][v] = peso;
        }
        // 6. m conta a aresta uma única vez
        m++;
        return true;
    }

    /**
     * Remove a aresta v-w (nos dois sentidos se o grafo não é orientado).
     *
     * @return true se removeu; false se vértice inválido ou aresta inexistente.
     */
    public boolean removeA(int v, int w) {
        // 1. Vértices válidos e aresta existente
        if (!verticeValido(v) || !verticeValido(w) || !existeAresta(v, w)) {
            return false;
        }
        // 2. Apaga da matriz
        adj[v][w] = SEM_ARESTA;
        if (!isOrientado()) {
            adj[w][v] = SEM_ARESTA;
        }
        // 3. m diminui uma única vez
        m--;
        return true;
    }

    /**
     * Insere um novo vértice (artista) sem arestas e devolve o índice dele,
     * que é sempre o n antigo (o novo vértice entra no fim).
     * A matriz tem tamanho fixo, então é realocada com uma linha e uma
     * coluna a mais.
     *
     * @return índice do novo vértice; -1 se o rótulo é vazio ou já existe.
     */
    public int insereV(String rotulo, float peso) {
        // 1. Rótulo obrigatório e sem artista duplicado
        if (rotulo == null || rotulo.trim().isEmpty() || buscaPorRotulo(rotulo) != -1) {
            return -1;
        }
        // 2. Nova matriz (n+1)x(n+1) copiando a antiga; o resto fica 0 = SEM_ARESTA
        float[][] novaAdj = new float[n + 1][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(adj[i], 0, novaAdj[i], 0, n);
        }
        // 3. Vetores de rótulo e peso com uma posição a mais
        rotulos = Arrays.copyOf(rotulos, n + 1);
        pesosVertices = Arrays.copyOf(pesosVertices, n + 1);

        // 4. Dados do novo vértice na última posição
        adj = novaAdj;
        rotulos[n] = rotulo.trim();
        pesosVertices[n] = temPesoVertice() ? peso : 0f;
        n++;
        return n - 1;
    }

    /**
     * Remove o vértice v e TODAS as arestas incidentes a ele.
     * Os vértices com índice > v são "puxados" uma posição para trás,
     * então a numeração muda — isso aparece no grafo.txt gravado.
     *
     * @return true se removeu; false se v é inválido.
     */
    public boolean removeV(int v) {
        // 1. Validação
        if (!verticeValido(v)) {
            return false;
        }
        // 2. Desconta de m as arestas incidentes em v.
        //    Não orientado: basta a linha v (a coluna é espelho dela).
        //    Orientado: linha v (saída) + coluna v (entrada). Não há laço
        //    (insereA rejeita), então nenhuma aresta é contada duas vezes.
        int removidas = 0;
        for (int w = 0; w < n; w++) {
            if (adj[v][w] != SEM_ARESTA) removidas++;
            if (isOrientado() && w != v && adj[w][v] != SEM_ARESTA) removidas++;
        }
        m -= removidas;

        // 3. Nova matriz (n-1)x(n-1) pulando a linha v e a coluna v
        float[][] novaAdj = new float[n - 1][n - 1];
        for (int i = 0, ni = 0; i < n; i++) {
            if (i == v) continue;
            for (int j = 0, nj = 0; j < n; j++) {
                if (j == v) continue;
                novaAdj[ni][nj] = adj[i][j];
                nj++;
            }
            ni++;
        }

        // 4. Desloca rótulos e pesos uma posição para trás a partir de v
        String[] novosRotulos = new String[n - 1];
        float[] novosPesos = new float[n - 1];
        for (int i = 0, ni = 0; i < n; i++) {
            if (i == v) continue;
            novosRotulos[ni] = rotulos[i];
            novosPesos[ni] = pesosVertices[i];
            ni++;
        }

        // 5. Troca as estruturas e atualiza n
        adj = novaAdj;
        rotulos = novosRotulos;
        pesosVertices = novosPesos;
        n--;
        return true;
    }

    /**
     * Devolve o índice do artista com esse nome (ignora maiúsculas/minúsculas
     * e espaços nas pontas) ou -1 se não existir.
     */
    public int buscaPorRotulo(String rotulo) {
        if (rotulo == null) return -1;
        String alvo = rotulo.trim();
        for (int v = 0; v < n; v++) {
            if (rotulos[v] != null && rotulos[v].equalsIgnoreCase(alvo)) {
                return v;
            }
        }
        return -1;
    }

    /**
     * Grau do vértice v.
     * Não orientado: nº de artistas similares a v.
     * Orientado: grau total = grau de saída + grau de entrada.
     *
     * @return o grau, ou -1 se v é inválido.
     */
    public int grau(int v) {
        if (!verticeValido(v)) return -1;
        int g = 0;
        for (int w = 0; w < n; w++) {
            if (adj[v][w] != SEM_ARESTA) g++;
            if (isOrientado() && adj[w][v] != SEM_ARESTA) g++;
        }
        return g;
    }

    /** Grau médio do grafo (0 se não há vértices). */
    public double grauMedio() {
        if (n == 0) return 0;
        double soma = 0;
        for (int v = 0; v < n; v++) soma += grau(v);
        return soma / n;
    }

    /**
     * Opção h) do menu — mostra o grafo como MATRIZ de adjacência.
     * Com até LIMITE_MATRIZ_COMPLETA vértices imprime a matriz inteira;
     * acima disso a matriz não cabe no terminal, então imprime cada LINHA
     * da matriz listando só as posições não nulas ("visão de lista" da
     * mesma matriz — a estrutura em memória continua sendo a matriz).
     */
    public void show() {
        System.out.println("Tipo " + tipo + " | n = " + n + " vértices | m = " + m + " arestas");
        if (n == 0) {
            System.out.println("(grafo vazio)");
            return;
        }
        if (n <= LIMITE_MATRIZ_COMPLETA) {
            mostrarMatrizCompleta();
        } else {
            mostrarLinhasDaMatriz();
        }
    }

    /** Matriz n x n; "-" indica ausência de aresta. */
    private void mostrarMatrizCompleta() {
        System.out.print("      ");
        for (int w = 0; w < n; w++) System.out.printf("%6d", w);
        System.out.println();
        for (int v = 0; v < n; v++) {
            System.out.printf("%4d |", v);
            for (int w = 0; w < n; w++) {
                if (adj[v][w] == SEM_ARESTA) {
                    System.out.printf("%6s", "-");
                } else if (temPesoAresta()) {
                    System.out.printf("%6.2f", adj[v][w]);
                } else {
                    System.out.printf("%6d", 1);
                }
            }
            System.out.println("   " + rotulos[v]);
        }
    }

    /** Uma linha por vértice: v (rótulo): w1(peso) w2(peso) ... */
    private void mostrarLinhasDaMatriz() {
        System.out.println("(n > " + LIMITE_MATRIZ_COMPLETA
                + ": exibindo as posições não nulas de cada linha da matriz)");
        for (int v = 0; v < n; v++) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%4d (%s):", v, rotulos[v]));
            boolean vazia = true;
            for (int w = 0; w < n; w++) {
                if (adj[v][w] == SEM_ARESTA) continue;
                vazia = false;
                sb.append(' ').append(w);
                if (temPesoAresta()) sb.append(String.format("(%.2f)", adj[v][w]));
            }
            if (vazia) sb.append(" (sem arestas)");
            System.out.println(sb);
        }
    }
}
