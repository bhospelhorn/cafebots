package waffleoRai_cafebotCommands;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.Schedule;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | August 7, 2018
 * 	Added message ID tracking
 * 
 * 1.1.0 -> 1.1.1 | August 11, 2018
 * 	MessageID update
 *
 * 1.1.1 -> 1.1.2 | November 1, 2018
 * 	Added a method to push a request back on top of the queue.
 * 
 */

/**
 * A deque/map wrapper for management of user responses. Bots may prompt for confirmation
 * or cancellation of a particular action and will require a response from the user who
 * issued the initial command to continue. This queue records pending responses, handles
 * timeouts, and queues responses for processing once they are received.
 * <br><br><b>Background Threads:</b>
 * <br>- Timer Thread (<i>ResponseQueue.TimerThread</i>)
 * <br>Instantiated at Construction: N
 * <br>Started at Construction: N
 * <br>- Cleaner Thread (<i>ResponseQueue.CleanerThread</i>)
 * <br>Instantiated at Construction: N
 * <br>Started at Construction: N
 * <br><br><b>I/O Options:</b>
 * <br>[None]
 * <br><br><i>Outstanding Issues:</i>
 * <br>
 * @author Blythe Hospelhorn
 * @version 1.1.2
 * @since November 1, 2018
 */
public class ResponseQueue {
	
	/* ----- Constants ----- */
	
	/**
	 * Default number of seconds until a prompt for user confirmation times out.
	 */
	public static final int TIMEOUT_APPR_SECONDS = 90;

	/* ----- Instance Variables ----- */
	
	private Map<Long, ResponseCard> pending;
	private Deque<Response> queue;
	
	private TimerThread timer;
	private CleanerThread sweeper;
	
	private AbstractBot bot;
	
	/* ----- Inner Classes ----- */
	
	private class TimerThread extends Thread
	{
		private boolean killMe;
		
		public TimerThread()
		{
			killMe = false;
			this.setDaemon(true);
			Random r = new Random();
			this.setName("ResponseQueueTimerThread_" + Integer.toHexString(r.nextInt()));
		}
		
