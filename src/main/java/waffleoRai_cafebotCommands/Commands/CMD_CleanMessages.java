package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 24, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 */

/**
 * A Command for requesting deletion of messages from a channel.
 * <br><br><b>Standard Commands:</b>
 * <br>cleanme
 * <br>cleanmeday
 * <br><br><i>Admin Only:</i>
 * <br>cleanday
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
 */
public class CMD_CleanMessages extends CommandAdapter{

	private MessageChannel channel;
	private Member user;
	private boolean userOnly;
	private boolean dayOnly;

	/**
	 * Construct a new CleanMessages command.
	 * @param ch Channel command was sent to. Bot should also clean messages from this channel
	 * and use this channel to communicate with the commanding user.
	 * @param u User sending the command as a JDA Member object.
	 * @param useronly Whether messages deleted should only include messages sent by the requesting user.
	 * @param dayonly Whether messages deleted should only include messages sent in the last 24 hours.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_CleanMessages(MessageChannel ch, Member u, boolean useronly, boolean dayonly, long cmdID)
	{
		channel = ch;
		user = u;
		userOnly = useronly;
		dayOnly = dayonly;
		super.setCommandMessageID(cmdID);
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		if (dayOnly)
		{
			if (userOnly) bot.cleanChannelMessages_allUserDay_prompt(channel.getIdLong(), user, this);
			else bot.cleanChannelMessages_allDay_prompt(channel.getIdLong(), user, this);
		}
		else
		{
			if (userOnly) bot.cleanChannelMessages_allUser_prompt(channel.getIdLong(), user, this);
		}
		super.cleanAfterMyself(bot);
		
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	public long getUserID()
	{
		return user.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot, long msgid) {
		if (dayOnly)
		{
			if (userOnly) bot.cleanChannelMessages_allUserDay(channel.getIdLong(), user);
			else bot.cleanChannelMessages_allDay(channel.getIdLong(), user);
		}
		else
		{
			if (userOnly) bot.cleanChannelMessages_allUser(channel.getIdLong(), user);
		}
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_reject(AbstractBot bot, long msgid) {
		bot.displayGeneralCancel(getChannelID(), user.getUser().getName());
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

	public long getGuildID()
	{
		return user.getGuild().getIdLong();
	}
	
}
