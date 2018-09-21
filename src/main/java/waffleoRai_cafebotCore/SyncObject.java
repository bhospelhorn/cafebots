package waffleoRai_cafebotCore;

public class SyncObject<T> {
	
	private T obj;
	
	public SyncObject()
	{
		obj = null;
	}
	
	public synchronized T get()
	{
		return obj;
	}
	
	public synchronized void set(T o)
	{
		obj = o;
	}
	
	public synchronized boolean isNull()
	{
		return obj == null;
	}

}
