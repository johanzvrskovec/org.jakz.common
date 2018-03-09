package org.jakz.common.security;

import java.util.Random;

public class NicePasswordGenerator
{
	private long seed;
	private Random randomGenerator;
	private static String candidateLetters = "abcdefghijklmnopqrstuvwxyz";
	private static String candidateVowel = "aoueiy";
	private static String candidateNumbers = "0123456789";
	private static String candidateSpecialCharacters ="!@$*%?+";
	private static String[] coreWord = {"area","book","case","day","eye","fact","hand","home","lot","month","night","number","part","place","point","right","room","story","study","thing","time","water","way","week","word","work","world","year"};
	
	public NicePasswordGenerator(long seed)
	{
		this.seed=seed;
		randomGenerator= new Random(seed);
	}
	
	public String generateSimple()
	{
		String coreWord1 = getRandomCoreWord();
		String composite1 = coreWord1+getRandomCandidateCharacter(candidateVowel.replace(coreWord1.substring(coreWord1.length()-1, coreWord1.length()), ""));
		composite1+=getRandomCandidateCharacter(candidateNumbers);
		composite1 = appendCandidateCharactersUpUntilLength(composite1,candidateNumbers,7);
		composite1+=getRandomCandidateCharacter(candidateLetters);
		composite1 = addRandomUpperCase(composite1,0.5);
		
		return composite1;
	}
	
	private String getRandomCoreWord()
	{
		int icoreWord = Math.abs(randomGenerator.nextInt() % coreWord.length);
		return coreWord[icoreWord];
	}
	
	private char getRandomCandidateCharacter(String candidateCharString)
	{
		int iCandidate = Math.abs(randomGenerator.nextInt() % candidateCharString.length());
		return candidateCharString.charAt(iCandidate);
	}
	
	private String addRandomUpperCase(String target, double p)
	{
		StringBuilder toReturn = new StringBuilder();
		for(int i=0; i<target.length(); i++)
		{
			if(randomGenerator.nextDouble()<p)
				toReturn.append(target.substring(i, i+1).toUpperCase());
			else
				toReturn.append(target.charAt(i));
		}
		
		return toReturn.toString();
	}
	
	private String appendCandidateCharactersUpUntilLength(String target,String candidateCharString,int targetLength)
	{
		StringBuilder toReturn  = new StringBuilder(target);
		for(int i=0; i<targetLength&&toReturn.length()<targetLength; i++)
		{
			toReturn.append(getRandomCandidateCharacter(candidateCharString));
		}
		
		return toReturn.toString();
	}
}
