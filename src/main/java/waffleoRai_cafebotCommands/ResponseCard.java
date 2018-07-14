package waffleoRai_cafebotCommands;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * Class to contain information about a pending bot prompt.
 * These are generated whenever a bot requires user confirmation or
 * cancellation to continue execution of a command.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class ResponseCard {

	private Command pendingCommand;
	private long channelID;
	private int timesinceSubmission;
	
	/**
	 * Construct a ResponseCard given the pending Command and the
	 * channel the response is expected on.
	 * @param cmd Pending Command
	 * @param chanID Discord long UID of pending channel.
	 */
	public ResponseCard(Command cmd, long chanID)
	{
		pendingCommand = cmd;
		channelID = chanID;
		timesinceSubmission = 0;
	}
	
	/**
	 * Increment the amount of time passed since submission (assumed
	 * unit in seconds) by one.
	 */
	public synchronized void incrementTime()
	{
		timesinceSubmission++;
	}
	
	/**
	 * Get the amount of time passed since response request submission.
	 * It is assumed that the unit of measurement is seconds.
	 * @return The time passed since response request submission.
	 */
	public synchronized int checkTime()
	{
		return timesinceSubmission;
	}
	
	/**
	 * Get the Discord long UID of the channel the response is expected on
	 * and the Command will complete execution on.
	 * @return UID of channel response is expected on.
	 */
	public long getChannelID()
	{
		return channelID;
	}
	
	/**
	 * Get whether the provided channel ID matches the channel ID of the
	 * pending channel.
	 * @param chanID Discord long UID of channel to check.
	 * @return True if the provided channel is the same as the pending channel. False if it isn't.
	 */
	public boolean checkChannelID(long chanID)
	{
		return (channelID == chanID);
	}
	
	/**
	 * Get the Command the response is pending for.
	 * @return Command associated with this response.
	 */
	public Command getCommand()
	{
		return pendingCommand;
	}
	
}
