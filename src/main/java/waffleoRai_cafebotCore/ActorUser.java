package waffleoRai_cafebotCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_cafebotRoles.ActorRole;
import waffleoRai_schedulebot.CalendarEvent;
import waffleoRai_schedulebot.EventType;

public class ActorUser {
	
	private long UID;
	//private Member userlink;
	
	private TimeZone timezone;
	
	private Map<EventType, Boolean[]> rswitches;
	
	private boolean admin;
	private boolean pingGreetings;
	private boolean pingFarewells;
	private long pingGreetingsChannel;
	
	private Set<CalendarEvent> requested_events;
	private Set<CalendarEvent> target_events;
	
	//private Birthday birthday;
	
	private Set<ActorRole> roles;
	private boolean audioConfirmed;
	
	public ActorUser(Member mem)
	{
		User u = mem.getUser();
		//userlink = mem;
		UID = u.getIdLong();
		timezone = TimeZone.getDefault();
		
		rswitches = new HashMap<EventType, Boolean[]>();
		for (int i = 0; i < 7; i++)
		{
			int ct = GuildSettings.REMINDER_COUNT;
			Boolean[] barr = new Boolean[ct];
			for (int j = 0; j < ct; j++){
				if (j < 2) barr[j] = true;
				else barr[j] = false;
			}
			rswitches.put(EventType.getEventType(i), barr);
		}
		
		admin = mem.isOwner();
		pingGreetings = false;
		pingFarewells = false;
		pingGreetingsChannel = mem.getDefaultChannel().getIdLong();
		audioConfirmed = false;
		
		requested_events = new HashSet<CalendarEvent>();
		target_events = new HashSet<CalendarEvent>();
		
		//birthday = null;
		roles = new HashSet<ActorRole>();
	}
	
	public ActorUser(BufferedReader userfile) throws UnsupportedFileTypeException
	{
		try
		{
			String line = userfile.readLine();
			//UID = Long.parseLong(line);
			UID = Long.parseUnsignedLong(line);
			//userlink = guild.getMemberById(UID);
			timezone = TimeZone.getTimeZone(userfile.readLine());
			
			rswitches = new HashMap<EventType, Boolean[]>();
			for (int i = 0; i < 7; i++)
			{
				line = userfile.readLine();
				int len = line.length();
				Boolean[] barr = new Boolean[len];
				for (int j = 0; j < len; j++)
				{
					char c = line.charAt(j);
					if (c == '1') barr[j] = true;
					else barr[j] = false;
				}
				rswitches.put(EventType.getEventType(i), barr);
			}
		
			line = userfile.readLine();
			if (line.equals("1")) admin = true;
			else admin = false;
			
			line = userfile.readLine();
			if (line.equals("1")) pingGreetings = true;
			else pingGreetings = false;
			if (line.equals("1")) pingFarewells = true;
			else pingFarewells = false;
			
			line = userfile.readLine();
			pingGreetingsChannel = Long.parseUnsignedLong(line);
			
			line = userfile.readLine();
			if (line.equals("1")) audioConfirmed = true;
			else audioConfirmed = false;
			
			requested_events = new HashSet<CalendarEvent>();
			target_events = new HashSet<CalendarEvent>();
			
			//birthday = null;
			roles = new HashSet<ActorRole>();
			
		}
		catch(Exception e)
		{
			throw new FileBuffer.UnsupportedFileTypeException();
		}
	}

	/* ----- Getters ----- */
	
	public long getUID()
	{
		return UID;
	}
	
	/*
	public Member getMember()
	{
		return userlink;
	}*/
	
	public TimeZone getTimeZone()
	{
		return timezone;
	}

	public boolean reminderOn(EventType type, int rlevel)
	{
		if (rlevel < 0) return false;
		Boolean[] barr = rswitches.get(type);
		if (barr == null) return false;
		if (rlevel > barr.length) return false;
		return barr[rlevel];
	}
	
	public boolean isAdmin()
	{
		return admin;
	}
	
