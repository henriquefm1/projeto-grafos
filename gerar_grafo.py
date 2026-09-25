"""
=====================================================================
ConexSom — Script de COLETA DE DADOS e geração do grafo.txt
Arquivo : gerar_grafo.py   (ferramenta offline; a aplicação é Java)

Integrantes:
  Enrique Cipolla Martins ........ RA 10427834
  Henrique Ferreira Marciano ..... RA 10439797
  Pedro Henrique Saraiva Arruda .. RA 10437747

Síntese:
  Coleta dados REAIS de artistas na API pública do Last.fm, calcula a
  similaridade entre cada par de artistas com uma fórmula definida
  (cosseno sobre as tags de estilo ponderadas por TF-IDF), cria uma aresta quando a
  similaridade atinge o LIMIAR e grava o grafo.txt no formato do
  enunciado (tipo 2: não orientado com peso na aresta).

  Etapas:
    1. Expansão (BFS) a partir de ARTISTAS_SEMENTE usando
       artist.getSimilar, até juntar MAX_ARTISTAS artistas.
    2. Tags de estilo de cada artista com artist.getTopTags
       (contagem de 0 a 100 = relevância da tag para o artista).
       Colaborações ("A & B" com A ou B já coletado) são removidas,
       pois os vértices são artistas individuais.
    3. Peso TF-IDF de cada tag e similaridade do cosseno entre os
       vetores de pesos.
    4. Aresta (i, j) se sim(i, j) >= LIMIAR; peso = sim(i, j).
    5. Estatísticas para calibrar/justificar o LIMIAR no relatório.

  Os dados brutos ficam em dados_lastfm.json (cache + evidência dos
  dados reais, com data da coleta). Rodar de novo reaproveita o cache
  e não chama a API outra vez.

Como rodar (Python 3.9+, sem bibliotecas externas):
  PowerShell:  $env:LASTFM_API_KEY = "sua_chave"
               python gerar_grafo.py
  Só recalcular com outro limiar (sem API):  python gerar_grafo.py 0.55

  A chave NUNCA vai para o código nem para o GitHub: ela é lida da
  variável de ambiente LASTFM_API_KEY.

Histórico de alterações (data — autor — descrição):
  23/09/2026 — Enrique Cipolla Martins — Esqueleto do script
  25/09/2026 — Henrique Ferreira Marciano — Coleta, filtro de tags,
               similaridade do cosseno, limiar e estatísticas (TODOs 21 a 28)
  25/09/2026 — Henrique Ferreira Marciano — Após a 1ª coleta: TF-IDF (tag "mpb"
               dominava o cosseno), corte de relevância 5 -> 1 (descartava
               45 artistas), remoção de colaborações e grafias duplicadas;
               LIMIAR 0,40
=====================================================================
"""

import json
import math
import os
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
from collections import deque
from datetime import datetime

# ---------------------------------------------------------------------
# Parâmetros da coleta (registrar os valores finais no relatório)
# ---------------------------------------------------------------------
URL_BASE = "https://ws.audioscrobbler.com/2.0/"
PASTA = os.path.dirname(os.path.abspath(__file__))
ARQUIVO_CACHE = os.path.join(PASTA, "dados_lastfm.json")
ARQUIVO_GRAFO = os.path.join(PASTA, "grafo.txt")

# Universo: música brasileira (MPB e vizinhanças), a partir destas sementes
ARTISTAS_SEMENTE = ["Djavan", "Caetano Veloso", "Marisa Monte",
                    "Gilberto Gil", "Tim Maia"]
MAX_ARTISTAS = 110        # coleta um pouco mais que 80: alguns saem por falta de tags
SIMILARES_POR_ARTISTA = 10  # quantos similares pedir a cada artista na BFS
MAX_TAGS = 15             # usa só as 15 tags mais relevantes de cada artista
MIN_CONTAGEM_TAG = 1      # ignora tags com relevância 0 (em 100)
MIN_TAGS = 2              # artista com menos tags úteis que isso é descartado

