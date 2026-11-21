"""Gera os fundos de cena (assets/fundos/*.jpg, 1280x720) com Pillow.

Uso (na raiz do projeto):   python tools/gerar_fundos.py            (gera todos)
                            python tools/gerar_fundos.py --novas    (só os cenários das Temporadas 2 a 5 e dos finais)
                            python tools/gerar_fundos.py pia fuga   (só os cenários citados)
Precisa de: Python 3, Pillow e numpy. Não é necessário para jogar: as imagens já estão prontas.
Cada cenário usado pela história vira assets/fundos/fundo_<cenario>.jpg; sem imagem, a janela usa o fundo genérico.
"""
import numpy as np, random, math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

W, H = 1280, 720
RAIZ = Path(__file__).resolve().parent.parent
SAIDA = str(RAIZ / "assets" / "fundos") + "/"
YY, XX = np.mgrid[0:H, 0:W].astype(np.float32)

def vgrad(topo, base, curva=1.0):
    t = (YY / (H - 1)) ** curva
    return np.stack([topo[i] + (base[i] - topo[i]) * t for i in range(3)], axis=-1).astype(np.float32)

def brilho(cx, cy, rx, ry, cor, forca=1.0):
    d = np.exp(-(((XX - cx) / rx) ** 2 + ((YY - cy) / ry) ** 2))
    return d[..., None] * np.array(cor, np.float32)[None, None, :] * forca

def camada():
    return Image.new("RGBA", (W, H), (0, 0, 0, 0))

def compor(base, silhueta, suave=0.8):
    s = silhueta.filter(ImageFilter.GaussianBlur(suave))
    a = np.asarray(s, np.float32) / 255.0
    return base * (1 - a[..., 3:4]) + a[..., :3] * 255.0 * a[..., 3:4]

def acabamento(base, nome, grao=5.5, vinheta=0.55, seed=1):
    rng = np.random.default_rng(seed)
    d = np.sqrt(((XX - W / 2) / (W / 2)) ** 2 + ((YY - H / 2) / (H / 2)) ** 2)
    v = 1 - vinheta * np.clip(d - 0.35, 0, 1) ** 1.6
    img = base * v[..., None] + rng.normal(0, grao, (H, W, 1))
    Image.fromarray(np.clip(img, 0, 255).astype(np.uint8)).save(SAIDA + f"fundo_{nome}.jpg", quality=88, optimize=True)

def frasco(d, cx, topo, base_y, largura, cor_vidro=(8, 12, 16, 255), contorno=None):
    pescoco = largura * 0.16
    colo_y = topo + (base_y - topo) * 0.38
    pts = [(cx - pescoco, topo), (cx + pescoco, topo), (cx + pescoco, colo_y),
           (cx + largura / 2, base_y - 14), (cx - largura / 2, base_y - 14), (cx - pescoco, colo_y)]
    d.polygon(pts, fill=cor_vidro)
    d.ellipse([cx - largura / 2, base_y - 34, cx + largura / 2, base_y + 6], fill=cor_vidro)
    d.rectangle([cx - pescoco - 8, topo - 8, cx + pescoco + 8, topo + 6], fill=cor_vidro)
    if contorno:   # luz de contorno: separa o vidro do fundo
        d.line(pts + [pts[0]], fill=contorno, width=3)
        d.arc([cx - largura / 2, base_y - 34, cx + largura / 2, base_y + 6], 0, 180, fill=contorno, width=3)
        d.line([(cx - pescoco - 8, topo - 8), (cx + pescoco + 8, topo - 8)], fill=contorno, width=3)

def pessoa(d, cx, y_chao, altura, cor=(4, 5, 8, 255), rim=(130, 105, 80, 255), lado_da_luz=1, sentada_ate=None):
    """Silhueta de pessoa: cabeça, ombros inclinados, casaco e pernas. Contorno de luz do lado da lâmpada."""
    def forma(dx, dy, c):
        topo = y_chao - altura
        cab_y = topo + altura * 0.085
        ombro = topo + altura * 0.17
        fim_torso = sentada_ate if sentada_ate else y_chao - altura * 0.40
        larg = altura * 0.19
        d.ellipse([cx + dx - altura * 0.062, cab_y + dy - altura * 0.075, cx + dx + altura * 0.062, cab_y + dy + altura * 0.085], fill=c)
        d.rectangle([cx + dx - altura * 0.028, ombro + dy - altura * 0.05, cx + dx + altura * 0.028, ombro + dy + altura * 0.02], fill=c)
        d.polygon([(cx + dx - altura * 0.075, ombro + dy), (cx + dx + altura * 0.075, ombro + dy),
                   (cx + dx + larg, ombro + dy + altura * 0.07), (cx + dx + larg * 1.05, fim_torso + dy),
                   (cx + dx - larg * 1.05, fim_torso + dy), (cx + dx - larg, ombro + dy + altura * 0.07)], fill=c)
        if not sentada_ate:
            d.rectangle([cx + dx - larg * 0.85, fim_torso + dy - 4, cx + dx - larg * 0.07, y_chao + dy], fill=c)
            d.rectangle([cx + dx + larg * 0.07, fim_torso + dy - 4, cx + dx + larg * 0.85, y_chao + dy], fill=c)
    forma(3 * lado_da_luz, -2, rim)   # contorno claro, deslocado para o lado da luz
    forma(0, 0, cor)

