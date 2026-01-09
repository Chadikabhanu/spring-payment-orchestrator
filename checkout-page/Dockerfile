# Use Node 18 Alpine image
FROM node:18-alpine

# Set working directory
WORKDIR /app

# Copy package files first (better caching)
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Expose port and start the app
EXPOSE 80
CMD ["npm", "run", "dev", "--", "--host", "--port", "80"]