package com.linkedin.chirper.services

import voldemort.server.VoldemortServer

object VoldemortRunner {
  def main(args: Array[String]) = {
	val params = new Array[String](1)
	params(0) = "config/voldemort"
    VoldemortServer.main(params)    
  }
}
