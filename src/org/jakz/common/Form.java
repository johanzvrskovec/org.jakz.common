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
	public static enum FieldType {FORM,QUERY,INFO};
	
	public String id;
	public FieldType type;
	public String name;
	public String text;
	public ArrayList<TypedValue> value;
	protected Form parent;
	public IndexedMap<String,Form> content;
	
	private void init()
	{
		parent=null;
		content=new IndexedMap<String, Form>();
		value=new ArrayList<TypedValue>();
		
		name="";
		text="";
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
		j.put("name", name);
		j.put("text", text);
		j.put("type", type.name());
		j.put("value",value);
		if(parent!=null)
			j.put("parent", parent.id);
		else
			j.put("parent", JSONObject.NULL);
		
		j.put("content",content.values());
		return null;
	}

	@Override
	public void fromJSONObject(JSONObject source) 
	{
		init();
		id=source.getString("id");
		name=source.getString("name");
		text=source.getString("text");
		type=FieldType.valueOf(source.getString("type").toUpperCase());
		JSONArray a = source.getJSONArray("value");
		for(int i=0; i<a.length(); i++)
		{
			JSONObject toputJSON= a.getJSONObject(i);
			TypedValue toput = new TypedValue();
			toput.fromJSONObject(toputJSON);
			value.add(toput);
		}
		
		content=new IndexedMap<String,Form>();
		JSONArray contentArray = source.getJSONArray("content");
		for(int i=0; i<contentArray.length(); i++)
		{
			Form newForm = new Form("DEFAULT", FieldType.FORM);
			newForm.fromJSONObject(contentArray.getJSONObject(i));
			newForm.parent=this;
			content.put(newForm.id, newForm);
		}
		
	}
	
	public boolean getHasContent()
	{
		return content!=null&&content.size()>0;
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
