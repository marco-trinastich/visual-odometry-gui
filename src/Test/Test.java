package Test;

import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.feature.tracker.PointTrackerTwoPass;
import boofcv.io.video.CombineFilesTogether;
import boofcv.io.video.CreateMJpeg;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

import voGui.TrackerGenerator;


public class Test <T extends ImageSingleBand>  {

	Class<T> imgType;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//test();
		buffertest();
	}
	
	public static void buffertest(){
		
		final ArrayList<String> buffer=new ArrayList<String>();
		final int buff_size = 10;
		
		
		final ArrayList<String> sourcedata = new ArrayList<String>();
		for(int i=1;i<=100;i++){
			sourcedata.add(String.valueOf(i));
		}
		
		Thread producer = new Thread(new Runnable(){

			int data_pos=-1;
			
			@Override
			public void run() {
				
				while(true){
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					if(data_pos==99) return;
					
					
					if(buffer.size()<buff_size){
						data_pos++;
						buffer.add(sourcedata.get(data_pos));
					}else{
						System.out.println("BUFFER FULL!");
						data_pos++; //Flusso input continuo (i dati vengono prodotti continuamente anche se il buffer è pieno)
									//==> Se PRODUCER è più veloce di CONSUMER, perdiamo dati
					}
					
					
				}
				
				
			}
			
		});
		
		
		
		Thread consumer = new Thread(new Runnable(){

			@Override
			public void run() {
				
				
				while(true){
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					if(buffer.size()>0){
						String read_data = buffer.get(0);
						buffer.remove(0);
						System.out.println(read_data);
					}else{
						System.out.println("BUFFER EMPTY!");
					}
				
				}
			}
			
		});
		
		producer.start();
		consumer.start();
		
	}
	
	
	public static void test(){
		String a = " 1 , 20 , 30,   40   , 168";
		
		int[] nums = extract_IntArray(a);
		
		if(nums!=null){
		for(int i=0;i<nums.length;i++){
		System.out.println(nums[i]);
		}}
		else{System.out.println("NULL");}
		
		
		TrackerGenerator c = new TrackerGenerator(ImageUInt8.class);
		
		PointTracker<ImageUInt8> p = c.create_default_KLT_TwoPass();
		p = (PointTrackerTwoPass<ImageUInt8>) p;
		
		double b = 1.111;
		System.out.println(b);
		System.out.println(Math.round(b));
		System.out.println(Math.round(9*b));
		System.out.println((int)(b));
		System.out.println((int)(9*b));
		

		System.out.println("");
		for(int i=1;i<11;i++){
			System.out.println(i%10);
		}
		
		 EventQueue.invokeLater(new Runnable() {

	            @Override
	            public void run() {
	                ScrollPanePaint a;
	                a = new ScrollPanePaint();
	                
	            }
	        });
		 
		 
	
		 JPanel test_panel = new JPanel() {

			 /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			 public void paint(Graphics g) {
				 g.fillOval(25, 25, 120, 120);
				 g.setColor(Color.red);
				 g.drawLine(0, 0, 85, 85);	
			 }

		 };
		   
		 JFrame test_frame = new JFrame();
		 test_frame.getContentPane().add(test_panel);

		 test_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 test_frame.setSize(200,200);
		 test_frame.setVisible(true);
		 
		Test app = new Test();
		app.classEqualsClass();

	}
	
	
	
	public void classEqualsClass(){
		 imgType = (Class<T>) ImageUInt8.class;		
		 System.out.println(imgType.equals(ImageUInt8.class));
	}
	
	

	/**
	 * @see http://stackoverflow.com/a/10097538/230513
	 * @see http://stackoverflow.com/a/2846497/230513
	 * @see http://stackoverflow.com/a/3518047/230513
	 */
	public static class ScrollPanePaint extends JFrame {

	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int TILE = 64;

		
		
	    public ScrollPanePaint() {
	        JViewport viewport = new MyViewport();
	        viewport.setView(new MyPanel());
	        JScrollPane scrollPane = new JScrollPane();
	        scrollPane.setViewport(viewport);
	        this.add(scrollPane);
	        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	        this.pack();
	        this.setLocationRelativeTo(null);
	        this.setVisible(true);
	    }

	    
	    private class MyViewport extends JViewport {

	        public MyViewport() {
	            this.setOpaque(false);
	            this.setPreferredSize(new Dimension(6 * TILE, 6 * TILE));
	        }

	        @Override
	        public void paint(Graphics g) {
	            super.paint(g);
	            g.setColor(Color.blue);
	            g.fillRect(TILE, TILE, 3 * TILE, 3 * TILE);
	        }
	    }

	    private class MyPanel extends JPanel {

	        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public MyPanel() {
	            this.setOpaque(false);
	            this.setPreferredSize(new Dimension(9 * TILE, 9 * TILE));
	        }

	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            g.setColor(Color.lightGray);
	            int w = this.getWidth() / TILE + 1;
	            int h = this.getHeight() / TILE + 1;
	            for (int row = 0; row < h; row++) {
	                for (int col = 0; col < w; col++) {
	                    if ((row + col) % 2 == 0) {
	                        g.fillRect(col * TILE, row * TILE, TILE, TILE);
	                    }
	                }
	            }
	        }
	    }

	 
	}
	
	
	
	
	
	
	//extracts an array of integer from strings formatted this way: 1,2,3,4 (comma separated numbers)
		static private int[] extract_IntArray(String string){
			
			int[]	result=null;
			
			String stringpart=string;
			
			while(!stringpart.equalsIgnoreCase("")){
				int firstcomma = stringpart.indexOf(",");
				if(firstcomma!=-1){
					try{
						int value = Integer.parseInt(stringpart.substring(0,firstcomma).trim());
						if(result==null){
							result = new int[1];
							result[0] = value;
						}else{
							int[] tmp = result;
							result = new int[tmp.length+1];
							System.arraycopy(tmp, 0, result, 0, tmp.length);
							result[result.length-1] = value;
						}
						if(stringpart.length()==firstcomma+1) return null;
						stringpart = stringpart.substring(firstcomma+1,stringpart.length());
					}catch(Exception e){
						return null;
					}
				}else{
					try{
						int value = Integer.parseInt(stringpart.trim());
						if(result==null){
							result = new int[1];
							result[0] = value;
						}else{
							int[] tmp = result;
							result = new int[tmp.length+1];
							System.arraycopy(tmp, 0, result, 0, tmp.length);
							result[result.length-1] = value;
						}
						stringpart = "";
					}catch(Exception e){
					return null;
					}
				}
				
				
				
			}
			
			return result;
		}
}
