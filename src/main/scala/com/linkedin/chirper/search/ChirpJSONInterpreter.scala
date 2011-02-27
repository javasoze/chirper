package com.linkedin.chirper.search

import org.json.JSONObject
import com.sensei.indexing.api.JSONDataInterpreter
import org.apache.lucene.document._
import java.text._
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.Field.Index

// Converts a json object to a Lucene document
class ChirpJSONInterpreter extends JSONDataInterpreter{

	// get the UID from a tweet
		override def extractUID(obj:JSONObject): Long = {
		  val id = obj.getString("id_str").toLong
		  id
		}

		// build a Lucene doc, we only gonna index the tweet text and the time
		override def  buildDoc(obj:JSONObject): Document = {
		  val doc = new Document()

		//date format, e.g. Mon Mar 22 20:23:34 +0000 2010
		  val date = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy").parse(obj.getString("created_at"))
		  val dateFormated = new DecimalFormat("0000000000000000000000000000000000000000").format(date.getTime())
	      val dateField = new Field("time",dateFormated,Store.NO,Index.NOT_ANALYZED_NO_NORMS)
	      dateField.setOmitTermFreqAndPositions(true)
		  doc.add(new Field("contents",obj.optString("text"),Store.NO,Index.ANALYZED))
	      doc.add(dateField)
		  doc
		}
}
