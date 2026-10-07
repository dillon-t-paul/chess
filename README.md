# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

[Server Sequence Diagram](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUelXBjUASQw5YQfqgRZLbcUCTco-1qL90XeE0-yjGM40KP8QJTDBagAVicJwelGMYc1UPN5kWADoHqCCfw7W4iKgBt0A4ID2xhJ5S2xNE8TUXssAYuF2zAmBXkNJUlnqfYQQvPV2ggOs0EE5ZLgTSh22QsB6nCDCswmfjPhgITgWWUT4nEyTpP2K4aNMUwvF8AJoHYRkYhFOAI2kOAFBgAAZCAskKRTBR45o2i6XoDHUfI0CzRU5jWX5-g4K4-0FTj-T48tPkhEEop2L5oUeLinU7eoEA88UMXczyCSJMBSURHcqVvfkGSZKdwq5G8aX3e9hRgMUJTdGU5TLKC5kwN91Q3WA0AgZgRV8Jth35ECqq7Hs+zokDyP9N0ADkJqmnxOFglBYxCuT4QqRS0ycQI1Nw-CCzGSjXS1cbmAAM2m3YTKbYbPzQEBoBRcA3MKtB4BRDhzCQE1bVqxdcqoJFRo9BbuOqf0v1+4sUHAOBQfByH9sO+M4tWs6YHTABGK7+RuoT7oVaZL2gJAAC8UHextaK+gNlGm5hxQ3FreTvVaFoezd5BWioeLdL8mgQYBLHx+Djp9En0ycSnc3zGni2Irnft25gTTNeU+cmaimyR3dWpHMcJxQZ94gxB3r1mxdKgfNcA0vV9VW+yDpl-FH-x1qAgPmoPwO-KDA+oShFaOxDTuQVN0Mw7Drq1u6Q5IqOA7W2BKPN2jLfuBLSxK8UMlUdjMDL5HY9LcY9IM+stJkhsieTZOUJgFT1dGZYmr2b5m4k1vtKLszPG8Px-C8FB0BiOJEnnxeSt8LBvNh-0GmkCNXIjdoI26HogtUELhlHyTldL7LErGK-60hLLYXk2H4YK+wN+KjyN7KtQKorRqtbOq8B4iYzVA7c8l4W5oDnCAt2Qp6hdSfN7bQsp5SP0KJzL8VATRIFQXqGAsCBYLlUOHPKS1+wl19BHGAuD8EcFgfHQmQcFLdyUqTJwFN05Uy1oWbO9C0B4OQOuWBk9XYCmFs6LsXsXxbktsAwWdUURUDHlArB8DlGIOXGDbg6I5FXgUTIwUkj6hwHASASBl5oFiTHnA0hbUlwdRQYY3qmCYH2KGr7QGX9TydXBKzRxQt34ujccYzs9cajIMCRwdep4WEITYcTDh9RU4azwvwrOJZ6jxMmrEiR0NyGSxkbUKhEtKhl1yb-U8Vca512kQ3F4sVkldxKJwvuWYPq0TMhZWeoMYjYHFBqVyaIYAAHElQaC3rQppjRxmHxPvYJUl9PHXyJpUu+jcH5rKfsZWuWyfKlJgDUJAz1LAYgAGqUDOZYf+JIgEyCKeYyx1izyaOCQ6ZxyDxSEKMfIDBxpdnYJ8Qw0RhjiFeMkRQuGLpyk0PzjnERBDmHRgOkrTulRVbcIydTAROShHIrEfYwpCCpGhNkc7CJsLgLlDMYyMAyycwYi0WQioy5XEmgQBMpUPt3xCP9tBIOlEw4lKaaRaOME0UEySQ3JCqTU5YWzHwgi2TdYSrznFQu3TPnqBhUiKhPRJlzERjIkCVS3JomNWoDE1rrxlxhc8cYTK1ALAaM6pUH5pALEGBwAA1GsYYYxNjxF1CgN0nIvgrGGMkUAaoI0CVSjAINLqNpKmfp0Fpcqk7tOUqpbCLrVBuo9XML1Pr-WBqkiGsNCaUrCRjXLKxtbVU6RTUqNNcwM06t6TPAIHAADsbgnAoCcDECMwQ4AOQAGwgyPIYa1MAigcKOXMvyHQlkrPpnYySWZU1KizYmTZr9tlYKjcmqSe662yQabMml9RTnnKuTc859zAGKKeWSl5ECNHAtZU492LjfnhIBX1LB3j+VgoIRCkh0KxV3vhSYxp0TCWMNRXBBOrSsWpK4Tw5VmsCL4vVcIxhkLDI6skVE+GVLxYLQqGYiZTJrUYkvc1aF5RlydSA3a9BfUWPgZGgujthgwzX1g6BUp3GaNmrg-6a1UYbSJOOvK3NXD+54cyaqhdRpaz1nI0U-VcLezUOk3S552M51Mck8AP9I52UdUfDyk1W5+MalTjAEVtGZOlg1UKhuinE7lBJuk3h+G9gKGBrtK02BMbonXJQEsMAIDKB8KDSennyhVPM3IFAdSEAcS2Y6uoJaUBlvqL6v1B7KDKdTJ0gtnrvVlf9d28yvb-CWBQH2CAmwl5IASGAdrnXusACkIB8wXf4WNVil3tJXch5ozIAo9Bdas7d6AszYDlu1qAcAIAFSgGsF1XrKvwiPYxF4OzVtSSTUGjb8tKA7b2wsAA6iwD8R8egACFXIKDgAAaUrWMQ7DXysv0YrN+GD6LnXKgLc19lVENKLIV+qxP7Ls2a+QBn5EpqPAEBWBnBxHwUO1I+gXVxTxOUKMxUxFKHRFofRRhhu7CVPk1xVk2mkHiVkfZmTyjYScdh1M5+8ZjGlTMfq+jvV7HhQi+YFZwFQOXPwHYE9Eb4oRViZFlZ0VFPkNwBVxNNXaA5PSoxUHarPc1Zs4I2MB70AYBbWYAoPBpxdDcEnpza1jvnfgjdygDzJmeJe4mj713yR-em8ZzUC3nD0yZhCxp26gmJoBhd3LcPaWzVC+0aOI3lneXaEiptsW1myd2ddNgLQBj5f9HFI4W7W2+UjR8zHGoAfOyFZzoK1vcdI+sOzYF7DwX1N4rGOFumUWYusxgPF+3SWQApdZpnjvFQLVG9y-l49neRjHZj7UWrO+ec9ssnPKA8set9a8OfxAwZYDAGwBtwgeQCiLpmTT3e+9D7H16MYJTGWtm1CH4Oq67wwogGBgBYhqBMg8h6AGBw6C70YgDcB4AwH6AoAspl7S71AZAzAQA0BGDUqiB0bPKQ4354CswYFsYcacpoDyhN4aioEGAwBPbECGAiixJl5wbwxV7AA648RVzQHaCwEoDsGnD+bm45qpgwDD44Qqp7CMGGAsHRYBKnBL56ZkoGaLRU7pYWoCFgAb4HJb5eaAG76SEoQH46pAA)
## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
