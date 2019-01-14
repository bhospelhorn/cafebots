package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | July 14, 2018
 * Version 1.0.0 Documentation | July 14, 2018
 * 
 */

/**
 * A bot command for sending a farewell message to the server when a user leaves,
 * and pinging requesting admins to alert them to the departure.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since January 14, 2019
 */
public class CMD_MemberFarewell extends CommandAdapter{

	//private Member user;
	
	/**
	 * Construct a MemberFarewell command.
	 * @param m Member that just left.
	 */
	public CMD_MemberFarewell(Member m)
	{
		//user = m;
		super.requestingUser = m;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		bot.farewellUser(requestingUser);
		bot.pingUserDeparture(requestingUser);
	}

	public String toString()
	{
		return ParseCore.CMD_FAREWELLMEMBER;
	}

}
