package org.jakz.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil 
{
	
	public static void copy(InputStream in, File target) throws IOException
	{
		copy(in,target,null,null);
	}
	
	public static void copy(InputStream in, File target, Boolean append) throws IOException
	{
		copy(in,target,append,null);
	}
	
	public static void copy(InputStream in,File target,Boolean append,Integer bufferSizeBytes) throws IOException
	{
		if(append==null)
			append=false;
		if(bufferSizeBytes==null)
			bufferSizeBytes=100000;
		
		byte[] buf = new byte[bufferSizeBytes];
		FileOutputStream fos = new FileOutputStream(target,append);
		int readNumBytes = -1;
		while((readNumBytes = in.read(buf))>=0)
		{
			fos.write(buf, 0, readNumBytes);
		}
		
		fos.flush();
		fos.close();
	}
	
	public static boolean deleteFileIfExistsOldCompatSafe(File f) throws IOException
	{
		return deleteFileIfExistsOldCompatSafe(f, false);
	}
	
	public static boolean deleteFileIfExistsOldCompatSafe(File f, boolean fallbackToDeleteOnExit) throws IOException
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
			try
			{
				toreturn = f.delete();
			}
			catch (Exception e)
			{
				if(!fallbackToDeleteOnExit)
					throw new IOException(e);
			}
			
			if(fallbackToDeleteOnExit)
			{
				f.deleteOnExit();
				return true;
			}
			
			return toreturn;
		}
		
		return toreturn;
	}
	
	public static boolean deleteDirectoryIfExistsOldCompatSafe(File f, boolean recursive) throws IOException
	{
		return deleteDirectoryIfExistsOldCompatSafe(f,recursive,false);
	}
	
	public static boolean deleteDirectoryIfExistsOldCompatSafe(File f, boolean recursive, boolean fallbackToDeleteOnExit) throws IOException
	{
		boolean toreturn = false;
		f=f.getAbsoluteFile();
		if(f.exists()&&f.isDirectory())
		{
			if(recursive)
			{
				File[] content = f.listFiles();
				for(File c : content)
				{
					boolean subResult = false;
					if(c.isFile())
						subResult=FileUtil.deleteFileIfExistsOldCompatSafe(c,fallbackToDeleteOnExit);
					else if(c.isDirectory())
						subResult=FileUtil.deleteDirectoryIfExistsOldCompatSafe(c,recursive,fallbackToDeleteOnExit);
					
					if(!subResult)
						return false;
						//throw new IOException("Could not delete child in directory.");
				}
			}
			
			try
			{
				toreturn = f.delete();
			}
			catch (Exception e)
			{
				if(!fallbackToDeleteOnExit)
					throw new IOException(e);
			}
			
			if(fallbackToDeleteOnExit)
			{
				f.deleteOnExit();
				return true;
			}
			
			return toreturn;
		}
		
		return toreturn;
	}
}
