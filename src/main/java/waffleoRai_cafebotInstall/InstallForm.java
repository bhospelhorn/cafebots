package waffleoRai_cafebotInstall;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import waffleoRai_cafebotCore.Bot;
import waffleoRai_cafebotCore.BotConstructor;
import waffleoRai_cafebotCore.BotSet;
import waffleoRai_cafebotCore.InitBot;
import waffleoRai_cafebotCore.Language;
import waffleoRai_cafebotCore.LaunchCore;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotCommands.BotScheduler;
import waffleoRai_cafebotCommands.BotScheduler.Position;

public class InstallForm extends JFrame{
	
	private static final long serialVersionUID = 8371459981801954127L;
	
	private Set<Component> allcomps;
	
	private JTextField txtInstallDir;
	private JTextField txtb1;
	private JTextField txtb2;
	private JTextField txtb3;
	private JTextField txtb4;
	private JTextField txtb5;
	private JTextField txtb6;
	private JTextField txtb7;
	private JTextField txtb8;
	private JTextField txtb9;
	
	private JSpinner spn1;
	private JSpinner spn2;
	private JSpinner spn3;
	private JSpinner spn4;
	private JSpinner spn5;
	private JSpinner spn6;
	private JSpinner spn7;
	private JSpinner spn8;
	private JSpinner spn9;
	
	public InstallForm() {
		allcomps = new HashSet<Component>();
		initGUI();
	}
	
