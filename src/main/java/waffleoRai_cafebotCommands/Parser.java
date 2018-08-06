package waffleoRai_cafebotCommands;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.parsers.PRS_AddBirthday;
import waffleoRai_cafebotCommands.parsers.PRS_AttendEvent;
import waffleoRai_cafebotCommands.parsers.PRS_CancelEvent;
import waffleoRai_cafebotCommands.parsers.PRS_ChChan;
import waffleoRai_cafebotCommands.parsers.PRS_CheckGreetingStatus;
import waffleoRai_cafebotCommands.parsers.PRS_CheckRSVP;
import waffleoRai_cafebotCommands.parsers.PRS_CleanMessages_1;
import waffleoRai_cafebotCommands.parsers.PRS_CleanMessages_2;
import waffleoRai_cafebotCommands.parsers.PRS_CleanMessages_3;
import waffleoRai_cafebotCommands.parsers.PRS_DeclineEvent;
import waffleoRai_cafebotCommands.parsers.PRS_EventBiweekly;
import waffleoRai_cafebotCommands.parsers.PRS_EventHelp;
import waffleoRai_cafebotCommands.parsers.PRS_EventMonthlyDOM;
import waffleoRai_cafebotCommands.parsers.PRS_EventMonthlyDOW;
import waffleoRai_cafebotCommands.parsers.PRS_EventOnetime;
import waffleoRai_cafebotCommands.parsers.PRS_EventWeekly;
import waffleoRai_cafebotCommands.parsers.PRS_GetTZ;
import waffleoRai_cafebotCommands.parsers.PRS_Help;
import waffleoRai_cafebotCommands.parsers.PRS_PingMeFarewells;
import waffleoRai_cafebotCommands.parsers.PRS_PingMeGreetings;
import waffleoRai_cafebotCommands.parsers.PRS_SOR;
import waffleoRai_cafebotCommands.parsers.PRS_SaySomething;
import waffleoRai_cafebotCommands.parsers.PRS_SeeEvents;
import waffleoRai_cafebotCommands.parsers.PRS_SeeTZ;
import waffleoRai_cafebotCommands.parsers.PRS_SetFarewells;
import waffleoRai_cafebotCommands.parsers.PRS_SetGreetings;
import waffleoRai_cafebotCommands.parsers.PRS_SetTZ;
import waffleoRai_cafebotCommands.parsers.PRS_qChan;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */

/**
 * An interface for a Parser object - an object that can convert a String command
 * into a Command object.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public interface Parser {
	
	/**
	 * Take an array of string arguments, similar to a command line induction, and interpret
	 * them, returning an executable bot Command.
	 * @param args Command arguments specified in a Discord message.
	 * @param event Event that was fired by submission of Discord message containing command.
	 * @return Executable bot command representing the user submitted string command.
	 */
	public Command generateCommand(String[] args, MessageReceivedEvent event);
	
	/**
	 * Generate and return a string command/Parser map of all default commands.
	 * @return A string command/Parser map containing all default functions.
	 */
	public static Map<String, Parser> mapAllKnownParsers()
	{
		Map<String, Parser> pmap = new HashMap<String, Parser>();
		
		pmap.put("help", new PRS_Help());
		pmap.put("cleanme", new PRS_CleanMessages_1());
		pmap.put("cleanmeday", new PRS_CleanMessages_2());
		pmap.put("myevents", new PRS_SeeEvents());
		pmap.put("ecancel", new PRS_CancelEvent());
		pmap.put("onetime", new PRS_EventOnetime());
		pmap.put("deadline", null);
		pmap.put("weekly", new PRS_EventWeekly());
		pmap.put("biweekly", new PRS_EventBiweekly());
		pmap.put("monthlyday", new PRS_EventMonthlyDOM());
		pmap.put("monthlydow", new PRS_EventMonthlyDOW());
		pmap.put("birthday", new PRS_AddBirthday());
		pmap.put("gettz", new PRS_GetTZ());
		pmap.put("changetz", new PRS_SetTZ());
		pmap.put("seealltz", new PRS_SeeTZ());
		pmap.put("sor", new PRS_SOR());
		pmap.put("eventhelp", new PRS_EventHelp());
		pmap.put("listroles", null);
		pmap.put("amiaudconf", null);
		pmap.put("qchan", new PRS_qChan());
		
		pmap.put("addperm", null);
		pmap.put("remperm", null);
		pmap.put("chchan", new PRS_ChChan());
		pmap.put("makerole", null);
		pmap.put("seeroles", null);
		pmap.put("revokerole", null);
		pmap.put("completerole", null);
		pmap.put("pushdeadline", null);
		pmap.put("cleanday", new PRS_CleanMessages_3());
		pmap.put("setGreetings", new PRS_SetGreetings());
		pmap.put("pingGreetings", new PRS_PingMeGreetings());
		pmap.put("setFarewells", new PRS_SetFarewells());
		pmap.put("pingFarewells", new PRS_PingMeFarewells());
		pmap.put("checkg", new PRS_CheckGreetingStatus());
		pmap.put("audconf", null);
		pmap.put("cmdclean", null);
		pmap.put("autocmdclean", null);
		
		pmap.put("accept", null);
		pmap.put("decline", null);
		
		pmap.put("attend", new PRS_AttendEvent());
		pmap.put("cantgo", new PRS_DeclineEvent());
		pmap.put("checkrsvp", new PRS_CheckRSVP());
		
		pmap.put("saysomething", new PRS_SaySomething());
		pmap.put("bringmeagene", null);
		
		return pmap;
	}

}
