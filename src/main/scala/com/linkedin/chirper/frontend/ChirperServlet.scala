package com.linkedin.chirper.servlet

import javax.servlet._
import org.scalatra._
import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate._
import org.fusesource.scalate.TemplateEngine

class ChirperServlet extends ScalatraServlet with ScalateSupport {
  var t1: Long = 0
  var t2: Long = 0

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
}
