package waffleoRai_cafebotCommands.Commands;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.EventType;

/*
 * UPDATES
 * 
 * Creation | June 19, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * A command for sending a message informing the user that the arguments for an event
 * creation command could not be parsed correctly.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_InsufficientArgs extends CommandAdapter{
	
	private long chid;
	private EventType type;

	/**
	 * Construct an InsufficientArgs command.
	 * @param channelID Discord long UID of channel command was issued on and reply should be sent to.
	 * @param et EventType of event creation attempt.
	 */
	public CMD_InsufficientArgs(long channelID, EventType et)
	{
		chid = channelID;
		type = et;
	}

	/**
	 * Select the function to execute based on the set event type.
	 * @param bot Bot to execute function on.
	 */
	private void pickFunct(AbstractBot bot)
	{
		switch(type)
		{
		case BIRTHDAY:
			bot.insufficientArgsMessage_birthday(chid);
			break;
		case BIWEEKLY:
			break;
		case DEADLINE:
			break;
		case MONTHLYA:
			break;
		case MONTHLYB:
			break;
		case ONETIME:
			break;
		case WEEKLY:
			break;
		default:
			break;
		}
	}
	
	@Override
	public long getChannelID()
	{
		return chid;
	}
	
	public long getUserID()
	{
		return -1;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) 
	{
		pickFunct(bot);
	}

	@Override
	public String toString()
	{
		return "insufargs";
	}

	
}
