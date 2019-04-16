package waffleoRai_cafebotCommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_Utils.BitStreamer;
import waffleoRai_cafebotCommands.BotScheduler.Position;
import waffleoRai_cafebotCommands.Commands.CMD_AutocleanCommands;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_IssueReminder;
import waffleoRai_cafebotCommands.Commands.CMD_MemberFarewell;
import waffleoRai_cafebotCommands.Commands.CMD_NewMemberNotify;
import waffleoRai_cafebotCommands.Commands.CMD_NotifyCancellation;
import waffleoRai_cafebotCommands.Commands.CMD_OtherBotHandledMessage;
import waffleoRai_cafebotCommands.Commands.CMD_ParserBlockWarn;
import waffleoRai_cafebotCommands.Commands.CMD_ProcessRoleUpdate;
import waffleoRai_cafebotCommands.Commands.CMD_ResetCheck;
import waffleoRai_cafebotCommands.Commands.CMD_WishBirthday;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_cafebotRoles.ActorRole;
import waffleoRai_schedulebot.Birthday;
import waffleoRai_schedulebot.CalendarEvent;
import waffleoRai_schedulebot.EventAdapter;
import waffleoRai_schedulebot.EventType;
import waffleoRai_schedulebot.Schedule;

/*
 * UPDATES
 * 
 * Creation | June 10, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 14, 2018
 * 	Added user leave farewell message framework
 * 1.1.0 -> 1.1.1 | July 15, 2018
 * 	Altered default perm map set to revolve around 7 bots instead of 9.
 * 	Added methods to access information
 * 1.1.1 -> 1.2.0 | July 20, 2018
 * 	Initial update for command ID compatibility
 * 1.2.0 -> 1.2.1 | August 7, 2018
 * 	Added command ID passing for responses
 * 1.2.1 -> 1.2.2 | August 11, 2018
 * Updated command ID to MessageID object
 * 1.2.2 -> 1.3.0 | January 14, 2019
 * Update for JDA/ ability to interrupt commands, waits for bot status checks
 * 1.3.0 -> 1.3.1 | April 15, 2019
 * Better handling of null and empty input messages?
 */

/**
 * The class responsible for parsing commands and queuing them for the bots to execute.
 * <br><br><b>Background Threads:</b>
 * <br>- Parser Thread (<i>ParseCore.ParserThread</i>)
 * <br>Instantiated at Construction: N
 * <br>Started at Construction: N
 * <br><br><b>I/O Options:</b>
 * <br>[None]
 * <br><br><i>Outstanding Issues:</i>
 * <br> - Should ideally be multithreaded
 * <br> - Should ideally have a more tightly managed queue size
 * @author Blythe Hospelhorn
 * @version 1.3.1
 * @since April 15, 2019
 */
public class ParseCore {
	
	/* ----- Constants ----- */
	
	public static final String CMD_WISHBIRTHDAY = "wishbday";
	public static final String CMD_GREETNEWMEMBER = "greetnewb";
	public static final String CMD_PINGARRIVAL = "pingarrival";
	public static final String CMD_FAREWELLMEMBER = "farewelljerk";
	public static final String CMD_PINGDEPARTURE = "pingdepart";
	
	public static final String CMD_AUTOCLEAN = "doautoclean";
	
	public static final String CMD_REM_ONETIME = "remonetime";
	public static final String CMD_REM_WEEKLY = "remweekly";
	public static final String CMD_REM_BIWEEKLY = "rembiweekly";
	public static final String CMD_REM_MONTHLYA = "remmontha";
	public static final String CMD_REM_MONTHLYB = "remmonthb";
	public static final String CMD_REM_DEADLINE = "remdeadline";
	
	public static final String CMD_ROLE_MAJOR = "pingrolemaj";
	public static final String CMD_ROLE_MINOR = "pingrolemin";
	public static final String CMD_ROLE_EXTRA = "pingroleext";
	
	public static final String CMD_NOTIFY_AUDCONF = "notify_audconf";
	public static final String CMD_NOTIFY_ROLECOMPLETE = "notify_rolecomplete";
	public static final String CMD_NOTIFY_ROLEREVOKED = "notify_roledelete";
	
	public static final String CMD_ROLEUPDATE = "onroleupdate";
	
	/* ----- Static Variables ----- */
	
	private static Map<String, Parser> parserMap;
	
	/* ----- Instance Variables ----- */
	
	private TransientBlacklist blacklist;
	private BotScheduler scheduler;
	private AbstractBot[] mybots;
	private String std_prefix;
	
	private MessageQueue mqueue;
	private GreetingQueue gqueue;
	private FarewellQueue fqueue;
	private ParserThread parserthread;
	
	private boolean block;
	
	private boolean[] statusLocks;
	
	/* ----- Construction ----- */
	
