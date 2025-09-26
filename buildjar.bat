@echo off

REM Obtenir le chemin du script
SET SCRIPT_DIR=%~dp0

REM Configuration des variables avec chemins absolus
SET SRC_DIR=%SCRIPT_DIR%framework\src\main\java
SET BUILD_DIR=%SCRIPT_DIR%buildjar
SET LIB_DIR=%SCRIPT_DIR%framework\lib
SET JAR_NAME=FirstServletFramework.jar
SET SERVLET_API_JAR=%LIB_DIR%\servlet-api.jar

REM Nettoyage de l'ancien build
if exist "%BUILD_DIR%" (
    rmdir /s /q "%BUILD_DIR%"
)
mkdir "%BUILD_DIR%"

REM Compilation des fichiers .java
echo Compilation des fichiers Java du framework...
dir /b /s "%SRC_DIR%\*.java" > "%BUILD_DIR%\sources.txt"
javac -cp "%SERVLET_API_JAR%" -d "%BUILD_DIR%" @"%BUILD_DIR%\sources.txt"
del "%BUILD_DIR%\sources.txt"

REM Création du JAR du framework
pushd "%BUILD_DIR%"
jar cvf "%SCRIPT_DIR%%JAR_NAME%" *
popd

REM Nettoyage du dossier temporaire
rmdir /s /q "%BUILD_DIR%"

echo JAR du framework créé : %JAR_NAME%