# ------------------------------------------------------------------ MENU
def menu():
    rng = random.Random(3)
    base = vgrad((6, 14, 20), (14, 30, 34), 1.2)
    base += brilho(900, 470, 330, 270, (40, 190, 110), 0.55)
    base += brilho(200, 120, 520, 260, (20, 60, 70), 0.35)
    s = camada(); d = ImageDraw.Draw(s)
    x = 0
    while x < W:                                              # horizonte de uma cidade ao longe
        w = rng.randint(26, 78); h = rng.randint(70, 250)
        d.rectangle([x, 560 - h, x + w, 600], fill=(6, 10, 14, 255))
        for jx in range(x + 6, x + w - 6, 12):
            for jy in range(560 - h + 10, 590, 18):
                if rng.random() < 0.13:
                    d.rectangle([jx, jy, jx + 4, jy + 7], fill=(232, 190, 90, 255))
        x += w + rng.randint(0, 6)
    d.rectangle([0, 598, W, H], fill=(5, 8, 11, 255))
    base = compor(base, s)
    s = camada(); d = ImageDraw.Draw(s)
    frasco(d, 900, 230, 640, 330, contorno=(80, 190, 160, 235))   # o frasco: símbolo do jogo
    base = compor(base, s, 1.2)
    liquido = camada(); d = ImageDraw.Draw(liquido)
    d.polygon([(760, 520), (1040, 520), (1062, 610), (738, 610)], fill=(70, 230, 130, 235))
    d.ellipse([738, 596, 1062, 636], fill=(70, 230, 130, 235))
    d.polygon([(760, 520), (1040, 520), (1048, 548), (752, 548)], fill=(150, 255, 190, 235))
    base = compor(base, liquido.filter(ImageFilter.GaussianBlur(2)), 1.0)
    base += brilho(900, 575, 190, 90, (90, 255, 150), 0.7)
    for _ in range(26):                                       # bolhas e vapor subindo
        bx = 900 + rng.gauss(0, 55); by = rng.uniform(150, 540); r = rng.uniform(3, 11)
        base += brilho(bx, by, r * 1.6, r * 1.6, (90, 255, 160), rng.uniform(0.25, 0.7))
    for i in range(7):
        base += brilho(895 + rng.gauss(0, 18), 215 - i * 38, 60 + i * 12, 38 + i * 6, (70, 150, 120), 0.16)
    acabamento(base, "menu", seed=11)

# ------------------------------------------------------------------ GENÉRICO e TÍTULO
def hexagonos(base, cor, forca, seed):
    rng = random.Random(seed); img = Image.new("L", (W, H), 0); d = ImageDraw.Draw(img)
    r = 46
    for q in range(-1, 16):
        for p in range(-1, 11):
            cx = q * r * 1.5; cy = p * r * 1.732 + (r * 0.866 if q % 2 else 0)
            if rng.random() < 0.34:
                pts = [(cx + r * math.cos(math.radians(60 * k)), cy + r * math.sin(math.radians(60 * k))) for k in range(6)]
                d.line(pts + [pts[0]], fill=int(rng.uniform(40, 120)), width=2)
                if rng.random() < 0.4: d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=200)
    a = np.asarray(img.filter(ImageFilter.GaussianBlur(0.8)), np.float32) / 255.0
    return base + a[..., None] * np.array(cor, np.float32) * forca

def generico():
    base = vgrad((8, 13, 20), (12, 20, 26), 1.0) + brilho(W / 2, 330, 520, 300, (22, 70, 66), 0.6)
    acabamento(hexagonos(base, (90, 200, 160), 0.55, 5), "generico", seed=12)

def titulo():
    rng = random.Random(8)
    base = vgrad((5, 9, 13), (9, 18, 20), 1.0) + brilho(W / 2, 380, 430, 230, (36, 170, 100), 0.55)
    for _ in range(46):
        bx = rng.uniform(80, W - 80); by = rng.uniform(80, H - 40); r = rng.uniform(3, 14)
        base += brilho(bx, by, r * 1.5, r * 1.5, (90, 255, 160), rng.uniform(0.12, 0.4))
    acabamento(base, "titulo", seed=13)

# ------------------------------------------------------------------ JANTAR (Cena 1)
def jantar():
    base = vgrad((26, 18, 14), (14, 9, 8), 0.9)
    base += brilho(250, 210, 230, 190, (60, 90, 150), 0.60)             # luar na janela
    base += brilho(700, 330, 520, 320, (255, 170, 80), 0.50)            # luz quente da lâmpada
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([110, 90, 400, 360], fill=(48, 62, 90, 255))            # vidro da janela
    for y in range(100, 355, 22): d.rectangle([110, y, 400, y + 8], fill=(18, 14, 12, 255))   # persiana
    d.rectangle([100, 80, 410, 92], fill=(16, 11, 9, 255)); d.rectangle([100, 360, 410, 372], fill=(16, 11, 9, 255))
    d.rectangle([0, 548, W, H], fill=(20, 12, 9, 255))                  # chão
    d.line([(700, 0), (700, 170)], fill=(12, 8, 6, 255), width=3)       # fio da lâmpada
    d.polygon([(660, 172), (740, 172), (772, 214), (628, 214)], fill=(22, 14, 10, 255))
    base = compor(base, s)
    base += brilho(700, 205, 70, 40, (255, 215, 140), 0.9)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 500, 520, 285, cor=(9, 6, 5, 255), rim=(150, 100, 60, 255), lado_da_luz=1, sentada_ate=520)    # as duas pessoas, sentadas
    pessoa(d, 900, 520, 268, cor=(9, 6, 5, 255), rim=(150, 100, 60, 255), lado_da_luz=-1, sentada_ate=520)
    base = compor(base, s, 1.2)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([410, 470, 990, 550], fill=(34, 20, 13, 255))                         # a mesa com toalha, na frente das pessoas
    d.rectangle([410, 470, 990, 478], fill=(70, 44, 28, 255))
    for px in (585, 815):                                                             # pratos e copos sobre a mesa
        d.ellipse([px - 46, 460, px + 46, 480], fill=(205, 185, 150, 255))
        d.ellipse([px - 30, 463, px + 30, 476], fill=(150, 130, 105, 255))
    d.rounded_rectangle([690, 438, 712, 470], 4, fill=(120, 150, 170, 200))
    base = compor(base, s)
    base += brilho(700, 468, 260, 24, (255, 190, 110), 0.55)
    acabamento(base, "jantar", seed=14)

# ------------------------------------------------------------------ RUA (A Escolha)
def rua():
    base = vgrad((6, 10, 22), (12, 14, 26), 1.0)
    base += brilho(0, 340, 420, 330, (230, 40, 40), 0.65)
    base += brilho(W, 340, 420, 330, (50, 90, 235), 0.65)
    base += brilho(660, 200, 200, 150, (255, 205, 130), 0.55)
    s = camada(); d = ImageDraw.Draw(s); rng = random.Random(9)
    x = 0
    while x < W:
        w = rng.randint(80, 190); h = rng.randint(190, 360)
        d.rectangle([x, 560 - h, x + w, 560], fill=(17, 21, 36, 255))     # prédios mais claros que as pessoas
        for jx in range(x + 12, x + w - 12, 26):
            for jy in range(560 - h + 16, 540, 34):
                if rng.random() < 0.18: d.rectangle([jx, jy, jx + 10, jy + 15], fill=(230, 180, 90, 255))
        x += w + rng.randint(0, 10)
    d.rectangle([0, 556, W, H], fill=(11, 13, 19, 255))
    for k in range(8): d.polygon([(190 + k * 140, 700), (205 + k * 140, 700), (648 + k * 8, 590), (638 + k * 8, 590)], fill=(36, 38, 46, 255))
    d.rectangle([640, 170, 650, 560], fill=(5, 6, 9, 255)); d.rectangle([640, 170, 700, 180], fill=(5, 6, 9, 255))   # poste
    base = compor(base, s)
    base += brilho(690, 190, 60, 36, (255, 235, 190), 1.0)
    base += brilho(660, 560, 330, 110, (120, 90, 130), 0.30)             # névoa no chão, atrás das pessoas
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 520, 655, 400, lado_da_luz=1)                               # Valter e Kadu frente a frente
    pessoa(d, 830, 655, 372, lado_da_luz=-1)
    base = compor(base, s, 1.1)
    acabamento(base, "rua", seed=15)

