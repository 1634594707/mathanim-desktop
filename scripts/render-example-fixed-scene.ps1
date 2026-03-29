# 渲染 mathanim-desktop 根目录下的 triple_integral_ai_scene_fixed.py（若存在）
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
Write-Host "Working directory: $root"
# -qm 720p30；-qh 1080p30（与桌面端画质选项对应）
& python -m manim render triple_integral_ai_scene_fixed.py GeneratedScene -qm --fps 30
