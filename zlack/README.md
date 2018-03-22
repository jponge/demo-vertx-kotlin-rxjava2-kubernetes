Mongo:

    docker-compose -f docker-compose-for-testing.yml up

Backend API:

    http GET localhost:8080/api/messages
    http POST localhost:8080/api/messages author='Julien' content='Yo!'

Docker:

    docker build -t zlack:latest .
