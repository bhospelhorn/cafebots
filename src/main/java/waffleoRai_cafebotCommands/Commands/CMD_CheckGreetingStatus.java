package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_CheckGreetingStatus extends CommandAdapter{
	
	private MessageChannel channel;
	private Member member;
	private boolean farewell;
	private boolean ping;
	
	public CMD_CheckGreetingStatus(MessageChannel c, Member m, boolean f, boolean p, long cmdID)
	{
		channel = c;
		member = m;
		farewell = f;
		ping = p;
		MessageID cmdmsg = new MessageID(cmdID, c.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	public long getChannelID()
	{
		return channel.getIdLong();
	}

	public long getUserID()
	{
		return member.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) 
	{
		if (ping)
		{
			bot.checkGreetingPingStatus(channel.getIdLong(), member, farewell);
		}
		else
		{
			bot.checkGreetingStatus(channel.getIdLong(), member.getGuild(), farewell);
		}
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "checkg";
	}
	
	public long getGuildID()
	{
		return member.getGuild().getIdLong();
	}
	

}
