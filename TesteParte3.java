/*
 * =====================================================================
 * ConexSom — Recomendação Musical por Similaridade entre Artistas
 * Arquivo : TesteParte3.java
 *
 * Integrantes:
 *   Enrique Cipolla Martins ........ RA 10427834
 *   Henrique Ferreira Marciano ..... RA 10439797
 *   Pedro Henrique Saraiva Arruda .. RA 10437747
 *
 * Síntese:
 *   Testes reproduzíveis da Parte 3 (Recomendacao e Caracteristicas) em
 *   grafos pequenos com resposta conhecida (K5, C6, P4, estrela, Petersen,
 *   exemplo do ConexSom) e num grafo aleatório de 100 vértices.
 *   Requer TGrafo.insereA funcionando.
 *
 *   Executar:
 *     javac -encoding UTF-8 -d bin src/*.java
 *     java -cp bin TesteParte3
 *
 * Histórico de alterações (data — autor — descrição):
 *   11/11/2026 — Henrique Ferreira Marciano — Versão inicial (Parte 3)
 * =====================================================================
 */

import java.util.List;
import java.util.Random;

public class TesteParte3 {

    private static int ok = 0, falhas = 0;

    public static void main(String[] args) {
        testarRecomendacao();
        testarCaracteristicasConhecidas();
        testarGrafoAleatorio();
        System.out.println();
        System.out.println(ok + " verificações corretas, " + falhas + " falhas.");
        if (falhas > 0) System.exit(1);
    }

    // ------------------------------------------------------------------

    private static void testarRecomendacao() {
        System.out.println("== Recomendacao (grafo_exemplo.txt, 6 artistas) ==");
        TGrafo g = grafo(6, new double[][] {
            {0, 1, 0.82}, {0, 3, 0.61}, {1, 2, 0.90}, {1, 3, 0.74},
            {2, 5, 0.58}, {4, 5, 0.67}, {3, 4, 0.35}});

        List<Recomendacao.Sugestao> viz = Recomendacao.vizinhosPorSimilaridade(g, 0);
        confere("vizinhos de 0: 2 artistas", viz.size() == 2);
        confere("1º vizinho é o 1 (0,82)", viz.get(0).artista == 1 && perto(viz.get(0).forca, 0.82));
        confere("2º vizinho é o 3 (0,61)", viz.get(1).artista == 3 && perto(viz.get(1).forca, 0.61));

        List<Recomendacao.Sugestao> rec = Recomendacao.recomendar(g, 0, 2);
        confere("alcança 4 artistas em até 2 passos (1,2,3,4)", rec.size() == 4);
        confere("ordem: 1, 2, 3, 4", rec.get(0).artista == 1 && rec.get(1).artista == 2
                && rec.get(2).artista == 3 && rec.get(3).artista == 4);
        confere("2 = 0,82*0,90 = 0,738 via 1", perto(rec.get(1).forca, 0.738)
                && !rec.get(1).direta() && rec.get(1).caminho[1] == 1);
        confere("3 continua direto (0,61 > 0,82*0,74)", rec.get(2).direta() && perto(rec.get(2).forca, 0.61));
        confere("4 = 0,61*0,35 = 0,2135 via 3", perto(rec.get(3).forca, 0.2135) && rec.get(3).caminho[1] == 3);

        List<Recomendacao.Sugestao> rec1 = Recomendacao.recomendar(g, 0, 1);
        confere("profundidade 1 devolve só os 2 vizinhos", rec1.size() == 2);

        Recomendacao.Sugestao cam = Recomendacao.caminhoMaisSimilar(g, 0, 5);
        confere("caminho 0->5 é 0-1-2-5", cam != null && java.util.Arrays.equals(cam.caminho, new int[] {0, 1, 2, 5}));
        confere("força 0,82*0,90*0,58 = 0,4280", cam != null && perto(cam.forca, 0.82 * 0.90 * 0.58));

        TGrafo h = grafo(4, new double[][] {{0, 1, 0.5}, {2, 3, 0.5}});
        confere("sem caminho entre componentes => null", Recomendacao.caminhoMaisSimilar(h, 0, 3) == null);

        confere("resolver por índice", Recomendacao.resolver(g, "3") == 3);
        confere("resolver índice fora => -1", Recomendacao.resolver(g, "99") == -1);
        TGrafo nomes = new TGrafo(2, 3);
        nomes.setVertice(0, "Djavan", 0);
        nomes.setVertice(1, "Caetano Veloso", 0);
        nomes.setVertice(2, "Caetano Moraes", 0);
        confere("resolver sem acento/caixa", Recomendacao.resolver(nomes, "DJAVAN") == 0);
        confere("resolver parte do nome ambígua => -2", Recomendacao.resolver(nomes, "caetano") == -2);
        confere("resolver parte do nome única", Recomendacao.resolver(nomes, "veloso") == 1);
    }

    // ------------------------------------------------------------------

