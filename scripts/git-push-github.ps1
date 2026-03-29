# 推送到 GitHub：git@github.com:1634594707/mathanim-desktop.git
# 在 PowerShell 中执行： .\scripts\git-push-github.ps1
# 需已配置 SSH 公钥到 GitHub，且对仓库有写权限。

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

if (-not (Test-Path .git)) {
  git init
}

git remote remove origin 2>$null
git remote add origin "git@github.com:1634594707/mathanim-desktop.git"

git add -A
$status = git status --porcelain
if (-not $status) {
  Write-Host "无变更可提交。"
} else {
  git commit -m "chore: initial push MathAnim desktop (JavaFX + GeoGebra)"
}

git branch -M main
git push -u origin main

Write-Host "完成。若首次推送被拒绝，可在 GitHub 创建空仓库后再执行本脚本。"
