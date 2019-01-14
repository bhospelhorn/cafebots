package waffleoRai_cafebotCommands.Commands;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.EventAdapter;

public class CMD_IssueReminder extends CommandAdapter{

	private EventAdapter event;
	private int reminder_level;
	private long guildID;
	
	public CMD_IssueReminder(EventAdapter e, int lv, long gid)
	{
		event = e;
		reminder_level = lv;
		guildID = gid;
	}

	@Override
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		bot.issueEventReminder(event, reminder_level, guildID);
	}
	
	@Override
	public long getChannelID()
	{
		return event.getRequesterChannel();
	}

	@Override
	public long getUserID() {
		return event.getRequestingUser();
	}
	
	public long getGuildID()
	{
		return guildID;
	}
	
	public String toString()
	{
		return "issueReminder_event" + Long.toUnsignedString(event.getEventID());
	}
	
}
