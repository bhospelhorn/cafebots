package waffleoRai_cafebotCommands;

import java.util.HashMap;
import java.util.Map;

import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | August 7, 2018
 * 	Added message ID
 * 
 * 1.1.0 -> 1.1.1 | August 11, 2018
 * 	MessageID update (long -> MessageID)
 * 
 * 1.1.1 -> 1.2.0 | November 1, 2018
 * 	Added framework for interrupt handling
 */

/**
 * An object containing information on a user response to a bot command prompt.
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since November 1, 2018
 */
public class Response {
	
	public static final int RESPONSE_YES = 1;
	public static final int RESPONSE_NO = 0;
	public static final int RESPONSE_TIMEOUT = 2;
	public static final int RESPONSE_INVALID = -1;
	
	public static final String[] DEFAULT_ENGLISH_YES_STRINGS = {"yes", "y", "yeah", "sure"};
	public static final String[] DEFAULT_ENGLISH_NO_STRINGS = {"no", "n", "nah", "nope"};
	
	private Command cmd;
	private int response;
	private MessageID messageID;
	
	private static Map<String, Integer> rMap; //Map of interpretable strings to response enums
	
	/**
	 * Construct a Response to Command c.
	 * @param c Command to respond to.
	 * @param r Response to command (see Response class constants).
	 * @param messageUID UID of message sent in Discord containing response
	 */
 	public Response(Command c, int r, MessageID messageUID)
	{
		cmd = c;
		response = r;
		messageID = messageUID;
	}
	
 	/**
 	 * Execute the appropriate response using the given bot.
 	 * @param bot Bot to execute command response.
 	 * @throws InterruptedException If the command execution is interrupted.
 	 * @throws NullPointerException If bot is null.
 	 */
	public void execute(AbstractBot bot) throws InterruptedException
	{
		switch(response)
		{
		case RESPONSE_YES:
			cmd.execute_confirm(bot, messageID);
			break;
		case RESPONSE_NO:
			cmd.execute_reject(bot, messageID);
			break;
		case RESPONSE_TIMEOUT:
			cmd.execute_timeout(bot);
			break;
		case RESPONSE_INVALID:
			cmd.execute_rerequest(bot, messageID);
			break;
		}
	}
	
	/**
	 * Build a map of potential user response strings to program interpretable responses.
	 * @param yesStrings Array of strings to associate with a "yes" response.
	 * @param noStrings Array of strings to associate with a "no" response.
	 */
	public static void buildResponseMap(String[] yesStrings, String[] noStrings)
	{
		rMap = new HashMap<String, Integer>();
		if(yesStrings != null)
		{
			for (String y : yesStrings) rMap.put(y.toLowerCase(), RESPONSE_YES);
		}
		if(noStrings != null)
		{
			for (String n : noStrings) rMap.put(n.toLowerCase(), RESPONSE_NO);
		}
	}
	
	/**
	 * Interpret a message sent by a human user as a potential response to a bot prompt.
	 * @param s The user message string.
	 * @return <code>Response.RESPONSE_YES</code>, <code>Response.RESPONSE_NO</code>, 
	 * or <code>Response.RESPONSE_INVALID</code>
	 */
	public static int interpretResponse(String s)
	{
		if (rMap == null){
			buildResponseMap(DEFAULT_ENGLISH_YES_STRINGS, DEFAULT_ENGLISH_NO_STRINGS);
			if (rMap == null) return RESPONSE_INVALID;
		}
		Integer i = rMap.get(s.toLowerCase());
		if (i == null) return RESPONSE_INVALID;
		return i;
	}
	
	public MessageID getMessageID()
	{
		return messageID;
	}
	
	/** If the execution of this response throws an InterruptedException, this method
	 * tells an bot execution thread exception handler whether or not the response should
	 * be re-queued and execution re-attempted from the beginning.
	 * @return True if response should be pushed back to the top of the queue if interrupted.
	 * False if it should be tossed.
	 */
	public boolean requeueIfInterrupted()
	{
		return cmd.requeueIfInterrupted();
	}
}
