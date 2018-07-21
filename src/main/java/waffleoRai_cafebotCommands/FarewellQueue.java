package waffleoRai_cafebotCommands;

import java.util.Deque;
import java.util.LinkedList;

import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

/*
 * UPDATES
 * 
 * Creation | July 14, 2018
 * Version 1.0.0 Documentation | July 14, 2018
 * 
 */

/**
 * A deque wrapper for objects containing information about user leaving guild events (ie. JDA
 * GuildMemberLeaveEvent).
 * All functions accessing the internal deque are
 * synchronized so as to reduce the potential for concurrency errors. This object
 * is designed to be safe for access by multiple threads.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 14, 2018
 */
public class FarewellQueue {
	
private Deque<GuildMemberLeaveEvent> queue;
	
	/**
	 * Construct an empty FarewellQueue. Instantiate the internal deque as a LinkedList.
	 */
	public FarewellQueue()
	{
		queue = new LinkedList<GuildMemberLeaveEvent>();
	}
	
	/**
	 * Pop a leave event from the front of the internal deque. This method both returns the
	 * event and removes it from the deque.
	 * <br>If no events are currently in the queue, this method will simply return null.
	 * @return The event at the front of the queue.
	 */
	public synchronized GuildMemberLeaveEvent pop()
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
	 * Queue a new GuildMemberLeaveEvent by adding it to the end of the queue.
	 * @param e Event to add to queue.
	 */
	public synchronized void add(GuildMemberLeaveEvent e)
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
