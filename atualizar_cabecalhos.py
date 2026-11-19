#!/usr/bin/env python3
# =====================================================================
# ConexSom — Recomendação Musical por Similaridade entre Artistas
# Arquivo : atualizar_cabecalhos.py
#
# Integrantes:
#   Enrique Cipolla Martins ........ RA 10427834
#   Henrique Ferreira Marciano ..... RA 10439797
#   Pedro Henrique Saraiva Arruda .. RA 10437747
#
# Síntese:
#   Reescreve o "Histórico de alterações" do cabeçalho de cada arquivo .java
#   a partir do histórico REAL do git (data — autor — descrição de cada commit
#   que mexeu no arquivo), no formato exigido no enunciado.
#
#   Uso (na pasta do projeto, depois dos commits):
#     python atualizar_cabecalhos.py
#   Depois, faça um commit com os cabeçalhos atualizados.
#
# Histórico de alterações (data — autor — descrição):
#   19/11/2026 — Henrique Ferreira Marciano — Criação do script (parecer da Parte 2: cabeçalhos completos)
# =====================================================================
import glob
import re
import subprocess

MARCA = "Histórico de alterações (data — autor — descrição):"


def historico(arquivo):
    saida = subprocess.run(
        ["git", "log", "--follow", "--reverse", "--date=format:%d/%m/%Y",
         "--format=%ad — %an — %s", "--", arquivo],
        capture_output=True, text=True, encoding="utf-8").stdout
    return [l for l in saida.splitlines() if l.strip()]


def atualizar(arquivo):
    texto = open(arquivo, encoding="utf-8").read()
    linhas = texto.split("\n")
    # acha a linha do histórico e o fim do bloco (linha de "=====" ou "*/")
    ini = next((i for i, l in enumerate(linhas) if MARCA in l), None)
    if ini is None:
        print(f"{arquivo}: sem bloco de histórico, ignorado")
        return
    prefixo = re.match(r"^(\s*(?://|\*)\s?)", linhas[ini]).group(1)
    fim = ini + 1
    while fim < len(linhas) and not re.match(r"^\s*(//|\*)\s*=+\s*$|^\s*\*/", linhas[fim]):
        fim += 1
    entradas = historico(arquivo)
    if not entradas:
        print(f"{arquivo}: sem commits no git, mantido")
        return
    novas = [f"{prefixo}  {e}" for e in entradas]
    linhas[ini + 1:fim] = novas
    open(arquivo, "w", encoding="utf-8").write("\n".join(linhas))
    print(f"{arquivo}: {len(entradas)} entradas")


for a in sorted(glob.glob("*.java")):
    atualizar(a)
