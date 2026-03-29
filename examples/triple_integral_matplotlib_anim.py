"""
三重积分（第一卦限四面体）— Matplotlib 动画示例（不依赖 Manim）。

默认：**3D 透视**（mpl_toolkits.mplot3d），机位随帧旋转（view_init）；
公式、标题用 **figure 坐标系 2D 叠加**（fig.text），避免数学字被 3D 透视拉歪。

可选：python examples/triple_integral_matplotlib_anim.py --2d  仅导出旧版纯 2D 平面动画。

依赖：
  pip install -r examples/requirements-matplotlib.txt
  导出 mp4：脚本会自动用 imageio-ffmpeg 自带的 ffmpeg（无需系统 PATH）；
  若未安装该包，则尝试 PATH 中的 ffmpeg；仍失败则退化为 GIF。

运行（仓库根目录 MathAnim）：
  python examples/triple_integral_matplotlib_anim.py
  python examples/triple_integral_matplotlib_anim.py --2d
"""
from __future__ import annotations

import argparse
import shutil
import sys
from pathlib import Path

import numpy as np


def _resolve_ffmpeg_executable() -> str | None:
    """供 Matplotlib FFMpegWriter 使用；避免 WinError2 找不到 ffmpeg。"""
    try:
        import imageio_ffmpeg

        p = imageio_ffmpeg.get_ffmpeg_exe()
        if p and Path(p).exists():
            return p
    except Exception:
        pass
    w = shutil.which("ffmpeg")
    return w


def _apply_ffmpeg_to_matplotlib() -> str | None:
    import matplotlib

    exe = _resolve_ffmpeg_executable()
    if exe:
        matplotlib.rcParams["animation.ffmpeg_path"] = exe
    return exe

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "media" / "matplotlib_exports"
OUT_MP4 = OUT_DIR / "triple_integral_mpl.mp4"
OUT_GIF = OUT_DIR / "triple_integral_mpl.gif"


def _save_anim(fig, anim, fps: int) -> None:
    import matplotlib.pyplot as plt
    from matplotlib.animation import FFMpegWriter, PillowWriter

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    ff = _apply_ffmpeg_to_matplotlib()
    try:
        writer = FFMpegWriter(fps=fps, metadata={"title": "Triple integral demo"})
        anim.save(OUT_MP4, writer=writer)
        print(f"已写入: {OUT_MP4}")
        if ff:
            print(f"（ffmpeg: {ff}）")
    except Exception as e:
        print(f"mp4 导出失败（{e}），改用 GIF…", file=sys.stderr)
        if ff is None:
            print(
                "提示：安装 imageio-ffmpeg 可把 ffmpeg 打进环境而无需单独配置 PATH：\n"
                "  pip install imageio-ffmpeg",
                file=sys.stderr,
            )
        anim.save(OUT_GIF, writer=PillowWriter(fps=fps))
        print(f"已写入: {OUT_GIF}")
    plt.close(fig)


def run_3d() -> int:
    import matplotlib.pyplot as plt
    from matplotlib.animation import FuncAnimation
    from mpl_toolkits.mplot3d.art3d import Poly3DCollection

    fig = plt.figure(figsize=(12.8, 7.2), facecolor="white")
    # 底部留给 2D 公式条
    ax = fig.add_subplot(111, projection="3d")
    fig.subplots_adjust(left=0.02, right=0.98, top=0.94, bottom=0.14)

    verts = np.array(
        [
            [0.0, 0.0, 0.0],
            [1.0, 0.0, 0.0],
            [0.0, 1.0, 0.0],
            [0.0, 0.0, 1.0],
        ]
    )
    face_idx = [[0, 1, 2], [0, 1, 3], [0, 2, 3], [1, 2, 3]]
    faces = [verts[idx] for idx in face_idx]

    poly = Poly3DCollection(
        faces,
        linewidths=1.0,
        edgecolors="#333333",
        alpha=0.28,
        facecolors="#4A90E2",
    )
    ax.add_collection3d(poly)

    # 坐标轴参考线（第一卦限）
    r = 1.15
    ax.plot([0, r], [0, 0], [0, 0], color="#888888", lw=1.2)
    ax.plot([0, 0], [0, r], [0, 0], color="#888888", lw=1.2)
    ax.plot([0, 0], [0, 0], [0, r], color="#888888", lw=1.2)

    ax.set_xlim(0, 1.15)
    ax.set_ylim(0, 1.15)
    ax.set_zlim(0, 1.15)
    ax.set_box_aspect((1, 1, 1))
    ax.set_xticks([0, 0.5, 1.0])
    ax.set_yticks([0, 0.5, 1.0])
    ax.set_zticks([0, 0.5, 1.0])
    ax.xaxis.pane.fill = False
    ax.yaxis.pane.fill = False
    ax.zaxis.pane.fill = False
    ax.grid(True, alpha=0.35)

    # 2D 叠加：标题与公式（不随 3D 透视变形）
    title_txt = (
        r"$\iiint_V x\,\mathrm{d}V$   "
        r"$V:\ x,y,z\geq 0,\ x+y+z\leq 1$"
    )
    fig.text(0.5, 0.96, title_txt, ha="center", va="top", fontsize=16, color="#222222")

    eq_line = fig.text(
        0.5,
        0.07,
        r"$\iiint_V x\,\mathrm{d}V = "
        r"\int_0^1 \mathrm{d}x \int_0^{1-x} \mathrm{d}y \int_0^{1-x-y} x\,\mathrm{d}z"
        r"=\frac{1}{24}$",
        ha="center",
        va="center",
        fontsize=13,
        color="#333333",
    )
    hint = fig.text(
        0.5,
        0.025,
        "3D perspective: view_init(elev, azim); formulas overlaid in 2D (no warp)",
        ha="center",
        va="bottom",
        fontsize=10,
        color="#888888",
    )

    total_frames = 240
    fps = 30

    def animate(frame: int) -> None:
        # 水平旋转一周 + 轻微俯仰，突出立体感
        azim = (frame / total_frames) * 360.0
        elev = 22.0 + 10.0 * np.sin(2 * np.pi * frame / total_frames)
        ax.view_init(elev=elev, azim=azim, roll=0)
        # 后半段再显示完整结果行（避免首帧 mathtext 过重）
        alpha = min(1.0, max(0.0, (frame - 20) / 40))
        eq_line.set_alpha(alpha)
        hint.set_alpha(min(1.0, frame / 30))

    anim = FuncAnimation(
        fig,
        animate,
        frames=total_frames,
        interval=1000 / fps,
        repeat=False,
        blit=False,
    )
    _save_anim(fig, anim, fps)
    return 0


