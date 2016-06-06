package org.jakz.common;

import java.io.Serializable;
import org.json.JSONException;

public class JSONArray extends org.json.JSONArray implements Serializable
{

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 3433718803782110393L;

	public JSONArray() throws JSONException
	{
		//super("");
	}
	
	public JSONArray(String sjson) throws JSONException
	{
		super(sjson);
	}
	
	public JSONArray(JSONArray json) throws JSONException
	{
		super(json.toString());
	}
	
	public JSONObject getJSONObject(int index) throws JSONException
	{
		return new JSONObject(super.getJSONObject(index).toString());
	}
	
}
