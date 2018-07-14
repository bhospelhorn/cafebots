package waffleoRai_cafebotsGUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.SwingUtilities;

import waffleoRai_cafebotCore.BotBrain;

public class MainGUI {

	public static void main(String[] args) 
	{
		 SwingUtilities.invokeLater(new Runnable() 
	        {
	            public void run() 
	            {
	            	CafebotsFrame myGUI = new CafebotsFrame();
	            	myGUI.addWindowListener(new WindowAdapter(){

						@Override
						public void windowClosing(WindowEvent e) {
							//Stop everything...
							BotBrain core = myGUI.getBrain();
							if (core != null)
							{
								try 
								{
									core.terminate();
								} 
								catch (IOException e1) 
								{
									e1.printStackTrace();
									System.exit(1);
								}	
							}
							System.exit(0);
						}
	            		
	            	});
	            	myGUI.render();
	            }

	        });

	}
}
