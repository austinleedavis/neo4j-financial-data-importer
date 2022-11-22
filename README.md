# Neo4j Financial Data Importer
This library is developed to support reaserch into Corporate Entity Tracking Automation (CETA). Specifically, it provides financial and legal entity data from authoritative sources like the [National Information Center](https://www.ffiec.gov/nicpubweb/content/help/helpaboutnic.htm) and the [GLEIF](https://www.gleif.org/en/lei-data/access-and-use-lei-data)

## Installation
This project uses Gradle. You can obtain Gradle by following the instructions at [https://gradle.org/install/](https://gradle.org/install).

Once Gradle is installed in your environment, run:
```shell
gradle -q uberJar #builds n4jfindat.jar
```

## Usage

There are four required command line arguments: 

* `db`: The database to which the session binds (e.g., "neo4j)
* `uri`: The URL to a Neo4j instance (e.g, "bolt://localhost:7687")
* `username`: The username on the Neo4j instance
* `pass`: The user's password on the Neo4j instance

```shell
java -jar .\app\build\libs\n4jfindat.jar -db neo4j -uri bolt://localhost:9674 -username neo4j -pass password
```
