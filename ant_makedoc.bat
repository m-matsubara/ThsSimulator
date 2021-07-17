@echo off
setlocal

cd %~p0

rem JAVA_HOME 環境変数の検査（と設定）
if not "%JAVA_HOME%" == "" goto checkAnt
if exist \jbuilder4\jdk1.3\bin\java.exe   set JAVA_HOME=\jbuilder4\jdk1.3
if exist \jbuilder5\jdk1.3\bin\java.exe   set JAVA_HOME=\jbuilder5\jdk1.3
if exist \jdk1.3\bin\java.exe             set JAVA_HOME=\jdk1.3
if exist \jdk1.3.0\bin\java.exe           set JAVA_HOME=\jdk1.3.0
if exist \JBuilder6\jdk1.3.1\bin\java.exe set JAVA_HOME=\JBuilder6\jdk1.3.1
if exist \jdk1.3.1\bin\java.exe           set JAVA_HOME=\jdk1.3.1
if exist \jdk1.3.1_02\bin\java.exe        set JAVA_HOME=\jdk1.3.1_02
if exist \j2sdk1.4.0\bin\java.exe         set JAVA_HOME=\j2sdk1.4.0
if not "%JAVA_HOME%" == "" goto checkAnt
echo JAVA_HOME 環境変数が設定されていません。
goto end

rem ANT_HOME 環境変数の検査（と設定）
:checkAnt
if not "%ANT_HOME%" == "" goto runAnt
if exist \jakarta-ant-1.1\bin\ant.bat   set ANT_HOME=\jakarta-ant-1.1
if exist \jakarta-ant-1.2\bin\ant.bat   set ANT_HOME=\jakarta-ant-1.2
if exist \jakarta-ant-1.3\bin\ant.bat   set ANT_HOME=\jakarta-ant-1.3
if exist \jakarta-ant-1.4\bin\ant.bat   set ANT_HOME=\jakarta-ant-1.4
if exist \jakarta-ant-1.4.1\bin\ant.bat set ANT_HOME=\jakarta-ant-1.4.1
if not "%ANT_HOME%" == "" goto runAnt
echo ANT_HOME 環境変数が設定されていません。
goto end

rem ビルド
:runAnt
call %ANT_HOME%\bin\ant.bat make_doc
goto end

:end
pause
endlocal