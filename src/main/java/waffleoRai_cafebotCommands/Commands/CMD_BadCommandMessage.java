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
 * An all-purpose command that simply tells the bot to send its default
 * "command was not understood" message to the requested channel.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_BadCommandMessage extends CommandAdapter {

	private MessageChannel channel;
	
	/**
	 * Construct a BadCommandMessage command by providing the channel to send
	 * the message to.
	 * @param ch Raw MessageChannel object from captured listener event - the channel
	 * the bad command was received on and the bot reply should be sent to.
	 */
	public CMD_BadCommandMessage(MessageChannel ch)
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
		bot.displayBadCommandMessage(channel.getIdLong());
		
	}

	@Override
	public String toString()
	{
		return "idontunderstand";
	}
	
}