LIMIAR = 0.40             # aresta só se similaridade >= LIMIAR (ver tabela de calibração)
MIN_VERTICES = 80
MIN_ARESTAS = 200

# Tags que NÃO descrevem estilo musical (preferência pessoal, nacionalidade,
# voz). Nacionalidade sairia porque quase todos são brasileiros: ela
# deixaria todo mundo parecido com todo mundo sem dizer nada sobre o som.
TAGS_IGNORADAS = {
    "seen live", "favorites", "favourite", "favorite", "favourites",
    "my favorite", "love", "awesome", "beautiful", "amazing", "good",
    "brazil", "brazilian", "brasil", "brasileiro", "brasileira",
    "musica brasileira", "música brasileira", "brazilian music",
    "portuguese", "portugues", "português", "latin", "latino",
    "male vocalists", "female vocalists", "male vocalist", "female vocalist",
    "singer", "cantor", "cantora", "singer-songwriter", "cantautor",
    "all", "music", "musica", "música", "legend", "classic", "old",
    "female vocal", "brasilian", "nacional", "lusofonia", "vi ao vivo", "11",
}


# ---------------------------------------------------------------------
# Acesso à API
# ---------------------------------------------------------------------
def chamar_api(metodo: str, **params) -> dict:
    """GET na API do Last.fm; tenta 3 vezes em caso de erro de rede."""
    chave = os.environ.get("LASTFM_API_KEY", "").strip()
    if not chave:
        sys.exit("Defina a variável de ambiente LASTFM_API_KEY com a sua chave.")
    params.update({"method": metodo, "api_key": chave, "format": "json",
                   "autocorrect": 1})
    url = URL_BASE + "?" + urllib.parse.urlencode(params)
    for tentativa in range(3):
        try:
            with urllib.request.urlopen(url, timeout=15) as resp:
                dados = json.load(resp)
            time.sleep(0.25)  # respeitar o limite de requisições
            if "error" in dados:
                print(f"  API: {dados.get('message')} ({params.get('artist')})")
                return {}
            return dados
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as e:
            print(f"  erro de rede ({e}); tentando de novo...")
            time.sleep(2 * (tentativa + 1))
    return {}


def buscar_similares(artista: str) -> list:
    dados = chamar_api("artist.getsimilar", artist=artista,
                       limit=SIMILARES_POR_ARTISTA)
    lista = dados.get("similarartists", {}).get("artist", [])
    return [a["name"] for a in lista if a.get("name")]


def buscar_tags_brutas(artista: str) -> list:
    dados = chamar_api("artist.gettoptags", artist=artista)
    lista = dados.get("toptags", {}).get("tag", [])
    return [[t["name"], int(t.get("count", 0))] for t in lista if t.get("name")]


