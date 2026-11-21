"""Gera os retratos dos personagens (assets/personagens/*.png) com Pillow.

Uso (na raiz do projeto):   python tools/gerar_retratos.py [--folha]
Precisa de: Python 3, Pillow e numpy.  --folha também grava tools/folha_de_retratos.png para conferir o elenco.
Não é necessário para jogar: as imagens já estão prontas em assets/personagens.
Para criar um personagem novo: escreva uma função como as de baixo, acrescente-a em PERSONAGENS, rode o script
e cadastre o id em view/fx/Personagens.java (o teste NarrativaTest avisa se alguém fala sem ter rosto).
"""
import math, random, sys
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageChops

W, H = 560, 700            # desenhado em 2x; salvo em 280x350
RAIZ = Path(__file__).resolve().parent.parent
SAIDA = str(RAIZ / "assets" / "personagens") + "/"
CX = 280

# ---------------------------------------------------------------- utilitários
def camada(): return Image.new("RGBA", (W, H), (0, 0, 0, 0))
def comp(base, layer): return Image.alpha_composite(base, layer)
def rgba(c, a=255): return (c[0], c[1], c[2], a)

def traco(img, fn, blur=0):
    """Desenha algo translúcido numa camada à parte e mistura (ImageDraw direto em RGBA substitui o pixel, não mistura)."""
    l = camada(); fn(ImageDraw.Draw(l))
    if blur: l = l.filter(ImageFilter.GaussianBlur(blur))
    return comp(img, l)

def mascara(pts):
    m = Image.new("L", (W, H), 0); ImageDraw.Draw(m).polygon(pts, fill=255); return m

def recortar(layer, m):
    out = layer.copy(); out.putalpha(ImageChops.multiply(layer.getchannel("A"), m)); return out

def sombra(base, m, desenhar, cor, alpha, blur):
    """Sombra/luz suave dentro de uma máscara (ex.: dentro do rosto)."""
    l = camada(); d = ImageDraw.Draw(l); desenhar(d, rgba(cor, alpha))
    return comp(base, recortar(l.filter(ImageFilter.GaussianBlur(blur)), m))

def pts_cabeca(cx, cy, rx, ry, n=2.2, queixo=0.12, k=140):
    pts = []
    for i in range(k):
        t = 2 * math.pi * i / k; c, s = math.cos(t), math.sin(t)
        x = rx * math.copysign(abs(c) ** (2 / n), c); y = ry * math.copysign(abs(s) ** (2 / n), s)
        if y > 0: x *= (1 - queixo * (y / ry) ** 2)
        pts.append((cx + x, cy + y))
    return pts

def escurecer(c, f): return tuple(max(0, min(255, int(v * f))) for v in c)

# ---------------------------------------------------------------- fundo e moldura
def fundo(cor_topo, cor_base):
    arr = np.zeros((H, W, 4), np.uint8)
    t = np.linspace(0, 1, H)[:, None]
    for i in range(3): arr[..., i] = (cor_topo[i] + (cor_base[i] - cor_topo[i]) * t).astype(np.uint8)
    arr[..., 3] = 255
    img = Image.fromarray(arr, "RGBA")
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    brilho = np.exp(-(((xx - W * 0.35) / 260) ** 2 + ((yy - H * 0.30) / 300) ** 2))[..., None]
    a = np.asarray(img, np.float32); a[..., :3] = np.clip(a[..., :3] + brilho * 38, 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")

def moldura(img):
    m = Image.new("L", (W, H), 0); ImageDraw.Draw(m).rounded_rectangle([0, 0, W - 1, H - 1], 56, fill=255)
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0)); out.paste(img, (0, 0), m)
    d = ImageDraw.Draw(out); d.rounded_rectangle([4, 4, W - 5, H - 5], 54, outline=(230, 184, 74, 215), width=7)
    return out

# ---------------------------------------------------------------- partes do corpo
class P:
    """Parâmetros de um personagem."""
    def __init__(s, **k): s.__dict__.update(k)

def desenhar_corpo(img, p):
    d = ImageDraw.Draw(img)
    pele, pele_sombra = p.pele, escurecer(p.pele, 0.78)
    # pescoço
    d.rounded_rectangle([CX - p.pescoco, 400, CX + p.pescoco, 560], 28, fill=rgba(pele_sombra))
    # tronco (ombros arredondados)
    d.ellipse([CX - p.ombros, 520, CX + p.ombros, 1000], fill=rgba(p.roupa))
    return img

