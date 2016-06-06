package org.jakz.common;

import java.io.Serializable;
import org.json.JSONException;

public class JSONObject extends org.json.JSONObject implements Serializable
{

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 7959547151127701409L;
	
	public JSONObject() throws JSONException
	{
		//super("");
	}

	public JSONObject(String sjson) throws JSONException
	{
		super(sjson);
	}
	
	public JSONObject(JSONObject json) throws JSONException
	{
		super(json.toString());
	}
	
	public JSONObject getJSONObject(String key) throws JSONException
	{
		return new JSONObject(super.getJSONObject(key).toString());
	}
	
	public JSONArray getJSONArray(String key) throws JSONException
	{
		return new JSONArray(super.getJSONArray(key).toString());
	}
	
}
