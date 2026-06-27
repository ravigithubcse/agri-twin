"""
╔══════════════════════════════════════════════════════════════════════════════╗
║          AGRI-TWIN AI — Architecture Animation Video Generator              ║
║                                                                              ║
║  Author  : Ravikumar (Ravi)                                                 ║
║  Email   : rn5127610@gmail.com                                              ║
║  Phone   : 9686906521                                                        ║
║  LinkedIn: https://www.linkedin.com/in/ravikumar2002/                       ║
║  GitHub  : https://github.com/ravigithubcse/agri-twin                       ║
║  Company : Ravi Future Labs                                                  ║
║                                                                              ║
║  Generates a full MP4 walkthrough of the Agri-Twin microservices            ║
║  architecture — scene by scene, with animated data-flow arrows,             ║
║  colour-coded layers, and explanatory captions.                             ║
║                                                                              ║
║  Usage:                                                                      ║
║    pip install matplotlib numpy pillow                                       ║
║    python agri_twin_architecture_animation.py                                ║
║                                                                              ║
║  Output: agri_twin_architecture.mp4  (≈ 60 seconds, 30 fps, 1920×1080)     ║
╚══════════════════════════════════════════════════════════════════════════════╝
"""

import math
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import matplotlib.patheffects as pe
from matplotlib.patches import FancyArrowPatch, FancyBboxPatch, FancyArrow
from matplotlib.animation import FuncAnimation, FFMpegWriter
import warnings
warnings.filterwarnings("ignore")

# ─────────────────────────────────────────────────────────────────────────────
# CONSTANTS & COLOUR PALETTE
# ─────────────────────────────────────────────────────────────────────────────

W, H        = 19.2, 10.8          # figure size in inches  →  1920×1080 px @ 100 dpi
FPS         = 30
DPI         = 100
TOTAL_SEC   = 62                   # total video length

BG          = "#0a0e1a"            # deep navy background

# Component colours
C_FRONTEND  = "#1565C0"            # Angular  – blue
C_USER_SVC  = "#1b5e20"            # user-service – forest green
C_FARM_SVC  = "#2e7d32"            # farm-twin-service – mid green
C_DB_USER   = "#b71c1c"            # users_db – deep red
C_DB_FARM   = "#bf360c"            # farmtwin_db – deep orange-red
C_CI        = "#1a237e"            # GitHub Actions – dark blue
C_FUTURE    = "#212121"            # planned modules – dark grey

# Arrow colours
A_REQUEST   = "#42a5f5"            # HTTP requests
A_JWT       = "#ffca28"            # JWT flow
A_DB        = "#ff8a65"            # DB connections
A_CI        = "#7986cb"            # CI dashed
A_FUTURE    = "#424242"

# Text
T_TITLE     = "#ffffff"
T_CAPTION   = "#b0bec5"
T_LABEL     = "#e3f2fd"
T_SMALL     = "#90a4ae"

FONT_FAMILY = "monospace"

# ─────────────────────────────────────────────────────────────────────────────
# HELPER DRAWING FUNCTIONS
# ─────────────────────────────────────────────────────────────────────────────

def rounded_box(ax, x, y, w, h, colour, alpha=1.0, lw=1.5,
                edgecolour=None, zorder=4, radius=0.04):
    """Draw a rounded rectangle and return the patch."""
    ec = edgecolour or _lighten(colour, 0.5)
    box = FancyBboxPatch(
        (x - w / 2, y - h / 2), w, h,
        boxstyle=f"round,pad=0,rounding_size={radius}",
        facecolor=colour, edgecolor=ec,
        linewidth=lw, alpha=alpha, zorder=zorder
    )
    ax.add_patch(box)
    return box


def glow_box(ax, x, y, w, h, colour, label_lines, fontsize=9,
             alpha=1.0, zorder=4):
    """Box + centred multi-line label with subtle glow edge."""
    rounded_box(ax, x, y, w, h, colour, alpha=alpha, lw=2,
                edgecolour=_lighten(colour, 0.6), zorder=zorder)
    # Glow (larger transparent version)
    rounded_box(ax, x, y, w + 0.05, h + 0.05, colour,
                alpha=0.15, lw=0, zorder=zorder - 1)
    text = "\n".join(label_lines)
    ax.text(x, y, text,
            ha="center", va="center", fontsize=fontsize,
            color=T_LABEL, fontfamily=FONT_FAMILY,
            fontweight="bold", zorder=zorder + 1,
            multialignment="center",
            linespacing=1.35)


def animated_arrow(ax, x0, y0, x1, y1, colour, alpha=1.0,
                   lw=2.0, dashed=False, label="", zorder=5):
    """Draw an arrow between two points."""
    ls = (0, (5, 4)) if dashed else "solid"
    ax.annotate(
        "", xy=(x1, y1), xytext=(x0, y0),
        arrowprops=dict(
            arrowstyle="-|>",
            color=colour,
            lw=lw,
            linestyle=ls,
            connectionstyle="arc3,rad=0.0",
            alpha=alpha
        ),
        zorder=zorder
    )
    if label:
        mx, my = (x0 + x1) / 2, (y0 + y1) / 2 + 0.08
        ax.text(mx, my, label, ha="center", va="bottom",
                fontsize=7, color=colour, alpha=alpha,
                fontfamily=FONT_FAMILY, zorder=zorder + 1)


