# CBA TEST

The repository contains implementation of the requirement as given below.

1. Build a Kafka streamer that reads from 1 topic and publish into 2 different topics
2. Input topic will have a payload of first name, last name, date of birth.
3. Logic: when age is even number, publish to topic: CustomerEVEN, if age is odd number, publish to topic: CustomerODD
4. Build using Maven/Gradle and Spring Boot.


Spring cloud kafka stream is used to develop this functionality as it uses functional programming to implement producers/consumers.  Also, most of the routing is automatically achieved via configuration over code. 

Assumptions :
1. There is no key associated with the payload as per the requirement above.
2. Age 0 is considered even and published to CustomerEVEN topic.

## Code

The main logic of the code is present in two place, 

1. application.yaml:  This file contains the details of how the kafka topics and binders are configured to read from an input topic named "Customer" and publish it to the target topics "CustomerEVEN" and "CustomerODD" depending on the criteria.

2. KafkaStreamProcessor.java:  This java file contains the functional implementation of stream processor that is bound to th configuration in application.yml. It implements a function call "processor" that reads customer details from the message sent to Customer topic, deserializes it to Customer model, calculates the age from the dateOfBirth and then sends to CustomerEven if the age is even or to CustomerOdd otherwize. 

Schema registry is used to store schema associated with the input message which basically contains firstName,lastName and dateOfBirth. An avro schema file Customer.avsc is present under /src/main/resources/avro/schema folder which defines the schema for the message. I have used "avro-maven-plugin" to generate POJO for the Customer. 

A sample message payload looks like 

```
  {"firstName":"John","lastName":"Doe","dateOfBirth":"1991-12-12"}
```

Exception Handling: An exceptions wrto schema of the payload will result in avro serializer failure, I am catching this to log the error and throwing the exception back such it ends up in dead-letter-topic. This dlq topic is also configured in application.yml.

# How to build

Build:

Navigate to the root of the directory and perform ```mvn clean package`` which executes unit test, compiles and builds the jar file. 


# How to run this locally

Please run the docker-compose.yml file present on the root of the project directory that sets up Kafka cluster with kafka instance, zookeeper , schema-registry and kafka-ui. 

Navigte to the project root and execute ``` docker-compose up``` command to bring up all the contiainers.
Please wait for a couple of minutes for all the services to boot as they are all dependent on kafka broker to be available. 

Please access the kafka-ui at http://localhost:8081 to view whole kafka cluster in browser UI. This UI can be used to publish and read messages from various topics.  I have used this to test my application.

Please ensure that the java application is running before accessing this ui to publish messages. 

Run the applciation:

Please run the jar file as below. Please ensure the kafka is up and running before running the jar. 
```
java -jar target/cba-test-kafka-0.0.1-SNAPSHOT.jar 
```
