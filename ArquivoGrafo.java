/*
 * =====================================================================
 * ConexSom — Recomendação Musical por Similaridade entre Artistas
 * Arquivo : ArquivoGrafo.java
 *
 * Integrantes:
 *   Enrique Cipolla Martins ........ RA 10427834
 *   Henrique Ferreira Marciano ..... RA 10439797
 *   Pedro Henrique Saraiva Arruda .. RA 10437747
 *
 * Síntese:
 *   Leitura (opção a), gravação (opção b) e exibição formatada (opção g)
 *   do arquivo grafo.txt, no formato do enunciado:
 *
 *     tipo
 *     n
 *     0 "Nome do artista 0" [peso]      <- peso só nos tipos 1,3,5,7
 *     ...
 *     m
 *     v w [peso]                        <- peso só nos tipos 2,3,6,7
 *     ...
 *
 * Histórico de alterações (data — autor — descrição):
 *   23/09/2026 — Enrique Cipolla Martins — Versão inicial (leitura)
 *   24/09/2026 — Henrique Ferreira Marciano — Validações da leitura, gravação
 *                e exibição formatada (TODOs 8 a 11)
 * =====================================================================
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArquivoGrafo {

    /** Captura: índice, "rótulo entre aspas" e um peso opcional (com ou sem aspas). */
    private static final Pattern LINHA_VERTICE =
            Pattern.compile("^(\\d+)\\s+\"([^\"]*)\"(?:\\s+\"?([-\\d.,]+)\"?)?\\s*$");

    /**
     * Opção a) — lê o grafo.txt e monta o TGrafo em memória.
     *
     * Convenção do ConexSom: no grafo NÃO orientado cada aresta aparece
     * UMA única vez no arquivo (v w peso) e m é o número real de arestas.
     * Linhas repetidas (inclusive "w v" depois de "v w") ou inválidas são
     * ignoradas com aviso, e o total efetivamente carregado é informado.
     *
     * @throws IOException com mensagem clara se o arquivo estiver mal formatado.
     */
    public static TGrafo ler(String caminho) throws IOException {
        List<String> linhas = lerLinhasUteis(caminho);
        int[] pos = {0};   // posição atual (vetor para poder avançar dentro de proximaLinha)

        int tipo = lerInteiro(proximaLinha(linhas, pos, "tipo do grafo"), "tipo do grafo");
        if (tipo < 0 || tipo > 7) {
            throw new IOException("Tipo do grafo inválido: " + tipo + " (esperado de 0 a 7).");
        }
        int n = lerInteiro(proximaLinha(linhas, pos, "número de vértices"), "número de vértices");
        if (n < 0) throw new IOException("Número de vértices negativo: " + n);
        TGrafo g = new TGrafo(tipo, n);

        // ---- vértices ----
        for (int i = 0; i < n; i++) {
            String linha = proximaLinha(linhas, pos, "vértice " + i);
            Matcher mt = LINHA_VERTICE.matcher(linha.trim());
            if (!mt.matches()) {
                throw new IOException("Linha de vértice inválida: " + linha);
            }
            int v = Integer.parseInt(mt.group(1));
            if (v != i) {
                throw new IOException("Vértices devem estar numerados de 0 a " + (n - 1)
                        + " em ordem; esperado " + i + ", encontrado " + v + ".");
            }
            String rotulo = mt.group(2);
            float peso = (mt.group(3) != null) ? parseFloat(mt.group(3)) : 0f;
            g.setVertice(v, rotulo, peso);
        }

        // ---- arestas ----
        int m = lerInteiro(proximaLinha(linhas, pos, "número de arestas"), "número de arestas");
        int ignoradas = 0;
        for (int i = 0; i < m; i++) {
            String linha = proximaLinha(linhas, pos, "aresta " + i);
            String[] partes = linha.trim().split("\\s+");
            if (partes.length < 2) {
                throw new IOException("Linha de aresta inválida: " + linha);
            }
            int v = lerInteiro(partes[0], "vértice de origem");
            int w = lerInteiro(partes[1], "vértice de destino");
            float peso = (partes.length >= 3) ? parseFloat(partes[2]) : 1f;

            if (!g.insereA(v, w, peso)) {
                System.out.println("  Aviso: aresta ignorada (inválida ou repetida): " + linha.trim());
                ignoradas++;
            }
        }
        if (ignoradas > 0) {
            System.out.println("  " + ignoradas + " aresta(s) ignorada(s); m declarado = " + m
                    + ", m carregado = " + g.getM() + ".");
        }
        return g;
    }

    /**
     * Opção b) — grava o grafo da memória no MESMO formato da leitura.
     * No não orientado grava só w > v (cada aresta uma vez); no orientado
     * percorre a matriz inteira. Pesos sempre com ponto decimal.
     */
    public static void gravar(TGrafo g, String caminho) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(caminho), StandardCharsets.UTF_8))) {
            pw.println(g.getTipo());
            pw.println(g.getN());
            for (int v = 0; v < g.getN(); v++) {
                String linha = v + " \"" + g.getRotulo(v) + "\"";
                if (g.temPesoVertice()) linha += " " + formatarPeso(g.getPesoVertice(v));
                pw.println(linha);
            }
            pw.println(g.getM());
            for (int v = 0; v < g.getN(); v++) {
                int inicio = g.isOrientado() ? 0 : v + 1;
                for (int w = inicio; w < g.getN(); w++) {
                    if (!g.existeAresta(v, w)) continue;
                    String linha = v + " " + w;
                    if (g.temPesoAresta()) linha += " " + formatarPeso(g.getPesoAresta(v, w));
                    pw.println(linha);
                }
            }
            if (pw.checkError()) {
                throw new IOException("Falha ao escrever em " + caminho);
            }
        }
    }

    /**
     * Opção g) — mostra o conteúdo do grafo de forma legível:
     * resumo (tipo por extenso, n, m, grau médio), lista de artistas com
     * grau e lista de similaridades com os nomes dos dois artistas.
     */
    public static void mostrarConteudo(TGrafo g) {
        String titulo = " ConexSom — conteúdo do grafo.txt ";
        String barra = "═".repeat(titulo.length());
        System.out.println("╔" + barra + "╗");
        System.out.println("║" + titulo + "║");
        System.out.println("╚" + barra + "╝");
        System.out.printf("Tipo %d: %s%n", g.getTipo(), descricaoTipo(g.getTipo()));
        System.out.printf("%d artistas | %d similaridades | grau médio %.2f%n",
                g.getN(), g.getM(), g.grauMedio());

        System.out.println();
        System.out.println("--- Artistas (vértices) ---");
        for (int v = 0; v < g.getN(); v++) {
            String linha = String.format("  %3d  %-30s grau %d", v, g.getRotulo(v), g.grau(v));
            if (g.temPesoVertice()) linha += String.format("   peso %.2f", g.getPesoVertice(v));
            if (g.grau(v) == 0) linha += "   (isolado)";
            System.out.println(linha);
        }

        System.out.println();
        System.out.println("--- Similaridades (arestas) ---");
        String esq = g.isOrientado() ? "--" : "<--";   // orientado: só uma ponta
        String dir = "-->";
        for (int v = 0; v < g.getN(); v++) {
            int inicio = g.isOrientado() ? 0 : v + 1;
            for (int w = inicio; w < g.getN(); w++) {
                if (!g.existeAresta(v, w)) continue;
                String meio = g.temPesoAresta()
                        ? String.format(" %s %.2f %s ", esq, g.getPesoAresta(v, w), dir)
                        : " " + esq + dir + " ";
                System.out.printf("  %-30s%s%s%n", g.getRotulo(v), meio, g.getRotulo(w));
            }
        }
    }

    public static String descricaoTipo(int tipo) {
        switch (tipo) {
            case 0: return "não orientado sem peso";
            case 1: return "não orientado com peso no vértice";
            case 2: return "não orientado com peso na aresta";
            case 3: return "não orientado com peso nos vértices e arestas";
            case 4: return "orientado sem peso";
            case 5: return "orientado com peso no vértice";
            case 6: return "orientado com peso na aresta";
            case 7: return "orientado com peso nos vértices e arestas";
            default: return "tipo desconhecido";
        }
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** Lê o arquivo em UTF-8 (acentos nos nomes!) ignorando linhas em branco. */
    private static List<String> lerLinhasUteis(String caminho) throws IOException {
        List<String> linhas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(caminho), StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (!linha.trim().isEmpty()) linhas.add(linha);
            }
        }
        return linhas;
    }

    /** Devolve a próxima linha útil ou lança erro dizendo o que faltou. */
    private static String proximaLinha(List<String> linhas, int[] pos, String oQue) throws IOException {
        if (pos[0] >= linhas.size()) {
            throw new IOException("Arquivo terminou antes do esperado (faltou: " + oQue + ").");
        }
        return linhas.get(pos[0]++);
    }

    /** Converte para int com mensagem clara em caso de erro. */
    private static int lerInteiro(String s, String oQue) throws IOException {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Valor inválido para " + oQue + ": \"" + s.trim() + "\"");
        }
    }

    /** Aceita "0.82" e "0,82". */
    static float parseFloat(String s) {
        return Float.parseFloat(s.replace(',', '.'));
    }

    /** Formata peso sempre com ponto (para gravação). */
    static String formatarPeso(float p) {
        return String.format(Locale.US, "%.4f", p);
    }
}