	/**
	 * Construct a ParseCore by linking it to a BotScheduler. The scheduler is required
	 * to know which bot accepts which commands at any given point in time.
	 * <br>The standard command prefix defaults to "!"
	 * <br>If the static standard parser map has not been instantiated, this constructor
	 * attempts to create and populate a new one.
	 * @param s BotScheduler to link to this ParseCore.
	 */
	public ParseCore(BotScheduler s)
	{
		blacklist = new TransientBlacklist();
		scheduler = s;
		std_prefix = "!";
		mybots = new AbstractBot[10];
		statusLocks = new boolean[10];
		if (parserMap == null) populateParserMap_Standard();
		block = false;
		mqueue = new MessageQueue();
		gqueue = new GreetingQueue();
		fqueue = new FarewellQueue();
	}
	
	/* ----- Getters ----- */
	
	/**
	 * Get the string representing the standard command prefix - that is, the string
	 * a message sent over Discord must start with if no bot is mentioned in the message
	 * for the parser to recognize the message as a potential command and invest time in 
	 * attempting to interpret it.
	 * @return Standard command prefix string currently recognized by this ParseCore.
	 */
	public String getPrefix()
	{
		return std_prefix;
	}
	
	private int getBotIndex(User botuser)
	{
		if (mybots == null) return -1;
		for (int i = 0; i < mybots.length; i++)
		{
			if (mybots[i] == null) continue;
			if (botuser.getIdLong() == mybots[i].getBotUser().getIdLong()) return i;
		}
		return -1;
	}
	
	/* ----- Setters ----- */
	
	/**
	 * Set the string representing the standard command prefix recognized by this ParseCore.
	 * This is the string of characters a message sent over Discord must begin with in order
	 * to be recognized as a potential command, assuming no bot users are mentioned.
	 * @param p String to set as command prefix. Cannot be null or empty!
	 */
	public void setPrefix(String p)
	{
		if (p == null) return;
		if (p.isEmpty()) return;
		std_prefix = p;
	}
	
	/**
	 * Load a bot into one of the bot ports for this ParseCore.
	 * @param b The bot to link.
	 * @param i The index to link the bot at.
	 */
	public void linkBot(AbstractBot b, int i)
	{
		if (i < 0) return;
		if (i >= mybots.length) return;
		mybots[i] = b;
	}
	
	/**
	 * Block the parser so that it throws out any commands its queue throws at it
	 * until it is unblocked. This can be potentially useful if other parts of the program
	 * aren't booted or certain bots have not logged in yet.
	 */
	public void block()
	{
		block = true;
	}
	
	/**
	 * Unblock a blogged parser so that it can once again accept commands.
	 */
	public void unblock()
	{
		block = false;
	}
	
	/**
	 * Request temporary blacklisting of a user. When a user is blacklisted, the bots
	 * will ignore any commands sent by that user in any channel or guild. This is
	 * intended to prevent users from issuing more commands while a bot is waiting for an
	 * answer from a particular user.
	 * @param uid Discord long UID of user to blacklist.
	 * @param bot Index of bot requesting blacklisting. Blacklist status has to be released for
	 * all bots waiting on user response for normal commands to be available to user again.
	 */
	public void blacklist(long uid, int bot)
	{
		blacklist.blacklist(uid, bot);
	}
	
	/**
	 * Clear blacklist request of user from specified bot.
	 * @param uid User to clear blacklist mark for.
	 * @param bot Bot requesting blacklist clear.
	 */
	public void unblacklist(long uid, int bot)
	{
		blacklist.clearBlock(uid, bot);
	}
	
	/* ----- Command Formatting ----- */
	
	private String removeMentions(String rawmsg, List<Member> mentioned)
	{
		//System.err.println("DEBUG ParseCore.removeMentions || Raw message: " + rawmsg);
		//System.err.println("DEBUG ParseCore.removeMentions || Number of mentions: " + mentioned.size());
		if(rawmsg == null) return "";
		if(rawmsg.isEmpty()) return rawmsg;
		for (Member u : mentioned)
		{
			String mstr = u.getAsMention();
			//System.out.println("DEBUG ParseCore.removeMentions || Mention string generated for user " + u.getUser().getName() + ": " + mstr);
			rawmsg = rawmsg.replace(mstr, "");
		}
		//System.err.println("DEBUG ParseCore.removeMentions || Raw message after mention removal: " + rawmsg);
		//Clean up any white space at the beginning and end.
		rawmsg = rawmsg.trim();
		//System.out.println("DEBUG ParseCore.removeMentions || Output: " + rawmsg);
		return rawmsg;
	}
	
