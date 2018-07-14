package waffleoRai_cafebotCore;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.client.exceptions.VerificationLevelException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.CommandQueue;
import waffleoRai_cafebotCommands.Response;
import waffleoRai_cafebotCommands.ResponseQueue;
import waffleoRai_schedulebot.CalendarEvent;
import waffleoRai_schedulebot.EventType;
import waffleoRai_schedulebot.Schedule;

public abstract class AbstractBot implements Bot{
	
	/* ----- Constants ----- */
	
	//Group keys
	public static final String KEY_MAINGROUP_BOTSTRINGS = "botstrings";
	public static final String KEY_GROUP_GENERAL = ".generalbotstrings";
	public static final String KEY_GROUP_BIRTHDAY = ".event_birthday";
	public static final String KEY_GROUP_STATUS = ".gameplayingstatus";
	public static final String KEY_GROUP_USERQUERY = ".userquery";
	public static final String KEY_GROUP_USERMANAGE = ".usermanage";
	public static final String KEY_GROUP_CLEANMSG = ".cleanmessages";
	public static final String KEY_GROUP_EVENTMANAGE = ".eventmanage";
	public static final String KEY_GROUP_GREETINGS = ".greetings";
	
	//Group: General
	public static final String KEY_SOR_ON = ".soron";
	public static final String KEY_SOR_OFF = ".soroff";
	public static final String KEY_SOR_ALLON = ".soronall";
	public static final String KEY_SOR_ALLOFF = ".soroffall";
	public static final String KEY_RESPONSE_GENERALNO = ".userno_general";
	public static final String KEY_BADRESPONSE_TIMEOUT = ".responsetimeout";
	public static final String KEY_BADRESPONSE_REPROMPT = ".responseinvalid";
	public static final String KEY_PARSERBLOCKED = ".parserblocked";
	public static final String KEY_GREET = ".servergreeting";
	public static final String KEY_PINGGREET = ".newmemberping";
	public static final String KEY_NOADMINPERM = ".insufficentPermissions";
	public static final String KEY_BADCMD = ".cannotunderstand";
	public static final String KEY_OTHERBOT = ".theygotit";
	public static final String KEY_WRONGBOT = ".wrongbot";
	public static final String KEY_EVENTHELPSTEM = ".eventhelpmessage";
	public static final String KEY_SORHELPSTEM = ".sorhelpmessage";
	public static final String KEY_HELPSTEM_STANDARD = ".helpmessage";
	public static final String KEY_HELPSTEM_ADMIN = "_admin";
	
	//Group: Greetings
	public static final String KEY_GREET_CHCHAN_SUCCESS = ".confirm_chset_success";
	public static final String KEY_GREET_CHCHAN_FAILURE = ".confirm_chset_failure";
	
	//Group: Event Manage
	public static final String KEY_VIEWEVENTS_ALLUSER = ".viewuserevents";
	public static final String KEY_VIEWEVENTS_REQUSER = ".viewrequestedevents";
	public static final String KEY_CANCELEVENTS_PROMPT = ".canceleventconfirm_prompt";
	public static final String KEY_CANCELEVENTS_SUCCESS = ".canceleventconfirm_success";
	public static final String KEY_CANCELEVENTS_CANCEL = ".canceleventconfirm_cancel";
	public static final String KEY_CANCELEVENTS_FAILURE = ".canceleventconfirm_failure";
	
	//Group: Game playing status
	public static final String KEY_STATUSSTEM_OFF = ".off";
	public static final String KEY_STATUSSTEM_ON = ".on";
	
	//Group: User Query
	public static final String KEY_SAYSOMETHING = ".saysomething";
	
	//Group: User manage
	public static final String KEY_SEEALLTZ = ".seealltz";
	public static final String KEY_GETTZ = ".gettz";
	public static final String KEY_SETTZ_SUCCESS = ".changetz_success";
	public static final String KEY_SETTZ_FAIL = ".changetz_fail";
	
	//Group: (Event)
	public static final String KEY_BADARGS = ".moreargs";
	
	//Group: Clean
	public static final String KEY_USERALL_PROMPT = ".cmme_confirm";
	public static final String KEY_USERALL_SUCCESS = ".cmme_success";
	public static final String KEY_USERALL_FAIL = ".cmme_fail";
	public static final String KEY_USERDAY_PROMPT = ".cmmeday_confirm";
	public static final String KEY_USERDAY_SUCCESS = ".cmmeday_success";
	public static final String KEY_USERDAY_FAIL = ".cmmeday_fail";
	public static final String KEY_ALLDAY_PROMPT = ".cmday_confirm";
	public static final String KEY_ALLDAY_SUCCESS = ".cmday_success";
	public static final String KEY_ALLDAY_FAIL = ".cmday_fail";
	
	public static final int STANDARD_HELP_PARTS = 4;
	public static final int ADMIN_HELP_PARTS = 2;
	
	public static final CharSequence REPLACE_REQUSER = "%r";
	public static final CharSequence REPLACE_REQUSER_MENTION = "%R";
	public static final CharSequence REPLACE_TARGUSER = "%u";
	public static final CharSequence REPLACE_TARGUSER_MENTION = "%U";
	public static final CharSequence REPLACE_CHANNEL = "%c";
	public static final CharSequence REPLACE_CHANNEL_MENTION = "%C";
	
	public static final CharSequence REPLACE_TIME = "%T";
	public static final CharSequence REPLACE_TIME_NOTZ = "%t";
	public static final CharSequence REPLACE_YEAR = "%Y";
	public static final CharSequence REPLACE_MONTH_NAME = "%M";
	public static final CharSequence REPLACE_MONTH = "%m";
	public static final CharSequence REPLACE_DAYOFWEEK = "%D";
	public static final CharSequence REPLACE_DAYOFMONTH = "%d";
	public static final CharSequence REPLACE_NTH = "%I";
	public static final CharSequence REPLACE_TIMEONLY = "%i";
	public static final CharSequence REPLACE_TIMEZONE = "%z";
	
	public static final CharSequence REPLACE_EVENTENTRY = "%E";
	public static final CharSequence REPLACE_EVENTNAME = "%e";
	public static final CharSequence REPLACE_EVENTTYPE = "%t"; //Same as Time without tz!!
	public static final CharSequence REPLACE_EVENTLEVEL = "%l";
	
	public static final CharSequence REPLACE_GENERALNUM = "%n"; 
	
