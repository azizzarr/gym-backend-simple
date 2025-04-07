@echo off
echo Deploying to Railway...

echo Checking if Railway CLI is installed...
railway --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
  echo Railway CLI is not installed. Installing...
  npm install -g @railway/cli
)

echo Logging in to Railway...
railway login

echo Linking to Railway project...
echo Please enter your Railway project name:
set /p project_name=
railway link %project_name%

echo Building the application...
mvn clean package -DskipTests

echo Deploying to Railway...
railway up

echo Deployment complete! Check your Railway dashboard for the deployment URL. 