def pulse_dot(ax, x, y, t, colour, radius=0.06, speed=2.5, zorder=8):
    """Draw a pulsing circle at (x, y) for animated flow."""
    alpha = 0.6 + 0.4 * math.sin(t * speed)
    size  = radius * (0.85 + 0.3 * math.sin(t * speed))
    circle = plt.Circle((x, y), size, color=colour,
                         alpha=alpha, zorder=zorder)
    ax.add_patch(circle)


def _lighten(hex_colour, amount=0.3):
    """Lighten a hex colour by mixing with white."""
    hex_colour = hex_colour.lstrip("#")
    r, g, b = (int(hex_colour[i:i+2], 16) for i in (0, 2, 4))
    r = int(r + (255 - r) * amount)
    g = int(g + (255 - g) * amount)
    b = int(b + (255 - b) * amount)
    return f"#{r:02x}{g:02x}{b:02x}"


def section_label(ax, x, y, w, h, colour, title):
    """Subtle section background + header label."""
    bg_box = FancyBboxPatch(
        (x - w / 2, y - h / 2), w, h,
        boxstyle="round,pad=0,rounding_size=0.06",
        facecolor=colour, edgecolor=_lighten(colour, 0.3),
        linewidth=1, alpha=0.18, zorder=2
    )
    ax.add_patch(bg_box)
    ax.text(x, y + h / 2 - 0.18, title,
            ha="center", va="top", fontsize=8,
            color=_lighten(colour, 0.5), fontfamily=FONT_FAMILY,
            fontweight="bold", alpha=0.9, zorder=3)


# ─────────────────────────────────────────────────────────────────────────────
# LAYOUT  —  all positions in figure-normalised space (0..1 mapped to axes)
# ─────────────────────────────────────────────────────────────────────────────
# We work in data coords: x ∈ [0, 19.2], y ∈ [0, 10.8]

LAYOUT = {
    # ── Angular Frontend ──────────────────────────────────────
    "fe": dict(x=2.8, y=7.2, w=3.8, h=4.0, colour=C_FRONTEND,
               label=["🌐 Angular 19 SPA",
                      "Register · Login",
                      "Dashboard",
                      "Land Parcels · Crop History"]),
    # ── user-service ──────────────────────────────────────────
    "us": dict(x=8.2, y=8.3, w=3.6, h=2.0, colour=C_USER_SVC,
               label=["🔐 user-service  :8081",
                      "JWT Issue · Auth · RBAC · Profile"]),
    # ── farm-twin-service ─────────────────────────────────────
    "fts": dict(x=8.2, y=5.5, w=3.6, h=2.0, colour=C_FARM_SVC,
                label=["🌿 farm-twin-service  :8082",
                       "Farm Twin · Land Parcels · Crops"]),
    # ── PostgreSQL users_db ───────────────────────────────────
    "pg1": dict(x=13.4, y=8.3, w=3.0, h=1.7, colour=C_DB_USER,
                label=["🐘 PostgreSQL  :5433",
                       "users_db",
                       "Flyway migrations"]),
    # ── PostgreSQL farmtwin_db ────────────────────────────────
    "pg2": dict(x=13.4, y=5.5, w=3.0, h=1.7, colour=C_DB_FARM,
                label=["🐘 PostgreSQL  :5434",
                       "farm_twin_db",
                       "Flyway migrations"]),
    # ── GitHub Actions CI ─────────────────────────────────────
    "ci": dict(x=2.8, y=2.4, w=3.8, h=2.0, colour=C_CI,
               label=["⚙️ GitHub Actions CI",
                      "Build + Test on every push"]),
    # ── Docker Compose ────────────────────────────────────────
    "docker": dict(x=8.2, y=2.4, w=3.6, h=1.6, colour="#004d40",
                   label=["🐳 Docker Compose",
                           "agritwin-net bridge"]),
    # ── Planned future ────────────────────────────────────────
    "f1": dict(x=16.8, y=8.8, w=2.2, h=0.85, colour=C_FUTURE,
               label=["📡 Kafka Events"]),
    "f2": dict(x=16.8, y=7.7, w=2.2, h=0.85, colour=C_FUTURE,
               label=["⚡ Redis Cache"]),
    "f3": dict(x=16.8, y=6.6, w=2.2, h=0.85, colour=C_FUTURE,
               label=["🛰️ Satellite AI"]),
    "f4": dict(x=16.8, y=5.5, w=2.2, h=0.85, colour=C_FUTURE,
               label=["🤖 ML Yield Pred."]),
    "f5": dict(x=16.8, y=4.4, w=2.2, h=0.85, colour=C_FUTURE,
               label=["📱 Flutter Mobile"]),
    "f6": dict(x=16.8, y=3.3, w=2.2, h=0.85, colour=C_FUTURE,
               label=["⛓️ Blockchain"]),
}

