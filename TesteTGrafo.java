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
 *   24/09/2026 — (preencher) — Versão inicial dos testes de TGrafo
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

    public static void main(String[] args) {
        System.out.println("=== Testes do TGrafo ===");
        blocoA_construcao();
        blocoB_arestas();
        blocoC_buscaEGrau();

        System.out.println("\n=== Resultado: " + (total - falhas) + "/" + total
                + " verificações passaram ===");
        System.exit(falhas == 0 ? 0 : 1);
    }
}