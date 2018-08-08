package waffleoRai_cafebotCommands.parsers;

import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeOnetime;
import waffleoRai_cafebotCommands.Commands.CMD_InsufficientArgs;
import waffleoRai_schedulebot.EventType;

public class PRS_EventOnetime implements Parser{
	
	private static String[] groupargs = {"everyone", "all", "group"};
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) 
	{
		if (args.length < 8){
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.ONETIME, event.getAuthor().getName(), event.getGuild().getIdLong(), event.getMessageIdLong());
		}
		
		String rawyear = args[1];
		String rawmonth = args[2];
		String rawday = args[3];

		String rawtime = args[4];
		String ename = args[5];
		String rchan = args[6];
		String tchan = args[7];
		
		int year = 2015;
		int month = 1;
		int day = 1;
		try
		{
			year = Integer.parseInt(rawyear);
			month = Integer.parseInt(rawmonth);
			day = Integer.parseInt(rawday);
		}
		catch (NumberFormatException e)
		{
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.ONETIME, event.getAuthor().getName(), event.getGuild().getIdLong(), event.getMessageIdLong());
		}
		
		String[] time = rawtime.split(":");
		if (time.length != 2){
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.ONETIME, event.getAuthor().getName(), event.getGuild().getIdLong(), event.getMessageIdLong());
		}
		int hr = 0;
		int min = 0;
		try
		{
			hr = Integer.parseInt(time[0]);
			min = Integer.parseInt(time[1]);
		}
		catch (NumberFormatException e)
		{
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.ONETIME, event.getAuthor().getName(), event.getGuild().getIdLong(), event.getMessageIdLong());
		}

		boolean group = false;
		List<String> tusers = null;
		if (args.length > 8)
		{
			//Check argument 8 to see if it's a group cue
			for (String a : groupargs)
			{
				if (a.equalsIgnoreCase(args[8]))
				{
					group = true;
					break;
				}
			}
			if(!group)
			{
				tusers = new LinkedList<String>();
				if (args.length > 8)
				{
					for (int i = 8; i < args.length; i++)
					{
						tusers.add(args[i]);
					}
				}
			}
		}
		
		CMD_EventMakeOnetime cmd = null;
		if (group)
		{
			cmd = new CMD_EventMakeOnetime(event.getChannel(), event.getMember(), event.getMessageIdLong());
		}
		else
		{
			cmd = new CMD_EventMakeOnetime(event.getChannel(), event.getMember(), tusers, event.getMessageIdLong());
		}
		cmd.setEventName(ename);
		cmd.setRCHAN_name(rchan);
		cmd.setTCHAN_name(tchan);
		cmd.setTime(hr, min);
		cmd.setDate(year, month, day);
		
		return cmd;
	}

}
