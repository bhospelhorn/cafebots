package waffleoRai_cafebotsGUI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class PicturePanel extends JPanel{
	
	private static final long serialVersionUID = -8183633998621475715L;
	
	public static final int WIDTH = 90;
	public static final int HEIGHT = 90;
	
	private Image image;
	
	public PicturePanel()
	{
		image = null;
		initPanel();
	}
	
	private void initPanel()
	{
		this.setLayout(null);
		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
	}
	
	public void setImage(BufferedImage i)
	{
		if (i != null) image = i.getScaledInstance(WIDTH, HEIGHT, BufferedImage.SCALE_SMOOTH);
		else image = null;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (image != null)
		{
			g.drawImage(this.image, 0, 0, WIDTH, HEIGHT, null);
		}
	}

}
