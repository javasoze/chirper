package com.linkedin.chirper.streamer


import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader

import org.json._

import com.linkedin.led.twitter.Config
import com.linkedin.led.twitter.streaming.StreamProcessor


import kafka.message._
import kafka.producer._

import voldemort.scalmert.client.StoreClient
import voldemort.client.ClientConfig
import voldemort.client.SocketStoreClientFactory
import voldemort.scalmert.Implicits._
import voldemort.scalmert.versioning._

class ChirperStreamProcessor extends StreamProcessor{
	val voldurl = Config.readString("voldemort.url")
	val kafkaHost = Config.readString("kafka.host")
	val kafkaPort = Config.readInt("kafka.port")
	val kafkaTopic = Config.readString("kafka.topic")
	val voldemortUrl = Config.readString("voldemort.url")
	val voldemortStore = Config.readString("voldemort.store")
	
	val factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(voldemortUrl));
	val tweetStore: StoreClient[String, String] = factory.getStoreClient[String, String](voldemortStore)
	val kafkaProducer = new SimpleProducer(kafkaHost,kafkaPort, 64 * 1024, 100000, 10000)	
	
	override def process(is: InputStream): Unit = {
	  val reader: BufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
	  var line = reader.readLine()
	  while (line != null) {
		val jsonObj = new JSONObject(line)
		val id = jsonObj.getString("idStr")
		tweetStore(id) = line
	    kafkaProducer.send(kafkaTopic,new ByteBufferMessageSet(new Message(line.getBytes("UTF8"))))
	    line = reader.readLine()
	  }
	  is.close
	}
}
