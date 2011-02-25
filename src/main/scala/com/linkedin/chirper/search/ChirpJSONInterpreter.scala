package com.linkedin.chirper.search

import org.json.JSONObject
import com.sensei.indexing.api.JSONValueInterpreter
import org.apache.lucene.document._
import java.text._
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.Field.Index

// Converts a json object to a Lucene document
class ChirpJSONInterpreter extends JSONValueInterpreter[TweetEvent](classOf[TweetEvent]){

    override def buildDataObj(obj : JSONObject) : TweetEvent = {
	  val id = obj.getString("id_str").toLong
	  val date = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy").parse(obj.getString("created_at")).getTime()
	  val tweetEvent = new TweetEvent(id,date)
	  tweetEvent.contents = obj.optString("text")
	  tweetEvent
    }
}
