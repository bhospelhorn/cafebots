package waffleoRai_cafebotsGUI;

import javax.security.auth.login.LoginException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.border.BevelBorder;

import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_cafebotCore.BotBrain;
import waffleoRai_cafebotCore.LaunchCore;
import javax.swing.JButton;
import java.awt.Font;

public class CafebotsFrame extends JFrame {

	private static final long serialVersionUID = -503878424307955804L;
	
	
	private BotPanel botpnl1;
	private BotPanel botpnl2;
	private BotPanel botpnl3;
	private BotPanel botpnl4;
	private BotPanel botpnl5;
	private BotPanel botpnl6;
	private BotPanel botpnl7;
	private BotPanel botpnl8;
	private BotPanel botpnl9;
	
	private List<BotPanel> panels;
	private boolean loading;
	
	private BotBrain core;
	private JButton btnLogin;
	
	public CafebotsFrame()
	{
		panels = new ArrayList<BotPanel>(9);
		initGUI();
		loading = false;
		core = null;
	}
	
	private void initGUI()
	{
		//Calculate sizes...
		int w = (BotPanel.WIDTH * 3) + 20;
		int h = (BotPanel.HEIGHT * 3) + 30 + 100;
		
		setResizable(false);
		setTitle("Caf\u00E9bots");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 100, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		this.setMinimumSize(new Dimension(w,h));
		this.setMaximumSize(new Dimension(w,h));
		this.setPreferredSize(new Dimension(w,h));
		
		botpnl1 = new BotPanel();
		botpnl1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl1 = new GridBagConstraints();
		gbc_botpnl1.fill = GridBagConstraints.BOTH;
		gbc_botpnl1.insets = new Insets(0, 0, 5, 5);
		gbc_botpnl1.gridx = 0;
		gbc_botpnl1.gridy = 0;
		getContentPane().add(botpnl1, gbc_botpnl1);
		panels.add(botpnl1);
		
		botpnl2 = new BotPanel();
		botpnl2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl2 = new GridBagConstraints();
		gbc_botpnl2.insets = new Insets(0, 0, 5, 5);
		gbc_botpnl2.fill = GridBagConstraints.BOTH;
		gbc_botpnl2.gridx = 1;
		gbc_botpnl2.gridy = 0;
		getContentPane().add(botpnl2, gbc_botpnl2);
		panels.add(botpnl2);
		
		botpnl3 = new BotPanel();
		botpnl3.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl3 = new GridBagConstraints();
		gbc_botpnl3.insets = new Insets(0, 0, 5, 0);
		gbc_botpnl3.fill = GridBagConstraints.BOTH;
		gbc_botpnl3.gridx = 2;
		gbc_botpnl3.gridy = 0;
		getContentPane().add(botpnl3, gbc_botpnl3);
		panels.add(botpnl3);
		
		botpnl4 = new BotPanel();
		botpnl4.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl4 = new GridBagConstraints();
		gbc_botpnl4.insets = new Insets(0, 0, 5, 5);
		gbc_botpnl4.fill = GridBagConstraints.BOTH;
		gbc_botpnl4.gridx = 0;
		gbc_botpnl4.gridy = 1;
		getContentPane().add(botpnl4, gbc_botpnl4);
		panels.add(botpnl4);
		
		botpnl5 = new BotPanel();
		botpnl5.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl5 = new GridBagConstraints();
		gbc_botpnl5.insets = new Insets(0, 0, 5, 5);
		gbc_botpnl5.fill = GridBagConstraints.BOTH;
		gbc_botpnl5.gridx = 1;
		gbc_botpnl5.gridy = 1;
		getContentPane().add(botpnl5, gbc_botpnl5);
		panels.add(botpnl5);
		
		botpnl6 = new BotPanel();
		botpnl6.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl6 = new GridBagConstraints();
		gbc_botpnl6.insets = new Insets(0, 0, 5, 0);
		gbc_botpnl6.fill = GridBagConstraints.BOTH;
		gbc_botpnl6.gridx = 2;
		gbc_botpnl6.gridy = 1;
		getContentPane().add(botpnl6, gbc_botpnl6);
		panels.add(botpnl6);
		
		botpnl7 = new BotPanel();
		botpnl7.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl7 = new GridBagConstraints();
		gbc_botpnl7.insets = new Insets(0, 0, 5, 5);
		gbc_botpnl7.fill = GridBagConstraints.BOTH;
		gbc_botpnl7.gridx = 0;
		gbc_botpnl7.gridy = 2;
		getContentPane().add(botpnl7, gbc_botpnl7);
		panels.add(botpnl7);
		
		botpnl8 = new BotPanel();
		botpnl8.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl8 = new GridBagConstraints();
		gbc_botpnl8.insets = new Insets(0, 0, 5, 5);
		gbc_botpnl8.fill = GridBagConstraints.BOTH;
		gbc_botpnl8.gridx = 1;
		gbc_botpnl8.gridy = 2;
		getContentPane().add(botpnl8, gbc_botpnl8);
		panels.add(botpnl8);
		
		botpnl9 = new BotPanel();
		botpnl9.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_botpnl9 = new GridBagConstraints();
		gbc_botpnl9.insets = new Insets(0, 0, 5, 0);
		gbc_botpnl9.fill = GridBagConstraints.BOTH;
		gbc_botpnl9.gridx = 2;
		gbc_botpnl9.gridy = 2;
		getContentPane().add(botpnl9, gbc_botpnl9);
		panels.add(botpnl9);
		
		JPanel ctrlpnl = new JPanel();
		ctrlpnl.setMaximumSize(new Dimension(32767, 100));
		ctrlpnl.setPreferredSize(new Dimension(10, 100));
		ctrlpnl.setMinimumSize(new Dimension(10, 100));
		ctrlpnl.setLayout(null);
		GridBagConstraints gbc_ctrlpnl = new GridBagConstraints();
		gbc_ctrlpnl.anchor = GridBagConstraints.NORTH;
		gbc_ctrlpnl.gridwidth = 3;
		gbc_ctrlpnl.insets = new Insets(0, 0, 0, 5);
		gbc_ctrlpnl.fill = GridBagConstraints.HORIZONTAL;
		gbc_ctrlpnl.gridx = 0;
		gbc_ctrlpnl.gridy = 3;
		getContentPane().add(ctrlpnl, gbc_ctrlpnl);
		
		btnLogin = new JButton("LOGIN");
		btnLogin.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnLogin.setBounds(10, 3, 89, 23);
		ctrlpnl.add(btnLogin);
		btnLogin.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				loadBots();
			}
			
		});
	}
	
	public void render()
	{
		this.pack();
		Dimension center = getCenteringCoordinates();
		setBounds(new Rectangle((int)center.getWidth(), (int)center.getHeight(), this.getWidth(), this.getHeight()));
		this.setVisible(true);
	}

	public Dimension getCenteringCoordinates()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double sWidth = screenSize.getWidth();
		double sHeight = screenSize.getHeight();
		
		int centerX = (int)(sWidth / 2.0);
		int centerY = (int)(sHeight / 2.0);
		
		int X = centerX - ((this.getWidth()) / 2);
		int Y = centerY - (this.getHeight() / 2);
		
		return new Dimension(X, Y);
	}

	public void setWait()
	{
		for (BotPanel p : panels)
		{
			p.setWait();
		}
		btnLogin.setEnabled(false);
	}
	
	public void unsetWait()
	{
		for (BotPanel p : panels)
		{
			p.unsetWait();
		}
		btnLogin.setEnabled(true);
	}
	
	public void updatePanels()
	{
		for (BotPanel p : panels)
		{
			p.updatePanel();
		}
	}
	
	public void loadBots()
	{
		System.err.println("CafebotsFrame.loadBots || Function called!");
		loading = true;
		setWait();
		System.err.println("CafebotsFrame.loadBots || Wait set.");
		
		LoadingDialog dialog = new LoadingDialog(this);
		dialog.setLocationRelativeTo(this);
		System.err.println("CafebotsFrame.loadBots || Dialog created...");
		dialog.addWindowListener(new WindowAdapter(){

			public void windowOpened(WindowEvent e) 
			{
				dialog.startTimer();
			}

			@Override
			public void windowClosing(WindowEvent e) 
			{
				dialog.stopTimer();
				if(loading)
				{
					//Make visible again.
					dialog.setVisible(true);
				}
				else
				{
					//Clean dialog and refresh main GUI
					//dialog.setVisible(false);
					dialog.dispose();
				}
			}
			
		});
		
		SwingWorker<Void, Void> myTask = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception {
				try
				{
					core = LaunchCore.loadCore(false);
					AbstractBot[] barr = core.getBots();
					for (int i = 1; i < 10; i++)
					{
						AbstractBot b = barr[i];
						panels.get(i-1).setBot(b);
					}
				}
				catch(Exception e)
				{
					System.err.println("Exception loading core...");
					e.printStackTrace();
					loading = false;
					dialog.setVisible(false);
					showError("ERROR: Program could not be loaded. See stderr for details.");
					System.exit(1);
					//dialog.dispose();
				}
				return null;
			}
			
			public void done()
			{
				try 
				{
					core.start(false);
				} 
				catch (LoginException e) 
				{
					e.printStackTrace();
					loading = false;
					dialog.setVisible(false);
					showError("ERROR: One or more bots could not log in. See stderr for details.");
					System.exit(1);
				}
				updatePanels();
				unsetWait();
				loading = false;
			}
			
		};
		System.err.println("CafebotsFrame.loadBots || Executing load routine in worker thread...");
		myTask.execute();
		System.err.println("CafebotsFrame.loadBots || Opening dialog...");
		dialog.setVisible(true);
		
		
		//unsetWait();
		//loading = false;
	}
	
	public void showError(String message)
	{
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public BotBrain getBrain()
	{
		return core;
	}
	
}