    private static void testarCaracteristicasConhecidas() {
        System.out.println("== Caracteristicas (grafos com resposta conhecida) ==");

        // K5: euleriano, hamiltoniano, χ = 5
        TGrafo k5 = completo(5);
        confere("K5 euleriano", Caracteristicas.temCicloEuleriano(k5));
        confere("K5 hamiltoniano (ciclo)", Caracteristicas.hamilton(k5, true, 100000).status == Caracteristicas.Status.SIM);
        confere("K5 Dirac/Ore vale", Caracteristicas.condicaoSuficienteCiclo(k5) != null);
        int[] corK5 = Caracteristicas.colorir(k5);
        confere("K5 coloração válida com 5 cores", Caracteristicas.coloracaoValida(k5, corK5) && cores(corK5) == 5);
        confere("K5 clique = 5", Caracteristicas.cliqueGulosa(k5).length == 5);

        // C6: euleriano, hamiltoniano, bipartido (2 cores)
        TGrafo c6 = ciclo(6);
        confere("C6 euleriano", Caracteristicas.temCicloEuleriano(c6));
        confere("C6 hamiltoniano (ciclo)", Caracteristicas.hamilton(c6, true, 100000).status == Caracteristicas.Status.SIM);
        int[] corC6 = Caracteristicas.colorir(c6);
        confere("C6 colorido com 2 cores", Caracteristicas.coloracaoValida(c6, corC6) && cores(corC6) == 2);

        // P4: percurso euleriano (2 ímpares), sem ciclo hamiltoniano, com caminho
        TGrafo p4 = grafo(4, new double[][] {{0, 1, 1}, {1, 2, 1}, {2, 3, 1}});
        confere("P4 não é euleriano", !Caracteristicas.temCicloEuleriano(p4));
        confere("P4 tem percurso euleriano", Caracteristicas.temPercursoEuleriano(p4));
        confere("P4 sem ciclo hamiltoniano", Caracteristicas.hamilton(p4, true, 100000).status == Caracteristicas.Status.NAO);
        confere("P4 com caminho hamiltoniano", Caracteristicas.hamilton(p4, false, 100000).status == Caracteristicas.Status.SIM);

        // Estrela K1,3: 3 folhas => sem caminho hamiltoniano
        TGrafo estrela = grafo(4, new double[][] {{0, 1, 1}, {0, 2, 1}, {0, 3, 1}});
        confere("estrela sem caminho hamiltoniano", Caracteristicas.hamilton(estrela, false, 100000).status == Caracteristicas.Status.NAO);
        confere("estrela sem percurso euleriano (4 ímpares)", !Caracteristicas.temPercursoEuleriano(estrela));

        // Petersen: sem ciclo hamiltoniano (prova exaustiva), com caminho, χ = 3, grau 3 (sem Euler)
        TGrafo pet = grafo(10, new double[][] {
            {0, 1, 1}, {1, 2, 1}, {2, 3, 1}, {3, 4, 1}, {4, 0, 1},
            {0, 5, 1}, {1, 6, 1}, {2, 7, 1}, {3, 8, 1}, {4, 9, 1},
            {5, 7, 1}, {7, 9, 1}, {9, 6, 1}, {6, 8, 1}, {8, 5, 1}});
        confere("Petersen sem ciclo hamiltoniano (exaustivo)",
                Caracteristicas.hamilton(pet, true, 1000000).status == Caracteristicas.Status.NAO);
        Caracteristicas.ResultadoHamilton hp = Caracteristicas.hamilton(pet, false, 1000000);
        confere("Petersen com caminho hamiltoniano válido", hp.status == Caracteristicas.Status.SIM && caminhoValido(pet, hp.caminho, false));
        confere("Petersen não euleriano", !Caracteristicas.temPercursoEuleriano(pet));
        int[] corPet = Caracteristicas.colorir(pet);
        confere("Petersen coloração válida (≤ 4 cores, χ=3)", Caracteristicas.coloracaoValida(pet, corPet) && cores(corPet) <= 4);

        // Desconexo
        TGrafo desc = grafo(4, new double[][] {{0, 1, 1}, {2, 3, 1}});
        confere("desconexo não é conexo", !Caracteristicas.conexo(desc));
        confere("desconexo sem hamilton", Caracteristicas.hamilton(desc, false, 1000).status == Caracteristicas.Status.NAO);

        // Limite de passos: com orçamento mínimo, nunca afirma NAO sem prova
        Caracteristicas.ResultadoHamilton lim = Caracteristicas.hamilton(pet, true, 3);
        confere("orçamento minúsculo => INDETERMINADO (não NAO)", lim.status == Caracteristicas.Status.INDETERMINADO);
    }

    // ------------------------------------------------------------------

