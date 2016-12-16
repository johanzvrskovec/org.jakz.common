package org.jakz.common;

import java.util.ArrayList;

public class Form
{
	public static enum FieldType {FORM,ELEMENT};
	
	public String id;
	public FieldType type;
	public ArrayList<TypedValue> value;
	private ArrayList<Form> children;
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

}
