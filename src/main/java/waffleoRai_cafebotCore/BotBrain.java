package waffleoRai_cafebotCore;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import waffleoRai_Utils.Athread;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_cafebotCommands.BotScheduler;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.JoinListener;
import waffleoRai_cafebotCommands.MessageListener;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotCommands.RoleChangeListener;
import waffleoRai_cafebotCommands.Commands.CMD_SyncGuilds;
import waffleoRai_schedulebot.Birthday;
import waffleoRai_schedulebot.BiweeklyEvent;
import waffleoRai_schedulebot.CalendarEvent;
import waffleoRai_schedulebot.DeadlineEvent;
import waffleoRai_schedulebot.EventType;
import waffleoRai_schedulebot.MonthlyDOMEvent;
import waffleoRai_schedulebot.MonthlyDOWEvent;
import waffleoRai_schedulebot.OneTimeEvent;
import waffleoRai_schedulebot.ReminderMap;
import waffleoRai_schedulebot.ReminderTime;
import waffleoRai_schedulebot.Schedule;
import waffleoRai_schedulebot.WeeklyEvent;

public class BotBrain {
	
	/* ----- Constants ----- */
	
	public static final int LOGIN_RETRY_COUNT_RESET_TIME = 900; //Seconds
	public static final int SESSION_MONITOR_LOOP_MINUTES = 30;
	
	/* ----- Instance Variables ----- */
	
	private String programDirectory;
	
	private Language language;
	
	private GuildMap userdata;
	private AbstractBot[] bots;
	private BotScheduler shiftManager;
	
	private ParseCore parser;
	
	private StringMap commonStrings;

	private GuildDataAdder userdataThread;
	private ResetMonitorThread sessionMonitor;
	private ResetLoginCountThread loginTryMonitor;
	
	private boolean on;
	
	/* ----- Construction ----- */
	
  	public BotBrain(String basedir, Language l)
	{
 		AbstractBot.setOffdutyBotsOffline(true);
 		
		programDirectory = basedir;	
		userdata = null;
		bots = new AbstractBot[10];
		shiftManager = null;
		commonStrings = null;
		//remindertimes = null;
		parser = null;
		commonStrings = new StringMap();
		//remindertimes = new ReminderMap();
		language = l;
		on = false;
	}
	
	/* ----- Boot/Shutdown ----- */
	
	public void start(boolean verbose) throws LoginException, InterruptedException
	{
		if (bots[1] == null){
			System.err.println("BotBrain.start || ERROR: Master bot (index 1) required."); 
			throw new IllegalStateException();
		}
		//Here, the listeners are generated and given to the master bot.
		
		MessageListener ml = new MessageListener(parser, verbose);
		GreetingListener gl = new GreetingListener(parser, verbose);
		JoinListener jl = new JoinListener(verbose, this);
		RoleChangeListener rl = new RoleChangeListener(verbose, this);
		//bots[1].addListener(debuglistener);
		bots[1].addListener(ml);
		bots[1].addListener(gl);
		bots[1].addListener(jl);
		bots[1].addListener(rl);
		if (bots[2] != null) bots[2].addListener(new StatusChangeListener(bots[2], bots[1]));
		//Logs in all bots and starts all threads
		parser.block();
		//Will go ahead and start them all, but this method blocks until everything is ready.
		for (int i = 2; i < 10; i++)
		{
			if (bots[i] != null)
			{
				bots[1].addListener(new StatusChangeListener(bots[1], bots[i]));
				bots[i].addListener(new StatusChangeListener(bots[i], bots[1]));
			}
		}
		
		LoginListener l = new LoginListener();
		int bcount = 0;
		for (int i = 1; i < 10; i++)
		{
			if (bots[i] != null)
			{
				bcount++;
				bots[i].addListener(l);
				//bots[i].addStatusChangeDebugListener();
				bots[i].loginAsync();
				/*try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
					Thread.interrupted();
					e.printStackTrace();
				}*/
			}
		}
		
		//Start user data manager
		userdataThread = new GuildDataAdder();
		userdataThread.start();
		
		//Start parser core
		parser.startParserThread();
		
		//Start shift scheduling thread
		shiftManager.startTimer();
		
		//Start guild schedule and backup threads
		userdata.startAllBackgroundThreads();
		
		//Start session monitor
		sessionMonitor = new ResetMonitorThread();
		sessionMonitor.start();
		
		//Start login retry count monitor
		loginTryMonitor = new ResetLoginCountThread();
		loginTryMonitor.start();
		
		//Block until bots have logged in.
		int secondCounter = 0;
		while (l.getLoginCount() < bcount)
		{
			//Wait
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				System.err.println("BotBrain.start || Thread start wait sleep interrupted... Rechecking bot status...");
				e.printStackTrace();
			}
			secondCounter++;
			if (secondCounter % 60 == 0)
			{
				System.out.println("BotBrain.start || There appears to be one or more hanging logins... (" + secondCounter + " seconds since login check started.)");
				for (int i = 1; i < 10; i++)
				{
					if (bots[i] != null)
					{
						if (!bots[i].isOn())
						{
							System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.start || BOT" + i + " is not online. Attempting to restart login...");
							JDA botcore = bots[i].getJDA();
							if (botcore != null) botcore.shutdownNow();
							bots[i].loginAsync();
						}
					}
				}
			}
		}
		l.resetLoginCounter();
		bots[1].submitCommand(new CMD_SyncGuilds());
		System.out.println("BotBrain.start || All bots have logged in!");
		
