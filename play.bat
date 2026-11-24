@echo off
setlocal
cd /d "%~dp0"

echo ==========================================================
echo   CORE BREAKER - Mini Project PBO (Java Console Edition)
echo ==========================================================
echo [1/2] Menyiapkan direktori build dan meng-compile kode Java...

if not exist "bin" mkdir "bin"
del /q "bin\*.class" 2>nul

javac -encoding UTF-8 -d "bin" src\*.java
if errorlevel 1 (
    echo.
    echo [ERROR] Kompilasi gagal! Pastikan JDK ^(javac^) sudah terinstal dan terdaftar di PATH.
    pause
    exit /b 1
)

echo [2/2] Kompilasi berhasil! Membuka jendela CMD permainan...
start "CORE BREAKER - Mini Project PBO" /D "%~dp0" cmd /c "mode con: cols=96 lines=38 & color 0F & java -cp bin Main & if errorlevel 1 pause"

exit /b 0
