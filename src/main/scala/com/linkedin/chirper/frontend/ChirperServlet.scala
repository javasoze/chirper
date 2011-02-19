package com.linkedin.chirper.servlet

import com.linkedin.led.twitter.config._
import com.linkedin.chirper.search.ChirpSearchNode
import javax.servlet._
import org.scalatra._
import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate._
import org.fusesource.scalate.TemplateEngine
import com.sensei.search.req.SenseiRequest
import com.sensei.search.svc.impl.ClusteredSenseiServiceImpl
import com.sensei.search.client.servlet.DefaultSenseiJSONServlet

class ChirperServlet extends ScalatraServlet with ScalateSupport {
  var t1: Long = 0
  var t2: Long = 0

  val clusterName = Config.readString("zookeeper.cluster")
  val zkurl = Config.readString("zookeeper.url")
  val timeout = 30000

  val senseiSvc = new ClusteredSenseiServiceImpl(zkurl,timeout,clusterName)

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
	val req = new SenseiRequest()
	req.setOffset(offset)
	req.setCount(count)
	req.setFetchStoredFields(false)
	val results = senseiSvc.doQuery(req) // no facets for this
	DefaultSenseiJSONServlet.buildJSONResultString(req,results)
  }
}
