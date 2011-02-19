package com.linkedin.chirper.services

import kafka.Kafka

object KafkaRunner {
  def main(args: Array[String]) = {
	val params = new Array[String](1)
	params(0) = "config/kafka/server.properties"
    Kafka.main(params)    
  }
}
