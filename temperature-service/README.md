Building:

    docker build -t temperature-service:1 .
    docker build -t temperature-service:latest .

Running:

    docker run -d --rm temperature-service:latest
    docker run -d --rm -p 8081:8080 temperature-service:latest
