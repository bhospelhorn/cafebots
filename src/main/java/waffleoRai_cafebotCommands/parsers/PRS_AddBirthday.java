package waffleoRai_cafebotCommands.parsers;

import java.util.GregorianCalendar;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_AddBirthday;
import waffleoRai_cafebotCommands.Commands.CMD_InsufficientArgs;
import waffleoRai_schedulebot.EventType;

/**
 * Parser implementation for the add birthday command.
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since July 20, 2018
 *
 */
public class PRS_AddBirthday implements Parser{

	public Command generateCommand(String[] args, MessageReceivedEvent event)
	{
		//Need: month, day, user, channel ID
		long chid = event.getChannel().getIdLong();
		if (args.length < 3){
			return new CMD_InsufficientArgs(chid, EventType.BIRTHDAY, event.getAuthor().getName(), event.getMessageIdLong());
		}
		try
		{
			int month = Integer.parseInt(args[1]) - 1;
			int day = Integer.parseInt(args[2]);
			Member user = event.getMember();
			return new CMD_AddBirthday(month, day, user, chid, event.getMessageIdLong());
		}
		catch (NumberFormatException e)
		{
			GregorianCalendar gc = new GregorianCalendar();
			System.out.println(Thread.currentThread().getName() + " || PRS_AddBirthday.generateCommand || " + " Parser failed: Could not read required argument(s) | " + FileBuffer.formatTimeAmerican(gc));
			return new CMD_InsufficientArgs(chid, EventType.BIRTHDAY, event.getAuthor().getName(), event.getMessageIdLong());
		}
	}

}
