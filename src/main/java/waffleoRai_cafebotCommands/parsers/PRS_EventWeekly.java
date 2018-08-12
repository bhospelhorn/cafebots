package waffleoRai_cafebotCommands.parsers;

import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_EventMakeWeekly;
import waffleoRai_cafebotCommands.Commands.CMD_InsufficientArgs;
import waffleoRai_schedulebot.EventType;
import waffleoRai_schedulebot.Schedule;

public class PRS_EventWeekly implements Parser{
	
	private static String[] groupargs = {"everyone", "all", "group"};
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) 
	{
		//Debug message
		//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || DEBUG - Called! args...");
		//for (int i = 0; i < args.length; i++) System.err.println("\t" + args[i]);
		
		//Min args - 6
		if (args.length < 6){
			//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || DEBUG - Rejecting command [Insufficient Args]");
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.WEEKLY, event.getAuthor().getName(), event.getGuild().getIdLong(), event.getMessageIdLong());
		}
		String rawdow = args[1];
		String rawtime = args[2];
		String ename = args[3];
		String rchan = args[4];
		String tchan = args[5];
		
		int dow = Schedule.getDayOfWeek(rawdow);
		if (dow < 1) return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.WEEKLY, event.getAuthor().getName(), event.getGuild().getIdLong(), event.getMessageIdLong());
		//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || Day of Week Detected: " + dow);
		
		String[] time = rawtime.split(":");
		if (time.length != 2){
			//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || DEBUG - Rejecting command [Time arg formatted incorrectly]");
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.WEEKLY, event.getAuthor().getName(), event.getGuild().getIdLong(), event.getMessageIdLong());
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
			//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || DEBUG - Rejecting command [Could not parse time arg]");
			return new CMD_InsufficientArgs(event.getChannel().getIdLong(), EventType.WEEKLY, event.getAuthor().getName(), event.getGuild().getIdLong(), event.getMessageIdLong());
		}
		//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || Hour Detected: " + hr);
		//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || Minute Detected: " + min);
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
						//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || Target User Detected: " + args[i]);
						tusers.add(args[i]);
					}
				}
			}
		}
		
		CMD_EventMakeWeekly cmd = null;
		if (group)
		{
			cmd = new CMD_EventMakeWeekly(event.getChannel(), event.getMember(), dow, event.getMessageIdLong());
		}
		else
		{
			cmd = new CMD_EventMakeWeekly(event.getChannel(), event.getMember(), dow, tusers, event.getMessageIdLong());
		}
		//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || Event Name Detected: " + ename);
		cmd.setEventName(ename);
		//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || RChannel Name Detected: " + rchan);
		cmd.setRCHAN_name(rchan);
		//System.err.println(Thread.currentThread().getName() + " || PRS_EventWeekly.generateCommand || TChannel Name Detected: " + tchan);
		cmd.setTCHAN_name(tchan);
		cmd.setTime(hr, min);
		
		return cmd;
	}

}
