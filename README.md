# Pokedense

Prototype of a pokemon list with weight/height information and filtering ([following provided constraints](https://miuros.notion.site/Full-stack-Engineer-Coding-test-a5939dbbce334fd999901f8579f429cf)).

## Setup

The project uses maven to manage java dependencies and build:

`mvn clean package`

You can then launch the app by executing the generated jar:

`java -jar target/pokedense-1.0-SNAPSHOT-jar-with-dependencies.jar`

You may provide a port number as first argument to override the default of 8080.
Once launched, the server prints in the console the URL you may use to access the app.

`Server successfully started, you can go to http://localhost:8080/index.html`

You can request the API at `/api/v1/pokemons` with query parameters `name`, `weight` (kg), `weightOperator` (`>`/`<`/`=`), `height` (m), `heightOperator`

## Design choices

### Server

Simple fat jar: In a prototype context focused on coding skills, I chose the simplest java installation/configuration/deployment I know of.
Still, the business logic (Pokemon* classes) is separated and can be easily migrated to any framework.

Caching all pokemon data: I did not find a way to provide filters to pokeAPI or to fetch height/weight in one query, so I had to get data in 2 steps. Caching all needed data allows to provide required functionnality without spamming the pokeAPI.
A more advanced project may cache data in permanent storage instead of memory and update with a batch mechanism.