# ------------------------------------------------------------------ LABORATÓRIO (A Primeira Fornada)
def laboratorio():
    rng = random.Random(4)
    base = vgrad((5, 11, 10), (8, 20, 15), 1.0)
    base += brilho(640, 430, 520, 260, (40, 200, 110), 0.55)
    s = camada(); d = ImageDraw.Draw(s)
    for x in range(0, W, 128): d.line([(x, 0), (x, 520)], fill=(10, 20, 16, 255), width=2)
    d.rectangle([0, 520, W, H], fill=(6, 10, 9, 255))
    d.rectangle([60, 520, W - 60, 560], fill=(11, 18, 15, 255))
    for x in (110, W - 130): d.rectangle([x, 560, x + 22, H], fill=(8, 13, 11, 255))
    d.line([(640, 0), (640, 150)], fill=(8, 12, 10, 255), width=3)
    base = compor(base, s)
    base += brilho(640, 168, 90, 55, (200, 255, 190), 0.85)
    base += brilho(640, 330, 320, 210, (120, 255, 170), 0.20)
    s = camada(); d = ImageDraw.Draw(s)
    for cx, topo, larg in ((260, 330, 150), (460, 380, 110), (820, 340, 160), (1020, 385, 105)):
        frasco(d, cx, topo, 520, larg)
    for cx in (360, 650, 920):                                          # béqueres
        d.rectangle([cx - 36, 440, cx + 36, 520], fill=(8, 12, 16, 255))
    d.rectangle([590, 360, 610, 520], fill=(8, 12, 16, 255)); d.rectangle([590, 360, 760, 378], fill=(8, 12, 16, 255))   # suporte
    base = compor(base, s, 1.1)
    for cx, topo, larg in ((260, 330, 150), (460, 380, 110), (820, 340, 160), (1020, 385, 105)):
        base += brilho(cx, 470, larg * 0.36, 34, (90, 255, 150), 0.85)
    for _ in range(40):
        bx = rng.choice([260, 460, 820, 1020]) + rng.gauss(0, 30); by = rng.uniform(210, 500); r = rng.uniform(2, 7)
        base += brilho(bx, by, r * 1.6, r * 1.6, (110, 255, 170), rng.uniform(0.2, 0.6))
    for i in range(9): base += brilho(640 + rng.gauss(0, 120), 330 - i * 28, 130, 38, (70, 160, 120), 0.10)
    acabamento(base, "laboratorio", seed=16)

# ------------------------------------------------------------------ DESFECHO (final da recusa)
def desfecho():
    base = vgrad((38, 44, 56), (22, 24, 30), 1.0)
    base += brilho(W / 2, 250, 480, 300, (210, 190, 170), 0.45)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([420, 70, 860, 470], fill=(196, 188, 178, 255))         # janela e a luz fria da manhã
    d.rectangle([420, 70, 860, 470], outline=(16, 16, 20, 255), width=14)
    d.rectangle([634, 70, 646, 470], fill=(16, 16, 20, 255)); d.rectangle([420, 266, 860, 278], fill=(16, 16, 20, 255))
    d.rectangle([0, 560, W, H], fill=(14, 15, 19, 255))
    base = compor(base, s)
    base += brilho(640, 470, 300, 80, (220, 205, 180), 0.45)
    s = camada(); d = ImageDraw.Draw(s)
    d.rounded_rectangle([540, 430, 740, 500], 14, fill=(8, 8, 11, 255))                 # cadeira vazia
    d.rectangle([548, 500, 566, 640], fill=(8, 8, 11, 255)); d.rectangle([714, 500, 732, 640], fill=(8, 8, 11, 255))
    d.rounded_rectangle([546, 300, 566, 440], 6, fill=(8, 8, 11, 255)); d.rounded_rectangle([714, 300, 734, 440], 6, fill=(8, 8, 11, 255))
    d.rectangle([546, 296, 734, 330], fill=(8, 8, 11, 255))
    d.rectangle([880, 470, 1000, 640], fill=(8, 8, 11, 255))                              # criado-mudo
    base = compor(base, s, 1.0)
    base += brilho(700, 640, 330, 46, (200, 185, 160), 0.30)     # faixa de luz da janela no chão
    acabamento(base, "desfecho", vinheta=0.6, seed=17)

# ================================================================== TEMPORADAS 2 A 5 E FINAIS
def chao(d, y, cor=(8, 9, 13, 255)):
    d.rectangle([0, y, W, H], fill=cor)

def luz_de_carro(base, cx, cy, cor=(255, 245, 215), forca=1.0):
    base += brilho(cx, cy, 120, 70, cor, forca)            # o clarão do farol
    base += brilho(cx, cy, 26, 18, (255, 255, 255), 1.2)    # o núcleo
    return base

def carro(d, cx, base_y, larg, cor=(5, 6, 9, 255)):
    h = larg * 0.30
    d.rounded_rectangle([cx - larg / 2, base_y - h, cx + larg / 2, base_y], 18, fill=cor)
    d.polygon([(cx - larg * 0.30, base_y - h), (cx - larg * 0.20, base_y - h * 1.65),
               (cx + larg * 0.20, base_y - h * 1.65), (cx + larg * 0.30, base_y - h)], fill=cor)
    for dx in (-0.30, 0.30):
        d.ellipse([cx + larg * dx - 26, base_y - 22, cx + larg * dx + 26, base_y + 22], fill=(2, 2, 4, 255))

