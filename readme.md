## Online Learning Algorithms as Apache Kafka Streams

This project aims at showing that Online Learning algorithms and Apache Kafka as a Stream processing system are a great fit. 

Currently the only connected algorithm is the recursive least
squares algorithm from the SMILE project. 

### Prerequisites

* Java 8 or higher
* Apache Maven

The tests will start up a single node Kafka cluster. So there is no need to stand up your own.

### How to run these samples

`mvn clean test` will execute the algorithms. 





