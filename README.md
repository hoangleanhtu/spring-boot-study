#Getting Started

## Prerequisites
* JDK 17
* Docker Compose

## Run Database
Go to folder `infras` and run:
```
docker-compose up -d
```

## Run Application
At root of project, run:
```
mvn spring-boot:run
```

Test an API:
```
curl http://127.0.0.1:8181/accounts
```