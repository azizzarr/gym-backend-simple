#!/bin/bash

# Initialize Git repository
echo "Initializing Git repository..."
git init

# Add all files
echo "Adding files to Git..."
git add .

# Commit changes
echo "Committing changes..."
git commit -m "Initial commit"

# Add GitHub remote (replace with your repository URL)
echo "Adding GitHub remote..."
echo "Please enter your GitHub repository URL:"
read repo_url
git remote add origin $repo_url

# Push to GitHub
echo "Pushing to GitHub..."
git push -u origin master

echo "Done! Your code is now on GitHub." 