# ---------------------------------------------------------------------
# Etapa 1 e 2 — coleta (com cache em JSON)
# ---------------------------------------------------------------------
def coletar() -> dict:
    """BFS por artist.getSimilar + tags de cada artista. Salva o cache."""
    if os.path.exists(ARQUIVO_CACHE):
        with open(ARQUIVO_CACHE, encoding="utf-8") as f:
            cache = json.load(f)
        print(f"Usando dados já coletados em {cache['data_coleta']} "
              f"({len(cache['artistas'])} artistas). Apague {os.path.basename(ARQUIVO_CACHE)} "
              "para coletar de novo.")
        return cache

    print("Coletando artistas (BFS a partir das sementes)...")
    artistas = []                 # ordem de descoberta
    vistos = set()                # nomes em minúsculas, para evitar duplicatas
    fila = deque(ARTISTAS_SEMENTE)
    for s in ARTISTAS_SEMENTE:
        vistos.add(s.lower())
        artistas.append(s)

    while fila and len(artistas) < MAX_ARTISTAS:
        atual = fila.popleft()
        for similar in buscar_similares(atual):
            if similar.lower() in vistos:
                continue
            vistos.add(similar.lower())
            artistas.append(similar)
            fila.append(similar)
            if len(artistas) >= MAX_ARTISTAS:
                break
        print(f"  {len(artistas)} artistas...")

    print("Coletando tags de cada artista...")
    tags = {}
    for i, a in enumerate(artistas, 1):
        tags[a] = buscar_tags_brutas(a)
        if i % 10 == 0:
            print(f"  {i}/{len(artistas)}")

    cache = {
        "fonte": "Last.fm API (artist.getSimilar, artist.getTopTags)",
        "data_coleta": datetime.now().strftime("%d/%m/%Y %H:%M"),
        "sementes": ARTISTAS_SEMENTE,
        "parametros": {"MAX_ARTISTAS": MAX_ARTISTAS,
                       "SIMILARES_POR_ARTISTA": SIMILARES_POR_ARTISTA},
        "artistas": artistas,
        "tags_brutas": tags,
    }
    with open(ARQUIVO_CACHE, "w", encoding="utf-8") as f:
        json.dump(cache, f, ensure_ascii=False, indent=1)
    print(f"Dados brutos salvos em {os.path.basename(ARQUIVO_CACHE)}")
    return cache


def filtrar_tags(artista: str, brutas: list, nomes_artistas: set) -> dict:
    """Mantém só tags de estilo: {tag: contagem}, no máximo MAX_TAGS.
    Descarta tags da lista TAGS_IGNORADAS, tags que são o nome (ou parte
    do nome) do próprio artista (ex.: "vercillo"), tags com o nome de
    algum artista coletado (ex.: "raul seixas") e tags de relevância baixa."""
    resultado = {}
    for nome, contagem in brutas:
        t = nome.strip().lower()
        if (t in TAGS_IGNORADAS or t in artista.lower() or t in nomes_artistas
                or contagem < MIN_CONTAGEM_TAG or t in resultado):
            continue
        resultado[t] = contagem
        if len(resultado) >= MAX_TAGS:
            break
    return resultado


def eh_colaboracao(nome: str, nomes_artistas: set) -> bool:
    """ "Caetano Veloso & Gilberto Gil" e "Rita Lee, Roberto de Carvalho" são
    colaborações (alguma parte é um artista coletado); "Secos & Molhados"
    é banda e continua."""
    partes = nome.replace(",", "&").split("&")
    if len(partes) < 2:
        return False
    return any(parte.strip().lower() in nomes_artistas for parte in partes)


def chave_nome(nome: str) -> str:
    """Normaliza o nome para achar o mesmo artista com grafias diferentes no
    Last.fm (ex.: "Emílio Santiago"/"Emilio Santiago",
    "Jorge Vercillo"/"Jorge Vercilo"): tira acentos, maiúsculas e letras
    repetidas em sequência."""
    sem_acento = "".join(c for c in unicodedata.normalize("NFD", nome.lower())
                         if unicodedata.category(c) != "Mn")
    resultado = []
    for c in sem_acento:
        if c.isalnum() and (not resultado or resultado[-1] != c):
            resultado.append(c)
    return "".join(resultado)


# ---------------------------------------------------------------------
# Etapa 3 — pesos TF-IDF e fórmula de similaridade
# ---------------------------------------------------------------------
def pesos_tfidf(lista_tags: list) -> list:
    """
    Converte {tag: relevância} em {tag: peso} com TF-IDF:

        peso(a, t) = rel(a, t) · log( N / df(t) )

    N = nº de artistas; df(t) = nº de artistas que têm a tag t.
    Motivo: quase todos os artistas coletados têm "mpb" como tag
    principal (relevância 100). Sem o IDF essa tag domina o cosseno e
    todo mundo fica parecido com todo mundo (na 1ª coleta, centenas de
    pares > 0,90). Com o IDF, tags que TODOS têm pesam ~0 e as que
    diferenciam (samba, soul, rock, bossa nova...) passam a decidir.
    Tags presentes em todos os artistas (log = 0) são removidas.
    """
    n = len(lista_tags)
    df = {}
    for tags in lista_tags:
        for t in tags:
            df[t] = df.get(t, 0) + 1
    resultado = []
    for tags in lista_tags:
        pesos = {}
        for t, rel in tags.items():
            idf = math.log(n / df[t])
            if idf > 0:
                pesos[t] = rel * idf
        resultado.append(pesos)
    return resultado