def desenhar_cabeca(img, p):
    cy = 300
    pts = pts_cabeca(CX, cy, p.rx, p.ry, p.quadrado, p.queixo)
    m = mascara(pts)
    d = ImageDraw.Draw(img)
    # orelhas
    for sx in (-1, 1):
        d.ellipse([CX + sx * (p.rx - 6) - 20, cy - 10, CX + sx * (p.rx - 6) + 20, cy + 56], fill=rgba(escurecer(p.pele, 0.92)))
    d.polygon(pts, fill=rgba(p.pele))
    # volume: sombra do lado direito + sob o queixo + luz na testa
    img = sombra(img, m, lambda dd, c: dd.ellipse([CX + 30, cy - 160, CX + 260, cy + 220], fill=c), escurecer(p.pele, 0.55), 95, 38)
    img = sombra(img, m, lambda dd, c: dd.ellipse([CX - 200, cy + p.ry - 70, CX + 200, cy + p.ry + 90], fill=c), escurecer(p.pele, 0.5), 90, 30)
    img = sombra(img, m, lambda dd, c: dd.ellipse([CX - 120, cy - p.ry + 10, CX + 40, cy - 30], fill=c), (255, 245, 225), 55, 30)
    return img, m

def olhos(img, p, cy=292):
    d = ImageDraw.Draw(img)
    for sx in (-1, 1):
        ex = CX + sx * 56; ab = p.olho_abertura
        d.ellipse([ex - 27, cy - ab, ex + 27, cy + ab], fill=(244, 240, 232, 255))
        ix = ex + sx * 2
        d.ellipse([ix - 11, cy - 11, ix + 11, cy + 11], fill=rgba(p.iris))
        d.ellipse([ix - 5.5, cy - 5.5, ix + 5.5, cy + 5.5], fill=(14, 12, 12, 255))
        d.ellipse([ix + 2, cy - 8, ix + 7, cy - 3], fill=(255, 255, 255, 235))
        # pálpebra superior
        d.arc([ex - 29, cy - ab - 4, ex + 29, cy + ab], 195, 345, fill=rgba(escurecer(p.pele, 0.36)), width=5)
        if p.caida:
            d.polygon([(ex - 28, cy - ab - 2), (ex + 28, cy - ab - 2), (ex + 28, cy - ab + p.caida), (ex - 28, cy - ab + p.caida * 0.4)] if sx == -1 else
                      [(ex - 28, cy - ab - 2), (ex + 28, cy - ab - 2), (ex + 28, cy - ab + p.caida * 0.4), (ex - 28, cy - ab + p.caida)], fill=rgba(escurecer(p.pele, 0.82)))
        if p.olheiras:
            l = camada(); ImageDraw.Draw(l).ellipse([ex - 30, cy + ab - 4, ex + 30, cy + ab + 26], fill=(90, 60, 90, 90))
            img = comp(img, l.filter(ImageFilter.GaussianBlur(7))); d = ImageDraw.Draw(img)
    return img

def sobrancelhas(img, p, cy=262):
    d = ImageDraw.Draw(img); cor = rgba(p.cor_sobrancelha)
    for sx in (-1, 1):
        x0, x1 = CX + sx * 22, CX + sx * 92          # interna, externa
        y_int, y_ext = cy - p.sobr_inclinacao, cy + p.sobr_inclinacao
        esp = p.sobr_espessura
        d.polygon([(x0, y_int - esp), (x1, y_ext - esp * 0.7), (x1, y_ext + esp * 0.7), (x0, y_int + esp)], fill=cor)
    return img

def nariz_boca(img, p, cy=292):
    cor_nariz = escurecer(p.pele, 0.70)
    img = traco(img, lambda d: d.polygon([(CX - 4, cy + 14), (CX + 12, cy + 72), (CX - 18, cy + 76), (CX - 10, cy + 60)],
                                         fill=rgba(escurecer(p.pele, 0.80), 70)), blur=3)
    img = traco(img, lambda d: d.arc([CX - 24, cy + 62, CX + 24, cy + 88], 20, 160, fill=rgba(cor_nariz, 150), width=4))
    img = traco(img, lambda d: d.ellipse([CX - 8, cy + 48, CX + 4, cy + 62], fill=(255, 245, 230, 60)), blur=3)
    d = ImageDraw.Draw(img)
    my = cy + 114
    if p.boca == "sorriso":
        d.arc([CX - 52, my - 34, CX + 52, my + 18], 18, 162, fill=rgba(p.cor_labio), width=6)
    elif p.boca == "sorriso_torto":
        d.arc([CX - 48, my - 30, CX + 56, my + 14], 20, 150, fill=rgba(p.cor_labio), width=6)
        d.line([(CX + 50, my - 2), (CX + 60, my - 12)], fill=rgba(p.cor_labio), width=5)
    elif p.boca == "serio":
        d.line([(CX - 44, my), (CX + 44, my + 2)], fill=rgba(p.cor_labio), width=7)
    elif p.boca == "preocupado":
        d.arc([CX - 44, my - 6, CX + 44, my + 40], 200, 340, fill=rgba(p.cor_labio), width=6)
    elif p.boca == "fechado_leve":
        d.arc([CX - 44, my - 16, CX + 44, my + 14], 25, 155, fill=rgba(p.cor_labio), width=6)
    return img

