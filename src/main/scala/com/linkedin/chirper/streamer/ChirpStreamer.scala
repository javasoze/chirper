package com.linkedin.chirper.streamer

import com.linkedin.led.twitter.Config
import com.linkedin.led.twitter.streaming.StreamingClient

object ChirpStream {
  def main(args: Array[String]) = {
    val username = Config.readString("username")
    val password = Config.readString("password")

    val processor = new ChirperStreamProcessor()
	
    val twitterClient = new StreamingClient(username, password, processor)
    twitterClient.sample
  }
}