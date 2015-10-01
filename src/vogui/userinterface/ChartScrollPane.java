package vogui.userinterface;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import georegression.struct.point.Point2D_F64;
import boofcv.gui.image.ImagePanel;

public class ChartScrollPane extends JScrollPane {
	
	/**
	 *ChartScrollPane / Author: Marco Trinastich 
	 */
	private static final long 		serialVersionUID = -3901750331781915922L;
	
	private ImagePanel				chart_panel;
	private Color					background_color=null;
	private	JScrollBar				horizontal_bar;
	private JScrollBar				vertical_bar;
	
	private int						originX, originY;
	private int						initX, initY;
	private boolean					centered_originX=true;
	private boolean					centered_originY=true;
	private ArrayList<Point2D_F64>	stored_points;
	private double					chart_scale = 1.0;
	private Color					chart_color=null;
	private boolean					follow_new_points=true;
	private boolean					thick_points=false; //Draw bigger points
	
	private boolean					show_axis=true;
	private boolean					show_axis_units=true;
	private boolean					show_permanent_axis_names=true;
	private String					axis_x_name="X";
	private String					axis_y_name="Y";
	private Color					axis_color=null;
	private Color					axis_units_color=null;
	private Color					axis_names_color=null;
	
	private boolean					multiple_colors=true;
	private boolean					show_legend=false;
	private int						charts_count=0;
	
	private boolean					move_to_origin_flag=false;
	private ChartScrollPane			myself;
	
	
	public ChartScrollPane(){
		this(null);							//No Border Color and Default Origin(0,0) with Autocentering
	}
	
	public ChartScrollPane(Color bordercolor){
		this(0, 0, true, true, bordercolor); //Start Origin (0,0) with Autocentering enabled for X and Y Axis
	}

	
	public ChartScrollPane(int originX, int originY, boolean centerX, boolean centerY, Color bordercolor){
		super();							//Customized Chart (Custom start origin, custom axis centering and
											//border color)
		
		this.chart_panel = this.createChartPanel();
		if(bordercolor!=null)this.chart_panel.setBorder(BorderFactory.createLineBorder(bordercolor));
		this.chart_panel.setOpaque(false);
		
		this.setViewport(new ChartViewPort());
		this.setViewportView(this.chart_panel);
		
		this.horizontal_bar = this.getHorizontalScrollBar();
		this.vertical_bar = this.getVerticalScrollBar();
	    this.addMouseMotionListener(new DragMoveListener(this.getViewport(), this.chart_panel)); //Chart Drag'n'Drop Listener
	    //this.addMouseMotionListener(new DragMoveListenerScrollBars(this));
	    this.setOpaque(false);
	    
		this.originX = this.initX = originX;
		this.originY = this.initY = originY;
		this.centered_originX = centerX;
		this.centered_originY = centerY;

		this.stored_points = new ArrayList<Point2D_F64>();	    
		
		myself = this;
}

	
	public void addPoint(double x, double y){		
				stored_points.add(new Point2D_F64(x, y));
				
				if(!follow_new_points)chart_panel.repaint();
				else{
					chart_panel.repaint();
					
					int dispx = (int) Math.round(originX+(x*chart_scale));
					int dispy = (int) Math.round(originY-(y*chart_scale));
					
					int scroll_x = dispx-(getWidth()/2);
					int scroll_y = dispy-(getHeight()/2);
					
//					if((scroll_x<=horizontal_bar.getMinimum() || scroll_x>=horizontal_bar.getMaximum()) &&
//							(scroll_y<=vertical_bar.getMinimum() || scroll_y>=vertical_bar.getMaximum())){
//						chart_panel.repaint();
//					}else{
						horizontal_bar.setValue(scroll_x);
						vertical_bar.setValue(scroll_y);
//					}
					
				}
	}
	
