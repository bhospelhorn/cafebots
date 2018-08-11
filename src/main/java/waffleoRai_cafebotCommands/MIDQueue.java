package waffleoRai_cafebotCommands;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A deque wrapper for MessageIDs. All functions accessing the internal deque are
 * synchronized so as to reduce the potential for concurrency errors. This object
 * is designed to be safe for access by multiple threads.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since August 10, 2018
 *
 */
public class MIDQueue {
	
	private int maxSize;
	private Deque<MessageID> queue;
	
	/**
	 * Construct an empty messageID queue. Instantiate the internal deque as a LinkedList.
	 * @param maxsize - The maximum number of elements the queue will hold before it starts to
	 * overwrite the oldest elements.
	 * <br>This is meant to prevent memory issues.
	 */
	public MIDQueue(int maxsize)
	{
		maxSize = maxsize;
		queue = new LinkedList<MessageID>();
	}
	
	/**
	 * Add a MessageID to the end of the queue.
	 * @param mid MessageID to add to queue.
	 */
	public synchronized void add(MessageID mid)
	{
		if (queue.size() >= maxSize) queue.pop();
		queue.addLast(mid);
	}
	
	/**
	 * Pop a MessageID from the front of the internal deque. This method both returns the
	 * ID and removes it from the deque.
	 * <br>If no IDs are currently in the queue, this method will simply return null.
	 * @return The ID at the front of the queue.
	 */
	public synchronized MessageID pop()
	{
		try
		{
			return queue.pop();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Get the number of MessageIDs currently in the queue.
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
	public synchronized List<MessageID> getQueueCopy()
	{
		List<MessageID> copy = new ArrayList<MessageID>(queue.size());
		copy.addAll(queue);
		return copy;
	}
	
	

}
