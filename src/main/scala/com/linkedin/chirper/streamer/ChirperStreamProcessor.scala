package com.linkedin.chirper.streamer


import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader

import org.json._

import com.linkedin.led.twitter.config._
import com.linkedin.led.twitter.streaming.StreamProcessor


import kafka.message._
import kafka.producer._

import voldemort.scalmert.client.StoreClient
import voldemort.client.ClientConfig
import voldemort.client.SocketStoreClientFactory
import voldemort.scalmert.Implicits._
import voldemort.scalmert.versioning._
import com.linkedin.chirper.DefaultConfigs

// processes each tweet from the streamer
class ChirperStreamProcessor extends StreamProcessor{
	val kafkaHost = Config.readString("kafka.host")
	val kafkaPort = Config.readInt("kafka.port")
	val kafkaTopic = Config.readString("kafka.topic")
	val voldemortUrl = Config.readString("voldemort.url")
	val voldemortStore = Config.readString("voldemort.store")
	
	val factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(voldemortUrl));
	val tweetStore: StoreClient[String, String] = factory.getStoreClient[String, String](voldemortStore)
	val kafkaProducer = new SimpleProducer(kafkaHost,kafkaPort, 64 * 1024, 100000, 10000)
	
	def shutdown() = {
		kafkaProducer.close()
		factory.close()
	}	
	
	override def process(is: InputStream): Unit = {
	  val reader: BufferedReader = new BufferedReader(new InputStreamReader(is,DefaultConfigs.UTF8Charset))
	  var line = reader.readLine()
	  while (line != null) {
		// for each tweet
		try{
		  // output to console
		  println(line)
		  val jsonObj = new JSONObject(line)
		  val id = jsonObj.getString("id_str")
		  // send to kafka
	      kafkaProducer.send(kafkaTopic,new ByteBufferMessageSet(new Message(line.getBytes(DefaultConfigs.UTF8Charset))))
	      // send to voldemort store
		  tweetStore(id) = line  
        }
        catch{
	      case je: JSONException => 
	      case e: Exception => e.printStackTrace()
        }
        line = reader.readLine()
	  }
	  is.close
	}
}
