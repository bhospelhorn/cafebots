package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 27, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 */

/**
 * A Command for changing the guild (server) greeting/birthday channels.
 * <br><br><b>Standard Commands:</b>
 * <br>chchan birthday [channelName]
 * <br>chchan greeting [channelName]
 * <br><i>These commands are for guild admins only.</i>
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
 */
public class CMD_ChChan extends CommandAdapter{

	private MessageChannel cmd_channel;
	private Member user;
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
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_ChChan(MessageChannel ch, Member u, String tchan, boolean birthday, long cmdID)
	{
		cmd_channel = ch;
		user = u;
		trg_channel = tchan;
		bday = birthday;
		super.setCommandMessageID(cmdID);
	}
	
	@Override
	public long getChannelID()
	{
		return cmd_channel.getIdLong();
	}
	
	public long getUserID()
	{
		return user.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		if (bday) bot.setBirthdayChannel(cmd_channel.getIdLong(), trg_channel, user);
		else bot.setGreetingChannel(cmd_channel.getIdLong(), trg_channel, user);
	}
	
	@Override
	public String toString()
	{
		return "chchan";
	}

}
