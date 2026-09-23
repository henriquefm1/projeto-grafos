# ConexSom — Recomendação Musical por Similaridade entre Artistas

Projeto de Teoria dos Grafos (Mackenzie, 6º D) — Parte 2.
Integrantes: Enrique Cipolla Martins (10427834), Henrique Ferreira Marciano (10439797), Pedro Henrique Saraiva Arruda (10437747).

**Entrega:** 28/09, até 23h59 (relatório + fontes + grafo.txt + GitHub público).
**Apresentação:** 01/10, no horário da aula, máximo de 5 minutos, com todos presentes.

## Estrutura

```
conexsom/
├── src/
│   ├── ConexSom.java      menu a)–j) (pronto, só chama os outros)
│   ├── TGrafo.java        matriz de adjacência — alinhar com a classe de aula
│   ├── ArquivoGrafo.java  ler / gravar / mostrar grafo.txt
│   └── Conexidade.java    conexo/desconexo, C0–C3, FCONEX, reduzido
├── coleta/
│   └── gerar_grafo.py     coleta dos dados reais e geração do grafo.txt
├── grafo.txt              (hoje é uma cópia do exemplo; substituir pelo real)
└── grafo_exemplo.txt      6 artistas, para testar o código cedo
```

Compilar e rodar:

```
javac -encoding UTF-8 -d bin src/*.java
java -cp bin ConexSom
```

O esqueleto já compila e o menu roda; as operações ainda respondem "[TODO]".

## Decisões de modelagem (respondendo ao parecer da Parte 1)

Cada item abaixo é uma cobrança explícita do professor. Resolvam e registrem no relatório.

- **Vértices = artistas, somente.** Removam do texto qualquer menção a "artistas ou músicas".
- **Fonte dos dados reais:** definir já (sugestão no `gerar_grafo.py`: API do Last.fm) e registrar a origem e a data da coleta.
- **Fórmula de similaridade:** escrever a equação no relatório (Jaccard ou cosseno sobre tags; BPM só se tiverem fonte real).
- **Limiar para criar aresta:** um valor fixo e justificado, calibrado para dar n ≥ 80 e m ≥ 200.
- **Categoria 2** (não orientado, peso na aresta): similaridade é simétrica, por isso não orientado; o peso é o grau de similaridade.
- **ODS 3 precisa ser revista ou removida.** Ou argumentem de verdade (música e bem-estar/saúde mental, com referência), ou fiquem só com ODS 9.
- **Algoritmo de recomendação (Parte 3):** não assumir que caminho mínimo é o melhor. Opções coerentes com similaridade: ranquear vizinhos pelo peso; BFS até profundidade 2 combinando pesos; ou caminho de maior similaridade usando custo = −log(sim) ou 1 − sim. Citar isso como "próximas etapas".

## Mapa dos TODOs no código

| TODO | Onde | O quê |
| --- | --- | --- |
| 0 | TGrafo | Alinhar com a classe TGrafo dada em aula (obrigatório pelo enunciado) |
| 1–2 | TGrafo | insereA / removeA |
| 3–4 | TGrafo | insereV / removeV (realocar matriz, renumerar) |
| 5–6 | TGrafo | buscaPorRotulo, grau |
| 7 | TGrafo | show() legível com 80+ vértices |
| 8–9 | ArquivoGrafo | Validações da leitura; aresta aparece uma vez no arquivo |
| 10 | ArquivoGrafo | gravar() no mesmo formato, com ponto decimal |
| 11 | ArquivoGrafo | mostrarConteudo() "visualmente atraente" |
| 12–13 | Conexidade | Componentes conexas (BFS/DFS) e listagem |
| 14–16 | Conexidade | Categoria C0–C3, FCONEX, grafo reduzido |
| 17–20 | ConexSom | Ajustes de menu e validações |
| 21–28 | gerar_grafo.py | Coleta, fórmula, limiar, metas n ≥ 80 e m ≥ 200 |

Sugestão de ordem: 0 → 1–2 → 5 → 7 → 10 (já dá para testar ler/gravar) → 3–4 → 11 → 13 → 14–16 → coleta em paralelo.

## Checklist de entrega

### Código
- [ ] Cabeçalho em todos os arquivos com integrantes, síntese e histórico (data — autor — descrição)
- [ ] Título do app acima do menu
- [ ] Todas as opções a)–j) funcionando
- [ ] Conexidade funcionando também com um grafo orientado de teste (o professor pode testar)
- [ ] grafo.txt real com n ≥ 80 e m ≥ 200

### Relatório (seguir o template)
- [ ] Título, integrantes (nome, e-mail, RA), resumo
- [ ] Introdução: contextualização, justificativa, objetivo (usar o parecer para corrigir)
- [ ] Fundamentação teórica: grafo, grafo ponderado, matriz de adjacência, conexidade, componentes
- [ ] Trabalhos relacionados: 2 ou 3 sistemas ou artigos de recomendação por grafo, com a solução de cada um
- [ ] Metodologia: etapas (coleta → similaridade → limiar → grafo → aplicação → algoritmo na Parte 3)
- [ ] Coleta de dados detalhada (fonte, data, quantidade, filtros)
- [ ] Modelagem: vértices, arestas, pesos, rótulos, categoria justificada, fórmula e limiar
- [ ] Imagem do grafo no Graph Online (ou Gephi, que lida melhor com 80+ vértices)
- [ ] ODS com justificativa revisada
- [ ] Prints: pelo menos 2 testes de cada opção do menu
- [ ] Resultados parciais e análise crítica: n, m, grau médio, conexo ou não, limitações
- [ ] Referências citadas no texto
- [ ] Apêndice com o link do GitHub

### GitHub (público)
- [ ] Relatório, fontes, grafo.txt, README
- [ ] Sem API key no repositório
- [ ] Commits de todos os integrantes (vira o "histórico de evolução" da rubrica)

### Apresentação (5 minutos)
- [ ] Problema → modelagem → dados → demo do menu → conexidade → limitações → próxima etapa
