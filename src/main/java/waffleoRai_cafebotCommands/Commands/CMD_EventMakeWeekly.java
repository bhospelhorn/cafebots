package waffleoRai_cafebotCommands.Commands;

import java.util.List;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

public class CMD_EventMakeWeekly extends CommandAdapter{
	
	private MessageChannel comChannel;
	
	private Member requestingUser;
	
	private int DOW;
	private String eventName;
	private String rChannel;
	private long rChannel_resolved;
	private String tChannel;
	private long tChannel_resolved;
	private List<String> targetUsers;
	
	private int hour;
	private int minutes;
	
	public CMD_EventMakeWeekly(MessageChannel ch, Member req, int dayOfWeek, List<String> targets, long cmdID)
	{
		comChannel = ch;
		requestingUser = req;
		DOW = dayOfWeek;
		targetUsers = targets;
		eventName = null;
		rChannel = null;
		tChannel = null;
		hour = 12;
		minutes = 0;
		super.setCommandMessageID(cmdID);
	}
	
	public MessageChannel getCommandChannel()
	{
		return comChannel;
	}
	
	public Member getRequestingUser()
	{
		return requestingUser;
	}
	
	public int getDayOfWeek()
	{
		return DOW;
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
	
	public long getUserID()
	{
		return requestingUser.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) 
	{
		bot.makeWeeklyEvent_prompt(this);
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_confirm(AbstractBot bot) {
		bot.makeWeeklyEvent_complete(this, true);
	}

	@Override
	public void execute_reject(AbstractBot bot) {
		bot.makeWeeklyEvent_complete(this, false);
	}

	@Override
	public String toString()
	{
		return "weekly";
	}

}
