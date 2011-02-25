package com.linkedin.chopchop.log

import kafka.serializer.Encoder 
import kafka.message.Message
import org.apache.log4j.spi.LoggingEvent
import org.json.JSONObject

import com.linkedin.chirper.DefaultConfigs

class ChopchopLogMessageEncoder extends Encoder[LoggingEvent]{
	override def toMessage(event: LoggingEvent):Message = {
		val jsonObj = new JSONObject()
		
		jsonObj.put("class",event.getFQNOfLoggerClass())
		jsonObj.put("thread",event.getThreadName())
		jsonObj.put("level",String.valueOf(event.getLevel()))
		jsonObj.put("timeStamp",event.getTimeStamp())
		jsonObj.put("logger",event.getLoggerName())
		jsonObj.put("message",event.getRenderedMessage())
		
		//TODO: add properties
		//TODO: add throwable info
		val jsonString = jsonObj.toString()
		new Message(jsonString.getBytes(DefaultConfigs.UTF8Charset))
	}
}
