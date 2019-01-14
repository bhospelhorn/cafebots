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
 * 	Added command message ID note
 * 1.1.0 -> 1.1.1 | August 7, 2018
 * 	Added more command cleaning stuff
 * 1.1.1 -> 1.1.2 | August 11, 2018
 * 	MessageID
 */

/**
 * A Command for changing the guild (server) greeting/birthday channels.
 * <br><br><b>Standard Commands:</b>
 * <br>chchan birthday [channelName]
 * <br>chchan greeting [channelName]
 * <br><i>These commands are for guild admins only.</i>
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since January 14, 2019
 */
public class CMD_ChChan extends CommandAdapter{

	private MessageChannel cmd_channel;
	//private Member user;
	private String trg_channel;
	private boolean bday;
	
	/**
	 * Construct a ChangeChannel (ChChan) command.
	 * @param ch Channel command was sent through and bot should send messages regarding
	 * the success or failure of this command to.
	 * @param u User sending the command as a JDA Member object.
	 * @param tchan Name of channel to set as the birthday or greeting channel.
	 * @param birthday 
	 * <br>True - If user wants to change the birthday channel
	 * <br>False - If user wants to change the greeting channel
	 * @param cmdID ID of the message the command was sent in.
	 */
	public CMD_ChChan(MessageChannel ch, Member u, String tchan, boolean birthday, long cmdID)
	{
		cmd_channel = ch;
		//user = u;
		super.requestingUser = u;
		trg_channel = tchan;
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
		if (bday) bot.setBirthdayChannel(cmd_channel.getIdLong(), trg_channel, super.requestingUser);
		else bot.setGreetingChannel(cmd_channel.getIdLong(), trg_channel, super.requestingUser);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "chchan";
	}

}
