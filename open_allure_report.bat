@echo off
echo ===================================================
echo   🚀 Launching Allure Test Report...
echo   (Maven will auto-download dependencies if needed)
echo ===================================================
cd /d "%~dp0"
call mvn allure:serve
pause
