@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    LevelHeaded - Prepare for build and runtime
echo ========================================

:: Parse arguments
set "NORUN=0"
set "CLEAR=0"

for %%a in (%*) do (
    if /i "%%a" == "-norun" set NORUN=1
    if /i "%%a" == "-clear" set CLEAR=1
)

:: === NEW CLEANUP TASK - Archive old logs and zips ===
echo [Cleanup] Archiving old build logs and source zips...

:: Move build*.log files
for %%f in (build_err*.log) do (
    if exist "%%f" (
        echo Moving %%f to old_errors\
        move "%%f" old_errors\ >nul
    )
)

:: Move src*.zip files (your source snapshots)
for %%f in (src*.zip) do (
    if exist "%%f" (
        echo Moving %%f to old_builds\
        move "%%f" old_builds\ >nul
    )
)

echo [Cleanup] Done. Old files moved to old_errors\ and old_builds\

:: === BUILD NUMBER MANAGEMENT ===
set BUILD_NUM_FILE=build_number.txt
if not exist %BUILD_NUM_FILE% (
    echo 001 > %BUILD_NUM_FILE%
)
set /p BUILD_NUM=<%BUILD_NUM_FILE%
set /a BUILD_NUM+=1
set BUILD_NUM=000!BUILD_NUM!
set BUILD_NUM=!BUILD_NUM:~-3!

:: === TIMESTAMP ===
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "TIMESTAMP=%dt:~0,4%-%dt:~4,2%-%dt:~6,2% %dt:~8,2%:%dt:~10,2%:%dt:~12,2%"

:: === READ VERSIONS ===
for /f "tokens=2 delims==" %%a in ('findstr /b "mod_version=" gradle.properties') do set MOD_VERSION=%%a
for /f "tokens=2 delims==" %%a in ('findstr /b "minecraft_version=" gradle.properties') do set MC_VERSION=%%a
set FULL_MOD_VERSION=%MOD_VERSION%-b%BUILD_NUM%

:: === GIT SHORT HASH ===
set GIT_HASH=unknown
if exist .git (
    for /f %%a in ('git rev-parse --short HEAD 2^>nul') do set GIT_HASH=%%a
)

:: === ZIP FILENAME with build number ===
set "ZIPFILE=src_%dt:~2,6%_%dt:~8,4%_b%BUILD_NUM%.zip"
set "LOGFILE=build_error_%dt:~2,6%_%dt:~8,4%.log"
set "BUILDOUTPUT=build_output.log"

echo Timestamp: %TIMESTAMP%
echo Build number: %BUILD_NUM%
echo Target ZIP: %ZIPFILE%

if %CLEAR%==1 (
    echo Clearing .gradle, build, and run folders...

    :: Kill any lingering Java processes (prevents lock issues)
    taskkill /f /im java.exe 2>nul

    if exist .gradle (
        rd /s /q .gradle 2>nul
        echo .gradle folder deleted
    )
    if exist build (
        rd /s /q build 2>nul
        echo build folder deleted
    )
    if exist run (
        rd /s /q run 2>nul
        echo run folder deleted
    )
    echo Clearing complete.
)

:: Delete old plain src.zip if it exists
if exist src.zip del src.zip

:: Clean previous build
echo Running clean...
call gradlew clean --quiet

:: Run build
echo Building project...
call gradlew build > "%BUILDOUTPUT%" 2>&1

set BUILD_RESULT=fail
if %errorlevel% equ 0 (
    set BUILD_RESULT=pass
    echo [SUCCESS] Build completed successfully.

    if %NORUN%==0 (
        echo.
        echo Launching Minecraft client for testing...
        echo (Close the game window when done testing)
        call gradlew runClient > run_output.log 2>&1
    ) else (
        echo.
        echo -norun flag used. Skipping client launch.
    )
) else (
    echo [BUILD FAILED] Errors detected.
    copy "%BUILDOUTPUT%" "%LOGFILE%" >nul
    echo Full error log saved as: %LOGFILE%
    echo.
    echo Last 40 lines:
    echo ----------------------------------------
    tail -n 40 "%BUILDOUTPUT%" 2>nul || type "%BUILDOUTPUT%"
    echo ----------------------------------------
)

