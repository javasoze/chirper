package com.linkedin.chirper.streamer

import com.linkedin.chirper.DefaultConfigs
import com.linkedin.led.twitter.config._
import com.linkedin.led.twitter.streaming.StreamingClient

// Runner that starts a streamer via Twitter sample API, can be change to pull in other stream types, 
// e.g. gardenhost etc.
object ChirpStream {
  def main(args: Array[String]) = {
    val username = Config.readString("username")
    val password = Config.readString("password")

    val processor = new ChirperStreamProcessor()
	
	DefaultConfigs.addShutdownHook(processor.shutdown())
    val twitterClient = new StreamingClient(username, password, processor)
    twitterClient.sample
  }
}
