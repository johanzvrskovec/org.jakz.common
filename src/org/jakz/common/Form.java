package org.jakz.common;

import java.util.ArrayList;

public class Form implements JSONReader, JSONWriter
{
	public static enum FieldType {form,element};
	
	public String id;
	public FieldType type;
	public ArrayList<TypedValue> value;
	private Form parent;
	private IndexedMap<String,Form> content;
	
	
	
	private void init()
	{
		parent=null;
		content=new IndexedMap<String, Form>();
		value=new ArrayList<TypedValue>();
	}

	public Form(String nid, FieldType ntype) 
	{
		init();
		id=nid;
		type=ntype;	
	}

	@Override
	public JSONObject toJSON() 
	{
		JSONObject j = new JSONObject();
		j.put("id", id);
		j.put("type", type.name());
		j.put("value",value);
		if(parent!=null)
			j.put("parent", parent.id);
		else
			j.put("parent", JSONObject.NULL);
		
		//TODO this might be adapted later
		j.put("content",new JSONObject(content.toJSON().toString()));
		return null;
	}

	@Override
	public void fromJSON(JSONObject source) 
	{
		init();
		id=source.getString("id");
		type=FieldType.valueOf(source.getString("type"));
		JSONArray a = source.getJSONArray("value");
		for(int i=0; i<a.length(); i++)
		{
			JSONObject toputJSON= a.getJSONObject(i);
			TypedValue toput = new TypedValue();
			toput.fromJSON(toputJSON);
			value.add(toput);
		}
		
		//TODO
		//source.getString("parent");
		
		content.fromJSON(source.getJSONObject("content"));
		
	}

}
