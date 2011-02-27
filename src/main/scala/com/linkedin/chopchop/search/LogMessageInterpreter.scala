package com.linkedin.chopchop.search

import org.json.JSONObject
import com.sensei.indexing.api.DefaultSenseiInterpreter
import com.sensei.dataprovider.kafka.KafkaJsonStreamDataProvider
import com.sensei.indexing.api.JSONDataInterpreter
import org.apache.lucene.document._
import java.text._
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.Field.Index

class LogMessageInterpreter extends JSONDataInterpreter{
	
	// get the UID from a tweet
	override def extractUID(obj:JSONObject): Long = {
	  val id = obj.getString(KafkaJsonStreamDataProvider.KAFKA_MSG_OFFSET).toLong
	  id
	}
	// build a Lucene doc, we only gonna index the tweet text and the time
	override def  buildDoc(jsonObj:JSONObject): Document = {
	  val doc = new Document()

	  val time = jsonObj.getString("timeStamp").toLong
	  val dateFormated = new DecimalFormat("0000000000000000000000000000000000000000").format(time)
	
	  val dateField = new Field("time",dateFormated,Store.NO,Index.NOT_ANALYZED_NO_NORMS)
	  dateField.setOmitTermFreqAndPositions(true)
	  doc.add(dateField)
	
	  val classField = new Field("class",jsonObj.optString("class"),Store.NO,Index.NOT_ANALYZED_NO_NORMS)
	  classField.setOmitTermFreqAndPositions(true)
	  doc.add(classField)
	 
	  val threadField = new Field("thread",jsonObj.optString("thread"),Store.NO,Index.NOT_ANALYZED_NO_NORMS)
	  threadField.setOmitTermFreqAndPositions(true)
	  doc.add(threadField)
	
	  val levelField = new Field("level",jsonObj.optString("level"),Store.NO,Index.NOT_ANALYZED_NO_NORMS)
	  levelField.setOmitTermFreqAndPositions(true)
	  doc.add(levelField)
	
	  val loggerField = new Field("logger",jsonObj.optString("logger"),Store.NO,Index.NOT_ANALYZED_NO_NORMS)
	  loggerField.setOmitTermFreqAndPositions(true)
	  doc.add(loggerField)
	
	  val jsonField = new Field("json",jsonObj.toString(),Store.YES,Index.NO)
	  doc.add(jsonField)
	
	  doc.add(new Field("contents",jsonObj.optString("message"),Store.NO,Index.ANALYZED))
	  
	  doc
	}
}
