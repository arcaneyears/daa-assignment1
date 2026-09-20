#!/usr/bin/env python3
"""
Turns results.csv into the three required plots (time, depth, ratio) plus two
bonus plots, and writes the numeric Theta-check table used in REPORT.md.

Usage:  python3 scripts/plot_results.py [results.csv] [output_dir]
"""
import csv
import math
import sys
from collections import defaultdict
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter, FixedLocator

CSV = Path(sys.argv[1] if len(sys.argv) > 1 else "results.csv")
OUT = Path(sys.argv[2] if len(sys.argv) > 2 else "docs/plots")
OUT.mkdir(parents=True, exist_ok=True)

SURFACE = "#fcfcfb"
INK = "#0b0b0b"
INK_2 = "#52514e"
GRID = "#dedcd6"

# Colour follows the algorithm (the entity), never its rank in a panel.
# Validated as a 5-slot categorical set on the light surface
# (adjacent CVD dE 9.2, normal-vision dE 27.6) with markers as secondary encoding.
STYLE = {
    "mergesort":           ("#2a78d6", "o", "MergeSort"),
    "quicksort":           ("#eb6834", "s", "QuickSort"),
    "insertionsort":       ("#1baf7a", "^", "InsertionSort"),
    "quickselect":         ("#4a3aa7", "D", "QuickSelect"),
    "deterministicselect": ("#e87ba4", "v", "Median-of-Medians"),
    "closestpair":            ("#2a78d6", "o", "Closest pair O(n log n)"),
    "closestpair_bruteforce": ("#eb6834", "s", "Closest pair brute force"),
}
SORTS = ["mergesort", "quicksort", "insertionsort"]
SELECTS = ["quickselect", "deterministicselect"]
INPUTS = ["random", "sorted", "duplicates", "reversed"]

plt.rcParams.update({
    "figure.facecolor": SURFACE,
    "axes.facecolor": SURFACE,
    "savefig.facecolor": SURFACE,
    "axes.edgecolor": GRID,
    "axes.labelcolor": INK_2,
    "text.color": INK,
    "xtick.color": INK_2,
    "ytick.color": INK_2,
    "font.size": 9,
    "axes.titlesize": 10,
    "axes.grid": True,
    "grid.color": GRID,
    "grid.linewidth": 0.6,
    "legend.frameon": False,
})

rows = []
with CSV.open() as fh:
    for r in csv.DictReader(fh):
        rows.append({
            "algorithm": r["algorithm"],
            "input": r["input"],
            "n": int(r["n"]),
            "time_ms": float(r["time_ms"]),
            "comparisons": int(r["comparisons"]),
            "max_depth": int(r["max_depth"]),
        })

series = defaultdict(list)          # (algorithm, input) -> [(n, row), ...]
for r in rows:
    series[(r["algorithm"], r["input"])].append(r)
for key in series:
    series[key].sort(key=lambda r: r["n"])


def line(ax, algo, inp, ykey, transform=None):
    data = series.get((algo, inp), [])
    if not data:
        return
    colour, marker, label = STYLE[algo]
    xs = [d["n"] for d in data]
    ys = [transform(d) if transform else d[ykey] for d in data]
    ax.plot(xs, ys, color=colour, marker=marker, markersize=5.5, linewidth=2,
            label=label, markeredgecolor=SURFACE, markeredgewidth=0.8)


def human(n, _pos=None):
    if n >= 1_000_000:
        return f"{n / 1_000_000:g}M"
    if n >= 1_000:
        return f"{n / 1_000:g}K"
    return f"{n:g}"


def decorate(ax, title, xlabel="n (array size)", ylabel="", ticks=(1_000, 10_000, 100_000, 1_000_000)):
    ax.set_title(title, color=INK, pad=8)
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.set_xscale("log")
    ax.xaxis.set_major_locator(FixedLocator(list(ticks)))
    ax.xaxis.set_minor_locator(FixedLocator([]))
    ax.xaxis.set_major_formatter(FuncFormatter(human))
    ax.tick_params(length=0)
    for spine in ("top", "right"):
        ax.spines[spine].set_visible(False)


