package com.linkedin.chirper.servlet

import com.linkedin.led.twitter.config._
import com.linkedin.chirper.DefaultConfigs
import com.linkedin.chirper.search._
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
import org.apache.lucene.search.highlight._
import org.apache.commons.lang.StringEscapeUtils

import net.lag.logging.Logger

class ChirperServlet extends ScalatraServlet with ScalateSupport {
  var t1: Long = 0
  var t2: Long = 0

  val log = Logger.get

  val tweetClusterName = Config.readString("tweet.zookeeper.cluster")
  val logClusterName = Config.readString("chopchop.zookeeper.cluster")

  val timeout = 30000

  val voldemortStore = Config.readString("tweet.voldemort.store")

  val factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(DefaultConfigs.voldemortUrl));
  val tweetStore: StoreClient[String, String] = factory.getStoreClient[String, String](voldemortStore)

  val defaultPageSize = Config.readString("search.perPage")

  val doHighlighting = Config.readBoolean("search.highlight.dohighlight")

  val tweetSearchSvc = new ClusteredSenseiServiceImpl(DefaultConfigs.zkurl,timeout,tweetClusterName)
  tweetSearchSvc.start()

  val logSearchSvc = new ClusteredSenseiServiceImpl(DefaultConfigs.zkurl,timeout,logClusterName)
  logSearchSvc.start()

  DefaultConfigs.addShutdownHook{ tweetSearchSvc.shutdown; logSearchSvc.shutdown }

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

  get("/logs"){
	val start = System.currentTimeMillis()
	// params
	var q = params.getOrElse("q", "")
	val offset = params.getOrElse("offset", "0").toInt
	val count = params.getOrElse("count", defaultPageSize).toInt

	// Build a search request
	val req = new SenseiRequest()
	// Paging
	req.setOffset(offset)
	req.setCount(count)
	req.setFetchStoredFields(true)

	var highlightScorer : Option[QueryScorer] = None
	// Parse a query
	if (q != null) {
	  q = q.trim()
	  if (q.length>1){
	      try {
	        val sq = new StringQuery(q)
	        req.setQuery(sq)
	        if (doHighlighting && q.length()>2){
	          val luceneQ = DefaultConfigs.queryBuilderFactory.getQueryBuilder(sq).buildQuery()
	          highlightScorer = Some(new QueryScorer(luceneQ))
            }
	      } catch {
	        case e: Exception => e.printStackTrace()
	      }
	  }
	}

	// sort by time
	req.addSortField(new SortField("time", SortField.CUSTOM, true))

	// do search
	val searchStart = System.currentTimeMillis()
	val results = logSearchSvc.doQuery(req) // no facets for this
	val searchEnd = System.currentTimeMillis()

	// build a json object
	val resultJSON = DefaultSenseiJSONServlet.buildJSONResult(req,results)

	val end = System.currentTimeMillis()
	resultJSON.put("searchtime",(searchEnd-searchStart))
	resultJSON.put("totaltime",(end-start))
	resultJSON.toString()
  }

  get("/search"){
	log.info(request.getQueryString)
	val start = System.currentTimeMillis()
	// params
	var q = params.getOrElse("q", "")
	val offset = params.getOrElse("offset", "0").toInt
	val count = params.getOrElse("count", defaultPageSize).toInt
	
	// Build a search request
	val req = new SenseiRequest()
	// Paging
	req.setOffset(offset)
	req.setCount(count)
	req.setFetchStoredFields(false)
	
	var highlightScorer : Option[QueryScorer] = None
	// Parse a query
	if (q != null) {
	  q = q.trim()
	  if (q.length>1){
	      try {
	        val sq = new StringQuery(q)
	        req.setQuery(sq)
	        if (doHighlighting && q.length()>2){
	          val luceneQ = DefaultConfigs.queryBuilderFactory.getQueryBuilder(sq).buildQuery()
	          highlightScorer = Some(new QueryScorer(luceneQ))
            }
	      } catch {
	        case e: Exception => e.printStackTrace()
	      }
	  }
	}
	
	// sort by time
	req.addSortField(new SortField("time", SortField.CUSTOM, true))
	
	// do search
	val searchStart = System.currentTimeMillis()
	val results = tweetSearchSvc.doQuery(req) // no facets for this
	val searchEnd = System.currentTimeMillis()
	
	// build a json object
	val resultJSON = DefaultSenseiJSONServlet.buildJSONResult(req,results)
	val hitsArray = resultJSON.getJSONArray("hits")
	val hitsArrayLen = hitsArray.length()
	val fetchStart = System.currentTimeMillis()
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
		  highlightScorer match {
		     case Some(x) => {
			   var text = statusJsonObj.optString("text")
			   if (text.length()>0){
			     text = StringEscapeUtils.escapeHtml(text)
			   }
			   val highlighter = new Highlighter(DefaultConfigs.formatter,DefaultConfigs.encoder,x)
			   val segments = highlighter.getBestFragments(DefaultConfigs.zoieConfig.getAnalyzer(),"contents",text,1)
			   if (segments.length > 0) text = segments(0)
			   statusJsonObj.put("text",text)
		     }
		     case _	=>
		  }
		}
		catch{
		  case e : Exception => e.printStackTrace()
		}
		hit.put("status",statusJsonObj)
	  }
	  i+=1
	}
	val fetchEnd = System.currentTimeMillis()
	val end = System.currentTimeMillis()
	resultJSON.put("fetchtime",(fetchEnd-fetchStart))
	resultJSON.put("searchtime",(searchEnd-searchStart))
	resultJSON.put("totaltime",(end-start))
	resultJSON.toString()
  }
}
