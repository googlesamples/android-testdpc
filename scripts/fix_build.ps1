$ErrorActionPreference = "Stop"
$GradleVersion = "8.5"
$SdkPath = "C:\Users\AhsanHussain\AppData\Local\Android\Sdk"
$ProjectRoot = Get-Location
$TempDir = Join-Path $env:TEMP "gradle_bootstrap_v5"

Write-Host "Checking build environment..."

# 1. Check/Fix local.properties
$LocalPropFile = "local.properties"
if (-not (Test-Path $LocalPropFile)) {
    Write-Host "Creating local.properties..."
    $EscapedSdkPath = $SdkPath -replace "\\", "\\"
    "sdk.dir=$EscapedSdkPath" | Out-File $LocalPropFile -Encoding utf8
} else {
    Write-Host "local.properties exists."
}

# 2. Check/Fix Gradle Wrapper
# We check for the JAR too now, to force repair if jar is missing
if (-not (Test-Path "gradle/wrapper/gradle-wrapper.jar")) {
    Write-Host "Gradle wrapper JAR missing. Bootstrapping in clean environment..."
    
    if (Test-Path $TempDir) { Remove-Item $TempDir -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $TempDir | Out-Null
    
    $ZipUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
    $ZipFile = Join-Path $TempDir "gradle.zip"
    
    Write-Host "Downloading Gradle $GradleVersion from $ZipUrl..."
    Invoke-WebRequest -Uri $ZipUrl -OutFile $ZipFile
    
    Write-Host "Unzipping..."
    Expand-Archive -Path $ZipFile -DestinationPath $TempDir
    
    $GradleBin = Join-Path $TempDir "gradle-$GradleVersion\bin\gradle.bat"
    
    Write-Host "Generating wrapper in temp dir..."
    $WrapperGenDir = Join-Path $TempDir "wrapper_gen"
    New-Item -ItemType Directory -Force -Path $WrapperGenDir | Out-Null
    
    New-Item -Path (Join-Path $WrapperGenDir "settings.gradle") -ItemType File -Force | Out-Null
    
    Push-Location $WrapperGenDir
    & $GradleBin wrapper --gradle-version $GradleVersion
    Pop-Location
    
    Write-Host "Copying wrapper files to project root..."
    Copy-Item (Join-Path $WrapperGenDir "gradlew") $ProjectRoot -Force
    Copy-Item (Join-Path $WrapperGenDir "gradlew.bat") $ProjectRoot -Force
    
    # Explicitly copy JAR
    $WrapperDest = Join-Path $ProjectRoot "gradle\wrapper"
    $WrapperSource = Join-Path $WrapperGenDir "gradle\wrapper\gradle-wrapper.jar"
    Copy-Item $WrapperSource $WrapperDest -Force
    
    Write-Host "Cleaning up..."
    try { Remove-Item $TempDir -Recurse -Force -ErrorAction SilentlyContinue } catch {}
    
    Write-Host "Wrapper restored."
} else {
    Write-Host "Gradle wrapper JAR already exists."
}

Write-Host "Bootstrap complete."
