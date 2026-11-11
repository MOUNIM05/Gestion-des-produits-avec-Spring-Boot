param([Parameter(ValueFromRemainingArguments = $true)][string[]]$MavenArguments = @('verify'))
$ErrorActionPreference = 'Stop'
$cacheRoot = Join-Path ([System.IO.Path]::GetPathRoot($PSScriptRoot)) 'Caches\gestion-produits'
$repository = Join-Path $cacheRoot 'repository'
$tempDirectory = Join-Path $cacheRoot 'temp'
New-Item -ItemType Directory -Path $repository, $tempDirectory -Force | Out-Null
$previousMavenHome = $env:MAVEN_USER_HOME
$previousTemp = $env:TEMP
$previousTmp = $env:TMP
Push-Location $PSScriptRoot
try {
    $env:MAVEN_USER_HOME = $cacheRoot
    $env:TEMP = $tempDirectory
    $env:TMP = $tempDirectory
    & "$PSScriptRoot\mvnw.cmd" "-Dmaven.repo.local=$repository" "-Djava.io.tmpdir=$tempDirectory" @MavenArguments
    $mavenExitCode = $LASTEXITCODE
} finally {
    Pop-Location
    $env:MAVEN_USER_HOME = $previousMavenHome
    $env:TEMP = $previousTemp
    $env:TMP = $previousTmp
}
exit $mavenExitCode