# ------------------------------------------------------------------ FURGÃO ENCURRALADO
def furgao():
    base = vgrad((5, 9, 20), (10, 12, 22), 1.0)
    base += brilho(640, 300, 380, 240, (40, 70, 120), 0.35)
    s = camada(); d = ImageDraw.Draw(s); rng = random.Random(21)
    d.rectangle([0, 0, 250, 600], fill=(14, 16, 24, 255)); d.rectangle([1030, 0, W, 600], fill=(14, 16, 24, 255))   # paredes do beco
    for y in range(20, 600, 34):
        for x in range(0, 250, 62): d.rectangle([x + (31 if (y // 34) % 2 else 0), y, x + 58 + (31 if (y // 34) % 2 else 0), y + 28], outline=(9, 10, 16, 255), width=2)
        for x in range(1030, W, 62): d.rectangle([x + (31 if (y // 34) % 2 else 0), y, x + 58 + (31 if (y // 34) % 2 else 0), y + 28], outline=(9, 10, 16, 255), width=2)
    chao(d, 600)
    d.rectangle([520, 330, 800, 560], fill=(11, 14, 20, 255)); d.rectangle([780, 380, 880, 560], fill=(11, 14, 20, 255))   # o furgão ao fundo
    d.rectangle([548, 360, 640, 420], fill=(40, 70, 60, 255))
    for cx in (580, 800): d.ellipse([cx - 28, 536, cx + 28, 592], fill=(3, 3, 5, 255))
    base = compor(base, s)
    base += brilho(660, 470, 190, 100, (60, 220, 130), 0.40)       # o brilho verde do laboratório no furgão
    s = camada(); d = ImageDraw.Draw(s)
    carro(d, 300, 640, 470); carro(d, 980, 640, 470)             # os dois carros pretos fechando a saída
    base = compor(base, s, 1.0)
    for cx in (140, 460, 820, 1140): base = luz_de_carro(base, cx, 575, forca=0.9)
    base += brilho(640, 640, 520, 70, (230, 225, 205), 0.28)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 690, 640, 360, lado_da_luz=-1)                       # Maluco
    pessoa(d, 575, 640, 270, lado_da_luz=1, sentada_ate=600)       # Kadu de joelhos
    d.line([(655, 405), (612, 372)], fill=(4, 5, 8, 255), width=11)  # o braço e a arma
    d.rectangle([590, 360, 624, 376], fill=(4, 5, 8, 255))
    base = compor(base, s, 1.1)
    acabamento(base, "furgao", seed=21)

# ------------------------------------------------------------------ SANGUE NA PIA
def pia():
    base = vgrad((10, 20, 26), (6, 12, 16), 1.0)
    base += brilho(560, 250, 430, 270, (150, 205, 220), 0.55)
    s = camada(); d = ImageDraw.Draw(s)
    for x in range(0, W, 80): d.line([(x, 0), (x, 560)], fill=(14, 28, 34, 255), width=2)   # azulejos
    for y in range(0, 560, 80): d.line([(0, y), (W, y)], fill=(14, 28, 34, 255), width=2)
    d.rectangle([380, 90, 760, 400], fill=(26, 44, 52, 255)); d.rectangle([380, 90, 760, 400], outline=(8, 12, 15, 255), width=12)   # o espelho
    chao(d, 560, (6, 9, 12, 255))
    d.rectangle([330, 410, 810, 440], fill=(18, 26, 30, 255))                 # bancada
    d.polygon([(380, 440), (760, 440), (730, 520), (410, 520)], fill=(12, 17, 20, 255))
    d.ellipse([440, 408, 700, 452], fill=(62, 78, 84, 255)); d.ellipse([456, 414, 684, 446], fill=(34, 44, 48, 255))   # a cuba da pia
    d.line([(570, 410), (570, 340)], fill=(70, 80, 86, 255), width=10); d.line([(570, 340), (620, 340)], fill=(70, 80, 86, 255), width=10)   # a torneira
    d.rectangle([880, 60, W, 560], fill=(5, 7, 10, 255))                       # a porta aberta
    base = compor(base, s)
    base += brilho(1000, 330, 160, 280, (255, 190, 110), 0.55)                 # luz quente do corredor
    s = camada(); d = ImageDraw.Draw(s)
    d.ellipse([520, 418, 640, 440], fill=(150, 10, 18, 255))                   # sangue na cuba
    for gx in (545, 590, 625): d.line([(gx, 430), (gx + 6, 470)], fill=(130, 8, 16, 255), width=3)
    base = compor(base, s, 1.6)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 1010, 580, 440, lado_da_luz=-1)                                   # Marisa na porta
    pessoa(d, 840, 580, 330, lado_da_luz=-1)                                    # Enzo no corredor
    base = compor(base, s, 1.1)
    acabamento(base, "pia", seed=22)

# ------------------------------------------------------------------ NOVO ESCONDERIJO
def esconderijo():
    base = vgrad((5, 9, 14), (9, 15, 18), 1.0)
    base += brilho(640, 70, 560, 130, (60, 120, 130), 0.50)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([0, 0, W, 90], fill=(8, 11, 15, 255))                         # teto baixo
    for y in (30, 58): d.rectangle([0, y, W, y + 10], fill=(18, 24, 28, 255))  # canos
    for x in range(120, W, 260): d.rectangle([x, 0, x + 14, 90], fill=(14, 19, 23, 255))
    chao(d, 560, (6, 8, 11, 255))
    for i in range(6):                                                         # fileira de máquinas de lavar
        x = 70 + i * 200
        d.rectangle([x, 330, x + 160, 560], fill=(16, 22, 27, 255)); d.rectangle([x, 330, x + 160, 350], fill=(22, 30, 36, 255))
        d.ellipse([x + 22, 380, x + 138, 496], fill=(7, 10, 13, 255)); d.ellipse([x + 38, 396, x + 122, 480], fill=(26, 40, 44, 255))
    d.rectangle([1090, 170, W, 560], fill=(9, 13, 16, 255))                    # a escada de acesso
    base = compor(base, s)
    for i in range(6): base += brilho(110 + i * 200, 438, 40, 40, (80, 190, 150), 0.30)   # luz fraca nos vidros
    base += brilho(640, 92, 90, 32, (210, 255, 235), 0.9)                    # lâmpada fluorescente
    base += brilho(940, 540, 300, 60, (70, 150, 130), 0.22)
    s = camada(); d = ImageDraw.Draw(s)
    for k in range(6): d.polygon([(1100 + k * 28, 170 + k * 52), (1280, 170 + k * 52), (1280, 190 + k * 52), (1100 + k * 28, 190 + k * 52)], fill=(5, 7, 10, 255))
    pessoa(d, 470, 590, 340, lado_da_luz=1); pessoa(d, 620, 590, 320, lado_da_luz=-1)   # Valter e Kadu no porão
    base = compor(base, s, 1.0)
    acabamento(base, "esconderijo", seed=23)

# ------------------------------------------------------------------ O ATACADISTA
def atacadista():
    base = vgrad((24, 15, 10), (10, 7, 6), 0.9)
    base += brilho(900, 250, 360, 230, (60, 80, 130), 0.40)
    s = camada(); d = ImageDraw.Draw(s); rng = random.Random(24)
    d.rectangle([560, 60, 1220, 420], fill=(22, 30, 52, 255))                  # janela panorâmica da cidade
    x = 560
    while x < 1220:
        w = rng.randint(30, 70); h = rng.randint(60, 230)
        d.rectangle([x, 420 - h, x + w, 420], fill=(10, 13, 24, 255))
        for jx in range(x + 5, x + w - 5, 11):
            for jy in range(420 - h + 8, 410, 16):
                if rng.random() < 0.22: d.rectangle([jx, jy, jx + 4, jy + 7], fill=(238, 190, 100, 255))
        x += w + 4
    for fx in (560, 780, 1000, 1216): d.rectangle([fx, 60, fx + 8, 420], fill=(8, 6, 5, 255))
    chao(d, 560, (14, 8, 6, 255))
    d.rectangle([380, 470, 1000, 500], fill=(34, 20, 13, 255)); d.rectangle([420, 500, 440, 600], fill=(26, 15, 10, 255)); d.rectangle([940, 500, 960, 600], fill=(26, 15, 10, 255))
    d.polygon([(660, 470), (700, 430), (780, 430), (820, 470)], fill=(20, 52, 40, 255))                      # abajur de banqueiro
    for k in range(3): d.rectangle([420 + k * 40, 452 - k * 4, 480 + k * 40, 470], fill=(70, 78, 62, 255))   # maços sobre a mesa
    base = compor(base, s)
    base += brilho(740, 465, 200, 60, (255, 200, 120), 0.65)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 700, 520, 330, lado_da_luz=1, sentada_ate=500)                 # O Fidalgo atrás da mesa
    pessoa(d, 190, 640, 420, lado_da_luz=1); pessoa(d, 330, 640, 400, lado_da_luz=1)   # os dois capangas
    base = compor(base, s, 1.1)
    acabamento(base, "atacadista", seed=24)

# ------------------------------------------------------------------ FUNDO FALSO
def fundo_falso():
    base = vgrad((22, 14, 12), (10, 7, 7), 0.9)
    base += brilho(400, 180, 240, 200, (80, 100, 150), 0.40)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([80, 60, 360, 540], fill=(18, 12, 10, 255)); d.line([(220, 60), (220, 540)], fill=(8, 5, 4, 255), width=4)   # o armário
    d.rectangle([560, 90, 760, 300], fill=(42, 56, 84, 255)); d.rectangle([560, 90, 760, 300], outline=(10, 7, 6, 255), width=10)   # a janela
    chao(d, 540, (22, 13, 9, 255))
    for x in range(0, W, 110): d.line([(x, 540), (x - 60, H)], fill=(14, 8, 6, 255), width=3)                              # tábuas do piso
    d.polygon([(600, 600), (940, 600), (990, 690), (550, 690)], fill=(3, 2, 2, 255))                                       # o vão aberto no piso
    base = compor(base, s)
    base += brilho(770, 650, 260, 70, (255, 200, 110), 0.85)                   # luz saindo do esconderijo
    s = camada(); d = ImageDraw.Draw(s); rng = random.Random(25)
    d.polygon([(640, 612), (900, 612), (930, 664), (610, 664)], fill=(34, 74, 44, 255))                                    # a mochila de dinheiro
    for k in range(9): d.rectangle([640 + k * 29, 596 + (k % 2) * 6, 664 + k * 29, 622], fill=(86, 128, 70, 255))
    d.polygon([(800, 640), (880, 628), (884, 642), (830, 662), (812, 690), (794, 686)], fill=(8, 8, 10, 255))               # a arma
    base = compor(base, s, 1.0)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 1010, 560, 430, lado_da_luz=-1)                                  # Marisa em choque
    base = compor(base, s, 1.1)
    acabamento(base, "fundo_falso", seed=25)

# ------------------------------------------------------------------ ENCRUZILHADA
def encruzilhada():
    base = vgrad((8, 8, 22), (30, 16, 20), 1.4)
    base += brilho(640, 330, 420, 150, (255, 120, 60), 0.45)
    base += brilho(120, 260, 280, 200, (230, 40, 40), 0.40); base += brilho(1160, 260, 280, 200, (50, 90, 235), 0.40)
    s = camada(); d = ImageDraw.Draw(s)
    d.polygon([(0, 420), (260, 330), (520, 360), (640, 330), (800, 350), (1040, 320), (W, 400), (W, H), (0, H)], fill=(7, 8, 14, 255))   # colinas
    d.rectangle([0, 560, W, H], fill=(8, 9, 13, 255))
    d.polygon([(560, 345), (720, 345), (1100, 720), (180, 720)], fill=(22, 22, 30, 255))                                  # a estrada que se divide
    d.polygon([(640, 345), (700, 345), (1280, 560), (1280, 650)], fill=(22, 22, 30, 255))
    d.polygon([(640, 345), (580, 345), (0, 560), (0, 650)], fill=(22, 22, 30, 255))
    d.polygon([(640, 420), (640, 720), (600, 720)], fill=(8, 9, 13, 255))
    d.rectangle([636, 380, 646, 600], fill=(5, 5, 8, 255)); d.polygon([(646, 392), (730, 392), (750, 408), (730, 424), (646, 424)], fill=(5, 5, 8, 255))
    d.polygon([(636, 440), (556, 440), (536, 456), (556, 472), (636, 472)], fill=(5, 5, 8, 255))                              # as placas
    base = compor(base, s)
    for fx in (120, 250, 380): base += brilho(fx, 480, 90, 22, (235, 70, 70), 0.30)     # luzes de viatura à esquerda
    for fx in (1160, 1030, 900): base += brilho(fx, 480, 90, 22, (70, 110, 240), 0.30)
    base += brilho(640, 345, 70, 26, (255, 170, 90), 0.55)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 640, 660, 300, lado_da_luz=1)                                          # Valter na encruzilhada
    base = compor(base, s, 1.1)
    acabamento(base, "encruzilhada", seed=26)

# ------------------------------------------------------------------ FUGA (final da queda) e PRAIA (final da fuga)
def sala_invadida(base, d_porta, seed):
    pass

def fuga():
    base = vgrad((14, 8, 10), (8, 5, 6), 0.9)
    base += brilho(300, 250, 380, 300, (220, 40, 50), 0.50); base += brilho(1000, 250, 380, 300, (50, 90, 235), 0.50)
    s = camada(); d = ImageDraw.Draw(s); rng = random.Random(27)
    d.rectangle([0, 520, W, H], fill=(15, 8, 8, 255))
    d.polygon([(480, 90), (800, 90), (840, 520), (440, 520)], fill=(150, 150, 170, 255))          # a porta arrombada com luz da rua
    d.polygon([(480, 90), (800, 90), (840, 520), (440, 520)], outline=(8, 5, 5, 255), width=14)
    d.polygon([(470, 100), (540, 120), (510, 520), (440, 520)], fill=(10, 6, 6, 255))              # a folha caída da porta
    d.rectangle([60, 300, 250, 520], fill=(16, 10, 10, 255)); d.rectangle([1030, 330, 1230, 520], fill=(16, 10, 10, 255))   # sofá e estante
    d.polygon([(760, 560), (820, 520), (900, 540), (860, 600)], fill=(10, 6, 6, 255)); d.line([(780, 600), (760, 660)], fill=(10, 6, 6, 255), width=8)   # cadeira tombada
    base = compor(base, s)
    base += brilho(640, 330, 130, 190, (200, 210, 255), 0.45)
    s = camada(); d = ImageDraw.Draw(s)
    for _ in range(26):                                                                            # notas espalhadas pelo chão
        x = rng.uniform(220, 1060); y = rng.uniform(570, 690); a = rng.uniform(-0.5, 0.5)
        d.polygon([(x, y), (x + 36, y + 10 * a), (x + 34, y + 22), (x - 2, y + 14)], fill=(70, 110, 60, 255))
    d.ellipse([460, 590, 740, 650], fill=(70, 6, 10, 255))                                         # mancha no tapete
    d.rounded_rectangle([500, 596, 700, 628], 14, fill=(6, 4, 5, 255)); d.ellipse([690, 590, 726, 626], fill=(6, 4, 5, 255))   # a silhueta caída
    pessoa(d, 640, 520, 330, cor=(3, 2, 3, 255), rim=(160, 160, 190, 255), lado_da_luz=1)           # um policial na porta
    base = compor(base, s, 1.2)
    acabamento(base, "fuga", seed=27)

def praia():
    base = vgrad((255, 160, 90), (50, 40, 70), 0.9)
    base = base * np.array([0.78, 0.75, 0.85], np.float32)
    base += brilho(640, 395, 330, 130, (255, 200, 120), 0.55)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([0, 392, W, H], fill=(24, 40, 62, 255))                                              # o mar
    for y in range(400, 560, 14): d.line([(0, y), (W, y + (y % 3))], fill=(40, 62, 90, 255), width=2)
    d.polygon([(0, 560), (W, 520), (W, H), (0, H)], fill=(60, 48, 40, 255))                          # a areia
    for dx, topo in ((110, 300), (250, 340)):                                                         # coqueiros
        d.polygon([(dx - 8, 650), (dx + 8, 650), (dx + 62, topo), (dx + 46, topo)], fill=(12, 10, 10, 255))
        cx = dx + 54
        for ang in (-170, -145, -115, -80, -50, -20, 10):                                              # folhas caindo em arco
            r = math.radians(ang); fim = (cx + math.cos(r) * 120, topo + math.sin(r) * 40 + 70)
            meio = (cx + math.cos(r) * 70, topo - 38 + math.sin(r) * 20)
            pts = [(cx, topo)] + [((1 - t) ** 2 * cx + 2 * (1 - t) * t * meio[0] + t ** 2 * fim[0],
                                   (1 - t) ** 2 * topo + 2 * (1 - t) * t * meio[1] + t ** 2 * fim[1]) for t in np.linspace(0.1, 1, 10)]
            d.line(pts, fill=(12, 14, 12, 255), width=9)
    base = compor(base, s)
    base += brilho(640, 440, 260, 28, (255, 215, 150), 0.85)                                         # reflexo do sol no mar
    base += brilho(640, 392, 160, 60, (255, 230, 170), 0.80)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 800, 650, 250, cor=(6, 5, 8, 255), rim=(255, 190, 120, 255), lado_da_luz=-1)           # a família na praia
    pessoa(d, 880, 650, 270, cor=(6, 5, 8, 255), rim=(255, 190, 120, 255), lado_da_luz=-1)
    pessoa(d, 960, 650, 170, cor=(6, 5, 8, 255), rim=(255, 190, 120, 255), lado_da_luz=-1)
    base = compor(base, s, 1.1)
    acabamento(base, "praia", vinheta=0.45, seed=28)

# ------------------------------------------------------------------ ATAQUE (final da vingança)
def ataque():
    base = vgrad((20, 8, 6), (8, 5, 6), 1.0)
    base += brilho(640, 430, 520, 300, (255, 120, 30), 0.95)
    base += brilho(640, 440, 230, 160, (255, 220, 120), 1.0)
    s = camada(); d = ImageDraw.Draw(s); rng = random.Random(29)
    d.rectangle([0, 560, W, H], fill=(8, 5, 5, 255))
    for x in range(0, W, 150): d.line([(x, 120), (x + rng.randint(-30, 30), 560)], fill=(10, 6, 5, 255), width=8)   # vigas torcidas do galpão
    d.line([(0, 140), (W, 120)], fill=(10, 6, 5, 255), width=14)
    base = compor(base, s)
    for _ in range(60):                                                                                    # faíscas e brasas
        bx = 640 + rng.gauss(0, 260); by = rng.uniform(80, 560); r = rng.uniform(2, 8)
        base += brilho(bx, by, r * 1.5, r * 2.2, (255, 170, 60), rng.uniform(0.3, 0.9))
    for i in range(7): base += brilho(640 + rng.gauss(0, 120), 260 - i * 30, 200, 60, (60, 40, 36), 0.30)       # fumaça subindo
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 640, 640, 250, cor=(3, 2, 2, 255), rim=(255, 190, 110, 255), lado_da_luz=1)                  # Valter caminhando no meio do fogo
    base = compor(base, s, 1.2)
    acabamento(base, "ataque", seed=29)

# ------------------------------------------------------------------ PRISÃO
def prisao():
    base = vgrad((18, 22, 28), (8, 10, 13), 0.9)
    base += brilho(800, 120, 260, 220, (170, 190, 215), 0.45)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([0, 0, W, 90], fill=(12, 14, 18, 255)); d.rectangle([0, 560, W, H], fill=(10, 12, 15, 255))
    d.rectangle([680, 130, 920, 300], fill=(150, 168, 190, 255))                                    # janelinha de luz fria
    for x in range(700, 920, 48): d.rectangle([x, 130, x + 12, 300], fill=(8, 10, 13, 255))             # as grades da janela
    d.rectangle([80, 90, 120, 560], fill=(6, 7, 9, 255)); d.rectangle([1160, 90, 1200, 560], fill=(6, 7, 9, 255))
    for x in range(160, 1160, 90): d.rectangle([x, 90, x + 16, 560], fill=(6, 7, 9, 255))               # as grades da cela
    d.rectangle([80, 300, 1200, 316], fill=(6, 7, 9, 255))
    d.rectangle([860, 420, 1100, 470], fill=(20, 24, 28, 255))                                       # o catre
    base = compor(base, s)
    base += brilho(800, 590, 300, 40, (150, 170, 195), 0.30)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 480, 600, 330, cor=(5, 6, 8, 255), rim=(150, 168, 190, 255), lado_da_luz=1, sentada_ate=560)  # o preso sentado, de cabeça baixa
    base = compor(base, s, 1.1)
    s = camada(); d = ImageDraw.Draw(s)
    for x in range(160, 1160, 90): d.rectangle([x, 90, x + 16, 560], fill=(3, 4, 6, 255))             # grades na frente de tudo
    d.rectangle([80, 300, 1200, 316], fill=(3, 4, 6, 255))
    base = compor(base, s, 0.6)
    acabamento(base, "prisao", vinheta=0.6, seed=30)