def legend(ax, **kw):
    kw.setdefault("loc", "best")
    ax.legend(fontsize=8, frameon=True, facecolor=SURFACE, edgecolor="none",
              framealpha=0.92, **kw)


# ---------------------------------------------------------------- 1. time vs n
fig, axes = plt.subplots(1, 4, figsize=(15, 4.0), sharey=True)
for ax, inp in zip(axes, INPUTS):
    for algo in SORTS + SELECTS:
        line(ax, algo, inp, "time_ms")
    ax.set_yscale("log")
    decorate(ax, inp, ylabel="median time, ms" if inp == INPUTS[0] else "")
legend(axes[0], loc="upper left")
fig.suptitle("Running time vs n — median of 5 runs (log–log)", color=INK, x=0.01, ha="left",
             fontsize=12)
fig.tight_layout(rect=(0, 0, 1, 0.94))
fig.savefig(OUT / "time_vs_n.png", dpi=160)
plt.close(fig)

# --------------------------------------------------------------- 2. depth vs n
fig, axes = plt.subplots(1, 4, figsize=(15, 4.0), sharey=True)
for ax, inp in zip(axes, INPUTS):
    for algo in ["mergesort", "quicksort", "quickselect", "deterministicselect"]:
        line(ax, algo, inp, "max_depth")
    ns = sorted({r["n"] for r in rows if r["algorithm"] == "mergesort"})
    ax.plot(ns, [math.log2(n) for n in ns], color=INK_2, linestyle=":", linewidth=1.4,
            label="log2(n)")
    ax.plot(ns, [2 * math.log2(n) for n in ns], color=INK_2, linestyle="--", linewidth=1.4,
            label="2·log2(n) limit")
    decorate(ax, inp, ylabel="max recursion depth" if inp == INPUTS[0] else "")
legend(axes[0], loc="upper left")
fig.suptitle("Maximum recursion depth vs n", color=INK, x=0.01, ha="left", fontsize=12)
fig.tight_layout(rect=(0, 0, 1, 0.94))
fig.savefig(OUT / "depth_vs_n.png", dpi=160)
plt.close(fig)

# --------------------------------------------------------------- 3. ratio vs n
fig, axes = plt.subplots(2, 4, figsize=(15, 7.4))
for col, inp in enumerate(INPUTS):
    ax = axes[0][col]
    for algo in ["mergesort", "quicksort"]:
        line(ax, algo, inp, None, transform=lambda d: d["comparisons"] / (d["n"] * math.log2(d["n"])))
    decorate(ax, f"sorts — {inp}",
             ylabel="comparisons / (n·log2 n)" if col == 0 else "")
    ax.set_ylim(bottom=0)

    ax = axes[1][col]
    for algo in SELECTS:
        line(ax, algo, inp, None, transform=lambda d: d["comparisons"] / d["n"])
    decorate(ax, f"selection — {inp}", ylabel="comparisons / n" if col == 0 else "")
    ax.set_ylim(bottom=0)
legend(axes[0][0], loc="lower left")
legend(axes[1][0], loc="center right")
fig.suptitle("Theta check — measured comparisons divided by the predicted growth",
             color=INK, x=0.01, ha="left", fontsize=12)
fig.tight_layout(rect=(0, 0, 1, 0.95))
fig.savefig(OUT / "ratio_vs_n.png", dpi=160)
plt.close(fig)

# ------------------------------------------------- 4. bonus A: select comparison
fig, axes = plt.subplots(1, 2, figsize=(11, 4.2))
for inp, dash in (("random", "-"), ("sorted", "--")):
    for algo in SELECTS:
        data = series.get((algo, inp), [])
        colour, marker, label = STYLE[algo]
        axes[0].plot([d["n"] for d in data], [d["time_ms"] for d in data], color=colour,
                     marker=marker, linestyle=dash, linewidth=2, markersize=5.5,
                     label=f"{label} — {inp}", markeredgecolor=SURFACE, markeredgewidth=0.8)
        axes[1].plot([d["n"] for d in data], [d["comparisons"] / d["n"] for d in data],
                     color=colour, marker=marker, linestyle=dash, linewidth=2, markersize=5.5,
                     label=f"{label} — {inp}", markeredgecolor=SURFACE, markeredgewidth=0.8)