# Arrow definitions: (src_key, dst_key, colour, label, dashed, bend)
ARROWS = [
    # FE → user-service (register/login)
    ("fe_right", "us_left",   A_REQUEST, "POST /api/v1/auth/register\nPOST /api/v1/auth/login",  False, 0.0),
    # FE → farm-twin-service (farm data with JWT)
    ("fe_right", "fts_left",  A_JWT,     "GET/POST /api/v1/farm-twins\n(JWT Bearer)",              False, 0.0),
    # user-service → users_db
    ("us_right", "pg1_left",  A_DB,      "Spring Data JPA + Flyway",                               False, 0.0),
    # farm-twin-service → farmtwin_db
    ("fts_right","pg2_left",  A_DB,      "Spring Data JPA + Flyway",                               False, 0.0),
    # farm-twin-service → user-service (JWT verification)
    ("fts_top",  "us_bottom", A_JWT,     "verifies JWT issued by",                                  True, 0.0),
    # CI → user-service
    ("ci_right", "us_left2",  A_CI,      "Build + Test",                                            True, 0.0),
    # CI → farm-twin-service
    ("ci_right", "fts_left2", A_CI,      "Build + Test",                                            True, 0.0),
    # Docker → services
    ("docker_top","fts_bottom",A_CI,     "docker compose up",                                       True, 0.0),
]

# Connection points helper
def cp(key, side):
    n = LAYOUT[key]
    x, y, w, h = n["x"], n["y"], n["w"], n["h"]
    if side == "left":   return (x - w/2, y)
    if side == "right":  return (x + w/2, y)
    if side == "top":    return (x, y + h/2)
    if side == "bottom": return (x, y - h/2)
    return (x, y)


# ─────────────────────────────────────────────────────────────────────────────
# SCENE DEFINITIONS
# Each scene is a function that draws on the given axes at time t_local (0..1)
# ─────────────────────────────────────────────────────────────────────────────

SCENES = []  # filled below via @scene decorator

_scene_registry = []

def scene(start_sec, end_sec, caption):
    """Register a scene function with its time window and caption."""
    def decorator(fn):
        _scene_registry.append((start_sec, end_sec, caption, fn))
        return fn
    return decorator


# ─── Shared drawing: always-visible base layout ───────────────────────────

def draw_base(ax, alpha_fe=1, alpha_us=1, alpha_fts=1,
              alpha_pg1=1, alpha_pg2=1, alpha_ci=1,
              alpha_docker=1, alpha_future=1):
    """Draw all boxes at given alphas."""
    ax.set_facecolor(BG)
    ax.set_xlim(0, 19.2)
    ax.set_ylim(0, 10.8)
    ax.axis("off")

    # Section backgrounds
    section_label(ax, 2.8, 6.9, 4.8, 5.4, C_FRONTEND,   "FRONTEND")
    section_label(ax, 8.2, 7.2, 4.8, 5.6, C_USER_SVC,   "BACKEND SERVICES")
    section_label(ax, 13.4, 7.0, 4.0, 5.2, C_DB_USER,   "DATA LAYER")
    section_label(ax, 2.8, 2.4, 4.8, 2.8, C_CI,         "CI / INFRA")
    section_label(ax, 16.8, 6.3, 2.8, 7.2, C_FUTURE,    "FUTURE MODULES")

    # Main components
    alphas = dict(
        fe=alpha_fe, us=alpha_us, fts=alpha_fts,
        pg1=alpha_pg1, pg2=alpha_pg2, ci=alpha_ci,
        docker=alpha_docker
    )
    for key, info in LAYOUT.items():
        if key.startswith("f") and key[1:].isdigit():
            a = alpha_future
        else:
            a = alphas.get(key, 1.0)
        glow_box(ax, info["x"], info["y"],
                 info["w"], info["h"],
                 info["colour"], info["label"],
                 fontsize=8, alpha=a)


# ─── SCENE 0: Title card ─────────────────────────────────────────────────