# ------------------------------------------------------------------ DELAÇÃO
def delacao():
    base = vgrad((12, 16, 22), (6, 8, 11), 0.9)
    base += brilho(640, 200, 440, 240, (80, 120, 150), 0.40)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([0, 570, W, H], fill=(6, 8, 10, 255))
    d.rectangle([760, 520, 1180, 548], fill=(24, 30, 36, 255)); d.rectangle([770, 548, 790, 640], fill=(16, 20, 24, 255)); d.rectangle([1150, 548, 1170, 640], fill=(16, 20, 24, 255))   # a mesa da sala de interrogatório
    d.rectangle([430, 90, 900, 330], fill=(34, 48, 60, 255)); d.rectangle([430, 90, 900, 330], outline=(8, 10, 12, 255), width=12)   # o vidro espelhado
    base = compor(base, s)
    s = camada(); d = ImageDraw.Draw(s)
    d.rounded_rectangle([860, 470, 1040, 520], 6, fill=(150, 130, 90, 255))                            # a pasta com o dossiê
    d.rounded_rectangle([880, 460, 1060, 512], 6, fill=(178, 160, 112, 255))
    for k in range(4): d.line([(900, 476 + k * 9), (1030, 476 + k * 9)], fill=(120, 105, 74, 255), width=2)
    base = compor(base, s, 0.8)
    base += brilho(960, 500, 190, 40, (255, 220, 160), 0.55)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 560, 640, 400, lado_da_luz=1); pessoa(d, 1080, 640, 380, lado_da_luz=-1)                 # Valter e Heitor frente a frente
    base = compor(base, s, 1.1)
    acabamento(base, "delacao", seed=31)

