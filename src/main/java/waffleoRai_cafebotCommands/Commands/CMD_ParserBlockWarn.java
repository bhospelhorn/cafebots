package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 17, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * Command to send a message warning users attempting to command the bots
 * that the parser is blocked and their commands will be ignored.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_ParserBlockWarn extends CommandAdapter{

	private long channel;
	
	/**
	 * Construct a ParserBlockWarn command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 */
	public CMD_ParserBlockWarn(MessageChannel ch)
	{
		channel = ch.getIdLong();
	}
	
	@Override
	public long getChannelID()
	{
		return channel;
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
		bot.warnBlock(channel);
	}

	@Override
	public String toString()
	{
		return "parserblockwarn";
	}

}
