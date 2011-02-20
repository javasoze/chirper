package com.linkedin.chirper.search


import com.linkedin.led.twitter.config._

import org.json.JSONObject
import proj.zoie.impl.indexing.StreamDataProvider
import com.sensei.dataprovider.kafka.KafkaJsonStreamDataProvider
import com.sensei.search.nodes.impl.StreamIndexLoaderFactory
import proj.zoie.api._

// Hooks up to kafka to listen to events
class ChirpIndexLoaderFactory extends StreamIndexLoaderFactory[JSONObject,DefaultZoieVersion]{
	
	val kafkahost = Config.readString("kafka.host")
	val port = Config.readInt("kafka.port")
	val kafkatopic = Config.readString("kafka.topic")
	val batch = Config.readInt("search.node.index.batch")
	
	 override def buildStreamDataProvider(partitinId : Int,version: DefaultZoieVersion) : StreamDataProvider[JSONObject,DefaultZoieVersion] ={
		// get the current offset to stream from
		var currentOffset : Long = 0
		if (version==null) currentOffset = 0
		else currentOffset = version.getVersionId()
		val dataprovider = new KafkaJsonStreamDataProvider(kafkahost,port,30000,batch,kafkatopic,currentOffset)
		dataprovider
	}
}
