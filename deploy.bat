@echo off
echo Building the application...
call mvnw clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
  echo Build successful!
  
  echo Starting the application...
  java -jar target/backend-0.0.1-SNAPSHOT.jar
) else (
  echo Build failed. Please check the logs for errors.
  exit /b 1
) 