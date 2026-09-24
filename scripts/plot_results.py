import csv
import math
import sys
from collections import defaultdict
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

CSV = Path(sys.argv[1] if len(sys.argv) > 1 else "results.csv")
OUT = Path(sys.argv[2] if len(sys.argv) > 2 else "docs/plots")
OUT.mkdir(parents=True, exist_ok=True)

ALGORITHMS = ["mergesort", "quicksort", "insertionsort", "quickselect"]
INPUTS = ["random", "sorted", "duplicates"]
MARKERS = {"random": "o", "sorted": "s", "duplicates": "^"}
LINESTYLES = {"random": "-", "sorted": "--", "duplicates": ":"}
COLORS = {"mergesort": "tab:blue", "quicksort": "tab:orange",
          "insertionsort": "tab:green", "quickselect": "tab:purple"}

series = defaultdict(list)
with CSV.open() as fh:
    for r in csv.DictReader(fh):
        series[(r["algorithm"], r["input"])].append(
            (int(r["n"]), float(r["time_ms"]), int(r["comparisons"]), int(r["max_depth"])))
for key in series:
    series[key].sort()


def plot(ax, algos, value):
    for algo in algos:
        for inp in INPUTS:
            data = series.get((algo, inp))
            if not data:
                continue
            ax.plot([d[0] for d in data], [value(algo, d) for d in data],
                    color=COLORS[algo], marker=MARKERS[inp], linestyle=LINESTYLES[inp],
                    label=f"{algo} / {inp}")
    ax.set_xscale("log")
    ax.set_xlabel("n")
    ax.grid(True, alpha=0.3)


fig, ax = plt.subplots(figsize=(9, 6))
plot(ax, ALGORITHMS, lambda algo, d: d[1])
ax.set_yscale("log")
ax.set_ylabel("median time, ms")
ax.set_title("Time vs n (median of 5 runs)")
ax.legend(fontsize=7, ncol=2)
fig.tight_layout()
fig.savefig(OUT / "time_vs_n.png", dpi=150)
plt.close(fig)

fig, ax = plt.subplots(figsize=(9, 6))
plot(ax, ALGORITHMS, lambda algo, d: d[3])
ns = sorted({d[0] for data in series.values() for d in data})
ax.plot(ns, [2 * math.log2(n) for n in ns], color="black", linestyle="-.", label="2*log2(n)")
ax.set_ylabel("max recursion depth")
ax.set_title("Max recursion depth vs n")
ax.legend(fontsize=7, ncol=2)
fig.tight_layout()
fig.savefig(OUT / "depth_vs_n.png", dpi=150)
plt.close(fig)


def ratio(algo, d):
    n, comparisons = d[0], d[2]
    return comparisons / n if algo == "quickselect" else comparisons / (n * math.log2(n))


fig, ax = plt.subplots(figsize=(9, 6))
plot(ax, ["mergesort", "quicksort", "quickselect"], ratio)
ax.set_ylim(bottom=0)
ax.set_ylabel("comparisons / (n*log2 n) for sorts, comparisons / n for quickselect")
ax.set_title("Ratio vs n")
ax.legend(fontsize=7, ncol=2)
fig.tight_layout()
fig.savefig(OUT / "ratio_vs_n.png", dpi=150)
plt.close(fig)

print("Theta check: c1 = min ratio, c2 = max ratio for n >= n0")
checks = [(a, i, 10_000) for a in ("mergesort", "quicksort", "quickselect") for i in INPUTS]
checks.append(("insertionsort", "random", 1_000))
for algo, inp, n0 in checks:
    data = [d for d in series.get((algo, inp), []) if d[0] >= n0]
    if algo == "insertionsort":
        ratios = [d[2] / (d[0] * d[0]) for d in data]
    else:
        ratios = [ratio(algo, d) for d in data]
    print(f"{algo:14s} {inp:11s} n0={n0:<6d} c1={min(ratios):.3f} c2={max(ratios):.3f} "
          f"c2/c1={max(ratios) / min(ratios):.2f}")
print("wrote plots to", OUT)