@scene(0, 4, "")
def scene_title(ax, t):
    ax.set_facecolor(BG)
    ax.set_xlim(0, 19.2)
    ax.set_ylim(0, 10.8)
    ax.axis("off")

    fade = min(1.0, t * 2.5)

    # Big title
    ax.text(9.6, 6.8,
            "🌾  AGRI-TWIN AI",
            ha="center", va="center",
            fontsize=44, color="#66bb6a",
            fontfamily=FONT_FAMILY, fontweight="bold",
            alpha=fade)

    ax.text(9.6, 5.8,
            "Farm Commodity Digital Twin",
            ha="center", va="center",
            fontsize=22, color="#a5d6a7",
            fontfamily=FONT_FAMILY, alpha=fade)

    ax.text(9.6, 5.0,
            "India's Smallholder Farmer Intelligence Platform",
            ha="center", va="center",
            fontsize=14, color=T_CAPTION,
            fontfamily=FONT_FAMILY, alpha=fade)

    # Author block
    author_y = 3.6
    for i, line in enumerate([
        "👤  Ravikumar (Ravi)   ·   Ravi Future Labs   ·   Bengaluru, India 🇮🇳",
        "📧  rn5127610@gmail.com   ·   📞  9686906521",
        "🔗  linkedin.com/in/ravikumar2002   ·   github.com/ravigithubcse/agri-twin",
    ]):
        ax.text(9.6, author_y - i * 0.55,
                line, ha="center", va="center",
                fontsize=10, color=T_CAPTION,
                fontfamily=FONT_FAMILY, alpha=fade * 0.85)

    # Tech stack pills
    pills = [
        ("Java 21", "#ED8B00"), ("Spring Boot 3", "#6DB33F"),
        ("Angular 19", "#DD0031"), ("PostgreSQL 16", "#4169E1"),
        ("JWT Auth", "#ffca28"), ("Docker Compose", "#2496ED"),
        ("GitHub Actions", "#2088FF"),
    ]
    pill_y = 2.2
    total_w = sum(len(p[0]) * 0.13 + 0.7 for p in pills) + 0.3 * (len(pills)-1)
    x_start = 9.6 - total_w / 2
    for label, col in pills:
        pw = len(label) * 0.13 + 0.7
        rounded_box(ax, x_start + pw/2, pill_y, pw, 0.48,
                    col, alpha=fade * 0.9, zorder=5)
        ax.text(x_start + pw/2, pill_y, label,
                ha="center", va="center",
                fontsize=8.5, color="white",
                fontfamily=FONT_FAMILY, fontweight="bold",
                zorder=6, alpha=fade)
        x_start += pw + 0.3

    # Pulse dot bottom
    pulse_dot(ax, 9.6, 1.3, t * 3, "#66bb6a", radius=0.12)


# ─── SCENE 1: Full architecture overview ─────────────────────────────────

@scene(4, 14, "📐  Full Architecture Overview — 6 core components across 4 layers")
def scene_overview(ax, t):
    fade = min(1.0, t * 2.0)
    draw_base(ax,
              alpha_fe=fade, alpha_us=fade, alpha_fts=fade,
              alpha_pg1=fade, alpha_pg2=fade,
              alpha_ci=fade, alpha_docker=fade, alpha_future=fade * 0.4)

    # Draw all connection lines at fade alpha (no animation yet)
    a = fade * 0.6
    animated_arrow(ax, *cp("fe", "right"),  *cp("us", "left"),    A_REQUEST, a, lw=1.5, label="HTTP + JWT" if t > 0.5 else "")
    animated_arrow(ax, *cp("fe", "right"),  *cp("fts","left"),    A_JWT,     a, lw=1.5)
    animated_arrow(ax, *cp("us", "right"),  *cp("pg1","left"),    A_DB,      a, lw=1.5)
    animated_arrow(ax, *cp("fts","right"),  *cp("pg2","left"),    A_DB,      a, lw=1.5)
    animated_arrow(ax, *cp("fts","top"),    *cp("us", "bottom"),  A_JWT,     a * 0.7, lw=1, dashed=True)
    animated_arrow(ax, *cp("ci", "right"),  *cp("us", "left"),    A_CI,      a * 0.5, lw=1, dashed=True)
    animated_arrow(ax, *cp("ci", "right"),  *cp("fts","left"),    A_CI,      a * 0.5, lw=1, dashed=True)


# ─── SCENE 2: Angular 19 Frontend deep dive ──────────────────────────────

@scene(14, 23, "🌐  Angular 19 SPA — Register · Login · Dashboard · Land Parcels · Crop History")
def scene_frontend(ax, t):
    draw_base(ax,
              alpha_us=0.25, alpha_fts=0.25,
              alpha_pg1=0.25, alpha_pg2=0.25,
              alpha_ci=0.25, alpha_docker=0.25, alpha_future=0.1)

    # Feature components inside Angular box
    features = [
        ("Register / Login",    5.2, 8.6, "#1976d2"),
        ("Dashboard (Profile Score)", 5.2, 7.7, "#1565c0"),
        ("Land Parcel Form",    5.2, 6.8, "#0d47a1"),
        ("Land Parcel List",    5.2, 5.9, "#0d47a1"),
        ("Crop History Records",5.2, 5.0, "#0d47a1"),
    ]
    fade = min(1.0, t * 1.8)
    for i, (lbl, fx, fy, fc) in enumerate(features):
        a = min(1.0, max(0, (t - i * 0.12) * 3))
        glow_box(ax, fx, fy, 2.8, 0.55, fc, [lbl], fontsize=8, alpha=a)

    # Angular internals labels
    ax.text(5.2, 9.4, "Standalone Components · Signals · Angular Material",
            ha="center", va="center", fontsize=7.5, color="#90caf9",
            fontfamily=FONT_FAMILY, alpha=fade)
    ax.text(5.2, 9.1, "AuthInterceptor  →  auto-attaches JWT to every request",
            ha="center", va="center", fontsize=7.5, color="#80cbc4",
            fontfamily=FONT_FAMILY, alpha=fade)
    ax.text(5.2, 4.3, "TokenStorageService  →  localStorage  |  AuthGuard  →  route protection",
            ha="center", va="center", fontsize=7.5, color=T_SMALL,
            fontfamily=FONT_FAMILY, alpha=fade)

    # Pulse on HTTP arrow
    pulse_t = t * 4
    px = 4.7 + (8.2 - 4.7) * ((pulse_t % 1.0))
    pulse_dot(ax, px, 8.3, t, A_REQUEST, radius=0.09)
    animated_arrow(ax, *cp("fe", "right"), *cp("us", "left"),
                   A_REQUEST, fade * 0.7, lw=2,
                   label="HTTP + JWT Bearer")


