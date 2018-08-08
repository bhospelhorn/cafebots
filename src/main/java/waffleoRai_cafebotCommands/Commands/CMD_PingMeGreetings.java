package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_PingMeGreetings extends CommandAdapter{
	
	private Member req_user;
	private boolean setting;
	private MessageChannel channel;
	//private MessageChannel targetchannel; //Just ping to channel of cmd?
	
	public CMD_PingMeGreetings(boolean dir, Member user, MessageChannel ch, long cmdID)
	{
		req_user = user;
		setting = dir;
		channel = ch;
		super.setCommandMessageID(cmdID);
	}
	
	public long getUserID()
	{
		return req_user.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) 
	{
		bot.setUserPingGreetings(channel.getIdLong(), req_user, setting);
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "pingGreetings";
	}

	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	public long getGuildID()
	{
		return req_user.getGuild().getIdLong();
	}

}
