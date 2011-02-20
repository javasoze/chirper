What is Chirper?
==================

Chirper is a real time tweet search engine (written in Scala) using open source technology built by the [LinkedIn SNA team](http://sna-projects.com):

* [Twitter Streamer](https://github.com/acrosa/Scala-TwitterStreamer)
* [Kafka - Distributed Messaging System](http://sna-projects.com/kafka/)
* [Voldemort - Distribute Key/Value Store](http://sna-projects.com/voldemort/)
* [Sensei - Distributed Realtime Faceted Search System](http://sna-projects.com/sensei/)

Although the amount of code is minimal, the system can scale horizontally infinitely by leveraging the distributed system above.

### Build and run the system:

From the top level directory, e.g. ~/chirper

1. sbt update (do this once to setup the probject)
2. sbt compile (build the system)
3. sbt run

You will see a list of classes that ca be run:

    Multiple main classes detected, select one to run:

    [1] com.linkedin.chirper.streamer.ChirpStream
    [2] com.linkedin.chirper.services.ZookeeperRunner
    [3] com.linkedin.chirper.services.KafkaRunner
    [4] com.linkedin.chirper.services.VoldemortRunner
    [5] com.linkedin.chirper.search.ChirpSearchNode

    Enter number:

Select a class to run (you'll need to run one per console window)

The order of class to be run are:

1. [2] - Zookeeper
2. [3] - Kafka
3. [4] - Voldemort
4. [1] - Streamer (Make sure your twitter username/password is set in the [config file](https://github.com/javasoze/chirper/blob/master/config/TwitterStreamer.conf))
5. [5] - Search Node

Each of the components are pre-configured, details see [configs](https://github.com/javasoze/chirper/tree/master/config).

The last thing to run is a Restful servlet and interacts with the system (Comes with a beautiful UI):

    chirper$ sbt
    [info] Building project Chirper 1.0 against Scala 2.8.0
    [info]    using ChirperStreamerProject with sbt 0.7.4 and Scala 2.7.7
    > jetty-restart

Now you can point to:

* [Chirp UI - http://localhost:8080](http://localhost:8080)
* [Chirp API - http://localhost:8080/search?q=](http://localhost:8080/search?q=)