def run_2d() -> int:
    import matplotlib.pyplot as plt
    from matplotlib.animation import FuncAnimation

    fig, ax = plt.subplots(figsize=(12.8, 7.2), facecolor="white")
    ax.set_facecolor("white")
    ax.set_aspect("equal")
    ax.axis("off")
    fig.subplots_adjust(left=0.06, right=0.94, top=0.92, bottom=0.08)

    tri_x = np.array([0.0, 1.0, 0.0, 0.0])
    tri_y = np.array([0.0, 0.0, 1.0, 0.0])

    total_frames = 180
    fps = 30

    title = (
        r"$\iiint_V x\,\mathrm{d}V$ \quad "
        r"$V:\ x,y,z\geq 0,\ x+y+z\leq 1$"
    )

    def draw_frame(frame: int) -> None:
        ax.clear()
        ax.set_facecolor("white")
        ax.axis("off")
        ax.set_xlim(-0.15, 1.35)
        ax.set_ylim(-0.2, 1.25)

        if frame < 40:
            alpha = min(1.0, frame / 25)
            ax.text(
                0.5,
                1.05,
                title,
                fontsize=20,
                ha="center",
                va="top",
                alpha=alpha,
                color="#222222",
            )
            return

        ax.text(
            0.5,
            1.05,
            title,
            fontsize=18,
            ha="center",
            va="top",
            color="#222222",
        )

        t = min(1.0, (frame - 40) / 55)
        n = max(2, int(1 + t * (len(tri_x) - 1)))
        ax.plot(tri_x[:n], tri_y[:n], color="#4A90E2", lw=2.5)
        if t >= 0.95:
            ax.fill([0, 1, 0], [0, 0, 1], color="#4A90E2", alpha=0.25)

        ax.text(0.5, -0.08, r"$D:\ x\geq0,\ y\geq0,\ x+y\leq 1$", fontsize=16, ha="center", color="#333333")

        if frame >= 100:
            prog = min(1.0, (frame - 100) / 35)
            ax.text(
                1.02,
                0.45,
                r"$0\leq z\leq 1-x-y$",
                fontsize=17,
                color="#1565c0",
                alpha=prog,
                va="center",
            )

        if frame >= 135:
            eq = (
                r"$\iiint_V x\,\mathrm{d}V = "
                r"\int_0^1 \mathrm{d}x \int_0^{1-x} \mathrm{d}y \int_0^{1-x-y} x\,\mathrm{d}z$"
            )
            ax.text(0.5, 0.42, eq, fontsize=14, ha="center", va="center", color="#222222")

        if frame >= 155:
            ax.text(
                0.5,
                0.22,
                r"$=\dfrac{1}{24}$",
                fontsize=28,
                ha="center",
                va="center",
                color="#e65100",
                fontweight="bold",
            )

    anim = FuncAnimation(fig, draw_frame, frames=total_frames, interval=1000 / fps, repeat=False)
    _save_anim(fig, anim, fps)
    return 0


def main() -> int:
    p = argparse.ArgumentParser(description="三重积分 Matplotlib 演示（默认 3D 透视）")
    p.add_argument(
        "--2d",
        action="store_true",
        dest="flat_2d",
        help="仅生成旧版 2D 平面动画（无 3D）",
    )
    args = p.parse_args()
    if args.flat_2d:
        return run_2d()
    return run_3d()


if __name__ == "__main__":
    raise SystemExit(main())