def similaridade(pesos_a: dict, pesos_b: dict) -> float:
    """
    Similaridade do cosseno entre os vetores TF-IDF de dois artistas:

        sim(a, b) = Σ_t w_a,t · w_b,t / ( ‖w_a‖ · ‖w_b‖ )

    Como os pesos são >= 0, o resultado está em [0, 1]:
    1 = mesmo perfil de estilos, 0 = nenhuma tag em comum.
    """
    comuns = set(pesos_a) & set(pesos_b)
    if not comuns:
        return 0.0
    produto = sum(pesos_a[t] * pesos_b[t] for t in comuns)
    norma_a = math.sqrt(sum(v * v for v in pesos_a.values()))
    norma_b = math.sqrt(sum(v * v for v in pesos_b.values()))
    if norma_a == 0 or norma_b == 0:
        return 0.0
    return produto / (norma_a * norma_b)


# ---------------------------------------------------------------------
# Etapa 4 e 5 — arestas e estatísticas
# ---------------------------------------------------------------------
def montar_arestas(tags: list, limiar: float) -> list:
    arestas = []
    for i in range(len(tags)):
        for j in range(i + 1, len(tags)):
            s = similaridade(tags[i], tags[j])
            if s >= limiar:
                arestas.append((i, j, round(s, 4)))
    return arestas


def estatisticas(n: int, arestas: list) -> dict:
    grau = [0] * n
    vizinhos = [[] for _ in range(n)]
    for v, w, _ in arestas:
        grau[v] += 1
        grau[w] += 1
        vizinhos[v].append(w)
        vizinhos[w].append(v)
    # componentes conexas por BFS
    comp = [-1] * n
    qtd = 0
    for s in range(n):
        if comp[s] != -1:
            continue
        comp[s] = qtd
        fila = deque([s])
        while fila:
            v = fila.popleft()
            for w in vizinhos[v]:
                if comp[w] == -1:
                    comp[w] = qtd
                    fila.append(w)
        qtd += 1
    m = len(arestas)
    return {
        "n": n, "m": m,
        "grau_medio": 2 * m / n if n else 0,
        "densidade": 2 * m / (n * (n - 1)) if n > 1 else 0,
        "isolados": sum(1 for g in grau if g == 0),
        "componentes": qtd,
        "grau": grau,
    }


def gravar_grafo_txt(artistas: list, arestas: list, caminho: str) -> None:
    """Grava no formato do enunciado: tipo 2 (não orientado, peso na aresta)."""
    with open(caminho, "w", encoding="utf-8") as f:
        f.write("2\n")
        f.write(f"{len(artistas)}\n")
        for i, nome in enumerate(artistas):
            nome_limpo = nome.replace('"', "'")   # aspas quebrariam o formato
            f.write(f'{i} "{nome_limpo}"\n')
        f.write(f"{len(arestas)}\n")
        for v, w, p in arestas:
            f.write(f"{v} {w} {p:.4f}\n")


