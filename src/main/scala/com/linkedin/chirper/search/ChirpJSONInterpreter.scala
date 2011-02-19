package com.linkedin.chirper.search

import org.json.JSONObject
import com.sensei.indexing.api.JSONDataInterpreter
import org.apache.lucene.document._

import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.Field.Index

class ChirpJSONInterpreter extends JSONDataInterpreter{

	override def extractUID(obj:JSONObject): Long = {
	  val id = obj.getString("text").toLong
	  id
	}
	
	override def  buildDoc(obj:JSONObject): Document = {
	  val doc = new Document()
	  doc.add(new Field("contents",obj.getString("text"),Store.NO,Index.ANALYZED))
	  doc
	} 
}