	public void addEndPoint(){		
		stored_points.add(null); //Null point is detected as Chart End Point (and start of a new Chart)
		repaint();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Point2D_F64> getAllPoints(){
		return (ArrayList<Point2D_F64>) stored_points.clone();
	}
	
	public void setCenteredOriginX(boolean centered_originX){
		this.centered_originX = centered_originX;
	}
	
	public void setCenteredOriginY(boolean centered_originY){
		this.centered_originY = centered_originY;
	}

	public Point2D_F64 getOrigin(){
		return new Point2D_F64(originX, originY);
	}
	
	public void setOrigin(int originX, int originY){
		this.originX = this.initX = originX;
		this.originY = this.initY = originY;
	}
	
	public void clearAllPoints(){
		charts_count=0;
		stored_points.clear();
		this.originX = this.initX;
		this.originY = this.initY;
		repaint();
	}
	
	
	public boolean moveToOrigin(){
		int scroll_to_origin_x = originX-(getWidth()/2);
		int scroll_to_origin_y = originY-(getHeight()/2); 
		horizontal_bar.setValue(scroll_to_origin_x);
		vertical_bar.setValue(scroll_to_origin_y);
		return true;
	}
	
	public void moveToOriginAfterPaint(){
		move_to_origin_flag = true;
		repaint();
	}
	
	public boolean moveToLast(){
		if(stored_points==null) return false;
		try{
			int last_x=0, last_y=0;
			if(stored_points.get(stored_points.size()-1)!=null){
				last_x = (int) Math.round(originX+(stored_points.get(stored_points.size()-1).getX()*chart_scale));
				last_y = (int) Math.round(originY-(stored_points.get(stored_points.size()-1).getY()*chart_scale));
			}else {
				last_x = (int) Math.round(originX+(stored_points.get(stored_points.size()-2).getX()*chart_scale));
				last_y = (int) Math.round(originY-(stored_points.get(stored_points.size()-2).getY()*chart_scale));
			}
			int scroll_to_last_x = last_x-(getWidth()/2);
			int scroll_to_last_y = last_y-(getHeight()/2);
			horizontal_bar.setValue(scroll_to_last_x);
			vertical_bar.setValue(scroll_to_last_y);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	
	public boolean moveToPoint(int index){
		if(stored_points==null) return false;
		try{
			int point_x = (int) Math.round(originX+(stored_points.get(index).getX()*chart_scale));
			int point_y = (int) Math.round(originY-(stored_points.get(index).getY()*chart_scale));
			int scroll_to_point_x = point_x-(getWidth()/2);
			int scroll_to_point_y = point_y-(getHeight()/2);
			horizontal_bar.setValue(scroll_to_point_x);
			vertical_bar.setValue(scroll_to_point_y);
			
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	public boolean moveToPoint(double x, double y){
		int point_x = (int) Math.round(originX+(x*chart_scale));
		int point_y = (int) Math.round(originY-(y*chart_scale));
		int scroll_to_point_x = point_x-(getWidth()/2);
		int scroll_to_point_y = point_y-(getHeight()/2);
		horizontal_bar.setValue(scroll_to_point_x);
		vertical_bar.setValue(scroll_to_point_y);
		
		return true;
	}
	
	
	public void setChartScalingFactor(double chart_scale){
		
		this.chart_scale = chart_scale;
	}
	
	
	public double getChartScalingFactor(){
		return chart_scale;
	}
	
	
	public void resetSize(){
		
		this.originX = this.initX;
		this.originY = this.initY;
		
		this.chart_panel.setPreferredSize(new Dimension(0,0));
		this.setViewportView(chart_panel);
		
		moveToOriginAfterPaint();	//repaint();//paintImmediately(getVisibleRect());
			
	}
	
	
	public void setFollowNewPoints(boolean follow_new_points){
		this.follow_new_points = follow_new_points;
	}
	
	public void setShowLegend(boolean showlegend){
		this.show_legend = showlegend;
	}
	
	public void setMultipleColors_for_Charts(boolean multiplecolors){
		this.multiple_colors=multiplecolors;
	}
	
	public int getChartsCount(){
		int totalCharts = 0;
		for(Point2D_F64 P: stored_points){
			if(P==null)totalCharts++;
		}
		return totalCharts;
	}
	
	public void setBackgroundColor(Color background_color){
		this.background_color = background_color;
	}
	
	public void setChartColor(Color chart_color){
		this.chart_color = chart_color;
	}
	
	public void setThickPoints(boolean thick){
		this.thick_points = thick;
	}
	
	public void setShowAxis(boolean show_axis){
		this.show_axis = show_axis;
	}
	
	public void setShowAxisUnits(boolean show_axis_units){
		this.show_axis_units = show_axis_units;
	}

	public void setAxisNames(String x, String y){
		this.axis_x_name = x;
		this.axis_y_name = y;
	}
	
	public void setShowPermanentAxisNames(boolean permanent_axis_names){
		this.show_permanent_axis_names = permanent_axis_names;
	}
	
	
	public void setAxisColor(Color axis_color){
		this.axis_color = axis_color;
	}
	
	public void setAxisUnitsColor(Color axis_units_color){
		this.axis_units_color = axis_units_color;
	}
	
	public void setAxisNamesColor(Color axis_names_color){
		this.axis_names_color = axis_names_color;
	}
	
	
	
	
	
	
	private ImagePanel createChartPanel(){
		
		return new ImagePanel(){


			/**
			 * CHART PANEL (ChartScrollPane Component) 
			 */
			
			
			private static final long serialVersionUID = -7205365687339816088L;

			
			@Override
			public void paintComponent(Graphics g) {
				
				super.paintComponent(g);
				
				if(centered_originX){originX = (int)Math.round(this.getWidth()/2);}
				if(centered_originY){originY = (int)Math.round(this.getHeight()/2);}
				
				int chart_width = chart_panel.getWidth();
				int chart_height = chart_panel.getHeight();
				
				
				Graphics2D g1 = (Graphics2D)g;
				
				// Aggiornamento grafico
				if(g1==null) return;
				if(stored_points==null) return;
				
				int curr_chart=0;	
					try{
						
						if(stored_points.size()>0 && show_axis){							
							g1.setColor(axis_color!=null ? axis_color : Color.black);
							g1.drawLine(0, originY, this.getWidth(), originY);
							g1.drawLine(originX, 0, originX, this.getHeight());
							
							
							if(show_axis_units){
								if(axis_units_color!=null) g1.setColor(axis_units_color);
								g1.setFont(new Font("Arial",Font.BOLD,11));
								
								for(int x=0;x<this.getWidth();x++){
									
									int relative_x = x-originX;
									
									if((relative_x)%(10*chart_scale)==0) g1.drawLine(x, originY-2, x, originY+2);
									if((relative_x)%(50*chart_scale)==0){
										g1.drawLine(x+1, originY-2, x+1, originY+2);
										if(relative_x==0){
											g1.drawString(String.valueOf(Math.round(relative_x/chart_scale)), x+3, originY+15);
										}else if(relative_x>0 && relative_x<100){
											g1.drawString(String.valueOf(Math.round(relative_x/chart_scale)), x-7, originY+15);
										}else if(relative_x>=100){										
											g1.drawString(String.valueOf(Math.round(relative_x/chart_scale)), x-10, originY+15);
										}else if(relative_x<0 && relative_x>-100){
											g1.drawString(String.valueOf(Math.round(relative_x/chart_scale)), x-12, originY+15);
										}else if(relative_x<=-100){
											g1.drawString(String.valueOf(Math.round(relative_x/chart_scale)), x-15, originY+15);
										}
										
									}
								}
								for(int y=0;y<this.getHeight();y++){
									
									int relative_y = originY-y; //Because Height values in windows are measured from 0 on the TOP
																//so the Y values must be inverted to be positive on TOP
									if((relative_y)%(10*chart_scale)==0) g1.drawLine(originX-2, y, originX+2, y);
									if((relative_y)%(50*chart_scale)==0){
										g1.drawLine(originX-2, y+1, originX+2, y+1);
										if(relative_y>0){
											g1.drawString(String.valueOf(Math.round(relative_y/chart_scale)), originX+6, y+4);
										}else if(relative_y<0){
											g1.drawString(String.valueOf(Math.round(relative_y/chart_scale)), originX+9, y+4);
										}
										
									}									
								}
							}
						}
						
						Color draw_color = chart_color!=null ? chart_color : Color.blue;
						g1.setColor(draw_color);
						
						for(Point2D_F64 P: stored_points){
							
						if(P!=null){
							
							//int posx=200,posz=400;
							int scaledx = (int)(Math.round(P.getX()*chart_scale)); 
							int scaledy = (int)(Math.round(P.getY()*chart_scale)); 
						
							int dispx = originX + scaledx; 
							int dispy = originY - scaledy;			
						
							
							
							
							//CHART ADJUSTMENT IN CASE OF POINTS OUT OF THE CHART SIZE (AUTO-RESIZE)							if(dispx<1 || dispy<1 || dispx>chart_width || dispy>chart_height){
								if(dispx>chart_width){
									int new_width = dispx>(chart_width*2) ? dispx : (chart_width*2);
									setPreferredSize(new Dimension(new_width, chart_height));
									setViewportView(this);
									return;
								}
							
								if(dispy>chart_height){
									int new_height = dispy>(chart_height*2) ? dispy : (chart_height*2);
									setPreferredSize(new Dimension(chart_width, new_height));
									setViewportView(this);
									return;
								}
								
								if(dispx<1){
									int new_width = dispx<-(chart_width*2) ? dispx : (chart_width*2);
									if(!centered_originX){
									//originX += 1-dispx; 	//MOTO FLUIDO X (Origine spostata leggermente, Grafico si ridimensiona in modo aderente ai punti)
									originX += (int) Math.round(new_width/2); //Origine molto spostata, Grafico viene raddoppiato nella dimensione sforata (moto meno fluido)
									}
									
									setPreferredSize(new Dimension((int)(new_width), chart_height));
									setViewportView(this);
									return;
								}

								if(dispy<1){
									int new_height = dispy<-(chart_height*2) ? dispy : (chart_height*2);
									
									if(!centered_originY){
									//originY += 1-dispy;		//MOTO FLUIDO Y (Origine spostata leggermente, Grafico si ridimensiona in modo aderente ai punti)
									originY += (int) Math.round(new_height/2); //Origine molto spostata, Grafico viene raddoppiato nella dimensione sforata (moto meno fluido)
									}
									
									setPreferredSize(new Dimension(chart_width, (int)(new_height)));
									setViewportView(this);
									return;
								}
							

							//g1.drawLine(dispx, dispy, dispx+5, dispy);	//LINES RENDERING
							
							//thick_points=true;
							if(!thick_points){
								g1.fillOval(dispx-2, dispy-2, 4, 4);			//Circles Rendering
							}else{
								g1.fill3DRect(dispx-25, dispy-25, 50, 50, true);	//3D Thick Rendering
							}
						
						}else{
							curr_chart++;
							
							if(multiple_colors){								
								float[] hsb = Color.RGBtoHSB(draw_color.getRed(), draw_color.getGreen(), draw_color.getBlue(), null);
								
								float new_h = ((100*hsb[0]+ 10*curr_chart )%100)/100; //Color Wheel / 10 Different Colors
																			 //starting from the selected chart_color or from Blue
																			 //The wheel doesn't work starting with Black or White
																			 //Because there you need to alterate also the Saturation
																			 //and Brightness values
								
								g1.setColor(Color.getHSBColor(new_h, hsb[1], hsb[2]));
								
							}
						}
						
						}
						
						
					    if(stored_points!=null && stored_points.size()>0 
					    		&& show_axis && !show_permanent_axis_names){	//Draw Axis names only at the end of the axis
					    	g1.setFont(new Font("Arial",Font.PLAIN,14));
					    	if(axis_names_color!=null) g1.setColor(axis_names_color);
					    	else g1.setColor(axis_color!=null ? axis_color : Color.black);
					    	
					    	g1.drawString(axis_y_name, originX-15-((axis_y_name.length()-1)*8), 15);
					    	g1.drawString(axis_x_name, this.getWidth()-15-((axis_x_name.length()-1)*8), originY-10);
					    }
							
					}catch(Exception e){}
				
				charts_count=curr_chart;
				
				
				if(move_to_origin_flag){
					int scroll_to_origin_x = originX-(myself.getWidth()/2);
					int scroll_to_origin_y = originY-(myself.getHeight()/2); 
					horizontal_bar.setValue(scroll_to_origin_x);
					vertical_bar.setValue(scroll_to_origin_y);
					move_to_origin_flag = false;
				}
				//System.out.println("paint component");
				
			}
			
			
			
		};

	}
	

	private class ChartViewPort extends JViewport{
		
	

		/**
		 * 
		 */
		private static final long serialVersionUID = 6392963381400821425L;


		public ChartViewPort(){
			this.setOpaque(false); //Key Element to Redraw only needed bits
		}
		
		@Override
		public void paint(Graphics g) {	//ViewPort Paint Event --> Paints ABOVE  Contained Chart
		    super.paint(g);

		    
			Color draw_color = chart_color!=null ? chart_color : Color.blue;
			if(stored_points!=null && stored_points.size()>0 
					&& multiple_colors
					&& show_legend){
			
			
				for(int i=0;i<charts_count;i++){
					float[] hsb = Color.RGBtoHSB(draw_color.getRed(), draw_color.getGreen(), draw_color.getBlue(), null);
					
					float new_h = ((100*hsb[0]+ 10*i )%100)/100; //Color Wheel / 10 Different Colors
																 //starting from the selected chart_color or from Blue
																 //The wheel doesn't work starting with Black or White
																 //Because there you need to alterate also the Saturation
																 //and Brightness values
					
					g.setColor(Color.getHSBColor(new_h, hsb[1], hsb[2]));
					
//					int new_red = (draw_color.getRed()+100*i)%256;
//					int new_green = (draw_color.getGreen()+50*i)%256;
//					int new_blue = (draw_color.getBlue()+200*i)%256;
//					
//					g.setColor(new Color(new_red,new_green,new_blue));
					
					int xpos = this.getWidth()-70;
					int ypos = i==0 ? 20 : (20+(15*i));
					g.drawString("Chart "+(i+1), xpos, ypos);					
				}
			}
			
		    
		}
		
		
		@Override
		public void paintComponent(Graphics g) { //ViewPort PaintComponent Event --> Paints BELOW Contained Chart
		    super.paintComponent(g);
		    
		    if(background_color!=null){
		    	g.setColor(background_color);
		    	g.fillRect(0, 0, this.getWidth(), this.getHeight());
		    }
		    
		    if(stored_points!=null && stored_points.size()>0 
		    		&& show_axis && show_permanent_axis_names){
		    	if(axis_names_color!=null) g.setColor(axis_names_color);
		    	else g.setColor(axis_color!=null ? axis_color : Color.black);
		    	
		    	if(horizontal_bar.getValue()>=(originX-this.getWidth()) &&
		    			horizontal_bar.getValue()<originX){
		    		g.drawString(axis_y_name, originX-horizontal_bar.getValue()-15-((axis_y_name.length()-1)*8), 15);
		    	}
		    	if(vertical_bar.getValue()>=(originY-this.getHeight()) &&
		    			vertical_bar.getValue()<originY){
		    		g.drawString(axis_x_name, this.getWidth()-15-((axis_x_name.length()-1)*8), originY-vertical_bar.getValue()-10);
		    	}
		    }
		}

//		    g.setColor(Color.blue);
//		    g.fillRect(64, 64, 192, 192);
	}
	
	
	
	
	class DragMoveListener extends MouseInputAdapter
    implements MouseMotionListener{
        private final Cursor dc;
        private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        private final Rectangle rect = new Rectangle();
        private final JComponent comp;
        private final JViewport vport;
        private Point startPt = new Point();
        private Point move  = new Point();
 
        public DragMoveListener(JViewport vport, JComponent comp) {
            this.vport = vport;
            this.comp = comp;
            this.dc = comp.getCursor();
            vport.addMouseMotionListener(this);
            vport.addMouseListener(this);
        }
 
        public void mouseDragged(MouseEvent e) {
            Point pt = e.getPoint();
            move.setLocation(pt.x-startPt.x, pt.y-startPt.y);
            startPt.setLocation(pt);
            Rectangle vr = vport.getViewRect();
            int w = vr.width;
            int h = vr.height;
            Point ptZero = SwingUtilities.convertPoint(vport,0,0,comp);
            rect.setRect(ptZero.x-move.x, ptZero.y-move.y, w, h);
            comp.scrollRectToVisible(rect);
        }
         
        public void mousePressed(MouseEvent e) {
            comp.setCursor(hc);
            startPt.setLocation(e.getPoint());
        }
        public void mouseReleased(MouseEvent e) {
            comp.setCursor(dc);
        }
        
        public void mouseClicked(MouseEvent e){
        	this.vport.getParent().dispatchEvent(e);
        }
    }


	
	
	class DragMoveListenerScrollBars extends MouseInputAdapter implements MouseMotionListener {
	
		private Point2D_F64 start_point = new Point2D_F64(0,0);
		private Point2D_F64 start_bars_values = new Point2D_F64(0,0);
		private JScrollBar hbar;
		private JScrollBar vbar;
		
		public DragMoveListenerScrollBars(JScrollPane scrollpane){
			this.hbar = scrollpane.getHorizontalScrollBar();
			this.vbar = scrollpane.getVerticalScrollBar();
			scrollpane.addMouseListener(this);
		}
			
		@Override
		public void mouseReleased(MouseEvent evt) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent evt) {
			start_point.set(evt.getX(), evt.getY());
			start_bars_values.setX(hbar.getValue());
			start_bars_values.setY(vbar.getValue());
		}
			
		@Override
		public void mouseMoved(MouseEvent evt) {
			
		}
			
		@Override
		public void mouseDragged(MouseEvent evt) {
			
			int delta_x = -(int)Math.round((evt.getX()-start_point.getX()));
			int delta_y = -(int)Math.round((evt.getY()-start_point.getY()));
				
				
			hbar.setValue((int)start_bars_values.getX()+delta_x);
			vbar.setValue((int)start_bars_values.getY()+delta_y);
			
			start_point.set(evt.getX(), evt.getY());
			start_bars_values.setX(hbar.getValue());
			start_bars_values.setY(vbar.getValue());
		}

		
	}
	
}
