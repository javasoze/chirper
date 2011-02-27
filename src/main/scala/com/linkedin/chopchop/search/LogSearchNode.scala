package com.linkedin.chopchop.search

import com.linkedin.led.twitter.config._

import com.linkedin.chirper.DefaultConfigs

import org.json.JSONObject

import proj.zoie.api.DefaultZoieVersion
import proj.zoie.api.DefaultZoieVersion.DefaultZoieVersionFactory
import proj.zoie.hourglass.impl.HourGlassScheduler
import proj.zoie.hourglass.impl.HourGlassScheduler.FREQUENCY

import proj.zoie.impl.indexing.ZoieConfig

import java.util._
import java.io.File
import java.text.SimpleDateFormat

import com.linkedin.norbert.javacompat.cluster.{ClusterClient,ZooKeeperClusterClient}
import com.linkedin.norbert.javacompat.network.{NetworkServer,NettyNetworkServer}
import com.sensei.search.nodes.{SenseiHourglassFactory,SenseiIndexLoaderFactory,SenseiIndexReaderDecorator,SenseiServer}
import com.sensei.search.nodes.impl._


// Build a search node
object LogSearchNode{
	def main(args: Array[String]) = {
	
	  val nodeid = Config.readInt("chopchop.search.node.id")
	  val port = Config.readInt("chopchop.search.node.port")
	  val partList = Config.readString("chopchop.search.node.partitions")
	
	  // where to put the index
	  val idxDir = new File(Config.readString("chopchop.search.node.index.dir"))
	
	  // rolls daily at midnight, keep 7 days
	  val hfFactory = new SenseiHourglassFactory[JSONObject, DefaultZoieVersion](idxDir,LogSearchConfig.logIndexInterpreter,
                          new SenseiIndexReaderDecorator(LogSearchConfig.logHandlerList,null), 
	                      DefaultConfigs.zoieConfig, "00 00 00", 7, FREQUENCY.DAILY)
	
	  val clusterName = Config.readString("chopchop.zookeeper.cluster")

      // zookeeper cluster client
      val clusterClient = new ZooKeeperClusterClient(clusterName,DefaultConfigs.zkurl,DefaultConfigs.timeout);


      // build a default netty-based network server
      val networkServer = SenseiBuilderHelper.buildDefaultNetworkServer(clusterClient);
		
      // builds the server
	  val server = new SenseiServer(nodeid, port, partList.split(",").map{i=>i.toInt},
			                      idxDir,networkServer,
			                      clusterClient,hfFactory,LogSearchConfig.logIndexLoaderFactory,DefaultConfigs.queryBuilderFactory)
			
	  DefaultConfigs.addShutdownHook{ server.shutdown }
	
	  // starts the server
	  server.start(true)
		
    }	
}

