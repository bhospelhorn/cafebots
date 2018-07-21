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
 * @version 1.0.1
 * @since July 20, 2018
 */
public class PRS_SOR implements Parser{
	
	public static final String ALLON = "allon";
	public static final String ALLOFF = "alloff";

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMessageIdLong());
		
		if (args[1].equals(ALLOFF))
		{
			return new CMD_SOR(event.getChannel(), event.getMember(), false, event.getMessageIdLong());
		}
		else if (args[1].equals(ALLON))
		{
			return new CMD_SOR(event.getChannel(), event.getMember(), true, event.getMessageIdLong());
		}
		else
		{
			//Type Level
			if (args.length < 3) return new CMD_BadCommandMessage(event.getChannel(), event.getMessageIdLong());
			EventType t = EventType.getEventType(args[1]);
			if (t == null) return new CMD_BadCommandMessage(event.getChannel(), event.getMessageIdLong());
			int l = -1;
			try{l = Integer.parseInt(args[2]);}
			catch(NumberFormatException e){return new CMD_BadCommandMessage(event.getChannel(), event.getMessageIdLong());}
			return new CMD_SOR(event.getChannel(), event.getMember(), t, l, event.getMessageIdLong());
		}
		
	}

}
