package waffleoRai_cafebotCommands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
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
 * 1.1.0 -> 1.2.0 | July 10, 2018
 *  Command ID (long -> MessageID)
 *  
 * 1.2.0 -> 1.3.0 | January 14, 2019
 *  + Some AbstractBot methods now request full objects instead of just IDs
 *  + Handling for InterruptedException
 * 
 */

/**
 * An abstract class implementing the less commonly used Command methods as either
 * empty methods or general methods.
 * <br>Command classes that don't prompt for user input would be well suited for CommandAdapter
 * extension.
 * @author Blythe Hospelhorn
 * @version 1.3.0
 * @since January 14, 2019
 */
public abstract class CommandAdapter implements Command{
	
	private MessageID command_message_ID = null;
	protected Member requestingUser;
	
	/**
	 * Get the raw Long Integer UID of the channel this command is set to send messages to.
	 * @return The Discord channel UID for the message target of this command. If there is no
	 * message channel set for this command, this method returns -1.
	 */
	public long getChannelID()
	{
		return command_message_ID.getChannelID();
	}
	
	/**
	 * Get the raw Long UID of the user who originally sent the command.
	 * @return The Discord user UID of the commanding user.
	 */
	public long getUserID()
	{
		if (requestingUser == null) return -1;
		return requestingUser.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 * @throws InterruptedException If calling thread is interrupted during execution of command.
	 */
	public void execute_confirm(AbstractBot bot, MessageID msgid) throws InterruptedException 
	{
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 * @throws InterruptedException If calling thread is interrupted during execution of command.
	 */
	public void execute_reject(AbstractBot bot, MessageID msgid) throws InterruptedException {
		bot.displayGeneralCancel(getChannelID(), getRequestingMember());
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 * @throws InterruptedException If calling thread is interrupted during execution of command.
	 */
	public void execute_timeout(AbstractBot bot) {
		bot.timeoutPrompt(getChannelID(), getUserID());
		
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_rerequest(AbstractBot bot, MessageID msgid) throws InterruptedException {
		bot.displayRerequestMessage(getChannelID());
		bot.queueRerequest(this, this.getChannelID(), this.getUserID());
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}
	
	protected void setCommandMessageID(MessageID cmdID)
	{
		command_message_ID = cmdID;
	}
	
	public MessageID getCommandMessageID() 
	{
		return command_message_ID;
	}
	
	public long getGuildID()
	{
		if (requestingUser == null) return -1;
		return this.requestingUser.getGuild().getIdLong();
	}
	
	public void cleanAfterMyself(AbstractBot bot) throws InterruptedException
	{
		bot.queueCommandMessageForCleaning(getCommandMessageID(), getGuildID());
	}
	
	/**
	 * If the execution of this command throws an InterruptedException, this method
	 * tells an bot execution thread exception handler whether or not the command should
	 * be re-queued and execution re-attempted from the beginning.
	 * <br>CommandAdapter default is TRUE.
	 */
	public boolean requeueIfInterrupted()
	{
		return true; //This is the default
	}

	public Member getRequestingMember()
	{
		return requestingUser;
	}
	
	public Guild getGuild()
	{
		if (requestingUser == null) return null;
		return requestingUser.getGuild();
	}
	
}
