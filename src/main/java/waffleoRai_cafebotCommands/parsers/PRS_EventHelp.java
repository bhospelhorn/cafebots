package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_Eventhelp;

public class PRS_EventHelp implements Parser{
	
	public static final String ARGHELP = "arghelp";
	public static final String SORHELP = "sorhelp";

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2 || args[1] == null) return new CMD_BadCommandMessage(event.getChannel());
		
		if (args[1].equals(ARGHELP)) return new CMD_Eventhelp(event.getChannel(), event.getAuthor(), false);
		if (args[1].equals(SORHELP)) return new CMD_Eventhelp(event.getChannel(), event.getAuthor(), true);
		
		return new CMD_BadCommandMessage(event.getChannel());
	}

}
