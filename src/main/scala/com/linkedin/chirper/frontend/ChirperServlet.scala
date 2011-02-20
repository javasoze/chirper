package com.linkedin.chirper.servlet

import com.linkedin.led.twitter.config._
import com.linkedin.chirper.search.ChirpSearchNode
import javax.servlet._
import org.scalatra._
import org.scalatra._
import org.apache.lucene.search._
import scalate.ScalateSupport
import org.fusesource.scalate._
import org.fusesource.scalate.TemplateEngine
import com.sensei.search.req.SenseiRequest
import com.sensei.search.req.StringQuery
import com.sensei.search.svc.impl.ClusteredSenseiServiceImpl
import com.sensei.search.client.servlet.DefaultSenseiJSONServlet
import voldemort.scalmert.client.StoreClient
import voldemort.client.ClientConfig
import voldemort.client.SocketStoreClientFactory
import voldemort.scalmert.Implicits._
import voldemort.scalmert.versioning._
import org.json._

class ChirperServlet extends ScalatraServlet with ScalateSupport {
  var t1: Long = 0
  var t2: Long = 0

  val clusterName = Config.readString("zookeeper.cluster")
  val zkurl = Config.readString("zookeeper.url")
  val timeout = 30000

  val voldemortUrl = Config.readString("voldemort.url")
  val voldemortStore = Config.readString("voldemort.store")

  val factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(voldemortUrl));
  val tweetStore: StoreClient[String, String] = factory.getStoreClient[String, String](voldemortStore)

  val senseiSvc = new ClusteredSenseiServiceImpl(zkurl,timeout,clusterName)
  senseiSvc.start()

  ChirpSearchNode.addShutdownHook{ senseiSvc.shutdown }

  before {
    t1 = System.currentTimeMillis()
    contentType = "application/json; charset=utf-8"
  }

  after {
    t2 = System.currentTimeMillis()
    response.setHeader("X-Runtime", (t2-t1).toString) // Display the time it took to complete the request
  }

  /**
   * Application Routes
   */
  get("/") {
    contentType = "text/html"
    templateEngine.layout("index.ssp")
  }

  get("/search"){
	// params
	val q = params.getOrElse("q", "")
	val offset = params.getOrElse("offset", "0").toInt
	val count = params.getOrElse("count", Config.readString("search.perPage")).toInt
	
	// Build a search request
	val req = new SenseiRequest()
	// Paging
	req.setOffset(offset)
	req.setCount(count)
	req.setFetchStoredFields(false)
	
	// Parse a query
	if (q != null && q.length() > 0) {
	      try {
	        val sq = new StringQuery(q)
	        req.setQuery(sq)
	      } catch {
	        case e: Exception => e.printStackTrace()
	      }
	}
	
	// sort by time
	req.addSortField(new SortField("time", SortField.CUSTOM, true))
	
	// do search
	val results = senseiSvc.doQuery(req) // no facets for this
	
	// build a json object
	val resultJSON = DefaultSenseiJSONServlet.buildJSONResult(req,results)
	val hitsArray = resultJSON.getJSONArray("hits")
	val hitsArrayLen = hitsArray.length()
	var i = 0
	while (i < hitsArrayLen){
	  val hit = hitsArray.getJSONObject(i)
	  val uid = hit.getString("uid")
	  val statusString = tweetStore(uid)
	  var statusJsonObj = new JSONObject();
	  if (statusString!=null){
		try{
		  // go to voldemort store to get the original tweet text for display
		  val voldObj = new JSONObject(statusString)
		  val tweetString = voldObj.getString("value")
		  statusJsonObj = new JSONObject(tweetString)
		}
		catch{
		  case e : Exception => e.printStackTrace()
		}
		hit.put("status",statusJsonObj)
	  }
	  i+=1
	}
	resultJSON.toString()
  }
}
