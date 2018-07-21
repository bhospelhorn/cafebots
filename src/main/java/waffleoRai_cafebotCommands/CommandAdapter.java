package waffleoRai_cafebotCommands;

import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 21, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 
 */

/**
 * An abstract class implementing the less commonly used Command methods as either
 * empty methods or general methods.
 * <br>Command classes that don't prompt for user input would be well suited for CommandAdapter
 * extension.
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
 */
public abstract class CommandAdapter implements Command{
	
	private long command_message_ID = -1;
	
	/**
	 * Get the raw Long Integer UID of the channel this command is set to send messages to.
	 * @return The Discord channel UID for the message target of this command. If there is no
	 * message channel set for this command, this method returns -1.
	 */
	public long getChannelID()
	{
		return -1;
	}
	
	/**
	 * Get the raw Long UID of the user who originally sent the command.
	 * @return The Discord user UID of the commanding user.
	 */
	public abstract long getUserID();
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot) {
		//Does nothing.
		
	}

	@Override
	public void execute_reject(AbstractBot bot) {
		bot.displayGeneralCancel(getChannelID(), "user");
		
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_timeout(AbstractBot bot) {
		bot.timeoutPrompt(getChannelID(), getUserID());
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_rerequest(AbstractBot bot) {
		bot.displayRerequestMessage(getChannelID());
	}
	
	protected void setCommandMessageID(long cmdID)
	{
		command_message_ID = cmdID;
	}
	
	public long getCommandMessageID() 
	{
		return command_message_ID;
	}

}
