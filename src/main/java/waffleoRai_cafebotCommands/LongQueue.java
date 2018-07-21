package waffleoRai_cafebotCommands;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A deque wrapper for long IDs. All functions accessing the internal deque are
 * synchronized so as to reduce the potential for concurrency errors. This object
 * is designed to be safe for access by multiple threads.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 20, 2018
 *
 */
public class LongQueue {

	private int maxSize;
	private Deque<Long> queue;
	
	/**
	 * Construct an empty long queue. Instantiate the internal deque as a LinkedList.
	 * @param maxsize - The maximum number of elements the queue will hold before it starts to
	 * overwrite the oldest elements.
	 * <br>This is meant to prevent memory issues.
	 */
	public LongQueue(int maxsize)
	{
		maxSize = maxsize;
		queue = new LinkedList<Long>();
	}
	
	/**
	 * Add a long ID to the end of the queue.
	 * @param l Long ID to add to queue.
	 */
	public synchronized void add(long l)
	{
		if (queue.size() >= maxSize) queue.pop();
		queue.addLast(l);
	}
	
	/**
	 * Pop a long ID from the front of the internal deque. This method both returns the
	 * ID and removes it from the deque.
	 * <br>If no IDs are currently in the queue, this method will simply return null.
	 * @return The ID at the front of the queue.
	 */
	public synchronized long pop()
	{
		try
		{
			return queue.pop();
		}
		catch (Exception e)
		{
			return -1;
		}
	}
	
	/**
	 * Get the number of long IDs currently in the queue.
	 * @return The number of IDs in the queue (the queue size).
	 */
	public synchronized int size()
	{
		return queue.size();
	}
	
	/**
	 * Get whether the queue is currently empty.
	 * @return True if the queue is empty. False if the queue is not empty.
	 */
	public synchronized boolean isEmpty()
	{
		return queue.isEmpty();
	}
	
	/**
	 * Get a copy of the queue in the same order they are in the queue at the time
	 * the method is called.
	 * @return Ordered list containing the same elements in the queue.
	 */
	public synchronized List<Long> getQueueCopy()
	{
		List<Long> copy = new ArrayList<Long>(queue.size());
		copy.addAll(queue);
		return copy;
	}
	
	
}
