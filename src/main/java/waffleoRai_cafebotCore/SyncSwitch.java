package waffleoRai_cafebotCore;

public class SyncSwitch {
	
	private boolean value;
	
	public synchronized boolean get()
	{
		return value;
	}
	
	public synchronized void set(boolean b)
	{
		value = b;
	}

}
