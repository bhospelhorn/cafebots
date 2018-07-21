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
 */

/**
 * A basic interface for executable bot commands.
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
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
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot);
	
	/**
	 * Execute the procedure that should be executed upon user cancellation
	 * using the referenced bot.
	 * @param bot Bot to execute this command with.
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_reject(AbstractBot bot);
	
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
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_rerequest(AbstractBot bot);
	
	/**
	 * Retrieve the long UID of the original message containing the user-issued
	 * text command the bot brain received.
	 * @return Long UID of command message, or -1 if there is none.
	 */
	public long getCommandMessageID();
	
}
