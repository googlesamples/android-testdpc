$ErrorActionPreference = "Stop"
$GradleVersion = "8.5"
$SdkPath = "C:\Users\AhsanHussain\AppData\Local\Android\Sdk"
$WorkDir = Get-Location
$TempDir = Join-Path $env:TEMP "gradle_bootstrap"

Write-Host "Checking build environment..."

# 1. Check/Fix local.properties
$LocalPropFile = "local.properties"
if (-not (Test-Path $LocalPropFile)) {
    Write-Host "Creating local.properties..."
    # Escape backslashes for properties file (single backslash is escape char in properties, so we need double)
    # But wait, Java properties usually handle single backslashes on Windows if not followed by special chars, 
    # but strictly they should be escaped. Android Studio usually writes sdk.dir=C\:\\Path
    $EscapedSdkPath = $SdkPath -replace "\\", "\\"
    "sdk.dir=$EscapedSdkPath" | Out-File $LocalPropFile -Encoding utf8
} else {
    Write-Host "local.properties exists."
}

# 2. Check/Fix Gradle Wrapper
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "Gradle wrapper missing. Bootstrapping..."
    
    if (Test-Path $TempDir) { Remove-Item $TempDir -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $TempDir | Out-Null
    
    $ZipUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
    $ZipFile = Join-Path $TempDir "gradle.zip"
    
    Write-Host "Downloading Gradle $GradleVersion from $ZipUrl..."
    Invoke-WebRequest -Uri $ZipUrl -OutFile $ZipFile
    
    Write-Host "Unzipping..."
    Expand-Archive -Path $ZipFile -DestinationPath $TempDir
    
    $GradleBin = Join-Path $TempDir "gradle-$GradleVersion\bin\gradle.bat"
    
    Write-Host "Generating wrapper..."
    & $GradleBin wrapper --gradle-version $GradleVersion
    
    Write-Host "Cleaning up..."
    Remove-Item $TempDir -Recurse -Force
    
    Write-Host "Wrapper restored."
} else {
    Write-Host "Gradle wrapper already exists."
}

Write-Host "Bootstrap complete."
