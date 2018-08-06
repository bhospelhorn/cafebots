package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.Attendance;
import waffleoRai_schedulebot.CalendarEvent;

public class CMD_AttendEvent extends CommandAdapter{
	
	private MessageChannel channel;
	private Member member;
	private long eventID;
	
	private CalendarEvent event;
	
	public CMD_AttendEvent(MessageChannel c, Member m, long e, long cmdID)
	{
		channel = c;
		member = m;
		eventID = e;
		super.setCommandMessageID(cmdID);
		event = null;
	}
	
	@Override
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
	public void execute(AbstractBot bot) {
		if (event == null)
		{
			event = bot.retrieveEvent(eventID, member.getGuild().getIdLong());
			bot.redirectEventCommand(this, event.getType());
		}
		else
		{
			bot.RSVPEvent(channel.getIdLong(), member, event, Attendance.YES);
		}
	}
	
	@Override
	public String toString()
	{
		return "attend";
	}
	


}
