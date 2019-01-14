package waffleoRai_cafebotCommands.Commands;

import java.util.List;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_EventMakeOnetime extends CommandAdapter{
	
	private MessageChannel comChannel;
	
	//private Member requestingUser;
	
	private String eventName;
	private String rChannel;
	private long rChannel_resolved;
	private String tChannel;
	private long tChannel_resolved;
	private List<String> targetUsers;
	private boolean groupEvent;
	
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minutes;
	
	public CMD_EventMakeOnetime(MessageChannel ch, Member req, List<String> targets, long cmdID)
	{
		//System.err.println(Thread.currentThread().getName() + " || CMD_EventMakeBiweekly.<init> || DEBUG - Called!");
		comChannel = ch;
		requestingUser = req;
		targetUsers = targets;
		eventName = null;
		rChannel = null;
		tChannel = null;
		hour = 12;
		minutes = 0;
		groupEvent = false;
		year = 2015;
		month = 1;
		day = 1;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	public CMD_EventMakeOnetime(MessageChannel ch, Member req, long cmdID)
	{
		//System.err.println(Thread.currentThread().getName() + " || CMD_EventMakeBiweekly.<init> || DEBUG - Called!");
		comChannel = ch;
		requestingUser = req;
		targetUsers = null;
		eventName = null;
		rChannel = null;
		tChannel = null;
		hour = 12;
		minutes = 0;
		groupEvent = true;
		year = 2015;
		month = 1;
		day = 1;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	public MessageChannel getCommandChannel()
	{
		return comChannel;
	}
	
	public Member getRequestingUser()
	{
		return requestingUser;
	}
	
	public int getYear()
	{
		return year;
	}
	
	public int getMonth()
	{
		return month;
	}
	
	public int getDay()
	{
		return day;
	}
	
	public int getHour()
	{
		return hour;
	}
	
	public int getMinute()
	{
		return minutes;
	}
	
	public String getEventName()
	{
		return eventName;
	}
	
	public String getRequesterChannelName()
	{
		return rChannel;
	}
	
	public String getTargetChannelName()
	{
		return tChannel;
	}
	
	public boolean isGroupEvent()
	{
		return groupEvent;
	}
	
	public void resolveRequesterChannel(long cid)
	{
		rChannel_resolved = cid;
	}
	
	public void resolveTargetChannel(long cid)
	{
		tChannel_resolved = cid;
	}
	
	public long getRequesterChannelID()
	{
		return rChannel_resolved;
	}
	
	public long getTargetChannelID()
	{
		return tChannel_resolved;
	}
	
	public List<String> getTargetUsers()
	{
		return targetUsers;
	}
	
	public void setEventName(String s)
	{
		eventName = s;
	}
	
	public void setRCHAN_name(String s)
	{
		rChannel = s;
	}
	
	public void setTCHAN_name(String s)
	{
		tChannel = s;
	}
	
	public void setDate(int y, int m, int d)
	{
		year = y;
		month = m;
		day = d;
	}
	
	public void setTime(int h, int m)
	{
		hour = h;
		minutes = m;
	}
	
	@Override
 	public long getChannelID()
	{
		return comChannel.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		bot.makeOnetimeEvent_prompt(this);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot, MessageID msgid) throws InterruptedException {
		bot.makeOnetimeEvent_complete(this, true);
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}

	@Override
	public void execute_reject(AbstractBot bot, MessageID msgid) throws InterruptedException {
		bot.makeOnetimeEvent_complete(this, false);
		bot.queueCommandMessageForCleaning(msgid, getGuildID());
	}

	@Override
	public String toString()
	{
		return "onetime";
	}

	public String getUsername()
	{
		return requestingUser.getUser().getName();
	}

	
}