	public static final CharSequence REPLACE_GUILDNAME = "%G";
	
	public static final CharSequence REPLACE_BOTNAME = "%s";
	
	/* ----- Instance Variables ----- */
	
	private int localIndex;
	
	private String sToken;
	private String sVersion;
	private String sInitKey;
	
	protected List<Object> lListeners;
	protected Map<String, String> botStrings;
	protected CommandQueue cmdQueue;
	protected ResponseQueue rspQueue;
	
	private ExecutorThread cmdThread;
	
	private JDABuilder botbuilder;
	private JDA botcore;
	private SelfUser me;
	private boolean on;
	
	protected BotBrain brain;
	
	/* ----- Instantiation ----- */
	
	protected void instantiateListenerList()
	{
		lListeners = new LinkedList<Object>();
	}
	
	/* ----- Threads ----- */
	
	public void start()
	{
		cmdThread = new ExecutorThread(this);
		cmdThread.start();
		rspQueue.startThreads();
	}
	
	public void terminate()
	{
		cmdThread.kill();
		rspQueue.killThreads();
	}
	
	/* ----- Getters ----- */
	
	public int getLocalIndex()
	{
		return localIndex;
	}
	
	public String getToken()
	{
		return sToken;
	}
	
	public String getVersion()
	{
		return sVersion;
	}
	
	public String getXMLKey()
	{
		return sInitKey;
	}
	
	public String getBotString(String key)
	{
		return botStrings.get(key);
	}
	
	public SelfUser getBotUser()
	{
		return me;
	}
	
	public String getBotName()
	{
		return me.getName();
	}
	
	public String getBotDiscriminator()
	{
		return me.getDiscriminator();
	}
	
	public String getBotNickname(Guild g)
	{
		return g.getMember(me).getNickname();
	}
	
	public String getBotStatus()
	{
		return botcore.getPresence().getGame().getName();
	}
	
	public BufferedImage getBotAvatar() throws IOException
	{
		String imgurl = me.getAvatarUrl();
		if (imgurl == null) imgurl = me.getDefaultAvatarUrl();
		URL myimg = new URL(imgurl);
		InputStream is = myimg.openStream();
		BufferedImage img = ImageIO.read(is);
		return img;
	}
	
	public CommandQueue getCommandQueue()
	{
		return cmdQueue;
	}
	
	public ResponseQueue getResponseQueue()
	{
		return rspQueue;
	}
	
	public JDA getJDA()
	{
		return botcore;
	}
	
	/* ----- Setters ----- */
	
	public void setLocalIndex(int i)
	{
		localIndex = i;
	}
	
	public void setToken(String t)
	{
		sToken = t;
	}
	
	public void setVersionString(String v)
	{
		sVersion = v;
	}
	
	public void setXMLInitKey(String k)
	{
		sInitKey = k;
	}
	
	public void addListener(Object l)
	{
		lListeners.add(l);
		if (on) botbuilder.addEventListener(l);
	}
	
	public void removeAllListeners()
	{
		if (lListeners == null) return;
		if (lListeners.isEmpty()) return;
		for (Object l : lListeners)
		{
			botbuilder.removeEventListener(l);
		}
	}
	
	public void setStringMap(Map<String, String> smap)
	{
		botStrings = smap;
	}
	
	public void setBotGameStatus(String status)
	{
		botbuilder.setGame(Game.playing(status));
	}
	
	/* ----- Connection ----- */
	
	private void addListeners(JDABuilder builder)
	{
		if (lListeners == null) return;
		if (lListeners.isEmpty()) return;
		for (Object l : lListeners)
		{
			builder.addEventListener(l);
		}
	}
	
	public void loginAsync() throws LoginException
	{
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(sToken);
		addListeners(builder);
		botcore = builder.buildAsync();
		botbuilder = builder;
		me = botcore.getSelfUser();
		on = true;
		start();
	}
	
	public void loginBlock() throws LoginException, InterruptedException
	{
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(sToken);
		addListeners(builder);
		botcore = builder.buildBlocking();
		botbuilder = builder;
		me = botcore.getSelfUser();
		on = true;
		start();
	}
	
	public void closeJDA()
	{
		on = false;
		terminate();
		botcore.shutdown();
	}

	/* ----- Userdata Management ----- */
	
