package com.linkedin.chopchop.search

import org.json.JSONObject
import com.sensei.indexing.api.DefaultSenseiInterpreter
import com.sensei.dataprovider.kafka.KafkaJsonStreamDataProvider
import com.sensei.indexing.api.JSONValueInterpreter

class LogMessageInterpreter extends JSONValueInterpreter[LogIndexEvent](classOf[LogIndexEvent]){

	override def buildDataObj(jsonObj : JSONObject) : LogIndexEvent = {
		val uid = jsonObj.getString(KafkaJsonStreamDataProvider.KAFKA_MSG_OFFSET).toLong
		val time = jsonObj.getString("timeStamp").toLong
		val event = new LogIndexEvent(uid,time)
		
		event.classname = jsonObj.optString("class")
		event.threadName = jsonObj.optString("thread")
		event.logLevel = jsonObj.optString("level")
		event.logName = jsonObj.optString("logger")
		event.logMessage = jsonObj.optString("message")
		event.json = jsonObj.toString()
		event
	}
}
