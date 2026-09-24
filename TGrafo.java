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
 *   dd/mm/aaaa — Fulano — Versão inicial baseada na classe TGrafo de aula
 *   dd/mm/aaaa — Fulano — ...
 * =====================================================================
 *
 * TODO(0) IMPORTANTE: o enunciado exige que a implementação seja baseada
 *   NECESSARIAMENTE na classe apresentada em aula. Abram o TGrafo do
 *   professor e alinhem nomes de atributos/métodos (n, m, adj, insereA,
 *   removeA, show...). Este arquivo é um guia, não um substituto.
 */

public class TGrafo {

    /** Valor que indica "não existe aresta" na matriz.
     *  Como o peso é uma similaridade em (0, 1], zero pode ser usado como
     *  ausência. Se algum dia um peso 0 for válido, troquem por NaN. */
    public static final float SEM_ARESTA = 0f;

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
    // Consultas simples (já prontas)
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
     * @return true se inseriu; false se inválida ou já existente.
     *
     * TODO(1) Implementar:
     *   1. Validar v e w (verticeValido) e rejeitar laço (v == w) — um
     *      artista não é "similar a si mesmo" no nosso modelo.
     *   2. Se a aresta já existe, retornar false (não contar m duas vezes).
     *   3. Se o grafo NÃO tem peso na aresta, usar peso = 1.
     *   4. adj[v][w] = peso; se NÃO orientado, também adj[w][v] = peso.
     *   5. m++ (uma única vez, mesmo no não orientado).
     */
    public boolean insereA(int v, int w, float peso) {
    // 1. Vértices válidos e sem laço (artista não é similar a si mesmo)
    if (!verticeValido(v) || !verticeValido(w) || v == w) {
        return false;
    }
    // 2. Peso inválido: 0 significa "sem aresta" na matriz (rejeita também NaN)
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

    public boolean removeA(int v, int w) {
    // 1. Vértices válidos e aresta existente
    if (!verticeValido(v) || !verticeValido(w) || !existeAresta(v, w)) {
        return false;
    }
    // 2. Apaga da matriz (nos dois sentidos se não orientado)
    adj[v][w] = SEM_ARESTA;
    if (!isOrientado()) {
        adj[w][v] = SEM_ARESTA;
    }
    // 3. m diminui uma única vez
    m--;
    return true;
}

    /**
     * Insere um novo vértice (artista) e devolve o índice dele (= n antigo).
     *
     * TODO(3) Implementar (a matriz tem tamanho fixo, então é preciso realocar):
     *   1. Criar novaAdj[n+1][n+1] e copiar adj para ela.
     *   2. Criar novos vetores rotulos/pesosVertices de tamanho n+1 e copiar
     *      (dica: java.util.Arrays.copyOf).
     *   3. Guardar rotulo/peso na posição n.
     *   4. n++ e devolver o índice do novo vértice.
     *   5. (Opcional) Impedir artista duplicado usando buscaPorRotulo.
     */
    public int insereV(String rotulo, float peso) {
        // TODO(3)
        return -1;
    }

    /**
     * Remove o vértice v e TODAS as arestas incidentes a ele.
     * Os vértices com índice > v são "puxados" uma posição para trás,
     * então a numeração muda — isso tem de aparecer no grafo.txt gravado.
     *
     * TODO(4) Implementar:
     *   1. Validar v.
     *   2. Descontar de m as arestas incidentes em v
     *      (não orientado: conta a linha v; orientado: linha v + coluna v,
     *       cuidado para não contar duas vezes).
     *   3. Montar novaAdj[n-1][n-1] pulando a linha v e a coluna v.
     *   4. Remover rotulos[v] e pesosVertices[v] deslocando o restante.
     *   5. n--.
     *   Testem: remover o primeiro, um do meio e o último vértice.
     */
    public boolean removeV(int v) {
        // TODO(4)
        return false;
    }

    /**
     * Devolve o índice do artista com esse nome (ignorando maiúsculas) ou -1.
     * TODO(5) Implementar — facilita muito o menu (usuário digita nome,
     *   não número) e será essencial na recomendação da próxima etapa.
     */
    public int buscaPorRotulo(String rotulo) {
        // TODO(5)
        return -1;
    }

    /**
     * Grau do vértice v (nº de artistas similares a ele).
     * TODO(6) Implementar. Útil no relatório: grau médio, artista mais
     *   conectado, artistas isolados (grau 0) que indicam limiar alto demais.
     */
    public int grau(int v) {
        // TODO(6)
        return 0;
    }

    /**
     * Opção h) do menu — mostra o grafo como MATRIZ de adjacência.
     *
     * TODO(7) Implementar:
     *   - Cabeçalho com tipo, n e m.
     *   - Com ~80+ vértices a matriz inteira fica ilegível no terminal.
     *     Sugestão: imprimir a matriz completa só se n <= 15 e, acima disso,
     *     imprimir por linha: "v (rótulo): w1(peso) w2(peso) ...",
     *     que é a "visão de lista" da mesma matriz. Expliquem isso no relatório.
     *   - Formatar pesos com 2 casas: String.format("%.2f", peso).
     */
    public void show() {
        // TODO(7)
        System.out.println("[TODO] show() ainda não implementado.");
    }
}
