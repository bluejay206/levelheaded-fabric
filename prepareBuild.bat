@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    SurveyorSays - Prepare for build and runtime
echo ========================================

:: Get current timestamp in YYMMDD_HHMM format
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "timestamp=%dt:~2,6%_%dt:~8,4%"

set "ZIPFILE=src_%timestamp%.zip"
set "LOGFILE=build_error_%timestamp%.log"
set "BUILDOUTPUT=build_output.log"

echo Timestamp: %timestamp%
echo Target ZIP: %ZIPFILE%

:: Delete old plain src.zip if it exists
if exist src.zip (
    echo Deleting old src.zip...
    del src.zip
)

:: Clean previous build
echo Running clean...
call gradlew clean --quiet

:: Run build and capture all output
echo Building project...
call gradlew build > "%BUILDOUTPUT%" 2>&1

set BUILD_FAILED=0
if %errorlevel% neq 0 (
    set BUILD_FAILED=1
    echo.
    echo [BUILD FAILED] Errors detected.

    copy "%BUILDOUTPUT%" "%LOGFILE%" >nul
    echo Full error log saved as: %LOGFILE%

    echo.
    echo Last 40 lines of build output:
    echo ----------------------------------------
    tail -n 40 "%BUILDOUTPUT%" 2>nul || type "%BUILDOUTPUT%"
    echo ----------------------------------------
) else (
    echo [SUCCESS] Build completed successfully.

    :: === NEW: Auto-launch client on successful build ===
    echo.
    echo Launching Minecraft client for testing...
    echo (Close the game window when done testing)
    call gradlew runClient
)

:: Create proper zip archive using PowerShell
echo.
echo Creating proper zip archive: %ZIPFILE%

powershell -NoProfile -Command ^
"Compress-Archive -Path build.gradle, gradle.properties, settings.gradle, prepareBuild.bat, 'src/main', 'src/client', 'src/main/resources/assets/surveyorsays/texts/splashes.txt' -DestinationPath '%ZIPFILE%' -Force"

:: If build failed, append the error log to the zip
if %BUILD_FAILED%==1 (
    if exist "%LOGFILE%" (
        echo Appending failure log to zip...
        powershell -NoProfile -Command ^
        "Compress-Archive -Path '%LOGFILE%' -DestinationPath '%ZIPFILE%' -Update"
        echo Failure log included.
    )
)

:: Final status
if exist "%ZIPFILE%" (
    echo.
    echo Success! Created %ZIPFILE%
    echo Size: %~zZIPFILE% bytes
    if %BUILD_FAILED%==1 (
        echo Build failed - error log has been added to the zip.
    ) else (
        echo Build succeeded + client launched. Ready for upload.
    )
) else (
    echo ERROR: Failed to create zip file.
)

:: Cleanup temporary files
if exist "%BUILDOUTPUT%" del "%BUILDOUTPUT%"

echo.
echo Script completed.