# ------------------------------------------------------------------ RICINA
def ricina():
    rng = random.Random(32)
    base = vgrad((6, 12, 8), (10, 20, 12), 1.0)
    base += brilho(640, 330, 520, 300, (150, 190, 40), 0.50)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([0, 580, W, H], fill=(5, 8, 6, 255))
    for x in range(0, W, 160): d.line([(x, 100), (x, 580)], fill=(9, 16, 11, 255), width=3)
    d.line([(0, 110), (W, 110)], fill=(9, 16, 11, 255), width=10)
    for cx, topo, larg in ((230, 400, 120), (1050, 380, 140)): frasco(d, cx, topo, 580, larg)
    base = compor(base, s)
    for i in range(14): base += brilho(640 + rng.gauss(0, 230), 330 + rng.gauss(0, 90), 160, 70, (170, 220, 60), 0.18)   # o ar contaminado
    s = camada(); d = ImageDraw.Draw(s)
    for cx, y, ang in ((300, 640, 1), (460, 660, -1), (860, 650, 1), (980, 640, -1)):                      # corpos caídos
        d.rounded_rectangle([cx - 90, y - 26, cx + 90, y + 20], 16, fill=(3, 5, 4, 255)); d.ellipse([cx + ang * 80 - 26, y - 32, cx + ang * 80 + 26, y + 20], fill=(3, 5, 4, 255))
    base = compor(base, s, 1.0)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 640, 600, 330, cor=(3, 5, 4, 255), rim=(170, 210, 80, 255), lado_da_luz=1)                    # Valter, de pé e calmo
    base = compor(base, s, 1.1)
    acabamento(base, "ricina", seed=32)

