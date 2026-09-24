/*
 * =====================================================================
 * ConexSom — Recomendação Musical por Similaridade entre Artistas
 * Arquivo : TesteTGrafo.java
 *
 * Integrantes:
 *   Enrique Cipolla Martins ........ RA 10427834
 *   Henrique Ferreira Marciano ..... RA 10439797
 *   Pedro Henrique Saraiva Arruda .. RA 10437747
 *
 * Síntese:
 *   Teste automático simples (sem JUnit) da classe TGrafo. Cada verificação
 *   imprime OK ou FALHOU; no final mostra o total e devolve código de saída
 *   diferente de zero se algo falhar. Serve também como evidência de teste
 *   para o relatório (printscreen da saída).
 *
 * Histórico de alterações (data — autor — descrição):
 *   24/09/2026 — Enrique Cipolla Martins — Versão inicial dos testes de TGrafo
 *   24/09/2026 — Henrique Ferreira Marciano — Blocos D (vértices) e E (arquivo)
 * =====================================================================
 *
 * Como rodar (na pasta onde estão os .java):
 *   javac -encoding UTF-8 -d bin *.java
 *   java -cp bin TesteTGrafo
 *
 * Se você alinhou os nomes com a classe de aula (TODO 0) e algum método
 * mudou de nome, ajuste as chamadas abaixo — a lógica dos testes continua
 * a mesma.
 */
public class TesteTGrafo {

    private static int total = 0;
    private static int falhas = 0;

    /** Registra uma verificação e imprime o resultado. */
    private static void confere(String descricao, boolean condicao) {
        total++;
        if (condicao) {
            System.out.println("  OK     " + descricao);
        } else {
            falhas++;
            System.out.println("  FALHOU " + descricao);
        }
    }

    private static boolean igual(float a, float b) {
        return Math.abs(a - b) < 0.0001f;
    }

    /** Grafo de 6 artistas, tipo 2 (não orientado, peso na aresta). */
    private static TGrafo novoGrafoDeTeste() {
        TGrafo g = new TGrafo(2, 6);
        String[] nomes = {"Djavan", "Caetano Veloso", "Marisa Monte",
                          "Gilberto Gil", "Chico Buarque", "Milton Nascimento"};
        for (int i = 0; i < nomes.length; i++) {
            g.setVertice(i, nomes[i], 0f);
        }
        return g;
    }

    // ------------------------------------------------------------------
    // Bloco A — funciona logo após o TODO(0): construtor e consultas
    // ------------------------------------------------------------------
    private static void blocoA_construcao() {
        System.out.println("\n[A] Construção e consultas básicas (TODO 0)");
        TGrafo g = novoGrafoDeTeste();
        confere("tipo == 2", g.getTipo() == 2);
        confere("n == 6", g.getN() == 6);
        confere("m == 0 no grafo recém-criado", g.getM() == 0);
        confere("grafo tipo 2 não é orientado", !g.isOrientado());
        confere("grafo tipo 2 tem peso na aresta", g.temPesoAresta());
        confere("grafo tipo 2 não tem peso no vértice", !g.temPesoVertice());
        confere("rótulo do vértice 1 é Caetano Veloso",
                "Caetano Veloso".equals(g.getRotulo(1)));
        confere("verticeValido(0) e (5) verdadeiros", g.verticeValido(0) && g.verticeValido(5));
        confere("verticeValido(-1) e (6) falsos", !g.verticeValido(-1) && !g.verticeValido(6));
    }

