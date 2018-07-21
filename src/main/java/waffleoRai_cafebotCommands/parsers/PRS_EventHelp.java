package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_Eventhelp;

/**
 * Event help (eventhelp) command parser
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since July 20, 2018
 */
public class PRS_EventHelp implements Parser{
	
	/**
	 * Event help command type to get help on event creation arguments.
	 */
	public static final String ARGHELP = "arghelp";
	
	/**
	 * Event help command type to get help on the sor (switch on/off reminders) command.
	 */
	public static final String SORHELP = "sorhelp";

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2 || args[1] == null) return new CMD_BadCommandMessage(event.getChannel(), event.getMessageIdLong());
		
		if (args[1].equals(ARGHELP)) return new CMD_Eventhelp(event.getChannel(), event.getAuthor(), false, event.getMessageIdLong());
		if (args[1].equals(SORHELP)) return new CMD_Eventhelp(event.getChannel(), event.getAuthor(), true, event.getMessageIdLong());
		
		return new CMD_BadCommandMessage(event.getChannel(), event.getMessageIdLong());
	}

}
