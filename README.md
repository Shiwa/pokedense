# Pokedense

Prototype of a pokemon list with weight/height information and filtering ([following provided constraints](https://miuros.notion.site/Full-stack-Engineer-Coding-test-a5939dbbce334fd999901f8579f429cf)).

## Setup

The project uses jdk 17 and maven for the server side, and npm/angular for the frontend.

To launch the backend use:
```shell
mvn clean package

java -jar target/pokedense-1.0-SNAPSHOT-jar-with-dependencies.jar
```

You may provide a port number as first argument of the jar to override the default of 8080 (update it also in the `proxy.config.json` to make the frontend work in dev).

The pokemon list API is then available at `http://localhost:8080/api/v1/pokemons` with query parameters `name`, `weight` (kg), `weightOperator` (`>`/`<`/`=`), `height` (m), `heightOperator`

The frontend can be installed with npm and launched with
```shell
npm run start
```
It should open the app in your browser and load pokemons directly.

TODO bundle and launch everything with maven

## Design choices

### Server

Simple fat jar: In a prototype context focused on coding skills, I chose the simplest java installation/configuration/deployment I know of.
Still, the business logic (Pokemon* classes) is separated and can be easily migrated to any framework.

Caching all pokemon data: I did not find a way to provide filters to pokeAPI or to fetch height/weight in one query, so I had to get data in 2 steps. Caching all needed data allows to provide required functionnality without spamming the pokeAPI.
A more advanced project may cache data in permanent storage instead of memory and update with a batch mechanism.

### Frontend

I’m not familiar with angular, so I used the bootstraper with the simplest configuration, in a distinct folder to avoid any interference with the maven pipeline.

Then I read the angular "getting started guide" parts that seemed useful for the project.