		public void run()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " ResponseQueue.TimerThread.run || Thread " + this.getName() + " started!");
			while(!killMe())
			{
				Set<Long> pkeys = getAllUIDs();
				for (Long l : pkeys)
				{
					ResponseCard card = getCard(l);
					card.incrementTime();
				}
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
					//e.printStackTrace();
					Thread.interrupted();
				}
			}
			System.err.println(Schedule.getErrorStreamDateMarker() + " ResponseQueue.TimerThread.run || Thread " + this.getName() + " terminating...");
		}
		
		public synchronized boolean killMe()
		{
			return killMe;
		}
		
		public synchronized void kill()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " ResponseQueue.TimerThread.run || Thread " + this.getName() + " termination requested!");
			killMe = true;
			this.interrupt();
		}
		
	}
	
	private class CleanerThread extends Thread
	{
		private boolean killMe;
		
		public CleanerThread()
		{
			killMe = false;
			this.setDaemon(true);
			Random r = new Random();
			this.setName("ResponseQueueCleanerThread_" + Integer.toHexString(r.nextInt()));	
		}
		
		public void run()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " ResponseQueue.CleanerThread.run || Thread " + this.getName() + " started!");
			while(!killMe())
			{
				Set<Long> pkeys = getAllUIDs();
				if (pkeys.size() < 1)
				{
					try 
					{
						Thread.sleep(1000 * TIMEOUT_APPR_SECONDS);
					} 
					catch (InterruptedException e) {
						//e.printStackTrace();
						Thread.interrupted();
					}
				}
				else
				{
					for (Long l : pkeys)
					{
						ResponseCard card = getCard(l);
						if (card == null) continue;
						if (card.checkTime() >= TIMEOUT_APPR_SECONDS)
						{
							System.err.println(Schedule.getErrorStreamDateMarker() + " ResponseQueue.CleanerThread.run || Timeout detected. Queueing timeout command...");
							removeCard(l);
							Response r = new Response(card.getCommand(), Response.RESPONSE_TIMEOUT, null);
							addToQueue(r);
						}
					}	
				}
			}
			System.err.println(Schedule.getErrorStreamDateMarker() + " ResponseQueue.CleanerThread.run || Thread " + this.getName() + " terminating...");
		}
		
		public synchronized boolean killMe()
		{
			return killMe;
		}
		
		public synchronized void kill()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " ResponseQueue.CleanerThread.run || Thread " + this.getName() + " termination requested!");
			killMe = true;
			this.interrupt();
		}
		
	}
	
	/* ----- Construction ----- */
	
	/**
	 * Construct an empty ResponseQueue. ResponseCard map is implemented as a HashMap, 
	 * Response queue is implemented as a LinkedList.
	 * <br>Background threads are neither instantiated nor started by this constructor!
	 */
	public ResponseQueue(AbstractBot linkedBot)
	{
		pending = new HashMap<Long, ResponseCard>();
		queue = new LinkedList<Response>();
		bot = linkedBot;
	}
	
	/* ----- Getters ----- */
	
	/**
	 * Pop the first Response off the response queue. If there are no Responses when this
	 * method is called, it will return null.
	 * @return The first Response in the queue, or null if the queue is empty.
	 */
	public synchronized Response popQueue()
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
	 * Push a request back on top of the stack (at the head of the queue).
	 * This should be used when execution is interrupted to re-queue the request.
	 * @param r Response to push back.
	 */
	public synchronized void pushRequest(Response r)
	{
		if (r != null) queue.push(r);
	}
	
	/**
	 * Get the number of Responses currently in the response queue.
	 * @return The size of the response queue.
	 */
	public synchronized int queueSize()
	{
		return queue.size();
	}
	
	/**
	 * Get whether the response queue is currently empty.
	 * @return True if the queue is empty, False if it is not empty.
	 */
	public synchronized boolean queueEmpty()
	{
		return queue.isEmpty();
	}
	
	/**
	 * Get whether there is a response currently pending for the given user.
	 * @param u User to look up.
	 * @return True if the bot using this response queue is currently waiting for a user
	 * response to a prompt, False otherwise.
	 */
	public synchronized boolean responsePending(User u)
	{
		long uid = u.getIdLong();
		return (pending.get(uid) != null);
	}
	
	/**
	 * Get whether there is a response currently pending for the given user.
	 * @param uid Discord long UID of user to look up.
	 * @return True if the bot using this response queue is currently waiting for a user
	 * response to a prompt, False otherwise.
	 */
	public synchronized boolean responsePending(long uid)
	{
		return (pending.get(uid) != null);
	}
	
	/**
	 * Get the UID of the channel on which the response pending from a given user is.
	 * @param u User response is pending on.
	 * @return Long UID of the Discord channel the response is expected on.
	 */
	public synchronized long getPendingChannel(User u)
	{
		long uid = u.getIdLong();
		ResponseCard c = pending.get(uid);
		if (c == null) return -1;
		else return c.getChannelID();
	}
	
	/**
	 * Get the UID of the channel on which the response pending from a given user is.
	 * @param uid Long ID of user response is pending on.
	 * @return Long UID of the Discord channel the response is expected on.
	 */
	public synchronized long getPendingChannel(long uid)
	{
		ResponseCard c = pending.get(uid);
		if (c == null) return -1;
		else return c.getChannelID();
	}
	
	/**
	 * Get the Discord long UIDs of all users the bot brain is waiting on a 
	 * response from.
	 * @return Set view of all long user UIDs pending response.
	 */
	public synchronized Set<Long> getAllUIDs()
	{
		return pending.keySet();
	}
	
	/* ----- Setters ----- */
	
	/**
	 * Queue a user response so that the pending bot can execute the rest of its
	 * command accordingly.
	 * @param r Response to add.
	 */
	public synchronized void addToQueue(Response r)
	{
		queue.addLast(r);
		if (bot != null) bot.interruptExecutionThread();
	}
	
	/* ----- Responding ----- */
	
	/**
	 * Send a response (as a pseudo enum) from a user and channel. The bot will check
	 * for any pending responses and reply accordingly.
	 * @param response Int correlating to response to send. See <i>Response</i> class constants.
	 * @param u User that sent the response.
	 * @param c Channel the response was sent from.
	 */
	public synchronized void respond(int response, User u, MessageChannel c, MessageID msgid)
	{
		long chanid = getPendingChannel(u);
		if (chanid != c.getIdLong())
		{
			forceTimeout(u);
			return;
		}
		switch(response)
		{
		case Response.RESPONSE_YES:
			respondYes(u, msgid);
			break;
		case Response.RESPONSE_NO:
			respondNo(u, msgid);
			break;
		case Response.RESPONSE_INVALID:
			respondInvalid(u, msgid);
			break;
		case Response.RESPONSE_TIMEOUT:
			forceTimeout(u);
			break;
		}
	}
	
	/**
	 * Send a "yes" response from User u to the bot. If there is a pending request
	 * for that user, the bot will reply accordingly.
	 * @param u User sending response.
	 */
	public synchronized void respondYes(User u, MessageID msgid)
	{
		long uid = u.getIdLong();
		ResponseCard c = pending.remove(uid);
		if (c == null) return;
		Response r = new Response(c.getCommand(), Response.RESPONSE_YES, msgid);
		addToQueue(r);
	}
	
	/**
	 * Send a "no" response from User u to the bot. If there is a pending request
	 * for that user, the bot will reply accordingly.
	 * @param u User sending response.
	 */
	public synchronized void respondNo(User u, MessageID msgid)
	{
		long uid = u.getIdLong();
		ResponseCard c = pending.remove(uid);
		if (c == null) return;
		Response r = new Response(c.getCommand(), Response.RESPONSE_NO, msgid);
		addToQueue(r);
	}
	
	/**
	 * Induce a "response invalid" reply from the bot from User u.
	 * Like a proper response, this method also removes the response card from the queue.
	 * @param u User sending response.
	 */
	public synchronized void respondInvalid(User u, MessageID msgid)
	{
		long uid = u.getIdLong();
		ResponseCard c = pending.remove(uid);
		if (c == null) return;
		Response r = new Response(c.getCommand(), Response.RESPONSE_INVALID, msgid);
		addToQueue(r);
	}
	
	/**
	 * Induce a "timeout" reply from the bot from User u.
	 * Like a proper response, this method also removes the response card from the queue.
	 * @param u User sending response.
	 */
	public synchronized void forceTimeout(User u)
	{
		long uid = u.getIdLong();
		ResponseCard c = pending.remove(uid);
		if (c == null) return;
		Response r = new Response(c.getCommand(), Response.RESPONSE_TIMEOUT, null);
		addToQueue(r);
	}
	
	/* ----- Thread Management ----- */
	
	/**
	 * Instantiate and start the thread for this queue that simply increments
	 * the time since submission count for each response card every second - essentially
	 * counting seconds since a response card was added.
	 */
	public synchronized void startTimer()
	{
		timer = new TimerThread();
		timer.start();
	}
	
	/**
	 * Kill the timer thread for this response queue. If this thread is inactive,
	 * time after response card submission will not be counted.
	 * <br>This method does nothing if the timer thread is null or not running.
	 */
	public synchronized void stopTimer()
	{
		if (timer == null) return;
		if (!timer.isAlive()) return;
		timer.kill();
	}
	
	/**
	 * Instantiate and start the thread that monitors the current response cards and
	 * removes any that have been queued for too long (have timed out). 
	 * <br>The time since submission for response cards is incremented by the Timer Thread,
	 * so if the Timer is not running, then the cleaner won't be cleaning anything.
	 */
	public synchronized void startCleaner()
	{
		sweeper = new CleanerThread();
		sweeper.start();
	}
	
	/**
	 * Kill the cleaner thread either for the purposes of shutting down the
	 * program or to stop timeouts from occurring.
	 * <br>If the cleaner thread is null or not running, this method will do nothing.
	 */
	public synchronized void killCleaner()
	{
		if (sweeper == null) return;
		if (!sweeper.isAlive()) return;
		sweeper.kill();
	}
	
	/**
	 * Instantiate and start both background threads (timer and cleaner) 
	 * for this response queue.
	 */
	public synchronized void startThreads()
	{
		startTimer();
		startCleaner();
	}
	
	/**
	 * Kill both background threads (timer and cleaner) for this response queue
	 * if they are running.
	 */
	public synchronized void killThreads()
	{
		stopTimer();
		killCleaner();
	}

	/* ----- Response Requesting ----- */
	
	/**
	 * Get information about a pending response in the form of a ResponseCard object.
	 * @param uid Long UID of the user to look up a response card for.
	 * @return ResponseCard of pending command if there is one for that user. Null if
	 * there is no response pending for the given user.
	 */
	public synchronized ResponseCard getCard(long uid)
	{
		return pending.get(uid);
	}
	
	/**
	 * Get information about a pending response for a given user and remove the
	 * response request from the ResponseQueue.
	 * @param uid Long UID of the user to look up a response card for.
	 * @return ResponseCard of pending command if there is one for that user. Null if
	 * there is no response pending for the given user.
	 */
	public synchronized ResponseCard removeCard(long uid)
	{
		return pending.remove(uid);
	}
	
	/**
	 * Request a user response to a command (such as confirmation or cancellation)
	 * from User u as should be expected on the channel the command was originally sent on.
	 * @param cmd Command the bot requires a response to complete.
	 * @param u User that sent the original command and who needs to provide response.
	 * @param ch Channel the original command was sent on and the response is expected on.
	 */
	public synchronized void requestResponse(Command cmd, User u, MessageChannel ch)
	{
		ResponseCard card = new ResponseCard(cmd, ch.getIdLong());
		pending.put(u.getIdLong(), card);
	}
	
}
