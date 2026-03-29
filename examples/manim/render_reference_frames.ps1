# 从仓库根目录运行：对各例题渲染最后一帧 PNG，并复制到 examples/manim/reference_frames/
# 用法: .\examples\manim\render_reference_frames.ps1
$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Set-Location $root

$out = Join-Path $root "examples\manim\reference_frames"
New-Item -ItemType Directory -Force -Path $out | Out-Null

$files = @(
  "triple_lecture_1.py",
  "triple_lecture_2.py",
  "triple_direct_1.py",
  "calc_secant_tangent_1.py",
  "la_projection_1.py",
  "la_matrix_grid_1.py",
  "mech_lecture_1.py",
  "mech_lecture_2.py",
  "mech_direct_1.py",
  "mech_projectile_1.py"
)

$mediaImages = Join-Path $root "media\images"
foreach ($f in $files) {
  $rel = "examples\manim\$f"
  if (-not (Test-Path $rel)) { Write-Warning "Skip missing: $rel"; continue }
  $base = [System.IO.Path]::GetFileNameWithoutExtension($f)
  Write-Host "Last frame: $f -> $base.png"
  python -m manim render $rel GeneratedScene -qm -s --format png
  if (Test-Path $mediaImages) {
    $png = Get-ChildItem -Path $mediaImages -Recurse -Filter "*.png" -ErrorAction SilentlyContinue |
      Where-Object { $_.DirectoryName -like "*${base}*" } |
      Sort-Object LastWriteTime -Descending |
      Select-Object -First 1
    if ($png) {
      Copy-Item -Force $png.FullName (Join-Path $out "${base}.png")
      Write-Host "  copied: $($png.Name)"
    }
  }
}

Write-Host "Reference PNG dir: $out"