    private static void testarGrafoAleatorio() {
        System.out.println("== Grafo aleatório (100 artistas, ~250 arestas, semente fixa) ==");
        Random r = new Random(42);
        int n = 100;
        TGrafo g = new TGrafo(2, n);
        for (int i = 0; i < n; i++) g.setVertice(i, "Artista " + i, 0f);
        while (g.getM() < 250) {
            int v = r.nextInt(n), w = r.nextInt(n);
            g.insereA(v, w, 0.30f + 0.70f * r.nextFloat());
        }
        confere("m = 250", g.getM() == 250);

        int[] d = Caracteristicas.graus(g);
        int soma = 0;
        for (int x : d) soma += x;
        confere("soma dos graus = 2m", soma == 500);

        int[] cor = Caracteristicas.colorir(g);
        confere("coloração válida", Caracteristicas.coloracaoValida(g, cor));
        int maxGrau = 0;
        for (int x : d) maxGrau = Math.max(maxGrau, x);
        confere("cores ≤ Δ+1", cores(cor) <= maxGrau + 1);
        confere("clique ≤ cores", Caracteristicas.cliqueGulosa(g).length <= cores(cor));

        Caracteristicas.ResultadoHamilton hc = Caracteristicas.hamilton(g, true, Caracteristicas.LIMITE_PASSOS);
        confere("ciclo hamiltoniano: se SIM com certificado, o certificado é válido",
                hc.caminho == null || caminhoValido(g, hc.caminho, true));
        Caracteristicas.ResultadoHamilton hpth = Caracteristicas.hamilton(g, false, Caracteristicas.LIMITE_PASSOS);
        confere("caminho hamiltoniano: se SIM com certificado, o certificado é válido",
                hpth.caminho == null || caminhoValido(g, hpth.caminho, false));

        // Recomendação em grafo grande: forças em (0,1], ordenadas, caminhos coerentes
        boolean ordenado = true, caminhosOk = true;
        for (int o = 0; o < n; o += 7) {
            List<Recomendacao.Sugestao> rec = Recomendacao.recomendar(g, o, 2);
            for (int i = 0; i < rec.size(); i++) {
                Recomendacao.Sugestao s = rec.get(i);
                if (i > 0 && rec.get(i - 1).forca < s.forca - 1e-12) ordenado = false;
                if (s.caminho[0] != o || s.caminho[s.caminho.length - 1] != s.artista
                        || s.caminho.length > 3) caminhosOk = false;
                double prod = 1;
                for (int k = 1; k < s.caminho.length; k++) {
                    if (!g.existeAresta(s.caminho[k - 1], s.caminho[k])) caminhosOk = false;
                    else prod *= g.getPesoAresta(s.caminho[k - 1], s.caminho[k]);
                }
                if (!perto(prod, s.forca)) caminhosOk = false;
            }
        }
        confere("recomendações ordenadas por força", ordenado);
        confere("cada caminho existe, tem ≤ 2 arestas e produto = força", caminhosOk);

        // Dijkstra nunca é pior que o melhor caminho de ≤ 2 arestas
        boolean dijkstraOk = true;
        for (int o = 0; o < n; o += 11) {
            for (Recomendacao.Sugestao s : Recomendacao.recomendar(g, o, 2)) {
                Recomendacao.Sugestao dj = Recomendacao.caminhoMaisSimilar(g, o, s.artista);
                if (dj == null || dj.forca < s.forca - 1e-9) dijkstraOk = false;
            }
        }
        confere("Dijkstra ≥ melhor caminho de até 2 passos", dijkstraOk);

        // A saída completa roda sem exceção
        System.out.println();
        System.out.println("-- saída completa de Caracteristicas.apresentar() no grafo aleatório --");
        Caracteristicas.apresentar(g);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static TGrafo grafo(int n, double[][] arestas) {
        TGrafo g = new TGrafo(2, n);
        for (int i = 0; i < n; i++) g.setVertice(i, "V" + i, 0f);
        for (double[] a : arestas) g.insereA((int) a[0], (int) a[1], (float) a[2]);
        return g;
    }

    private static TGrafo completo(int n) {
        TGrafo g = new TGrafo(2, n);
        for (int i = 0; i < n; i++) g.setVertice(i, "V" + i, 0f);
        for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) g.insereA(i, j, 1f);
        return g;
    }

    private static TGrafo ciclo(int n) {
        TGrafo g = new TGrafo(2, n);
        for (int i = 0; i < n; i++) g.setVertice(i, "V" + i, 0f);
        for (int i = 0; i < n; i++) g.insereA(i, (i + 1) % n, 1f);
        return g;
    }

    private static int cores(int[] cor) {
        int k = 0;
        for (int c : cor) k = Math.max(k, c + 1);
        return k;
    }

    private static boolean caminhoValido(TGrafo g, int[] cam, boolean ciclo) {
        if (cam == null || cam.length != g.getN()) return false;
        boolean[] visto = new boolean[g.getN()];
        for (int i = 0; i < cam.length; i++) {
            if (visto[cam[i]]) return false;
            visto[cam[i]] = true;
            if (i > 0 && !g.existeAresta(cam[i - 1], cam[i])) return false;
        }
        return !ciclo || g.existeAresta(cam[cam.length - 1], cam[0]);
    }

    private static boolean perto(double a, double b) { return Math.abs(a - b) < 1e-4; }

    private static void confere(String descricao, boolean condicao) {
        if (condicao) ok++; else falhas++;
        System.out.println((condicao ? "  [ok]    " : "  [FALHA] ") + descricao);
    }
}
