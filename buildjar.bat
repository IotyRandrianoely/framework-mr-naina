@echo off
setlocal EnableDelayedExpansion

REM Configuration des variables
SET "ROOT_DIR=%~dp0"
SET "SRC_DIR=%ROOT_DIR%sprint6\src\main\java"
SET "BUILD_DIR=%ROOT_DIR%buildjar"
SET "LIB_DIR=%ROOT_DIR%sprint6\lib"
SET "JAR_NAME=FirstServletFramework.jar"
SET "SERVLET_API_JAR=%LIB_DIR%\servlet-api.jar"

REM Nettoyage de l'ancien build
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"

REM Compilation des fichiers Java
echo Compilation des fichiers Java du framework...

REM Creation de la liste des fichiers sources
set "SOURCES="
for /r "%SRC_DIR%" %%i in (*.java) do (
    set "SOURCES=!SOURCES! "%%i""
)

REM Compilation avec encodage UTF-8
javac -encoding UTF-8 -cp "%SERVLET_API_JAR%" -d "%BUILD_DIR%" %SOURCES%

if errorlevel 1 (
    echo Erreur de compilation
    pause
    exit /b 1
)

REM Creation du JAR du framework
pushd "%BUILD_DIR%"
jar cvf "%ROOT_DIR%%JAR_NAME%" *
popd

REM Nettoyage du dossier temporaire
rmdir /s /q "%BUILD_DIR%"

echo JAR du framework cree : %JAR_NAME%

endlocal