	private String[] splitArgs(String cleanstring)
	{
		//Start by splitting along spaces...
		String[] init = cleanstring.split(" ");
		
		//Scan for double quotes and recombine all values within...
		List<String> list = new ArrayList<String>(init.length);
		boolean inquotes = false;
		String compound = "";
		for (String s : init)
		{
			if (!inquotes)
			{
				//Check for start of quotes...
				if (s.charAt(0) == '\"'){
					inquotes = true;
					compound += s.substring(1);
				}
				else
				{
					list.add(s);
				}
			}
			else
			{
				//Check for end quotes
				if (s.charAt(s.length() - 1) == '\"')
				{
					inquotes = false;
					compound += " " + s.substring(0, s.length() - 1);
					list.add(compound);
					compound = "";
				}
				else compound += " " + s;
			}
		}
		
		//Turn back into boring old array
		String[] args = new String[list.size()];
		args = list.toArray(args);
		
		return args;
	}
	
	/* ----- Parser Thread ----- */
	
	/**
	 * A background thread for processing unparsed messages and unhandled greeting
	 * events still in the ParseCore's queue. The implementation of this background
	 * thread and its accompanying queues is intended to keep from tying up the Listeners.
	 * <br>At the moment, the ParseCore is only single-threaded. It can in theory be made
	 * multi-threaded by instantiated more of these ParserThreads, but other potential
	 * concurrency issues may arise with the blacklist/response mechanism.
	 * <br>Because this program was written to be run on a two-core computer, resolving these
	 * potential concurrency conflicts was not a priority.
	 * <br><br><b>Dormancy period:</b> 1 minute
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since July 1, 2018
	 */
	public class ParserThread extends Thread
	{
		private boolean killMe;
		
		/**
		 * Construct a new ParserThread. This method does NOT start the thread.
		 */
		public ParserThread()
		{
			killMe = false;
			this.setDaemon(true);
			Random r = new Random();
			this.setName("CafebotsParserDaemon_" + Integer.toHexString(r.nextInt()));
		}
		
		@Override
		public void run()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.ParserThread.run || Thread " + this.getName() + " started!");
			while (!isDead())
			{
				//Check message queue
				while (!mqueue.isEmpty())
				{
					MessageReceivedEvent e = mqueue.pop();
					parseMessage(e);
				}
				//Check greeting queue
				while (!gqueue.isEmpty())
				{
					GuildMemberJoinEvent e = gqueue.pop();
					memberJoined(e);
				}
				//Check farewell queue
				while (!fqueue.isEmpty())
				{
					GuildMemberLeaveEvent e = fqueue.pop();
					memberLeft(e);
				}
				//We assume there's nothing left until interrupted
				if (!Thread.interrupted())
				{
					try 
					{
						//Sleep for a minute unless interrupted.
						Thread.sleep(60000);
					} 
					catch (InterruptedException e) 
					{
						Thread.interrupted();
					}
				}
			}
			System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.ParserThread.run || Thread " + this.getName() + " terminating...");
		}
		
		/**
		 * Get whether the internal kill switch has been flipped. Note that this value
		 * may not reflect the value of <code>Thread.isAlive()</code>, that is to say, 
		 * this method may return true though the thread is still running because the kill
		 * signal has not been resolved yet.
		 * @return True if the kill switch has been set, False otherwise.
		 */
		public synchronized boolean isDead()
		{
			return killMe;
		}
		
