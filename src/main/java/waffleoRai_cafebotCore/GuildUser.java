package waffleoRai_cafebotCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.dv8tion.jda.core.entities.Member;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_schedulebot.EventType;

public class GuildUser {
	
	/* ----- Constants ----- */
	
	public static final int AUDIO_CONFIRMATION_NONE = 0;
	public static final int AUDIO_CONFIRMATION_UNSPECIFIED = 1;
	public static final int AUDIO_CONFIRMATION_USB_LQ = 2;
	public static final int AUDIO_CONFIRMATION_USB_OKAY = 3;
	public static final int AUDIO_CONFIRMATION_XLR_LQ = 4;
	public static final int AUDIO_CONFIRMATION_XLR = 5;
	public static final int AUDIO_CONFIRMATION_XLR_HQ = 6;
	
	public static final String NULL_USER_NAME = "Mysterious Individual";
	
	/* ----- Instance Variables ----- */
	
	private ActorUser user;

	private Map<EventType, Boolean[]> rswitches;
	
	private boolean admin;
	private boolean pingGreetings;
	private boolean pingFarewells;
	private long pingGreetingsChannel;
	private int audioConfirmed;
	
	private String localName;
	
	/* ----- Construction ----- */
	
	public GuildUser(Member mem, ActorUser userProfile)
	{
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
		
		localName = mem.getUser().getName();
		
		admin = mem.isOwner();
		pingGreetings = false;
		pingFarewells = false;
		pingGreetingsChannel = mem.getDefaultChannel().getIdLong();
		audioConfirmed = AUDIO_CONFIRMATION_NONE;
		
		if (userProfile != null) user = userProfile;
		else user = new ActorUser(mem.getUser());
	}

	public GuildUser(BufferedReader gufile, ActorUser userProfile) throws UnsupportedFileTypeException
	{
		if (userProfile == null) throw new IllegalArgumentException();
		try
		{
			String line = gufile.readLine(); //The first line is the UID to double-check for match
			long UID = Long.parseUnsignedLong(line);
			if (UID != userProfile.getUID()) throw new FileBuffer.UnsupportedFileTypeException();
			
			localName = gufile.readLine();
			
			rswitches = new HashMap<EventType, Boolean[]>();
			for (int i = 0; i < 7; i++)
			{
				line = gufile.readLine();
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
		
			line = gufile.readLine();
			if (line.equals("1")) admin = true;
			else admin = false;
			
			line = gufile.readLine();
			if (line.equals("1")) pingGreetings = true;
			else pingGreetings = false;
			if (line.equals("1")) pingFarewells = true;
			else pingFarewells = false;
			
			line = gufile.readLine();
			pingGreetingsChannel = Long.parseUnsignedLong(line);
			
			line = gufile.readLine();
			audioConfirmed = Integer.parseInt(line);	
		}
		catch(Exception e)
		{
			throw new FileBuffer.UnsupportedFileTypeException();
		}
	}

	/* ----- Getters ----- */
	
	public ActorUser getUserProfile()
	{
		return user;
	}
	
	public boolean reminderOn(EventType type, int rlevel)
	{
		int lv = rlevel - 1;
		if (lv < 0) return false;
		Boolean[] barr = rswitches.get(type);
		if (barr == null) return false;
		if (lv > barr.length) return false;
		return barr[lv];
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
	
	public int getAudioConfirmation()
	{
		return audioConfirmed;
	}
	
	public boolean isAudioConfirmed()
	{
		return (audioConfirmed != AUDIO_CONFIRMATION_NONE);
	}
	
	public String getLocalName()
	{
		return localName;
	}
	
	/* ----- Setters ----- */
	
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
		if (!b) audioConfirmed = AUDIO_CONFIRMATION_NONE;
		else audioConfirmed = AUDIO_CONFIRMATION_UNSPECIFIED;
	}
	
	public void setAudioConfirmation(int audio)
	{
		audioConfirmed = audio;
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
	
	public void resetRemindersToDefault()
	{
		Set<EventType> alltypes = rswitches.keySet();
		for (EventType t : alltypes)
		{
			Boolean[] switches = rswitches.get(t);
			for (int i = 0; i < switches.length; i++)
			{
				if (i < 2) switches[i] = true;
				else switches[i] = false;
			}
		}
	}
	
	public void setLocalName(String name)
	{
		localName = name;
	}

	/* ----- Disk Access ----- */
	
	public void writeToFile(BufferedWriter bw) throws IOException
	{
		bw.write(Long.toUnsignedString(user.getUID()) + "\n");
		bw.write(localName + "\n");
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
		
		bw.write(Integer.toString(audioConfirmed) + "\n");
	}
	
}