	public boolean pingGreetingsOn()
	{
		return pingGreetings;
	}

	public boolean pingFarewellsOn()
	{
		return this.pingFarewells;
	}
	
	public long getPingGreetingsChannel()
	{
		return pingGreetingsChannel;
	}
	
	public List<CalendarEvent> getLinkedRequestedEvents()
	{
		List<CalendarEvent> elist = new ArrayList<CalendarEvent>(requested_events.size());
		elist.addAll(requested_events);
		Collections.sort(elist);
		return elist;
	}
	
	public List<CalendarEvent> getLinkedTargetEvents()
	{
		List<CalendarEvent> elist = new ArrayList<CalendarEvent>(target_events.size());
		elist.addAll(target_events);
		Collections.sort(elist);
		return elist;
	}
	
	public List<ActorRole> getLinkedRoles()
	{
		List<ActorRole> rlist = new ArrayList<ActorRole>(roles.size());
		rlist.addAll(roles);
		Collections.sort(rlist);
		return rlist;
	}
	
	public boolean audioConfirmed()
	{
		return audioConfirmed;
	}
	
	/* ----- Setters ----- */
	
	public void setTimeZone(String tzID)
	{
		timezone = TimeZone.getTimeZone(tzID);
	}
	
	public void setReminder(EventType type, int rlevel, boolean on)
	{
		if (rlevel < 0) return;
		Boolean[] barr = rswitches.get(type);
		if (barr == null) return;
		if (rlevel > barr.length) return;
		barr[rlevel] = on;
	}
	
	public void setAdmin(boolean b)
	{
		admin = b;
	}
	
	public void setGreetingPings(boolean b)
	{
		pingGreetings = b;
	}
	
	public void setFarewellPings(boolean b)
	{
		this.pingFarewells = b;
	}
	
	public void setGreetingPingsChannel(long channelID)
	{
		pingGreetingsChannel = channelID;
	}
	
	public void setAudioConfirmation(boolean b)
	{
		audioConfirmed = b;
	}
	
	public void linkEvent(CalendarEvent e)
	{
		//See if requesting user...
		long r = e.getRequestingUser();
		if (r == UID)
		{
			requested_events.add(e);
			return;
		}
		List<Long> llist = e.getTargetUsers();
		if (llist.contains(UID))
		{
			target_events.add(e);
			return;
		}
	}
	
	public void linkRole(ActorRole r)
	{
		if (r.getActorUser() != UID) return;
		roles.add(r);
	}
	
	public void turnOffAllReminders()
	{
		EventType[] all = EventType.values();
		for (EventType t : all)
		{
			Boolean[] switches = rswitches.get(t);
			if (switches != null)
			{
				for (int i = 0; i < switches.length; i++) switches[i] = false;
			}
		}
	}
	
	public void turnOnAllReminders()
	{
		EventType[] all = EventType.values();
		for (EventType t : all)
		{
			Boolean[] switches = rswitches.get(t);
			if (switches != null)
			{
				for (int i = 0; i < switches.length; i++) switches[i] = true;
			}
		}
	}
	
	/* ----- Disk Access ----- */
	
	public void writeToFile(BufferedWriter bw) throws IOException
	{
		bw.write(Long.toUnsignedString(UID) + "\n");
		bw.write(timezone.getID() + "\n");
		for (int i = 0; i < 7; i++)
		{
			Boolean[] barr = rswitches.get(EventType.getEventType(i));
			for (int j = 0; j < barr.length; j++)
			{
				if (barr[j]) bw.write("1");
				else bw.write("0");
			}
			bw.write("\n");
		}
		
		if (admin) bw.write("1\n");
		else bw.write("0\n");
		
		if (pingGreetings) bw.write("1\n");
		else bw.write("0\n");
		if (pingFarewells) bw.write("1\n");
		else bw.write("0\n");
		
		bw.write(Long.toUnsignedString(pingGreetingsChannel) + "\n");
		
		if (audioConfirmed) bw.write("1\n");
		else bw.write("0\n");
	}
	
}
