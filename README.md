# NASK_recruitment_task
> NASK recruitment task for backend developer position

Project working as a REST API proxy for [SWAPI](https://swapi.dev) star wars database API. 
Its main functionality is to add homeworld and starhips data to person object both for single person and for page.
It exposes two endpoints:

`/characters?page={pageNum}` - returns page json with number of Star Wars people, where `{pageNum}` is a page number

`/characters/{id}` - returns a person json for specific `{id}`

For more details in polish language look at [task_details](/task_details/zadanie-rekrutacyjne-backend.pdf).

## Installing / Getting started

Tu run this application you need to have Java version 11 installed on your computer as well as maven.

Tu run project run the following scripts in the root folder.
In case you are running it on Windows operating system substitute `mvn` with `mvnw`

```shell
mvn clean install spring-boot:run
```

Above script will install all dependencies and run a dev server listening on `localhost:8080`.

Additionally, application exposes swagger documentation of rest endpoints at `/swagger-ui/`.
Prometheus scrape page is exposed at `/actuator/prometheus/`.

### Initial Configuration

Initial server port on which application is listening is 8080,
and it is specified in [application.properties](/src/main/resources/application.properties).
Additionally, it contains address of the SWAPI and configuration for prometheus monitoring.

### Deploying / Publishing

This project uses docker as a platform for deploying application.
Running following shell script in project root folder will allow for quick and easy creation of docker image.
Again if you are using Windows operating system replace `mvn` with `mvnw`.

```shell
mvn clean package
docker build -t nask/nask-recruitment-task .
```

This script will firstly package the project into a .jar file
and then use the configuration placed in [Dockerfile](/Dockerfile) to create a docker image.
the `-t` flag specifies what will be the name of created image.
After creating an image you can start it using:

```shell
docker run -d -p {desired-port}:8080 --name nask_task nask/nask-recruitment-task
```
 
Above script runs a docker image `nask/nask-recruitment-task`.

Flag `-d` detaches process from command line so it will start in a background.

Flag `-p` publishes a `{desired-port}` port on which the docker container
will be listening and maps it on `8080` port inside a container on which application is listening.

Flag `--name nask_task` creates a name for this container for easier management.

## Licensing

The code in this project is licensed under GNU GENERAL PUBLIC LICENSE.