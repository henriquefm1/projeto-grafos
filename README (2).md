# ConexSom — Recomendação Musical por Similaridade entre Artistas

Projeto da disciplina **Teoria dos Grafos** (Universidade Presbiteriana Mackenzie, 6º D — Prof. Dr. Ivan Carlos Alcântara de Oliveira), Parte 2.

| Integrante | RA |
| --- | --- |
| Enrique Cipolla Martins | 10427834 |
| Henrique Ferreira Marciano | 10439797 |
| Pedro Henrique Saraiva Arruda | 10437747 |

## Sobre o projeto

O ConexSom modela artistas da música brasileira como um **grafo de similaridade**: cada vértice é um artista e cada aresta liga dois artistas com estilos parecidos, com peso igual ao grau de similaridade. O objetivo final é recomendar artistas a partir de um artista que o usuário já conhece.

- **Dados reais:** API pública do Last.fm (artistas similares e tags de estilo), coletados em 25/09/2026.
- **Similaridade:** cosseno entre os vetores de tags ponderadas por TF-IDF.
- **Aresta:** criada quando a similaridade é maior ou igual a 0,40.
- **Categoria do grafo:** 2 (não orientado com peso na aresta).
- **Resultado:** 103 artistas, 594 arestas, grau médio 11,53, 11 componentes conexas.

Os detalhes da coleta, da modelagem, dos testes e da análise estão no relatório.

## Estrutura do repositório

| Arquivo | Conteúdo |
| --- | --- |
| `Relatorio_Parte2_ConexSom.docx` | relatório do projeto |
| `grafo.txt` | grafo modelado (formato do enunciado) |
| `dados_lastfm.json` | dados brutos da coleta no Last.fm (evidência dos dados reais) |
| `gerar_grafo.py` | coleta, limpeza, TF-IDF, cosseno, calibração do limiar e geração do `grafo.txt` |
| `TGrafo.java` | grafo em matriz de adjacência: inserir/remover vértice e aresta, busca, grau, exibição |
| `ArquivoGrafo.java` | leitura, gravação e exibição formatada do `grafo.txt` |
| `Conexidade.java` | conexo/desconexo (BFS), categoria C0–C3, FCONEX e grafo reduzido |
| `ConexSom.java` | menu de opções a) a j) |
| `TesteTGrafo.java` | 63 testes automáticos |
| `grafo_conexsom.png` | visualização do grafo |

## Como executar

Requisitos: Java 11+ e Python 3.9+ (sem bibliotecas externas).

```
javac -encoding UTF-8 -d bin *.java
java -cp bin TesteTGrafo
java -cp bin ConexSom
```

No PowerShell, para os acentos aparecerem corretamente:

```
chcp 65001
java "-Dstdout.encoding=UTF-8" -cp bin ConexSom
```

Para gerar o `grafo.txt` de novo a partir dos dados já coletados (não precisa de chave):

```
python gerar_grafo.py
```

Para testar outro limiar: `python gerar_grafo.py 0.6`. Para uma nova coleta, apague `dados_lastfm.json` e defina a chave da API do Last.fm na variável de ambiente `LASTFM_API_KEY` (a chave nunca é salva no repositório).

## Menu da aplicação

```
a) Ler dados do arquivo grafo.txt
b) Gravar dados no arquivo grafo.txt
c) Inserir vértice (artista)
d) Inserir aresta (similaridade)
e) Remover vértice (artista)
f) Remover aresta (similaridade)
g) Mostrar conteúdo do arquivo
h) Mostrar grafo (matriz de adjacência)
i) Apresentar a conexidade do grafo e o reduzido
j) Encerrar a aplicação
```

Artistas podem ser informados pelo índice ou pelo nome.

## Próxima etapa

Adicionar ao menu a recomendação de artistas: ranqueamento dos vizinhos pelo peso, busca até profundidade 2 e caminho de maior similaridade (Dijkstra com custo −log(sim)).
