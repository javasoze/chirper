package com.linkedin.chirper.search

import com.linkedin.led.twitter.config._

import proj.zoie.api.DefaultZoieVersion;
import proj.zoie.api.DefaultZoieVersion.DefaultZoieVersionFactory;
import proj.zoie.impl.indexing.ZoieConfig;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.util.Version;

import com.sensei.search.nodes.SenseiQueryBuilderFactory
import com.sensei.search.nodes.impl.SimpleQueryBuilderFactory
import com.browseengine.bobo.facets.FacetHandler
import com.browseengine.bobo.facets.data.PredefinedTermListFactory
import com.browseengine.bobo.facets.impl.RangeFacetHandler

object ChirpSearchConfig{
	// zoie configuration, use default
	val zoieConfig = new ZoieConfig[DefaultZoieVersion](new DefaultZoieVersionFactory());
	 
	// define query parser builder
	val queryParser = new QueryParser(Version.LUCENE_29,"contents",new StandardAnalyzer(Version.LUCENE_29))
	queryParser.setDefaultOperator(Operator.AND)
	val queryBuilderFactory = new SimpleQueryBuilderFactory(queryParser)
	
	// how do we convert an indexing event, in this case a json obj, into a lucene document
	val interpreter = new ChirpJSONInterpreter();
	
	// use a bobo range facet handler for fast sorting on time
	val handlerList = new java.util.ArrayList[FacetHandler[_]]()
	val rangeList = new java.util.ArrayList[java.lang.String]()
	val timeHandler = new RangeFacetHandler("time",new PredefinedTermListFactory[java.lang.Long](classOf[java.lang.Long], "0000000000000000000000000000000000000000"),rangeList)
	handlerList.add(timeHandler)
	
	// highlighting
	val formatter = new org.apache.lucene.search.highlight.SimpleHTMLFormatter(Config.readString("search.highlight.pretag"),Config.readString("search.highlight.posttag"))
	val encoder = new org.apache.lucene.search.highlight.SimpleHTMLEncoder()
}