# ================================================================ cabelo e acessórios genéricos
def cabelo_na_regiao(img, p, regiao, cor, expandir=1.07, brilho=True):
    """Pinta cabelo/boné dentro de uma região, limitado ao contorno (ligeiramente expandido) da cabeça."""
    m_exp = mascara(pts_cabeca(CX, 300, p.rx * expandir, p.ry * (expandir - 0.01), p.quadrado, p.queixo))
    m_reg = mascara(regiao)
    m = ImageChops.multiply(m_exp, m_reg)
    l = camada(); ImageDraw.Draw(l).rectangle([0, 0, W, H], fill=rgba(cor))
    img = comp(img, recortar(l, m))
    if brilho:
        img = sombra(img, m, lambda d, c: d.ellipse([CX - 130, 120, CX + 10, 230], fill=c), (255, 255, 255), 60, 26)
        img = sombra(img, m, lambda d, c: d.ellipse([CX + 40, 130, CX + 260, 330], fill=c), (0, 0, 0), 70, 34)
    return img

def regiao_topo(y):      # tudo acima de y, na largura total
    return [(0, 0), (W, 0), (W, y), (0, y)]

def estampar_barba(img, m_rosto, y_min, cor, densidade, alpha, seed=3):
    rng = np.random.default_rng(seed)
    ruido = (rng.random((H, W)) < densidade).astype(np.uint8) * 255
    mm = Image.fromarray(ruido, "L").filter(ImageFilter.GaussianBlur(0.8))
    mm = mm.point(lambda v: 255 if v > 70 else 0)
    corte = Image.new("L", (W, H), 0); ImageDraw.Draw(corte).rectangle([0, y_min, W, H], fill=255)
    mm = ImageChops.multiply(ImageChops.multiply(mm, corte), m_rosto)
    l = camada(); ImageDraw.Draw(l).rectangle([0, 0, W, H], fill=rgba(cor, alpha))
    return comp(img, recortar(l, mm))

def oculos_redondos(img, cor, raio=41, y=292, espessura=7, cor_lente=(180, 210, 230, 38)):
    l = camada(); d = ImageDraw.Draw(l)
    for sx in (-1, 1):
        cx = CX + sx * 56
        d.ellipse([cx - raio, y - raio, cx + raio, y + raio], fill=cor_lente)
        d.ellipse([cx - raio, y - raio, cx + raio, y + raio], outline=rgba(cor), width=espessura)
        d.arc([cx - raio + 12, y - raio + 12, cx + raio - 12, y + raio - 12], 205, 255, fill=(255, 255, 255, 170), width=4)
    d.line([(CX - 56 + raio - 2, y - 4), (CX + 56 - raio + 2, y - 4)], fill=rgba(cor), width=espessura)
    d.line([(CX - 56 - raio, y - 4), (CX - 110, y - 8)], fill=rgba(cor), width=espessura - 1)
    d.line([(CX + 56 + raio, y - 4), (CX + 110, y - 8)], fill=rgba(cor), width=espessura - 1)
    return comp(img, l)

def oculos_retangulares(img, cor, y=292):
    l = camada(); d = ImageDraw.Draw(l)
    for sx in (-1, 1):
        cx = CX + sx * 56
        d.rounded_rectangle([cx - 44, y - 30, cx + 44, y + 28], 10, fill=(190, 215, 235, 30), outline=rgba(cor), width=4)
    d.line([(CX - 12, y - 8), (CX + 12, y - 8)], fill=rgba(cor), width=4)
    return comp(img, l)

def bigode(img, cor, y, largura=62, altura=22, grosso=1.0):
    l = camada(); d = ImageDraw.Draw(l); a = altura * grosso
    d.polygon([(CX - largura, y + a * 0.7), (CX - largura * 0.55, y - a * 0.35), (CX, y), (CX + largura * 0.55, y - a * 0.35),
               (CX + largura, y + a * 0.7), (CX + largura * 0.55, y + a * 1.1), (CX, y + a * 0.55), (CX - largura * 0.55, y + a * 1.1)], fill=rgba(cor))
    return comp(img, l.filter(ImageFilter.GaussianBlur(1.2)))

