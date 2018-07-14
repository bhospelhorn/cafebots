package waffleoRai_cafebotInstall;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XMLTest {

	public static void main(String[] args) {
		String myxml = "C:\\Users\\Blythe\\Documents\\Desktop\\Notes\\cafemaster_ENG.xml";
		try
		{
			FileInputStream fis = new FileInputStream(myxml);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(fis);
			//System.out.println("XML OPEN");
			while(reader.hasNext())
			{
				try
				{
					System.out.println("XML EVENT || TYPE: " + reader.getEventType());
					System.out.println(reader.getLocalName());
				}
				catch(IllegalStateException e)
				{
					System.out.println("[No text available]");
				}
				reader.next();
			}		
			System.out.println("XML CLOSED");
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (XMLStreamException e) 
		{
			
			e.printStackTrace();
		}

	}

}
