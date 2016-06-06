package org.jakz.common;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.omg.IOP.Encoding;

public class ZAuth_test 
{

	
	public static void main(String [] args) throws NoSuchAlgorithmException
	{
		System.out.println("Test>");
		ZAuth zauth = new ZAuth();
		
		int [] username = ZAuth.convertToUnsigned("johan".getBytes());
		int [] realPass = ZAuth.convertToUnsigned("hej123".getBytes());
		
		ZAuth.debugPrintBytes(username);
		ZAuth.debugPrintBytes(realPass);
		
		zauth.addCredentialOverwrite(username, realPass);
		//System.out.println("Auth hash test>"+new String (zauth.createAuthenticationHash("abcdefgh".getBytes(), "hej123".getBytes()), Charset.forName("UTF-8")));
		
		
		MessageDigest md;
		md = MessageDigest.getInstance("MD5");
		System.out.println(zauth.authenticate(username,ZAuth.convertToUnsigned("ndhatnmeyanoirhn".getBytes())));
		//System.out.println("Last auth hash>"+new String(zauth.lastAuthenticationHash)+"<");
		System.out.println("Last auth hash length>"+zauth.lastAuthenticationHash.length+"<");
		//System.out.println("Last true hash>"+new String(zauth.lastTrueHash)+"<");
		System.out.println("Last true hash length>"+zauth.lastTrueHash.length+"<");
		//System.out.println(zauth.authenticate(username, zauth.createAuthenticationHash("abcdefgh".getBytes(), md.digest("hej123".getBytes()))));
		System.out.println(zauth.authenticate(username, ZAuth.createAuthenticationHash(zauth.getRandomData(), ZAuth.convertToUnsigned("hej123".getBytes()))));
		//System.out.println("Last auth hash>"+new String(zauth.lastAuthenticationHash)+"<");
		//System.out.println("Last auth hash length>"+zauth.lastAuthenticationHash.length+"<");
		//System.out.println("Last true hash>"+new String(zauth.lastTrueHash)+"<");
		System.out.println("Last true hash length>"+zauth.lastTrueHash.length+"<");
		
		byte[] asbytes = "teststring".getBytes();
		int [] asints = ZAuth.convertToUnsigned(asbytes);
		
		ZAuth.debugPrintBytes(asbytes);
		ZAuth.debugPrintBytes(asints);
	}
}
