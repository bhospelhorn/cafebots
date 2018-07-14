package waffleoRai_cafebotsGUI;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.Timer;

import java.awt.Font;
import java.awt.Dimension;

public class LoadingDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = -5454037606579977386L;
	public static final int MAXDOTS = 26;
	
	//private Frame parent;
	private JLabel lblDots;
	private Timer timer;
	
	private int dotCount;
	
	private JLabel lblLoading;
	
	public LoadingDialog(Frame parentComp)
	{
		super(parentComp, true);
		getContentPane().setMinimumSize(new Dimension(370, 144));
		getContentPane().setPreferredSize(new Dimension(370, 144));
		setResizable(false);
		setTitle("Loading");
		getContentPane().setLayout(null);
		
		JLabel lblPleaseWait = new JLabel("Please Wait");
		lblPleaseWait.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPleaseWait.setBounds(137, 11, 89, 14);
		getContentPane().add(lblPleaseWait);
		
		lblLoading = new JLabel("Bot core is loading");
		lblLoading.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLoading.setBounds(126, 47, 107, 14);
		getContentPane().add(lblLoading);
		
		lblDots = new JLabel("");
		lblDots.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblDots.setBounds(108, 90, 137, 14);
		getContentPane().add(lblDots);
		
		
		
		//timer.start();
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (dotCount >= MAXDOTS)
		{
			dotCount = 0;
			lblDots.setText("");
		}
		else
		{
			lblDots.setText(lblDots.getText() + ".");
			dotCount++;
		}
		
		lblDots.repaint();
	}
	
	public void startTimer()
	{
		timer = new Timer(500, this);
		timer.start();
	}

	public void stopTimer()
	{
		timer.stop();
	}
	
	public void setLoadedMessage()
	{
		lblLoading.setText("Bot core has loaded!");
		lblLoading.repaint();
	}

}