		/**
		 * Flip the kill switch on this thread. This method cannot instantaneously terminate
		 * the thread. If the thread is in the middle of processing queued events, it will
		 * not respond to the termination signal until it is done with its current round.
		 */
		public synchronized void terminate()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.ParserThread.run || Thread " + this.getName() + " termination requested!");
			killMe = true;
			this.interrupt();
		}
		
		/**
		 * Explicitly request an interruption of this thread.
		 */
		public synchronized void interruptMe()
		{
			this.interrupt();
		}
		
	}

	/**
	 * Instantiate (or re-instantiate) and start the internal ParserThread for this
	 * ParseCore. The ParserThread is responsible for popping greeting events and Discord
	 * messages off of the core's queues, interpreting them, converting them into commands,
	 * and sending them to the correct bots.
	 */
	public void startParserThread()
	{
		parserthread = new ParserThread();
		parserthread.start();
	}
	
	/**
	 * If there is a ParserThread running, this method sends it a kill signal. Note that
	 * there may be some delay between the calling of this method and the proper termination
	 * of the ParserThread.
	 * <br>This method does nothing if there is no running ParserThread.
	 */
	public void killParserThread()
	{
		if (parserthread != null)
		{
			if (parserthread.isAlive()) parserthread.terminate();
		}
	}
	
	/**
	 * Check to see if the parser thread is running.
	 * @return True if the parser thread is running, false if it is not.
	 */
	public boolean parserThreadRunning()
	{
		if (parserthread == null) return false;
		return parserthread.isAlive();
	}
	
	/* ----- Command Handling ----- */
	
	/**
	 * Queue a Discord message (wrapped by a MessageReceivedEvent) for processing by the
	 * ParserThread, or any external thread written to process such events.
	 * @param event Event to queue
	 */
	public void queueMessage(MessageReceivedEvent event)
	{
		//System.err.println(Thread.currentThread().getName() + " || ParseCore.queueMessage || DEBUG - Queuing message event!");
		mqueue.add(event);
		parserthread.interruptMe();
		//System.err.println(Thread.currentThread().getName() + " || ParseCore.queueMessage || DEBUG - Parser thread interrupt sent!");
	}
	
	/**
	 * Queue a GuildMemberJoinEvent (an event fired by Discord when a user joins a new guild/server)
	 * for processing by the ParserThread, or any external thread written to process such events.
	 * @param event Event to queue
	 */
	public void queueGreeting(GuildMemberJoinEvent event)
	{
		gqueue.add(event);
		parserthread.interruptMe();
	}
	
	/**
	 * Queue a GuildMemberLeaveEvent (an event fired by Discord when a user leaves a guild/server)
	 * for processing by the ParserThread, or any external thread written to process such events.
	 * @param event Event to queue
	 */
	public void queueFarewell(GuildMemberLeaveEvent event)
	{
		fqueue.add(event);
		parserthread.interruptMe();
	}
	
	/**
	 * Explicitly request that a command for a birthday wish be issued to the appropriate bot.
	 * This method is expected to be utilized primarily by the Schedule birthday event monitoring thread.
	 * <br>Note that although the Birthday event contains information about the date, neither this method
	 * nor the default AbstractBot method that handles it checks this information - the command will be
	 * executed once the bot reaches it, regardless of current date.
	 * @param b Birthday event to wish (containing user UID).
	 * @param guildID UID of guild (server) to issue birthday wish on.
	 */
	public void command_BirthdayWish(Birthday b, long guildID)
	{
		Command c = new CMD_WishBirthday(b, guildID);
		int bot = scheduler.sendCommandTo(CMD_WISHBIRTHDAY);
		if (bot < 0) return;
		if (bot >= mybots.length) return;
		if (mybots[bot] == null) return;
		//mybots[bot].getCommandQueue().addCommand(c);
		mybots[bot].submitCommand(c); //This one throws an interrupt!
	}
	
	/**
	 * Evaluate a received message, determine whether it is a valid command, generate a command
	 * object if the message can be interpreted, and issue the command, or any additional commands
	 * that may arise, to the most appropriate bot(s).
	 * <br>There are several factors that may prevent a message from being converted to a command.
	 * If the parser is blocked, the user has been blacklisted because one or more bots is waiting
	 * on a response, or the parser doesn't detect any identifying strings, the message may be
	 * thrown out as a non-command.
	 * @param event MessageReceivedEvent containing information about the message to parse.
	 */
	public void parseMessage(MessageReceivedEvent event)
	{
		//System.err.println(Thread.currentThread().getName() + " || ParseCore.parseMessage || DEBUG - Message parse requested.");
		if (block)
		{
			//Tell bot 1 to warn that channel that this command will not be executed since
			//the program is still booting.
			if (mybots[1] == null) return;
			Command c = new CMD_ParserBlockWarn(event.getChannel());
			mybots[1].getCommandQueue().addCommand(c);
			return;
		}
		if (event.getAuthor().isBot()) return; //Ignore anything sent by other bots
		//See if sender is currently blocked due to pending requests
		boolean b = blacklist.isBlacklisted(event.getAuthor());
		//Extract any mentions
		List<Member> mentioned = event.getMessage().getMentionedMembers();
		if (b)
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.parseMessage || DEBUG - Message received from blacklisted user " + event.getAuthor().getName());
			//Response from at least one bot is pending...
			int pendingVector = blacklist.getBlacklistVector(event.getAuthor());
			String command = event.getMessage().getContentRaw();
			if (!mentioned.isEmpty())
			{
				//Mask vector to not send message to any bots not mentioned.
				int mask = 0;
				for (Member u : mentioned)
				{
					int bi = getBotIndex(u.getUser());
					if (bi >= 0)
					{
						//User match!
						mask |= 1 << bi;
					}
				}
				pendingVector &= mask;
				//Remove mentions from command.
				command = removeMentions(command, mentioned);
			}
			//Parse message
			int r = Response.interpretResponse(command);
			//Use vector to determine which bots to send message to.
			for (int i = 0; i < mybots.length; i++)
			{
				boolean er = BitStreamer.readABit(pendingVector, i);
				if (er)
				{
					AbstractBot bot = mybots[i];
					if (bot == null) continue;
					ResponseQueue rq = bot.getResponseQueue();
					MessageID rmsg = new MessageID(event.getMessageIdLong(), event.getChannel().getIdLong());
					rq.respond(r, event.getAuthor(), event.getChannel(), rmsg);	
				}
			}
		}
		else
		{
			//System.err.println(Thread.currentThread().getName() + " || ParseCore.parseMessage || DEBUG - No user block.");
			String command = event.getMessage().getContentRaw();
			if (command == null || command.isEmpty()) return; //Just... ignore it
			if (!mentioned.isEmpty())
			{
				//Need to clean up command
				command = removeMentions(command, mentioned);
			}
			else
			{
				//Look for standard command prefix (and remove)
				command = command.trim();
				if (!command.startsWith(std_prefix)) return;
				command = command.substring(std_prefix.length());
			}
			String[] args = splitArgs(command);
			Parser p = parserMap.get(args[0]);
			Command c = null;
			if (p == null){
				c = new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
			}
			else c = p.generateCommand(args, event);
			//Determine which bot(s) to send command to.
			if (!mentioned.isEmpty())
			{
				//Query scheduler to see which bot is best
				int bot = scheduler.sendCommandTo(args[0]);
				//Make a list of the bot indices mentioned
				List<Integer> mbotlist = new ArrayList<Integer>(mentioned.size());
				for (Member u : mentioned)
				{
					int bi = getBotIndex(u.getUser());
					if (bi > 0 && bi < 10) mbotlist.add(bi);
				}
				
				//See if target bot is in mentions. If so, send command to that bot,
				//	and send the command to the others to display a message saying it's been handled.
				if (mbotlist.contains(bot) && mybots[bot] != null)
				{
					for (Integer i : mbotlist)
					{
						if (i == bot)
						{
							//System.err.println(Thread.currentThread().getName() + " || ParseCore.parseMessage || DEBUG - Sending command to bot " + i);
							//mybots[bot].getCommandQueue().addCommand(c);
							mybots[bot].submitCommand(c);
						}
						else
						{
							if (mybots[i] != null)
							{
								//System.err.println(Thread.currentThread().getName() + " || ParseCore.parseMessage || DEBUG - Sending command to bot " + i);
								//mybots[i].getCommandQueue().addCommand(new CMD_OtherBotHandledMessage(event.getChannel()));
								mybots[bot].submitCommand(new CMD_OtherBotHandledMessage(event.getChannel(), event.getMember()));
							}
						}
					}
					return;
				}
				
				//If target bot is not mentioned, then send to first bot mentioned.
				// If there is more than one bot mentioned, then send command to display handled message to others.
				boolean sent = false;
				for (Integer i : mbotlist)
				{
					if (!sent)
					{
						if (mybots[i] != null)
						{
							//mybots[i].getCommandQueue().addCommand(c);
							mybots[bot].submitCommand(c);
							sent = true;
						}
					}
					else
					{
						//mybots[i].getCommandQueue().addCommand(new CMD_OtherBotHandledMessage(event.getChannel()));
						mybots[bot].submitCommand(new CMD_OtherBotHandledMessage(event.getChannel(), event.getMember()));
					}
				}
			}
			else
			{
				//Query scheduler
				int bot = scheduler.sendCommandTo(args[0]);
				if (bot < 0) return;
				if (bot >= mybots.length) return;
				if (mybots[bot] == null) return;
				System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.parseMessage || DEBUG - Command accepted " + Long.toUnsignedString(event.getChannel().getIdLong()) + ":" + Long.toUnsignedString(event.getMessageIdLong()));
				//mybots[bot].getCommandQueue().addCommand(c);
				mybots[bot].submitCommand(c);
			}
		}
	}

	/**
	 * Issue a command to the appropriate bot to greet a newly joined member and 
	 * notify any requesting admins of the new user's arrival.
	 * @param e Event fired by the Discord API when new user joined.
	 */
	public void memberJoined(GuildMemberJoinEvent e)
	{
		//Ping greeting channel if set.
		//Ping admin users that request notification.
		//Bot command should do both.
		//Command c = new CMD_NewMemberNotify(e.getGuild(), e.getMember());
		Command c = new CMD_NewMemberNotify(e.getMember());
		int bot = scheduler.sendCommandTo(CMD_GREETNEWMEMBER);
		if (bot < 0) return;
		if (bot >= mybots.length) return;
		if (mybots[bot] == null) return;
		//mybots[bot].getCommandQueue().addCommand(c);
		mybots[bot].submitCommand(c);
	}
	
	/**
	 * Issue a command to the appropriate bot to print a farewell message and 
	 * notify any requesting admins of a member leaving the guild.
	 * @param e Event fired by the Discord API when a member left.
	 */
	public void memberLeft(GuildMemberLeaveEvent e)
	{
		Command c = new CMD_MemberFarewell(e.getMember());
		int bot = scheduler.sendCommandTo(CMD_FAREWELLMEMBER);
		if (bot < 0) return;
		if (bot >= mybots.length) return;
		if (mybots[bot] == null) return;
		//mybots[bot].getCommandQueue().addCommand(c);
		mybots[bot].submitCommand(c);
	}
	
	/**
	 * Issue an explicit command to a specific bot. This method is public, but its primary
	 * expected use is by other bots for passing along commands they cannot understand.
	 * @param botIndex Index of bot (as linked to this ParseCore) to send command to.
	 * @param cmd Command to send to bot.
	 */
	public void issueDirectCommand(int botIndex, Command cmd)
	{
		if (botIndex <= 0) return;
		if (botIndex > mybots.length) return;
		AbstractBot bot = mybots[botIndex];
		if (bot == null) return;
		//bot.getCommandQueue().addCommand(cmd);
		bot.submitCommand(cmd);
	}

	public void command_EventReminder(EventAdapter e, int level, long guildID)
	{
		int bot = 1;
		switch (e.getType())
		{
		case BIRTHDAY:
			bot = scheduler.sendCommandTo(CMD_WISHBIRTHDAY);
			break;
		case BIWEEKLY:
			bot = scheduler.sendCommandTo(CMD_REM_BIWEEKLY);
			break;
		case DEADLINE:
			bot = scheduler.sendCommandTo(CMD_REM_DEADLINE);
			break;
		case MONTHLYA:
			bot = scheduler.sendCommandTo(CMD_REM_MONTHLYA);
			break;
		case MONTHLYB:
			bot = scheduler.sendCommandTo(CMD_REM_MONTHLYB);
			break;
		case ONETIME:
			bot = scheduler.sendCommandTo(CMD_REM_ONETIME);
			break;
		case WEEKLY:
			bot = scheduler.sendCommandTo(CMD_REM_WEEKLY);
			break;
		default:
			break;
		}
		
		Command cmd = new CMD_IssueReminder(e, level, guildID);
		AbstractBot b = mybots[bot];
		if (b == null)
		{
			System.err.println("ParseCore.command_EventReminder || Bot " + bot + " does not exist!");
			return;
		}
		//b.getCommandQueue().addCommand(cmd);
		b.submitCommand(cmd);
	}
	
	public void command_EventCancellation(CalendarEvent e, boolean instance, long guild)
	{
		int bot = 1;
		switch (e.getType())
		{
		case BIRTHDAY:
			bot = scheduler.sendCommandTo(CMD_WISHBIRTHDAY);
			break;
		case BIWEEKLY:
			bot = scheduler.sendCommandTo(CMD_REM_BIWEEKLY);
			break;
		case DEADLINE:
			bot = scheduler.sendCommandTo(CMD_REM_DEADLINE);
			break;
		case MONTHLYA:
			bot = scheduler.sendCommandTo(CMD_REM_MONTHLYA);
			break;
		case MONTHLYB:
			bot = scheduler.sendCommandTo(CMD_REM_MONTHLYB);
			break;
		case ONETIME:
			bot = scheduler.sendCommandTo(CMD_REM_ONETIME);
			break;
		case WEEKLY:
			bot = scheduler.sendCommandTo(CMD_REM_WEEKLY);
			break;
		default:
			break;
		}
		
		Command cmd = new CMD_NotifyCancellation(e, instance, guild);
		AbstractBot b = mybots[bot];
		if (b == null)
		{
			System.err.println("ParseCore.command_EventReminder || Bot " + bot + " does not exist!");
			return;
		}
		//b.getCommandQueue().addCommand(cmd);
		b.submitCommand(cmd);
	}
	
	public void redirectEventCommand(Command cmd, EventType t)
	{
		int bot = 0;
		switch (t)
		{
		case BIRTHDAY:
			bot = scheduler.sendCommandTo(CMD_WISHBIRTHDAY);
			break;
		case BIWEEKLY:
			bot = scheduler.sendCommandTo(CMD_REM_BIWEEKLY);
			break;
		case DEADLINE:
			bot = scheduler.sendCommandTo(CMD_REM_DEADLINE);
			break;
		case MONTHLYA:
			bot = scheduler.sendCommandTo(CMD_REM_MONTHLYA);
			break;
		case MONTHLYB:
			bot = scheduler.sendCommandTo(CMD_REM_MONTHLYB);
			break;
		case ONETIME:
			bot = scheduler.sendCommandTo(CMD_REM_ONETIME);
			break;
		case WEEKLY:
			bot = scheduler.sendCommandTo(CMD_REM_WEEKLY);
			break;
		default: return;
		}
		
		this.issueDirectCommand(bot, cmd);
		
	}
	
	public void processRoleChange(Member member, List<Role> roles, boolean add)
	{
		int bot = 1;
		bot = scheduler.sendCommandTo(CMD_ROLEUPDATE);
		Command cmd = new CMD_ProcessRoleUpdate(member, roles, add);
		issueDirectCommand(bot, cmd);
	}
	
	public void command_AutoCommandClean(long guildID)
	{
		int bot = scheduler.sendCommandTo(CMD_AUTOCLEAN);
		if (bot < 0 || bot > 9) return;
		Command cmd = new CMD_AutocleanCommands(guildID);
		issueDirectCommand(bot, cmd);
	}
	
	public void deferToBot(Command cmd, int botConstructor)
	{
		for (int i = 1; i < mybots.length; i++)
		{
			AbstractBot b = mybots[i];
			if (b != null)
			{
				if (b.getConstructorType() == botConstructor)
				{
					b.submitCommand(cmd);
					return;
				}
			}
		}
		System.err.println(Thread.currentThread().getName() + " || ParseCore.deferToBot || Compatible bot not found...");
	}
	
	public void command_SessionCheck()
	{
		if (mybots == null) return;
		for (int i = 1; i < mybots.length; i++)
		{
			AbstractBot bot = mybots[i];
			if (bot != null) bot.submitCommand(new CMD_ResetCheck());
			try 
			{
				Thread.sleep(500);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public void setBetaBot()
	{
		//Find the first bot online that isn't the masterbot (1) and set as beta
		//Set others non-beta
		System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.setBetaBot || CALLED!");
		boolean bset = false;
		for (int i = 2; i < mybots.length; i++)
		{
			AbstractBot b = mybots[i];
			if (b == null) continue;
			if (!bset)
			{
				try
				{
					if (b.visiblyOnline()){
						b.setBeta(true);
						bset = true;
						System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.setBetaBot || BOT" + i + " set to beta bot!");
					}
					else b.setBeta(false);
				}
				catch (InterruptedException e)
				{
					//If it can't check to see if its online for whatever reason, 
					//	just assume it isn't.
					b.setBeta(false);
					System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.setBetaBot || BOT" + i + " was interrupted before it could check online status. Assuming offline...");
				}
			}
			else
			{
				b.setBeta(false);
			}
		}
		System.err.println(Schedule.getErrorStreamDateMarker() + " ParseCore.setBetaBot || RETURNING");
	}

	public synchronized boolean statusLock()
	{
		for (int i = 1; i < 10; i++)
		{
			if (mybots[i] != null && !statusLocks[i]) return true;
		}
		return false;
	}
	
	public synchronized void clearStatusLock()
	{
		for (int i = 1; i < 10; i++)
		{
			statusLocks[i] = false;
		}
	}
	
	public synchronized void signalStatusChange(int botIndex)
	{
		if (botIndex < 0 || botIndex >= 10) return;
		statusLocks[botIndex] = true;
	}
	
	/* ----- Response Handling ----- */
	
	/**
	 * Request a response on behalf of a linked bot from the specified user. This method
	 * blacklists the user in the ParseCore until a response is received and also queues
	 * the response request in the bot's response queue so that it knows what it is waiting
	 * for.
	 * @param botIndex Index of linked bot to request response for.
	 * @param user Discord User response is requested from.
	 * @param cmd Command that elicited response request.
	 * @param ch Discord Channel the response is expected in.
	 */
	public void requestResponse(int botIndex, User user, Command cmd, MessageChannel ch)
	{
		if (botIndex < 1 || botIndex >= mybots.length || mybots[botIndex] == null) return;
		blacklist(user.getIdLong(), botIndex);
		ResponseQueue rq = mybots[botIndex].getResponseQueue();
		rq.requestResponse(cmd, user, ch);
	}
	
	/* ----- Parser Handling ----- */
	
	/**
	 * Set and populate the default parser map. 
	 */
	public static void populateParserMap_Standard()
	{
		parserMap = Parser.mapAllKnownParsers();
	}
	
	/**
	 * Add a new parser to the static parser map.
	 * @param command String representation of command as submitted by users to associate
	 * with Parser.
	 * @param p Parser that should be mapped to the given command string.
	 */
	public static void addParser(String command, Parser p)
	{
		if(parserMap == null) parserMap = new HashMap<String, Parser>();
		parserMap.put(command, p);
	}

	/* ----- Bot Duty Distribution ----- */
	
	//Includes methods for generating position and command lists denovo...
	
	/**
	 * For easy BotScheduler construction - this method generates a collection
	 * containing the default Positions with all of their commands which can
	 * be passed to the de novo BotScheduler constructor.
	 * @return List containing all default <i>BotScheduler.Position</i>d objects set up
	 * for the cafebots.
	 */
	public static List<Position> getDefaultShiftPositions()
	{
		//Greeter = 0
		//Helpdesk = 1
		//Member data = 2
		//Cleanup = 3
		
		/*
		 * Greeter (These are all automatic - need to be assigned strings for mapping)
		 * 		greet
		 * 		ping admins new member
		 * 		wish birthday
		 * 
		 * Helpdesk
		 * 		help
		 * 		saysomething
		 * 		seealltz
		 *      eventhelp
		 * 
		 * Member data
		 * 		gettz
		 * 		changetz
		 * 		sor
		 * 		amiaudconf
		 * 
		 * Cleanup
		 * 		cleanme
		 * 		cleanmeday
		 * 		cleanday
		 */
		
		Position greeter = new Position(0);
		greeter.addCommand(CMD_GREETNEWMEMBER);
		greeter.addCommand(CMD_PINGARRIVAL);
		greeter.addCommand(CMD_WISHBIRTHDAY);
		greeter.addCommand("qchan");
		greeter.addCommand(CMD_FAREWELLMEMBER);
		greeter.addCommand(CMD_PINGDEPARTURE);
		greeter.addCommand("checkg");
		
		Position helpdesk = new Position(1);
		helpdesk.addCommand("help");
		helpdesk.addCommand("saysomething");
		helpdesk.addCommand("seealltz");
		helpdesk.addCommand("eventhelp");
		
		Position userhelper = new Position(2);
		userhelper.addCommand("gettz");
		userhelper.addCommand("changetz");
		userhelper.addCommand("sor");
		userhelper.addCommand("amiaudconf");
		userhelper.addCommand(CMD_NOTIFY_AUDCONF);
		userhelper.addCommand("eventinfo");
		userhelper.addCommand("roleinfo");
		
		Position cleaner = new Position(3);
		cleaner.addCommand("cleanme");
		cleaner.addCommand("cleanmeday");
		cleaner.addCommand("cleanday");
		cleaner.addCommand("cmdclean");
		//cleaner.addCommand("autocmdclean");
		cleaner.addCommand(CMD_AUTOCLEAN);
		
		List<Position> plist = new ArrayList<Position>(4);
		plist.add(greeter);
		plist.add(helpdesk);
		plist.add(userhelper);
		plist.add(cleaner);
		
		return plist;
	}
	
	/**
	 * For easy BotScheduler construction - this method generates a map containing
	 * the default command string/ bot index pairings.
	 * @return Default Map of commands to bot indices set to take those commands.
	 */
	public static Map<String, Integer> getDefaultPermPositionMap()
	{
		//Note - moving 9 to 3 and 8 to 4
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		//map.put("myevents", 9);
		map.put("myevents", 3);
		map.put("ecancel", 1);
		map.put("onetime", 1);
		map.put("deadline", 1);
		map.put("weekly", 1);
		map.put("biweekly", 1);
		map.put("monthlyday", 1);
		map.put("monthlydow", 1);
		map.put("birthday", 1);
		//map.put("listroles", 9);
		map.put("listroles", 4);
		map.put("addperm", 1);
		map.put("remperm", 1);
		map.put("chchan", 1);
		map.put("makerole", 2);
		map.put("seeroles", 2);
		map.put("revokerole", 2);
		map.put("completerole", 2);
		map.put("pushdeadline", 2);
		map.put("setGreetings", 1);
		map.put("pingGreetings", 1);
		map.put("setFarewells", 1);
		map.put("pingFarewells", 1);
		map.put("audconf", 2);
		map.put("autocmdclean", 1);
		map.put("checkperm", 1);
		
		map.put(CMD_ROLEUPDATE, 1);
		
		map.put("attend", 1);
		map.put("cantgo", 1);
		map.put("checkrsvp", 1);
		
		map.put("accept", 2);
		map.put("decline", 2);
		
		//map.put(CMD_REM_DEADLINE, 8);
		map.put(CMD_REM_MONTHLYB, 3);
		map.put(CMD_REM_DEADLINE, 4);
		map.put(CMD_REM_WEEKLY, 7);
		map.put(CMD_REM_BIWEEKLY, 6);
		map.put(CMD_REM_MONTHLYA, 5);
		map.put(CMD_REM_ONETIME, 5);
		//map.put(CMD_REM_ONETIME, 4);
		//map.put(CMD_REM_MONTHLYB, 3);
		
		//map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_SCHOOLGIRL, 9);
		//map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_SCHOOLGIRL_CROWD, 9);
		//map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_ANNOUNCE, 8);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_SCHOOLGIRL, 3);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_SCHOOLGIRL_CROWD, 3);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_ANNOUNCE, 4);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_SCHOOLBOY, 7);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_SCHOOLBOY_CROWD, 7);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_STUDENT, 7);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_STUDENT_CROWD, 7);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_MONSTER, 6);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_MONSTER_CROWD, 6);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_ADULT, 5);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_ADULT_CROWD, 5);
		//map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_CHILD, 4);
		//map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_CHILD_CROWD, 4);
		//map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_ANIMAL, 3);
		//map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_ANIMAL_CROWD, 3);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_CHILD, 6);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_CHILD_CROWD, 6);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_ANIMAL, 7);
		map.put(CMD_ROLE_EXTRA + ActorRole.ROLETYPE_EXT_ANIMAL_CROWD, 7);
		map.put(CMD_ROLE_MINOR, 2);
		map.put(CMD_ROLE_MAJOR, 1);
		
		return map;
	}
	
}
