#!/bin/bash

echo "Deploying to Railway..."

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
  echo "Railway CLI is not installed. Installing..."
  npm install -g @railway/cli
fi

# Login to Railway
echo "Logging in to Railway..."
railway login

# Link to Railway project
echo "Linking to Railway project..."
echo "Please enter your Railway project name:"
read project_name
railway link $project_name

# Deploy to Railway
echo "Deploying to Railway..."
railway up

echo "Deployment complete! Check your Railway dashboard for the deployment URL." 