:: === CREATE / APPEND TO Build_Status.csv ===
set CSVFILE=Build_Status.csv
if not exist %CSVFILE% (
    echo timestamp,build_number,build_result,zip_filename,mod_version,minecraft_version,git_short_hash > %CSVFILE%
)
echo %TIMESTAMP%,%BUILD_NUM%,%BUILD_RESULT%,%ZIPFILE%,%FULL_MOD_VERSION%,%MC_VERSION%,%GIT_HASH% >> %CSVFILE%

echo.
echo Build logged to Build_Status.csv (build #%BUILD_NUM% - %BUILD_RESULT%)

:: === CREATE ZIP (proper src/ structure + JAR) ===
echo.
echo Creating zip archive: %ZIPFILE%
powershell -NoProfile -Command ^
"Compress-Archive -Path build.gradle, gradle.properties, settings.gradle, prepareBuild.bat, RULES.txt, 'src', 'run/surveys', 'src/main/resources/assets/levelleaded/texts/splashes.txt' -DestinationPath '%ZIPFILE%' -Force"

:: === ADD COMPILED JAR ===
if exist "build\libs\levelleaded-*.jar" (
    echo Adding compiled mod JAR to zip...
    powershell -NoProfile -Command "Compress-Archive -Path 'build\libs\levelleaded-*.jar' -DestinationPath '%ZIPFILE%' -Update"
) else (
    echo WARNING: No JAR found in build/libs/
)

:: === ADD FULL LOGS AND SCREENSHOTS TO THE ZIP ===
echo Adding full console logs and screenshots to the ZIP...

if exist run_output.log (
    powershell -NoProfile -Command "Compress-Archive -Path 'run_output.log' -DestinationPath '%ZIPFILE%' -Update"
)

if exist "%BUILDOUTPUT%" (
    powershell -NoProfile -Command "Compress-Archive -Path '%BUILDOUTPUT%' -DestinationPath '%ZIPFILE%' -Update"
)

if exist run\logs\latest.log (
    powershell -NoProfile -Command "Compress-Archive -Path 'run\logs\latest.log' -DestinationPath '%ZIPFILE%' -Update"
)

if exist src (
    powershell -NoProfile -Command "Compress-Archive -Path 'src' -DestinationPath '%ZIPFILE%' -Update"
)

if exist preparebuild.bat (
    powershell -NoProfile -Command "Compress-Archive -Path 'preparebuild.bat' -DestinationPath '%ZIPFILE%' -Update"
)

:: Add all screenshots from run\screenshots
if exist run\screenshots (
    powershell -NoProfile -Command "Compress-Archive -Path 'run\screenshots\*.png' -DestinationPath '%ZIPFILE%' -Update" 2>nul
)

if %BUILD_RESULT%==fail (
    if exist "%LOGFILE%" (
        echo Appending failure log to zip...
        powershell -NoProfile -Command ^
        "Compress-Archive -Path '%LOGFILE%' -DestinationPath '%ZIPFILE%' -Update"
    )
)

:: Save new build number
echo %BUILD_NUM% > %BUILD_NUM_FILE%

:: Final status
if exist "%ZIPFILE%" (
    echo.
    echo Success! Created %ZIPFILE%
    echo Size: %~zZIPFILE% bytes
    echo (Now includes: proper src/ folder + compiled .jar + logs + screenshots)
) else (
    echo ERROR: Failed to create zip file.
)

:: Cleanup temporary build files
if exist "%BUILDOUTPUT%" del "%BUILDOUTPUT%"
if exist run_output.log del run_output.log

:: Kill any lingering Java processes (prevents lock issues)
taskkill /f /im java.exe 2>nul

echo.
echo Script completed.
echo Build #%BUILD_NUM% (%BUILD_RESULT%) logged.
echo All console output, screenshots, and CSV surveys have been added to the ZIP.