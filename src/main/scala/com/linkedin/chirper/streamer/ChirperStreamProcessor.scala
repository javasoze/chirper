package com.linkedin.chirper.streamer


import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader

import org.json._

import com.linkedin.led.twitter.Config
import com.linkedin.led.twitter.streaming.StreamProcessor

class ChirperStreamProcessor extends StreamProcessor{
	val voldurl = Config.readString("voldemort.url")
	val kafkaHost = Config.readString("kafka.host")
	val kafkaPort = Config.readInt("kafka.port")
	val kafkaTopic = Config.readString("kafka.topic")
	
	override def process(is: InputStream): Unit = {
	  val reader: BufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"))

	  var line = reader.readLine()
	  while (line != null) {
		val jsonObj = new JSONObject(line)
	    println(line)
	    line = reader.readLine()
	  }

	  is.close
	}
}
