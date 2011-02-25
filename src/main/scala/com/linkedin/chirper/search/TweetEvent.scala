package com.linkedin.chirper.search

import com.sensei.indexing.api._
import com.linkedin.chirper.DefaultConfigs

class TweetEvent(uid:Long,tweetTime:Long){

  @Uid
  val id = uid
 
  @Meta(name="time")
  val time = tweetTime

  @Text(name="contents")
  var contents = ""
}
