package org.jakz.common.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a simple class for authenticating users and authorizing user access
 * @author johkal
 *
 */
public class Lockdown
{
	
	private Base64.Encoder b64encoder;
	private MessageDigest md;
	
	private String curentUsername;
	private boolean authenticated = false;
	
	private HashSet<String> roles;
	
	public Lockdown() throws NoSuchAlgorithmException
	{
		b64encoder=Base64.getEncoder();
		md = MessageDigest.getInstance("SHA-512");
		roles = new HashSet<String>();
	}
	
	public String generateUserHashedSecretB64String(String userRealUsername, String userRealPassword)
	{
		return b64encoder.encodeToString(md.digest((userRealPassword+userRealUsername+userRealPassword).getBytes()));
	}
	
	/**
	 * Authenticate using the object message digest.
	 * @param usernameString
	 * @param authenticationB64String
	 * @param randomB64String
	 */
	public void authenticate(String usernameString, String authenticationB64String, String randomB64String, String userHashedSecretB64String)
	{
		curentUsername=usernameString;
		authenticated = false;
		
		String calculatedCorrectAuthenticationString = b64encoder.encodeToString(md.digest((randomB64String+curentUsername+userHashedSecretB64String+randomB64String).getBytes()));
		authenticated = calculatedCorrectAuthenticationString!=null
				&&calculatedCorrectAuthenticationString.length()>0
				&&calculatedCorrectAuthenticationString.equals(authenticationB64String);
	}
	
	public boolean isAuthenticated()
	{
		return authenticated;
	}
	
	/**
	 * Adds role.
	 * @param nRole
	 * @return true on add, false on pre-existing role
	 */
	public boolean addRole(String nRole)
	{
		return roles.add(nRole);
	}
	
	public boolean authorize(Set<String> rolesToAuthorizeFor)
	{
		if(isAuthenticated())
		{
			roles.containsAll(rolesToAuthorizeFor);
		}
		return false;
	}
	
	public boolean authorize(String role)
	{
		return isAuthenticated()&&roles.contains(role);
	}
	
}
