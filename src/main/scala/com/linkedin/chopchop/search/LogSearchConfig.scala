package com.linkedin.chopchop.search

import com.linkedin.led.twitter.config._
import com.sensei.indexing.api.DefaultSenseiInterpreter
import com.browseengine.bobo.facets.FacetHandler
import com.browseengine.bobo.facets.data.PredefinedTermListFactory
import com.browseengine.bobo.facets.impl.RangeFacetHandler

import com.sensei.dataprovider.kafka.KafkaStreamIndexLoaderFactory.DefaultJsonFactory

import com.linkedin.chirper.DefaultConfigs

object LogSearchConfig{
	val kafkaLogtopic = Config.readString("chopchop.kafka.topic")
	val batch = Config.readInt("chopchop.search.node.index.batch")
	val logIndexLoaderFactory = new DefaultJsonFactory(DefaultConfigs.kafkahost,DefaultConfigs.kafkaport,kafkaLogtopic,batch,30000)
	
	val logIndexInterpreter = new LogMessageInterpreter()
	
	// use a bobo range facet handler for fast sorting on time
	val logHandlerList = new java.util.ArrayList[FacetHandler[_]]()
	val logTimerangeList = new java.util.ArrayList[java.lang.String]()
	val logTimeHandler = new RangeFacetHandler("time",DefaultSenseiInterpreter.getTermListFactory(classOf[java.lang.Long]),logTimerangeList)
	logHandlerList.add(logTimeHandler.asInstanceOf[FacetHandler[_]])
}