# ================================================================ VALTER — três origens
def valter_professor():
    p = P(pele=(232, 196, 164), rx=112, ry=138, quadrado=2.2, queixo=0.13, pescoco=46, ombros=262, roupa=(150, 108, 62),
          iris=(92, 70, 48), sobr_inclinacao=5, sobr_espessura=7, cor_sobrancelha=(118, 110, 100), boca="fechado_leve",
          cor_labio=(150, 88, 78), olho_abertura=15, caida=7, olheiras=True)
    img = fundo((92, 62, 40), (36, 24, 18))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.polygon([(CX - 78, 520), (CX + 78, 520), (CX, 660)], fill=(238, 236, 228, 255))               # camisa branca no decote em V
    d.polygon([(CX - 78, 520), (CX - 30, 548), (CX - 52, 592)], fill=(250, 248, 242, 255))           # gola
    d.polygon([(CX + 78, 520), (CX + 30, 548), (CX + 52, 592)], fill=(250, 248, 242, 255))
    d.polygon([(CX - 14, 560), (CX + 14, 560), (CX + 10, 585), (CX - 10, 585)], fill=(52, 104, 78, 255))   # gravata
    d.polygon([(CX - 10, 585), (CX + 10, 585), (CX + 24, 700), (CX - 24, 700)], fill=(60, 120, 88, 255))
    for sx in (-1, 1): d.line([(CX + sx * 78, 520), (CX + sx * 6, 668)], fill=(104, 72, 40, 255), width=7)   # contorno do colete
    img, m = desenhar_cabeca(img, p)
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    cinza = (150, 148, 146)
    img = cabelo_na_regiao(img, p, [(0, 150), (CX - 62, 150), (CX - 76, 248), (CX - 98, 360), (0, 360)], cinza)   # só nas laterais: entradas
    img = cabelo_na_regiao(img, p, [(CX + 62, 150), (W, 150), (W, 360), (CX + 98, 360), (CX + 76, 248)], cinza)
    img = cabelo_na_regiao(img, p, [(CX - 70, 100), (CX + 70, 100), (CX + 70, 176), (CX - 70, 176)], (168, 164, 158))   # fios ralos no topo
    img = bigode(img, (160, 156, 150), 392, 58, 20)
    img = oculos_redondos(img, (46, 38, 32))
    return img, p

def valter_industrial():
    p = P(pele=(214, 164, 124), rx=118, ry=134, quadrado=2.7, queixo=0.07, pescoco=58, ombros=274, roupa=(44, 56, 74),
          iris=(70, 58, 46), sobr_inclinacao=-8, sobr_espessura=12, cor_sobrancelha=(48, 38, 32), boca="serio",
          cor_labio=(130, 74, 66), olho_abertura=13, caida=9, olheiras=True)
    img = fundo((86, 58, 34), (30, 22, 20))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.polygon([(CX - 150, 560), (CX - 70, 520), (CX - 40, 700), (CX - 170, 700)], fill=(232, 118, 30, 255))   # colete refletivo laranja
    d.polygon([(CX + 150, 560), (CX + 70, 520), (CX + 40, 700), (CX + 170, 700)], fill=(232, 118, 30, 255))
    for yy in (600, 650): 
        d.line([(CX - 160, yy), (CX - 48, yy + 4)], fill=(225, 228, 232, 255), width=12)
        d.line([(CX + 160, yy), (CX + 48, yy + 4)], fill=(225, 228, 232, 255), width=12)
    img, m = desenhar_cabeca(img, p)
    img = estampar_barba(img, m, 380, (30, 24, 22), 0.16, 175)                      # barba por fazer
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    img = traco(img, lambda dd: dd.line([(CX - 82, 330), (CX - 54, 372)], fill=(200, 130, 110, 190), width=4), blur=0.6)   # cicatriz
    # capacete amarelo
    l = camada(); dl = ImageDraw.Draw(l)
    dl.pieslice([CX - 142, 118, CX + 142, 430], 180, 360, fill=(238, 196, 40, 255))
    dl.rounded_rectangle([CX - 160, 236, CX + 160, 262], 12, fill=(214, 172, 28, 255))
    dl.rounded_rectangle([CX - 18, 118, CX + 18, 232], 8, fill=(250, 214, 80, 255))
    l = l.filter(ImageFilter.GaussianBlur(0.6))
    img = comp(img, l)
    mh = Image.new("L", (W, H), 0); dm = ImageDraw.Draw(mh)
    dm.pieslice([CX - 142, 118, CX + 142, 430], 180, 360, fill=255); dm.rounded_rectangle([CX - 160, 236, CX + 160, 262], 12, fill=255)
    img = sombra(img, mh, lambda dd, c: dd.ellipse([CX + 20, 110, CX + 200, 300], fill=c), (90, 60, 0), 90, 30)
    return img, p

