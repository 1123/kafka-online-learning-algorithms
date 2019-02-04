## Recursive Least Squares Algorithm with Apache Kafka

[Online learning algorithms](https://en.wikipedia.org/wiki/Online_machine_learning) are a great fit for event streaming platforms such as [Apache Kafka](https://kafka.apache.org/), since they continuously adpat their model one event at a time as new data arrives. 

The [recursive least squares algorithm](https://en.wikipedia.org/wiki/Online_machine_learning#Online_learning:_recursive_least_squares) is possibly the most well known online learning algorithm. This project shows how to train a [linear least squares model](https://en.wikipedia.org/wiki/Linear_least_squares) using the recursive least squares algorithm reading data from a Kafka input topic one event at a time and writing the model (slope and y-intercept) to an output topic on Kafka.  

### Prerequisites

* Java 8 or higher
* Apache Maven

The tests will start up a single node Kafka cluster. So there is no need to stand up your own.

### How to run these samples

`mvn clean test` will execute the algorithms. 





