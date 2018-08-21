package org.jakz.common.util;

import java.util.HashSet;

public class DStructUtil
{
	public static HashSet<String> toHashSet(String[] sa)
	{
		HashSet<String> toReturn = new HashSet<String>((int)(sa.length*1.4));
		for(String s:sa)
		{
			toReturn.add(s);
		}
		return toReturn;
	}
}