def valter_precavido():
    p = P(pele=(238, 214, 192), rx=108, ry=140, quadrado=2.1, queixo=0.16, pescoco=42, ombros=252, roupa=(222, 226, 230),
          iris=(60, 92, 112), sobr_inclinacao=9, sobr_espessura=7, cor_sobrancelha=(96, 80, 66), boca="preocupado",
          cor_labio=(166, 98, 92), olho_abertura=18, caida=0, olheiras=False)
    img = fundo((34, 86, 92), (14, 38, 44))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.polygon([(CX - 86, 520), (CX + 86, 520), (CX, 640)], fill=(52, 58, 66, 255))                      # camisa escura
    d.polygon([(CX - 86, 520), (CX - 20, 560), (CX - 108, 700), (CX - 190, 700)], fill=(240, 243, 246, 255))   # lapelas do jaleco
    d.polygon([(CX + 86, 520), (CX + 20, 560), (CX + 108, 700), (CX + 190, 700)], fill=(240, 243, 246, 255))
    d.line([(CX, 640), (CX, 700)], fill=(168, 174, 182, 255), width=5)
    # máscara de gás pendurada no peito
    l = camada(); dl = ImageDraw.Draw(l)
    dl.line([(CX - 70, 520), (CX - 50, 640)], fill=(40, 46, 40, 255), width=9)
    dl.line([(CX + 70, 520), (CX + 50, 640)], fill=(40, 46, 40, 255), width=9)
    dl.rounded_rectangle([CX - 78, 626, CX + 78, 700], 34, fill=(78, 90, 78, 255))
    for sx in (-1, 1): dl.ellipse([CX + sx * 74 - 26, 640, CX + sx * 74 + 26, 692], fill=(58, 66, 58, 255), outline=(150, 160, 140, 255), width=6)
    dl.ellipse([CX - 30, 640, CX + 30, 692], fill=(46, 56, 50, 255), outline=(140, 150, 134, 255), width=5)
    img = comp(img, l)
    img, m = desenhar_cabeca(img, p)
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    img = cabelo_na_regiao(img, p, regiao_topo(214), (82, 64, 50))
    img = cabelo_na_regiao(img, p, [(CX + 20, 150), (W, 150), (W, 300), (CX + 100, 300), (CX + 94, 240)], (82, 64, 50))
    ImageDraw.Draw(img).line([(CX - 30, 168), (CX - 8, 214)], fill=(70, 56, 44, 255), width=5)           # risca do cabelo
    # óculos de proteção levantados na testa
    l = camada(); dl = ImageDraw.Draw(l)
    dl.rounded_rectangle([CX - 118, 188, CX + 118, 214], 10, fill=(34, 52, 78, 255))                   # tira
    for sx in (-1, 1):
        dl.rounded_rectangle([CX + sx * 56 - 50, 168, CX + sx * 56 + 50, 232], 24, fill=(150, 220, 226, 90), outline=(30, 40, 52, 255), width=9)
        dl.arc([CX + sx * 56 - 34, 176, CX + sx * 56 + 34, 224], 200, 250, fill=(255, 255, 255, 180), width=5)
    img = comp(img, l)
    return img, p

# ================================================================ ELENCO SECUNDÁRIO
def marisa():
    p = P(pele=(222, 178, 146), rx=100, ry=132, quadrado=2.0, queixo=0.21, pescoco=36, ombros=236, roupa=(44, 118, 126),
          iris=(82, 60, 42), sobr_inclinacao=9, sobr_espessura=6, cor_sobrancelha=(72, 40, 30), boca="preocupado",
          cor_labio=(178, 84, 90), olho_abertura=17, caida=0, olheiras=False)
    img = fundo((98, 52, 88), (38, 22, 40))
    cabelo = (98, 52, 34)
    l = camada(); dl = ImageDraw.Draw(l)
    dl.ellipse([CX - 158, 140, CX + 158, 470], fill=rgba(escurecer(cabelo, 0.8)))                       # cabelo atrás, até os ombros
    dl.rounded_rectangle([CX - 158, 300, CX + 158, 600], 70, fill=rgba(escurecer(cabelo, 0.8)))
    img = comp(img, l.filter(ImageFilter.GaussianBlur(1.2)))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.polygon([(CX - 70, 520), (CX + 70, 520), (CX, 640)], fill=(240, 230, 214, 255))                    # blusa creme no decote
    d.line([(CX - 70, 520), (CX, 650)], fill=(34, 90, 98, 255), width=7); d.line([(CX + 70, 520), (CX, 650)], fill=(34, 90, 98, 255), width=7)
    for yy in (610, 660): d.ellipse([CX + 54, yy, CX + 70, yy + 16], fill=(224, 196, 120, 255))         # botões do cardigã
    img, m = desenhar_cabeca(img, p)
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    d = ImageDraw.Draw(img)
    for sx in (-1, 1):                                                                                     # cílios
        d.arc([CX + sx * 56 - 29, 270, CX + sx * 56 + 29, 312], 195, 345, fill=(40, 24, 20, 255), width=8)
    img = cabelo_na_regiao(img, p, [(0, 100), (W, 100), (W, 196), (CX - 20, 250), (0, 250)], cabelo)     # franja de lado
    img = cabelo_na_regiao(img, p, [(CX + 20, 180), (W, 180), (W, 380), (CX + 112, 380), (CX + 86, 250)], cabelo)
    img = cabelo_na_regiao(img, p, [(0, 180), (CX - 92, 250), (CX - 104, 380), (0, 380)], cabelo)
    d = ImageDraw.Draw(img)
    for sx in (-1, 1): d.ellipse([CX + sx * 100 - 8, 372, CX + sx * 100 + 8, 392], fill=(236, 200, 110, 255))   # brincos
    return img, p

