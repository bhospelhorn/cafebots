package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_ChChan;

public class PRS_ChChan implements Parser{

	public static final String BIRTHDAY = "birthday";
	public static final String GREETING = "greeting";
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 3) return new CMD_BadCommandMessage(event.getChannel());
		if (args[1].equals(BIRTHDAY)) return new CMD_ChChan(event.getChannel(), event.getMember(), args[2], true);
		else if (args[1].equals(GREETING)) return new CMD_ChChan(event.getChannel(), event.getMember(), args[2], false);
		
		return new CMD_BadCommandMessage(event.getChannel());
	}

}
