package waffleoRai_cafebotCore;

import java.util.GregorianCalendar;
import java.util.Random;

import waffleoRai_Utils.FileBuffer;

public class InterruptTest {
	
	public static class MagicThread extends Thread
	{
		private boolean killme;
		
		public MagicThread()
		{
			killme = false;
			Random r = new Random();
			super.setName("MagicThread_" + Integer.toHexString(r.nextInt()));
		}
		
		public void run()
		{
			while (!isDead())
			{
				GregorianCalendar stamp = new GregorianCalendar();
				System.out.println(Thread.currentThread().getName() + " says: It is " + FileBuffer.formatTimeAmerican(stamp) + " and I am alive!");
				try 
				{
					Thread.sleep(10000);
				} 
				catch (InterruptedException e) 
				{
					stamp = new GregorianCalendar();
					System.out.println(Thread.currentThread().getName() + " says: It is " + FileBuffer.formatTimeAmerican(stamp) + " and my sleep has been interrupted! Rude!");
					Thread.interrupted();
				}
			}
		}
		
		private synchronized boolean isDead()
		{
			return killme;
		}
		
		public synchronized void kill()
		{
			killme = true;
			this.interrupt();
		}
		
		public synchronized void interruptMe()
		{
			this.interrupt();
		}
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MagicThread t1 = new MagicThread();
		MagicThread t2 = new MagicThread();
		
		System.out.println(Thread.currentThread().getName() + " says: Now starting magic thread 1...");
		t1.start();
		System.out.println(Thread.currentThread().getName() + " says: Now sleeping for 22 seconds...");
		try 
		{
			Thread.sleep(25000);
		} 
		catch (InterruptedException e) 
		{
			System.err.println(Thread.currentThread().getName() + " says: Sleep 1 interrupted!");
		}
		
		System.out.println(Thread.currentThread().getName() + " says: Now starting magic thread 2...");
		t2.start();
		
		System.out.println(Thread.currentThread().getName() + " says: Now interrupting magic thread 1...");
		t1.interruptMe();
		System.out.println(Thread.currentThread().getName() + " says: Now interrupting magic thread 2...");
		t2.interruptMe();
		
		System.out.println(Thread.currentThread().getName() + " says: Now sleeping for 7.5 seconds...");
		try 
		{
			Thread.sleep(7500);
		} 
		catch (InterruptedException e) 
		{
			System.err.println(Thread.currentThread().getName() + " says: Sleep 2 interrupted!");
		}
		
		System.out.println(Thread.currentThread().getName() + " says: Now interrupting magic thread 2...");
		t2.interruptMe();
		System.out.println(Thread.currentThread().getName() + " says: Now killing magic thread 1...");
		t1.kill();
		
		System.out.println(Thread.currentThread().getName() + " says: Now sleeping for 4.7 seconds...");
		try 
		{
			Thread.sleep(4700);
		} 
		catch (InterruptedException e) 
		{
			System.err.println(Thread.currentThread().getName() + " says: Sleep 3 interrupted!");
		}
		
		System.out.println(Thread.currentThread().getName() + " says: Now killing magic thread 2...");
		t2.kill();
		
		System.out.println(Thread.currentThread().getName() + " says: Now waiting for both threads to die.");
		
		while (t1.isAlive() || t2.isAlive())
		{
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				System.err.println(Thread.currentThread().getName() + " says: Sleep 4 interrupted!");
			}
		}
		
		System.out.println(Thread.currentThread().getName() + " says: That's all!");
		
	}

}
