# TQS Air Quality Meter
Minimalist web app that provides details on air quality for a certain region/city


## Overview
This application is the result of an assignment of the [Software Quality And Tests](https://www.ua.pt/pt/uc/8109) course. The goal was to develop a multi-layer web application in Spring Boot with automated tests of different types (unit tests, service level tests, integration tests, and functional testing, mainly on the web interface). \
In particular, this application should provide details on air quality for a given location (provided by the user) showing metrics like particles in suspension or gases present. To achieve these objectives, the project should include different components: a web page, that allows the interaction with the user; integration with external sources, like a third-party API to fetch that air quality data; an in-memory cache to save the latest results from that API; and its own REST API that can be called by external clients and obtain air quality data as well.

## How to run
The application project is the folder "airquality" in this repository. This will be our root directory.\
You can test this application by downloading the source code in this repository and run it directly from your favorite IDE.\
If you prefer, you can run it throught the command line. For that, you can execute `mvn spring-boot:run` in the root of the project or run the packaged application version (a JAR) executing java `-jar target/<jar_path>`, being `jar_path` the path of JAR archive of the application. If you are in the root of the project, `jar_path` will be `target/airquality-0.0.1-SNAPSHOT.jar`.\
\
Once the application is running, you can access the url `http://localhost:8080/` in your web browser and interact with the user interface.

## Documentation
More information about the overall project development and the developed API can be seen in the report available in this repository.

## License
This project is licensed under the MIT License - see the [LICENSE file](https://github.com/alexandrejflopes/tqs-air-quality-app/blob/master/LICENSE) for details.
