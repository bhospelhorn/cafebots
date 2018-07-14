package waffleoRai_cafebotInstall;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;


public class GUIMain {

	public static void main(String[] args) 
	{
		 SwingUtilities.invokeLater(new Runnable() 
	        {
	            public void run() 
	            {
	            	InstallForm mygui = new InstallForm();
	            	mygui.renderMe();
	            	mygui.addWindowListener(new WindowAdapter(){

						@Override
						public void windowClosing(WindowEvent e) {
							//Exit, you asshole
							System.exit(0);
						}
	            		
	            	});
	            }
	           

	        });

	}

}
