package waffleoRai_cafebotsGUI;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import waffleoRai_cafebotCore.AbstractBot;

import javax.swing.JTextField;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class BotPanel extends JPanel{

	private static final long serialVersionUID = -4824966650441634309L;
	
	public static final int WIDTH = 365;
	public static final int HEIGHT = 185;
	
	//private BufferedImage avatar;
	private PicturePanel pnlAvatar;
	
	private JLabel lblName;
	private JLabel lblDisc;
	private JLabel lblStatus;
	private JLabel lblNickname;

	private JComboBox<GuildListing> cmbxGuild;
	private JComboBox<ChannelListing> cmbxChannel;

	private JTextField txtMessage;
	private JButton btnSend;
	
	private AbstractBot bot;
	
	public BotPanel()
	{
		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		bot = null;
		//avatar = null;
		initPanel();
		updatePanel();
	}
	
	private void initPanel()
	{
		setLayout(null);
		
		pnlAvatar = new PicturePanel();
		pnlAvatar.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		pnlAvatar.setBounds(10, 11, 90, 90);
		add(pnlAvatar);
		
		txtMessage = new JTextField();
		txtMessage.setBounds(10, 122, 349, 20);
		add(txtMessage);
		txtMessage.setColumns(10);
		
		btnSend = new JButton("Send");
		btnSend.setBounds(270, 153, 89, 23);
		add(btnSend);
		btnSend.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				sendMessage();
				
			}
			
		});
		
		lblName = new JLabel("NAME");
		lblName.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblName.setBounds(115, 11, 188, 14);
		add(lblName);
		
		lblDisc = new JLabel("#DISC");
		lblDisc.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblDisc.setBounds(313, 11, 46, 14);
		add(lblDisc);
		
		cmbxGuild = new JComboBox<GuildListing>();
		cmbxGuild.setFont(new Font("Tahoma", Font.PLAIN, 11));
		cmbxGuild.setBounds(156, 56, 180, 20);
		add(cmbxGuild);
		cmbxGuild.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onGuildChanged();
				
			}
			
		});
		
		cmbxChannel = new JComboBox<ChannelListing>();
		cmbxChannel.setBounds(66, 154, 194, 20);
		add(cmbxChannel);
		
		lblStatus = new JLabel("STATUS");
		lblStatus.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblStatus.setBounds(115, 31, 240, 14);
		add(lblStatus);
		
		JLabel lblGuild = new JLabel("Guild:");
		lblGuild.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblGuild.setBounds(115, 59, 31, 14);
		add(lblGuild);
		
		lblNickname = new JLabel("GUILD NICKNAME");
		lblNickname.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblNickname.setBounds(115, 86, 244, 14);
		add(lblNickname);
		
		JLabel lblChannel = new JLabel("Channel:");
		lblChannel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblChannel.setBounds(10, 157, 46, 14);
		add(lblChannel);
	}
	
	public void render()
	{
		this.setVisible(true);
	}
	
	public void disableParts()
	{
		btnSend.setEnabled(false);
		txtMessage.setEnabled(false);
		cmbxGuild.setEnabled(false);
		cmbxChannel.setEnabled(false);
	}
	
	public void enableParts()
	{
		btnSend.setEnabled(true);
		txtMessage.setEnabled(true);
		cmbxGuild.setEnabled(true);
		cmbxChannel.setEnabled(true);
	}
	
	public void setWait()
	{
		disableParts();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait()
	{
		if (bot != null) enableParts();
		else disableParts();
		setCursor(null);
	}

	public void repaintAll()
	{
		pnlAvatar.repaint();
		lblName.repaint();
		lblDisc.repaint();
		lblStatus.repaint();
		cmbxGuild.repaint();
		cmbxChannel.repaint();
		txtMessage.repaint();
		btnSend.repaint();
	}
	
	public void setBot(AbstractBot b)
	{
		bot = b;
		updatePanel();
	}
	
	private void refreshGuildList()
	{
		if (bot == null)
		{
			cmbxGuild.setModel(new DefaultComboBoxModel<GuildListing>());
			return;
		}
		JDA botcore = bot.getJDA();
		List<Guild> guilds = botcore.getGuilds();
		
		DefaultComboBoxModel<GuildListing> model = new DefaultComboBoxModel<GuildListing>();
		for (Guild g : guilds)
		{
			model.addElement(new GuildListing(g));
		}
		
		cmbxGuild.setModel(model);
	}
	
	private void refreshChannelList()
	{
		Guild g = getSelectedGuild();
		if (g == null)
		{
			cmbxChannel.setModel(new DefaultComboBoxModel<ChannelListing>());
			return;
		}
		List<TextChannel> channels = g.getTextChannels();
		DefaultComboBoxModel<ChannelListing> model = new DefaultComboBoxModel<ChannelListing>();
		for (TextChannel c : channels)
		{
			model.addElement(new ChannelListing(c));
		}
		
		cmbxChannel.setModel(model);
		
	}
	
	public Guild getSelectedGuild()
	{
		Object o = cmbxGuild.getSelectedItem();
		if (o == null) return null;
		if (o instanceof GuildListing)
		{
			GuildListing gl = (GuildListing)o;
			return gl.getGuild();
		}
		else return null;
	}
	
	public MessageChannel getSelectedChannel()
	{
		Object o = cmbxChannel.getSelectedItem();
		if (o == null) return null;
		if (o instanceof ChannelListing)
		{
			ChannelListing cl = (ChannelListing)o;
			return cl.getChannel();
		}
		else return null;
	}
	
	public void updatePanel()
	{
		if (bot == null)
		{
			pnlAvatar.setImage(null);
			lblName.setText("[NO BOT LOADED]");
			lblDisc.setText("#");
			lblStatus.setText("");
			lblNickname.setText("");
			refreshGuildList();
			refreshChannelList();
			txtMessage.setText("");
			disableParts();
		}
		else
		{
			try 
			{
				pnlAvatar.setImage(bot.getBotAvatar());
			} 
			catch (IOException e) 
			{
				showError("ERROR: Could not retrieve bot avatar image!");
				e.printStackTrace();
			}
			lblName.setText(bot.getBotName());
			lblDisc.setText("#" + bot.getBotDiscriminator());
			lblStatus.setText(bot.getBotStatus());
			refreshGuildList();
			refreshChannelList();
			Guild g = getSelectedGuild();
			if (g != null) lblNickname.setText(bot.getBotNickname(g));
			else lblNickname.setText("[No guild selected]");
			enableParts();
		}
		repaintAll();
	}
	
	public void onGuildChanged()
	{
		updatePanel();
	}
	
	public void sendMessage()
	{
		if (bot == null){
			showError("Cannot send message from non-existent bot!");
			return;
		}
		String text = txtMessage.getText();
		if (text == null || text.isEmpty())
		{
			showError("Cannot send empty message!");
			return;
		}
		MessageChannel ch =  getSelectedChannel();
		if (ch == null)
		{
			showError("Please select a channel!");
			return;
		}
		
		try
		{
			bot.sendMessage(ch, text);
		}
		catch (Exception e)
		{
			showError("Message sending failed! See stderr for details.");
			e.printStackTrace();
		}
	}
	
	public void showError(String message)
	{
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
}
