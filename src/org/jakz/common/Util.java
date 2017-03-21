package org.jakz.common;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * DEPRECATED utility class
 * Use classes in org.jakz.common.util
 * @author johkal
 *
 */
@Deprecated
public class Util 
{

	//public Util() {}
	
	public static String stringSeparateFixedSpacingLeft(String target, String separator, int spacing)
	{
		StringBuilder result = new StringBuilder();
		
		for(int is=0; is<target.length(); is+=spacing)
		{
			if(is!=0)
				result.append(separator);
			if(is+spacing<target.length())
				result.append(target.substring(is, is+spacing));
			else
				result.append(target.substring(is));
		}
		
		return result.toString();
	}
	
	public static String stringSeparateFixedSpacingRight(String target, String separator, int spacing)
	{
		StringBuilder result = new StringBuilder();
		
		for(int is=target.length(); is>0; is-=spacing)
		{
			if(is!=target.length())
				result.insert(0,separator);
			if(is-spacing>=0)
				result.insert(0,target.substring(is-spacing, is));
			else
				result.insert(0,target.substring(0, is));
		}
		
		return result.toString();
	}
	
	public static Double numMinDouble(Double a, Double b)
	{
		if(a==null||b==null)
			return null;
		else
			return Math.min(a, b);
	}
	
	public static Double numMaxDouble(Double a, Double b)
	{
		if(a==null||b==null)
			return null;
		else
			return Math.min(a, b);
	}
	
	public static Integer numMinInteger(Integer a, Integer b)
	{
		if(a==null||b==null)
			return null;
		else
			return Math.min(a, b);
	}
	
	public static Integer numMaxInteger(Integer a, Integer b)
	{
		if(a==null||b==null)
			return null;
		else
			return Math.min(a, b);
	}
	
	public static Long numMinLong(Long a, Long b)
	{
		if(a==null||b==null)
			return null;
		else
			return Math.min(a, b);
	}
	
	public static Long numMaxLong(Long a, Long b)
	{
		if(a==null||b==null)
			return null;
		else
			return Math.min(a, b);
	}
	
	public static org.json.JSONArray copyJSONArrayInto(org.json.JSONArray from, org.json.JSONArray into)
	{
		for(int i=0; i<from.length(); i++)
		{
			into.put(from.get(i));
		}
		
		return into;
	}
	
	public static boolean deleteFileIfExistsOldCompatSafe(File f)
	{
		boolean toreturn = false;
		f=f.getAbsoluteFile();
		/*
		try
		{
			//This does not work
			toreturn=Files.deleteIfExists(f.toPath());
			return toreturn;
		}
		catch (Exception e)
		{
			//nothing
		}
		*/
		if(f.exists()&&f.isFile())
		{
			toreturn = f.delete();
			return toreturn;
		}
		
		return toreturn;
	}
	
	public static String getStackTraceString(Exception e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

}
