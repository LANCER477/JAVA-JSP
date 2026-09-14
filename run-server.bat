@echo off
chcp 65001 > nul
echo ========================================================
echo   Java 231 Web - Запуск проєкту та Apache Tomcat
echo ========================================================

set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
set "CATALINA_HOME=C:\Users\maksi\Downloads\apache-tomcat-11.0.25"
set "MAVEN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"

echo [1/3] Компіляція та збірка проєкту через Maven...
call "%MAVEN_CMD%" package -DskipTests=false
if %ERRORLEVEL% NEQ 0 (
    echo [ПОМИЛКА] Збірка не вдалася!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Копіювання war-файлу до Tomcat webapps...
copy /Y "target\java231web-1.0-SNAPSHOT.war" "%CATALINA_HOME%\webapps\java231web.war"

echo.
echo [3/3] Відкриття браузера та запуск Tomcat...
start http://localhost:8080/java231web/
echo.
echo Сервер запущено. Для зупинки натисніть Ctrl+C в цьому вікні.
echo.
call "%CATALINA_HOME%\bin\catalina.bat" run
