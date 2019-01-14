package waffleoRai_cafebotCommands;

import net.dv8tion.jda.core.entities.Member;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added the getCommandMessageID method
 * 
 * 1.1.0 -> 1.2.0 | August 7, 2018
 * 	Added methods for response/command message cleaning
 * 
 * 1.2.0 -> 1.3.0 | November 1, 2018
 * 	Added methods for command execution interrupt handling
 * 
 * 1.3.0 -> 1.3.1 | November 9, 2018
 * 	Added method to get the Member who sent the command.
 */

/**
 * A basic interface for executable bot commands.
 * @author Blythe Hospelhorn
 * @version 1.3.1
 * @since November 9, 2018
 */
public interface Command {
	
	/**
	 * Execute command or initial phase of command (such as a prompt for user
	 * confirmation) using the provided bot.
	 * @param bot Bot to execute this command with.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException;
	
	/**
	 * Execute the procedure that should be executed upon user confirmation
	 * using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @param msgid Long UID of Discord message containing the user response.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot, MessageID msgid) throws InterruptedException;
	
	/**
	 * Execute the procedure that should be executed upon user cancellation
	 * using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @param msgid Long UID of Discord message containing the user response.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_reject(AbstractBot bot, MessageID msgid) throws InterruptedException;
	
	/**
	 * Execute the procedure that should be executed upon user prompt timeout
	 * using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_timeout(AbstractBot bot) throws InterruptedException;
	
	/**
	 * Execute the procedure that should be executed upon automatic rejection
	 * of user input using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @param msgid Long UID of Discord message containing the user response.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_rerequest(AbstractBot bot, MessageID msgid) throws InterruptedException;
	
	/**
	 * Retrieve the MessageID (message & channel UIDs) of the original message containing the user-issued
	 * text command the bot brain received.
	 * @return MessageID of command message, or null if there is none.
	 */
	public MessageID getCommandMessageID();
	
	/**
	 * Retrieve the long UID of the guild the command was sent in.
	 * This is used for command cleaning mostly.
	 * @return Guild UID
	 */
	public long getGuildID();
	
	/**
	 * If the execution of this command throws an InterruptedException, this method
	 * tells an bot execution thread exception handler whether or not the command should
	 * be re-queued and execution re-attempted from the beginning.
	 * @return True if command should be pushed back to the top of the queue if interrupted.
	 * False if it should be tossed.
	 */
	public boolean requeueIfInterrupted();
	
	/**
	 * Get the Member who sent the command, if stored as metadata in the command.
	 * @return Requesting Member. If this returns null because the requesting member was not
	 * recorded, the requesting Member might be retrieved from the JDA using the channel and message
	 * IDs stored in the command.
	 */
	public Member getRequestingMember();
}