	public boolean newGuild(Guild g)
	{
		//Blocks until addition detected or timeout...
		int cycles = 0;
		long gid = g.getIdLong();
		brain.requestGuildAddition(g);
		boolean added = (brain.hasGuild(gid));
		while (!added && cycles < 120)
		{
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				System.out.println(Thread.currentThread().getName() + " || AbstractBot.newGuild || Block sleep interrupted...");
				//e.printStackTrace();
			}
			added = (brain.hasGuild(gid));
			cycles++;
		}
		return added;
	}
	
	public boolean newMember(Member m)
	{
		//Blocks until addition detected or timeout...
		int cycles = 0;
		long gid = m.getGuild().getIdLong();
		long uid = m.getUser().getIdLong();
		brain.requestMemberAddition(m);
		boolean added = (brain.hasMember(gid, uid));
		while (!added && cycles < 120)
		{
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				System.out.println(Thread.currentThread().getName() + " || AbstractBot.newMember || Block sleep interrupted...");
				//e.printStackTrace();
			}
			added = (brain.hasMember(gid, uid));
			cycles++;
		}
		return added;
	}
	
	/* ----- Commands : Queue ----- */
	
	public class ExecutorThread extends Thread
	{
		private boolean killMe;
		private AbstractBot bot;
		
		public ExecutorThread(AbstractBot b)
		{
			killMe = false;
			this.setDaemon(true);
			GregorianCalendar g = new GregorianCalendar();
			this.setName("CafebotExecutionThread_" + Long.toHexString(g.getTimeInMillis()));
			bot = b;
		}
		
		public void run()
		{
			while(!killMe())
			{
				//Clear responses
				while (!rspQueue.queueEmpty())
				{
					Response r = rspQueue.popQueue();
					r.execute(bot);
				}
				//Clear commands
				while (!cmdQueue.isEmpty())
				{
					Command c = cmdQueue.popCommand();
					c.execute(bot);
				}
				//Sleep
				try 
				{
					Thread.sleep(10000);
				} 
				catch (InterruptedException e) 
				{
					Thread.interrupted();
				}
			}
		}
		
		private synchronized boolean killMe()
		{
			return killMe;
		}
		
		public synchronized void kill()
		{
			killMe = true;
			this.interrupt();
		}
		
		public synchronized void interruptMe()
		{
			this.interrupt();
		}
		
	}
	
	/* ----- Commands : Basic Functions ----- */
	
		//TODO: MAKE SURE channels are being retrieved from the correct JDA instance!!!!
	
	public void sendMessage(long channelID, String message) throws InsufficientPermissionException, VerificationLevelException, IllegalArgumentException, UnsupportedOperationException
	{
		if (channelID == -1) return;
		MessageChannel channel = botcore.getTextChannelById(channelID);
		if (channel == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Channel " + channelID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		channel.sendMessage(message).queue();
	}
	
	public void sendMessage(MessageChannel channel, String message) throws InsufficientPermissionException, VerificationLevelException, IllegalArgumentException, UnsupportedOperationException  
	{
		channel.sendMessage(message).queue();
	}
	
	public void sendFile(long channelID, String message, File file)
	{
		if (channelID == -1) return;
		MessageChannel channel = botcore.getTextChannelById(channelID);
		if (channel == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Channel " + channelID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		channel.sendFile(file, message).queue();
	}
	
	private void insufficientPermissionsMessage(MessageChannel channel, Member filthycommoner)
	{
		String message = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_NOADMINPERM);
		User u = filthycommoner.getUser();
		String uname = u.getName() + "#" + u.getDiscriminator();
		message = message.replace(REPLACE_REQUSER_MENTION, "@" + uname);
		message = message.replace(REPLACE_REQUSER, u.getName());
		sendMessage(channel, message);
	}
	
	private MessageChannel getChannel(long channelID)
	{
		if (channelID == -1) return null;
		MessageChannel channel = botcore.getTextChannelById(channelID);
		if (channel == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.getChannel || ERROR: Channel " + channelID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		return channel;
	}
	
	public void displayHelp(long channelID, Member mem)
	{
		//Need to determine whether admin
		try
		{
			GuildSettings gs = brain.getUserData().getGuildSettings(mem.getGuild().getIdLong());
			if (gs == null)
			{
				boolean b = newGuild(mem.getGuild());
				if (!b)
				{
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Error retrieving user data: " + mem.getUser().getIdLong() + " | " + FileBuffer.formatTimeAmerican(stamp));
					displayHelp(channelID, false);
					return;
				}
				else gs = brain.getUserData().getGuildSettings(mem.getGuild().getIdLong());
			}
			ActorUser u = gs.getUserBank().getUser(mem.getUser().getIdLong());
			if (u == null)
			{
				boolean b = newMember(mem);
				if (!b)
				{
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Error retrieving user data: " + mem.getUser().getIdLong() + " | " + FileBuffer.formatTimeAmerican(stamp));
					displayHelp(channelID, false);
					return;
				}
				else u = gs.getUserBank().getUser(mem.getUser().getIdLong());
			}
			displayHelp(channelID, u.isAdmin());
		}
		catch (Exception e)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Error retrieving user data: " + mem.getUser().getIdLong() + " | " + FileBuffer.formatTimeAmerican(stamp));
			displayHelp(channelID, false);
		}
	}
	
	public void displayHelp(long channelID, boolean isAdmin)
	{
		if (channelID == -1) return;
		MessageChannel channel = botcore.getTextChannelById(channelID);
		if (channel == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.sendMessage || ERROR: Channel " + channelID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		try
		{
			String helpkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_HELPSTEM_STANDARD;
			if(isAdmin)
			{
				sendMessage(channel, botStrings.get(helpkey + "1" + KEY_HELPSTEM_ADMIN));
				sendMessage(channel, botStrings.get(helpkey + "2"));
				sendMessage(channel, botStrings.get(helpkey + "2" + KEY_HELPSTEM_ADMIN));
				sendMessage(channel, botStrings.get(helpkey + "4"));
			}
			else
			{
				sendMessage(channel, botStrings.get(helpkey + "1"));
				sendMessage(channel, botStrings.get(helpkey + "2"));
				sendMessage(channel, botStrings.get(helpkey + "3"));
				sendMessage(channel, botStrings.get(helpkey + "4"));
			}	
		}
		catch (Exception e)
		{
			//Eat and just don't send.
			e.printStackTrace();
		}
	}

	public void displayEventArgsHelp(long channelID, String username)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_EVENTHELPSTEM;
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		String msg1 = botStrings.get(key + "1");
		msg1 = msg1.replace(REPLACE_REQUSER, username);
		String msg2 = botStrings.get(key + "2");
		String msg3 = botStrings.get(key + "3");
		String msg4 = botStrings.get(key + "4");
		sendMessage(ch, msg1);
		sendMessage(ch, msg2);
		sendMessage(ch, msg3);
		sendMessage(ch, msg4);
	}
	
	public void displaySORHelp(long channelID, String username)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SORHELPSTEM;
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		String msg1 = botStrings.get(key + "1");
		msg1 = msg1.replace(REPLACE_REQUSER, username);
		String msg2 = botStrings.get(key + "2");
		String msg3 = botStrings.get(key + "3");
		sendMessage(ch, msg1);
		sendMessage(ch, msg2);
		sendMessage(ch, msg3);
	}
	
	public void saySomething(long channelID)
	{
		try
		{
			sendMessage(channelID, botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERQUERY + KEY_SAYSOMETHING));
		}
		catch (Exception e)
		{
			//Eat and just don't send.
			e.printStackTrace();
		}
	}
	
	public void warnBlock(long channelID)
	{
		try
		{
			sendMessage(channelID, botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_PARSERBLOCKED));
		}
		catch (Exception e)
		{
			System.err.println("AbstractBot.warnBlock || Message could not be sent.");
			e.printStackTrace();
		}
	}
	
	public void displayBadCommandMessage(long channelID)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_BADCMD;
		String msg = botStrings.get(key);
		sendMessage(channelID, msg);
	}
	
	public void displayCommandHandledMessage(long channelID)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_OTHERBOT;
		String msg = botStrings.get(key);
		sendMessage(channelID, msg);
	}
	
	public void displayWrongbotMessage(long channelID)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_WRONGBOT;
		String msg = botStrings.get(key);
		sendMessage(channelID, msg);
	}
	
	public void displayGeneralCancel(long channelID, String username)
	{
		String key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_RESPONSE_GENERALNO;
		String msg = botStrings.get(key);
		msg = msg.replace(REPLACE_REQUSER, username);
		sendMessage(channelID, msg);
	}
	
	/* ----- Commands : Greetings ----- */
	
	public void setGreetingChannel(long cmdChanID, String gChan, Member m)
	{
		//Check command channel...
		MessageChannel ch = getChannel(cmdChanID);
		if (ch == null) return;
		//Check member permissions...
		long uid = m.getUser().getIdLong();
		long gid = m.getGuild().getIdLong();
		ActorUser u = brain.getUser(gid, uid);
		if (u == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setGreetingChannel || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			u = brain.getUser(gid, uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, m);
			return;
		}
		//Parse greeting channel name...
		boolean success = true;
		List<TextChannel> clist = botcore.getTextChannelsByName(gChan, true);
		if (clist == null || clist.isEmpty()) success = false;
		else
		{
			MessageChannel targChan = clist.get(0);
			success = brain.changeGreetingChannel(gid, targChan.getIdLong());
			if (success)
			{
				String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + KEY_GREET_CHCHAN_SUCCESS;
				String msg = botStrings.get(msgkey);
				msg.replace(REPLACE_CHANNEL_MENTION, "#" + targChan.getName());
				msg.replace(AbstractBot.REPLACE_REQUSER, m.getUser().getName());
				sendMessage(ch, msg);
			}
		}
		if (!success)
		{
			String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + KEY_GREET_CHCHAN_FAILURE;
			String msg = botStrings.get(msgkey);
			msg.replace(AbstractBot.REPLACE_REQUSER, m.getUser().getName());
			sendMessage(ch, msg);
		}
		
	}
	
	public void displayGreetingChannel(Guild g, long cmdChanID)
	{
		long gid = g.getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayGreetingChannel || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		//Get the channel ID
		long gch = gs.getGreetingChannelID();
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".checkchempty");
		if (gch == -1)
		{
			sendMessage(cmdChanID, failmsg);
			return;
		}
		else
		{
			MessageChannel gchan = botcore.getTextChannelById(gch);
			if (gchan == null)
			{
				sendMessage(cmdChanID, failmsg);
				return;
			}
			String goodmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".checkch");
			goodmsg.replace(REPLACE_CHANNEL_MENTION, "#" + gchan.getName());
			sendMessage(cmdChanID, goodmsg);
		}
		
	}
	
	public void greetNewUser(Guild g, Member m)
	{
		GuildSettings gs = brain.getUserData().getGuildSettings(g.getIdLong());
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.greetNewUser || Action failed: Could not retrieve guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(g.getIdLong());
		}
		//Try to add member while we are at it.
		boolean b = newMember(m);
		if (!b)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.greetNewUser || NOTICE: Member data could not be created!");
		}
		if (!gs.greetingsOn()) return;
		long ch = gs.getGreetingChannelID();
		MessageChannel c = botcore.getTextChannelById(ch);
		if (c == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.greetNewUser || Action failed: Could not retrieve greeting channel.");
			return;
		}
		String greetstr = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_GREET);
		User u = m.getUser();
		String uname = u.getName() + "#" + u.getDiscriminator();
		greetstr = greetstr.replace(REPLACE_TARGUSER_MENTION, "@" + uname);
		greetstr = greetstr.replace(REPLACE_TARGUSER, u.getName());
		greetstr = greetstr.replace(REPLACE_GUILDNAME, g.getName());
		greetstr = greetstr.replace(REPLACE_BOTNAME, me.getName());
		sendMessage(c, greetstr);
	}
	
	public void pingUserArrival(Guild g, Member m)
	{
		GuildSettings gs = brain.getUserData().getGuildSettings(g.getIdLong());
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.pingUserArrival || Action failed: Could not retrieve guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(g.getIdLong());
		}
		List<ActorUser> reqlist = gs.getAllGreetingPingUsers();
		String greetstr = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_PINGGREET);
		User u = m.getUser();
		for (ActorUser admin : reqlist)
		{
			long ch = admin.getPingGreetingsChannel();
			if (ch < 0) continue;
			MessageChannel c = botcore.getTextChannelById(ch);
			if (c == null)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.greetNewUser || Action failed: Could not retrieve greeting ping channel for user " + admin.getUID());
				continue;
			}
			//Generate new message with placeholders replaced.
			User r = g.getMemberById(admin.getUID()).getUser();
			String rname = r.getName() + "#" + r.getDiscriminator();
			String pstr = greetstr.replace(REPLACE_TARGUSER, u.getName());
			pstr = pstr.replace(REPLACE_REQUSER_MENTION, "@" + rname);
			sendMessage(c, pstr);
		}
	}
	
	public void setGreetings(long channelID, Member mem, boolean on)
	{
		//Member needed for permission check.
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetfail");
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setGreetings || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, failmsg);
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setGreetings || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, failmsg);
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		
		gs.setGreetings(on);
		String msg = "";
		if (on) msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreeton");
		else msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetoff");
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		sendMessage(ch, msg);
	}
	
	public void setUserPingGreetings(long channelID, Member mem, boolean on, String targetChan)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingfail");
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, failmsg);
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, failmsg);
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		
		List<TextChannel> tchan = botcore.getTextChannelsByName(targetChan, true);
		
		if (tchan == null || tchan.isEmpty())
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Channel " + targetChan + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			sendMessage(ch, failmsg);
			return;	
		}
		
		MessageChannel chan = tchan.get(0);
		u.setGreetingPings(on);
		u.setGreetingPingsChannel(chan.getIdLong());
		String msg = "";
		if (on) msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingon");
		else msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingoff");
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + chan.getName());
		sendMessage(ch, msg);
	}
	
	public void setUserPingGreetings(long channelID, Member mem, boolean on)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingfail");
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, failmsg);
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				sendMessage(ch, failmsg);
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		
		MessageChannel pchan = botcore.getTextChannelById(u.getPingGreetingsChannel());
		if (pchan == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserPingGreetings || ERROR: Channel " + Long.toHexString(u.getPingGreetingsChannel()) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			sendMessage(ch, failmsg);
			return;	
		}
		
		u.setGreetingPings(on);
		String msg = "";
		if (on) msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingon");
		else msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GREETINGS + ".setgreetpingoff");
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + pchan.getName());
		sendMessage(ch, msg);
	}
	
	/* ----- Commands : Change Status ----- */
	
	public void changeShiftStatus(int month, int pos)
	{
		String nstat = null;
		if (pos < 0) nstat = this.botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_STATUS + KEY_STATUSSTEM_OFF + month);
		else nstat = this.botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_STATUS + KEY_STATUSSTEM_ON + pos);
		setBotGameStatus(nstat);
	}
	
	/* ----- User Management ----- */
	
	private void displayTZListGetFail(long chid)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_SEEALLTZ + "fail");
		sendMessage(chid, msg);
	}
	
	private void displayTZListGetSuccess(long chid, String userName, File f)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_SEEALLTZ);
		msg = msg.replace(REPLACE_REQUSER, userName);
		sendFile(chid, msg, f);
	}
	
	public void postTimezoneList(long channelID, String userName)
	{
		//Attempt to get the file off disk
		String tzfilepath = brain.getTimezoneListPath();
		if (!FileBuffer.fileExists(tzfilepath))
		{
			displayTZListGetFail(channelID);
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.postTimezoneList || File " + tzfilepath + " does not appear to exist...");
			return;
		}
		try
		{
			File f = new File(tzfilepath);
			displayTZListGetSuccess(channelID, userName, f);
		}
		catch(Exception e)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.postTimezoneList || Exception creating file object from path " + tzfilepath);
			e.printStackTrace();
			displayTZListGetFail(channelID);
			return;
		}
	}
	
	public void getUserTimezone(long channelID, Member user)
	{
		if (user == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.getUserTimezone || ERROR: Passed user was null! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		GuildSettings gs = brain.getUserData().getGuildSettings(user.getGuild().getIdLong());
		if (gs == null)
		{
			boolean b = newGuild(user.getGuild());
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.getUserTimezone || Action failed: Could not retrieve/create guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(user.getGuild().getIdLong());
		}
		ActorUser uset = gs.getUserBank().getUser(user.getUser().getIdLong());
		if (uset == null)
		{
			boolean b = newMember(user);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.getUserTimezone || Action failed: Could not retrieve/create user data.");
				return;
			}
			uset = gs.getUserBank().getUser(user.getUser().getIdLong());
		}
		TimeZone tz = uset.getTimeZone();
		GregorianCalendar usernow = new GregorianCalendar(tz);
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_GETTZ;
		String msg = botStrings.get(msgkey);
		//Username
		msg.replace(REPLACE_REQUSER, user.getUser().getName());
		//Timezone
		msg.replace(REPLACE_TIMEZONE, tz.getID());
		//Time
		Language l = brain.getLanguage();
		msg.replace(REPLACE_TIME_NOTZ, Language.formatDate(l, usernow, false, false));
		sendMessage(channelID, msg);
	}

	public void setUserTimezone(long channelID, Member user, String tzcode)
	{
		if (user == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserTimezone || ERROR: Passed user was null! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		//Get timezone
		TimeZone tz = TimeZone.getTimeZone(tzcode);
		boolean failed = false;
		if (tz != null)
		{
			//Success
			GuildSettings gs = brain.getUserData().getGuildSettings(user.getGuild().getIdLong());
			if (gs == null)
			{
				boolean b = newGuild(user.getGuild());
				if (!b)
				{
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserTimezone || Action failed: Could not retrieve/create guild data.");
					return;
				}
				gs = brain.getUserData().getGuildSettings(user.getGuild().getIdLong());
			}
			ActorUser uset = gs.getUserBank().getUser(user.getUser().getIdLong());
			if (uset == null)
			{
				boolean b = newMember(user);
				if (!b)
				{
					System.err.println(Thread.currentThread().getName() + " || AbstractBot.setUserTimezone || Action failed: Could not retrieve/create user data.");
					return;
				}
				uset = gs.getUserBank().getUser(user.getUser().getIdLong());
			}
			uset.setTimeZone(tzcode);
			if (!failed)
			{
				String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_SETTZ_SUCCESS;
				String msg = botStrings.get(msgkey);
				GregorianCalendar usernow = new GregorianCalendar(tz);
				//Username
				msg.replace(REPLACE_REQUSER, user.getUser().getName());
				//Timezone
				msg.replace(REPLACE_TIMEZONE, tz.getID());
				//Time
				Language l = brain.getLanguage();
				msg.replace(REPLACE_TIME_NOTZ, Language.formatDate(l, usernow, false, false));
				sendMessage(channelID, msg);
				return;
			}
		}
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_USERMANAGE + KEY_SETTZ_FAIL;
		String msg = botStrings.get(msgkey);
		//Username
		msg.replace(REPLACE_REQUSER, user.getUser().getName());
		sendMessage(channelID, msg);
	}
	
	public void sorAllOff(long channelID, Member user)
	{
		long gid = user.getGuild().getIdLong();
		long uid = user.getUser().getIdLong();
		
		//Get user object
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(user.getGuild());
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sorAllOff || Action failed: Could not retrieve/create guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(user);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sorAllOff || Action failed: Could not retrieve/create member data.");
				return;
			}
			u = gs.getUserBank().getUser(uid);
		}
		
		//Turn off reminders
		u.turnOffAllReminders();
		
		//Print message
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SOR_ALLOFF;
		String msg = botStrings.get(msgkey);
		msg = msg.replace(REPLACE_REQUSER, user.getUser().getName());
		sendMessage(channelID, msg);
	}
	
	public void sorAllOn(long channelID, Member user)
	{
		long gid = user.getGuild().getIdLong();
		long uid = user.getUser().getIdLong();
		
		//Get user object
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(user.getGuild());
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sorAllOn || Action failed: Could not retrieve/create guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(user);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sorAllOn || Action failed: Could not retrieve/create member data.");
				return;
			}
			u = gs.getUserBank().getUser(uid);
		}
		
		//Turn off reminders
		u.turnOnAllReminders();
		
		//Print message
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SOR_ALLON;
		String msg = botStrings.get(msgkey);
		msg = msg.replace(REPLACE_REQUSER, user.getUser().getName());
		sendMessage(channelID, msg);
	}
	
	public void sor(long channelID, Member user, EventType type, int level)
	{
		long gid = user.getGuild().getIdLong();
		long uid = user.getUser().getIdLong();
		
		//Get user object
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(user.getGuild());
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sor || Action failed: Could not retrieve/create guild data.");
				return;
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(user);
			if (!b)
			{
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.sor || Action failed: Could not retrieve/create member data.");
				return;
			}
			u = gs.getUserBank().getUser(uid);
		}
		
		//Detect current setting & set to the opposite
		boolean state = u.reminderOn(type, level);
		u.setReminder(type, level, !state);
		
		//Print message
		String key = "";
		if (state) key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SOR_OFF;
		else key = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_SOR_ON;
		String msg = botStrings.get(key);
		msg = msg.replace(REPLACE_REQUSER, user.getUser().getName());
		msg = msg.replace(REPLACE_EVENTTYPE, brain.getCommonString(type.getCommonKey()));
		msg = msg.replace(REPLACE_EVENTLEVEL, brain.getReminderLevelString(type, level, false));
		sendMessage(channelID, msg);
	}
	
	/* ----- Commands : Events ----- */
	
		// ---- Birthday
	
	public void wishBirthday(long uid, long gid, boolean coinflip)
	{
		String bdaystr = null;
		String strkey = null;
		if (coinflip) strkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".birthdaywish1";
		else strkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".birthdaywish2";
		bdaystr = botStrings.get(strkey);
		if (bdaystr == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.wishBirthday || ERROR: String \"" + strkey + "\" could not be found!");
			GregorianCalendar stamp = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
			System.err.println("\tTimestamp: " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		Guild g = botcore.getGuildById(gid);
		if (g == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.wishBirthday || ERROR: Guild \"" + gid + "\" could not be found!");
			GregorianCalendar stamp = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
			System.err.println("\tTimestamp: " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		User u = g.getMemberById(uid).getUser();
		if (u == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.wishBirthday || ERROR: User \"" + uid + "\" could not be found!");
			GregorianCalendar stamp = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
			System.err.println("\tTimestamp: " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		GuildMap guildmap = brain.getUserData();
		GuildSettings gs = guildmap.getGuildSettings(gid);
		long cid = gs.getBirthdayChannelID();
		MessageChannel c = g.getTextChannelById(cid);
		if (c == null)
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.wishBirthday || ERROR: Channel \"" + cid + "\" could not be found!");
			GregorianCalendar stamp = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
			System.err.println("\tTimestamp: " + FileBuffer.formatTimeAmerican(stamp));
			return;
		}
		
		String uname = u.getName() + "#" + u.getDiscriminator();
		bdaystr = bdaystr.replace(REPLACE_TARGUSER_MENTION, "@" + uname);
		bdaystr = bdaystr.replace(REPLACE_TARGUSER, u.getName());
		sendMessage(c, bdaystr);
	}
	
	private void birthdayChSetFailMessage(MessageChannel cmdChan, String errorDet, String username)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".confirm_chset_failure");
		msg.replace(AbstractBot.REPLACE_REQUSER, username);
		msg += "\nReason:\n" + errorDet;
		sendMessage(cmdChan, msg);
	}
	
	private void birthdayChSetSuccessMessage(MessageChannel cmdChan, MessageChannel targChan, String username)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".confirm_chset_success");
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + targChan.getName());
		msg.replace(AbstractBot.REPLACE_REQUSER, username);
		sendMessage(cmdChan, msg);
	}
	
	public void setBirthdayChannel(long cmdChanID, String bdayChan, Member m)
	{
		if (cmdChanID == -1) return;
		MessageChannel cmdChan = botcore.getTextChannelById(cmdChanID);
		if (cmdChan == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBirthdayChannel || ERROR: Channel " + cmdChanID + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
		}
		if (bdayChan == null || bdayChan.isEmpty())
		{
			birthdayChSetFailMessage(cmdChan, "Invalid channel name argument.", m.getUser().getName());
			return;
		}
		long guildID = m.getGuild().getIdLong();
		GuildMap guildmap = brain.getUserData();
		GuildSettings gs = guildmap.getGuildSettings(guildID);
		Guild g = botcore.getGuildById(guildID);
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b){
				birthdayChSetFailMessage(cmdChan, "Guild link could not be found.", m.getUser().getName());
				return;
			}
			gs = guildmap.getGuildSettings(guildID);
		}
		
		//Check to see if requesting user is an admin. If not, let them know they do not have permission to do this.
		long userID = m.getUser().getIdLong();
		ActorUser u = gs.getUserBank().getUser(userID);
		if (u == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				birthdayChSetFailMessage(cmdChan, "User data could not be found.", m.getUser().getName());
				return;
			}
			u = gs.getUserBank().getUser(userID);
		}
		if (!u.isAdmin())
		{
			//Get user object so the bot can refer to person by name.
			//Member m = g.getMemberById(userID);
			insufficientPermissionsMessage(cmdChan, m);
			return;
		}
		
		List<TextChannel> alltchan = g.getTextChannelsByName(bdayChan, false);
		if (alltchan == null || alltchan.isEmpty())
		{
			birthdayChSetFailMessage(cmdChan, "No channels by the name " + bdayChan + " could be found in this guild.", m.getUser().getName());
			return;
		}
		//Otherwise just uses the first one.
		MessageChannel bchan = alltchan.get(0);
		gs.setBirthdayChannel(bchan);
		birthdayChSetSuccessMessage(cmdChan, bchan, m.getUser().getName());
	}

	public void displayBirthdayChannel(Guild g, long cmdChanID)
	{
		long gid = g.getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(g);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayBirthdayChannel || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		//Get the channel ID
		long bch = gs.getBirthdayChannelID();
		String failmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".checkchempty");
		if (bch == -1)
		{
			sendMessage(cmdChanID, failmsg);
			return;
		}
		else
		{
			MessageChannel bchan = botcore.getTextChannelById(bch);
			if (bchan == null)
			{
				sendMessage(cmdChanID, failmsg);
				return;
			}
			String goodmsg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".checkch");
			goodmsg.replace(REPLACE_CHANNEL_MENTION, "#" + bchan.getName());
			sendMessage(cmdChanID, goodmsg);
		}
		
	}
	
	private void birthdaySetFailArgsMessage(MessageChannel cmdChan)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".moreargs");
		sendMessage(cmdChan, msg);
	}
	
	private void birthdaySetFailGeneralMessage(MessageChannel cmdChan, String reason)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".confirm_failure");
		msg += "\nReason:\n" + reason;
		sendMessage(cmdChan, msg);
	}
	
	private void birthdaySetSuccessMessage(MessageChannel cmdChan, Member m, int month, int day)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + ".confirm_success");
		msg = msg.replace(REPLACE_TARGUSER, m.getUser().getName());
		msg = msg.replace(REPLACE_MONTH_NAME, Schedule.getMonthName(month));
		msg = msg.replace(REPLACE_DAYOFMONTH, Integer.toString(day));
		sendMessage(cmdChan, msg);
	}
	
	public void setBirthday(Member m, int month, int day, long chID)
	{
		if (m == null){
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBirthday || command failed: member object not found");
			return;
		}
		if (chID == -1) return;
		
		MessageChannel cmdChan = botcore.getTextChannelById(chID);
		if (cmdChan == null) 
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBirthday || command failed: channel " + chID + " could not be retrieved...");
			return;
		}
		if (month < 0 || month > 11)
		{
			birthdaySetFailArgsMessage(cmdChan);
			return;
		}
		if (day < 1 || day > Schedule.MONTHDAYS[month])
		{
			birthdaySetFailArgsMessage(cmdChan);
			return;
		}
		long gid = m.getGuild().getIdLong();
		GuildMap guildmap = brain.getUserData();
		GuildSettings gs = guildmap.getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(m.getGuild());
			if (!b)
			{
				birthdaySetFailGeneralMessage(cmdChan, "Guild link could not be found.");
				return;
			}
			gs = guildmap.getGuildSettings(gid);
		}
		Schedule s = gs.getSchedule();
		if (s == null)
		{
			birthdaySetFailGeneralMessage(cmdChan, "Guild schedule is null!");
			return;
		}
		s.addBirthday(m.getUser().getIdLong(), month, day, m.getUser().getName());
		birthdaySetSuccessMessage(cmdChan, m, month, day);
		
	}

	public void insufficientArgsMessage_birthday(long chID)
	{
		MessageChannel cmdChan = botcore.getTextChannelById(chID);
		if (cmdChan == null) 
		{
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.setBirthday || command failed: channel " + chID + " could not be retrieved...");
			return;
		}
		//Get string
		String msg = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_BIRTHDAY + KEY_BADARGS;
		sendMessage(cmdChan, msg);
	}
	
		// ---- Manage
	
	public void displayAllUserEvents(long chID, Member m)
	{
		MessageChannel ch = getChannel(chID);
		if (ch == null) return;
		//Get guild
		long gid = m.getGuild().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			//Put in a request to create new guild data object...
			boolean b = newGuild(m.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayAllUserEvents || ERROR: Data for guild " + gid + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		long uid = m.getUser().getIdLong();
		ActorUser user = gs.getUserBank().getUser(uid);
		if (user == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayAllUserEvents || ERROR: Data for member " + m.getUser().getIdLong() + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			user = gs.getUserBank().getUser(uid);
		}
		TimeZone tz = user.getTimeZone();
		List<CalendarEvent> revents = brain.getRequestedEvents(gid, uid);
		List<CalendarEvent> tevents = brain.getTargetEvents(gid, uid);
		//Revent list
		String reventlist = "";
		if (revents == null || revents.isEmpty()) reventlist = "[" + brain.getCommonString("commonstrings.misc.empty") + "]";
		else
		{
			for (CalendarEvent e : revents) reventlist += brain.formatEventRecord(e, botcore, tz) + "\n";
		}
		
		//Tevent list
		String teventlist = "";
		if (tevents == null || tevents.isEmpty()) teventlist = "[" + brain.getCommonString("commonstrings.misc.empty") + "]";
		else
		{
			for (CalendarEvent e : tevents) teventlist += brain.formatEventRecord(e, botcore, tz) + "\n";
		}
		
		//Get strings
		String msg1 = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_VIEWEVENTS_ALLUSER + "1");
		String msg2 = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_VIEWEVENTS_ALLUSER + "2");
		//Substitute (look for %E1 for requested and %E2 for target)
		msg1.replace(REPLACE_EVENTENTRY, reventlist);
		msg2.replace(REPLACE_EVENTENTRY, teventlist);
		sendMessage(ch, msg1);
		sendMessage(ch, msg2);
	}
	
	public void cancelEvent_prompt(long chID, Member m, long eventID, Command cmd)
	{
		//Need member to see if has permission to delete event
		//Get event...
		long gid = m.getGuild().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(m.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_prompt || ERROR: Data for guild " + Long.toHexString(gid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		Schedule s = gs.getSchedule();
		if (s == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_prompt || ERROR: Guild schedule could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			return;	
		}
		CalendarEvent e = s.getEvent(eventID);
		if (e == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_prompt || ERROR: Data for event " + Long.toHexString(eventID) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			return;	
		}
		//Check permission...
		long uid = m.getUser().getIdLong();
		ActorUser user = gs.getUserBank().getUser(uid);
		if (user == null)
		{
			boolean b = newMember(m);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_prompt || ERROR: Data for member " + Long.toHexString(uid) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			user = gs.getUserBank().getUser(uid);
		}
		long eruid = e.getRequestingUser();
		MessageChannel ch = getChannel(chID);
		if (ch == null) return;
		if (!user.isAdmin() && (eruid != uid))
		{
			//PERMISSION DENIED, SCRUB
			insufficientPermissionsMessage(ch, m);
			return;
		}
		
		//Otherwise prompt
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_CANCELEVENTS_PROMPT);
		//Substitute event ID, name, type, and time
		msg.replace(REPLACE_GENERALNUM, Long.toUnsignedString(eventID));
		msg.replace(REPLACE_EVENTTYPE, brain.getEventtypeString(e.getType()));
		msg.replace(REPLACE_EVENTNAME, e.getEventName());
		msg.replace(REPLACE_TIME, brain.getTimeString(e, e.getType(), user.getTimeZone()));
		brain.requestResponse(localIndex, m.getUser(), cmd, ch);
		sendMessage(ch, msg);
	}
	
	public void cancelEvent_cancel(long chID, long guildID, long eventID)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_CANCELEVENTS_CANCEL);
		CalendarEvent e = brain.getEvent(guildID, eventID);
		if (e == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_cancel || ERROR: Data for event " + Long.toHexString(eventID) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			return;	
		}
		msg.replace(REPLACE_EVENTNAME, e.getEventName());
		sendMessage(chID, msg);
	}
	
	public void cancelEvent(long chID, long guildID, long eventID)
	{
		String msgs = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_CANCELEVENTS_SUCCESS);
		String msgf = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_EVENTMANAGE + KEY_CANCELEVENTS_FAILURE);
		CalendarEvent e = brain.getEvent(guildID, eventID);
		msgf.replace(REPLACE_GENERALNUM, Long.toUnsignedString(eventID));
		if (e == null)
		{
			GregorianCalendar stamp = new GregorianCalendar();
			System.err.println(Thread.currentThread().getName() + " || AbstractBot.cancelEvent_cancel || ERROR: Data for event " + Long.toHexString(eventID) + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
			sendMessage(chID, msgf);
			return;	
		}
		msgs.replace(REPLACE_GENERALNUM, Long.toUnsignedString(eventID));
		msgs.replace(AbstractBot.REPLACE_EVENTNAME, e.getEventName());
		boolean b = brain.cancelEvent(guildID, eventID);
		if (b) sendMessage(chID, msgs);
		else sendMessage(chID, msgf);
	}
	
	/* ----- Cleaning ----- */
	
	public List<Message> getAllMessages(MessageChannel ch, Member mem, boolean dayonly, boolean useronly)
	{
		List<Message> allChannelMessages = ch.getHistory().getRetrievedHistory();
		List<Message> newlist = new LinkedList<Message>();
		List<Message> list = new LinkedList<Message>();
		if (dayonly)
		{
			//Clear out anything older than 24 hours
			OffsetDateTime limit = OffsetDateTime.now();
			limit.minusDays(1);
			for (Message m : allChannelMessages)
			{
				OffsetDateTime mtime = m.getCreationTime();
				//Compare time
				if (mtime.isBefore(limit)) break;
				else newlist.add(m);
			}
		}
		else newlist.addAll(allChannelMessages);
		if (useronly)
		{
			//Toss anything not by user
			long myuid = mem.getUser().getIdLong();
			for (Message m : newlist)
			{
				if (m.getAuthor().getIdLong() != myuid) continue;
				else list.add(m);
			}
		}
		else list.addAll(newlist);
		return list;
	}
	
	public boolean deleteMessages(List<Message> mlist)
	{
		if (mlist == null) return true;
		if (mlist.isEmpty()) return true;
		boolean cleared = true;
		for (Message m : mlist)
		{
			try
			{
				m.delete();
				//AuditableRestAction<Void> a = m.delete();
			}
			catch (Exception e)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				e.printStackTrace();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.deleteMessages || Message could not be deleted! Stamp: " + FileBuffer.formatTimeAmerican(stamp));
				cleared = false;
			}
		}
		return cleared;
	}
	
	public void cleanChannelMessages_allDay_prompt(long channelID, Member mem, Command cmd)
	{
		//Need to check for admin priv.
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		long gid = mem.getGuild().getIdLong();
		long uid = mem.getUser().getIdLong();
		GuildSettings gs = brain.getUserData().getGuildSettings(gid);
		if (gs == null)
		{
			boolean b = newGuild(mem.getGuild());
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.cleanChannelMessages_allDay_prompt || ERROR: Data for guild " + gid + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			gs = brain.getUserData().getGuildSettings(gid);
		}
		ActorUser u = gs.getUserBank().getUser(uid);
		if (u == null)
		{
			boolean b = newMember(mem);
			if (!b)
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || AbstractBot.displayAllUserEvents || ERROR: Data for member " + uid + " could not be retrieved! | " + FileBuffer.formatTimeAmerican(stamp));
				return;	
			}
			u = gs.getUserBank().getUser(uid);
		}
		if (!u.isAdmin())
		{
			this.insufficientPermissionsMessage(ch, mem);
			return;
		}
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_ALLDAY_PROMPT;
		String msg = botStrings.get(msgkey);
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + ch.getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, msg);
	}
	
	public void cleanChannelMessages_allUser_prompt(long channelID, Member mem, Command cmd)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERALL_PROMPT;
		String msg = botStrings.get(msgkey);
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + ch.getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, msg);
	}
	
	public void cleanChannelMessages_allUserDay_prompt(long channelID, Member mem, Command cmd)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		rspQueue.requestResponse(cmd, mem.getUser(), ch);
		String msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERDAY_PROMPT;
		String msg = botStrings.get(msgkey);
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + ch.getName());
		brain.blacklist(mem.getUser().getIdLong(), localIndex);
		sendMessage(ch, msg);
	}
	
	public void cleanChannelMessages_allDay(long channelID, Member mem)
	{
		//Assumed admin confirmation already complete.
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, true, false);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_ALLDAY_SUCCESS;
		else msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_ALLDAY_FAIL;
		String msg = botStrings.get(msgkey);
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + ch.getName());
		sendMessage(ch, msg);
	}
	
	public void cleanChannelMessages_allUser(long channelID, Member mem)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, false, true);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERALL_SUCCESS;
		else msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERALL_FAIL;
		String msg = botStrings.get(msgkey);
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + ch.getName());
		sendMessage(ch, msg);
	}
	
	public void cleanChannelMessages_allUserDay(long channelID, Member mem)
	{
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		List<Message> messages = getAllMessages(ch, mem, true, true);
		boolean s = deleteMessages(messages);
		String msgkey = "";
		if (s) msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERDAY_SUCCESS;
		else msgkey = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_CLEANMSG + KEY_USERDAY_FAIL;
		String msg = botStrings.get(msgkey);
		msg.replace(REPLACE_REQUSER, mem.getUser().getName());
		msg.replace(REPLACE_CHANNEL_MENTION, "#" + ch.getName());
		sendMessage(ch, msg);
	}
	
	/* ----- User Response Handling ----- */
	
	public void timeoutPrompt(long channelID, long userID)
	{
		brain.unblacklist(userID, getLocalIndex());
		displayTimeoutMessage(channelID);
	}
	
	private void displayTimeoutMessage(long channelID)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_BADRESPONSE_TIMEOUT);
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		sendMessage(ch, msg);
	}
	
	public void displayRerequestMessage(long channelID)
	{
		String msg = botStrings.get(KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_BADRESPONSE_REPROMPT);
		MessageChannel ch = getChannel(channelID);
		if (ch == null) return;
		sendMessage(ch, msg);
	}
	
}
