package com.linkedin.chirper.search

import java.text._
import com.sensei.indexing.api.JsonFilter
import org.json.JSONObject

class ChirperJsonFilter extends JsonFilter{
	override def filter(obj:JSONObject): JSONObject = {
		val date = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy").parse(obj.getString("created_at"))
		val time = date.getTime()
		obj.put("time",time)
		obj
	}	
}
