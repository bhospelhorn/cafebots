package waffleoRai_cafebotCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import waffleoRai_Utils.FileBuffer;

import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_cafebotCommands.MIDQueue;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotRoles.ActingManager;
import waffleoRai_schedulebot.Schedule;

public class GuildSettings {
	
	/* ----- Constants ----- */
	
	public static final String INIT_FILENAME = "gsettings.ini";
	
	public static final String INIT_GUILDSDIR = "guilds";
	
	public static final String INIT_USERDIR = "members";
	public static final String INIT_SCHEDULEDIR = "events";
	public static final String INIT_ROLEDIR = "casting";
	
	public static final int BACKUP_THREAD_SLEEP_MINUTES = 60;
	
	public static final int REMINDER_COUNT = 5;
	
	public static final int MAX_SAVED_COMMANDS = 50000;
	
	/* ----- Static Variables ----- */
	
	private static String userDataDirectory;
	
	/* ----- Instance Variables ----- */
	
	private long guild;
	
	private long birthdayChannel;
	private long greetingChannel;
	private boolean greetingsOn;
	private boolean farewellsOn;
	
	private boolean auto_cmd_clear;
	private MIDQueue recentCommands;
	
	private UserBank users;
	private Schedule schedule;
	private ActingManager roleManager;
	
	private List<Long> adminRoles;
	
	private String dataDir;
	private BackupThread backup;
	
	/* ----- Construction/Parsing ----- */
	
	public GuildSettings(Guild g, ParseCore parser, ConcurrentHashMap<Long, ActorUser> actorBank)
	{
		//Build anew.
		guild = g.getIdLong();
		users = new UserBank();
		roleManager = new ActingManager();
		birthdayChannel = g.getDefaultChannel().getIdLong();
		greetingChannel = g.getDefaultChannel().getIdLong();
		greetingsOn = true;
		farewellsOn = true;
		schedule = new Schedule(users, guild, parser);
		//Get all the members in the guild currently and add to user bank
		List<Member> memlist = g.getMembers();
		for (Member m : memlist)
		{
			long uid = m.getUser().getIdLong();
			ActorUser au = actorBank.get(uid);
			GuildUser gu = new GuildUser(m, au);
			if (au == null) actorBank.putIfAbsent(uid, gu.getUserProfile());
		}
		dataDir = GuildSettings.getGuildDirectoryPath(g.getIdLong());
		adminRoles = new LinkedList<Long>();
		//recentCommands = new LongQueue(MAX_SAVED_COMMANDS);
		recentCommands = new MIDQueue(MAX_SAVED_COMMANDS);
		auto_cmd_clear = false;
	}
	
