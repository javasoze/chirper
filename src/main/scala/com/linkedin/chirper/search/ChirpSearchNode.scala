package com.linkedin.chirper.search

import com.sensei.search.nodes.SenseiServer


import java.io.File

// Build a search node
object ChirpSearchNode{
	def main(args: Array[String]) = {
		val params = new Array[String](1)
		params(0) = "config/sensei/chirper"
	    SenseiServer.main(params)
    }	
}

