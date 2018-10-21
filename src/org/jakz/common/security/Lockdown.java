package org.jakz.common.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a simple class for authenticating users and authorizing user access
 * @author johkal
 *
 */
public class Lockdown
{
	public byte[] authRandBytes;
	public String authRandB64String;
	
	
	private Base64.Encoder b64encoder;
	private MessageDigest md;
	private SecureRandom randGen;
	
	private String curentUsername;
	private boolean authenticated = false;
	
	private HashSet<String> roles;
	
	public boolean settingResetRolesOnAuthenticationChange=true;
	
	
	public Lockdown() throws NoSuchAlgorithmException
	{
		
		final byte[] dummy = new byte[9];
		try
		{
			randGen = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e)
		{
			randGen = new SecureRandom();
		}
		randGen.nextBytes(dummy);
		b64encoder=Base64.getEncoder();
		md = MessageDigest.getInstance("SHA-512");
		roles = new HashSet<String>();
		regenerateAuthRand();
	}
	
	public void regenerateAuthRand()
	{
		authRandBytes = new byte[1024];
		randGen.nextBytes(authRandBytes);
		authRandB64String = b64encoder.encodeToString(authRandBytes);
	}
	
	/**
	 * Use this to generate hashed secrets that can be stored per user, instead of stored plain passwords. Or use another hashing scheme.
	 * @param userRealUsername
	 * @param userRealPassword
	 * @return
	 */
	public String generateUserHashedSecretB64String(String userRealUsername, String userRealPassword)
	{
		return b64encoder.encodeToString(md.digest((userRealPassword+userRealUsername+userRealPassword).getBytes()));
	}
	
	/**
	 * Authenticate using the object message digest. Also regenerates the internal random number.
	 * @param usernameString
	 * @param authenticationB64String
	 * @param userSecret Can be either a straight-forward password, or even better, a hashed secret
	 */
	public void authenticate(String usernameString, String authenticationB64String, String userSecret)
	{
		if(settingResetRolesOnAuthenticationChange)
			roles.clear();
		
		if(authRandB64String==null)
			throw new IllegalArgumentException("The internal authRandB64String can not be null for this to work.");
		curentUsername=usernameString;
		authenticated = false;
		
		String calculatedCorrectAuthenticationString = b64encoder.encodeToString(md.digest((authRandB64String+curentUsername+userSecret+authRandB64String).getBytes()));
		authenticated = calculatedCorrectAuthenticationString!=null
				&&calculatedCorrectAuthenticationString.length()>0
				&&calculatedCorrectAuthenticationString.equals(authenticationB64String);
				
		regenerateAuthRand();
	}
	
	public boolean isAuthenticated()
	{
		return authenticated;
	}
	
	public void setAuthenticated(boolean nAuthenticated)
	{
		if(settingResetRolesOnAuthenticationChange)
			roles.clear();
		authenticated=nAuthenticated;
	}
	
	public boolean addRole(Set<String> nRole)
	{
		return roles.addAll(nRole);
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
		return isAuthenticated() && roles.containsAll(rolesToAuthorizeFor);
	}
	
	public boolean authorize(String roleToAuthorizeFor)
	{
		return isAuthenticated() && roles.contains(roleToAuthorizeFor);
	}
	
	public SecureRandom getRandomGenerator() {return randGen;}
	
	public String getCurrentUsername() {return curentUsername;}
	
}