		on = true;
		//Wait a second to choose beta bot
		while(parser.statusLock())
		{
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}	
		}
		System.out.println("BotBrain.start || All bots have updated their status!");
		parser.setBetaBot();
		parser.unblock();
		System.out.println("BotBrain.start || Parser has been unblocked!");
	}
	
	public void terminate() throws IOException
	{
		//Logs out all bots and terminates all threads
		parser.block();
		//Also destroys all listeners currently registered to bots.
		for (int i = 1; i < 10; i++)
		{
			if (bots[i] != null)
			{
				bots[i].closeJDA();
				bots[i].removeAllListeners();
			}
		}
		//Kill session manager
		sessionMonitor.kill();
		
		//Stop shift scheduling thread
		shiftManager.stopTimer();
				
		//Stop guild schedule and backup threads
		userdata.terminateAllBackgroundThreads();
		
		//Kill parser core
		parser.killParserThread();
		
		//Kill the login try monitor
		loginTryMonitor.kill();
		
		//Kill user data manager
		userdataThread.terminate();
		
		on = false;
		parser.unblock();
		
		saveUserData();
	}
	
	public void forceJDACheck()
	{
		parser.command_SessionCheck();
	}
	
	public void forceHardReset() throws LoginException
	{
		System.err.println(Schedule.getErrorStreamDateMarker() + " BotBarin.forceHardReset || Hard resetting all bots...");
		parser.block();
		int bcount = bots.length;
		for (int i = 1; i < bcount; i++)
		{
			if (bots[i] != null)
			{
				bots[i].forceKill();
			}
		}
		
		//Wait a couple seconds before sending command to reboot...
		try 
		{
			Thread.sleep(5000);
		} 
		catch (InterruptedException e) 
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " BotBarin.forceHardReset || Wait sleep interrupted! Premature login...");
			e.printStackTrace();
		}
		
		//Login
		LoginListener l = new LoginListener();
		int bin = 0;
		for (int i = 1; i < 10; i++)
		{
			if (bots[i] != null)
			{
				bin++;
				bots[i].addListener(l);
				bots[i].loginAsync();
			}
		}
		

		//Block until bots have logged in.
		int secondCounter = 0;
		while (l.getLoginCount() < bin)
		{
			//Wait
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.forceHardReset || Thread start wait sleep interrupted... Rechecking bot status...");
				e.printStackTrace();
			}
			secondCounter++;
			if (secondCounter % 60 == 0)
			{
				System.out.println(Schedule.getErrorStreamDateMarker() + " BotBrain.forceHardReset || There appears to be one or more hanging logins... (" + secondCounter + " seconds since login check started.)");
				for (int i = 1; i < 10; i++)
				{
					if (bots[i] != null)
					{
						if (!bots[i].isOn())
						{
							System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.forceHardReset || BOT" + i + " is not online. Attempting to restart login...");
							JDA botcore = bots[i].getJDA();
							if (botcore != null) botcore.shutdownNow();
							bots[i].loginAsync();
						}
					}
				}
			}
		}
		l.resetLoginCounter();
		
		bots[1].submitCommand(new CMD_SyncGuilds());
		parser.unblock();
		System.out.println(Schedule.getErrorStreamDateMarker() + " BotBrain.forceHardReset || All bots have logged in!");
	}
	
	/* ----- Bot Status ----- */
	
	protected boolean amIOnline(AbstractBot bot) throws InterruptedException
	{
		//TODO: Wait loops are infinite hang traps/CPU eaters.
		//	They need to be able to accept interrupts and process thread kill requests!
		
		if (bot == null) return false;
		
		//Get bot index
		int boti = bot.getLocalIndex();
		
		//Pick a bot that isn't that bot
		AbstractBot checker = null;
		for (int i = 1; i < bots.length; i++)
		{
			if (i == boti) continue;
			if (bots[i] == null) continue;
			checker = bots[i];
			break;
		}
		System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.amIOnline || Checking online status for BOT" + bot.getLocalIndex() + " using BOT" + checker.getLocalIndex());
		
		//Wait for check bot to be on
		while (!checker.isOn())
		{
			Thread.sleep(10);
		}
		//System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.amIOnline || BOT" + checker.getLocalIndex() + " is online!");
		//Check the JDA for checker
		JDA cjda = checker.getJDA();
		
		//Wait for target bot to be on
		while (!bot.isOn())
		{
			Thread.sleep(10);
		}
		//System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.amIOnline || BOT" + bot.getLocalIndex() + " is online!");
		//Get UID of bot to check
		long meid = bot.getBotUser().getIdLong();
		
		//Grab a random mutual guild
		User target = cjda.getUserById(meid);
		List<Guild> mutualg = target.getMutualGuilds();
		if (mutualg.isEmpty())
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " ERROR: BOT" + checker.getLocalIndex() + " & BOT" + bot.getLocalIndex() + " share no mutual guilds. Online status cannot be checked?");
			return true;
		}
		Guild g = mutualg.get(0);
		Member tmem = g.getMemberById(meid);
		
		//Get status of bot to check from other JDA
		//JDA visSesh = cjda.getUserById(meid).getJDA();
		OnlineStatus tonline = tmem.getOnlineStatus();
		//boolean amionline = (visSesh.getPresence().getStatus() != OnlineStatus.OFFLINE);
		//System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.amIOnline || BOT" + bot.getLocalIndex() + " visible status: " + visSesh.getPresence().getStatus().toString());
		boolean amionline = (tonline != OnlineStatus.OFFLINE);
		System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.amIOnline || BOT" + bot.getLocalIndex() + " visible status: " + tonline.toString());
		return amionline;
	}

	protected void signalStatusChange(AbstractBot bot)
	{
		//System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.signalStatusChange || BOT" + bot.getLocalIndex() + " signaled status change!");
		parser.signalStatusChange(bot.getLocalIndex());
	}
	
	/* ----- Inner Classes ----- */
	
	public static class StringMap
	{
		private Map<String, String> map;
		
		public StringMap()
		{
			map = new HashMap<String, String>();
		}
		
		public synchronized String get(String key)
		{
			return map.get(key);
		}
		
		public synchronized void put(String key, String value)
		{
			map.put(key, value);
		}
		
		public synchronized void replaceMap(Map<String, String> newmap)
		{
			map = newmap;
		}
		
	}
	
	/* ----- Getters ----- */
	
	public GuildMap getUserData()
	{
		return userdata;
	}
	
	public long getReminderTime(EventType eventType, int reminderlevel, long eventtime_millis)
	{
		/*if (remindertimes == null) return -1;
		ReminderTime[] tarr = remindertimes.get(eventType);
		if (tarr == null) return -1;
		if (reminderlevel < 0) return -1;
		if (reminderlevel > tarr.length) return -1;
		if (tarr[reminderlevel] == null) return -1;
		return tarr[reminderlevel].calculateReminderTime(eventtime_millis);*/
		return Schedule.getReminderTime(eventType, reminderlevel).calculateReminderTime(eventtime_millis);
	}

	public ReminderTime getReminderTime(EventType eventType, int reminderlevel)
	{
		/*if (remindertimes == null) return null;
		ReminderTime[] tarr = remindertimes.get(eventType);
		if (tarr == null) return null;
		if (reminderlevel < 0) return null;
		if (reminderlevel > tarr.length) return null;
		return tarr[reminderlevel];*/
		return Schedule.getReminderTime(eventType, reminderlevel);
	}
	
	public String getCommonString(String key)
	{
		if (commonStrings == null) return null;
		return commonStrings.get(key);
	}
	
	public String getEventtypeString(EventType t)
	{
		String key = "commonstrings.type." + t.getCommonKey();
		return this.getCommonString(key);
	}
	
	public String getSubjectivePronoun(int gender)
	{
		String key = BotStrings.getGenderPronounCommonKeyStem(gender);
		return getCommonString(key + "_subject");
	}
	
	public String getObjectivePronoun(int gender)
	{
		String key = BotStrings.getGenderPronounCommonKeyStem(gender);
		return getCommonString(key + "_object");
	}
	
	public String getPossessivePronoun(int gender)
	{
		String key = BotStrings.getGenderPronounCommonKeyStem(gender);
		return getCommonString(key + "_possess");
	}
	
	public AbstractBot[] getBots()
	{
		AbstractBot[] arr = new AbstractBot[10];
		for (int i = 0; i < 10; i++)
		{
			arr[i] = bots[i];
		}
		return arr;
	}
	
	public String getTimezoneListPath()
	{
		String sep = File.separator;
		return programDirectory + sep + LaunchCore.DIR_COREDATA + sep + LaunchCore.DIR_COREDATA_DATA + sep + LaunchCore.TZLIST_FILE;
	}
	
	public Language getLanguage()
	{
		return language;
	}
	
	public void registerNewGuild(Guild g)
	{
		//See if already has that guild.
		GuildSettings gs = userdata.getGuildSettings(g.getIdLong());
		if (gs != null) return;
		userdata.newGuild(g, parser);
		if (on)
		{
			gs = userdata.getGuildSettings(g.getIdLong());
			gs.startBackupThread();
			gs.startScheduleThreads();
		}
		
	}
	
	public void registerNewMember(Member m)
	{
		//boolean added = userdata.newMember(m);
		boolean added = userdata.newMember(m, parser);
		if (!added)
		{
			System.err.println(Thread.currentThread().getName() + " || BotBrain.registerNewUser || Guild data could not be found... Attempting to generate new guild.");
			Guild g = m.getGuild();
			System.err.println(Thread.currentThread().getName() + " || BotBrain.registerNewUser || Guild: " + g.getName() + " | User: " + m.getUser().getName() + " (" + m.getUser().getIdLong() + ")");
			registerNewGuild(g);
		}
	}
	
	public void registerNewUser(User u)
	{
		userdata.newUser(u);
	}
	
	public GuildSettings getGuildData(long guildID)
	{
		GuildSettings gs = userdata.getGuildSettings(guildID);
		return gs;
	}
	
	public ActorUser getUserProfile(long userID)
	{
		return userdata.getUserProfile(userID);
	}
	
	public GuildUser getGuildUser(long guildID, long userID)
	{
		GuildSettings gs = userdata.getGuildSettings(guildID);
		if (gs == null) return null;
		return gs.getUser(userID);
	}
	
	public int getIndexOfBotAtPosition(int pindex)
	{
		return this.shiftManager.getBotAtPosition(pindex);
	}
	
	/* ----- Setters ----- */
	
  	public void setUserDataMap(GuildMap data)
	{
		userdata = data;
	}
	
	public void setBot(AbstractBot b, int i)
	{
		if (i < 1) return;
		if (i > 9) return;
		bots[i] = b;
	}

	public void setShiftManager(BotScheduler s)
	{
		shiftManager = s;
	}
	
	public void setCommonStringMap(Map<String, String> stringmap)
	{
		commonStrings.replaceMap(stringmap);
	}
	
	public void generateReminderTimeMap(Map<String, String> xmlmap) throws UnsupportedFileTypeException
	{
		ReminderMap rm = new ReminderMap();
		EventType[] all = EventType.values(); //Get all event types...
		final String keystem = "reminder_";
		for (EventType t : all)
		{
			String keybase = "reminders." + t.getStandardKey();
			ReminderTime[] rts = new ReminderTime[5];
			for (int i = 0; i < 5; i++)
			{
				int j = i+1;
				String key = keybase + "." + keystem + j;
				String rt = xmlmap.get(key);
				if (rt == null) rts[i] = new ReminderTime();
				else rts[i] = new ReminderTime(rt);
			}
			rm.put(t, rts);
		}
		Schedule.loadReminderTimes(rm);
	}
	
	public void regenerateParserCore()
	{
		parser = new ParseCore(shiftManager);
		for (int i = 1; i < 10; i++) parser.linkBot(bots[i], i);
		shiftManager.setParseCore(parser);
		shiftManager.setCurrentShift();
	}
	
	public void loadUserData() throws IOException, UnsupportedFileTypeException, IllegalStateException
	{
		System.out.println("BotBrain.loadUserData || Loading user data...");
		if (parser == null)
		{
			System.err.println("BotBrain.loadUserData || ERROR: Parser must be instantiated before user data can be loaded!");
			throw new IllegalStateException();
		}
		String udatdir = programDirectory + File.separator + LaunchCore.DIR_USERDATA;
		userdata = new GuildMap(udatdir, parser);
		System.out.println("BotBrain.loadUserData || User data loaded!");
	}
	
	/* ----- Bot Commands ----- */
	
	public void redirectCommand(Command cmd, int bottype)
	{
		//Has to figure out which bot is most appropriate to send command to.
		//To redirect, must request a bot type (as seen in BotConstructor)
		//Looks for requested type. If can't find, looks for vanilla. If can't find, reports to stderr
		for (int i = 1; i < 10; i++)
		{
			AbstractBot b = bots[i];
			if (b == null) continue;
			int bt = b.getConstructorType();
			if (bt == bottype)
			{
				b.getCommandQueue().addCommand(cmd);
				return;
			}
		}
		
		for (int i = 1; i < 10; i++)
		{
			AbstractBot b = bots[i];
			if (b == null) continue;
			int bt = b.getConstructorType();
			if (bt == BotConstructor.VANILLA)
			{
				b.getCommandQueue().addCommand(cmd);
				return;
			}
		}
		
		System.err.println(Thread.currentThread().getName() + " || BotBrain.redirectCommand || Command redirection failed: " + cmd.toString() + " bottype = " + bottype);
	}
		
	public void redirectEventCommand(Command cmd, EventType type)
	{
		parser.redirectEventCommand(cmd, type);
	}
	
	public void blacklist(long uid, int bot)
	{
		parser.blacklist(uid, bot);
	}
	
	public void unblacklist(long uid, int bot)
	{
		parser.unblacklist(uid, bot);
	}
	
	public void requestResponse(int botIndex, User user, Command cmd, MessageChannel ch)
	{
		parser.requestResponse(botIndex, user, cmd, ch);
	}
	
	public List<CalendarEvent> getRequestedEvents(long guildID, long userID)
	{
		GuildSettings gs = userdata.getGuildSettings(guildID);
		if (gs == null) return null;
		return gs.getSchedule().getRequestedEvents(userID);
	}
	
	public List<CalendarEvent> getTargetEvents(long guildID, long userID)
	{
		GuildSettings gs = userdata.getGuildSettings(guildID);
		if (gs == null) return null;
		return gs.getSchedule().getTargetEvents(userID);
	}
	
	public String getDayOfWeek(int dow)
	{
		String key = "commonstrings.daysofweek.day" + (dow);
		return commonStrings.get(key);
	}
	
	public String getDayOfWeekAbbreviation(int dow)
	{
		String key = "commonstrings.daysofweekabb.day" + (dow);
		return commonStrings.get(key);
	}
	
	public String capitalize(String s)
	{
		String upper = s.toUpperCase();
		return upper.charAt(0) + s.substring(1, s.length());
	}
	
	public String getTimeString(CalendarEvent event, EventType type, TimeZone tz)
	{
		//System.err.println("BotBrain.getTimeString || DEBUG: TimeZone: " + tz);
		String rawstr = commonStrings.get("commonstrings.timestrings." + event.getType().getCommonKey());
		rawstr = rawstr.replace(ReplaceStringType.YEAR.getString(), Integer.toString(event.getYear(tz)));
		rawstr = rawstr.replace(ReplaceStringType.MONTH.getString(), Integer.toString(event.getMonth(tz)));
		rawstr = rawstr.replace(ReplaceStringType.MONTH_NAME.getString(), capitalize(Schedule.getMonthName(event.getMonth(tz)).substring(0, 3)));
		rawstr = rawstr.replace(ReplaceStringType.DAYOFMONTH.getString(), Integer.toString(event.getDayOfMonth(tz)));
		//System.err.println("BotBrain.getTimeString || DEBUG: Day of week: " + event.getDayOfWeek(tz));
		rawstr = rawstr.replace(ReplaceStringType.DAYOFWEEK.getString(), capitalize(getDayOfWeekAbbreviation(event.getDayOfWeek(tz))));
		rawstr = rawstr.replace(ReplaceStringType.TIMEONLY.getString(), String.format("%02d:%02d", event.getHour(tz), event.getMinute(tz)));
		rawstr = rawstr.replace(ReplaceStringType.TIMEZONE.getString(), tz.getDisplayName());
		rawstr = rawstr.replace(ReplaceStringType.NTH.getString(), Language.formatNumber(language, event.getWeekOfMonth(tz)));
		return rawstr;
	}
	
	public String formatEventRecord(CalendarEvent event, JDA jda, TimeZone tz)
	{
		//EventID type name time RUSER TUSERS
		String s = "";
		//GuildSettings gs = userdata.getGuildSettings(guildID);
		//if (gs == null) return null;
		s += Long.toUnsignedString(event.getEventID()) + "\t";
		String eventtype = commonStrings.get("commonstrings.type." + event.getType().getCommonKey());
		s += eventtype + "\t";
		s += event.getEventName() + "\t";
		s += getTimeString(event, event.getType(), tz);
		User ruser = jda.getUserById(event.getRequestingUser());
		s += ruser.getName() + "\t";
		List<Long> tusers = event.getTargetUsers();
		if (tusers == null || tusers.isEmpty())
		{
			s += "[N/A]";
		}
		else
		{
			boolean first = true;
			for (Long l : tusers)
			{
				if(!first) s += ", ";
				User tuser = jda.getUserById(l);
				s += tuser.getName();
			}
				
		}
		
		return s;
	}

	public String formatEventRecord_lite(CalendarEvent event)
	{
		return "[" + Long.toUnsignedString(event.getEventID()) + "]\t" + event.getEventName();
	}
	
	public boolean changeGreetingChannel(long guildID, long channelID)
	{
		//Assumes user admin status has already been checked.
		GuildSettings gs = userdata.getGuildSettings(guildID);
		if (gs == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || BotBrain.changeGreetingChannel || ERROR: Data for guild " + Long.toHexString(guildID) + " could not be found! | " + FileBuffer.formatTimeAmerican(stamp));
			return false;
		}
		gs.setGreetingChannel(channelID);
		
		return true;
	}
	
	public String formatDateString(GregorianCalendar date, boolean includetz)
	{
		DateFormatter df = Language.getDateFormatter(language);
		return df.formatTime(date, false, includetz);
	}
	
	public String formatTimeString_clocktime(int hours, int minutes)
	{
		return String.format("%02d:%02d", hours, minutes);
	}
	
	public String formatTimeString_clocktime(int hours, int minutes, TimeZone tz)
	{
		String time = String.format("%02d:%02d", hours, minutes);
		time += " " + tz.getDisplayName();
		return time;
	}
	
	public String getReminderTimeString_eventtime(long eventTime, TimeZone tz)
	{
		DateFormatter df = Language.getDateFormatter(language);
		if (df == null) return "";
		return df.getTimeRelative(eventTime, tz);
	}
	
	public String getReminderTimeString_timeleft(long eventTime, TimeZone tz)
	{
		DateFormatter df = Language.getDateFormatter(language);
		if (df == null) return "";
		return df.getTimeLeft(eventTime, tz);
	}
	
	public void requestCancellationNotification(CalendarEvent e, boolean instance, long guild)
	{
		parser.command_EventCancellation(e, instance, guild);
	}
	
	public String formatStringList(List<String> strings)
	{
		DateFormatter df = Language.getDateFormatter(language);
		return df.formatStringList(strings);
	}

	public String getEveryoneString()
	{
		DateFormatter df = Language.getDateFormatter(language);
		return df.getEveryoneString();
	}
	
	public String formatNth(int number)
	{
		DateFormatter df = Language.getDateFormatter(language);
		return df.formatSequentialNumber(number);
	}
	
	/* ----- Schedule ----- */
		
	public String cancelEvent(Member m, long eventID)
	{
		//Return the event name if success - null if failure
		GuildSettings gs = userdata.getGuildSettings(m.getGuild().getIdLong());
		if (gs == null) return null;
		Schedule s = gs.getSchedule();
		CalendarEvent e = s.getEvent(eventID);
		if (e == null) return null;
		long eruid = e.getRequestingUser();
		long uid = m.getUser().getIdLong();
		//ActorUser u = gs.getUserBank().getUser(uid);
		//if (!u.isAdmin() && (eruid != uid)) return null; //Can only delete events you created.
		GuildUser u = gs.getUser(uid);
		if(!u.isAdmin() && (eruid != uid)) return null;
		String ename = e.getEventName();
		if (s.cancelEvent(eventID)) return ename;
		return null;
	}
	
	public boolean cancelEvent(long gid, long eid)
	{
		GuildSettings gs = userdata.getGuildSettings(gid);
		if (gs == null) return false;
		Schedule s = gs.getSchedule();
		CalendarEvent e = s.getEvent(eid);
		if (e == null) return false;
		return s.cancelEvent(eid);
	}
	
	public boolean cancelEventInstance(long gid, long eid)
	{
		GuildSettings gs = userdata.getGuildSettings(gid);
		if (gs == null) return false;
		Schedule s = gs.getSchedule();
		return s.cancelEventInstance(eid);
	}
	
	public CalendarEvent getEvent(long gid, long eventID)
	{
		GuildSettings gs = userdata.getGuildSettings(gid);
		if (gs == null) return null;
		Schedule s = gs.getSchedule();
		CalendarEvent e = s.getEvent(eventID);
		return e;
	}
	
	public String getReminderLevelString(EventType t, int level, boolean includePlurals)
	{
		String keystem = "commonstrings.timeframe";
		ReminderTime rt = getReminderTime(t, level);
		if (rt == null) return "NULL";
		if (!rt.isSet()) return "NULL";
		String s = "";
		boolean first = true;
		int y = rt.getYears();
		if (y > 0)
		{
			String timestring = "";
			if (y > 1 || !includePlurals) timestring = this.getCommonString(keystem + ".year_pl");
			else timestring = this.getCommonString(keystem + ".year");
			s += y + " " + timestring;
			first = false;
		}
		int m = rt.getMonths();
		if (m > 0)
		{
			String timestring = "";
			if (m > 1 || !includePlurals) timestring = this.getCommonString(keystem + ".month_pl");
			else timestring = this.getCommonString(keystem + ".month");
			if (!first) s += " ";
			s += m + " " + timestring;
			first = false;
		}
		int w = rt.getWeeks();
		if (w > 0)
		{
			String timestring = "";
			if (w > 1 || !includePlurals) timestring = this.getCommonString(keystem + ".week_pl");
			else timestring = this.getCommonString(keystem + ".week");
			if (!first) s += " ";
			s += w + " " + timestring;
			first = false;
		}
		int d = rt.getDays();
		if (d > 0)
		{
			String timestring = "";
			if (d > 1 || !includePlurals) timestring = this.getCommonString(keystem + ".day_pl");
			else timestring = this.getCommonString(keystem + ".day");
			if (!first) s += " ";
			s += d + " " + timestring;
			first = false;
		}
		int h = rt.getHours();
		if (h > 0)
		{
			String timestring = "";
			if (h > 1 || !includePlurals) timestring = this.getCommonString(keystem + ".hour_pl");
			else timestring = this.getCommonString(keystem + ".hour");
			if (!first) s += " ";
			s += h + " " + timestring;
			first = false;
		}
		int n = rt.getMinutes();
		if (n > 0)
		{
			String timestring = "";
			if (n > 1 || !includePlurals) timestring = this.getCommonString(keystem + ".minute_pl");
			else timestring = this.getCommonString(keystem + ".minute");
			if (!first) s += " ";
			s += n + " " + timestring;
			first = false;
		}
		return s;
	}
	
	public int getReminderCount(EventType t)
	{
		switch(t)
		{
		case BIRTHDAY:
			return Birthday.STD_REMINDER_COUNT;
		case BIWEEKLY:
			return BiweeklyEvent.STD_REMINDER_COUNT;
		case DEADLINE:
			return DeadlineEvent.STD_REMINDER_COUNT;
		case MONTHLYA:
			return MonthlyDOMEvent.STD_REMINDER_COUNT;
		case MONTHLYB:
			return MonthlyDOWEvent.STD_REMINDER_COUNT;
		case ONETIME:
			return OneTimeEvent.STD_REMINDER_COUNT;
		case WEEKLY:
			return WeeklyEvent.STD_REMINDER_COUNT;
		default:
			return 0;
		}
	}
	
	/* ----- Requesting Addition ----- */
	
	//Again, this is to avoid concurrency issues
	
	public class GuildDataAdder extends Thread
	{
		
		public class GuildQueue
		{
			private Deque<Guild> q;
			
			public GuildQueue()
			{
				q = new LinkedList<Guild>();
			}
			
			public synchronized void add(Guild g)
			{
				if (g == null) return;
				q.add(g);
			}
			
			public synchronized Guild pop()
			{
				try {return q.pop();}
				catch (Exception e) {return null;}
			}
			
			public synchronized boolean isEmpty()
			{
				return q.isEmpty();
			}
		}
		
		public class MemberQueue
		{
			private Deque<Member> q;
			
			public MemberQueue()
			{
				q = new LinkedList<Member>();
			}
			
			public synchronized void add(Member m)
			{
				if (m == null) return;
				q.add(m);
			}
			
			public synchronized Member pop()
			{
				try {return q.pop();}
				catch (Exception e) {return null;}
			}
			
			public synchronized boolean isEmpty()
			{
				return q.isEmpty();
			}
		}
		
		private ConcurrentLinkedQueue<User> users;
		
		private boolean kill;
		
		private GuildQueue guilds;
		private MemberQueue members;
		
		public GuildDataAdder()
		{
			guilds = new GuildQueue();
			members = new MemberQueue();
			users = new ConcurrentLinkedQueue<User>();
			kill = false;
			Random r = new Random();
			this.setName("UserDataAdderDaemon_" + Integer.toHexString(r.nextInt()));
			this.setDaemon(true);
		}
		
		public void run()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.GuildDataAdder.run || Thread " + this.getName() + " started!");
			while (!killed())
			{
				while (!guilds.isEmpty())
				{
					Guild g = guilds.pop();
					registerNewGuild(g);
				}
				while (!members.isEmpty())
				{
					Member m = members.pop();
					registerNewMember(m);
				}
				while (!users.isEmpty())
				{
					User u = users.poll();
					registerNewUser(u);
				}
				try 
				{
					Thread.sleep(1000 * 3600);
				} 
				catch (InterruptedException e) 
				{
					Thread.interrupted();
				} //Default to an hour
			}
			System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.GuildDataAdder.run || Thread " + this.getName() + " terminating!");
		}
		
		private synchronized boolean killed()
		{
			return kill;
		}
		
		public synchronized void terminate()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " BotBrain.GuildDataAdder.run || Thread " + this.getName() + " termination requested!");
			kill = true;
			this.interrupt();
		}
		
		public synchronized void interruptMe()
		{
			this.interrupt();
		}
		
		public void requestGuildAddition(Guild g)
		{
			guilds.add(g);
		}
		
		public void requestMemberAddition(Member m)
		{
			members.add(m);
		}
		
		public void requestUserAddition(User u)
		{
			users.add(u);
		}
	}
	
	public synchronized void requestGuildAddition(Guild g)
	{
		userdataThread.requestGuildAddition(g);
		userdataThread.interruptMe();
	}
	
	public synchronized void requestMemberAddition(Member m)
	{
		userdataThread.requestMemberAddition(m);
		userdataThread.interruptMe();
	}
	
	public synchronized void requestUserAddition(User u)
	{
		userdataThread.requestUserAddition(u);
		userdataThread.interruptMe();
	}
	
	public synchronized boolean hasGuild(long id)
	{
		GuildSettings gs = userdata.getGuildSettings(id);
		return (gs != null);
	}
	
	public synchronized boolean hasUser(long uid)
	{
		return (userdata.getUserProfile(uid) != null);
	}
	
	public synchronized boolean hasMember(long gid, long uid)
	{
		GuildSettings gs = this.userdata.getGuildSettings(gid);
		if (gs == null) return false;
		GuildUser u = gs.getUser(uid);
		return (u != null);
	}
	
	/* ----- Session Expiration Monitor ----- */
	
	public class ResetMonitorThread extends Thread
	{
		private boolean killme;
		private OffsetDateTime lastloop;
		
		public ResetMonitorThread()
		{
			super.setName("SessionMonitorDaemon");
			super.setDaemon(true);
			lastloop = OffsetDateTime.now();
		}
		
		public void run()
		{
			lastloop = OffsetDateTime.now();
			try 
			{
				Thread.sleep(1000*60*SESSION_MONITOR_LOOP_MINUTES);
			} 
			catch (InterruptedException e) 
			{
				Thread.interrupted();
			}
			while(!killRequested())
			{
				//Check when the last loop was vs. now
				OffsetDateTime thisloop = OffsetDateTime.now();
				if (thisloop.getDayOfYear() != lastloop.getDayOfYear())
				{
					//We assume a date rollover. Hard reset!
					try 
					{
						forceHardReset();
					} 
					catch (LoginException e) 
					{
						System.err.println(Schedule.getErrorStreamDateMarker() + " BotBarin.ResetMonitorThread.run || Login exception encountered during hard reset! Reset aborted.");
						e.printStackTrace();
					}
				}
				else
				{
					//Just another loop. Soft reset!
					parser.command_SessionCheck();
				}
				lastloop = thisloop;
				//Go back to sleep
				try 
				{
					Thread.sleep(1000*60*SESSION_MONITOR_LOOP_MINUTES);
				} 
				catch (InterruptedException e) 
				{
					Thread.interrupted();
				}
			}
		}
		
		private synchronized boolean killRequested()
		{
			return killme;
		}
		
		public synchronized void kill()
		{
			killme = true;
			this.interrupt();
		}
		
		public synchronized void interruptMe()
		{
			this.interrupt();
		}
		
	}
	
	/* ----- Login Spam Monitor ----- */
	
	public class ResetLoginCountThread extends Athread
	{
		
		public ResetLoginCountThread()
		{
			super.setName("ResetLoginCountDaemon");
			super.setDaemon(true);
			super.setInitialDelay_millis(1000 * BotBrain.LOGIN_RETRY_COUNT_RESET_TIME);
			super.setSleeptime_millis(1000 * BotBrain.LOGIN_RETRY_COUNT_RESET_TIME);
		}

		public void doSomething() 
		{
			for (int i = 1; i < bots.length; i++)
			{
				if (bots[i] != null) bots[i].resetLoginCounter();
			}
		}
		
	}
	
	/* ----- Role Updates ----- */
	
	public void processRoleUpdate(Member member, List<Role> roles, boolean add)
	{
		parser.processRoleChange(member, roles, add);
	}
	
	/* ----- Force Save ----- */
	
	public synchronized void saveUserData() throws IOException
	{
		userdata.forceBackups();
	}

	/* ----- Threads ----- */
	
	public boolean schedulerThreadRunning()
	{
		if (this.shiftManager == null) return false;
		return shiftManager.timerRunning();
	}
	
	public boolean parserThreadRunning()
	{
		if (this.parser == null) return false;
		return parser.parserThreadRunning();
	}

}
