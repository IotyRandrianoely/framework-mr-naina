@echo off
setlocal EnableDelayedExpansion

REM Configuration des variables
SET "ROOT_DIR=%~dp0"
SET "SRC_DIR=%ROOT_DIR%framework\src\main\java"
SET "BUILD_DIR=%ROOT_DIR%buildjar"
SET "LIB_DIR=%ROOT_DIR%framework\lib"
SET "JAR_NAME=FirstServletFramework.jar"
SET "SERVLET_API_JAR=%LIB_DIR%\servlet-api.jar"

REM Nettoyage de l'ancien build
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"

REM Compilation des fichiers Java
echo Compilation des fichiers Java du framework...

REM Création de la liste des fichiers sources d'une manière plus sûre
set "SOURCES="
for /r "%SRC_DIR%" %%i in (*.java) do (
    set "SOURCES=!SOURCES! "%%i""
)

REM Compilation avec les chemins entre guillemets
javac -cp "%SERVLET_API_JAR%" -d "%BUILD_DIR%" %SOURCES%

REM Création du JAR du framework
pushd "%BUILD_DIR%"
jar cvf "%ROOT_DIR%%JAR_NAME%" *
popd

REM Nettoyage du dossier temporaire
rmdir /s /q "%BUILD_DIR%"

echo JAR du framework créé : %JAR_NAME%

endlocal