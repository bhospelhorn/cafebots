package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_PingMeGreetings;

public class PRS_PingMeGreetings implements Parser{
	
	public static final String OPTION_ON = "on";
	public static final String OPTION_OFF = "off";
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		boolean on = false;
		if (args[1].equals(OPTION_ON)) on = true;
		else if (args[1].equals(OPTION_OFF)) on = false;
		else return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		
		String chname = event.getChannel().getName();
		if (args.length > 2)
		{
			if (args[2] != null && !args[2].isEmpty())
			{
				if (args[2].charAt(0) == '#') args[2] = args[2].substring(1);
				chname = args[2];	
			}
		}
		return new CMD_PingMeGreetings(on, event.getMember(), event.getChannel(), event.getMessageIdLong(), chname);
	}

}