	private void initGUI()
	{
		setPreferredSize(new Dimension(450, 400));
		setResizable(false);
		setMinimumSize(new Dimension(450, 400));
		setTitle("Install Cafebots");
		getContentPane().setLayout(null);
		
		JLabel lblDirectory = new JLabel("Directory:");
		lblDirectory.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblDirectory.setBounds(10, 11, 58, 14);
		getContentPane().add(lblDirectory);
		
		txtInstallDir = new JTextField();
		txtInstallDir.setBounds(81, 8, 330, 20);
		getContentPane().add(txtInstallDir);
		txtInstallDir.setColumns(10);
		txtInstallDir.setText(LaunchCore.getDefaultInstallDir());
		allcomps.add(txtInstallDir);
		
		JButton btnInstall = new JButton("Install");
		btnInstall.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnInstall.setBounds(305, 316, 119, 36);
		getContentPane().add(btnInstall);
		btnInstall.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				runInstall();
			}
			
		});
		allcomps.add(btnInstall);
		
		JButton btnUninstall = new JButton("Uninstall");
		btnUninstall.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnUninstall.setBounds(206, 329, 89, 23);
		getContentPane().add(btnUninstall);
		btnUninstall.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				runUninstall();
			}
			
		});
		allcomps.add(btnUninstall);
		
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnBrowse.setBounds(335, 34, 89, 23);
		getContentPane().add(btnBrowse);
		btnBrowse.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				browse();
			}
			
		});
		allcomps.add(btnBrowse);
		
		JLabel lblBotmaster = new JLabel("Bot 1 [MASTER]");
		lblBotmaster.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBotmaster.setBounds(10, 88, 76, 14);
		getContentPane().add(lblBotmaster);
		
		JLabel lblTokens = new JLabel("Tokens");
		lblTokens.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTokens.setBounds(10, 58, 46, 14);
		getContentPane().add(lblTokens);
		
		txtb1 = new JTextField();
		txtb1.setBounds(89, 85, 271, 20);
		getContentPane().add(txtb1);
		txtb1.setColumns(10);
		allcomps.add(txtb1);
		
		JLabel lblBot = new JLabel("Bot 2");
		lblBot.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBot.setBounds(10, 113, 46, 14);
		getContentPane().add(lblBot);
		
		txtb2 = new JTextField();
		txtb2.setBounds(89, 110, 271, 20);
		getContentPane().add(txtb2);
		txtb2.setColumns(10);
		allcomps.add(txtb2);
		
		JLabel lblBot_1 = new JLabel("Bot 3");
		lblBot_1.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBot_1.setBounds(10, 138, 46, 14);
		getContentPane().add(lblBot_1);
		
		txtb3 = new JTextField();
		txtb3.setColumns(10);
		txtb3.setBounds(89, 135, 271, 20);
		getContentPane().add(txtb3);
		allcomps.add(txtb3);
		
		txtb4 = new JTextField();
		txtb4.setColumns(10);
		txtb4.setBounds(89, 160, 271, 20);
		getContentPane().add(txtb4);
		allcomps.add(txtb4);
		
		txtb5 = new JTextField();
		txtb5.setColumns(10);
		txtb5.setBounds(89, 185, 271, 20);
		getContentPane().add(txtb5);
		allcomps.add(txtb5);
		
		txtb6 = new JTextField();
		txtb6.setColumns(10);
		txtb6.setBounds(89, 210, 271, 20);
		getContentPane().add(txtb6);
		allcomps.add(txtb6);
		
		txtb7 = new JTextField();
		txtb7.setColumns(10);
		txtb7.setBounds(89, 235, 271, 20);
		getContentPane().add(txtb7);
		allcomps.add(txtb7);
		
		txtb8 = new JTextField();
		txtb8.setColumns(10);
		txtb8.setBounds(89, 260, 271, 20);
		getContentPane().add(txtb8);
		allcomps.add(txtb8);
		
		txtb9 = new JTextField();
		txtb9.setColumns(10);
		txtb9.setBounds(89, 285, 271, 20);
		getContentPane().add(txtb9);
		allcomps.add(txtb9);
		
		JLabel lblBot_7 = new JLabel("Bot 4");
		lblBot_7.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBot_7.setBounds(10, 163, 46, 14);
		getContentPane().add(lblBot_7);
		
		JLabel lblBot_6 = new JLabel("Bot 5");
		lblBot_6.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBot_6.setBounds(10, 188, 46, 14);
		getContentPane().add(lblBot_6);
		
		JLabel lblBot_5 = new JLabel("Bot 6");
		lblBot_5.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBot_5.setBounds(10, 213, 46, 14);
		getContentPane().add(lblBot_5);
		
		JLabel lblBot_4 = new JLabel("Bot 7");
		lblBot_4.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBot_4.setBounds(10, 238, 46, 14);
		getContentPane().add(lblBot_4);
		
		JLabel lblBot_3 = new JLabel("Bot 8");
		lblBot_3.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBot_3.setBounds(10, 263, 46, 14);
		getContentPane().add(lblBot_3);
		
		JLabel lblBot_2 = new JLabel("Bot 9");
		lblBot_2.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBot_2.setBounds(10, 288, 46, 14);
		getContentPane().add(lblBot_2);
		
		spn1 = new JSpinner();
		spn1.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn1.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn1.setBounds(370, 85, 54, 20);
		getContentPane().add(spn1);
		allcomps.add(spn1);
		
		spn2 = new JSpinner();
		spn2.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn2.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn2.setBounds(370, 110, 54, 20);
		getContentPane().add(spn2);
		allcomps.add(spn2);
		
		spn3 = new JSpinner();
		spn3.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn3.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn3.setBounds(370, 135, 54, 20);
		getContentPane().add(spn3);
		allcomps.add(spn3);
		
		spn4 = new JSpinner();
		spn4.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn4.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn4.setBounds(370, 160, 54, 20);
		getContentPane().add(spn4);
		allcomps.add(spn4);
		
		spn5 = new JSpinner();
		spn5.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn5.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn5.setBounds(370, 185, 54, 20);
		getContentPane().add(spn5);
		allcomps.add(spn5);
		
		spn6 = new JSpinner();
		spn6.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn6.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn6.setBounds(370, 210, 54, 20);
		getContentPane().add(spn6);
		allcomps.add(spn6);
		
		spn7 = new JSpinner();
		spn7.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn7.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn7.setBounds(370, 235, 54, 20);
		getContentPane().add(spn7);
		allcomps.add(spn7);
		
		spn8 = new JSpinner();
		spn8.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn8.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn8.setBounds(370, 260, 54, 20);
		getContentPane().add(spn8);
		allcomps.add(spn8);
		
		spn9 = new JSpinner();
		spn9.setModel(new SpinnerNumberModel(0, 0, 9, 1));
		spn9.setFont(new Font("Tahoma", Font.PLAIN, 11));
		spn9.setBounds(370, 285, 54, 20);
		getContentPane().add(spn9);
		allcomps.add(spn9);
		
		JLabel lblType = new JLabel("Type");
		lblType.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblType.setBounds(370, 59, 46, 14);
		getContentPane().add(lblType);
	}

	public void renderMe()
	{
		this.pack();
		this.setVisible(true);
	}
	
	public BotSet generateBotInfo()
	{
		BotSet set = new BotSet();
		
		int btype = (Integer)spn1.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 1 type = " + btype);
		String t = txtb1.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "1");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(1);
			set.addBot(b);	
		}
		
		if (t == null || t.isEmpty()) return null; //Can't skip bots - must be consecutive
		btype = (Integer)spn2.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 2 type = " + btype);
		t = txtb2.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "2");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(2);
			set.addBot(b);	
		}
		else return set;
		
		if (t == null || t.isEmpty()) return null; //Can't skip bots - must be consecutive
		btype = (Integer)spn3.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 3 type = " + btype);
		t = txtb3.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "3");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(3);
			set.addBot(b);	
		}
		else return set;
		
		if (t == null || t.isEmpty()) return null; //Can't skip bots - must be consecutive
		btype = (Integer)spn4.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 4 type = " + btype);
		t = txtb4.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "4");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(4);
			set.addBot(b);	
		}
		else return set;
		
		if (t == null || t.isEmpty()) return null; //Can't skip bots - must be consecutive
		btype = (Integer)spn5.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 5 type = " + btype);
		t = txtb5.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "5");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(5);
			set.addBot(b);	
		}
		else return set;
		
		if (t == null || t.isEmpty()) return null; //Can't skip bots - must be consecutive
		btype = (Integer)spn6.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 6 type = " + btype);
		t = txtb6.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "6");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(6);
			set.addBot(b);	
		}
		else return set;
		
		if (t == null || t.isEmpty()) return null; //Can't skip bots - must be consecutive
		btype = (Integer)spn7.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 7 type = " + btype);
		t = txtb7.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "7");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(7);
			set.addBot(b);	
		}
		else return set;
		
		if (t == null || t.isEmpty()) return null; //Can't skip bots - must be consecutive
		btype = (Integer)spn8.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 8 type = " + btype);
		t = txtb8.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "8");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(8);
			set.addBot(b);	
		}
		else return set;
		
		if (t == null || t.isEmpty()) return null; //Can't skip bots - must be consecutive
		btype = (Integer)spn9.getValue();
		System.out.println("DEBUG InstallForm.generateBotInfo || Bot 9 type = " + btype);
		t = txtb9.getText();
		if (t != null && !t.isEmpty())
		{
			InitBot b = new InitBot(LaunchCore.defo_bot_XML_keyStem + "9");
			b.setVersionString(BotConstructor.getVersion(btype));
			b.setToken(t);
			b.setConstructorType(btype);
			b.setLocalIndex(9);
			set.addBot(b);	
		}
		else return set;
		
		return set;
	}
	
	public void browse()
	{
		JFileChooser fc = new JFileChooser(this.txtInstallDir.getText());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int retVal = fc.showOpenDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			File f = fc.getSelectedFile();
			String p = f.getAbsolutePath();
			this.txtInstallDir.setText(p);
		}
	}
	
	private void copyResourceToDisk(String jpath, String tpath) throws IOException
	{
		System.err.println("Reading resource " + jpath);
		InputStream is = InstallForm.class.getResourceAsStream(jpath);
		if (is == null)
		{
			showError("ERROR: Resource " + jpath + " could not be copied! Aborting installation...");
			return;
		}
		System.err.println("Copying resource " + jpath + " to " + tpath);
		FileBuffer.copyInputStreamToDisk(tpath, is);
		is.close();
	}
	
	private void writeLANINIT_file(Map<String, String> lanini, String outpath) throws IOException
	{
		FileWriter fw = new FileWriter(outpath);
		BufferedWriter bw = new BufferedWriter(fw);
		
		String key = LaunchCore.LANINI_KEY_DIR;
		String val = lanini.get(key);
		if (val == null) val = "null";
		bw.write(key + "=" + val + "\n");
		
		key = LaunchCore.LANINI_KEY_CMN;
		val = lanini.get(key);
		if (val == null) val = "null";
		bw.write(key + "=" + val + "\n");
		
		key = LaunchCore.LANINI_KEY_MTH;
		val = lanini.get(key);
		if (val == null) val = "null";
		bw.write(key + "=" + val + "\n");
		
		for (int i = 1; i <= 9; i++)
		{
			key = LaunchCore.LANINI_KEY_BOTSTEM + i;
			val = lanini.get(key);
			if (val == null) val = "null";
			bw.write(key + "=" + val + "\n");
		}
		
		for (int i = 0; i <= 9; i++)
		{
			key = LaunchCore.LANINI_KEY_BOTCONSTEM + i;
			val = lanini.get(key);
			if (val == null) val = "null";
			if (i < 9) bw.write(key + "=" + val + "\n");
			else bw.write(key + "=" + val);
		}
		
		bw.close();
		fw.close();
	}
	
	private String getSTRName(String botXMLpath) throws XMLStreamException, FileNotFoundException
	{
		System.err.println("DEBUG InstallForm.getSTRName || Called! botXMLpath = " + botXMLpath);
		final String key = "filename";
		FileInputStream fis = new FileInputStream(botXMLpath);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(fis);
		boolean myfield = false;
		String str = "";
		while(reader.hasNext())
		{
			if(myfield)
			{
				if(reader.getEventType() == XMLStreamReader.CHARACTERS)
				{
					str += reader.getText();
				}
				else if (reader.getEventType() == XMLStreamReader.END_ELEMENT)
				{
					if(reader.getLocalName().equals(key)) myfield = false;
				}
			}
			if(reader.getEventType() == XMLStreamReader.START_ELEMENT)
			{
				if(reader.getLocalName().equals(key)) myfield = true;
			}
			reader.next();
		}
		reader.close();
		
		System.err.println("DEBUG InstallForm.getSTRName || Returning: " + str);
		return str;
	}
	
	public void setAllEnabled(boolean b)
	{
		for (Component c : allcomps) c.setEnabled(b);	
	}
	
	public void setWait()
	{
		setAllEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait()
	{
		setAllEnabled(true);
		setCursor(null);
	}
	
	public void runInstall()
	{
		SwingWorker<Void, Void> myTask = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				
				setWait();
				try{install();}
				catch(Exception e){e.printStackTrace();}
				
				return null;
			}
			
			public void done()
			{
				unsetWait();
			}
			
		};
		myTask.execute();
	}
	
	public void runUninstall()
	{
		SwingWorker<Void, Void> myTask = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				
				setWait();
				try{uninstall();}
				catch(Exception e){e.printStackTrace();}
				
				return null;
			}
			
			public void done()
			{
				unsetWait();
			}
			
		};
		myTask.execute();
	}
	
	public void install()
	{
		System.out.println("InstallForm.install || Gathering bot data...");
		BotSet mybots = generateBotInfo();
		if (mybots == null)
		{
			System.err.println("InstallForm.install || Non-consecutive bot slots... sad!");
			showError("ERROR: Bot slots must be filled consecutively!");
			return;
		}
		System.out.println("InstallForm.install || Gathering install path data...");
		String datadir = this.txtInstallDir.getText();
		
		String sep = File.separator;
		
		System.out.println("InstallForm.install || Setting data path and generating init file...");
		try 
		{
			LaunchCore.setDataPath(datadir);
		} 
		catch (IOException e) {
			showError("ERROR: Init file could not be created! Aborting installation...");
			e.printStackTrace();
			return;
		}
		
		System.out.println("InstallForm.install || Creating bots initialization XML file...");
		try 
		{
			LaunchCore.writeInitXML(mybots, datadir);
		} 
		catch (IOException e) 
		{
			showError("ERROR: Init bot XML could not be created! Aborting installation...");
			e.printStackTrace();
			return;
		}
		
		//Copy data
		System.out.println("InstallForm.install || Creating directories and copying common data...");
		try 
		{
			Files.createDirectories(Paths.get(datadir + File.separator + LaunchCore.DIR_BOTDATA));
			Files.createDirectories(Paths.get(datadir + File.separator + LaunchCore.DIR_COREDATA));
			Files.createDirectories(Paths.get(datadir + File.separator + LaunchCore.DIR_USERDATA));
			String jarpath = "resources/";
			//Copy remind times
			String rtpath = jarpath + LaunchCore.REMINDERTIMES_FILE;
			String rtpathd = datadir + File.separator + LaunchCore.REMINDERTIMES_FILE;
			copyResourceToDisk(rtpath, rtpathd);
			//Copy other things
			for (String datfile : LaunchCore.COMMON_DATA_FILES)
			{
				String jpath = jarpath + datfile;
				String datadata = datadir + File.separator + LaunchCore.DIR_COREDATA + File.separator + LaunchCore.DIR_COREDATA_DATA;
				Files.createDirectories(Paths.get(datadata));
				String tpath = datadata + File.separator + datfile;
				copyResourceToDisk(jpath, tpath);
			}
		} 
		catch (IOException e) 
		{
			showError("ERROR: Data could not be copied! Aborting installation...");
			e.printStackTrace();
			return;
		}
		
		//Copy language files, making sure to update the ini files with correct bot paths
		System.out.println("InstallForm.install || Copying common language data...");
		Language[] alllan = Language.values();
		Map<String, Map<String, String>> lanini = new HashMap<String, Map<String, String>>();
		try 
		{
			for (Language l : alllan)
			{
				String key = l.getCode();
				//Extract init file, then read it back into memory
				String jarpath = "resources/" + key + "/";
				String jpath = jarpath + key + ".ini";
				String tpath = datadir + File.separator + LaunchCore.DIR_COREDATA + File.separator + key + ".ini";
				copyResourceToDisk(jpath, tpath);
				lanini.put(key, LaunchCore.mapINIFile(tpath));
				//Find other files using init file and copy them to disk
				String dirstr = lanini.get(key).get(LaunchCore.LANINI_KEY_DIR);
				String commonstr = lanini.get(key).get(LaunchCore.LANINI_KEY_CMN);
				String monthlist = lanini.get(key).get(LaunchCore.LANINI_KEY_MTH);
				String cmnlandir = datadir + File.separator + LaunchCore.DIR_COREDATA + File.separator + dirstr;
				Files.createDirectories(Paths.get(cmnlandir));
				//Write common string file
				jpath = jarpath + commonstr;
				tpath = cmnlandir + File.separator + commonstr;
				copyResourceToDisk(jpath, tpath);
				//Write month list file
				jpath = jarpath + monthlist;
				tpath = cmnlandir + File.separator + monthlist;
				copyResourceToDisk(jpath, tpath);
			}
		} 
		catch (Exception e) 
		{
			showError("ERROR: Common language data could not be copied! Aborting installation...");
			e.printStackTrace();
			return;
		} 
	
		//Copy bot string files, update language ini files...
		System.out.println("InstallForm.install || Copying selected bot data...");
		List<Bot> blist = mybots.getBots();
		try 
		{
			for (Bot b : blist)
			{
				int index = b.getLocalIndex();
				String botkey = LaunchCore.LANINI_KEY_BOTSTEM + index;
				String botconkey = LaunchCore.LANINI_KEY_BOTCONSTEM + b.getConstructorType();
				String tdir = datadir + sep + LaunchCore.DIR_BOTDATA + sep + botkey;
				if (!FileBuffer.directoryExists(tdir)) Files.createDirectory(Paths.get(tdir));
				for (Language l : alllan)
				{
					Map<String, String> lmap = lanini.get(l.getCode());
					String xmlname = lmap.get(botconkey);
					String jarpath = "resources/" + l.getCode() + "/";
					String jpath = jarpath + xmlname;
					String tpath = tdir + sep + xmlname;
					copyResourceToDisk(jpath, tpath);
					String strname = getSTRName(tpath);
					jpath = jarpath + strname;
					tpath = tdir + sep + strname;
					copyResourceToDisk(jpath, tpath);
					lmap.put(botkey, xmlname);
				}
			}
		} 
		catch (Exception e) 
		{
			showError("ERROR: Bot data could not be copied! Aborting installation...");
			e.printStackTrace();
			return;
		}
		
		//Update language ini files
		try
		{
			for (Language l : alllan)
			{
				String key = l.getCode();
				Map<String, String> lmap = lanini.get(key);
				String tpath = datadir + File.separator + LaunchCore.DIR_COREDATA + File.separator + key + ".ini";
				writeLANINIT_file(lmap, tpath);
			}	
		}
		catch (Exception e) 
		{
			showError("ERROR: Language init file could not be updated! Aborting installation...");
			e.printStackTrace();
			return;
		}
		
		//Generate bot schedule file
		System.out.println("InstallForm.install || Generating bot shift schedules...");
		Map<String, Integer> ppos = ParseCore.getDefaultPermPositionMap();
		List<Position> spos = ParseCore.getDefaultShiftPositions();
		BotScheduler sched = new BotScheduler(LaunchCore.SHIFTS_PER_DAY_DEFO, spos, blist.size(), ppos);
		System.out.println("InstallForm.install || Bot shift schedule generated. Now saving...");
		String bdatdir = datadir + File.separator + LaunchCore.DIR_BOTDATA;
		String bsfile = bdatdir + File.separator + LaunchCore.BOTSCHED_FILE;
		String bpfile = bdatdir + File.separator + LaunchCore.BOTPERM_FILE;
		try 
		{
			sched.saveSchedule(bsfile, bpfile);
		} 
		catch (IOException e) {
			showError("ERROR: Bot schedule could not be written! Aborting installation...");
			e.printStackTrace();
			return;
		}
		
		//Generate settings file
		System.out.println("InstallForm.install || Generating boot settings file...");
		String stgfile = datadir + File.separator + LaunchCore.SETTINGS_FILE;
		try
		{
			FileWriter fw = new FileWriter(stgfile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(LaunchCore.SETTINGS_KEY_LAN + "=ENG"); //For now, just default to English.
			bw.close();
			fw.close();	
		}
		catch (Exception e)
		{
			showError("ERROR: Initial settings file could not be written! Aborting installation...");
			e.printStackTrace();
			return;
		}
		System.out.println("InstallForm.install || Installation complete!");
	}
	
	public void uninstall()
	{
		int n = JOptionPane.showOptionDialog(this,
				 "Are you SURE you want to uninstall cafebots?\n"
						    + "All of your data on this machine will be deleted!!",
			    "Uninstall Cafebots",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.WARNING_MESSAGE, null, null, null);
		if (n == JOptionPane.NO_OPTION) return;
		String homedir = LaunchCore.getUserHome(false);
		String datadir = null;
		try
		{
			datadir = LaunchCore.getDataDirectory(homedir);
		}
		catch(IOException e)
		{
			showError("Installation directory could not be found! Uninstall aborted.");
			e.printStackTrace();
			return;
		}
		try 
		{
			LaunchCore.cleanAllData(datadir);
		} 
		catch (DirectoryIteratorException e) 
		{
			showError("Error with directory stream! Uninstall aborted.");
			e.printStackTrace();
			return;
		} 
		catch (IOException e) 
		{
			showError("Error during file deletion! Uninstall aborted.");
			e.printStackTrace();
			return;
		}
	}
	
	public void showError(String message)
	{
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
}
