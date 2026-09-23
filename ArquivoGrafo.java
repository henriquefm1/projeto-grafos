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
 *   dd/mm/aaaa — Fulano — Versão inicial
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
     * Lança IOException com mensagem clara se o arquivo estiver mal formatado.
     */
    public static TGrafo ler(String caminho) throws IOException {
        List<String> linhas = lerLinhasUteis(caminho);
        int pos = 0;

        int tipo = Integer.parseInt(linhas.get(pos++).trim());
        int n = Integer.parseInt(linhas.get(pos++).trim());
        TGrafo g = new TGrafo(tipo, n);

        // ---- vértices ----
        for (int i = 0; i < n; i++) {
            String linha = linhas.get(pos++);
            Matcher mt = LINHA_VERTICE.matcher(linha);
            if (!mt.matches()) {
                throw new IOException("Linha de vértice inválida: " + linha);
            }
            int v = Integer.parseInt(mt.group(1));
            String rotulo = mt.group(2);
            float peso = (mt.group(3) != null) ? parseFloat(mt.group(3)) : 0f;

            // TODO(8) Validar que v == i (vértices numerados 0..n-1, em ordem).
            g.setVertice(v, rotulo, peso);
        }

        // ---- arestas ----
        int m = Integer.parseInt(linhas.get(pos++).trim());
        for (int i = 0; i < m; i++) {
            String[] partes = linhas.get(pos++).trim().split("\\s+");
            int v = Integer.parseInt(partes[0]);
            int w = Integer.parseInt(partes[1]);
            float peso = (partes.length >= 3) ? parseFloat(partes[2]) : 1f;

            // TODO(9) Decidir e documentar: no grafo NÃO orientado, cada aresta
            //   aparece UMA vez no arquivo (v w peso) ou duas (v w / w v)?
            //   O exemplo do PDF lista os dois sentidos, mas ele é orientado.
            //   Recomendação: uma vez só, e m = nº real de arestas (>= 200).
            //   Se insereA devolver false (aresta repetida/inválida), avisar.
            g.insereA(v, w, peso);
        }
        return g;
    }

    /**
     * Opção b) — grava o grafo da memória no MESMO formato da leitura.
     *
     * TODO(10) Implementar com PrintWriter (UTF-8):
     *   1. Linha 1: tipo.  Linha 2: n.
     *   2. Para cada v: v "rotulo"  (+ " peso" se g.temPesoVertice()).
     *   3. Linha: m.
     *   4. Arestas: no não orientado percorrer só w > v (senão sai duplicado);
     *      no orientado percorrer a matriz toda.
     *      Acrescentar peso se g.temPesoAresta().
     *   5. Usar String.format(Locale.US, "%.4f", peso) — com Locale padrão
     *      pt-BR o Java escreve "0,82" e a leitura pode quebrar.
     *   Teste obrigatório: ler -> gravar -> ler de novo e comparar n e m.
     */
    public static void gravar(TGrafo g, String caminho) throws IOException {
        // TODO(10)
        System.out.println("[TODO] gravar() ainda não implementado.");
    }

    /**
     * Opção g) — mostra o conteúdo de forma "visualmente compreensiva e atraente".
     *
     * TODO(11) Implementar, por exemplo:
     *   ╔══ ConexSom — grafo.txt ══╗
     *   Tipo 2: não orientado com peso na aresta | 85 artistas | 230 similaridades
     *   --- Artistas ---
     *     0  Djavan              (grau 7)
     *   --- Similaridades ---
     *     Djavan  <-- 0,82 -->  Caetano Veloso
     *   Mostrar o nome do tipo por extenso (usar descricaoTipo abaixo).
     */
    public static void mostrarConteudo(TGrafo g) {
        // TODO(11)
        System.out.println("[TODO] mostrarConteudo() ainda não implementado.");
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

    /** Aceita "0.82" e "0,82". */
    static float parseFloat(String s) {
        return Float.parseFloat(s.replace(',', '.'));
    }

    /** Formata peso sempre com ponto (para gravação). */
    static String formatarPeso(float p) {
        return String.format(Locale.US, "%.4f", p);
    }
}
