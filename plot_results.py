"""Create graphs from the results saved by Benchmark.java."""

import csv
from pathlib import Path

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter


ROOT = Path(__file__).resolve().parent
PLOTS = ROOT / "plots"

PLOTS.mkdir(exist_ok=True)

COLORS = {
    "LCS": "#2563eb",
    "EditDistance": "#ea580c",
    "Naive": "#2563eb",
    "KMP": "#0f766e",
    "RabinKarp": "#ea580c"
}

plt.rcParams.update({
    "font.family": "DejaVu Sans",
    "font.size": 11,
    "axes.spines.top": False,
    "axes.spines.right": False,
    "axes.titleweight": "bold",
    "savefig.facecolor": "white"
})


def read_csv(name):
    path = ROOT / "results" / name

    with path.open(newline="", encoding="utf-8") as file:
        return list(csv.DictReader(file))


def style_axis(ax, sizes, xlabel):
    ax.set_xlabel(xlabel)
    ax.set_ylabel("Average execution time (ms)")
    ax.set_xticks(sizes)

    ax.xaxis.set_major_formatter(
        FuncFormatter(lambda value, position: f"{value:,.0f}")
    )

    ax.set_ylim(bottom=0)
    ax.grid(axis="y", color="#e2e8f0", linewidth=0.8)
    ax.set_axisbelow(True)


def add_series(ax, rows, name, size_key):
    selected = [
        row for row in rows
        if row["algorithm"] == name
    ]

    selected.sort(key=lambda row: int(row[size_key]))

    sizes = [
        int(row[size_key])
        for row in selected
    ]

    times = [
        float(row["average_ns"]) / 1_000_000
        for row in selected
    ]

    ax.plot(
        sizes,
        times,
        label=name,
        marker="o",
        linewidth=2,
        color=COLORS[name]
    )


def plot_dp():
    rows = read_csv("dp.csv")

    fig, ax = plt.subplots(
        figsize=(8, 5),
        constrained_layout=True
    )

    for name in ["LCS", "EditDistance"]:
        add_series(ax, rows, name, "length")

    style_axis(
        ax,
        [100, 500, 1000],
        "Length of each input string"
    )

    ax.set_title("LCS and Edit Distance", pad=24)

    ax.text(
        0,
        1.015,
        "Mean of 5 timed runs; 5 warmup runs",
        transform=ax.transAxes,
        fontsize=9,
        color="#475569"
    )

    ax.legend(frameon=False)

    fig.savefig(PLOTS / "dp_time.png", dpi=200)

    plt.close(fig)


def plot_matching():
    rows = read_csv("matching.csv")

    fig, axes = plt.subplots(
        1,
        2,
        figsize=(12, 4.8),
        sharey=True,
        constrained_layout=True
    )

    for ax, case in zip(axes, ["present", "absent"]):
        selected = [
            row for row in rows
            if row["case"] == case
        ]

        for name in ["Naive", "KMP", "RabinKarp"]:
            add_series(
                ax,
                selected,
                name,
                "text_length"
            )

        style_axis(
            ax,
            [10000, 50000, 100000],
            "Text length"
        )

        ax.set_title(f"Pattern {case}")
        ax.legend(frameon=False, loc="upper left")

    fig.suptitle(
        "String matching | pattern length = 20\n"
        "Mean of 5 timed runs; preprocessing included",
        fontsize=13
    )

    fig.savefig(PLOTS / "matching_time.png", dpi=200)

    plt.close(fig)


def plot_kasai():
    rows = read_csv("kasai.csv")

    rows.sort(key=lambda row: int(row["length"]))

    sizes = [
        int(row["length"])
        for row in rows
    ]

    times = [
        float(row["average_ns"]) / 1_000_000
        for row in rows
    ]

    fig, ax = plt.subplots(
        figsize=(8, 5),
        constrained_layout=True
    )

    ax.plot(
        sizes,
        times,
        marker="o",
        linewidth=2,
        color="#7c3aed",
        label="Kasai"
    )

    style_axis(
        ax,
        [1000, 5000, 10000],
        "String length"
    )

    ax.set_title("Kasai LCP construction", pad=24)

    ax.text(
        0,
        1.015,
        "Mean of 5 timed runs; suffix array construction excluded",
        transform=ax.transAxes,
        fontsize=9,
        color="#475569"
    )

    ax.legend(frameon=False)

    fig.savefig(PLOTS / "kasai_time.png", dpi=200)

    plt.close(fig)


if __name__ == "__main__":
    plot_dp()
    plot_matching()
    plot_kasai()

    print("Saved three graphs in plots/")