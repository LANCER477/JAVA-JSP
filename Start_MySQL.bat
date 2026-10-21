@echo off
chcp 65001 > nul
echo Запуск службы MySQL84...
net start MySQL84
echo.
if %errorlevel% neq 0 (
    echo [!] Ошибка: запустите этот файл правой кнопкой мыши -> "Запуск от имени администратора"
) else (
    echo [+] MySQL успешно запущен! Теперь можете перезапустить Tomcat.
)
echo.
pause