def kadu():
    p = P(pele=(176, 126, 94), rx=104, ry=134, quadrado=2.2, queixo=0.15, pescoco=44, ombros=256, roupa=(116, 120, 130),
          iris=(48, 34, 28), sobr_inclinacao=-2, sobr_espessura=8, cor_sobrancelha=(28, 22, 20), boca="sorriso_torto",
          cor_labio=(120, 66, 60), olho_abertura=14, caida=5, olheiras=False)
    img = fundo((120, 36, 40), (40, 14, 20))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.ellipse([CX - 96, 470, CX + 96, 580], fill=rgba((92, 96, 106)))                                       # capuz do moletom
    d.ellipse([CX - 66, 496, CX + 66, 560], fill=rgba(escurecer(p.pele, 0.78)))
    d.line([(CX - 30, 560), (CX - 36, 650)], fill=(230, 230, 232, 255), width=6); d.line([(CX + 30, 560), (CX + 36, 650)], fill=(230, 230, 232, 255), width=6)
    d.arc([CX - 92, 500, CX + 92, 640], 20, 160, fill=(232, 192, 70, 255), width=9)                         # corrente dourada
    d.ellipse([CX - 14, 618, CX + 14, 646], fill=(236, 198, 76, 255))
    img, m = desenhar_cabeca(img, p)
    img = estampar_barba(img, m, 400, (24, 18, 16), 0.12, 160, seed=5)
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    l = camada(); dl = ImageDraw.Draw(l)
    dl.polygon([(CX - 20, 416), (CX + 20, 416), (CX + 14, 446), (CX - 14, 446)], fill=(24, 18, 16, 225))   # cavanhaque
    img = comp(img, l.filter(ImageFilter.GaussianBlur(1.5)))
    img = bigode(img, (26, 20, 18), 386, 44, 14)
    # gorro vermelho
    l = camada(); dl = ImageDraw.Draw(l)
    dl.pieslice([CX - 128, 70, CX + 128, 420], 180, 360, fill=(150, 40, 48, 255))
    dl.rounded_rectangle([CX - 126, 214, CX + 126, 262], 22, fill=(176, 56, 62, 255))
    for x in range(CX - 120, CX + 122, 26): dl.line([(x, 220), (x, 258)], fill=(130, 30, 38, 255), width=5)
    img = comp(img, l.filter(ImageFilter.GaussianBlur(0.6)))
    d = ImageDraw.Draw(img); d.ellipse([CX + 96, 340, CX + 112, 358], fill=(236, 198, 76, 255))            # brinco
    return img, p

def heitor():
    p = P(pele=(226, 184, 150), rx=116, ry=136, quadrado=2.5, queixo=0.1, pescoco=54, ombros=278, roupa=(30, 48, 88),
          iris=(62, 74, 86), sobr_inclinacao=-4, sobr_espessura=11, cor_sobrancelha=(40, 30, 26), boca="serio",
          cor_labio=(150, 90, 80), olho_abertura=14, caida=4, olheiras=False)
    img = fundo((34, 60, 112), (12, 22, 46))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.polygon([(CX - 82, 520), (CX - 20, 540), (CX - 36, 600)], fill=(44, 66, 112, 255))                    # gola da polo
    d.polygon([(CX + 82, 520), (CX + 20, 540), (CX + 36, 600)], fill=(44, 66, 112, 255))
    d.polygon([(CX - 20, 540), (CX + 20, 540), (CX + 14, 660), (CX - 14, 660)], fill=(22, 36, 70, 255))
    d.line([(CX - 96, 520), (CX - 40, 640)], fill=(212, 168, 50, 255), width=7); d.line([(CX + 96, 520), (CX + 40, 640)], fill=(212, 168, 50, 255), width=7)   # cordão
    d.rounded_rectangle([CX - 52, 636, CX + 52, 716], 10, fill=(238, 236, 228, 255), outline=(212, 168, 50, 255), width=6)   # crachá
    d.ellipse([CX - 20, 650, CX + 20, 690], fill=(50, 70, 120, 255)); d.rectangle([CX - 38, 700, CX + 38, 710], fill=(120, 120, 120, 255))
    img, m = desenhar_cabeca(img, p)
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    cabelo = (34, 26, 24)
    img = cabelo_na_regiao(img, p, regiao_topo(196), cabelo)
    img = cabelo_na_regiao(img, p, [(0, 150), (CX - 88, 196), (CX - 100, 330), (0, 330)], cabelo)
    img = cabelo_na_regiao(img, p, [(CX + 88, 196), (W, 150), (W, 330), (CX + 100, 330)], cabelo)
    img = bigode(img, (46, 34, 28), 392, 68, 26, 1.25)
    return img, p

