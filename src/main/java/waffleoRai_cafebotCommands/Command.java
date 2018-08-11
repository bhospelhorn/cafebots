package waffleoRai_cafebotCommands;

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
 */

/**
 * A basic interface for executable bot commands.
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since August 7, 2018
 */
public interface Command {
	
	/**
	 * Execute command or initial phase of command (such as a prompt for user
	 * confirmation) using the provided bot.
	 * @param bot Bot to execute this command with.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot);
	
	/**
	 * Execute the procedure that should be executed upon user confirmation
	 * using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @param msgid Long UID of Discord message containing the user response.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot, MessageID msgid);
	
	/**
	 * Execute the procedure that should be executed upon user cancellation
	 * using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @param msgid Long UID of Discord message containing the user response.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_reject(AbstractBot bot, MessageID msgid);
	
	/**
	 * Execute the procedure that should be executed upon user prompt timeout
	 * using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_timeout(AbstractBot bot);
	
	/**
	 * Execute the procedure that should be executed upon automatic rejection
	 * of user input using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @param msgid Long UID of Discord message containing the user response.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_rerequest(AbstractBot bot, MessageID msgid);
	
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
	
}
