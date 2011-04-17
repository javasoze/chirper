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
import com.sensei.search.svc.api.SenseiService
import com.sensei.search.client.servlet.DefaultSenseiJSONServlet
import voldemort.scalmert.client.StoreClient
import voldemort.client.ClientConfig
import voldemort.client.SocketStoreClientFactory
import voldemort.scalmert.Implicits._
import voldemort.scalmert.versioning._
import org.json._
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean
import org.apache.lucene.search.highlight._
import org.apache.commons.lang.StringEscapeUtils

import org.apache.commons.configuration.DataConfiguration
import org.apache.commons.configuration.web.ServletRequestConfiguration

import net.lag.logging.Logger

object ChirperServlet{
	val logger = org.apache.log4j.Logger.getLogger(classOf[ChirperServlet]);
}

class ChirperServlet extends ScalatraServlet with ScalateSupport {
  var t1: Long = 0
  var t2: Long = 0

  val log = Logger.get


  val voldemortStore = Config.readString("tweet.voldemort.store")

  val factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(DefaultConfigs.voldemortUrl));
  val tweetStore: StoreClient[String, String] = factory.getStoreClient[String, String](voldemortStore)

  val defaultPageSize = Config.readString("search.perPage")

  val doHighlighting = Config.readBoolean("search.highlight.dohighlight")


  val springInvokerBean = new HttpInvokerProxyFactoryBean()
  springInvokerBean.setServiceUrl(Config.readString("sensei.search.spring.url"))
  springInvokerBean.setServiceInterface(classOf[SenseiService])
  springInvokerBean.afterPropertiesSet()
  val tweetSearchSvc = springInvokerBean.getObject().asInstanceOf[SenseiService]

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
	//log.info(request.getQueryString)
	val start = System.currentTimeMillis()
	// params
	var q = params.getOrElse("q", "").trim()
	
	val req = DefaultSenseiJSONServlet.convertSenseiRequest(new DataConfiguration(new ServletRequestConfiguration(request)))
	
	// sort by time
	req.addSortField(new SortField("time", SortField.CUSTOM, true))
	
	req.setFetchStoredFields(false)
	
	var highlightScorer : Option[QueryScorer] = None
	// Parse a query
	  if (doHighlighting && q.length()>2){
	      try {
	        val sq = req.getQuery()
	        val luceneQ = DefaultConfigs.queryBuilderFactory.getQueryBuilder(sq).buildQuery()
	        highlightScorer = Some(new QueryScorer(luceneQ))
	      } catch {
	        case e: Exception => e.printStackTrace()
	      }
	  }
	
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
	  try{
	    val statusString = tweetStore(uid)
	    var statusJsonObj = new JSONObject();
	    if (statusString!=null){
		
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
	
		  hit.put("status",statusJsonObj)
	    }
	   }
	   catch{
	     case e : Exception => e.printStackTrace()
	   }
	   i+=1
	}
	val fetchEnd = System.currentTimeMillis()
	val end = System.currentTimeMillis()
	
	
	ChirperServlet.logger.info(request.getQueryString+" took: "+(end-start)+"ms")
	
	resultJSON.put("fetchtime",(fetchEnd-fetchStart))
	resultJSON.put("searchtime",(searchEnd-searchStart))
	resultJSON.put("totaltime",(end-start))
	resultJSON.toString()
	
  }
}
