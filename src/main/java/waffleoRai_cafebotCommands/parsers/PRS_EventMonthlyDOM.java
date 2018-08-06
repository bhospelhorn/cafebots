package waffleoRai_cafebotCommands.parsers;

import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeMonthlyDOM;
import waffleoRai_cafebotCommands.Commands.CMD_InsufficientArgs;
import waffleoRai_schedulebot.EventType;

public class PRS_EventMonthlyDOM implements Parser{
	
	private static String[] groupargs = {"everyone", "all", "group"};
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) 
	{
		if (args.length < 6){
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.MONTHLYA, event.getAuthor().getName(), event.getMessageIdLong());
		}
		String rawday = args[1];
		String rawtime = args[2];
		String ename = args[3];
		String rchan = args[4];
		String tchan = args[5];
		
		if (rawday == null) return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.MONTHLYA, event.getAuthor().getName(), event.getMessageIdLong());
		
		int day = 0;
		try
		{
			day = Integer.parseInt(rawday);
		}
		catch (NumberFormatException e)
		{
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.MONTHLYA, event.getAuthor().getName(), event.getMessageIdLong());
		}
		
		String[] time = rawtime.split(":");
		if (time.length != 2){
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.MONTHLYA, event.getAuthor().getName(), event.getMessageIdLong());
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
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.MONTHLYA, event.getAuthor().getName(), event.getMessageIdLong());
		}

		boolean group = false;
		List<String> tusers = null;
		if (args.length > 6)
		{
			//Check argument 6 to see if it's a group cue
			for (String a : groupargs)
			{
				if (a.equalsIgnoreCase(args[6]))
				{
					group = true;
					break;
				}
			}
			if(!group)
			{
				tusers = new LinkedList<String>();
				if (args.length > 6)
				{
					for (int i = 6; i < args.length; i++)
					{
						tusers.add(args[i]);
					}
				}
			}
		}
		
		CMD_EventMakeMonthlyDOM cmd = null;
		if (group)
		{
			cmd = new CMD_EventMakeMonthlyDOM(event.getChannel(), event.getMember(), day, event.getMessageIdLong());
		}
		else
		{
			cmd = new CMD_EventMakeMonthlyDOM(event.getChannel(), event.getMember(), day, tusers, event.getMessageIdLong());
		}
		cmd.setEventName(ename);
		cmd.setRCHAN_name(rchan);
		cmd.setTCHAN_name(tchan);
		cmd.setTime(hr, min);
		
		return cmd;
	}

}
