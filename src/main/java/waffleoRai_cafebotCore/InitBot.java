package waffleoRai_cafebotCore;

public class InitBot implements Bot{
	
	private String token;
	private String versionstr;
	private String xmlkey;
	
	private int constructor;
	private int index;
	
	public InitBot(String key)
	{
		xmlkey = key;
		token = "";
		versionstr = "0.0.0";
		constructor = BotConstructor.VANILLA;
		index = -1;
	}
	
	public String getToken()
	{
		return token;
	}
	
	public String getVersion()
	{
		return versionstr;
	}
	
	public String getXMLKey()
	{
		return xmlkey;
	}
	
	public int getConstructorType()
	{
		return constructor;
	}
	
	public void setVersionString(String v)
	{
		versionstr = v;
	}
	
	public void setToken(String t)
	{
		token = t;
	}
	
	public void setConstructorType(int t)
	{
		constructor = t;
	}

	@Override
	public int getLocalIndex() {
		return index;
	}

	@Override
	public void setLocalIndex(int i) {
		index = i;
	}

}
