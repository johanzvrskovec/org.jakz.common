package org.jakz.common;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import org.jakz.common.JSONObject;
import org.json.JSONException;
import org.json.JSONTokener;

public class JSONArray extends org.json.JSONArray implements Serializable
{
	private static final long serialVersionUID = 3433718803782110393L;

	public JSONArray() throws JSONException
	{
		super();
	}
	
	public JSONArray(JSONTokener x)
	{
		super(x);
	}
	
	public JSONArray(Collection<?> collection) 
	{
		this();
        if (collection != null) 
        {
        	for (Object o: collection)
        	{
        		put(JSONObject.wrapToJSON(o));
        	}
        }
    }
	
	public JSONArray(Object array) throws JSONException 
	{
        this();
        if (array.getClass().isArray())
        {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) 
            {
                put(JSONObject.wrapToJSON(Array.get(array, i)));
            }
        } else 
        {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
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
