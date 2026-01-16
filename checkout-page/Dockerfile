# Use a lightweight web server (Nginx)
FROM nginx:alpine

# Remove default Nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy OUR specific files to the server
COPY checkout.html /usr/share/nginx/html/index.html
COPY checkout.html /usr/share/nginx/html/checkout.html
COPY checkout.js /usr/share/nginx/html/checkout.js

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
