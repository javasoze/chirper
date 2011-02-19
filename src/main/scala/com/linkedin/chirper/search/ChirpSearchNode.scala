package com.linkedin.chirper.search

import com.linkedin.led.twitter.config._

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.json.JSONObject;

import proj.zoie.api.DefaultZoieVersion;
import proj.zoie.api.DefaultZoieVersion.DefaultZoieVersionFactory;
import proj.zoie.hourglass.impl.HourGlassScheduler;
import proj.zoie.hourglass.impl.HourGlassScheduler.FREQUENCY;
import proj.zoie.impl.indexing.ZoieConfig;

import java.util._
import java.text.SimpleDateFormat

import com.browseengine.bobo.facets.FacetHandler
import com.browseengine.bobo.facets.data.PredefinedTermListFactory
import com.browseengine.bobo.facets.impl.RangeFacetHandler

import com.linkedin.norbert.javacompat.cluster.ClusterClient;
import com.linkedin.norbert.javacompat.cluster.ZooKeeperClusterClient;
import com.linkedin.norbert.javacompat.network.NettyNetworkServer;
import com.linkedin.norbert.javacompat.network.NetworkServer;
import com.sensei.search.nodes.impl.SenseiBuilderHelper
import com.sensei.search.nodes.SenseiHourglassFactory
import com.sensei.search.nodes.SenseiIndexLoaderFactory
import com.sensei.search.nodes.SenseiIndexReaderDecorator
import com.sensei.search.nodes.SenseiQueryBuilderFactory
import com.sensei.search.nodes.SenseiServer
import com.sensei.search.nodes.impl._

import java.io.File

object ChirpSearchNode{
	def addShutdownHook(body: => Unit) = 
	  Runtime.getRuntime.addShutdownHook(new Thread {
	    override def run { body }
	})
	
	def main(args: Array[String]) = {
	  val decorator = new SenseiIndexReaderDecorator()
	  val zoieConfig = new ZoieConfig[DefaultZoieVersion](new DefaultZoieVersionFactory());
	
	  val queryParser = new QueryParser(Version.LUCENE_29,"contents",new StandardAnalyzer(Version.LUCENE_29))
	  val queryBuilderFactory = new SimpleQueryBuilderFactory(queryParser)
	
	  val nodeid = Config.readInt("search.node.id")
	  val port = Config.readInt("search.node.port")
	  val partList = Config.readString("search.node.partitions")
	
	  val idxDir = new File(Config.readString("search.node.index.dir"))
	  val interpreter = new ChirpJSONInterpreter();
	
	  val handlerList = new java.util.ArrayList[FacetHandler[_]]()
	  val rangeList = new java.util.ArrayList[java.lang.String]()
	  val timeHandler = new RangeFacetHandler("time",new PredefinedTermListFactory[java.lang.Long](classOf[java.lang.Long], "0000000000000000000000000000000000000000"),rangeList)
	  handlerList.add(timeHandler)
	  // rolls daily at midnight, keep 7 days
	  val hfFactory = new SenseiHourglassFactory[JSONObject, DefaultZoieVersion](idxDir,interpreter,new SenseiIndexReaderDecorator(handlerList,null), zoieConfig, "00 00 00", 7, FREQUENCY.DAILY)
	
	  val clusterName = Config.readString("zookeeper.cluster")
      val zkurl = Config.readString("zookeeper.url")
	  val timeout = 30000

      val clusterClient = new ZooKeeperClusterClient(clusterName,zkurl,timeout);

      val networkServer = SenseiBuilderHelper.buildDefaultNetworkServer(clusterClient);

	  val indexLoaderFactory = new ChirpIndexLoaderFactory();
		
	  val server = new SenseiServer(nodeid, port, partList.split(",").map{i=>i.toInt},
			                      idxDir,networkServer,
			                      clusterClient,hfFactory,indexLoaderFactory,queryBuilderFactory)
			
	  addShutdownHook{ server.shutdown }
	
	  server.start(true)
		
    }	
}

