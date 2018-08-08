package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_SOR;
import waffleoRai_schedulebot.EventType;

/**
 * Switch on/off reminder(s) (sor) parser
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since August 5, 2018
 */
public class PRS_SOR implements Parser{
	
	public static final String ALLON = "allon";
	public static final String ALLOFF = "alloff";
	public static final String DEFAULT = "defo";
	public static final String DEFAULT2 = "default";
	public static final String VIEW = "view";

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());
		
		if (args[1].equals(ALLOFF))
		{
			return new CMD_SOR(event.getChannel(), event.getMember(), false, false, event.getMessageIdLong());
		}
		else if (args[1].equals(ALLON))
		{
			return new CMD_SOR(event.getChannel(), event.getMember(), true, false, event.getMessageIdLong());
		}
		else if (args[1].equals(DEFAULT) || args[1].equals(DEFAULT2))
		{
			return new CMD_SOR(event.getChannel(), event.getMember(), true, true, event.getMessageIdLong());
		}
		else if (args[1].equals(VIEW))
		{
			if (args.length < 3) return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());
			EventType t = EventType.getEventType(args[2]);
			return new CMD_SOR(event.getChannel(), t, event.getMessageIdLong());
		}
		else
		{
			//Type Level
			if (args.length < 3) return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());
			EventType t = EventType.getEventType(args[1]);
			if (t == null) return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());
			int l = -1;
			try{l = Integer.parseInt(args[2]);}
			catch(NumberFormatException e){return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());}
			return new CMD_SOR(event.getChannel(), event.getMember(), t, l, event.getMessageIdLong());
		}
		
	}

}
