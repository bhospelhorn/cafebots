package waffleoRai_cafebotCommands.Commands;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.CalendarEvent;
import waffleoRai_schedulebot.EventAdapter;

//Internal only

public class CMD_NotifyCancellation extends CommandAdapter{

	//private long channelID;
	private CalendarEvent event;
	private long guildID;
	private boolean instanceOnly;
	
 	public CMD_NotifyCancellation(CalendarEvent e, boolean instance, long guild)
	{
		//channelID = channel;
		guildID = guild;
		event = e;
		instanceOnly = instance;
	}
	
	public long getChannelID()
	{
		if (event instanceof EventAdapter)
		{
			EventAdapter ea = (EventAdapter) event;
			return ea.getTargetChannel();
		}
		return -1;
	}

	public long getUserID()
	{
		return event.getRequestingUser();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		bot.notifyCancellation(event, instanceOnly, guildID);
	}

	@Override
	public String toString()
	{
		return "notifycancel";
	}
	
	public long getGuildID()
	{
		return guildID;
	}
	
}
