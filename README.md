
#Getting Started

## Prerequisites
* [JDK 17](https://www.oracle.com/java/technologies/downloads/)
* [Maven](https://maven.apache.org/download.cgi)
* [Docker Compose](https://docs.docker.com/compose/install/)

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
Use [Apache jMeter](https://jmeter.apache.org/download_jmeter.cgi) to run file *.jmx in folder `http`