def enzo():
    p = P(pele=(232, 196, 164), rx=98, ry=128, quadrado=2.0, queixo=0.22, pescoco=38, ombros=222, roupa=(66, 108, 188),
          iris=(66, 104, 60), sobr_inclinacao=3, sobr_espessura=6, cor_sobrancelha=(84, 56, 40), boca="sorriso",
          cor_labio=(170, 96, 92), olho_abertura=18, caida=0, olheiras=False)
    img = fundo((60, 120, 90), (20, 52, 44))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.ellipse([CX - 64, 500, CX + 64, 560], fill=rgba(escurecer(p.pele, 0.8)))                              # gola careca da camiseta
    d.ellipse([CX - 60, 596, CX + 60, 676], outline=(240, 240, 244, 255), width=9)                           # estampa
    d.ellipse([CX - 20, 616, CX + 20, 656], fill=(240, 196, 70, 255))
    img, m = desenhar_cabeca(img, p)
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    img = traco(img, lambda dd: [dd.ellipse([CX + sx * 66 - 22, 336, CX + sx * 66 + 22, 374], fill=(226, 120, 110, 80)) for sx in (-1, 1)], blur=8)   # bochechas
    cabelo = (88, 58, 40)
    img = cabelo_na_regiao(img, p, regiao_topo(206), cabelo)
    l = camada(); dl = ImageDraw.Draw(l)                                                                     # tufos bagunçados
    for x, a in ((-70, 70), (-30, 92), (14, 84), (56, 70), (90, 46)):
        dl.polygon([(CX + x - 28, 190), (CX + x + 6, 190 - a), (CX + x + 32, 196)], fill=rgba(cabelo))
    img = comp(img, l.filter(ImageFilter.GaussianBlur(1.0)))
    img = cabelo_na_regiao(img, p, [(0, 150), (CX - 84, 206), (CX - 94, 290), (0, 290)], cabelo)
    img = cabelo_na_regiao(img, p, [(CX + 84, 206), (W, 150), (W, 290), (CX + 94, 290)], cabelo)
    return img, p

def fidalgo():
    p = P(pele=(196, 156, 124), rx=108, ry=140, quadrado=2.2, queixo=0.18, pescoco=46, ombros=262, roupa=(46, 48, 56),
          iris=(40, 52, 44), sobr_inclinacao=-5, sobr_espessura=6, cor_sobrancelha=(150, 150, 152), boca="serio",
          cor_labio=(130, 84, 76), olho_abertura=12, caida=9, olheiras=False)
    img = fundo((64, 62, 70), (20, 20, 26))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.polygon([(CX - 74, 520), (CX + 74, 520), (CX, 650)], fill=(244, 244, 240, 255))                        # camisa branca
    d.polygon([(CX - 74, 520), (CX - 26, 548), (CX - 48, 596)], fill=(255, 255, 252, 255))
    d.polygon([(CX + 74, 520), (CX + 26, 548), (CX + 48, 596)], fill=(255, 255, 252, 255))
    d.polygon([(CX - 12, 560), (CX + 12, 560), (CX + 8, 584), (CX - 8, 584)], fill=(110, 26, 44, 255))      # gravata bordô
    d.polygon([(CX - 8, 584), (CX + 8, 584), (CX + 22, 700), (CX - 22, 700)], fill=(124, 30, 50, 255))
    d.polygon([(CX - 74, 520), (CX - 18, 580), (CX - 96, 700), (CX - 180, 700)], fill=(36, 38, 44, 255))    # lapelas do paletó
    d.polygon([(CX + 74, 520), (CX + 18, 580), (CX + 96, 700), (CX + 180, 700)], fill=(36, 38, 44, 255))
    d.polygon([(CX + 104, 640), (CX + 150, 636), (CX + 150, 660), (CX + 108, 664)], fill=(240, 240, 236, 255))   # lenço no bolso
    img, m = desenhar_cabeca(img, p)
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    prata = (204, 204, 208)
    img = cabelo_na_regiao(img, p, regiao_topo(222), prata)
    img = cabelo_na_regiao(img, p, [(0, 150), (CX - 94, 222), (CX - 104, 330), (0, 330)], escurecer(prata, 0.88))
    img = cabelo_na_regiao(img, p, [(CX + 94, 222), (W, 150), (W, 330), (CX + 104, 330)], escurecer(prata, 0.88))
    img = traco(img, lambda dd: [dd.arc([CX - 100 + i * 10, 150 + i * 4, CX + 100 + i * 6, 340], 200, 250, fill=(160, 160, 168, 140), width=3) for i in range(7)])   # fios penteados
    l = camada(); dl = ImageDraw.Draw(l)
    dl.polygon([(CX - 44, 408), (CX + 44, 408), (CX + 30, 458), (CX - 30, 458)], fill=rgba(escurecer(prata, 0.9), 235))   # cavanhaque grisalho
    img = comp(img, l.filter(ImageFilter.GaussianBlur(2.0)))
    img = bigode(img, (190, 190, 194), 392, 46, 12)
    l = camada(); dl = ImageDraw.Draw(l)                                                                     # óculos sem aro
    for sx in (-1, 1): dl.rounded_rectangle([CX + sx * 56 - 44, 264, CX + sx * 56 + 44, 322], 22, outline=(196, 200, 206, 235), width=4, fill=(190, 215, 235, 26))
    dl.line([(CX - 12, 284), (CX + 12, 284)], fill=(196, 200, 206, 235), width=4)
    img = comp(img, l)
    return img, p