def main() -> None:
    limiar = float(sys.argv[1].replace(",", ".")) if len(sys.argv) > 1 else LIMIAR
    cache = coletar()

    nomes_artistas = {a.lower() for a in cache["artistas"]}

    # Remove colaborações (vértice = artista individual)
    colaboracoes = [a for a in cache["artistas"] if eh_colaboracao(a, nomes_artistas)]
    if colaboracoes:
        print(f"\nColaborações removidas ({len(colaboracoes)}): {', '.join(colaboracoes)}")

    # Remove grafias duplicadas do mesmo artista: fica a grafia com mais
    # tags de estilo (em empate, a 1ª encontrada na BFS)
    escolhido = {}   # chave normalizada -> nome mantido
    for a in cache["artistas"]:
        if a in colaboracoes:
            continue
        k = chave_nome(a)
        n_tags = len(filtrar_tags(a, cache["tags_brutas"].get(a, []), nomes_artistas))
        if k not in escolhido or n_tags > len(filtrar_tags(
                escolhido[k], cache["tags_brutas"].get(escolhido[k], []), nomes_artistas)):
            escolhido[k] = a
    mantidos = set(escolhido.values())
    candidatos = [a for a in cache["artistas"] if a in mantidos]
    duplicados = [a for a in cache["artistas"]
                  if a not in mantidos and a not in colaboracoes]
    if duplicados:
        print(f"Grafias duplicadas removidas ({len(duplicados)}): {', '.join(duplicados)}")

    # Filtra tags de estilo e aplica TF-IDF
    brutas = [filtrar_tags(a, cache["tags_brutas"].get(a, []), nomes_artistas)
              for a in candidatos]
    pesos = pesos_tfidf(brutas)

    # Descarta artistas sem tags suficientes (depois do TF-IDF)
    artistas, tags, descartados = [], [], []
    for a, p in zip(candidatos, pesos):
        if len(p) >= MIN_TAGS:
            artistas.append(a)
            tags.append(p)
        else:
            descartados.append(a)
    if descartados:
        print(f"\nDescartados por ter menos de {MIN_TAGS} tags de estilo "
              f"({len(descartados)}): {', '.join(descartados)}")

    # Tabela de calibração: ajuda a escolher e JUSTIFICAR o limiar
    print("\nCalibração do limiar (n = %d):" % len(artistas))
    print("  limiar |    m | grau médio | densidade | isolados | componentes")
    for l in [0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50, 0.55, 0.60, 0.70, 0.80]:
        e = estatisticas(len(artistas), montar_arestas(tags, l))
        marca = " <- escolhido" if abs(l - limiar) < 1e-9 else ""
        print(f"   {l:.2f}  | {e['m']:4d} | {e['grau_medio']:10.2f} | "
              f"{e['densidade']:9.3f} | {e['isolados']:8d} | {e['componentes']:11d}{marca}")

    arestas = montar_arestas(tags, limiar)
    e = estatisticas(len(artistas), arestas)

    print(f"\nResultado com LIMIAR = {limiar:.2f}")
    print(f"  Vértices (artistas): {e['n']}   Arestas: {e['m']}")
    print(f"  Grau médio: {e['grau_medio']:.2f}   Densidade: {e['densidade']:.3f}")
    print(f"  Isolados: {e['isolados']}   Componentes conexas: {e['componentes']}")
    top = sorted(range(e["n"]), key=lambda v: -e["grau"][v])[:5]
    print("  Mais conectados: " + ", ".join(f"{artistas[v]} ({e['grau'][v]})" for v in top))
    if e["isolados"]:
        iso = [artistas[v] for v in range(e["n"]) if e["grau"][v] == 0]
        print("  Isolados: " + ", ".join(iso))

    ok = e["n"] >= MIN_VERTICES and e["m"] >= MIN_ARESTAS
    if not ok:
        print(f"\nATENÇÃO: meta não atingida (n >= {MIN_VERTICES} e m >= {MIN_ARESTAS}).")
        print("  m baixo -> diminua o limiar;  n baixo -> aumente MAX_ARTISTAS e apague o cache.")
    gravar_grafo_txt(artistas, arestas, ARQUIVO_GRAFO)
    print(f"\ngrafo.txt gravado{' (metas atingidas)' if ok else ''}.")


if __name__ == "__main__":
    main()