    // ------------------------------------------------------------------
    // Bloco B — depois dos TODOs 1 e 2: inserir/remover aresta
    // ------------------------------------------------------------------
    private static void blocoB_arestas() {
        System.out.println("\n[B] Inserir e remover aresta (TODOs 1 e 2)");
        TGrafo g = novoGrafoDeTeste();

        confere("insereA(0,1,0.80) devolve true", g.insereA(0, 1, 0.80f));
        confere("m == 1 depois da primeira inserção", g.getM() == 1);
        confere("existeAresta(0,1)", g.existeAresta(0, 1));
        confere("simetria: existeAresta(1,0) (não orientado)", g.existeAresta(1, 0));
        confere("peso 0,80 nos dois sentidos",
                igual(g.getPesoAresta(0, 1), 0.80f) && igual(g.getPesoAresta(1, 0), 0.80f));

        confere("inserir a mesma aresta de novo devolve false", !g.insereA(0, 1, 0.80f));
        confere("inserir a inversa (1,0) também devolve false", !g.insereA(1, 0, 0.80f));
        confere("m continua 1 (não contou em dobro)", g.getM() == 1);

        confere("laço insereA(2,2) é rejeitado", !g.insereA(2, 2, 0.5f));
        confere("vértice inválido insereA(0,9) é rejeitado", !g.insereA(0, 9, 0.5f));
        confere("vértice negativo insereA(-1,0) é rejeitado", !g.insereA(-1, 0, 0.5f));
        confere("m continua 1 depois das rejeições", g.getM() == 1);

        g.insereA(0, 2, 0.60f);
        g.insereA(1, 2, 0.70f);
        confere("m == 3 com três arestas distintas", g.getM() == 3);

        confere("removeA(0,1) devolve true", g.removeA(0, 1));
        confere("aresta sumiu nos dois sentidos", !g.existeAresta(0, 1) && !g.existeAresta(1, 0));
        confere("m == 2 depois de remover", g.getM() == 2);
        confere("remover aresta inexistente devolve false", !g.removeA(0, 1));
        confere("m continua 2", g.getM() == 2);
        confere("as outras arestas continuam lá", g.existeAresta(0, 2) && g.existeAresta(1, 2));
    }

    // ------------------------------------------------------------------
    // Bloco C — depois dos TODOs 5 e 6: busca por rótulo e grau
    // ------------------------------------------------------------------
    private static void blocoC_buscaEGrau() {
        System.out.println("\n[C] Busca por rótulo e grau (TODOs 5 e 6)");
        TGrafo g = novoGrafoDeTeste();
        g.insereA(0, 1, 0.80f);
        g.insereA(0, 2, 0.60f);
        g.insereA(0, 3, 0.55f);

        confere("buscaPorRotulo(\"Djavan\") == 0", g.buscaPorRotulo("Djavan") == 0);
        confere("busca ignora maiúsculas/minúsculas", g.buscaPorRotulo("marisa MONTE") == 2);
        confere("artista inexistente devolve -1", g.buscaPorRotulo("Fulano") == -1);

        confere("grau(0) == 3", g.grau(0) == 3);
        confere("grau(1) == 1", g.grau(1) == 1);
        confere("grau(5) == 0 (vértice isolado)", g.grau(5) == 0);
    }

