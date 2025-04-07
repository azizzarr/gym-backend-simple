# Deploying to Railway

This guide will help you deploy your Spring Boot application to Railway.

## Prerequisites

- A Railway account (sign up at [railway.app](https://railway.app))
- Node.js and npm (for the Railway CLI)
- Git

## Method 1: Using the Railway Dashboard (Recommended)

### Step 1: Push your code to GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin <your-github-repo-url>
git push -u origin main
```

### Step 2: Create a new project in Railway

1. Go to [railway.app](https://railway.app)
2. Click "New Project"
3. Select "Deploy from GitHub repo"
4. Connect your GitHub account if you haven't already
5. Select the repository containing your code
6. Click "Deploy Now"

### Step 3: Configure your project

1. In the Railway dashboard, go to your project
2. Click on the service that was created
3. Go to the "Settings" tab
4. Configure the following:
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/backend-0.0.1-SNAPSHOT.jar`
   - Add environment variables:
     - ADMIN_PASSWORD: (generate a secure password)
     - SPRING_PROFILES_ACTIVE: prod

### Step 4: Deploy your application

1. Go to the "Deployments" tab
2. Click "Deploy Now"

## Method 2: Using the Railway CLI

### Step 1: Install the Railway CLI

```bash
npm install -g @railway/cli
```

### Step 2: Login to Railway

```bash
railway login
```

### Step 3: Create a new project in Railway

1. Go to [railway.app](https://railway.app)
2. Click "New Project"
3. Select "Empty Project"
4. Give your project a name (e.g., "gymapp-backend")
5. Click "Create Project"

### Step 4: Link your local project to Railway

```bash
railway link
```

When prompted, select the project you created in Step 3.

### Step 5: Deploy your application

```bash
railway up
```

This will deploy your application to Railway. Once the deployment is complete, you'll get a URL where your application is accessible.

## Testing Your Deployment

After deployment, you can test your application by accessing:
```
https://your-railway-url/api/public/test
```

You should see the message: "Backend is working!"

## Troubleshooting

If you encounter any issues during deployment, check the logs in the Railway dashboard. Common issues include:

- Build failures: Check if your application builds locally
- Start failures: Check if your application starts locally
- Environment variables: Make sure all required environment variables are set

## Additional Resources

- [Railway Documentation](https://docs.railway.app/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot) 