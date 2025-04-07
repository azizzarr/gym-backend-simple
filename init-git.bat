@echo off
echo Initializing Git repository...
git init

echo Adding files to Git...
git add .

echo Committing changes...
git commit -m "Initial commit"

echo Adding GitHub remote...
echo Please enter your GitHub repository URL:
set /p repo_url=
git remote add origin %repo_url%

echo Pushing to GitHub...
git push -u origin master

echo Done! Your code is now on GitHub. 