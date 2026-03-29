# 在 mathanim-desktop 目录下执行；示例脚本若不存在请先生成或改用 examples/manim 下题目
$root = $PSScriptRoot
Set-Location $root
Write-Host "[MathAnim] Working directory: $root"
if (Test-Path "triple_integral_ai_scene_fixed.py") {
  python -m manim render triple_integral_ai_scene_fixed.py GeneratedScene -qm --fps 30
} else {
  Write-Warning "未找到 triple_integral_ai_scene_fixed.py，请改用 examples/manim 中的脚本或先生成该文件。"
}
