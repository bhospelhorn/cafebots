package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_PingMeGreetings extends CommandAdapter{
	
	//private Member req_user;
	private boolean setting;
	private MessageChannel channel;
	private String targetchannel; //Just ping to channel of cmd?
	
	public CMD_PingMeGreetings(boolean dir, Member user, MessageChannel ch, long cmdID, String chname)
	{
		//req_user = user;
		super.requestingUser = user;
		setting = dir;
		channel = ch;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
		targetchannel = chname;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		bot.setUserPingGreetings(channel.getIdLong(), super.requestingUser, setting, targetchannel);
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
	
}
