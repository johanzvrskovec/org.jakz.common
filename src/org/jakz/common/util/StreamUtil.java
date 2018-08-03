package org.jakz.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil
{
	public final static int DEFAULT_BUFFER_SIZE_BYTES = 0x4000;
	
	/**
	 * Read all bytes for < Java 9
	 * @param is
	 * @param estSizeBytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] readAllBytes(InputStream is, int estSizeBytes) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(estSizeBytes);
		byte[] readBuf = new byte[estSizeBytes];
		int numRead = 0;
		while((numRead=is.read(readBuf))>=0)
		{
			bos.write(readBuf, 0, numRead);
		}
		bos.flush();
		return bos.toByteArray();
	}
	
	/**
	 * Read all bytes for < Java 9
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static byte[] readAllBytes(InputStream is) throws IOException
	{
		return readAllBytes(is, DEFAULT_BUFFER_SIZE_BYTES);
	}
	
	public static void pipeAll(InputStream is, OutputStream os, int estSizeBytes) throws IOException
	{
		byte[] readBuf = new byte[estSizeBytes];
		int numRead = 0;
		while((numRead=is.read(readBuf))>=0)
		{
			os.write(readBuf, 0, numRead);
		}
		os.flush();
	}
	
	public static void pipeAll(InputStream is, OutputStream os) throws IOException
	{
		pipeAll(is,os,DEFAULT_BUFFER_SIZE_BYTES);
	}
}
