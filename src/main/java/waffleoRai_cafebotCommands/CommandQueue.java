package waffleoRai_cafebotCommands;

import java.util.Deque;
import java.util.LinkedList;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * A deque wrapper for bot commands. All functions accessing the internal deque are
 * synchronized so as to reduce the potential for concurrency errors. This object
 * is designed to be safe for access by multiple threads.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 *
 */
public class CommandQueue {

	private Deque<Command> queue;
	
	/**
	 * Construct an empty command queue. Instantiate the internal deque as a LinkedList.
	 */
	public CommandQueue()
	{
		queue = new LinkedList<Command>();
	}
	
	/**
	 * Add a bot command to the end of the queue.
	 * @param c Command to add to queue.
	 */
	public synchronized void addCommand(Command c)
	{
		//System.err.println(Thread.currentThread().getName() + " || CommandQueue.addCommand || DEBUG - queuing command...");
		queue.addLast(c);
	}
	
	/**
	 * Pop a command from the front of the internal deque. This method both returns the
	 * Command and removes it from the deque.
	 * <br>If no commands are currently in the queue, this method will simply return null.
	 * @return The command at the front of the queue.
	 */
	public synchronized Command popCommand()
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
	 * Get the number of Commands currently in the queue.
	 * @return The number of Commands in the queue (the queue size).
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
	
}
