package org.jakz.common.util;

import java.io.File;

public class FileUtil 
{
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
}
