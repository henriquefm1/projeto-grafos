/*
 * =====================================================================
 * ConexSom — Recomendação Musical por Similaridade entre Artistas
 * Arquivo : Caracteristicas.java
 *
 * Integrantes:
 *   Enrique Cipolla Martins ........ RA 10427834
 *   Henrique Ferreira Marciano ..... RA 10439797
 *   Pedro Henrique Saraiva Arruda .. RA 10437747
 *
 * Síntese:
 *   Item 2 da Parte 3 — características do problema modelado, obtidas com
 *   técnicas da disciplina (grafo NÃO orientado, tipo 2):
 *     1. Graus dos vértices (mín., máx., médio, densidade, isolados);
 *     2. Coloração (Welsh-Powell) com limite inferior por clique;
 *     3. Grafo euleriano / percurso euleriano (conexo + graus pares);
 *     4. Grafo hamiltoniano: condições de Dirac e Ore (suficientes) e
 *        busca exaustiva com limite de passos, que ou acha um ciclo/caminho
 *        (certificado), ou prova que não existe, ou admite não saber.
 *
 *   Só usa métodos de TGrafo que já existem (getN, getM, getRotulo,
 *   existeAresta, isOrientado), para não depender das demais tarefas.
 *
 * Histórico de alterações (data — autor — descrição):
 *   11/11/2026 — Henrique Ferreira Marciano — Versão inicial (Parte 3, item 2)
 * =====================================================================
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Caracteristicas {

    private static final Locale BR = new Locale("pt", "BR");

    /** Limite de passos da busca por ciclo/caminho hamiltoniano. */
    public static final long LIMITE_PASSOS = 1_000_000L;

    public enum Status { SIM, NAO, INDETERMINADO }

    /** Resultado da pesquisa de ciclo/caminho hamiltoniano. */
    public static class ResultadoHamilton {
        public final Status status;
        /** Certificado (sequência de vértices) quando foi encontrado; senão null. */
        public final int[] caminho;
        public final String motivo;

        ResultadoHamilton(Status status, int[] caminho, String motivo) {
            this.status = status;
            this.caminho = caminho;
            this.motivo = motivo;
        }
    }

    // ------------------------------------------------------------------
    // Opção do menu
    // ------------------------------------------------------------------

    /** Opção l) do menu: apresenta as quatro análises. */
    public static void apresentar(TGrafo g) {
        if (g.isOrientado()) {
            System.out.println("Estas análises valem para grafos NÃO orientados (o ConexSom é tipo 2).");
            return;
        }
        if (g.getN() == 0) {
            System.out.println("O grafo está vazio.");
            return;
        }
        System.out.println("=== Características do grafo ===");
        apresentarGraus(g);
        apresentarColoracao(g);
        apresentarEuler(g);
        apresentarHamilton(g);
    }

    // ------------------------------------------------------------------
    // 1. Graus
    // ------------------------------------------------------------------

    /** Grau de cada vértice (laços não existem no modelo). */
    public static int[] graus(TGrafo g) {
        int n = g.getN();
        int[] d = new int[n];
        for (int v = 0; v < n; v++) {
            for (int w = 0; w < n; w++) {
                if (w != v && g.existeAresta(v, w)) d[v]++;
            }
        }
        return d;
    }

    private static void apresentarGraus(TGrafo g) {
        int n = g.getN();
        int[] d = graus(g);
        int min = Integer.MAX_VALUE, max = 0, soma = 0, isolados = 0;
        for (int x : d) {
            min = Math.min(min, x);
            max = Math.max(max, x);
            soma += x;
            if (x == 0) isolados++;
        }
        int m = soma / 2;
        double densidade = n > 1 ? 2.0 * m / ((double) n * (n - 1)) : 0.0;

        System.out.println();
        System.out.println("1) Graus dos vértices");
        System.out.printf(BR, "   n = %d artistas, m = %d similaridades, densidade = %.4f%n", n, m, densidade);
        System.out.printf(BR, "   grau mínimo = %d | máximo = %d | médio = %.2f%n", min, max, (double) soma / n);
        System.out.println("   artistas isolados (grau 0): " + isolados);

        Integer[] ordem = ordemPorGrau(d);
        System.out.println("   mais conectados:");
        for (int i = 0; i < Math.min(5, n); i++) {
            System.out.printf("     %-28s grau %d%n", g.getRotulo(ordem[i]), d[ordem[i]]);
        }
        int[] comp = componentes(g);
        int qtd = 0;
        for (int c : comp) qtd = Math.max(qtd, c + 1);
        System.out.println("   componentes conexas: " + qtd);
    }

    // ------------------------------------------------------------------
    // 2. Coloração
    // ------------------------------------------------------------------

    /**
     * Coloração gulosa de Welsh-Powell: vértices por grau decrescente; cada
     * cor, em ordem, é atribuída a todo vértice ainda sem cor que não tenha
     * vizinho com essa cor. Devolve cor[v] (0, 1, 2, ...).
     * Serve de LIMITE SUPERIOR para o número cromático.
     */
    public static int[] colorir(TGrafo g) {
        int n = g.getN();
        Integer[] ordem = ordemPorGrau(graus(g));
        int[] cor = new int[n];
        Arrays.fill(cor, -1);
        int restantes = n;
        int atual = 0;
        while (restantes > 0) {
            for (int v : ordem) {
                if (cor[v] != -1) continue;
                boolean livre = true;
                for (int w = 0; w < n && livre; w++) {
                    if (w != v && cor[w] == atual && g.existeAresta(v, w)) livre = false;
                }
                if (livre) {
                    cor[v] = atual;
                    restantes--;
                }
            }
            atual++;
        }
        return cor;
    }

    /** true se nenhuma aresta liga dois vértices de mesma cor. */
    public static boolean coloracaoValida(TGrafo g, int[] cor) {
        for (int v = 0; v < g.getN(); v++) {
            for (int w = v + 1; w < g.getN(); w++) {
                if (g.existeAresta(v, w) && cor[v] == cor[w]) return false;
            }
        }
        return true;
    }

    /**
     * Clique encontrada de forma gulosa (a partir de cada vértice, juntando
     * os vizinhos de maior grau compatíveis). Um clique de tamanho q obriga
     * a usar pelo menos q cores: é um LIMITE INFERIOR para o número cromático.
     */
    public static int[] cliqueGulosa(TGrafo g) {
        int n = g.getN();
        Integer[] ordem = ordemPorGrau(graus(g));
        List<Integer> melhor = new ArrayList<>();
        for (int s = 0; s < n; s++) {
            List<Integer> clique = new ArrayList<>();
            clique.add(s);
            for (int c : ordem) {
                if (c == s || !g.existeAresta(s, c)) continue;
                boolean serve = true;
                for (int x : clique) {
                    if (!g.existeAresta(x, c)) { serve = false; break; }
                }
                if (serve) clique.add(c);
            }
            if (clique.size() > melhor.size()) melhor = clique;
        }
        int[] r = new int[melhor.size()];
        for (int i = 0; i < r.length; i++) r[i] = melhor.get(i);
        return r;
    }

    private static void apresentarColoracao(TGrafo g) {
        int n = g.getN();
        int[] cor = colorir(g);
        int k = 0;
        for (int c : cor) k = Math.max(k, c + 1);
        int[] clique = cliqueGulosa(g);
        int maxGrau = 0;
        for (int x : graus(g)) maxGrau = Math.max(maxGrau, x);

        System.out.println();
        System.out.println("2) Coloração (Welsh-Powell)");
        System.out.println("   cores usadas = " + k + " (coloração válida: " + (coloracaoValida(g, cor) ? "sim" : "NÃO") + ")");
        System.out.println("   clique encontrada de tamanho " + clique.length + " => número cromático ≥ " + clique.length);
        System.out.println("   limite superior: Welsh-Powell = " + k + ", Δ+1 = " + (maxGrau + 1)
                + " => número cromático ≤ " + Math.min(k, maxGrau + 1));
        if (clique.length == k) {
            System.out.println("   número cromático = " + k + " (limites coincidem)");
        } else {
            System.out.println("   número cromático entre " + clique.length + " e " + k);
        }
        for (int c = 0; c < k; c++) {
            StringBuilder sb = new StringBuilder();
            int qtd = 0;
            for (int v = 0; v < n; v++) {
                if (cor[v] == c) {
                    if (qtd++ > 0) sb.append(", ");
                    sb.append(g.getRotulo(v));
                }
            }
            System.out.println("   cor " + (c + 1) + " (" + qtd + " artistas, não são similares entre si): " + sb);
        }
    }

    // ------------------------------------------------------------------
    // 3. Euler
    // ------------------------------------------------------------------

    /** Vértices de grau ímpar. */
    public static List<Integer> verticesImpares(TGrafo g) {
        int[] d = graus(g);
        List<Integer> r = new ArrayList<>();
        for (int v = 0; v < d.length; v++) if (d[v] % 2 != 0) r.add(v);
        return r;
    }

    /** Ciclo euleriano: conexo e todos os graus pares. */
    public static boolean temCicloEuleriano(TGrafo g) {
        return conexo(g) && verticesImpares(g).isEmpty();
    }

    /** Percurso (trilha) euleriano: conexo e 0 ou 2 vértices de grau ímpar. */
    public static boolean temPercursoEuleriano(TGrafo g) {
        int impares = verticesImpares(g).size();
        return conexo(g) && (impares == 0 || impares == 2);
    }

    private static void apresentarEuler(TGrafo g) {
        List<Integer> impares = verticesImpares(g);
        boolean conexo = conexo(g);

        System.out.println();
        System.out.println("3) Euleriano");
        System.out.println("   grafo conexo: " + (conexo ? "sim" : "não")
                + " | vértices de grau ímpar: " + impares.size());
        if (!conexo && conexoSemIsolados(g)) {
            System.out.println("   (as arestas formam uma única componente; a desconexão vem só de vértices isolados)");
        }
        System.out.println("   grafo euleriano (ciclo euleriano): " + (temCicloEuleriano(g) ? "SIM" : "NÃO"));
        if (temPercursoEuleriano(g)) {
            if (impares.size() == 2) {
                System.out.println("   percurso euleriano: SIM, de \"" + g.getRotulo(impares.get(0))
                        + "\" até \"" + g.getRotulo(impares.get(1)) + "\"");
            } else {
                System.out.println("   percurso euleriano: SIM (fechado, pois todos os graus são pares)");
            }
        } else {
            System.out.println("   percurso euleriano: NÃO");
            if (conexo) {
                System.out.println("   motivo: " + impares.size() + " vértices de grau ímpar (só 0 ou 2 permitem)");
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. Hamilton
    // ------------------------------------------------------------------

    /**
     * Condição suficiente para ciclo hamiltoniano: devolve "Dirac" ou "Ore"
     * se alguma vale, ou null. Dirac: n ≥ 3 e grau(v) ≥ n/2 para todo v.
     * Ore: n ≥ 3 e grau(u)+grau(v) ≥ n para todo par não adjacente.
     */
    public static String condicaoSuficienteCiclo(TGrafo g) {
        int n = g.getN();
        if (n < 3) return null;
        int[] d = graus(g);
        boolean dirac = true;
        for (int x : d) if (2 * x < n) { dirac = false; break; }
        if (dirac) return "Dirac";
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                if (!g.existeAresta(u, v) && d[u] + d[v] < n) return null;
            }
        }
        return "Ore";
    }

    /**
     * Procura ciclo (ciclo=true) ou caminho (ciclo=false) hamiltoniano.
     * Descarta casos impossíveis por condições necessárias (desconexo, grau
     * menor que 2, folhas demais) e usa as condições de Dirac/Ore como
     * garantia. Nos demais, faz busca com retrocesso, visitando primeiro o
     * vizinho com menos opções livres (regra de Warnsdorff), com orçamento
     * de {@code limite} passos:
     *   - achou => SIM com certificado;
     *   - esgotou a busca sem achar => NAO (prova);
     *   - estourou o orçamento => INDETERMINADO.
     */
    public static ResultadoHamilton hamilton(TGrafo g, boolean ciclo, long limite) {
        int n = g.getN();
        String tipo = ciclo ? "ciclo" : "caminho";
        if (n == 0) return new ResultadoHamilton(Status.NAO, null, "grafo vazio");
        if (ciclo && n < 3) return new ResultadoHamilton(Status.NAO, null, "ciclo hamiltoniano exige n ≥ 3");
        if (n == 1) return new ResultadoHamilton(Status.SIM, new int[] {0}, "um único vértice");
        if (!conexo(g)) return new ResultadoHamilton(Status.NAO, null, "grafo desconexo");

        int[] d = graus(g);
        int folhas = 0;
        for (int v = 0; v < n; v++) {
            if (ciclo && d[v] < 2) {
                return new ResultadoHamilton(Status.NAO, null,
                        "o vértice \"" + g.getRotulo(v) + "\" tem grau " + d[v] + " (< 2)");
            }
            if (d[v] == 1) folhas++;
        }
        if (!ciclo && folhas > 2) {
            return new ResultadoHamilton(Status.NAO, null,
                    folhas + " vértices de grau 1 (um caminho hamiltoniano admite no máximo 2)");
        }

        Busca busca = new Busca(g, ciclo, limite);
        int[] achado = busca.executar();
        if (achado != null) {
            return new ResultadoHamilton(Status.SIM, achado, tipo + " encontrado pela busca");
        }
        if (busca.estourou) {
            String cond = ciclo ? condicaoSuficienteCiclo(g) : null;
            if (cond != null) {
                return new ResultadoHamilton(Status.SIM, null, "garantido pelo teorema de " + cond);
            }
            return new ResultadoHamilton(Status.INDETERMINADO, null,
                    "limite de " + limite + " passos atingido sem concluir");
        }
        return new ResultadoHamilton(Status.NAO, null, "busca exaustiva não encontrou " + tipo);
    }

    private static void apresentarHamilton(TGrafo g) {
        int n = g.getN();
        int[] d = graus(g);
        int delta = Integer.MAX_VALUE;
        for (int x : d) delta = Math.min(delta, x);

        System.out.println();
        System.out.println("4) Hamiltoniano");
        System.out.printf(BR, "   Dirac: grau mínimo = %d, n/2 = %.1f => %s%n", delta, n / 2.0,
                (n >= 3 && 2 * delta >= n) ? "satisfaz (garante ciclo)" : "não satisfaz (nada se conclui)");
        String cond = condicaoSuficienteCiclo(g);
        System.out.println("   Ore: " + ("Ore".equals(cond) || "Dirac".equals(cond)
                ? "satisfaz (garante ciclo)" : "não satisfaz (nada se conclui)"));

        imprimirHamilton(g, "ciclo hamiltoniano", hamilton(g, true, LIMITE_PASSOS));
        imprimirHamilton(g, "caminho hamiltoniano", hamilton(g, false, LIMITE_PASSOS));
    }

    private static void imprimirHamilton(TGrafo g, String nome, ResultadoHamilton r) {
        String texto;
        switch (r.status) {
            case SIM:  texto = "SIM"; break;
            case NAO:  texto = "NÃO"; break;
            default:   texto = "NÃO DETERMINADO"; break;
        }
        System.out.println("   " + nome + ": " + texto + " (" + r.motivo + ")");
        if (r.caminho != null) {
            StringBuilder sb = new StringBuilder("     ");
            for (int i = 0; i < r.caminho.length; i++) {
                if (i > 0) sb.append(" → ");
                sb.append(g.getRotulo(r.caminho[i]));
            }
            if (nome.startsWith("ciclo")) sb.append(" → ").append(g.getRotulo(r.caminho[0]));
            System.out.println(sb);
        }
    }

    /** Busca com retrocesso por ciclo/caminho hamiltoniano, com orçamento de passos. */
    private static class Busca {
        private final TGrafo g;
        private final int n;
        private final boolean ciclo;
        private final long limite;
        private final int[][] viz;
        private final int[] caminho;
        private final boolean[] usado;
        private long passos = 0;
        boolean estourou = false;

        Busca(TGrafo g, boolean ciclo, long limite) {
            this.g = g;
            this.n = g.getN();
            this.ciclo = ciclo;
            this.limite = limite;
            this.caminho = new int[n];
            this.usado = new boolean[n];
            this.viz = new int[n][];
            for (int v = 0; v < n; v++) {
                List<Integer> l = new ArrayList<>();
                for (int w = 0; w < n; w++) if (w != v && g.existeAresta(v, w)) l.add(w);
                viz[v] = l.stream().mapToInt(Integer::intValue).toArray();
            }
        }

        /** Devolve a sequência de vértices, ou null (ver estourou para saber o porquê). */
        int[] executar() {
            if (ciclo) {
                return tentar(0) ? caminho.clone() : null;   // um ciclo passa por qualquer vértice
            }
            int[] d = graus(g);
            Integer[] inicios = ordemCrescentePorGrau(d);
            for (int s : inicios) {
                if (tentar(s)) return caminho.clone();
                if (estourou) return null;
            }
            return null;
        }

        private boolean tentar(int inicio) {
            Arrays.fill(usado, false);
            caminho[0] = inicio;
            usado[inicio] = true;
            return dfs(inicio, 1);
        }

        private boolean dfs(int atual, int tam) {
            if (tam == n) return !ciclo || g.existeAresta(atual, caminho[0]);
            if (++passos > limite) { estourou = true; return false; }

            int[] cand = new int[viz[atual].length];
            int[] livres = new int[cand.length];
            int q = 0;
            for (int w : viz[atual]) {
                if (usado[w]) continue;
                int l = 0;
                for (int x : viz[w]) if (!usado[x]) l++;
                // insere ordenado por número de vizinhos livres (crescente)
                int i = q++;
                while (i > 0 && livres[i - 1] > l) { cand[i] = cand[i - 1]; livres[i] = livres[i - 1]; i--; }
                cand[i] = w;
                livres[i] = l;
            }
            for (int i = 0; i < q; i++) {
                int w = cand[i];
                usado[w] = true;
                caminho[tam] = w;
                if (dfs(w, tam + 1)) return true;
                usado[w] = false;
                if (estourou) return false;
            }
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Conexidade local (BFS) — independente de Conexidade.java
    // ------------------------------------------------------------------

    /** comp[v] = id da componente conexa de v (0, 1, ...). */
    public static int[] componentes(TGrafo g) {
        int n = g.getN();
        int[] comp = new int[n];
        Arrays.fill(comp, -1);
        int id = 0;
        for (int s = 0; s < n; s++) {
            if (comp[s] != -1) continue;
            ArrayDeque<Integer> fila = new ArrayDeque<>();
            fila.add(s);
            comp[s] = id;
            while (!fila.isEmpty()) {
                int u = fila.poll();
                for (int w = 0; w < n; w++) {
                    if (comp[w] == -1 && w != u && g.existeAresta(u, w)) {
                        comp[w] = id;
                        fila.add(w);
                    }
                }
            }
            id++;
        }
        return comp;
    }

    public static boolean conexo(TGrafo g) {
        if (g.getN() == 0) return true;
        for (int c : componentes(g)) if (c != 0) return false;
        return true;
    }

    /** true se todos os vértices com grau > 0 estão numa mesma componente. */
    public static boolean conexoSemIsolados(TGrafo g) {
        int[] comp = componentes(g);
        int[] d = graus(g);
        int ref = -1;
        for (int v = 0; v < g.getN(); v++) {
            if (d[v] == 0) continue;
            if (ref == -1) ref = comp[v];
            else if (comp[v] != ref) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** Índices ordenados por grau decrescente (empate: menor índice primeiro). */
    private static Integer[] ordemPorGrau(int[] d) {
        Integer[] ordem = new Integer[d.length];
        for (int i = 0; i < ordem.length; i++) ordem[i] = i;
        Arrays.sort(ordem, (a, b) -> d[a] != d[b] ? d[b] - d[a] : a - b);
        return ordem;
    }

    private static Integer[] ordemCrescentePorGrau(int[] d) {
        Integer[] ordem = new Integer[d.length];
        for (int i = 0; i < ordem.length; i++) ordem[i] = i;
        Arrays.sort(ordem, (a, b) -> d[a] != d[b] ? d[a] - d[b] : a - b);
        return ordem;
    }
}
