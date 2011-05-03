package com.linkedin.chirper.streamer


import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader

import org.json._

import com.linkedin.led.twitter.config._
import com.linkedin.led.twitter.streaming.StreamProcessor


import kafka.message._
import kafka.producer._
import org.apache.log4j.Logger
import voldemort.scalmert.client.StoreClient
import voldemort.client.{SocketStoreClientFactory,ClientConfig}
import voldemort.scalmert.Implicits._
import voldemort.scalmert.versioning._
import com.linkedin.chirper.DefaultConfigs

// processes each tweet from the streamer
class ChirperStreamProcessor extends StreamProcessor{

    val log = Logger.getLogger(classOf[ChirperStreamProcessor])

	val kafkaTopic = Config.readString("tweet.kafka.topic")
	val voldemortStore = Config.readString("tweet.voldemort.store")
	
	val factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(DefaultConfigs.voldemortUrl));
	val tweetStore: StoreClient[String, String] = factory.getStoreClient[String, String](voldemortStore)
	val kafkaProducer = new SimpleProducer(DefaultConfigs.kafkahost,DefaultConfigs.kafkaport, 64 * 1024, 100000, 10000)
	
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
		 /// println(line)
		  val jsonObj = new JSONObject(line)
  		  val id = jsonObj.getString("id_str")
		  // send to voldemort store
		  tweetStore(id) = line
		  
		  val tweetString = tweetStore.get(id)
		  log.debug("tweetid: "+id)
		  // send to kafka
	      kafkaProducer.send(kafkaTopic,new ByteBufferMessageSet(new Message(line.getBytes(DefaultConfigs.UTF8Charset))))
	      
        }
        catch{
	      case je: JSONException => je.printStackTrace()
	      case e: Exception => e.printStackTrace()
        }
        line = reader.readLine()
	  }
	  is.close
	}
}