# ─── SCENE 3: Auth flow — Register & Login ───────────────────────────────

@scene(23, 32, "🔐  Auth Flow — Register · Login · JWT Access Token · Refresh Token")
def scene_auth(ax, t):
    draw_base(ax,
              alpha_fts=0.2, alpha_pg2=0.25,
              alpha_ci=0.2, alpha_docker=0.2, alpha_future=0.08)

    steps = [
        (0.0,  "①  POST /api/v1/auth/register  →  phone + password + fullName + stateCode",
                2.8, 8.3, A_REQUEST),
        (0.15, "②  AuthService  →  checks phone uniqueness  →  BCrypt hash  →  User.save()",
                8.2, 7.65, "#66bb6a"),
        (0.28, "③  JwtTokenProvider.generateAccessToken()  →  sub=userId · role=FARMER · exp=15min",
                8.2, 7.05, A_JWT),
        (0.41, "④  RefreshTokenService.issue()  →  opaque UUID token  →  hashed in refresh_tokens table",
                8.2, 6.45, "#ffb74d"),
        (0.54, "⑤  AuthResponse  {accessToken, refreshToken, expiresIn, user}  →  Angular",
                5.5, 5.85, A_REQUEST),
        (0.67, "⑥  POST /api/v1/auth/refresh  →  rotates refresh token  →  issues new accessToken",
                5.5, 5.25, "#80cbc4"),
        (0.80, "⑦  POST /api/v1/auth/logout  →  revokeAllForUser()  →  refresh_tokens.revoked=true",
                5.5, 4.65, "#ef9a9a"),
    ]
    for delay, text, tx, ty, col in steps:
        a = min(1.0, max(0, (t - delay) * 5))
        ax.text(tx, ty, text,
                ha="center", va="center", fontsize=8,
                color=col, fontfamily=FONT_FAMILY, alpha=a,
                bbox=dict(boxstyle="round,pad=0.3",
                          facecolor=BG, edgecolor=col,
                          linewidth=0.8, alpha=a * 0.6))

    # Animated token pulse
    prog = (t * 2.5) % 1.0
    px = 4.7 + (10.0 - 4.7) * prog
    pulse_dot(ax, px, 8.3, t, A_JWT, radius=0.10)

    # DB arrow
    animated_arrow(ax, *cp("us", "right"), *cp("pg1", "left"),
                   A_DB, min(1.0, t * 2), lw=2,
                   label="INSERT users + refresh_tokens")


# ─── SCENE 4: Farm Twin Service flow ─────────────────────────────────────

@scene(32, 42, "🌿  Farm-Twin Service — One Digital Twin per Farmer · Land Parcels · Crop History")
def scene_farmtwin(ax, t):
    draw_base(ax,
              alpha_us=0.3, alpha_pg1=0.25,
              alpha_ci=0.2, alpha_docker=0.2, alpha_future=0.08)

    fade = min(1.0, t * 1.5)

    # JWT verification flow
    animated_arrow(ax, *cp("fts", "top"), *cp("us", "bottom"),
                   A_JWT, fade * 0.8, lw=2, dashed=True,
                   label="JwtAuthenticationFilter → validates\nsame shared JWT_SECRET")

    # DB arrow
    animated_arrow(ax, *cp("fts", "right"), *cp("pg2", "left"),
                   A_DB, fade, lw=2,
                   label="Spring Data JPA + Flyway")

    # API endpoints highlight
    endpoints = [
        (0.0,  "POST /api/v1/farm-twins/me",               "Create Farm Digital Twin (1 per user)"),
        (0.2,  "GET  /api/v1/farm-twins/me",               "Get Farm Twin + profile_completeness_score"),
        (0.38, "POST /api/v1/farm-twins/me/land-parcels",  "Add land parcel (area_acres, soil_type, GPS)"),
        (0.54, "GET  /api/v1/farm-twins/me/land-parcels",  "List all land parcels (ownership enforced)"),
        (0.70, "POST /api/v1/farm-twins/me/land-parcels/{id}/crop-history", "Record crop season (yield_quintals, income_inr)"),
    ]
    for i, (delay, endpoint, desc) in enumerate(endpoints):
        a = min(1.0, max(0, (t - delay) * 4))
        ey = 8.8 - i * 0.78
        glow_box(ax, 8.2, ey, 5.2, 0.52, "#0d3320", [endpoint], fontsize=7.5, alpha=a)
        ax.text(11.0, ey - 0.34, f"→  {desc}",
                ha="center", va="center", fontsize=7,
                color=T_SMALL, fontfamily=FONT_FAMILY, alpha=a)

    # Pulse on FE → FTS arrow
    prog = (t * 2.8) % 1.0
    px = 4.7 + (6.4 - 4.7) * prog
    py = 5.5
    pulse_dot(ax, px, py, t, A_JWT, radius=0.09)
    animated_arrow(ax, *cp("fe", "right"), *cp("fts", "left"),
                   A_JWT, fade, lw=2, label="JWT Bearer required")


