package org.jakz.common;

import java.util.ArrayList;

import org.jakz.common.Form.FieldType;
/**
 * The Form is either a complete form or a nested element in a form.
 * @author johan
 *
 */
public class Form implements JSONObjectReadAspect, JSONObjectWriteAspect
{
	public static enum FieldType {container,single,multi};
	
	public String id;
	public FieldType type;
	public ArrayList<TypedValue> value;
	protected Form parent;
	protected IndexedMap<String,Form> content;
	
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
	
	public String getGlobalID()
	{
		return parent.getGlobalID()+"."+id;
	}

	@Override
	public JSONObject toJSONObject() 
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
		j.put("content",content.toJSONObject());
		return null;
	}

	@Override
	public void fromJSONObject(JSONObject source) 
	{
		init();
		id=source.getString("id");
		type=FieldType.valueOf(source.getString("type"));
		JSONArray a = source.getJSONArray("value");
		for(int i=0; i<a.length(); i++)
		{
			JSONObject toputJSON= a.getJSONObject(i);
			TypedValue toput = new TypedValue();
			toput.fromJSONObject(toputJSON);
			value.add(toput);
		}
		
		//TODO
		//source.getString("parent");
		
		content=source.get("content", new IndexedMap<String,Form>());
		//content.fromJSONObject(source.getJSONObject("content"));
		
	}
	
	protected JSONObject getValues(JSONObject toReturn)
	{
		toReturn.put(id, value);
		for(int i=0; i<content.size(); i++)
		{
			content.getAt(i).value.getValues(toReturn);
		}
		return toReturn;
	}

}
