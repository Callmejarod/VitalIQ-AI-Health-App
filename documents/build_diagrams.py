"""Generate all diagrams (PNG) for the VitalIQ Week 5 SRS."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Ellipse, FancyArrowPatch, Rectangle, Circle
import os

OUT = os.path.join(os.path.dirname(__file__), "diagrams")
os.makedirs(OUT, exist_ok=True)

NAVY = "#1B3A57"
BLUE = "#2E6E9E"
TEAL = "#2BB3A3"
AMBER = "#E08E0B"
RED = "#C0392B"
GREY = "#5D6D7E"
LIGHT = "#EAF2F8"
LIGHT2 = "#FDEBD0"
GREEN = "#1E8449"


def box(ax, x, y, w, h, text, fc=LIGHT, ec=NAVY, fs=9, bold=False, style="round"):
    if style == "round":
        p = FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.02,rounding_size=0.08",
                           fc=fc, ec=ec, lw=1.4)
    else:
        p = Rectangle((x, y), w, h, fc=fc, ec=ec, lw=1.4)
    ax.add_patch(p)
    ax.text(x + w / 2, y + h / 2, text, ha="center", va="center", fontsize=fs,
            wrap=True, weight="bold" if bold else "normal", color="#14212B")


def diamond(ax, cx, cy, w, h, text, fc=LIGHT2, ec=AMBER, fs=8):
    pts = [[cx, cy + h / 2], [cx + w / 2, cy], [cx, cy - h / 2], [cx - w / 2, cy]]
    ax.add_patch(plt.Polygon(pts, fc=fc, ec=ec, lw=1.4))
    ax.text(cx, cy, text, ha="center", va="center", fontsize=fs, color="#14212B")


def arrow(ax, x1, y1, x2, y2, text="", color=NAVY, fs=8, rad=0.0, ls="-"):
    a = FancyArrowPatch((x1, y1), (x2, y2), arrowstyle="-|>", mutation_scale=14,
                        color=color, lw=1.3, connectionstyle=f"arc3,rad={rad}", linestyle=ls)
    ax.add_patch(a)
    if text:
        ax.text((x1 + x2) / 2, (y1 + y2) / 2, text, ha="center", va="center",
                fontsize=fs, color=color, backgroundcolor="white")


def stick(ax, cx, cy, label):
    ax.add_patch(Circle((cx, cy + 0.45), 0.18, fill=False, ec=NAVY, lw=1.6))
    ax.plot([cx, cx], [cy + 0.27, cy - 0.25], color=NAVY, lw=1.6)
    ax.plot([cx - 0.25, cx + 0.25], [cy + 0.1, cy + 0.1], color=NAVY, lw=1.6)
    ax.plot([cx, cx - 0.22], [cy - 0.25, cy - 0.6], color=NAVY, lw=1.6)
    ax.plot([cx, cx + 0.22], [cy - 0.25, cy - 0.6], color=NAVY, lw=1.6)
    ax.text(cx, cy - 0.85, label, ha="center", va="top", fontsize=9, weight="bold")


def save(fig, name):
    fig.savefig(os.path.join(OUT, name), dpi=160, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("wrote", name)


# ---------------------------------------------------------------------------
# USE CASE DIAGRAMS (4) - stick figures + ovals
# ---------------------------------------------------------------------------
def usecase(name, actor_label, system_label, ucs, extra_actor=None):
    fig, ax = plt.subplots(figsize=(8.2, 4.6))
    ax.set_xlim(0, 10); ax.set_ylim(0, 7); ax.axis("off")
    ax.add_patch(Rectangle((2.6, 0.4), 5.2, 6.2, fill=False, ec=GREY, lw=1.4))
    ax.text(5.2, 6.35, system_label, ha="center", fontsize=10, weight="bold", color=GREY)
    stick(ax, 1.0, 3.4, actor_label)
    n = len(ucs)
    ys = [6.6 - (i + 1) * (5.8 / (n + 1)) for i in range(n)]
    for uc, y in zip(ucs, ys):
        ax.add_patch(Ellipse((5.2, y), 4.0, 0.78, fc=LIGHT, ec=BLUE, lw=1.4))
        ax.text(5.2, y, uc, ha="center", va="center", fontsize=8.5)
        ax.plot([1.45, 3.2], [3.3, y], color=NAVY, lw=1.1)
    if extra_actor:
        stick(ax, 9.0, 3.4, extra_actor)
        for y in ys:
            ax.plot([8.55, 7.2], [3.3, y], color=GREY, lw=0.8, ls="--")
    save(fig, name)


usecase("uc1_workout.png", "Fitness User", "VitalIQ System",
        ["Start Workout Session", "View Live Metrics", "Pause / End Workout", "Persist Session"],
        extra_actor="Device Sensors")
usecase("uc2_bp.png", "Health User", "VitalIQ System",
        ["Open Health Log", "Enter BP Reading", "Validate Input", "Save to Repository", "View BP History"])
usecase("uc3_ai.png", "Fitness User", "VitalIQ System",
        ["Open AI Insights", "Generate Insights", "View Health Score", "View Recommendations"],
        extra_actor="AI Insights API")
usecase("uc4_profile.png", "First-Time User", "VitalIQ System",
        ["Open Profile Setup", "Enter Profile Data", "Validate Fields", "Save Profile", "Edit Profile Later"])


# ---------------------------------------------------------------------------
# ACTIVITY DIAGRAM - two vertical swimlanes (Main vs IO), explicit node list
# nodes: dict id -> (lane, row, text, kind)  kind in {box,diamond,start,end}
# edges: list of (a, b, label)
# ---------------------------------------------------------------------------
def activity_swim(name, title, nodes, edges, rows):
    fig, ax = plt.subplots(figsize=(10, 1.05 * rows + 2.0))
    ax.set_xlim(0, 12); ax.set_ylim(0, rows + 1.6); ax.axis("off")
    ax.text(6, rows + 1.2, title, ha="center", fontsize=12, weight="bold", color=NAVY)
    ax.add_patch(Rectangle((0.15, 0.1), 5.7, rows + 0.85, fc="#F4F9FD", ec=GREY, lw=1.2))
    ax.add_patch(Rectangle((6.15, 0.1), 5.7, rows + 0.85, fc="#FBF4E8", ec=GREY, lw=1.2))
    ax.text(3.0, rows + 0.65, "MAIN THREAD  (UI + viewModelScope)", ha="center",
            fontsize=9.5, weight="bold", color=BLUE)
    ax.text(9.0, rows + 0.65, "IO / DEFAULT DISPATCHER  (Repository)", ha="center",
            fontsize=9.5, weight="bold", color=AMBER)
    cx = {"main": 3.0, "io": 9.0}
    pos = {}

    def yof(r):
        return rows - r + 0.1
    for nid, (lane, r, txt, kind) in nodes.items():
        x = cx[lane]; y = yof(r)
        if kind == "diamond":
            diamond(ax, x, y, 2.2, 0.85, txt, fs=7.5)
            pos[nid] = (x, y, 1.1, 0.42)
        elif kind in ("start", "end"):
            ax.add_patch(Circle((x, y), 0.16, fc=(GREEN if kind == "start" else NAVY), ec=NAVY))
            ax.text(x + 0.35, y, txt, ha="left", va="center", fontsize=8)
            pos[nid] = (x, y, 0.16, 0.16)
        else:
            fc = LIGHT if lane == "main" else LIGHT2
            box(ax, x - 2.3, y - 0.32, 4.6, 0.64, txt, fc=fc, fs=8)
            pos[nid] = (x, y, 2.3, 0.32)
    for a, b, lbl in edges:
        x1, y1, hw1, hh1 = pos[a]; x2, y2, hw2, hh2 = pos[b]
        cross = abs(x1 - x2) > 2
        col = RED if cross else NAVY
        if cross:
            if x1 < x2:
                arrow(ax, x1 + hw1, y1, x2 - hw2, y2, lbl, color=col, fs=7.5)
            else:
                arrow(ax, x1 - hw1, y1, x2 + hw2, y2, lbl, color=col, fs=7.5)
        else:
            arrow(ax, x1, y1 - hh1, x2, y2 + hh2, lbl, color=col, fs=7.5)
    save(fig, name)


# Activity Diagram 1 (Week 5): AI Insights — dual data source + thread boundaries
activity_swim(
    "act_insights_week5.png",
    "Activity Diagram — AI Insights (Dual Data Source + Thread Boundaries)",
    nodes={
        "s":  ("main", 1, "start", "start"),
        "u1": ("main", 2, "User taps 'Generate Insights'", "box"),
        "u2": ("main", 3, "VM.generateInsights()\nviewModelScope.launch", "box"),
        "u3": ("main", 4, "Emit StateFlow = Loading", "box"),
        "r1": ("io",   5, "Repository.refreshInsights()\nwithContext(Dispatchers.IO)", "box"),
        "d1": ("io",   6, "cached insight\nfresh?", "diamond"),
        "r3": ("io",   7, "[no] ApiService.generateInsights()\nsuspend HTTP (IO)", "box"),
        "r4": ("io",   8, "Map InsightDto -> Domain -> Entity", "box"),
        "r5": ("io",   9, "Room DAO: upsert cached insight (IO)", "box"),
        "r2": ("io",  10, "[yes] Room DAO: read cached insight\n(reactive Flow, IO)", "box"),
        "u4": ("main", 11, "StateFlow emits Success / Error", "box"),
        "u5": ("main", 12, "Compose recomposes Insights screen", "box"),
        "e":  ("main", 13, "end", "end"),
    },
    edges=[
        ("s", "u1", ""), ("u1", "u2", ""), ("u2", "u3", ""),
        ("u3", "r1", "suspend"), ("r1", "d1", ""),
        ("d1", "r3", "no (remote)"),
        ("r3", "r4", ""), ("r4", "r5", ""),
        ("r5", "r2", "merge"),
        ("d1", "r2", "yes (cache)"),
        ("r2", "u4", "Flow emit"),
        ("u4", "u5", "resume Main"), ("u5", "e", ""),
    ],
    rows=13.4,
)

# Activity Diagram 2: Sensor-driven workout tracking (Main vs IO/Default)
activity_swim(
    "act_workout.png",
    "Activity Diagram — Sensor-Driven Workout Tracking",
    nodes={
        "s":  ("main", 1, "start", "start"),
        "u1": ("main", 2, "User taps 'Start Workout'", "box"),
        "u2": ("main", 3, "VM registers SensorManager\nlisteners (Main)", "box"),
        "u3": ("main", 4, "Sensor callback fires (Main)", "box"),
        "p1": ("io",   5, "Dispatchers.Default:\nfilter + classify activity", "box"),
        "p2": ("io",   6, "Compute steps, intensity, duration", "box"),
        "u4": ("main", 7, "StateFlow emits live metrics", "box"),
        "u5": ("main", 8, "Compose recomposes dashboard", "box"),
        "d1": ("main", 9, "user ends\nworkout?", "diamond"),
        "r1": ("io",   10.2, "Dispatchers.IO:\nRepository persists WorkoutSession", "box"),
        "e":  ("main", 11.2, "end", "end"),
    },
    edges=[
        ("s", "u1", ""), ("u1", "u2", ""), ("u2", "u3", ""),
        ("u3", "p1", "offload"), ("p1", "p2", ""),
        ("p2", "u4", "resume Main"), ("u4", "u5", ""),
        ("u5", "d1", ""),
        ("d1", "u3", "no (loop)"),
        ("d1", "r1", "yes (persist)"),
        ("r1", "e", "Flow emit"),
    ],
    rows=11.6,
)


# ---------------------------------------------------------------------------
# SEQUENCE DIAGRAM (Week 5) - dual data source, thread bands, scope origin
# ---------------------------------------------------------------------------
def sequence_week5(name):
    fig, ax = plt.subplots(figsize=(11.5, 9.2))
    ax.set_xlim(0, 14); ax.set_ylim(0, 16); ax.axis("off")
    ax.text(7, 15.5, "Sequence Diagram — AI Insights (Dual Data Source + Coroutine Scope)",
            ha="center", fontsize=12, weight="bold", color=NAVY)
    lifelines = [
        ("UI\n(Compose)", 1.4, BLUE),
        ("InsightsViewModel\n(viewModelScope)", 4.0, BLUE),
        ("Repository", 7.0, NAVY),
        ("Room DAO\n(cache)", 9.6, AMBER),
        ("ApiService\n(remote)", 12.2, AMBER),
    ]
    xs = {}
    for label, x, c in lifelines:
        box(ax, x - 1.0, 14.2, 2.0, 0.8, label, fc=LIGHT, ec=c, fs=8, bold=True)
        ax.plot([x, x], [0.6, 14.2], color=GREY, lw=1.0, ls=(0, (4, 3)))
        xs[label.split("\n")[0]] = x
    # thread bands
    ax.add_patch(Rectangle((0.3, 11.6), 13.4, 1.7, fc="#EAF2F8", ec="none", alpha=0.5))
    ax.text(0.45, 12.45, "MAIN", fontsize=8, weight="bold", color=BLUE, rotation=90, va="center")
    ax.add_patch(Rectangle((0.3, 3.0), 13.4, 8.4, fc="#FBF4E8", ec="none", alpha=0.5))
    ax.text(0.45, 7.2, "IO  (withContext(Dispatchers.IO))", fontsize=8, weight="bold", color=AMBER, rotation=90, va="center")
    ax.add_patch(Rectangle((0.3, 1.2), 13.4, 1.6, fc="#EAF2F8", ec="none", alpha=0.5))
    ax.text(0.45, 2.0, "MAIN", fontsize=8, weight="bold", color=BLUE, rotation=90, va="center")

    def msg(y, a, b, txt, ret=False, color=NAVY):
        x1, x2 = xs[a], xs[b]
        ls = (0, (5, 3)) if ret else "-"
        arrow(ax, x1, y, x2, y, "", color=color, ls="solid")
        a2 = FancyArrowPatch((x1, y), (x2, y), arrowstyle="-|>", mutation_scale=12,
                             color=color, lw=1.2, linestyle=ls)
        ax.add_patch(a2)
        midx = (x1 + x2) / 2
        ax.text(midx, y + 0.16, txt, ha="center", va="bottom", fontsize=7.6, color=color)

    msg(13.2, "UI", "InsightsViewModel", "1. onGenerateClick()")
    ax.text(4.0, 12.6, "viewModelScope.launch { }", fontsize=7.2, style="italic", color=BLUE, ha="center")
    msg(11.9, "InsightsViewModel", "Repository", "2. refreshInsights()  →  withContext(IO)", color=RED)
    msg(10.9, "Repository", "Room DAO", "3. read cached insight + aggregate logs")
    msg(10.0, "Room DAO", "Repository", "4. cached data", ret=True)
    diamond(ax, 7.0, 9.0, 2.6, 0.9, "5. cache fresh?", fs=7.5)
    msg(8.0, "Repository", "ApiService", "6. [stale] generateInsights() suspend HTTP")
    msg(7.0, "ApiService", "Repository", "7. InsightDto (JSON)", ret=True)
    ax.text(7.0, 6.1, "8. map DTO → Domain → Entity  (IO)", fontsize=7.6, ha="center",
            color=NAVY, style="italic", backgroundcolor="white")
    msg(5.4, "Repository", "Room DAO", "9. upsert cached insight (write-through)")
    msg(4.5, "Room DAO", "Repository", "10. ack", ret=True)
    msg(3.4, "Repository", "InsightsViewModel", "11. emit Domain result  →  resume Main", ret=True, color=RED)
    msg(2.4, "InsightsViewModel", "UI", "12. StateFlow = Success(insight)")
    ax.text(1.4, 1.6, "13. recompose", fontsize=7.6, ha="center", color=BLUE)
    save(fig, name)


sequence_week5("seq_insights_week5.png")


# ---------------------------------------------------------------------------
# STATE MACHINE - Workout session lifecycle
# ---------------------------------------------------------------------------
def state_machine(name):
    fig, ax = plt.subplots(figsize=(10, 6.4))
    ax.set_xlim(0, 12); ax.set_ylim(0, 8); ax.axis("off")
    ax.text(6, 7.6, "State Machine Diagram — Workout Session Lifecycle", ha="center",
            fontsize=12, weight="bold", color=NAVY)
    states = {
        "Idle":      (1.6, 5.6),
        "Active":    (5.2, 5.6),
        "Paused":    (5.2, 2.4),
        "Saving":    (9.0, 5.6),
        "Completed": (9.0, 2.4),
    }
    for s, (x, y) in states.items():
        box(ax, x - 1.0, y - 0.45, 2.0, 0.9, s, fc=LIGHT, ec=NAVY, bold=True, fs=9)
    ax.add_patch(Circle((1.6, 7.0), 0.13, fc=GREEN, ec=NAVY))
    arrow(ax, 1.6, 6.87, 1.6, 6.05, "")
    arrow(ax, 2.6, 5.6, 4.2, 5.6, "Start Workout")
    arrow(ax, 5.2, 5.15, 5.2, 2.85, "Pause")
    arrow(ax, 4.6, 2.85, 4.6, 5.15, "Resume", rad=0.0)
    arrow(ax, 6.2, 5.6, 8.0, 5.6, "End Workout")
    arrow(ax, 6.2, 2.5, 8.0, 5.3, "End Workout", rad=-0.2)
    arrow(ax, 9.0, 5.15, 9.0, 2.85, "persist OK\n(IO)")
    arrow(ax, 8.0, 2.6, 2.4, 5.2, "New Session", color=GREY, rad=0.25, ls="--")
    ax.add_patch(Circle((9.0, 1.3), 0.16, fc=NAVY, ec=NAVY))
    ax.add_patch(Circle((9.0, 1.3), 0.10, fc="white", ec=NAVY))
    arrow(ax, 9.0, 1.95, 9.0, 1.5, "")
    ax.text(6.0, 0.5, "Persistence transition (Saving → Completed) executes on Dispatchers.IO via the Repository; "
            "all other transitions are Main-thread state mutations on viewModelScope.",
            ha="center", fontsize=8, style="italic", color=GREY)
    save(fig, name)


state_machine("state_workout.png")


# ---------------------------------------------------------------------------
# LAYERED ARCHITECTURE DIAGRAM (as-built) — UI / ViewModel / Repository /
# Data sources, with class names and Main vs IO thread boundaries.
# ---------------------------------------------------------------------------
def layered_architecture(name):
    fig, ax = plt.subplots(figsize=(11.8, 9.6))
    ax.set_xlim(0, 14); ax.set_ylim(0, 16); ax.axis("off")
    ax.text(5.7, 15.5, "Layered Architecture — VitalIQ (As-Built)",
            ha="center", fontsize=13, weight="bold", color=NAVY)

    # Thread bands (right edge labels). MAIN covers UI + ViewModel + composition
    # root; IO covers the Repository internals + both data sources.
    ax.add_patch(Rectangle((0.2, 9.95), 11.0, 5.05, fc="#EAF2F8", ec="none", alpha=0.5))
    ax.add_patch(Rectangle((0.2, 0.5), 11.0, 8.95, fc="#FBF4E8", ec="none", alpha=0.5))
    ax.text(11.55, 12.4, "MAIN THREAD", rotation=90, va="center", ha="center",
            fontsize=9.5, weight="bold", color=BLUE)
    ax.text(12.1, 12.4, "UI render + ViewModel state\n(viewModelScope on Main)",
            rotation=90, va="center", ha="center", fontsize=6.8, color=BLUE)
    ax.text(11.55, 4.8, "IO DISPATCHER", rotation=90, va="center", ha="center",
            fontsize=9.5, weight="bold", color=AMBER)
    ax.text(12.1, 4.8, "Repository network + Room disk IO\n(Sensor classify: Default-eligible)",
            rotation=90, va="center", ha="center", fontsize=6.8, color=AMBER)

    # ---- Layer 1: UI ----
    ax.text(0.45, 14.62, "UI LAYER — Jetpack Compose", fontsize=10.5, weight="bold", color=BLUE)
    ui = ["DashboardScreen", "WorkoutScreen", "LogScreen",
          "InsightsScreen", "ProfileScreen", "HistoryScreen"]
    for i, s in enumerate(ui):
        box(ax, 0.45 + i * 1.74, 13.75, 1.62, 0.62, s, fc=LIGHT, ec=BLUE, fs=6.6)
    ax.text(0.45, 13.45, "observe state via collectAsStateWithLifecycle();  delegate user actions to the ViewModel",
            fontsize=7.0, style="italic", color=GREY)

    arrow(ax, 5.6, 13.7, 5.6, 12.55, "state ↓   /   events ↑", color=NAVY, fs=7.3)

    # ---- Layer 2: ViewModel ----
    ax.text(0.45, 12.35, "VIEWMODEL LAYER — StateFlow<UiState>, viewModelScope",
            fontsize=10.5, weight="bold", color=BLUE)
    vm = ["DashboardViewModel", "WorkoutViewModel", "LogViewModel",
          "InsightsViewModel", "ProfileViewModel", "HistoryViewModel"]
    for i, s in enumerate(vm):
        box(ax, 0.45 + i * 1.74, 11.45, 1.62, 0.62, s, fc=LIGHT, ec=BLUE, fs=6.1)
    ax.text(0.45, 11.15, "single source of truth; depends ONLY on a Repository interface (no Retrofit / Room / DAO imports)",
            fontsize=7.0, style="italic", color=GREY)

    # ---- Composition root strip ----
    box(ax, 0.45, 10.05, 10.55, 0.78,
        "COMPOSITION ROOT  —  ServiceLocator (di/ServiceLocator.kt) + VitalIQApp   "
        "|   manual DI, NO framework: builds RetrofitClient.apiService + AppDatabase once, hands each ViewModel its Repository interface",
        fc="#FEF9E7", ec=AMBER, fs=6.6)
    arrow(ax, 5.6, 11.4, 5.6, 10.9, "", color=NAVY)

    # ---- Thread boundary ----
    ax.plot([0.25, 11.15], [9.72, 9.72], color=RED, lw=1.5, ls=(0, (6, 3)))
    ax.text(5.7, 9.83, "THREAD BOUNDARY  —  Repository methods switch Main → IO via withContext(Dispatchers.IO)",
            fontsize=7.6, ha="center", color=RED, weight="bold")
    arrow(ax, 5.6, 10.0, 5.6, 8.65, "repo.<suspend fun>()", color=NAVY, fs=7.3)

    # ---- Layer 3: Repository ----
    ax.text(0.45, 8.55, "REPOSITORY LAYER — the SOLE data-access seam (interface + Impl)",
            fontsize=10.5, weight="bold", color=NAVY)
    repo = ["DashboardRepository", "WorkoutRepository", "HealthRepository",
            "InsightsRepository", "ProfileRepository"]
    for i, s in enumerate(repo):
        box(ax, 0.45 + i * 2.11, 7.6, 1.98, 0.72, s + "\n(+ Impl)", fc=LIGHT2, ec=NAVY, fs=6.2)
    ax.text(0.45, 7.28, "withContext(Dispatchers.IO);  network-first, write-through to Room, offline fallback to Room cache",
            fontsize=7.0, style="italic", color=GREY)

    arrow(ax, 3.3, 7.55, 2.9, 5.15, "suspend HTTP", color=NAVY, fs=7.0)
    arrow(ax, 7.7, 7.55, 8.6, 5.15, "DAO read / write", color=NAVY, fs=7.0)

    # ---- Layer 4: Data sources ----
    ax.text(0.45, 5.05, "DATA SOURCES", fontsize=10.5, weight="bold", color=AMBER)
    box(ax, 0.7, 3.55, 4.7, 1.4,
        "NETWORK  (system of record)\nRetrofitClient → ApiService (suspend)\nGson → FastAPI + MongoDB backend",
        fc=LIGHT, ec=BLUE, fs=7.2)
    box(ax, 6.3, 3.55, 4.7, 1.4,
        "LOCAL CACHE\nRoom AppDatabase (v2, KSP)\n7 DAOs + 7 entities  →  offline reads",
        fc=LIGHT2, ec=AMBER, fs=7.2)

    ax.text(5.7, 2.7,
            "Swapping a data source (e.g. Retrofit → Room) changes only the *RepositoryImpl + one ServiceLocator line — no ViewModel changes.",
            fontsize=7.4, ha="center", style="italic", color=GREEN)
    ax.text(5.7, 2.25,
            "Sensors (accelerometer + step counter) feed WorkoutViewModel state on Main; classification is CPU-light and Default-eligible.",
            fontsize=7.0, ha="center", style="italic", color=GREY)

    save(fig, name)


layered_architecture("arch_layered.png")
print("ALL DIAGRAMS DONE")