	public GuildSettings(String gdirPath, ParseCore parser, ConcurrentHashMap<Long, ActorUser> actorBank) throws IOException, UnsupportedFileTypeException
	{
		//Load from a file.
		//Find the main guild settings file.
		dataDir = gdirPath;
		FileReader fr = new FileReader(gdirPath + File.separator + INIT_FILENAME);
		BufferedReader br = new BufferedReader(fr);
		
		//First line is the guild ID
		try
		{
			long gid = Long.parseUnsignedLong(br.readLine());
			//guild = jda.getGuildById(gid);
			guild = gid;
			//System.err.println("GuildSettings.<init> || Debug - Line 1 (Guild ID): " + gid);
			
			//Bday channel
			long bdaycid = Long.parseUnsignedLong(br.readLine());
			//birthdayChannel = guild.getTextChannelById(bdaycid);
			birthdayChannel = bdaycid;
			//System.err.println("GuildSettings.<init> || Debug - Line 2 (BDAY Chan ID): " + bdaycid);
			
			//Greeting channel
			long greetcid = Long.parseUnsignedLong(br.readLine());
			//greetingChannel = guild.getTextChannelById(greetcid);
			greetingChannel = greetcid;
			//System.err.println("GuildSettings.<init> || Debug - Line 3 (Greet Chan ID): " + greetcid);
			
			String line = br.readLine();
			if (line.equals("1")) greetingsOn = true;
			else greetingsOn = false;
			line = br.readLine();
			if (line.equals("1")) farewellsOn = true;
			else farewellsOn = false;
			line = br.readLine();
			if (line.equals("1")) auto_cmd_clear = true;
			else auto_cmd_clear = false;
			
			//Comma delimited list of admin roles
			adminRoles = new LinkedList<Long>();
			String allroles = br.readLine();
			if (allroles != null && !allroles.isEmpty())
			{
				String[] roles = allroles.split(",");
				for (String s : roles) adminRoles.add(Long.parseUnsignedLong(s));
			}
			
			//Comma delimited list of recent command message IDs
			//recentCommands = new LongQueue(MAX_SAVED_COMMANDS);
			recentCommands = new MIDQueue(MAX_SAVED_COMMANDS);
			String reccom = br.readLine();
			if (reccom != null && !reccom.isEmpty())
			{
				String[] cmds = reccom.split(",");
				for (String s : cmds){
					String[] ids = s.split(":");
					long cid = Long.parseUnsignedLong(ids[0]);
					long mid = Long.parseUnsignedLong(ids[1]);
					recentCommands.add(new MessageID(mid, cid));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			br.close();
			fr.close();
			throw new FileBuffer.UnsupportedFileTypeException();
		}
		
		br.close();
		fr.close();
		
		loadUserInfo(gdirPath, actorBank);
		loadSchedule(gdirPath, parser);
		loadRoles(gdirPath);
		
		
	}
	
	private void loadUserInfo(String gdirPath, ConcurrentHashMap<Long, ActorUser> actorBank) throws IOException, UnsupportedFileTypeException
	{
		users = new UserBank();
		String userdir = gdirPath + File.separator + INIT_USERDIR;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(userdir)))
		{
			for (Path f : stream)
			{
				String strPath = f.toString();
				if (FileBuffer.fileExists(strPath))
				{
					//If it's a file...
					FileReader fr = new FileReader(f.toString());
					BufferedReader br = new BufferedReader(fr);
					try
					{
						String fname = f.getFileName().toString();
						int dot = fname.lastIndexOf('.');
						if (dot >= 0) fname = fname.substring(0, dot);
						long uid = Long.parseUnsignedLong(fname);
						GuildUser u = new GuildUser(br, actorBank.get(uid));
						users.addUser(u);
						br.close();
						fr.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
						br.close();
						fr.close();
					}
				}
			}
			stream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw e;
		}
		catch (DirectoryIteratorException e)
		{
			e.printStackTrace();
			throw e;
		}

	}
	
	private void loadSchedule(String gdirPath, ParseCore parser) throws IOException, UnsupportedFileTypeException
	{
		String sdir = gdirPath + File.separator + INIT_SCHEDULEDIR;
		schedule = new Schedule(users, guild, sdir, parser);
	}
	
	private void loadRoles(String gdirPath) throws IOException, UnsupportedFileTypeException
	{
		roleManager = new ActingManager(gdirPath + File.separator + INIT_ROLEDIR);
	}

	/* ----- Getters ----- */
	
	public synchronized long getGuildID()
	{
		return guild;
	}
	
	public synchronized long getBirthdayChannelID()
	{
		return birthdayChannel;
	}
	
	public synchronized long getGreetingChannelID()
	{
		return greetingChannel;
	}
	
	public UserBank getUserBank()
	{
		return users;
	}
	
	public Schedule getSchedule()
	{
		return schedule;
	}
	
	public ActingManager getRoleManager()
	{
		return roleManager;
	}
	
	public List<GuildUser> getAllGreetingPingUsers()
	{
		return users.getAllUsersWithGreetingPing();
	}
	
	public List<GuildUser> getAllLeavingPingUsers()
	{
		return users.getAllUsersWithFarewellPing();
	}
	
	public boolean memberShouldBeAdmin(Member m)
	{
		if (m.isOwner()) return true;
		if (adminRoles != null && !adminRoles.isEmpty())
		{
			List<Role> mroles = m.getRoles();
			if (mroles != null && !mroles.isEmpty())
			{
				for (Role r : mroles)
				{
					if (mroles.contains(r.getIdLong())) return true;
				}	
			}
			else return false;
		}
		return false;
	}
	