def maluco():
    p = P(pele=(158, 110, 82), rx=114, ry=134, quadrado=2.6, queixo=0.1, pescoco=62, ombros=282, roupa=(158, 110, 82),
          iris=(40, 30, 26), sobr_inclinacao=-9, sobr_espessura=13, cor_sobrancelha=(24, 18, 16), boca="serio",
          cor_labio=(110, 64, 58), olho_abertura=12, caida=11, olheiras=True)
    img = fundo((150, 34, 38), (46, 12, 16))
    img = desenhar_corpo(img, p)
    d = ImageDraw.Draw(img)
    d.polygon([(CX - 118, 540), (CX - 52, 520), (CX - 40, 700), (CX - 200, 700)], fill=(170, 34, 38, 255))   # regata vermelha
    d.polygon([(CX + 118, 540), (CX + 52, 520), (CX + 40, 700), (CX + 200, 700)], fill=(170, 34, 38, 255))
    d.ellipse([CX - 52, 520, CX + 52, 600], fill=rgba(escurecer(p.pele, 0.8)))
    for i in range(5): d.arc([CX - 50 + i * 6, 430 + i * 14, CX + 20 + i * 4, 500 + i * 14], 20, 200, fill=(24, 18, 22, 235), width=5)   # tatuagem no pescoço
    img, m = desenhar_cabeca(img, p)
    img = sombra(img, m, lambda dd, c: dd.ellipse([CX - 90, 150, CX - 10, 210], fill=c), (255, 255, 255), 100, 18)    # brilho na careca
    img = olhos(img, p); img = sobrancelhas(img, p); img = nariz_boca(img, p)
    l = camada(); dl = ImageDraw.Draw(l)                                                                     # barba cheia
    dl.polygon(pts_cabeca(CX, 300, p.rx * 1.02, p.ry * 1.02, p.quadrado, p.queixo), fill=(26, 20, 18, 255))
    mm = Image.new("L", (W, H), 0); ImageDraw.Draw(mm).polygon([(0, 372), (CX - 100, 380), (CX - 56, 412), (CX, 396), (CX + 56, 412), (CX + 100, 380), (W, 372), (W, H), (0, H)], fill=255)
    img = comp(img, recortar(l, ImageChops.multiply(mm, mascara(pts_cabeca(CX, 300, p.rx * 1.02, p.ry * 1.02, p.quadrado, p.queixo)))).filter(ImageFilter.GaussianBlur(1.2)))
    d = ImageDraw.Draw(img); d.line([(CX - 40, 424), (CX + 40, 428)], fill=(150, 90, 84, 255), width=6)     # boca no meio da barba
    d.ellipse([CX + 100, 346, CX + 122, 368], fill=(236, 198, 76, 255))                                      # brinco
    d.rectangle([CX - 6, 438, CX + 6, 450], fill=(236, 198, 76, 255))                                        # dente de ouro
    return img, p

def desconhecido():
    img = fundo((84, 88, 96), (30, 32, 38))
    d = ImageDraw.Draw(img)
    d.ellipse([CX - 250, 520, CX + 250, 1000], fill=(54, 58, 66, 255))
    d.rounded_rectangle([CX - 46, 400, CX + 46, 560], 28, fill=(44, 48, 56, 255))
    d.polygon(pts_cabeca(CX, 300, 108, 134, 2.2, 0.15), fill=(62, 66, 74, 255))
    from PIL import ImageFont
    f = None
    for caminho in ("DejaVuSans-Bold.ttf", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", "arialbd.ttf",
                    "Arial Bold.ttf", "LiberationSans-Bold.ttf"):
        try:
            f = ImageFont.truetype(caminho, 190); break
        except OSError:
            continue
    if f is None:
        raise SystemExit("Não achei uma fonte em negrito (DejaVu/Arial/Liberation) para desenhar o '?' do retrato desconhecido.")
    d.text((CX, 296), "?", font=f, fill=(150, 156, 166, 255), anchor="mm")
    return img, None

# ================================================================ geração
def salvar(nome, resultado):
    img = resultado[0]
    final = moldura(img).resize((280, 350), Image.LANCZOS)
    final.save(SAIDA + nome + ".png", optimize=True)
    return final

PERSONAGENS = [("valter_professor", valter_professor), ("valter_industrial", valter_industrial), ("valter_precavido", valter_precavido),
               ("marisa", marisa), ("kadu", kadu), ("heitor", heitor), ("enzo", enzo), ("fidalgo", fidalgo), ("maluco", maluco), ("desconhecido", desconhecido)]

if __name__ == "__main__":
    todos = []
    for nome, fn in PERSONAGENS:
        todos.append(salvar(nome, fn())); print("gerado:", nome)
    folha = Image.new("RGBA", (5 * 290 + 10, 2 * 360 + 10), (18, 22, 28, 255))
    for i, im in enumerate(todos):
        folha.alpha_composite(im, (10 + (i % 5) * 290, 10 + (i // 5) * 360))
    if "--folha" in sys.argv:
        folha.convert("RGB").save(str(RAIZ / "tools" / "folha_de_retratos.png"))
