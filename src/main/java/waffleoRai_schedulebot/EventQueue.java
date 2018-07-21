package waffleoRai_schedulebot;

import java.util.Collections;
import java.util.LinkedList;

public class EventQueue {
	
	private LinkedList<EventAdapter> queue;
	
	private Runnable lockingThread;
	//private boolean locked;
	
	public EventQueue()
	{
		queue = new LinkedList<EventAdapter>();
		//locked = false;
		lockingThread = null;
	}
	
	public synchronized void add(EventAdapter e) throws IllegalAccessException
	{
		if (lockingThread != null && lockingThread != Thread.currentThread()) throw new IllegalAccessException();
		queue.add(e);
	}
	
	public synchronized EventAdapter pop() throws IllegalAccessException
	{
		if (lockingThread != null && lockingThread != Thread.currentThread()) throw new IllegalAccessException();
		try
		{
			return queue.pop();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public synchronized void sort() throws IllegalAccessException
	{
		if (lockingThread != null && lockingThread != Thread.currentThread()) throw new IllegalAccessException();
		Collections.sort(queue);
	}
	
	public synchronized void lock()
	{
		lockingThread = Thread.currentThread();
		//locked = true;
	}
	
	public synchronized void unlock()
	{
		if (lockingThread != Thread.currentThread()) return;
		lockingThread = null;
		//locked = false;
	}
	
	public synchronized boolean isLocked()
	{
		if (lockingThread == null) return false;
		return lockingThread != Thread.currentThread();
	}

}
