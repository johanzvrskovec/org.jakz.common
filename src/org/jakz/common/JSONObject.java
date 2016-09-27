package org.jakz.common;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONString;
import org.json.JSONTokener;

public class JSONObject extends org.json.JSONObject implements Serializable
{

	private static final long serialVersionUID = 7959547151127701409L;
	
	public JSONObject() throws JSONException
	{
		super();
	}

	public JSONObject(String sjson) throws JSONException
	{
		super(sjson);
	}
	
	public JSONObject(JSONObject jo, String[] names) 
	{
        super(jo,names);
    }
	
	public JSONObject(JSONTokener x)
	{
		super(x);
	}
	
	public JSONObject(Map<?, ?> map)
	{
		 super(map);
	}
	
	public JSONObject(BeanTesta bean)
	{
		super(bean.theBean);
	}
	
	public JSONObject(Object pojo) throws IllegalArgumentException, IllegalAccessException
	{
		super();
		populateFromPOJO(pojo);
	}
	
	public JSONObject(Object object, String names[])
	{
		super(object,names);
	}
	
	public JSONObject(String baseName, Locale locale)
	{
		super(baseName,locale);
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
	
	private void populateFromPOJO(Object pojo) throws IllegalArgumentException, IllegalAccessException
	{
        Class<?> pojoClass = pojo.getClass();
        Field[] field = pojoClass.getFields();
        for(int i=0; i<field.length; i++)
		{
        	String fieldName = field[i].getName();
        	Object fieldValue = null;
        	
        	//try
        	//{
        		fieldValue = field[i].get(pojo);
        	//}
        	//catch (Exception e)
        	//{
        		//ignore this member
        	//}
        	put(fieldName, wrapToJSON(fieldValue));
		}
    }
	
	//TODO not working completely yet
	/**
	 * DO NOT USE! This is just a sketch in development.
	 * @param source
	 * @param target
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 */
	public static Object injectIntoPOJO(Object source, Object target) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, SecurityException, InvocationTargetException
	{
		if (	source instanceof Byte || source instanceof Character
                || source instanceof Short || source instanceof Integer
                || source instanceof Long || source instanceof Boolean
                || source instanceof Float || source instanceof Double
                || source instanceof String || source instanceof BigInteger
                || source instanceof BigDecimal) 
        {
            return source;
        }
		
		
		Class<?> pojoClass = null;
		if(target!=null)
			pojoClass=target.getClass();
		else
		{
			pojoClass=source.getClass();
			target=pojoClass.newInstance();
		}
		
		
		
        Field[] field = pojoClass.getFields();
        Object toReturn = pojoClass.newInstance();
        for(int i=0; i<field.length; i++)
		{
        	String fieldName = field[i].getName();
        	Class<?> fieldType = field[i].getType();
        	ParameterizedType fieldParameterizedType = (ParameterizedType) field[i].getGenericType();
        	
        	if(source instanceof JSONObject)
        	{
        		JSONObject sjson = (JSONObject) source;
	        	if(fieldType==Integer.class||fieldType==Byte.class)
	        		field[i].set(target, sjson.getInt(fieldName));
	        	else if(fieldType==BigInteger.class||fieldType==Long.class)
	        		field[i].set(target, sjson.getBigInteger(fieldName));
	        	else if(fieldType==Boolean.class)
	        		field[i].set(target, sjson.getBoolean(fieldName));
	        	else if(fieldType==Double.class)
	        		field[i].set(target, sjson.getDouble(fieldName));
	        	else if(fieldType==String.class||fieldType==Character.class)
	        		field[i].set(target, sjson.getString(fieldName));
	        	else if(fieldType.isArray())
	        	{
	        		JSONArray ajson = sjson.getJSONArray(fieldName);
	        		Object apojo = Array.newInstance(fieldType, ajson.length());
	        		Class<?> componentType = fieldType.getComponentType();
		            for (int ai = 0; ai < ajson.length(); ai++) 
		            {
		            	Object toAddValue = ajson.get(i);
		            	Object componentPOJO = componentType.newInstance();
		            	Array.set(apojo, ai, injectIntoPOJO(toAddValue,componentPOJO));
		            }
		            
		            field[i].set(target, apojo);
	        	}
	        	else if(Collection.class.isAssignableFrom(fieldType))
	        	{
	        		JSONArray ajson = sjson.getJSONArray(fieldName);
	        		Collection<?> cpojo = (Collection<?>)fieldType.newInstance();
	        		
		            for (int ai = 0; ai < ajson.length(); ai++) 
		            {
		            	Object toAddValue = ajson.get(i);
		            	Class<?> genericType = (Class<?>)fieldParameterizedType.getActualTypeArguments()[0];
		            	System.out.println("generic type: "+genericType.getName());
		            	Object componentPOJO = genericType.newInstance(); //TODO - funkar inte
		            	Method addMethod =  fieldType.getMethod("add", Object.class);
		            	addMethod.invoke(cpojo, injectIntoPOJO(toAddValue,componentPOJO));
		            }
		            
		            field[i].set(target, cpojo);
	        	}
	        	else
	        	{
	        		try
	        		{
	        			Object pojoInstance = fieldType.newInstance();
	        			field[i].set(target, injectIntoPOJO(sjson.get(fieldName),pojoInstance));
	        		}
	        		catch (Exception e)
	        		{
	        			throw new JSONException("Could not inject field "+fieldName+" of type "+fieldType.getName()+" into POJO.", e);
	        		}
	        	}
        	}
        	
        	//add more sources
		}
        
        return toReturn;
    }
	
	public Object toPOJO(Object pojo) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, SecurityException, InvocationTargetException
	{
		return injectIntoPOJO(this, pojo);
	}
	
	public static Object wrapToJSON(Object object)
	{
        if (object == null) 
        {
            return null;
        }
        
        if (object instanceof JSONObject || object instanceof JSONArray
                || NULL.equals(object) || object instanceof JSONString
                || object instanceof Byte || object instanceof Character
                || object instanceof Short || object instanceof Integer
                || object instanceof Long || object instanceof Boolean
                || object instanceof Float || object instanceof Double
                || object instanceof String || object instanceof BigInteger
                || object instanceof BigDecimal) 
        {
            return object;
        }

        if (object instanceof Collection) 
        {
            Collection<?> coll = (Collection<?>) object;
            return new JSONArray(coll);
        }
        if (object.getClass().isArray())
        {
            return new JSONArray(object);
        }
        if (object instanceof Map) 
        {
            Map<?, ?> map = (Map<?, ?>) object;
            return new JSONObject(map);
        }
        Package objectPackage = object.getClass().getPackage();
        String objectPackageName = objectPackage != null ? objectPackage
                .getName() : "";
        if (objectPackageName.startsWith("java.")
                || objectPackageName.startsWith("javax.")
                || object.getClass().getClassLoader() == null) 
        {
            return object.toString();
        }
        
       
    	try
    	{
    		return new JSONObject(object);
    	}
    	catch (Exception e)
    	{
    		return (JSONObject)org.json.JSONObject.wrap(object);
    	}

    }
	
	/*
	public static Object wrapToPOJO(Object object)
	{
        if (object == null) 
        {
            return null;
        }
        
        if (object instanceof JSONObject || object instanceof JSONArray
                || NULL.equals(object) || object instanceof JSONString
                || object instanceof Byte || object instanceof Character
                || object instanceof Short || object instanceof Integer
                || object instanceof Long || object instanceof Boolean
                || object instanceof Float || object instanceof Double
                || object instanceof String || object instanceof BigInteger
                || object instanceof BigDecimal) 
        {
            return object;
        }

        return 

    }
	*/
	
	//OLD ALTERNATE TRY
	/*
	public <T> T populate(T toReturn) throws IllegalArgumentException, IllegalAccessException, JSONException
	{
		Class<?> c = toReturn.getClass();
		
		Field[] field = c.getFields();
		for(int i=0; i<field.length; i++)
		{
			if(field[i].getType()==String.class||field[i].getType()==Integer.class||field[i].getType()==Double.class||field[i].getType()==Long.class||field[i].getType()==JSONObject.class||field[i].getType()==JSONArray.class||field[i].getType()==JSONObject[].class)
				field[i].set(toReturn, this.get(field[i].getName()));
		}
		
		return toReturn;
	}
	*/
}