    // ------------------------------------------------------------------
    // Bloco D — depois dos TODOs 3 e 4: inserir/remover vértice
    // ------------------------------------------------------------------
    private static void blocoD_vertices() {
        System.out.println("\n[D] Inserir e remover vértice (TODOs 3 e 4)");
        TGrafo g = novoGrafoDeTeste();
        g.insereA(0, 1, 0.80f);   // Djavan - Caetano
        g.insereA(1, 2, 0.70f);   // Caetano - Marisa
        g.insereA(2, 5, 0.40f);   // Marisa - Milton

        int novo = g.insereV("Tim Maia", 0f);
        confere("insereV devolve o índice 6", novo == 6);
        confere("n == 7 depois de inserir", g.getN() == 7);
        confere("rótulo do novo vértice é Tim Maia", "Tim Maia".equals(g.getRotulo(6)));
        confere("novo vértice nasce sem arestas", g.grau(6) == 0);
        confere("arestas antigas preservadas", g.existeAresta(0, 1) && g.getM() == 3);
        confere("aceita aresta com o novo vértice", g.insereA(6, 0, 0.50f) && g.getM() == 4);
        confere("artista duplicado é rejeitado", g.insereV("tim maia", 0f) == -1);
        confere("rótulo vazio é rejeitado", g.insereV("  ", 0f) == -1);

        // Remover um vértice do MEIO: Caetano (1), que tem grau 2
        confere("removeV(1) devolve true", g.removeV(1));
        confere("n == 6 depois de remover", g.getN() == 6);
        confere("m desconta as 2 arestas de Caetano", g.getM() == 2);
        confere("índices deslocados: Marisa agora é 1", g.buscaPorRotulo("Marisa Monte") == 1);
        confere("Caetano não existe mais", g.buscaPorRotulo("Caetano Veloso") == -1);
        confere("aresta Marisa-Milton sobreviveu (agora 1-4)",
                g.existeAresta(1, 4) && igual(g.getPesoAresta(1, 4), 0.40f));
        confere("aresta Tim Maia-Djavan sobreviveu (agora 5-0)", g.existeAresta(5, 0));

        // Remover o PRIMEIRO e o ÚLTIMO
        confere("removeV(0) (primeiro) devolve true", g.removeV(0));
        confere("m == 1 depois de remover Djavan", g.getM() == 1);
        confere("removeV(último) devolve true", g.removeV(g.getN() - 1));
        confere("n == 4 no final", g.getN() == 4);
        confere("vértice inválido é rejeitado", !g.removeV(10) && !g.removeV(-1));
    }

    // ------------------------------------------------------------------
    // Bloco E — TODOs 8 a 10: ler -> gravar -> ler de novo
    // ------------------------------------------------------------------
    private static void blocoE_arquivo() {
        System.out.println("\n[E] Ler -> gravar -> ler (TODOs 8 a 10)");
        try {
            java.io.File tmp = java.io.File.createTempFile("grafo_teste", ".txt");
            tmp.deleteOnExit();
            TGrafo g = novoGrafoDeTeste();
            g.insereA(0, 1, 0.8234f);
            g.insereA(3, 4, 0.35f);
            ArquivoGrafo.gravar(g, tmp.getPath());
            TGrafo lido = ArquivoGrafo.ler(tmp.getPath());
            confere("n preservado", lido.getN() == g.getN());
            confere("m preservado", lido.getM() == g.getM());
            confere("peso preservado com 4 casas", igual(lido.getPesoAresta(1, 0), 0.8234f));
            confere("rótulo preservado", "Chico Buarque".equals(lido.getRotulo(4)));

            // Grafo orientado com peso no vértice (tipo 5)
            TGrafo o = new TGrafo(5, 3);
            o.setVertice(0, "A", 1.5f);
            o.setVertice(1, "B", 2f);
            o.setVertice(2, "C", 3f);
            o.insereA(0, 1, 9f);
            o.insereA(1, 0, 9f);
            o.insereA(1, 2, 9f);
            confere("orientado: 0->1 e 1->0 são arestas distintas", o.getM() == 3);
            confere("orientado: 1->2 não cria 2->1", !o.existeAresta(2, 1));
            ArquivoGrafo.gravar(o, tmp.getPath());
            TGrafo o2 = ArquivoGrafo.ler(tmp.getPath());
            confere("orientado: m preservado", o2.getM() == 3);
            confere("orientado: peso do vértice preservado", igual(o2.getPesoVertice(0), 1.5f));
            confere("orientado: removeV(1) tira entrada e saída", o2.removeV(1) && o2.getM() == 0);
        } catch (Exception e) {
            confere("exceção inesperada: " + e.getMessage(), false);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Testes do TGrafo ===");
        blocoA_construcao();
        blocoB_arestas();
        blocoC_buscaEGrau();
        blocoD_vertices();
        blocoE_arquivo();

        System.out.println("\n=== Resultado: " + (total - falhas) + "/" + total
                + " verificações passaram ===");
        System.exit(falhas == 0 ? 0 : 1);
    }
}