#!/bin/bash

# Build the application
echo "Building the application..."
./mvnw clean package -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
  echo "Build successful!"
  
  # Run the application
  echo "Starting the application..."
  java -jar target/backend-0.0.1-SNAPSHOT.jar
else
  echo "Build failed. Please check the logs for errors."
  exit 1
fi 