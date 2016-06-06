package org.jakz.common;

import java.util.Comparator;
import java.util.List;

public interface Workable
{
	public Workable performWork() throws WorkableFailureException;
	public boolean canDivide();
	//public boolean isDone();
	public List<Workable> divide(int parallelism) throws WorkableFailureException;
	public List<Workable> divideMax() throws WorkableFailureException;
	public Workable unite(List<Workable> toUnite) throws WorkableFailureException;
	public Comparator<Workable> getComparator() throws WorkableFailureException;
	public Workable setSharedStore(SharedStore<String,Object> nSharedStore) throws WorkableFailureException;
	public Workable stop() throws WorkableFailureException;
	
}