# ------------------------------------------------------------------ FIM (epílogo e game over) e STATUS
def fim():
    base = vgrad((4, 7, 10), (8, 14, 16), 1.0)
    base += brilho(640, 400, 360, 260, (30, 120, 80), 0.40)
    s = camada(); d = ImageDraw.Draw(s)
    frasco(d, 640, 190, 600, 250, contorno=(70, 150, 130, 220))
    base = compor(base, s, 1.2)
    base += brilho(640, 540, 130, 50, (60, 190, 120), 0.35)
    acabamento(base, "fim", vinheta=0.7, seed=33)

def status():
    rng = random.Random(34)
    base = vgrad((10, 14, 16), (6, 9, 11), 1.0)
    base += brilho(640, 330, 560, 330, (50, 110, 100), 0.35)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([0, 520, W, H], fill=(12, 9, 7, 255))                                           # a mesa
    for k, (x, y, a) in enumerate(((180, 470, -5), (880, 480, 4), (1010, 455, -3))):             # papéis espalhados
        d.polygon([(x, y), (x + 230, y + a), (x + 236, y + 70 + a), (x + 6, y + 66)], fill=(190, 184, 166, 255))
        for l in range(4): d.line([(x + 18, y + 14 + l * 13), (x + 200, y + 14 + l * 13 + a / 2)], fill=(110, 106, 94, 255), width=2)
    d.rounded_rectangle([440, 430, 840, 560], 8, fill=(150, 120, 70, 255)); d.rectangle([440, 416, 560, 436], fill=(170, 138, 82, 255))   # a pasta de arquivo
    d.rectangle([840, 360, 860, 520], fill=(10, 8, 7, 255)); d.polygon([(850, 360), (760, 250), (780, 232), (880, 340)], fill=(10, 8, 7, 255))   # luminária
    d.polygon([(700, 250), (800, 190), (860, 240), (760, 290)], fill=(18, 14, 10, 255))
    base = compor(base, s)
    base += brilho(740, 300, 230, 220, (255, 205, 130), 0.55)
    base += brilho(650, 470, 300, 80, (255, 205, 130), 0.40)
    acabamento(base, "status", seed=34)

