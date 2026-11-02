@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    LevelHeaded - Prepare for build and runtime
echo ========================================

:: Parse arguments
set "NORUN=0"
set "CLEAR=0"
set "TARGET=mc262"

for %%a in (%*) do (
    if /i "%%a" == "-norun" set NORUN=1
    if /i "%%a" == "-clear" set CLEAR=1
    if /i "%%a" == "-1211" set TARGET=mc1211
    if /i "%%a" == "-all" set TARGET=all
)

echo [Cleanup] Archiving old build logs and source zips...
if not exist old_errors mkdir old_errors
if not exist old_builds mkdir old_builds

for %%f in (build_err*.log) do (
    if exist "%%f" move "%%f" old_errors\ >nul
)
for %%f in (src*.zip) do (
    if exist "%%f" move "%%f" old_builds\ >nul
)

set BUILD_NUM_FILE=build_number.txt
if not exist %BUILD_NUM_FILE% echo 000 > %BUILD_NUM_FILE%
set /p BUILD_NUM=<%BUILD_NUM_FILE%
set /a BUILD_NUM+=1
set BUILD_NUM=000!BUILD_NUM!
set BUILD_NUM=!BUILD_NUM:~-3!

for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "TIMESTAMP=%dt:~0,4%-%dt:~4,2%-%dt:~6,2% %dt:~8,2%:%dt:~10,2%:%dt:~12,2%"

for /f "tokens=2 delims==" %%a in ('findstr /b "mod_version=" gradle.properties') do set MOD_VERSION=%%a
set GIT_HASH=unknown
if exist .git (
    for /f %%a in ('git rev-parse --short HEAD 2^>nul') do set GIT_HASH=%%a
)

set "ZIPFILE=src_%dt:~2,6%_%dt:~8,4%_b%BUILD_NUM%.zip"
set "LOGFILE=build_error_%dt:~2,6%_%dt:~8,4%.log"
set "BUILDOUTPUT=build_output.log"

echo Timestamp: %TIMESTAMP%
echo Build number: %BUILD_NUM%
echo Target: %TARGET%
echo Target ZIP: %ZIPFILE%

if %CLEAR%==1 (
    echo Clearing .gradle and build folders...
    taskkill /f /im java.exe 2>nul
    if exist .gradle rd /s /q .gradle 2>nul
    if exist build rd /s /q build 2>nul
    if exist mc262\build rd /s /q mc262\build 2>nul
    if exist mc1211\build rd /s /q mc1211\build 2>nul
    if exist core\build rd /s /q core\build 2>nul
)

if exist src.zip del src.zip

echo Running clean...
call gradlew clean --quiet

if "%TARGET%"=="all" (
    echo Building all targets...
    call gradlew buildAll > "%BUILDOUTPUT%" 2>&1
) else (
    echo Building :%TARGET%...
    call gradlew :%TARGET%:build > "%BUILDOUTPUT%" 2>&1
)

set BUILD_RESULT=fail
if %errorlevel% equ 0 (
    set BUILD_RESULT=pass
    echo [SUCCESS] Build completed successfully.

    if %NORUN%==0 if not "%TARGET%"=="all" (
        echo.
        echo Launching :%TARGET%:runClient...
        call gradlew :%TARGET%:runClient > run_output.log 2>&1
    )
) else (
    echo [BUILD FAILED]
    copy "%BUILDOUTPUT%" "%LOGFILE%" >nul
    echo Full error log: %LOGFILE%
)

set CSVFILE=Build_Status.csv
if not exist %CSVFILE% (
    echo timestamp,build_number,build_result,zip_filename,mod_version,target,git_short_hash > %CSVFILE%
)
echo %TIMESTAMP%,%BUILD_NUM%,%BUILD_RESULT%,%ZIPFILE%,%MOD_VERSION%,%TARGET%,%GIT_HASH% >> %CSVFILE%

echo Creating zip: %ZIPFILE%
powershell -NoProfile -Command ^
"$items = @('build.gradle','gradle.properties','settings.gradle','prepareBuild.bat','README.md','RULES.txt','core','shared','mc262','mc1211'); $exist = $items | Where-Object { Test-Path $_ }; Compress-Archive -Path $exist -DestinationPath '%ZIPFILE%' -Force"

:: jars
for %%j in (mc262\build\libs\levelheaded-*.jar mc1211\build\libs\levelheaded-*.jar) do (
    if exist "%%j" (
        echo Adding %%j
        powershell -NoProfile -Command "Compress-Archive -Path '%%j' -DestinationPath '%ZIPFILE%' -Update"
    )
)

:: shot CSVs from the 26.2 run dir
if exist mc262\run\levelheaded\shots_*.csv (
    powershell -NoProfile -Command "Get-ChildItem 'mc262\run\levelheaded\shots_*.csv' | ForEach-Object { Compress-Archive -Path $_.FullName -DestinationPath '%ZIPFILE%' -Update }"
)

if exist "%BUILDOUTPUT%" powershell -NoProfile -Command "Compress-Archive -Path '%BUILDOUTPUT%' -DestinationPath '%ZIPFILE%' -Update"
if exist run_output.log powershell -NoProfile -Command "Compress-Archive -Path 'run_output.log' -DestinationPath '%ZIPFILE%' -Update"
if exist mc262\run\logs\latest.log powershell -NoProfile -Command "Compress-Archive -Path 'mc262\run\logs\latest.log' -DestinationPath '%ZIPFILE%' -Update"

if %BUILD_RESULT%==fail if exist "%LOGFILE%" (
    powershell -NoProfile -Command "Compress-Archive -Path '%LOGFILE%' -DestinationPath '%ZIPFILE%' -Update"
)

echo %BUILD_NUM% > %BUILD_NUM_FILE%

if exist "%ZIPFILE%" (
    echo Success: %ZIPFILE%
) else (
    echo ERROR: zip was not created.
)

if exist "%BUILDOUTPUT%" del "%BUILDOUTPUT%"
if exist run_output.log del run_output.log
taskkill /f /im java.exe 2>nul

echo.
echo Build #%BUILD_NUM% (%BUILD_RESULT%) target=%TARGET% logged.
