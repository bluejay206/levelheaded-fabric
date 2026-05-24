@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    LevelHeaded - Creating src.zip
echo ========================================

if exist src.zip (
    echo Deleting old src.zip...
    del src.zip
    if exist src.zip echo Warning: Could not delete old src.zip
)

echo Packing files...

tar -cf src.zip ^
    build.gradle ^
    gradle.properties ^
    settings.gradle ^
    src\*

if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] src.zip created successfully!
    for %%F in (src.zip) do echo Size: %%~zF bytes
) else (
    echo.
    echo [ERROR] Failed to create src.zip
)

echo.
pause