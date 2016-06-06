package org.jakz.common;

import java.awt.*;
import java.io.Serializable;

public class GApplet extends java.applet.Applet implements Runnable, Serializable
{
	protected static final long serialVersionUID = 4;
	protected Thread threadHandle;
	protected boolean runFlag;
	protected double activity = 0.03334;
	protected long timeMilliseconds=0;
	protected long starttimeMilliseconds=0;
	protected int lastSleptNanos =0;
	protected int graphicContextResets=0;
	
	
	//drawing
	protected int offHeight=0;
	protected int offWidth=0;
	protected Image offImage=null;
	protected Graphics offGraphics=null;

	
	
	public void init()
	{
		synchronized(this)
		{
			timeMilliseconds=0;
			starttimeMilliseconds=System.currentTimeMillis();
			runFlag=true;
			threadHandle=new Thread(this);
			threadHandle.start();
		}
	}
	
	public void start()
	{
		
	}
	
	public void stop()
	{
		
	}
	
	public void destroy()
	{
		synchronized(this)
		{
			runFlag=false;
		}
	}
	
	public void paintFrame(Graphics g)
	{
		int _width = getWidth();
		int _height = getHeight();
		g.drawRect(50, 50, _width - 1, _height - 1);
		synchronized(this)
		{
			g.drawString("Grrrrreeetings.... arrrr:"+timeMilliseconds, _width/2-20+50, _height/2+50);
			g.drawString("Index Applet @"+starttimeMilliseconds+"\n last slept "+lastSleptNanos+" nanoseconds, graphic resets="+graphicContextResets, 10+50, _height-20+50);
		}
	}
	
	public void update(Graphics g)
	{
		int _width = getWidth();
		int _height = getHeight();


		// Create the offscreen graphics context
		if ((offGraphics == null) || (_width != offWidth-100) || (_height != offHeight-100))
		{
		    offImage = createImage(_width+100, _height+100);
		    offWidth=offImage.getWidth(null);
		    offHeight=offImage.getHeight(null);
		    offGraphics = offImage.getGraphics();
		    
		    synchronized(this)
		    {
		    	graphicContextResets++;
		    }
		}

		// Erase the previous image
		offGraphics.setColor(getBackground());
		offGraphics.fillRect(0, 0, offWidth, offHeight);
		offGraphics.setColor(Color.black);

		// Paint the frame into the image
		paintFrame(offGraphics);

		// Paint the image onto the screen
		g.drawImage(offImage, -50, -50, null);
	}
	
	public void run()
	{
		
		try
		{
			boolean rfCopy;
			int nanosToSleep,millisToSleep;
			synchronized(this)
			{
				rfCopy = runFlag;
			}
			
			while(rfCopy)
			{
				synchronized(this)
				{
					nanosToSleep = 100000-(int)(activity*100000);
					lastSleptNanos=nanosToSleep;
				}
				
				
				millisToSleep=nanosToSleep/1000;
				nanosToSleep=nanosToSleep-millisToSleep*1000;
				Thread.sleep(millisToSleep,nanosToSleep);
				
				
				
				//Functions peformed
				contentActions();
				
				
				
				synchronized(this)
				{
					lastSleptNanos=nanosToSleep;
					rfCopy = runFlag;
					repaint();
				}
				
				
			}
		}
		catch (Exception e)
		{
			//error
		}
	}
	
	
	public void contentActions()
	{
		synchronized(this)
		{
			timeMilliseconds=System.currentTimeMillis();
		}
	}


}
