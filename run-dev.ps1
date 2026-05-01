# Use bundled Maven Wrapper when "mvn" is not on PATH. Infers JAVA_HOME from "java" on PATH if unset.
# Usage: .\run-dev.ps1
#        .\run-dev.ps1 test
#        .\run-dev.ps1 spring-boot:run
$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
cmd /c chcp 65001 > $null
Set-Location $PSScriptRoot

if (-not $env:JAVA_HOME) {
  $javaCmd = Get-Command java -ErrorAction SilentlyContinue
  if ($javaCmd) {
    $bin = Split-Path -Parent $javaCmd.Source
    $env:JAVA_HOME = Split-Path -Parent $bin
    Write-Host "JAVA_HOME inferred: $($env:JAVA_HOME)"
  }
}

if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
  Write-Error "JAVA_HOME is not set or invalid. Install a JDK and set JAVA_HOME."
  exit 1
}

if ($args.Count -eq 0) {
  & .\mvnw.cmd spring-boot:run
} else {
  & .\mvnw.cmd @args
}
