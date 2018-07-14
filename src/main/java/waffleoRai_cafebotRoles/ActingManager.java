package waffleoRai_cafebotRoles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class ActingManager {
	
	/* ----- Constants ----- */
	
	public static final String ROLE_FILENAME = "roles.tsv";
	
	/* ----- Instance Variables ----- */
	
	private UserRoleMap usermap; //Maps to roles by user UID
	private RoleMap rolemap; //Maps to roles by role UID
	
	/* ----- Inner Classes ----- */
	
	public static class UserRoleMap
	{
		private Map<Long, Set<ActorRole>> map;
		
		public UserRoleMap()
		{
			map = new HashMap<Long, Set<ActorRole>>();
		}
		
		public synchronized void addRole(long uid, ActorRole role)
		{
			Set<ActorRole> rolez = map.get(uid);
			if (rolez == null)
			{
				rolez = new HashSet<ActorRole>();
				rolez.add(role);
				map.put(uid, rolez);
			}
			else rolez.add(role);
		}
		
		public synchronized void removeRole(long uid, ActorRole role)
		{
			Set<ActorRole> rolez = map.get(uid);
			if (rolez == null) return;
			else rolez.remove(role);
		}
		
		public synchronized Collection<ActorRole> getAllRoles(long uid)
		{
			Set<ActorRole> rolez = map.get(uid);
			if (rolez == null || rolez.isEmpty()) return null;
			List<ActorRole> rlist = new ArrayList<ActorRole>(rolez.size());
			rlist.addAll(rolez);
			Collections.sort(rlist);
			return rlist;
		}
		
		public synchronized Collection<ActorRole> getAllActingRoles(long uid)
		{
			Set<ActorRole> rolez = map.get(uid);
			if (rolez == null || rolez.isEmpty()) return null;
			List<ActorRole> rlist = new ArrayList<ActorRole>(rolez.size());
			for (ActorRole r : rolez)
			{
				if (r.getActorUser() == uid) rlist.add(r);
			}
			Collections.sort(rlist);
			return rlist;
		}
		
		public synchronized Collection<ActorRole> getAllDirectingRoles(long uid)
		{
			Set<ActorRole> rolez = map.get(uid);
			if (rolez == null || rolez.isEmpty()) return null;
			List<ActorRole> rlist = new ArrayList<ActorRole>(rolez.size());
			for (ActorRole r : rolez)
			{
				if (r.getDirectorUser() == uid) rlist.add(r);
			}
			Collections.sort(rlist);
			return rlist;
		}
	
	}
	
	public static class RoleMap
	{
		private Map<Long, ActorRole> map;
		
		public RoleMap()
		{
			map = new HashMap<Long, ActorRole>();
		}
		
		public synchronized void addRole(ActorRole role)
		{
			map.put(role.getRoleUID(), role);
		}
		
		public synchronized void removeRole(long ruid)
		{
			map.remove(ruid);
		}
		
		public synchronized ActorRole getRole(long ruid)
		{
			return map.get(ruid);
		}
		
		public synchronized Set<Long> getKeyset()
		{
			return map.keySet();
		}
		
	}
	
	/* ----- Construction ----- */
	
	public ActingManager()
	{
		rolemap = new RoleMap();
		usermap = new UserRoleMap();
	}
	
	public ActingManager(String dirpath) throws IOException, UnsupportedFileTypeException
	{
		this();
		String path = dirpath + File.separator + ROLE_FILENAME;
		readFromFile(path);
	}
	
	private void readFromFile(String path) throws IOException, UnsupportedFileTypeException
	{
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		
		String line = null;
		while((line = br.readLine()) != null)
		{
			try
			{
				ActorRole r = new ActorRole(line);
				usermap.addRole(r.getActorUser(), r);
				usermap.addRole(r.getDirectorUser(), r);
				rolemap.addRole(r);
			}
			catch(Exception e)
			{
				br.close();
				fr.close();
				throw e;
			}
		}
		
		br.close();
		fr.close();
	}
	
	/* ----- Getters ----- */
	
	public Collection<ActorRole> getAllActorRoles(long uid)
	{
		return usermap.getAllActingRoles(uid);
	}
	
	public Collection<ActorRole> getAllDirectingRoles(long uid)
	{
		return usermap.getAllDirectingRoles(uid);
	}
	
	public ActorRole getRole(long ruid)
	{
		return rolemap.getRole(ruid);
	}
	
	public Collection<Long> getAllRoleUIDs()
	{
		return rolemap.getKeyset();
	}
	
	/* ----- Setters ----- */
	
	public void addRole(ActorRole r)
	{
		usermap.addRole(r.getActorUser(), r);
		usermap.addRole(r.getDirectorUser(), r);
		rolemap.addRole(r);
	}
	
	public void removeRole(long ruid)
	{
		ActorRole r = rolemap.getRole(ruid);
		rolemap.removeRole(ruid);
		usermap.removeRole(r.getActorUser(), r);
		usermap.removeRole(r.getDirectorUser(), r);
	}
	
	/* ----- Serialization ----- */
	
	public void writeToDisk(String dirpath) throws IOException
	{
		String path = dirpath + File.separator + ROLE_FILENAME;
		FileWriter fw = new FileWriter(path);
		BufferedWriter bw = new BufferedWriter(fw);
		
		Collection<Long> allroles = getAllRoleUIDs();
		boolean first = true;
		for (Long l : allroles)
		{
			ActorRole r = rolemap.getRole(l);
			if (r == null) continue;
			if (!first) bw.write("\n");
			bw.write(r.to_tsv_record());
			first = false;
		}
		
		bw.close();
		fw.close();
	}

}
