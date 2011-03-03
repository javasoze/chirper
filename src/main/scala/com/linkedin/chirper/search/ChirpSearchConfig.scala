package com.linkedin.chirper.search

import com.linkedin.led.twitter.config._


import com.sensei.indexing.api.DefaultSenseiInterpreter
import com.browseengine.bobo.facets.FacetHandler
import com.browseengine.bobo.facets.data.PredefinedTermListFactory
import com.browseengine.bobo.facets.impl.RangeFacetHandler

import com.linkedin.chirper.DefaultConfigs
import com.sensei.dataprovider.kafka.KafkaStreamIndexLoaderFactory.DefaultJsonFactory


object ChirpSearchConfig{
	// kafka config
	val kafkatopic = Config.readString("tweet.kafka.topic")
	val batch = Config.readInt("tweet.search.node.index.batch")
	
	val tweetIndexLoaderFactory = new DefaultJsonFactory(DefaultConfigs.kafkahost,DefaultConfigs.kafkaport,kafkatopic,batch,30000)
	
	// how do we convert an indexing event, in this case a json obj, into a lucene document
	val interpreter = new ChirpJSONInterpreter()
	
	// use a bobo range facet handler for fast sorting on time
	val handlerList = new java.util.ArrayList[FacetHandler[_]]()
	val rangeList = new java.util.ArrayList[java.lang.String]()
	val timeHandler = new RangeFacetHandler("time",DefaultSenseiInterpreter.getTermListFactory(classOf[java.lang.Long]),rangeList)
	handlerList.add(timeHandler)
	
}
