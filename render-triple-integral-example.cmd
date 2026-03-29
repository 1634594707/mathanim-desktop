@echo off
REM triple_integral_ai_scene_fixed.py 在上一级目录 MathAnim\，在 mathanim-desktop 里直接 manim 会找不到文件
set "ROOT=%~dp0.."
cd /d "%ROOT%"
echo [MathAnim] cd /d %CD%
python -m manim render triple_integral_ai_scene_fixed.py GeneratedScene -qm --fps 30
