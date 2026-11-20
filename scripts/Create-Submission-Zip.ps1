# Creates a clean, tracked-source archive for the CI6115 coursework hand-in.
# Must be run from a cloned Git repository. Does not include untracked secrets.
$ErrorActionPreference = 'Stop'
$root = (git rev-parse --show-toplevel).Trim()
if (-not $root) { throw 'Run this command inside the SmartStay Git checkout.' }
Push-Location $root
try {
  $destination = Join-Path $root 'submission'
  New-Item -ItemType Directory -Path $destination -Force | Out-Null
  $out = Join-Path $destination 'SmartStay-source.zip'
  if (Test-Path $out) { Remove-Item $out -Force }
  # Git archive only exports committed, tracked files. .env and build output stay excluded.
  git archive --format=zip --output=$out HEAD
  if ($LASTEXITCODE -ne 0) { throw 'git archive failed.' }
  $bytes = (Get-Item $out).Length
  Write-Host "Source ZIP created: $out ($bytes bytes)"
  Write-Host 'Review the ZIP for personal or secret files before submission.'
  Write-Host 'This ZIP does not replace the required consolidated coursework report.'
}
finally { Pop-Location }
