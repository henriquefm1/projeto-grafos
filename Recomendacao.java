/*
 * =====================================================================
 * ConexSom — Recomendação Musical por Similaridade entre Artistas
 * Arquivo : Recomendacao.java
 *
 * Integrantes:
 *   Enrique Cipolla Martins ........ RA 10427834
 *   Henrique Ferreira Marciano ..... RA 10439797
 *   Pedro Henrique Saraiva Arruda .. RA 10437747
 *
 * Síntese:
 *   Item 1 da Parte 3 — investigação de uma solução para o problema:
 *   recomendar artistas a partir de um artista de origem.
 *
 *   Modelo: a "força" de uma recomendação é o PRODUTO das similaridades
 *   (pesos, em (0, 1]) ao longo do melhor caminho entre a origem e o
 *   artista recomendado. Maximizar o produto equivale a minimizar a
 *   soma dos custos c(e) = -ln(sim(e)), que são não negativos.
 *   Assim:
 *     - vizinho direto: força = própria similaridade;
 *     - vizinho de vizinho: força = sim(o,x) * sim(x,c), com o melhor x.
 *   Técnicas usadas: ordenação de vizinhos pelo peso (nível 1), busca em
 *   largura limitada a 2 níveis com combinação de pesos (nível 2) e
 *   algoritmo de Dijkstra com custo -ln(sim) (caminho de maior
 *   similaridade entre dois artistas).
 *
 *   Só usa métodos de TGrafo que já existem (getN, getRotulo,
 *   getPesoAresta, existeAresta), para não depender das demais tarefas.
 *
 * Histórico de alterações (data — autor — descrição):
 *   11/11/2026 — Henrique Ferreira Marciano — Versão inicial (Parte 3, item 1)
 * =====================================================================
 */

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Recomendacao {

    private static final Locale BR = new Locale("pt", "BR");

    /** Profundidade da busca de descobertas (vizinhos de vizinhos). */
    public static final int PROFUNDIDADE = 2;
    /** Quantidade de sugestões exibidas em cada lista. */
    public static final int TOP = 5;

    // ------------------------------------------------------------------
    // Resultado
    // ------------------------------------------------------------------

    /** Uma sugestão: artista, força (produto das similaridades) e caminho. */
    public static class Sugestao {
        public final int artista;
        public final double forca;
        /** Caminho origem ... artista (inclui os dois extremos). */
        public final int[] caminho;

        Sugestao(int artista, double forca, int[] caminho) {
            this.artista = artista;
            this.forca = forca;
            this.caminho = caminho;
        }

        /** true se o artista é vizinho direto da origem. */
        public boolean direta() { return caminho.length == 2; }
    }

    // ------------------------------------------------------------------
    // Algoritmos
    // ------------------------------------------------------------------

    /**
     * Vizinhos diretos de origem, do mais para o menos similar
     * (empate: ordem alfabética do nome).
     */
    public static List<Sugestao> vizinhosPorSimilaridade(TGrafo g, int origem) {
        List<Sugestao> lista = new ArrayList<>();
        for (int w = 0; w < g.getN(); w++) {
            if (w != origem && g.existeAresta(origem, w)) {
                lista.add(new Sugestao(w, g.getPesoAresta(origem, w), new int[] {origem, w}));
            }
        }
        ordenar(g, lista);
        return lista;
    }

    /**
     * Melhor força para cada artista alcançável em até {@code profundidade}
     * arestas a partir de {@code origem} (a própria origem não entra).
     * É uma busca em largura por camadas: na rodada r só se usam os valores
     * da rodada r-1, o que garante o limite de profundidade. Guardamos o
     * predecessor de cada rodada para reconstruir o caminho exato.
     */
    public static List<Sugestao> recomendar(TGrafo g, int origem, int profundidade) {
        int n = g.getN();
        double[][] forca = new double[profundidade + 1][n];
        int[][] pred = new int[profundidade + 1][n];
        for (int[] linha : pred) Arrays.fill(linha, -1);
        forca[0][origem] = 1.0;

        for (int r = 1; r <= profundidade; r++) {
            forca[r] = forca[r - 1].clone();
            for (int u = 0; u < n; u++) {
                if (forca[r - 1][u] <= 0) continue;
                for (int w = 0; w < n; w++) {
                    if (w == u || w == origem || !g.existeAresta(u, w)) continue;
                    double val = forca[r - 1][u] * g.getPesoAresta(u, w);
                    if (val > forca[r][w]) {
                        forca[r][w] = val;
                        pred[r][w] = u;
                    }
                }
            }
        }

        List<Sugestao> lista = new ArrayList<>();
        for (int v = 0; v < n; v++) {
            if (v == origem || forca[profundidade][v] <= 0) continue;
            lista.add(new Sugestao(v, forca[profundidade][v],
                    reconstruir(pred, v, origem, profundidade)));
        }
        ordenar(g, lista);
        return lista;
    }

    /**
     * Caminho de MAIOR similaridade entre a e b (Dijkstra com custo
     * -ln(similaridade)). Devolve null se b não é alcançável a partir de a.
     * Com n da ordem de 100, a versão O(n²) é suficiente.
     */
    public static Sugestao caminhoMaisSimilar(TGrafo g, int a, int b) {
        int n = g.getN();
        double[] custo = new double[n];
        int[] pred = new int[n];
        boolean[] fechado = new boolean[n];
        Arrays.fill(custo, Double.POSITIVE_INFINITY);
        Arrays.fill(pred, -1);
        custo[a] = 0;

        for (int it = 0; it < n; it++) {
            int u = -1;
            for (int v = 0; v < n; v++) {
                if (!fechado[v] && custo[v] < Double.POSITIVE_INFINITY
                        && (u == -1 || custo[v] < custo[u])) u = v;
            }
            if (u == -1) break;
            fechado[u] = true;
            for (int w = 0; w < n; w++) {
                if (w == u || fechado[w] || !g.existeAresta(u, w)) continue;
                double c = custo[u] + Math.max(0.0, -Math.log(g.getPesoAresta(u, w)));
                if (c < custo[w]) {
                    custo[w] = c;
                    pred[w] = u;
                }
            }
        }
        if (custo[b] == Double.POSITIVE_INFINITY) return null;

        ArrayList<Integer> rev = new ArrayList<>();
        for (int v = b; v != -1; v = pred[v]) rev.add(v);
        Collections.reverse(rev);
        int[] caminho = new int[rev.size()];
        for (int i = 0; i < caminho.length; i++) caminho[i] = rev.get(i);
        return new Sugestao(b, Math.exp(-custo[b]), caminho);
    }

    // ------------------------------------------------------------------
    // Busca de artista por índice ou nome (sem depender de TGrafo.buscaPorRotulo)
    // ------------------------------------------------------------------

    /**
     * Resolve o texto digitado em um vértice. Aceita índice, nome exato ou
     * parte do nome (sem diferenciar maiúsculas nem acentos).
     * @return índice; -1 se não encontrou; -2 se o texto casa com vários artistas.
     */
    public static int resolver(TGrafo g, String texto) {
        String s = texto == null ? "" : texto.trim();
        if (s.isEmpty()) return -1;
        try {
            int v = Integer.parseInt(s);
            return (v >= 0 && v < g.getN()) ? v : -1;
        } catch (NumberFormatException e) {
            // não é número: segue para a busca por nome
        }
        String alvo = normalizar(s);
        int unico = -1;
        int achados = 0;
        for (int v = 0; v < g.getN(); v++) {
            String nome = normalizar(g.getRotulo(v));
            if (nome.equals(alvo)) return v;           // nome exato vence
            if (nome.contains(alvo)) { unico = v; achados++; }
        }
        if (achados == 1) return unico;
        return achados == 0 ? -1 : -2;
    }

    // ------------------------------------------------------------------
    // Opção do menu
    // ------------------------------------------------------------------

    /** Opção k) do menu: recomendação interativa. */
    public static void apresentar(TGrafo g, Scanner in) {
        System.out.print("Artista de origem (índice ou nome): ");
        int o = resolver(g, in.nextLine());
        if (o == -1) { System.out.println("Artista não encontrado."); return; }
        if (o == -2) { System.out.println("Mais de um artista combina; digite o nome completo ou o índice."); return; }

        System.out.println();
        System.out.println("Recomendações a partir de \"" + g.getRotulo(o) + "\"");
        System.out.println("(força = produto das similaridades ao longo do caminho; 1,00 = idênticos)");

        List<Sugestao> diretos = vizinhosPorSimilaridade(g, o);
        if (diretos.isEmpty()) {
            System.out.println("Este artista não tem similares no grafo (vértice isolado).");
            return;
        }

        System.out.println();
        System.out.println("Mais similares (vizinhos diretos):");
        int i = 0;
        for (Sugestao s : diretos) {
            if (++i > TOP) break;
            System.out.printf(BR, "  %d. %-28s %.2f%n", i, g.getRotulo(s.artista), s.forca);
        }

        System.out.println();
        System.out.println("Descobertas (vizinhos de vizinhos, ainda sem ligação direta):");
        i = 0;
        for (Sugestao s : recomendar(g, o, PROFUNDIDADE)) {
            if (s.direta()) continue;
            if (++i > TOP) break;
            System.out.printf(BR, "  %d. %-28s %.2f  (via %s)%n", i, g.getRotulo(s.artista),
                    s.forca, g.getRotulo(s.caminho[1]));
        }
        if (i == 0) System.out.println("  (nenhuma: todos os alcançáveis em 2 passos já são vizinhos diretos)");

        System.out.println();
        System.out.print("Comparar com outro artista? (índice ou nome; Enter para pular): ");
        String outro = in.nextLine();
        if (outro.trim().isEmpty()) return;
        int d = resolver(g, outro);
        if (d < 0) { System.out.println("Artista não encontrado (ou ambíguo)."); return; }
        if (d == o) { System.out.println("É o mesmo artista."); return; }

        Sugestao cam = caminhoMaisSimilar(g, o, d);
        if (cam == null) {
            System.out.println("Não há caminho entre \"" + g.getRotulo(o) + "\" e \""
                    + g.getRotulo(d) + "\": estão em componentes diferentes.");
            return;
        }
        System.out.println("Caminho de maior similaridade:");
        StringBuilder sb = new StringBuilder("  " + g.getRotulo(cam.caminho[0]));
        for (int k = 1; k < cam.caminho.length; k++) {
            sb.append(String.format(BR, " —%.2f→ ", g.getPesoAresta(cam.caminho[k - 1], cam.caminho[k])));
            sb.append(g.getRotulo(cam.caminho[k]));
        }
        System.out.println(sb);
        System.out.printf(BR, "  Força total: %.4f (%d passo(s))%n", cam.forca, cam.caminho.length - 1);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static void ordenar(TGrafo g, List<Sugestao> lista) {
        lista.sort((x, y) -> {
            int c = Double.compare(y.forca, x.forca);
            return c != 0 ? c : g.getRotulo(x.artista).compareToIgnoreCase(g.getRotulo(y.artista));
        });
    }

    /** Refaz o caminho olhando, para cada vértice, a rodada em que ele foi atualizado. */
    private static int[] reconstruir(int[][] pred, int destino, int origem, int rodada) {
        ArrayList<Integer> rev = new ArrayList<>();
        int atual = destino;
        int r = rodada;
        rev.add(atual);
        while (atual != origem) {
            while (r > 0 && pred[r][atual] == -1) r--;
            if (r == 0) break;                 // não deve ocorrer
            atual = pred[r][atual];
            rev.add(atual);
            r--;
        }
        Collections.reverse(rev);
        int[] caminho = new int[rev.size()];
        for (int i = 0; i < caminho.length; i++) caminho[i] = rev.get(i);
        return caminho;
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
