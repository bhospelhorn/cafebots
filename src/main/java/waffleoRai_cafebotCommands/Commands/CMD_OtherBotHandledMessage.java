package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 24, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * A command for displaying a message informing a requesting user that anotehr bot
 * is already handling their command.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_OtherBotHandledMessage extends CommandAdapter{

	private MessageChannel channel;
	
	/**
	 * Construct a new OtherBotHandledMessage command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 */
	public CMD_OtherBotHandledMessage(MessageChannel ch)
	{
		channel = ch;
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	public long getUserID()
	{
		return -1;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.displayCommandHandledMessage(channel.getIdLong());
		
	}

	@Override
	public String toString()
	{
		return "theygotit";
	}
	
}
