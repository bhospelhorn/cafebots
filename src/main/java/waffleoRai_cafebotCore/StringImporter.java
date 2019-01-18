package waffleoRai_cafebotCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class StringImporter {

	public static final String KEY_FILEINFO = "fileinfo";
	public static final String KEY_BOTNAME = "botname";
	public static final String KEY_PATH = "filename";
	
	private String filePath;
	private String botName;
	private Map<String, String> smap;
	
	private static class StrRecord
	{
		public int line;
		public int nlines;
	}
	
	public StringImporter(String xmlPath) throws XMLStreamException, UnsupportedFileTypeException, IOException
	{
		parseStringFile(parseXML(xmlPath));
	}
	
	private String generateMapKey(LinkedList<String> elements)
	{
		String s = "";
		for (String e : elements)
		{
			s += e + ".";
		}
		s = s.substring(0, s.length() - 1);
		return s;
	}
	
	private Map<String, StrRecord> parseXML(String xmlPath) throws FileNotFoundException, XMLStreamException, UnsupportedFileTypeException
	{
		FileInputStream fis = new FileInputStream(xmlPath);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(fis);
		String dir = xmlPath.substring(0, xmlPath.lastIndexOf(File.separator));
		Map<String, StrRecord> map = new HashMap<String, StrRecord>();
		LinkedList<String> elements = new LinkedList<String>();
		//System.out.println("XML OPEN");
		boolean fileinfo = false;
		while(reader.hasNext())
		{
			if(reader.getEventType() == XMLStreamReader.START_ELEMENT)
			{
				String str = reader.getLocalName();
				//System.err.println("StringImporter.parseXML || START_ELEMENT: " + str);
				if(str.equals(KEY_FILEINFO)){
					fileinfo = true;
					reader.next();
					continue;
				}
				elements.addLast(str);
			}
			else if(reader.getEventType() == XMLStreamReader.END_ELEMENT)
			{
				String str = reader.getLocalName();
				//System.err.println("StringImporter.parseXML || END_ELEMENT: " + str);
				if(str.equals(KEY_FILEINFO)){
					fileinfo = false;
					reader.next();
					continue;
				}
				elements.removeLast();
			}
			else if(reader.getEventType() == XMLStreamReader.CHARACTERS)
			{
				String str = reader.getText();
				//System.err.println("StringImporter.parseXML || CHARACTERS: " + str);
				if(fileinfo)
				{
					String el = elements.getLast();
					if (el.equals(KEY_BOTNAME)) botName = str;
					if (el.equals(KEY_PATH)) filePath = dir + File.separator + reader.getText();
				}
				else
				{
					String[] fields = str.split(",");
					if (fields.length != 2){
						//System.err.println("StringImporter.parseXML || Non-record encountered: " + str);
						reader.next();
						continue;
						//throw new FileBuffer.UnsupportedFileTypeException();
					}
					StrRecord r = new StrRecord();
					try
					{
						r.line = Integer.parseInt(fields[0]);
						r.nlines = Integer.parseInt(fields[1]);
					}
					catch(NumberFormatException e)
					{
						throw new FileBuffer.UnsupportedFileTypeException();
					}
					map.put(generateMapKey(elements), r);
				}
			}
			reader.next();
		}
		
		return map;
	}
	
	private void parseStringFile(Map<String, StrRecord> map) throws IOException
	{
		/*FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		
		LinkedList<String> allstr = new LinkedList<String>(); //A bit sketch, but whatever.
		String line = null;

		while ((line = br.readLine()) != null)
		{
			allstr.add(line);
		}
		
		Set<String> keyset = map.keySet();
		smap = new HashMap<String, String>();
		
		String[] sarr = new String[allstr.size()];
		sarr = allstr.toArray(sarr);*/
		
		
		//Charset mySet = Charset.forName("UTF8");
		//System.err.println("DEBUG -- StringImporter.parseStringFile || FilePath = " + filePath);
		FileBuffer strfile = FileBuffer.createBuffer(filePath);
		//System.err.println("DEBUG -- StringImporter.parseStringFile || strfile.getFileSize() = " + strfile.getFileSize());
		String file = strfile.readEncoded_string("UTF8", 0, strfile.getFileSize());
		
		String[] sarr = file.split("\n"); //Still not great. Whatever.
		//System.err.println("StringImporter.parseStringFromFile || sarr.length = " + sarr.length);
		Set<String> keyset = map.keySet();
		smap = new HashMap<String, String>();
		for(String k : keyset)
		{
			//System.err.println("StringImporter.parseStringFromFile || key: " + k);
			StrRecord r = map.get(k);
			String s = "";
			//System.err.println("\tr.line: " + r.line);
			//System.err.println("\tr.nlines: " + r.nlines);
			for (int l = -1; l < r.nlines - 1; l++)
			{
				s += sarr[r.line + l];
				if (l < (r.nlines - 1)) s += "\n";
			}
			//System.err.println("\tOutput string: " + s);
			smap.put(k, s);
		}
		
		//br.close();
		//fr.close();
	}

	public String getBotName()
	{
		return botName;
	}
	
	public Map<String, String> getStringMap()
	{
		return smap;
	}
	
}
