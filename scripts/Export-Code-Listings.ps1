# Generate a consolidated Markdown source-code appendix from tracked SmartStay files.
# This is NOT a substitute for the submitted report. Review for length, accuracy and personal data.
$ErrorActionPreference = 'Stop'
$root = (git rev-parse --show-toplevel).Trim()
if (-not $root) { throw 'Run in the SmartStay Git checkout.' }
Push-Location $root
try {
  $dir = Join-Path $root 'submission'
  New-Item -ItemType Directory -Force -Path $dir | Out-Null
  $destination = Join-Path $dir 'SmartStay-Code-Listings.md'
  $tracked = @(git ls-files | Where-Object {
    $_ -match '^backend/src/(main|test)/.*\.(java|yml|sql)$' -or
    $_ -match '^frontend/src/.*\.(ts|tsx|css)$'
  } | Sort-Object)
  $builder = New-Object System.Text.StringBuilder
  [void]$builder.AppendLine('# SmartStay complete code listings')
  [void]$builder.AppendLine()
  [void]$builder.AppendLine("Generated at $(Get-Date -Format 'yyyy-MM-dd HH:mm') from Git commit $(git rev-parse --short HEAD).")
  [void]$builder.AppendLine('Review this appendix and follow the final coursework instructions. It is generated, not a substitute for the class diagram or test cases.')
  [void]$builder.AppendLine()
  foreach ($path in $tracked) {
    $language = [IO.Path]::GetExtension($path).TrimStart('.')
    if ($language -eq 'tsx') { $language = 'tsx' }
    elseif ($language -eq 'ts') { $language = 'typescript' }
    elseif ($language -eq 'java') { $language = 'java' }
    elseif ($language -eq 'yml') { $language = 'yaml' }
    $source = (git show "HEAD:$path" | Out-String)
    [void]$builder.AppendLine("## $path")
    [void]$builder.AppendLine()
    [void]$builder.AppendLine("~~~$language")
    [void]$builder.Append($source.TrimEnd())
    [void]$builder.AppendLine()
    [void]$builder.AppendLine('~~~')
    [void]$builder.AppendLine()
  }
  [IO.File]::WriteAllText($destination, $builder.ToString(), [Text.UTF8Encoding]::new($false))
  Write-Host "Source-code listing created: $destination"
  Write-Host "Included $($tracked.Count) committed source files. Manually verify all required listings and screenshots."
}
finally { Pop-Location }
