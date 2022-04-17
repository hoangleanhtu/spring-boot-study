
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

## Load tests
Use Apache jMeter to run file *.jmx in folder `http`