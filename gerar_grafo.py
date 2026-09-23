"""
=====================================================================
ConexSom — Script de COLETA DE DADOS e geração do grafo.txt
Arquivo : coleta/gerar_grafo.py   (ferramenta offline; a aplicação é Java)

Integrantes:
  Enrique Cipolla Martins ........ RA 10427834
  Henrique Ferreira Marciano ..... RA 10439797
  Pedro Henrique Saraiva Arruda .. RA 10437747

Síntese:
  Responde à principal cobrança do professor na Parte 1: "de onde vêm os
  dados reais?". Coleta artistas e atributos de uma fonte pública,
  calcula a similaridade com uma FÓRMULA DEFINIDA, aplica um LIMIAR para
  criar arestas e grava o grafo.txt no formato do enunciado.

  Fonte sugerida: API pública do Last.fm (chave gratuita em
  https://www.last.fm/api/account/create). Métodos úteis:
    - artist.getSimilar  -> lista de artistas relacionados (expande o grafo)
    - artist.getTopTags  -> tags de gênero/estilo com contagem
  Registrem no relatório a fonte, a data da coleta e os parâmetros usados.

Histórico de alterações (data — autor — descrição):
  dd/mm/aaaa — Fulano — Versão inicial
=====================================================================
"""

import json
import time
import urllib.parse
import urllib.request

API_KEY = "COLOQUE_SUA_CHAVE_AQUI"   # TODO(21) não subir a chave real para o GitHub
URL_BASE = "https://ws.audioscrobbler.com/2.0/"

ARTISTAS_SEMENTE = ["Djavan", "Caetano Veloso", "Marisa Monte"]  # TODO(22) escolher o universo
MIN_VERTICES = 80
MIN_ARESTAS = 200
LIMIAR = 0.30        # TODO(23) calibrar: aresta só se similaridade >= LIMIAR


def chamar_api(metodo: str, **params) -> dict:
    """Faz uma chamada GET à API e devolve o JSON como dict."""
    params.update({"method": metodo, "api_key": API_KEY, "format": "json"})
    url = URL_BASE + "?" + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=15) as resp:
        dados = json.load(resp)
    time.sleep(0.25)  # respeitar o limite de requisições
    return dados


def coletar_artistas() -> list[str]:
    """
    TODO(24) A partir das sementes, fazer uma BFS usando artist.getSimilar
    até juntar pelo menos MIN_VERTICES artistas (sugestão: 90–120, porque
    alguns podem sair por falta de tags). Evitar duplicatas.
    """
    return []


def coletar_tags(artista: str) -> dict[str, int]:
    """
    TODO(25) Chamar artist.getTopTags e devolver {tag: contagem}.
    Filtrar tags que não são gênero/estilo ("seen live", "favorites", nomes
    de países...). Guardar tudo num JSON (cache) para não recoletar e para
    anexar ao GitHub como evidência dos dados reais.
    """
    return {}


def similaridade(tags_a: dict[str, int], tags_b: dict[str, int]) -> float:
    """
    FÓRMULA DE SIMILARIDADE — o professor pediu que seja definida
    matematicamente. Sugestão (escolham e JUSTIFIQUEM no relatório):

      Jaccard(A, B) = |A ∩ B| / |A ∪ B|          (sobre o conjunto de tags)

    ou, usando as contagens, similaridade do cosseno:

      cos(a, b) = Σ a_t·b_t / (‖a‖·‖b‖)

    Se forem manter BPM (a proposta original cita), é preciso uma fonte
    real de BPM por artista (ex.: média do tempo das músicas num dataset
    público) e combinar, por exemplo:
      sim = 0,7·cos(tags) + 0,3·(1 − min(|bpm_a − bpm_b| / 60, 1))
    Resultado sempre em [0, 1].

    TODO(26) Implementar a fórmula escolhida.
    """
    return 0.0


def gravar_grafo_txt(artistas: list[str], arestas: list[tuple[int, int, float]],
                     caminho: str = "grafo.txt") -> None:
    """Grava no formato do enunciado: tipo 2 (não orientado, peso na aresta)."""
    with open(caminho, "w", encoding="utf-8") as f:
        f.write("2\n")
        f.write(f"{len(artistas)}\n")
        for i, nome in enumerate(artistas):
            f.write(f'{i} "{nome}"\n')
        f.write(f"{len(arestas)}\n")
        for v, w, p in arestas:
            f.write(f"{v} {w} {p:.4f}\n")


def main() -> None:
    artistas = coletar_artistas()
    tags = {a: coletar_tags(a) for a in artistas}

    # TODO(27) Montar as arestas: para cada par (i < j), calcular a
    #   similaridade e criar aresta se >= LIMIAR.
    arestas: list[tuple[int, int, float]] = []

    # TODO(28) Conferir as metas antes de gravar e ajustar o LIMIAR:
    #   - n >= 80 e m >= 200
    #   - poucos (ou nenhum) artistas isolados
    #   - grafo não pode virar "todo mundo ligado a todo mundo"
    #   Reportar no relatório: n, m, grau médio, limiar final.
    print(f"Vértices: {len(artistas)} | Arestas: {len(arestas)}")

    gravar_grafo_txt(artistas, arestas)


if __name__ == "__main__":
    main()
