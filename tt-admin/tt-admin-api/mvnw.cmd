@REM Maven Wrapper for Windows - minimal run script
@echo off
setlocal

if "%JAVA_HOME%"=="" (
  echo [ERROR] JAVA_HOME is not set. Please set JAVA_HOME to your JDK 17 path, or run the app from IDEA.
  exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] JAVA_HOME does not point to a valid JDK: %JAVA_HOME%
  exit /b 1
)

set MAVEN_PROJECTBASEDIR=%~dp0
cd /d "%MAVEN_PROJECTBASEDIR%"

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

if not exist %WRAPPER_JAR% (
  echo Downloading Maven wrapper...
  powershell -NoProfile -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%') }"
  if errorlevel 1 exit /b 1
)

"%JAVA_HOME%\bin\java.exe" -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
