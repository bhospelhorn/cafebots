package waffleoRai_cafebotCore;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BotStringMap {
	
	private ConcurrentHashMap<String, String> mainmap;
	private AltStringMap altmap;
	
	private static class AltStringMap
	{
		private ConcurrentHashMap<String, GenderStringMap> map;
		
		public AltStringMap()
		{
			map = new ConcurrentHashMap<String, GenderStringMap>();
		}
		
		public void addFromXML(String key, int gender, boolean isTarg, String xmlfield)
		{
			GenderStringMap gsm = map.get(key);
			if (gsm == null){
				gsm = new GenderStringMap();
				map.put(key, gsm);
			}
			
			gsm.addFromXML(gender, isTarg, xmlfield);
		}
		
		public List<String> getReqStringList(String key, int gender)
		{
			GenderStringMap gsm = map.get(key);
			if (gsm == null) return null;
			return gsm.getReqStringList(gender);
		}
		
		public List<String> getTargStringList(String key, int gender)
		{
			GenderStringMap gsm = map.get(key);
			if (gsm == null) return null;
			return gsm.getTargStringList(gender);
		}
	
	}

	private static class GenderStringMap
	{
		private ConcurrentHashMap<Integer, UserTypeStringSet> map;
		
		public GenderStringMap()
		{
			map = new ConcurrentHashMap<Integer, UserTypeStringSet>();
		}
		
		public void addFromXML(int gender, boolean isTarg, String xmlfield)
		{
			UserTypeStringSet utss = map.get(gender);
			if (utss == null){
				utss = new UserTypeStringSet();
				map.put(gender, utss);
			}
			
			if(isTarg)utss.loadAndParseTargetStrings(xmlfield);
			else utss.loadAndParseRequestStrings(xmlfield);
		
		}
		
		public List<String> getReqStringList(int gender)
		{
			UserTypeStringSet utss = map.get(gender);
			if (utss == null) return null;
			return utss.getReqStringList();
		}
		
		public List<String> getTargStringList(int gender)
		{
			UserTypeStringSet utss = map.get(gender);
			if (utss == null) return null;
			return utss.getTargStringList();
		}
	
	}
	
	private static class UserTypeStringSet
	{
		private List<String> req_strings;
		private List<String> targ_strings;
		
		public UserTypeStringSet()
		{
			req_strings = null;
			targ_strings = null;
		}
		
		public void loadAndParseRequestStrings(String xmlfield)
		{
			req_strings = BotStrings.parseStringList(xmlfield);
		}
		
		public void loadAndParseTargetStrings(String xmlfield)
		{
			targ_strings = BotStrings.parseStringList(xmlfield);
		}
		
		public List<String> getReqStringList()
		{
			return req_strings;
		}
		
		public List<String> getTargStringList()
		{
			return targ_strings;
		}
	}
	
	public BotStringMap()
	{
		mainmap = new ConcurrentHashMap<String, String>();
		altmap = new AltStringMap();
	}
	
	public void loadMainMap(Map<String, String> amap)
	{
		Set<String> keyset = amap.keySet();
		for (String k : keyset)
		{
			mainmap.put(k, amap.get(k));
		}
	}
	
	public void loadAltStringXMLMap(Map<String, String> amap, int gender, boolean isTarg)
	{
		Set<String> keyset = amap.keySet();
		for (String k : keyset)
		{
			altmap.addFromXML(k, gender, isTarg, amap.get(k));
		}
	}

	public String getString(String key)
	{
		return mainmap.get(key);
	}
	
	public List<String> getAltStringList_ReqUser(String key, int gender)
	{
		return altmap.getReqStringList(key, gender);
	}
	
	public List<String> getAltStringList_TargUser(String key, int gender)
	{
		return altmap.getTargStringList(key, gender);
	}
	
}
