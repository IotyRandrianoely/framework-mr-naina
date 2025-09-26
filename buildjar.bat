@echo off

REM Configuration des variables
SET SRC_DIR=framework\src\main\java
SET BUILD_DIR=buildjar
SET LIB_DIR=framework\lib
SET JAR_NAME=FirstServletFramework.jar
SET SERVLET_API_JAR=%LIB_DIR%\servlet-api.jar

REM Nettoyage de l'ancien build
if exist %BUILD_DIR% (
    rmdir /s /q %BUILD_DIR%
)
mkdir %BUILD_DIR%

REM Compilation des fichiers .java
echo Compilation des fichiers Java du framework...
dir /b /s "%CD%\%SRC_DIR%\*.java" > sources.txt
javac -cp "%CD%\%SERVLET_API_JAR%" -d "%CD%\%BUILD_DIR%" @sources.txt
del sources.txt

REM Création du JAR du framework
cd %BUILD_DIR%
jar cvf ..\%JAR_NAME% *
cd ..

REM Nettoyage du dossier temporaire
rmdir /s /q %BUILD_DIR%

echo JAR du framework créé : %JAR_NAME%