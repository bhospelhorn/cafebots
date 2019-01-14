package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
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
 * 1.1.0 -> 1.2.0 | August 11, 2018
 * 	MessageID update
 */

/**
 * A Command for requesting deletion of messages from a channel.
 * <br><br><b>Standard Commands:</b>
 * <br>cleanme
 * <br>cleanmeday
 * <br><br><i>Admin Only:</i>
 * <br>cleanday
 * @author Blythe Hospelhorn
 * @version 1.3.0
 * @since January 14, 2019
 */
public class CMD_CleanMessages extends CommandAdapter{

	private MessageChannel channel;
	//private Member user;
	private boolean userOnly;
	private boolean dayOnly;

	/**
	 * Construct a new CleanMessages command.
	 * @param ch Channel command was sent to. Bot should also clean messages from this channel
	 * and use this channel to communicate with the commanding user.
	 * @param u User sending the command as a JDA Member object.
	 * @param useronly Whether messages deleted should only include messages sent by the requesting user.
	 * @param dayonly Whether messages deleted should only include messages sent in the last 24 hours.
	 * @param cmdID ID of the message the command was sent in.
	 */
	public CMD_CleanMessages(MessageChannel ch, Member u, boolean useronly, boolean dayonly, long cmdID)
	{
		channel = ch;
		//user = u;
		super.requestingUser = u;
		userOnly = useronly;
		dayOnly = dayonly;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		if (dayOnly)
		{
			if (userOnly) bot.cleanChannelMessages_allUserDay_prompt(channel.getIdLong(), super.requestingUser, this);
			else bot.cleanChannelMessages_allDay_prompt(channel.getIdLong(), super.requestingUser, this);
		}
		else
		{
			if (userOnly) bot.cleanChannelMessages_allUser_prompt(channel.getIdLong(), super.requestingUser, this);
		}
		super.cleanAfterMyself(bot);
		
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot, MessageID msgid) throws InterruptedException {
		if (dayOnly)
		{
			if (userOnly) bot.cleanChannelMessages_allUserDay(channel.getIdLong(), super.requestingUser);
			else bot.cleanChannelMessages_allDay(channel.getIdLong(), super.requestingUser);
		}
		else
		{
			if (userOnly) bot.cleanChannelMessages_allUser(channel.getIdLong(), super.requestingUser);
		}
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_reject(AbstractBot bot, MessageID msgid) throws InterruptedException {
		bot.displayGeneralCancel(getChannelID(), super.requestingUser);
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}

	@Override
	public String toString()
	{
		if (!userOnly && dayOnly) return "cleanday";
		else if (userOnly && dayOnly) return "cleanmeday";
		else if (userOnly && !dayOnly) return "cleanme";
		return "";
	}

}
