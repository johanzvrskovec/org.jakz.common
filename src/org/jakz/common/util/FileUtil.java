package org.jakz.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.ibatis.javassist.bytecode.ByteArray;

public class FileUtil 
{
	
	private final static int DEFAULT_BUFFER_SIZE_BYTES = 10000000;
	
	public static void copyAttributes(File source,File target)
	{
		target.setLastModified(source.lastModified());
		target.setExecutable(source.canExecute());
		target.setReadable(source.canRead());
		target.setWritable(source.canWrite());
	}
	
	public static void copy(InputStream in, File target) throws IOException
	{
		copy(in,target,false,null);
	}
	
	public static void copy(InputStream in, File target, boolean append) throws IOException
	{
		copy(in,target,append,null);
	}
	
	public static void copy(InputStream in,File target,boolean append,Integer bufferSizeBytes) throws IOException
	{

		if(bufferSizeBytes==null)
			bufferSizeBytes=DEFAULT_BUFFER_SIZE_BYTES;
		
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
	
	public static void copy(File source,File target) throws IOException
	{
		copy(source, target, false, null, true);
	}
	
	public static void copy(File source,File target,boolean append) throws IOException
	{
		copy(source, target, append, null, true);
	}
	
	public static void copy(File source,File target,boolean append,Integer bufferSizeBytes) throws IOException
	{
		copy(source, target, append, bufferSizeBytes, true);
	}
	
	public static void copy(File source,File target,boolean append,Integer bufferSizeBytes, boolean copyAttributes) throws IOException
	{
		FileInputStream fis = new FileInputStream(source);
		FileUtil.copy(fis,target,append,bufferSizeBytes);
		fis.close();
		
		if(copyAttributes)
			copyAttributes(source,target);
	}
	
	public static void move(File source,File target,boolean copyAttributes) throws IOException
	{
		//if(copyAttributes)
		//{
			//long lastModified = source.lastModified();
			//boolean canExecute = source.canExecute();
			//boolean canRead = source.canRead();
			//boolean canWrite=source.canWrite();
			//Files.move(source.toPath(), target.toPath());
			copy(source,target,false,DEFAULT_BUFFER_SIZE_BYTES,copyAttributes);
			//target.setLastModified(lastModified);
			//target.setExecutable(canExecute);
			//target.setReadable(canRead);
			//target.setWritable(canWrite);
			source.delete();
		//}
		//else
			//Files.move(source.toPath(), target.toPath());
	}
	
	public static void move(File source,File target) throws IOException
	{
		move(source,target,true);
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
	
	public static void touch(File f) throws IOException
	{
		if(!f.exists())
		{
			try
			{
				new FileOutputStream(f).close();
			} catch (FileNotFoundException e)
			{
				throw new IOException(e);
			}
		}
	}
	
	public static byte[] calculateFileChecksumSHA512(File f, Integer bufferSizeBytes) throws NoSuchAlgorithmException, IOException
	{
		MessageDigest d = MessageDigest.getInstance("SHA-512");
		FileInputStream fin = new FileInputStream(f);
		DigestInputStream din = new DigestInputStream(fin, d);
		
		if(bufferSizeBytes==null)
			bufferSizeBytes=DEFAULT_BUFFER_SIZE_BYTES;
		
		byte[] buf = new byte[bufferSizeBytes];
		while((din.read(buf))>=0)
		{
			//nothing
		}
		
		din.close();
		fin.close();
		
		return d.digest();
		
	}
	
	public static byte[] calculateFileChecksumSHA512(File f) throws NoSuchAlgorithmException, IOException
	{
		return calculateFileChecksumSHA512(f, DEFAULT_BUFFER_SIZE_BYTES);
	}
	
	public static boolean compareFileChecksumSHA512(File a, File b, Integer bufferSizeBytes) throws NoSuchAlgorithmException, IOException
	{
		byte[] archiveFileChecksum = FileUtil.calculateFileChecksumSHA512(a, bufferSizeBytes);
		byte[] currentFileChecksum = FileUtil.calculateFileChecksumSHA512(b, bufferSizeBytes);
		return Arrays.equals(archiveFileChecksum, currentFileChecksum);
	}
	
	public static boolean compareFileChecksumSHA512(File a, File b) throws NoSuchAlgorithmException, IOException
	{
		return compareFileChecksumSHA512(a, b, DEFAULT_BUFFER_SIZE_BYTES);
	}
	
}
