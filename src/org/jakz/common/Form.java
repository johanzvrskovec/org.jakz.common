package org.jakz.common;

import java.util.ArrayList;
/**
 * The Form is either a complete form or a nested element in a form.
 * @author johan
 *
 */

//TODO use modified DataEntry? Merge DataEntry and Form
public class Form implements JSONObjectReadAspect, JSONObjectWriteAspect
{
	public static enum FieldType {FORM,QUERY,INFO};
	
	/**
	 * Codeable identifier for the form object
	 */
	public String id;
	public FieldType type;
	/**
	 * Human readable name text
	 */
	public String name;
	/**
	 * Human readable descriptive text
	 */
	public String text;
	public ArrayList<TypedValue> value;
	protected Form parent;
	public IndexedMap<String,Form> content;
	public boolean required,nullable,writeable,tablekey;
	
	private void init()
	{
		parent=null;
		content=new IndexedMap<String, Form>();
		value=new ArrayList<TypedValue>();
		
		name="";
		text="";
		
		required =false;
		nullable = true;
		writeable = false;
		tablekey = false;
	}

	/**
	 * Shallow copy of source into this
	 * @param source
	 * @return
	 */
	public Form scopy(Form source)
	{
		id=source.id;
		type=source.type;
		name=source.name;
		text=source.text;
		value=source.value;
		parent=source.parent;
		content=source.content;
		required=source.required;
		nullable=source.nullable;
		writeable=source.writeable;
		tablekey=source.tablekey;
		return this;	
	}
	
	public Form(String nid, FieldType ntype) 
	{
		init();
		id=nid;
		type=ntype;	
	}
	
	public String getGlobalID()
	{
		if(parent!=null)
			return parent.getGlobalID()+"."+id;
		else
			return id;
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
		
		j.put("required",required);
		j.put("nullable",nullable);
		j.put("writeable",writeable);
		j.put("tablekey",tablekey);
		
		return j;
	}

	@Override
	public void fromJSONObject(JSONObject source) 
	{
		init();
		id=source.getString("id");
		name=source.getString("name");
		text=source.optString("text");
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
		
		required=source.optBoolean("required");
		nullable=source.optBoolean("nullable");
		writeable=source.optBoolean("writeable");
		tablekey=source.optBoolean("tablekey");
		
	}
	
	public boolean getHasContent()
	{
		return content!=null&&content.size()>0;
	}
	
	public Form addContent(Form nContent)
	{
		content.put(nContent.id, nContent);
		nContent.parent=this;
		return this;
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
	
	public String toString()
	{
		return toJSONObject().toString();
	}

}