	public boolean greetingsOn()
	{
		return greetingsOn;
	}
	
	public boolean farewellsOn()
	{
		return this.farewellsOn;
	}
	
	public boolean autoCommandClear()
	{
		return auto_cmd_clear;
	}
	
	public GuildUser getUser(long uid)
	{
		return users.getUser(uid);
	}
	
	/* ----- Setters ----- */
	
	public synchronized void setBirthdayChannel(MessageChannel ch)
	{
		//birthdayChannel = ch;
		birthdayChannel = ch.getIdLong();
	}
	
	public synchronized void setGreetingChannel(MessageChannel ch)
	{
		//greetingChannel = ch;
		greetingChannel = ch.getIdLong();
	}
	
	public synchronized void setBirthdayChannel(long cID)
	{
		birthdayChannel = cID;
	}
	
	public synchronized void setGreetingChannel(long cID)
	{
		greetingChannel = cID;
	}
	
	public void linkParser(ParseCore parser)
	{
		schedule.linkParserCore(parser);
	}
	
	public ActorUser newMember(Member m, ActorUser userProfile)
	{
		GuildUser u = users.getUser(m.getUser().getIdLong());
		if (u != null) return userProfile;
		u = new GuildUser(m, userProfile);
		//Check admin roles to see if this user has admin permissions...
		if (memberShouldBeAdmin(m)) u.setAdmin(true);
		users.addUser(u);
		return u.getUserProfile();
	}
	
	public synchronized void setGreetings(boolean on)
	{
		greetingsOn = on;
	}
	
	public synchronized void setFarewells(boolean on)
	{
		this.farewellsOn = on;
	}
	
	public synchronized void setAutoCommandClear(boolean on)
	{
		auto_cmd_clear = on;
		schedule.setCommandAutoclean(auto_cmd_clear);
	}

	/* ----- Admin Permissions ----- */
	
	public synchronized void updateAdminPermissions_add(Role role, Member member)
	{
		//See if any roles are in the admin role list. If so, make member an admin
		long uid = member.getUser().getIdLong();
		GuildUser user = users.getUser(uid);
		if (user == null) return;
		if (user.isAdmin()) return;
		long rid = role.getIdLong();
		for (long r : adminRoles)
		{
			if (rid == r)
			{
				user.setAdmin(true);
				return;
			}
		}
	}
	
	public synchronized void updateAdminPermissions_remove(Member member)
	{
		long uid = member.getUser().getIdLong();
		GuildUser user = users.getUser(uid);
		if (user == null) return;
		if (!user.isAdmin()) return;
		List<Role> mroles = member.getRoles();
		for (Role r : mroles)
		{
			long rid = r.getIdLong();
			for (long ar : adminRoles)
			{
				if (rid == ar) return;
			}
		}
		user.setAdmin(false);
	}
	
	public synchronized void grantAdminPermissions(Role role, List<Member> members)
	{
		adminRoles.add(role.getIdLong());
		for (Member m : members)
		{
			long uid = m.getUser().getIdLong();
			GuildUser user = users.getUser(uid);
			user.setAdmin(true);
		}
	}
	
	public synchronized void revokeAdminPermissions(Role role, List<Member> members)
	{
		adminRoles.remove(role.getId());
		boolean exempt = false;
		for (Member m : members)
		{
			exempt = false;
			//First see if user is exempt
			if (m.isOwner()) continue;
			List<Role> mroles = m.getRoles();
			for (Role r : mroles)
			{
				if (adminRoles.contains(r.getIdLong())){
					exempt = true;
					break;
				}
			}
			//Revoke role if not
			if (!exempt)
			{
				long uid = m.getUser().getIdLong();
				GuildUser user = users.getUser(uid);
				user.setAdmin(false);	
			}
		}
	}
	
	public synchronized List<Long> getAdminRoleList()
	{
		List<Long> copy = new LinkedList<Long>();
		copy.addAll(adminRoles);
		return copy;
	}
	
	/* ----- Command Cleaning ----- */
	
	public List<MessageID> getRecentCommandList()
	{
		return recentCommands.getQueueCopy();
	}
	
