package voGui;

/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package boofcv.io.v4l4j;

import au.edu.jcu.v4l4j.*;
import au.edu.jcu.v4l4j.exceptions.ControlException;
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.io.VideoCallBack;
import boofcv.io.VideoController;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageDataType;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;

import javax.swing.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Easy to use wrapper around V4L4J for processing videos.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("rawtypes")
public class V4l4jVideo<T extends ImageBase> extends WindowAdapter
		implements CaptureCallback, VideoController<T>
{
	private static int std = V4L4JConstants.STANDARD_WEBCAM;
	private static int channel = 0;

	VideoCallBack<T> callback;

	private VideoDevice     videoDevice;
	private FrameGrabber frameGrabber;
	private T imageBoof;


	private boolean ctrl_sust_fps=false;
	private boolean ctrl_timeout_img=false;
	private boolean ctrl_keep_format=false;
	
	private boolean convertBufferedImage=true;
	
	private boolean show_fps=false;
	private boolean seenFirstFrame=false;
	private long startTime;
	private int current_fps=0;
	
	/*
	{
		System.setProperty( "java.library.path", "/home/pja/projects/boofcv/integration/v4l4j/v4l4j-0.9.0" );

		Field fieldSysPath = null;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
			fieldSysPath.setAccessible( true );
			fieldSysPath.set( null, null );
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		System.load("/home/pja/projects/boofcv/integration/v4l4j/v4l4j-0.9.0/libvideo.so");
	}
	*/

	public static void main(String args[]) throws NoSuchFieldException, IllegalAccessException {

	/*	System.setProperty( "java.library.path", "/home/pja/iai/kinesys/boofcv/lib/v4l4j-0.9.0" );

		Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
		fieldSysPath.setAccessible( true );
		fieldSysPath.set( null, null );

		System.load("/home/pja/iai/kinesys/boofcv/lib/v4l4j-0.9.0/libvideo.so");
	*/
		
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				V4l4jVideo cam = new V4l4jVideo();
			cam.start("/dev/video0", 320, 240, new VideoCallBack<ImageUInt8>() {

				
				@Override
				public ImageType<ImageUInt8> getImageDataType() {
					
					// TODO Auto-generated method stub
					return new ImageType<ImageUInt8>(ImageType.Family.SINGLE_BAND, ImageDataType.U8,1);
				}

				@Override
				public void init(int arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public <O> void nextFrame(ImageUInt8 arg0, Object arg1, long arg2) {
					// TODO Auto-generated method stub
				//	System.out.println("nf");
				}

				@Override
				public boolean stopRequested() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void stopped() {
					// TODO Auto-generated method stub
					
				}
			});
			
			}
		});
		
		
	}

	/**
	 * Builds a WebcamViewer object
	 * @throws V4L4JException if any parameter if invalid
	 */
	public V4l4jVideo(){

	}

	@Override
	public boolean start( String device , int width, int height, VideoCallBack<T> callback) {
		this.callback = callback;

		// Initialise video device and frame grabber
		try {
			initFrameGrabber(device,width,height);
		} catch (V4L4JException e1) {
			System.err.println("Error setting up capture");
			e1.printStackTrace();

			// cleanup and exit
			cleanupCapture();
			return false;
		}

		// start capture
		try {
			frameGrabber.startCapture();
			return true;
		} catch (V4L4JException e){
			System.err.println("Error starting the capture");
			e.printStackTrace();
			return false;
		}
	}


	/**
	 * Initialises the FrameGrabber object
	 * @throws V4L4JException if any parameter if invalid
	 */
	private void initFrameGrabber( String device, int width , int height ) throws V4L4JException {
		videoDevice = new VideoDevice(device);
		frameGrabber = videoDevice.getJPEGFrameGrabber(width, height, channel, std, 80);
		frameGrabber.setCaptureCallback(this);
		width = frameGrabber.getWidth();
		height = frameGrabber.getHeight();

		// declare storage and initialize the callback
		imageBoof = callback.getImageDataType().createImage(width,height);
		callback.init(width, height);
		
		if(ctrl_sust_fps || ctrl_timeout_img || ctrl_keep_format){
			
			boolean ret=false;
			List<Control> ctrls = videoDevice.getControlList().getList();
			for(Control c: ctrls){
				if(c.getName().equalsIgnoreCase("sustain_framerate") && ctrl_sust_fps){
					c.setValue(1);
					ret=true;
					//System.out.println(c.getName());
				}else if(c.getName().equalsIgnoreCase("timeout_image_io") && ctrl_timeout_img){
					c.setValue(1);
					ret=true;
					//System.out.println(c.getName());
				}else if(c.getName().equalsIgnoreCase("keep_format") && ctrl_keep_format){
					c.setValue(1);
					ret=true;
					//System.out.println(c.getName());
				}else if(c.getName().equalsIgnoreCase("timeout")){
					//System.out.println(c.getName());
				}
			}
			if(!ret) throw new V4L4JException("Specified Controls not found");
		}
		
		if(show_fps){
			seenFirstFrame=false;
			startTime=0;
			current_fps=0;
		}
	
	}

	/**
	 * this method stops the capture and releases the frame grabber and video device
	 */
	protected void cleanupCapture() {
		try {
			frameGrabber.stopCapture();
		} catch (StateException ex) {
			// the frame grabber may be already stopped, so we just ignore
			// any exception and simply continue.
		}

		
		try {
		
			
		if(ctrl_sust_fps || ctrl_timeout_img || ctrl_keep_format){
			List<Control> ctrls = videoDevice.getControlList().getList();
			for(Control c: ctrls){
				if(c.getName().equalsIgnoreCase("sustain_framerate") && ctrl_sust_fps){
					c.setValue(0);
				}else if(c.getName().equalsIgnoreCase("timeout_image_io") && ctrl_timeout_img){
					c.setValue(0);
				}else if(c.getName().equalsIgnoreCase("keep_format") && ctrl_keep_format){
					c.setValue(0);
				}else if(c.getName().equalsIgnoreCase("timeout")){
				}			
			}
			videoDevice.releaseControlList();
			
			ctrl_sust_fps = false;
			ctrl_timeout_img = false;
			ctrl_keep_format = false;
		}
		
		} catch (ControlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
			
		
		
		// release the frame grabber and video device
		videoDevice.releaseFrameGrabber();
		videoDevice.release();

		callback.stopped();
	}

	/**
	 * Catch window closing event so we can free up resources before exiting
	 * @param e
	 */
	public void windowClosing(WindowEvent e) {
		cleanupCapture();
	}


	@Override
	public void exceptionReceived(V4L4JException e) {
		// This method is called by v4l4j if an exception
		// occurs while waiting for a new frame to be ready.
		// The exception is available through e.getCause()
		e.printStackTrace();
	}

	@Override
	public void nextFrame(VideoFrame frame) {
		
		
		// This method is called when a new frame is ready.
		// Don't forget to recycle it when done dealing with the frame.
		if(convertBufferedImage){
		ConvertBufferedImage.convertFrom(frame.getBufferedImage(),imageBoof,true);
		}
		callback.nextFrame(imageBoof,frame.getBufferedImage(),frame.getCaptureTime());



		if(show_fps){
			if(!seenFirstFrame){
				startTime=System.currentTimeMillis();
				seenFirstFrame=true;
			}

			current_fps++;
		
			long currentTime = System.currentTimeMillis();
			if (currentTime >= startTime + 1000) {
			
				System.out.println("fps: "+ current_fps);
			
				current_fps=0;
				startTime=System.currentTimeMillis();
			}
		}
		
		
		// recycle the frame
		frame.recycle();

		if( callback.stopRequested() ) {
			cleanupCapture();
		}
	}

	
	protected void activateControls(boolean sust_fps, boolean timeout_img, boolean keep_format){
		this.ctrl_sust_fps = sust_fps;
		this.ctrl_timeout_img = timeout_img;
		this.ctrl_keep_format = keep_format;
	}
	
	protected void activateAllControls(){
		this.ctrl_sust_fps = true;
		this.ctrl_timeout_img = true;
		this.ctrl_keep_format = true;
	}

	protected void setConvertBufferedImage(boolean v){
		this.convertBufferedImage = v;
	}
	
	protected void setShowFps(boolean v){
		this.show_fps = v;
	}
	
	

}
