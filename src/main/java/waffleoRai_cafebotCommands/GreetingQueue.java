package waffleoRai_cafebotCommands;

import java.util.Deque;
import java.util.LinkedList;

import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;

/*
 * UPDATES
 * 
 * Creation | June 23, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * A deque wrapper for objects containing information about user joining guild events (ie. JDA
 * GuildMemberJoinEvent).
 * All functions accessing the internal deque are
 * synchronized so as to reduce the potential for concurrency errors. This object
 * is designed to be safe for access by multiple threads.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class GreetingQueue {
	
	private Deque<GuildMemberJoinEvent> queue;
	
	/**
	 * Construct an empty GreetingQueue. Instantiate the internal deque as a LinkedList.
	 */
	public GreetingQueue()
	{
		queue = new LinkedList<GuildMemberJoinEvent>();
	}
	
	/**
	 * Pop a join event from the front of the internal deque. This method both returns the
	 * event and removes it from the deque.
	 * <br>If no events are currently in the queue, this method will simply return null.
	 * @return The event at the front of the queue.
	 */
	public synchronized GuildMemberJoinEvent pop()
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
	 * Queue a new GuildMemberJoinEvent by adding it to the end of the queue.
	 * @param e Event to add to queue.
	 */
	public synchronized void add(GuildMemberJoinEvent e)
	{
		queue.addLast(e);
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
