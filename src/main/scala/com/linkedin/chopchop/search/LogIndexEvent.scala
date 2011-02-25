package com.linkedin.chopchop.search

import com.sensei.indexing.api._

class LogIndexEvent(uid:Long,timeStamp:Long){
  @Uid
  val id = uid

  @Meta(name="class")
  var classname = ""

  @Meta(name="thread")
  var threadName = ""

  @Meta(name="level")
  var logLevel = ""

  @Meta(name="time")
  val time = timeStamp

  @Meta(name="logger")
  var logName = ""

  @Text(name="message")
  var logMessage = ""
}
