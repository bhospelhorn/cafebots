package waffleoRai_cafebotCore;

import java.util.HashSet;
import java.util.Set;

public class SyncSet<T> {
	
	private Set<T> set;
	
	public SyncSet()
	{
		set = new HashSet<T>();
	}
	
	public synchronized void add(T e)
	{
		set.add(e);
	}
	
	public synchronized void remove(T e)
	{
		set.remove(e);
	}
	
	public synchronized boolean contains(T e)
	{
		return set.contains(e);
	}

}
