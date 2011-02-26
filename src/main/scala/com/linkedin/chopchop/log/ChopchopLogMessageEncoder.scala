package com.linkedin.chopchop.log

import kafka.serializer.Encoder 
import kafka.message.Message
import org.apache.log4j.spi.LoggingEvent
import org.json.JSONObject

import com.linkedin.chirper.DefaultConfigs
import com.sensei.dataprovider.kafka.KafkaStreamIndexLoaderFactory.DefaultJsonFactory

class ChopchopLogMessageEncoder extends Encoder[LoggingEvent]{
	
	override def toMessage(event: LoggingEvent):Message = {
		val jsonString = toString(event)
		new Message(jsonString.getBytes(DefaultConfigs.UTF8Charset))
	}
	
	
	def toString(event: LoggingEvent):String = {
		val jsonObj = toJson(event)
		jsonObj.toString()
	}
	
	def toJson(event: LoggingEvent):JSONObject = {
		val jsonObj = new JSONObject()
		
		jsonObj.put("class",event.getFQNOfLoggerClass())
		jsonObj.put("thread",event.getThreadName())
		jsonObj.put("level",String.valueOf(event.getLevel()))
		jsonObj.put("timeStamp",event.getTimeStamp())
		jsonObj.put("logger",event.getLoggerName())
		jsonObj.put("message",event.getRenderedMessage())
		
		//TODO: add properties
		//TODO: add throwable info
		
		jsonObj
	}
}