# ─── SCENE 5: Database schema ─────────────────────────────────────────────

@scene(42, 50, "🗄️  PostgreSQL Schema — users_db · refresh_tokens · farm_twins · land_parcels · crop_history")
def scene_database(ax, t):
    draw_base(ax,
              alpha_fe=0.2, alpha_us=0.3, alpha_fts=0.3,
              alpha_ci=0.15, alpha_docker=0.15, alpha_future=0.08)

    fade = min(1.0, t * 1.8)

    # users_db schema box
    schema_us = [
        "users_db  (PostgreSQL :5433)",
        "──────────────────────────────────",
        "users:",
        "  id UUID PK  ·  phone VARCHAR(15) UNIQUE",
        "  password_hash  ·  full_name  ·  role",
        "  state_code  ·  district_code",
        "  language_preference  ·  account_status",
        "  tier  ·  last_login  ·  created_at",
        "",
        "refresh_tokens:",
        "  id UUID PK  ·  user_id FK→users",
        "  token_hash  ·  expires_at  ·  revoked",
    ]
    box_x, box_y = 13.4, 8.5
    for i, line in enumerate(schema_us):
        col = "#ef9a9a" if "users:" in line or "refresh_tokens:" in line else (
              "#ff8a65" if "──" in line else (
              "#ffcdd2" if "  id" in line or "  phone" in line or "  token" in line else T_SMALL))
        a = min(1.0, max(0, (t - i * 0.05) * 4)) * fade
        ax.text(box_x, box_y - i * 0.33, line,
                ha="center", va="center",
                fontsize=7, color=col,
                fontfamily=FONT_FAMILY, alpha=a)

    # farmtwin_db schema box
    schema_ft = [
        "farm_twin_db  (PostgreSQL :5434)",
        "──────────────────────────────────",
        "farm_twins:",
        "  id UUID PK  ·  user_id (unique, FK→user-svc)",
        "  profile_completeness_score SMALLINT",
        "  twin_data JSONB  ·  version  ·  last_updated",
        "",
        "land_parcels:",
        "  id UUID PK  ·  farm_twin_id FK",
        "  label  ·  area_acres  ·  soil_type",
        "  latitude · longitude  ·  current_crop",
        "",
        "crop_history:",
        "  id UUID PK  ·  land_parcel_id FK",
        "  season (KHARIF/RABI/ZAID/PERENNIAL)",
        "  yield_quintals  ·  income_inr  ·  sale_date",
    ]
    for i, line in enumerate(schema_ft):
        col = "#a5d6a7" if ("farm_twins:" in line or "land_parcels:" in line or "crop_history:" in line) else (
              "#ff8a65" if "──" in line else (
              "#dcedc8" if "  id" in line or "  farm_" in line or "  land_" in line else T_SMALL))
        a = min(1.0, max(0, (t - 0.25 - i * 0.04) * 4)) * fade
        ax.text(13.4, 5.2 - i * 0.30, line,
                ha="center", va="center",
                fontsize=7, color=col,
                fontfamily=FONT_FAMILY, alpha=a)

    # DB arrows
    animated_arrow(ax, *cp("us", "right"), *cp("pg1", "left"),
                   A_DB, fade, lw=2)
    animated_arrow(ax, *cp("fts", "right"), *cp("pg2", "left"),
                   A_DB, fade, lw=2)


# ─── SCENE 6: Docker Compose + CI ─────────────────────────────────────────