# ------------------------------------------------------------------ INTRODUÇÃO: a sala de aula e o consultório
def introducao():
    base = vgrad((22, 26, 30), (9, 11, 14), 0.9)
    base += brilho(1040, 220, 330, 230, (255, 170, 90), 0.55)                    # luz do fim de tarde pela janela
    s = camada(); d = ImageDraw.Draw(s); rng = random.Random(41)
    d.rectangle([980, 90, 1220, 420], fill=(150, 90, 60, 255))                    # a janela
    for fx in (1100, 1160): d.rectangle([fx, 90, fx + 8, 420], fill=(8, 7, 7, 255))
    d.rectangle([980, 250, 1220, 258], fill=(8, 7, 7, 255))
    d.rectangle([980, 90, 1220, 420], outline=(8, 7, 7, 255), width=10)
    d.rectangle([90, 110, 900, 440], fill=(16, 34, 28, 255)); d.rectangle([90, 110, 900, 440], outline=(30, 20, 14, 255), width=14)   # o quadro-negro
    d.rectangle([90, 436, 900, 452], fill=(30, 20, 14, 255))
    cx, cy, r = 240, 270, 70                                                      # anel de benzeno desenhado a giz
    pts = [(cx + r * math.cos(math.radians(60 * k + 30)), cy + r * math.sin(math.radians(60 * k + 30))) for k in range(6)]
    d.polygon(pts, outline=(205, 215, 205, 255)); d.ellipse([cx - 38, cy - 38, cx + 38, cy + 38], outline=(205, 215, 205, 255), width=2)
    for k in range(6): d.line([pts[k], pts[(k + 1) % 6]], fill=(205, 215, 205, 255), width=3)
    for i, (x, y, w) in enumerate(((380, 170, 300), (380, 210, 240), (380, 250, 330), (380, 300, 180), (380, 350, 270))):   # fórmulas
        d.line([(x, y), (x + w, y + rng.randint(-3, 3))], fill=(190, 200, 190, 255), width=3)
        for tick in range(x + 20, x + w, 46): d.line([(tick, y - 9), (tick, y + 9)], fill=(190, 200, 190, 255), width=2)
    chao(d, 560, (12, 9, 8, 255))
    for fila in range(2):                                                         # carteiras vazias
        for k in range(5):
            x = 150 + k * 190 + fila * 70; y = 560 + fila * 70
            d.rectangle([x, y, x + 110, y + 14], fill=(34, 24, 18, 255)); d.rectangle([x + 8, y + 14, x + 16, y + 60], fill=(22, 15, 11, 255))
            d.rectangle([x + 94, y + 14, x + 102, y + 60], fill=(22, 15, 11, 255)); d.rectangle([x + 20, y + 40, x + 90, y + 50], fill=(26, 18, 14, 255))
    base = compor(base, s)
    base += brilho(900, 580, 260, 50, (255, 170, 90), 0.30)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([620, 470, 860, 492], fill=(30, 20, 14, 255)); d.rectangle([630, 492, 646, 560], fill=(20, 13, 10, 255)); d.rectangle([836, 492, 852, 560], fill=(20, 13, 10, 255))   # mesa do professor
    pessoa(d, 540, 560, 360, lado_da_luz=-1)                                      # o professor diante do quadro
    base = compor(base, s, 1.1)
    acabamento(base, "introducao", seed=41)

def consultorio():
    base = vgrad((8, 14, 20), (5, 9, 12), 0.9)
    base += brilho(420, 250, 360, 260, (150, 200, 230), 0.60)
    s = camada(); d = ImageDraw.Draw(s)
    d.rectangle([180, 100, 660, 420], fill=(176, 196, 206, 255)); d.rectangle([180, 100, 660, 420], outline=(10, 14, 18, 255), width=12)   # a caixa de luz com a chapa
    d.rectangle([200, 120, 640, 400], fill=(26, 38, 46, 255))
    d.line([(420, 130), (420, 390)], fill=(150, 160, 168, 255), width=8)           # coluna
    for k in range(8):                                                                # costelas
        y = 150 + k * 30
        d.arc([260, y, 420, y + 70], 200, 340, fill=(120, 135, 145, 255), width=3); d.arc([420, y, 580, y + 70], 200, 340, fill=(120, 135, 145, 255), width=3)
    d.ellipse([270, 170, 405, 380], fill=(60, 78, 88, 255)); d.ellipse([435, 170, 570, 380], fill=(60, 78, 88, 255))   # os pulmões
    d.ellipse([490, 230, 548, 292], fill=(205, 215, 220, 255))                       # a mancha
    chao(d, 560, (6, 9, 11, 255))
    d.rectangle([700, 440, 1230, 470], fill=(22, 30, 34, 255)); d.rectangle([720, 470, 744, 600], fill=(14, 19, 22, 255)); d.rectangle([1190, 470, 1214, 600], fill=(14, 19, 22, 255))   # mesa do médico
    base = compor(base, s)
    base += brilho(420, 260, 150, 110, (210, 235, 250), 0.35)
    s = camada(); d = ImageDraw.Draw(s)
    pessoa(d, 1010, 470, 330, lado_da_luz=1, sentada_ate=440)                        # Dr. Nogueira atrás da mesa
    pessoa(d, 660, 640, 340, lado_da_luz=1)                                           # o paciente, de pé, de costas para a chapa
    base = compor(base, s, 1.1)
    acabamento(base, "consultorio", seed=42)

NOVAS = (furgao, pia, esconderijo, atacadista, fundo_falso, encruzilhada, fuga, praia, ataque, prisao, delacao, ricina, fim, status, introducao, consultorio)
ANTIGAS = (menu, generico, titulo, jantar, rua, laboratorio, desfecho)

if __name__ == "__main__":
    import sys
    todas = {f.__name__: f for f in ANTIGAS + NOVAS}
    pedidos = sys.argv[1:]
    if pedidos == ["--novas"]: escolhidas = NOVAS
    elif pedidos:  escolhidas = [todas[n] for n in pedidos]
    else:          escolhidas = ANTIGAS + NOVAS
    for f in escolhidas:
        f(); print("gerado:", f.__name__)
