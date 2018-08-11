package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 24, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 1.1.0 -> 1.2.0 | August 7, 2018
 * 	Command cleanup stuff
 * 1.2.0 -> 1.3.0 | August 11, 2018
 * 	Command cleanup stuff
 */

/**
 * An all-purpose command that simply tells the bot to send its default
 * "command was not understood" message to the requested channel.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.3.0
 * @since August 11, 2018
 */
public class CMD_BadCommandMessage extends CommandAdapter {

	private MessageChannel channel;
	private Guild guild;

	/**
	 * Construct a BadCommandMessage command by providing the channel to send
	 * the message to.
	 * @param ch Raw MessageChannel object from captured listener event - the channel
	 * the bad command was received on and the bot reply should be sent to.
	 * @param cmdID ID of the message the command was sent in.
	 */
	public CMD_BadCommandMessage(MessageChannel ch, Guild g, long cmdID)
	{
		channel = ch;
		guild = g;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
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
		cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "idontunderstand";
	}
	
	public long getGuildID()
	{
		return guild.getIdLong();
	}
	
}