@scene(50, 58, "🐳  Docker Compose + GitHub Actions CI — One Command to Run Everything")
def scene_docker_ci(ax, t):
    draw_base(ax,
              alpha_fe=0.25, alpha_us=0.55, alpha_fts=0.55,
              alpha_pg1=0.55, alpha_pg2=0.55, alpha_future=0.1)

    fade = min(1.0, t * 1.8)

    # Docker compose code snippet
    compose = [
        "$ docker compose up --build",
        "",
        "✓ postgres-users    :5433  ← healthcheck: pg_isready",
        "✓ postgres-farmtwin :5434  ← healthcheck: pg_isready",
        "✓ user-service      :8081  ← depends_on: postgres-users (healthy)",
        "✓ farm-twin-service :8082  ← depends_on: postgres-farmtwin + user-service (healthy)",
        "",
        "ENV: JWT_SECRET  DB_URL  DB_USERNAME  DB_PASSWORD",
        "     USER_SERVICE_URL=http://user-service:8081",
        "",
        "Network: agritwin-net (bridge)  →  service discovery by name",
    ]
    for i, line in enumerate(compose):
        col = "#80cbc4" if line.startswith("$") else (
              "#a5d6a7" if line.startswith("✓") else (
              "#ffcc80" if "ENV:" in line or "Network:" in line else T_SMALL))
        a = min(1.0, max(0, (t - i * 0.06) * 4)) * fade
        ax.text(8.2, 9.8 - i * 0.45, line,
                ha="center", va="center",
                fontsize=8, color=col,
                fontfamily=FONT_FAMILY, alpha=a)

    # CI steps
    ci_steps = [
        "GitHub Actions — backend-ci.yml",
        "  trigger: push · pull_request  →  all branches",
        "  jobs: test-user-service",
        "        test-farm-twin-service",
        "        → mvn verify (JUnit 5 + Testcontainers)",
        "",
        "GitHub Actions — frontend-ci.yml",
        "  jobs: test-frontend",
        "        → npm install && npm test (Karma + Jasmine)",
    ]
    for i, line in enumerate(ci_steps):
        col = "#7986cb" if "GitHub" in line else (
              "#b0bec5" if "trigger" in line else T_SMALL)
        a = min(1.0, max(0, (t - 0.4 - i * 0.07) * 3)) * fade
        ax.text(2.8, 4.4 - i * 0.38, line,
                ha="center", va="center",
                fontsize=7.5, color=col,
                fontfamily=FONT_FAMILY, alpha=a)

    # Dashed CI arrows
    animated_arrow(ax, *cp("ci", "right"), *cp("us", "left"),
                   A_CI, fade * 0.7, lw=1.5, dashed=True, label="Build + Test")
    animated_arrow(ax, *cp("ci", "right"), *cp("fts", "left"),
                   A_CI, fade * 0.7, lw=1.5, dashed=True)
    animated_arrow(ax, *cp("docker", "top"), *cp("fts", "bottom"),
                   A_CI, fade * 0.6, lw=1.5, dashed=True, label="orchestrates")


# ─── SCENE 7: Future modules roadmap ─────────────────────────────────────

@scene(58, 62, "🔮  Roadmap — What Is Planned for Future Modules")
def scene_future(ax, t):
    draw_base(ax,
              alpha_fe=0.25, alpha_us=0.25, alpha_fts=0.25,
              alpha_pg1=0.25, alpha_pg2=0.25,
              alpha_ci=0.25, alpha_docker=0.25,
              alpha_future=min(1.0, t * 2.5))

    fade = min(1.0, t * 2.0)

    roadmap = [
        ("Module 2", "#f57c00", [
            "Apache Kafka  →  async event streaming between services",
            "Redis Cache  →  session cache + hot data",
        ]),
        ("Module 3", "#6a1b9a", [
            "ML Yield Prediction  →  PyTorch + FastAPI",
            "Satellite Imagery AI  →  ISRO / Planet Labs API",
        ]),
        ("Module 4", "#1565c0", [
            "Flutter Mobile App  →  offline-first PWA for farmers",
            "Blockchain Traceability  →  farm-to-market audit",
        ]),
        ("Module 5", "#1b5e20", [
            "Aadhaar / Govt Data Integration",
            "Razorpay Billing  →  Kisan Pro / Expert tiers",
        ]),
    ]
    y_start = 8.8
    for i, (mod, col, items) in enumerate(roadmap):
        a = min(1.0, max(0, (t - i * 0.15) * 4)) * fade
        ax.text(8.2, y_start - i * 1.55, mod,
                ha="center", va="center",
                fontsize=11, color=col,
                fontfamily=FONT_FAMILY, fontweight="bold", alpha=a)
        for j, item in enumerate(items):
            ax.text(8.2, y_start - i * 1.55 - 0.48 - j * 0.48, f"  →  {item}",
                    ha="center", va="center",
                    fontsize=8.5, color=T_CAPTION,
                    fontfamily=FONT_FAMILY, alpha=a)

    # Author credit bottom
    a = min(1.0, t * 3) * fade
    ax.text(9.6, 0.6,
            "Ravi Future Labs  ·  Ravikumar  ·  rn5127610@gmail.com  ·  9686906521  ·  linkedin.com/in/ravikumar2002",
            ha="center", va="center", fontsize=8,
            color=T_SMALL, fontfamily=FONT_FAMILY, alpha=a)


# ─────────────────────────────────────────────────────────────────────────────
# CAPTION BAR
# ─────────────────────────────────────────────────────────────────────────────

def draw_caption(ax, text):
    if not text:
        return
    # Thin bar at bottom
    bar = FancyBboxPatch(
        (0, 0), 19.2, 0.8,
        boxstyle="square,pad=0",
        facecolor="#0d1117", edgecolor="#1f2937",
        linewidth=1, zorder=10
    )
    ax.add_patch(bar)
    ax.text(9.6, 0.38, text,
            ha="center", va="center",
            fontsize=9.5, color="#cfd8dc",
            fontfamily=FONT_FAMILY, zorder=11)