axes[0].set_yscale("log")
decorate(axes[0], "time", ylabel="median time, ms")
decorate(axes[1], "comparisons per element", ylabel="comparisons / n")
axes[1].set_ylim(bottom=0)
legend(axes[0], loc="upper left")
fig.suptitle("Bonus A — randomized QuickSelect vs deterministic Median-of-Medians",
             color=INK, x=0.01, ha="left", fontsize=12)
fig.tight_layout(rect=(0, 0, 1, 0.93))
fig.savefig(OUT / "select_quick_vs_deterministic.png", dpi=160)
plt.close(fig)

# --------------------------------------------------- 5. bonus B: closest pair
fig, axes = plt.subplots(1, 2, figsize=(11, 4.2))
for algo in ["closestpair", "closestpair_bruteforce"]:
    line(axes[0], algo, "random", "time_ms")
axes[0].set_yscale("log")
decorate(axes[0], "time", xlabel="n (points)", ylabel="median time, ms",
         ticks=(1_000, 2_000, 10_000, 100_000, 1_000_000))
axes[0].legend(fontsize=8, loc="upper left")

data = series.get(("closestpair", "random"), [])
axes[1].plot([d["n"] for d in data], [d["comparisons"] / (d["n"] * math.log2(d["n"])) for d in data],
             color=STYLE["closestpair"][0], marker="o", linewidth=2, markersize=5.5,
             markeredgecolor=SURFACE, markeredgewidth=0.8, label="distance evaluations / (n·log2 n)")
decorate(axes[1], "Theta check", xlabel="n (points)", ylabel="evaluations / (n·log2 n)")
axes[1].set_ylim(bottom=0)
legend(axes[1], loc="lower left")
fig.suptitle("Bonus B — closest pair of points", color=INK, x=0.01, ha="left", fontsize=12)
fig.tight_layout(rect=(0, 0, 1, 0.93))
fig.savefig(OUT / "closest_pair.png", dpi=160)
plt.close(fig)

# ------------------------------------------------------- Theta-check numbers
def envelope(algo, inp, growth, n0=10_000):
    data = [d for d in series.get((algo, inp), []) if d["n"] >= n0]
    if not data:
        return None
    ratios = [d["comparisons"] / growth(d["n"]) for d in data]
    return min(ratios), max(ratios), max(ratios) / min(ratios)


lines = ["# Theta check — empirical constants", "",
         "Ratio f(n)/g(n) over the measured sizes; c1 and c2 are the smallest and largest",
         "ratio observed for n >= n0, so c1*g(n) <= f(n) <= c2*g(n) holds on that range.", "",
         "| algorithm | input | g(n) | n0 | c1 | c2 | c2/c1 |",
         "|---|---|---|---|---|---|---|"]
for algo in ["mergesort", "quicksort"]:
    for inp in INPUTS:
        e = envelope(algo, inp, lambda n: n * math.log2(n))
        if e:
            lines.append(f"| {STYLE[algo][2]} | {inp} | n·log2 n | 10 000 | {e[0]:.3f} | {e[1]:.3f} | {e[2]:.2f} |")
for algo in SELECTS:
    for inp in INPUTS:
        e = envelope(algo, inp, lambda n: n)
        if e:
            lines.append(f"| {STYLE[algo][2]} | {inp} | n | 10 000 | {e[0]:.3f} | {e[1]:.3f} | {e[2]:.2f} |")
e = envelope("closestpair", "random", lambda n: n * math.log2(n))
if e:
    lines.append(f"| Closest pair | random points | n·log2 n | 10 000 | {e[0]:.3f} | {e[1]:.3f} | {e[2]:.2f} |")
e = envelope("insertionsort", "random", lambda n: n * n, n0=1_000)
if e:
    lines.append(f"| InsertionSort | random | n² | 1 000 | {e[0]:.3f} | {e[1]:.3f} | {e[2]:.2f} |")

(Path(OUT).parent / "theta_check.md").write_text("\n".join(lines) + "\n")
print("wrote plots to", OUT)
print("\n".join(lines))
