package org.jakz.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;

public class ZAuth 
{

	private HashMap<String, Credentialz> userCredentials;
	private int [] randomData;
	
	public int [] lastAuthenticationHash;
	public int [] lastTrueHash;
	
	private Credentialz aggregatedOperator;
	
	
	
	public ZAuth() 
	{
		userCredentials=new HashMap<String, Credentialz>();
		randomize(100);
		lastAuthenticationHash=new int[0];
		lastTrueHash=new int[0];
		aggregatedOperator=null;
	}
	
	public ZAuth(int [] nRandomData) 
	{
		userCredentials=new HashMap<String, Credentialz>();
		randomData=nRandomData;
		lastAuthenticationHash=new int[0];
		lastTrueHash=new int[0];
		aggregatedOperator=null;
	}
	
	public int [] randomize(int numBytes)
	{
		
		SecureRandom r = new SecureRandom();
		r.setSeed(System.currentTimeMillis());
		randomData= convertToUnsigned(r.generateSeed(numBytes));
		
		return randomData;
	}
	
	/*
	public String getRandomDataB64()
	{
		return DatatypeConverter.printBase64Binary(randomData);
	}
	*/
	
	public static String transformBytesToJSBody(int [] toTransform)
	{
		StringBuilder toReturn = new StringBuilder();
		
		
		toReturn.append("[");
		
		for(int i=0; i<toTransform.length; i++)
		{
			toReturn.append(toTransform[i]); 
			if(i!=toTransform.length-1)
				toReturn.append(",");
		}
		
		toReturn.append("]");
		
		return toReturn.toString();
	}
	
	public String getRandomDataJSBody()
	{
		return transformBytesToJSBody(randomData);
	}
	
	public int [] getRandomData()
	{
		return randomData;
	}
	
	public Credentialz getCredentials(int[] thename)
	{
		return userCredentials.get(new String(convertToBytes(thename)));
	}
	
	public void addCredentialOverwrite(int[] nname, int[] nAuthRaw)
	{
		MessageDigest md;
		try 
		{
			md = MessageDigest.getInstance("MD5");
			byte [] digest = md.digest(convertToBytes(nAuthRaw));
			//System.out.println("True digest of added user>"+new String(digest)+"<");
			userCredentials.put(new String(convertToBytes(nname)), new Credentialz(nname, nAuthRaw)); // put in digest for production
			//userCredentials.put(new String(nname), new Credentialz(nname, digest));
			
		} catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	/*
	
	public String createAuthenticationB64String(String rand, String auth)
	{
		MessageDigest md;
		try 
		{
			md = MessageDigest.getInstance("MD5");
			return DatatypeConverter.printBase64Binary(new String(md.digest(new String(rand+auth).getBytes())).getBytes());
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	*/
	
	public static int[] createAuthenticationHash(int[] rand, int[] auth)
	{
		MessageDigest md;
		try 
		{
			md = MessageDigest.getInstance("MD5");
			
			System.out.println("FUNCTION: Creating authentication hash");
			System.out.println("Random bytes following");
			debugPrintBytes(rand);
			System.out.println("Auth bytes following");
			debugPrintBytes(auth);
			System.out.println("Concatenated bytes following");
			debugPrintBytes(convertToBytes(concatBytes(rand,auth)));
			
			return convertToUnsigned(md.digest(convertToBytes(concatBytes(rand,auth))));
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean authenticate(int[] theid, int[] theauthentication)
	{
		System.out.println("FUNCTION: authenticate");
		Credentialz creds = getCredentials(theid);
		if(creds==null)
		{
			System.out.println("No credentials");
			return false;
		}
		
		int [] digest = createAuthenticationHash(randomData, creds.authentication);
		
		System.out.println("True hash following");
		debugPrintBytes(digest);
		lastTrueHash=digest;
		
		System.out.println("Authentication hash following");
		debugPrintBytes(theauthentication);
		lastAuthenticationHash=theauthentication;
		
		if(digest.length!=theauthentication.length)
		{
			System.out.println("Not same length!");
			return false;
		}
		
		for(int i=0; i<digest.length&&i<theauthentication.length; i++)
		{
			if(digest[i]!=theauthentication[i])
			{
				System.out.println("Found difference");
				return false;
			}
		}
		aggregatedOperator=creds;
		return true;
	}
	
	public static int[] concatBytes(int[] a, int[] b)
	{
		int[] toReturn = new int[a.length+b.length];
		for(int i=0; i<a.length; i++)
		{
			toReturn[i]=a[i];
		}
		
		for(int i=0; i<b.length; i++)
		{
			toReturn[a.length+i]=b[i];
		}
		
		return toReturn;
	}
	
	public static void debugPrintBytes(byte[] toprint)
	{
		System.out.println("Bytes>");
		for(int i=0; i<toprint.length; i++)
		{
			System.out.print("["+toprint[i]+"]");
		}
		System.out.println("<");
	}
	
	public static void debugPrintBytes(int[] toprint)
	{
		System.out.println("Unsigned Bytes>");
		for(int i=0; i<toprint.length; i++)
		{
			System.out.print("["+toprint[i]+"]");
		}
		System.out.println("<");
	}
	
	public static void debugPrintBytes(byte[] toprint, PrintStream stream)
	{
		stream.println("Bytes>");
		for(int i=0; i<toprint.length; i++)
		{
			stream.print("["+toprint[i]+"]");
		}
		stream.println("<");
	}
	
	public static int[] parseSerializedByteArray(String serialized, String delimeter)
	{
		String [] parts = serialized.split(delimeter);
		int[] toReturn = new int[parts.length];
		for(int i=0; i<parts.length; i++)
		{
			toReturn[i]=Integer.parseInt(parts[i]);
		}
		
		return toReturn;
	}
	
	public static int[] convertToUnsigned(byte[] signed)
	{
		int[] toReturn = new int[signed.length];
		
		for(int i=0; i<signed.length; i++)
		{
			byte toconvert = signed[i];
			toReturn[i]= toconvert & 255;
		}
		
		return toReturn;
	}
	
	public static byte[] convertToBytes(int[] ints)
	{
		byte[] toReturn = new byte[ints.length];
		
		for(int i=0; i<ints.length; i++)
		{
			byte toconvert = (byte)ints[i];
			toReturn[i]= toconvert;
		}
		
		return toReturn;
	}
	
	public static byte[] makeEasy(byte[] diff)
	{
		byte [] toReturn = new byte[diff.length];
		for(int i=0; i<diff.length; i++)
		{
			if(diff[i]<0)
				toReturn[i] = (byte)(-1*diff[i]);
			else
				toReturn[i] = diff[i];
		}
		
		return toReturn;
	}
	
	public Credentialz getAggregatedOperator()
	{
		return aggregatedOperator;
	}
}
