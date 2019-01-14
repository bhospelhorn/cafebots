package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 27, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID notes
 * 1.1.0 -> 1.1.1 | August 11, 2018
 * 	MessageID update
 * 
 */


/**
 * A Command for requesting the channel set for delivering birthday wishes or 
 * guild greetings.
 * <br><br><b>Standard Commands:</b>
 * <br>qchan birthday
 * <br>qchan greeting
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since January 14, 2019
 */
public class CMD_qChan extends CommandAdapter{
	
	private MessageChannel cmd_channel;
	//private Guild guild;
	private boolean bday;
	
	/**
	 * Construct a qChan (Query Channel) command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param g Guild housing the channel the command was issued on.
	 * @param birthday
	 * <br>True - If user wants to check the birthday channel
	 * <br>False - If user wants to check the greeting channel
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_qChan(MessageChannel ch, Member m, boolean birthday, long cmdID)
	{
		cmd_channel = ch;
		//guild = g;
		super.requestingUser = m;
		bday = birthday;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	@Override
	public long getChannelID()
	{
		return cmd_channel.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		if (bday) bot.displayBirthdayChannel(getGuild(), cmd_channel.getIdLong());
		else bot.displayGreetingChannel(getGuild(), cmd_channel.getIdLong());
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "qchan";
	}

}
