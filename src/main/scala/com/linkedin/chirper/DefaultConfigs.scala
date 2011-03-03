package com.linkedin.chirper;

import com.linkedin.led.twitter.config._

import java.nio.charset.Charset
import proj.zoie.api.DefaultZoieVersion
import proj.zoie.api.DefaultZoieVersion.DefaultZoieVersionFactory
import proj.zoie.impl.indexing.ZoieConfig


import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.queryParser.QueryParser.Operator
import org.apache.lucene.util.Version

import com.sensei.search.nodes.SenseiQueryBuilderFactory
import com.sensei.search.nodes.impl.SimpleQueryBuilderFactory

object DefaultConfigs{
	val UTF8Charset = Charset.forName("UTF-8")
	
	val kafkahost = Config.readString("kafka.host")
	val kafkaport = Config.readInt("kafka.port")
	
	// zookeeper settings
	val zkurl = Config.readString("zookeeper.url")
	val timeout = 30000
	
	// zoie configuration, use default
	val zoieConfig = new ZoieConfig[DefaultZoieVersion](new DefaultZoieVersionFactory());
	
	// voldemort configuration
	val voldemortUrl = Config.readString("voldemort.url")
	
	// query builder
	// define query parser builder
	val queryParser = new QueryParser(Version.LUCENE_29,"contents",new StandardAnalyzer(Version.LUCENE_29))
	queryParser.setDefaultOperator(Operator.AND)
	val queryBuilderFactory = new SimpleQueryBuilderFactory(queryParser)
	
	// highlighting
	val formatter = new org.apache.lucene.search.highlight.SimpleHTMLFormatter(Config.readString("search.highlight.pretag"),Config.readString("search.highlight.posttag"))
	val encoder = new org.apache.lucene.search.highlight.SimpleHTMLEncoder()
	
	def addShutdownHook(body: => Unit) = 
	  Runtime.getRuntime.addShutdownHook(new Thread {
	    override def run { body }
	})
}