def draw_progress_bar(ax, progress):
    """Thin green progress bar along the top."""
    bar = FancyBboxPatch(
        (0, 10.65), 19.2 * progress, 0.12,
        boxstyle="square,pad=0",
        facecolor="#66bb6a", edgecolor="none",
        linewidth=0, zorder=12
    )
    ax.add_patch(bar)


def draw_watermark(ax):
    ax.text(18.8, 10.5,
            "AGRI-TWIN AI  ·  Ravi Future Labs",
            ha="right", va="top",
            fontsize=7, color="#2d3748",
            fontfamily=FONT_FAMILY, zorder=12)


# ─────────────────────────────────────────────────────────────────────────────
# ANIMATION MAIN LOOP
# ─────────────────────────────────────────────────────────────────────────────

TOTAL_FRAMES = TOTAL_SEC * FPS


def make_frame(frame_idx):
    t_global = frame_idx / FPS          # global time in seconds

    fig, ax = plt.subplots(figsize=(W, H), dpi=DPI)
    fig.patch.set_facecolor(BG)
    ax.set_facecolor(BG)

    # Find active scene
    active_caption = ""
    active_fn      = None
    t_local        = 0.0

    for s_start, s_end, caption, fn in _scene_registry:
        if s_start <= t_global < s_end:
            duration = s_end - s_start
            t_local  = (t_global - s_start) / duration
            active_caption = caption
            active_fn      = fn
            break

    if active_fn is not None:
        active_fn(ax, t_local)

    draw_caption(ax, active_caption)
    draw_progress_bar(ax, t_global / TOTAL_SEC)
    draw_watermark(ax)

    ax.set_xlim(0, 19.2)
    ax.set_ylim(0, 10.8)
    ax.axis("off")

    fig.tight_layout(pad=0)
    return fig


def animate(frame_idx, ax, fig):
    ax.cla()
    ax.set_facecolor(BG)
    t_global = frame_idx / FPS

    active_caption = ""
    active_fn      = None
    t_local        = 0.0

    for s_start, s_end, caption, fn in _scene_registry:
        if s_start <= t_global < s_end:
            duration = s_end - s_start
            t_local  = (t_global - s_start) / duration
            active_caption = caption
            active_fn      = fn
            break

    if active_fn is not None:
        active_fn(ax, t_local)

    draw_caption(ax, active_caption)
    draw_progress_bar(ax, t_global / TOTAL_SEC)
    draw_watermark(ax)

    ax.set_xlim(0, 19.2)
    ax.set_ylim(0, 10.8)
    ax.axis("off")


# ─────────────────────────────────────────────────────────────────────────────
# ENTRY POINT
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    import sys
    import os

    output_file = "agri_twin_architecture.mp4"
    print("═" * 70)
    print("  AGRI-TWIN AI — Architecture Animation Generator")
    print("  Author: Ravikumar  |  Ravi Future Labs  |  Bengaluru")
    print("═" * 70)
    print(f"  Resolution : 1920 × 1080 px  ({DPI} DPI)")
    print(f"  Frame rate : {FPS} fps")
    print(f"  Duration   : {TOTAL_SEC} seconds  ({TOTAL_FRAMES} frames)")
    print(f"  Output     : {output_file}")
    print("═" * 70)

    # Check FFmpeg
    if os.system("ffmpeg -version > /dev/null 2>&1") != 0:
        print("\n⚠️  FFmpeg not found. Trying to save as GIF fallback...")
        USE_GIF = True
        output_file = "agri_twin_architecture.gif"
    else:
        USE_GIF = False

    fig, ax = plt.subplots(figsize=(W, H), dpi=DPI)
    fig.patch.set_facecolor(BG)

    def update(frame):
        if frame % 30 == 0:
            pct = int(frame / TOTAL_FRAMES * 100)
            bar = "█" * (pct // 5) + "░" * (20 - pct // 5)
            print(f"\r  Rendering  [{bar}]  {pct}%  frame {frame}/{TOTAL_FRAMES}", end="", flush=True)
        animate(frame, ax, fig)

    anim = FuncAnimation(
        fig, update,
        frames=TOTAL_FRAMES,
        interval=1000 / FPS,
        blit=False
    )

    if USE_GIF:
        print("\n  Saving GIF (this may take a few minutes)...")
        anim.save(output_file, writer="pillow", fps=FPS // 3)
    else:
        writer = FFMpegWriter(
            fps=FPS,
            codec="libx264",
            bitrate=4000,
            extra_args=[
                "-vf", "scale=1920:1080",
                "-pix_fmt", "yuv420p",
                "-preset", "fast",
                "-crf", "20"
            ]
        )
        print("\n  Saving MP4 via FFMpeg...")
        anim.save(output_file, writer=writer)

    plt.close(fig)
    size_mb = os.path.getsize(output_file) / 1_048_576
    print(f"\n\n✅  Done!  →  {output_file}  ({size_mb:.1f} MB)")
    print("═" * 70)