	public MIDQueue getCommandQueue()
	{
		return recentCommands;
	}
	
	public void queueCommandMessage(MessageID mid)
	{
		recentCommands.add(mid);
	}
	
	/* ----- Data Backup ----- */
	
	public class BackupThread extends Thread
	{
		private boolean killme;
		private int delay;
		private boolean backuprunning;
		
		public BackupThread(int delayMinutes)
		{
			this.setDaemon(true);
			this.setName("BackupDaemon_Guild" + Long.toHexString(guild));
			killme = false;
			this.delay = delayMinutes;
			backuprunning = false;
		}
		
		public void run()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " GuildSettings.BackupThread.run || Thread " + this.getName() + " (guild " + Long.toUnsignedString(guild) + ") started!");
			//Sleep during delay or until interrupted...
			try 
			{
				Thread.sleep(delay * 60 * 1000);
			} 
			catch (InterruptedException e1) 
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.err.println(Thread.currentThread().getName() + " || BackupThread.run || Delay sleep interrupted: " + FileBuffer.formatTimeAmerican(stamp));
				e1.printStackTrace();
			}
			while (!killme())
			{
				try 
				{
					setBackupRunning(true);
					saveToDisk(dataDir);
					setBackupRunning(false);
				} 
				catch (IOException e) 
				{
					setBackupRunning(false);
					System.err.println(Thread.currentThread().getName() + " || BackupThread.run || Backup failed: IOException");
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || BackupThread.run || Backup fail timestamp: " + FileBuffer.formatTimeAmerican(stamp));
					e.printStackTrace();
				}
				try 
				{
					Thread.sleep(BACKUP_THREAD_SLEEP_MINUTES * 60 * 1000);
				} 
				catch (InterruptedException e) 
				{
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || BackupThread.run || Thread sleep interrupted: " + FileBuffer.formatTimeAmerican(stamp));
					Thread.interrupted();
				}
			}
			System.err.println(Schedule.getErrorStreamDateMarker() + " GuildSettings.BackupThread.run || Thread " + this.getName() + " (guild " + Long.toUnsignedString(guild) + ") terminating....");
		}
		
		private synchronized boolean killme()
		{
			return killme;
		}
		
		private synchronized void setBackupRunning(boolean b)
		{
			backuprunning = b;
		}
		
		public synchronized void terminate()
		{
			System.err.println(Schedule.getErrorStreamDateMarker() + " GuildSettings.BackupThread.run || Thread " + this.getName() + " (guild " + Long.toUnsignedString(guild) + ") termination requested!");
			while(backuprunning)
			{
				//We have to wait...
				try 
				{
					Thread.sleep(10000);
				} 
				catch (InterruptedException e) 
				{
					GregorianCalendar stamp = new GregorianCalendar();
					System.err.println(Thread.currentThread().getName() + " || BackupThread.terminate || Termination wait interrupted: " + FileBuffer.formatTimeAmerican(stamp));
					e.printStackTrace();
				}
			}
			killme = true;
			this.interrupt();
		}
			
		public synchronized boolean backupRunning()
		{
			return backuprunning;
		}
		
	}
	
	public synchronized boolean backupRunning()
	{
		return backup.backupRunning();
	}
	
	public void startBackupThread()
	{
		//Random delay for staggering...
		Random r = new Random();
		int mwait = r.nextInt(BACKUP_THREAD_SLEEP_MINUTES);
		backup = new BackupThread(mwait);
		backup.start();
	}
	
	public void terminateBackupThread()
	{
		if (backup == null) return;
		//Should block until the backup is done...
		backup.terminate();
	}
	
	public void forceBackup() throws IOException
	{
		//If backup thread is alive, kill it and wait for it to die
		boolean balive = false;
		if (backup != null)
		{
			balive = backup.isAlive();
			if (balive)
			{
				terminateBackupThread();
				while (backup.isAlive())
				{
					try 
					{
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}	
		}
		
		//Backup
		saveToDisk(dataDir);
		
		//If backup thread was running, restart it.
		if(balive)
		{
			startBackupThread();
		}
	}
	
	/* ----- Schedule Threads ----- */
	
	public void startScheduleThreads()
	{
		schedule.startMonitorThreads(auto_cmd_clear);
	}
	
	public void terminateScheduleThreads()
	{
		schedule.killMonitorThreads();
	}
	
	/* ----- Directory Structure ----- */
	
	public static void setUserDataDirectory(String dirPath)
	{
		userDataDirectory = dirPath;
	}
	
	public static String getUserDataDirectory()
	{
		return userDataDirectory;
	}
	
	public static String getGuildDirectoryPath(long guildID)
	{
		String gdirPath = userDataDirectory + File.separator + INIT_GUILDSDIR + File.separator + Long.toHexString(guildID);
		return gdirPath;
	}
	
	/* ----- Serialization ----- */
	
	private void writeSettingsFile(String path) throws IOException
	{
		FileWriter fw = new FileWriter(path);
		BufferedWriter bw = new BufferedWriter(fw);
		
		//bw.write(guild.getIdLong() + "\n");
		//bw.write(birthdayChannel.getIdLong() + "\n");
		//bw.write(Long.toString(greetingChannel.getIdLong()));
		
		bw.write(Long.toUnsignedString(guild) + "\n");
		bw.write(Long.toUnsignedString(birthdayChannel) + "\n");
		bw.write(Long.toUnsignedString(greetingChannel) + "\n");
		if (greetingsOn) bw.write("1\n");
		else bw.write("0\n");
		if (farewellsOn) bw.write("1\n");
		else bw.write("0\n");
		if (auto_cmd_clear) bw.write("1\n");
		else bw.write("0\n");
		String rolelist = "";
		boolean first = true;
		for (Long l : adminRoles)
		{
			if (!first) rolelist += ",";
			rolelist += Long.toUnsignedString(l);
			first = false;
		}
		bw.write(rolelist + "\n");
		
		String cmdlist = "";
		first = true;
		List<MessageID> cmds = recentCommands.getQueueCopy();
		for (MessageID m : cmds)
		{
			if (!first) cmdlist += ",";
			cmdlist += Long.toUnsignedString(m.getChannelID()) + ":" + Long.toUnsignedString(m.getMessageID());
			first = false;
		}
		bw.write(cmdlist);
		
		bw.close();
		fw.close();
	}
	
	private void saveUserInfo(String gdirPath) throws IOException
	{
		String userdir = gdirPath + File.separator + INIT_USERDIR;
		if (!FileBuffer.directoryExists(userdir)) Files.createDirectories(Paths.get(userdir));
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(userdir)))
		{
			for (Path f : stream)
			{
				Files.deleteIfExists(f);
			}
			stream.close();
		}
		
		List<GuildUser> allusers = users.getAllUsers();
		for(GuildUser u : allusers)
		{
			String ufile = userdir + File.separator + Long.toUnsignedString(u.getUserProfile().getUID()) + ".guser";
			FileWriter fw = new FileWriter(ufile);
			BufferedWriter bw = new BufferedWriter(fw);
			
			u.writeToFile(bw);
			
			bw.close();
			fw.close();
		}
		
	}
	
	private void saveSchedule(String gdirPath) throws IOException
	{
		String sdir = gdirPath + File.separator + INIT_SCHEDULEDIR;
		if (!FileBuffer.directoryExists(sdir)) Files.createDirectories(Paths.get(sdir));
		schedule.saveToDisk(sdir);
	}
	
	private void saveRoles(String gdirPath) throws IOException
	{
		String rdir = gdirPath + File.separator + INIT_ROLEDIR;
		if (!FileBuffer.directoryExists(rdir)) Files.createDirectories(Paths.get(rdir));
		roleManager.writeToDisk(rdir);
	}
	
	public void saveToDisk(String gdirPath) throws IOException
	{
		//Don't forget to create any directories that need to be created!
		if (!FileBuffer.directoryExists(gdirPath)) Files.createDirectories(Paths.get(gdirPath));
		String inipath = gdirPath + File.separator + INIT_FILENAME;
		writeSettingsFile(inipath);
		saveUserInfo(gdirPath);
		saveSchedule(gdirPath);
		saveRoles(gdirPath);
	}
	
}
