package voGui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import boofcv.gui.image.ImagePanel;

public class InfoScrollPane extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8073332997799761341L;

	//Contained Panel
	private ImagePanel				main_panel;
	private int						main_panel_width;
	private int						main_panel_height;
	
	//Info Components
	public JLabel					lbl_status;
	
	private ImagePanel				info_panel;
	
	private JLabel					lbl_info;
	public JLabel					lbl_processed_file;
	public JLabel					lbl_processed_frame;
	public JLabel					lbl_elapsed_time;
	
	private JLabel					lbl_pos;
	public JLabel					lbl_xpos;
	public JLabel					lbl_ypos;
	public JLabel					lbl_zpos;
	
	private JLabel					lbl_tracker_info;
	public JLabel					lbl_inliers;
	public JLabel					lbl_tracks;
	
	private JLabel					lbl_input_fps;
	public JLabel					lbl_input_fps_current;
	public JLabel					lbl_input_fps_average;
	
	private JLabel					lbl_vo_fps;
	public JLabel					lbl_vo_fps_current;
	public JLabel					lbl_vo_fps_average;
	
	private JLabel					lbl_buffer;
	public JProgressBar				progress_buffer_load;
	public JLabel					lbl_buffer_load;
	
	private JLabel					lbl_points;
	private JScrollPane				lst_points_scroll;
	public JList<Object>			lst_points;
	private ArrayList<String>		lst_data;
	
	private SpringLayout 			info_panel_layout;
	
	
	
	public InfoScrollPane(){
		this(null);
	}
	
	public InfoScrollPane(Color bordercolor){
		this(400, 630, bordercolor);
	}
	
	public InfoScrollPane(int content_width, int content_height, Color bordercolor){
		super();
		createMainPanel(content_width, content_height, bordercolor);
	}
	
	
	private void createMainPanel(int content_width, int content_height, Color bordercolor){

		main_panel = new ImagePanel();
		this.main_panel_width = content_width;
		this.main_panel_height = content_height;
		main_panel.setPreferredSize(new Dimension(content_width, content_height));
		main_panel.setBorder(BorderFactory.createLineBorder(bordercolor!=null ? bordercolor : Color.blue));
		
		lbl_status = new JLabel("<html><b>Status:</b></html>");
		
		createInfoPanel();
		
		main_panel.add(lbl_status);
		main_panel.add(info_panel);
		
		SpringLayout layout = new SpringLayout();
		
		layout.putConstraint(SpringLayout.NORTH, lbl_status, 5, SpringLayout.NORTH, main_panel);
		layout.putConstraint(SpringLayout.WEST, lbl_status, 5, SpringLayout.WEST, main_panel);
		layout.putConstraint(SpringLayout.EAST, lbl_status, -5, SpringLayout.EAST, main_panel);
		
		layout.putConstraint(SpringLayout.NORTH, info_panel, 10, SpringLayout.SOUTH, lbl_status);
		layout.putConstraint(SpringLayout.WEST, info_panel, 5, SpringLayout.WEST, main_panel);
		layout.putConstraint(SpringLayout.EAST, info_panel, -5, SpringLayout.EAST, main_panel);
		layout.putConstraint(SpringLayout.SOUTH, info_panel, -5, SpringLayout.SOUTH, main_panel);
		
		main_panel.setLayout(layout);
		
		this.setViewportView(main_panel);
	}


	private void createInfoPanel(){
	
		
		
		lbl_info = new JLabel("Elaboration Info:");
		Font font = lbl_info.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
		lbl_info.setFont(boldFont);
		
		
		lbl_processed_file = new JLabel("Processing File: ");
		lbl_processed_frame = new JLabel("Processing Frame: ");
		lbl_elapsed_time = new JLabel("Elapsed Time: ");
		
		lbl_pos = new JLabel("Current Position:");
		lbl_pos.setFont(boldFont);
		lbl_xpos = new JLabel("X: ");
		lbl_ypos = new JLabel("Y: ");
		lbl_zpos = new JLabel("Z: ");
		
		lbl_tracker_info = new JLabel("Tracker info:");
		lbl_tracker_info.setFont(boldFont);
		lbl_tracks = new JLabel("Total tracked features: ");
		lbl_inliers = new JLabel("Inliers (matches): ");
		
		lbl_input_fps = new JLabel("Input Source Framerate:");
		lbl_input_fps.setFont(boldFont);
		lbl_input_fps_current = new JLabel("<html><b>Current FPS:</b></html>");
		lbl_input_fps_average = new JLabel("<html><b>Average FPS:</b></html>");
		
		lbl_vo_fps = new JLabel("Visual Odometry Framerate:");
		lbl_vo_fps.setFont(boldFont);
		lbl_vo_fps_current = new JLabel("<html><b>Current FPS:</b></html>");
		lbl_vo_fps_average = new JLabel("<html><b>Average FPS:</b></html>");
		
		lbl_buffer = new JLabel("<html><b>Buffer Load:</b></html>");
		progress_buffer_load = new JProgressBar();
		lbl_buffer_load = new JLabel("");
		
		
		lbl_points = new JLabel("Found Points (Log):");
		lbl_points.setFont(boldFont);
		lst_data = new ArrayList<String>();
		lst_points = new JList<Object>(lst_data.toArray());
		lst_points.addListSelectionListener(new CopyOnSelectionListener());
		lst_points.addMouseListener(new CopyAllOnDblClick());
		
		lst_points_scroll = new JScrollPane(lst_points);
		
		info_panel = new ImagePanel();
		
		info_panel.add(lbl_info);
		info_panel.add(lbl_processed_file);
		info_panel.add(lbl_processed_frame);
		info_panel.add(lbl_elapsed_time);
		info_panel.add(lbl_pos);
		info_panel.add(lbl_xpos);
		info_panel.add(lbl_ypos);
		info_panel.add(lbl_zpos);
		info_panel.add(lbl_tracker_info);
		info_panel.add(lbl_inliers);
		info_panel.add(lbl_tracks);
		info_panel.add(lbl_input_fps);
		info_panel.add(lbl_input_fps_current);
		info_panel.add(lbl_input_fps_average);
		info_panel.add(lbl_vo_fps);
		info_panel.add(lbl_vo_fps_current);
		info_panel.add(lbl_vo_fps_average);
		info_panel.add(lbl_buffer);
		info_panel.add(progress_buffer_load);
		info_panel.add(lbl_buffer_load);
		info_panel.add(lbl_points);
		info_panel.add(lst_points_scroll);
		
		
		info_panel_layout = new SpringLayout();
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_info, 5, SpringLayout.NORTH, info_panel);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_info, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_info, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_processed_file, 10, SpringLayout.SOUTH, lbl_info);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_processed_file, 15, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_processed_file, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_processed_frame, 10, SpringLayout.SOUTH, lbl_processed_file);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_processed_frame, 15, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_processed_frame, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_elapsed_time, 10, SpringLayout.SOUTH, lbl_processed_frame);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_elapsed_time, 15, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_elapsed_time, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_pos, 10, SpringLayout.SOUTH, lbl_elapsed_time);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_pos, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_pos, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_xpos, 10, SpringLayout.SOUTH, lbl_pos);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_xpos, 15, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_xpos, -5, SpringLayout.EAST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_ypos, 10, SpringLayout.SOUTH, lbl_xpos);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_ypos, 15, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_ypos, -5, SpringLayout.EAST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_zpos, 10, SpringLayout.SOUTH, lbl_ypos);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_zpos, 15, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_zpos, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_tracker_info, 10, SpringLayout.SOUTH, lbl_zpos);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_tracker_info, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_tracker_info, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_tracks, 10, SpringLayout.SOUTH, lbl_tracker_info);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_tracks, 15, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_tracks, -5, SpringLayout.EAST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_inliers, 10, SpringLayout.SOUTH, lbl_tracks);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_inliers, 15, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_inliers, -5, SpringLayout.EAST, info_panel);
		
		
		
		Spring half_width = new Spring(){

			@Override
			public int getMaximumValue() {
				// TODO Auto-generated method stub
				return (int)Math.round(info_panel.getWidth()/2);
			}

			@Override
			public int getMinimumValue() {
				// TODO Auto-generated method stub
				return (int)Math.round(info_panel.getWidth()/2);
			}

			@Override
			public int getPreferredValue() {
				// TODO Auto-generated method stub
				return (int)Math.round(info_panel.getWidth()/2);
			}

			@Override
			public int getValue() {
				// TODO Auto-generated method stub
				return (int)Math.round(info_panel.getWidth()/2);
			}

			@Override
			public void setValue(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_input_fps, 10, SpringLayout.SOUTH, lbl_inliers);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_input_fps, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_input_fps, half_width, SpringLayout.WEST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_input_fps_current, 10, SpringLayout.SOUTH, lbl_input_fps);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_input_fps_current, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_input_fps_current, half_width, SpringLayout.WEST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_input_fps_average, 10, SpringLayout.SOUTH, lbl_input_fps_current);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_input_fps_average, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_input_fps_average, half_width, SpringLayout.WEST, info_panel);
		
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_vo_fps, 10, SpringLayout.SOUTH, lbl_inliers);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_vo_fps, half_width, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_input_fps, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_vo_fps_current, 10, SpringLayout.SOUTH, lbl_vo_fps);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_vo_fps_current, half_width, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_vo_fps_current, -5, SpringLayout.EAST, info_panel);
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_vo_fps_average, 10, SpringLayout.SOUTH, lbl_vo_fps_current);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_vo_fps_average, half_width, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_vo_fps_average, -5, SpringLayout.EAST, info_panel);
		
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_buffer, 10, SpringLayout.SOUTH, lbl_input_fps_average);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_buffer, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.NORTH, progress_buffer_load, 10, SpringLayout.SOUTH, lbl_input_fps_average);
		info_panel_layout.putConstraint(SpringLayout.WEST, progress_buffer_load, 3, SpringLayout.EAST, lbl_buffer);
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_buffer_load, 10, SpringLayout.SOUTH, lbl_input_fps_average);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_buffer_load, 3, SpringLayout.EAST, progress_buffer_load);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_buffer_load, -5, SpringLayout.EAST, info_panel);
		
		
		
		info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_points, 15, SpringLayout.SOUTH, lbl_buffer);
		info_panel_layout.putConstraint(SpringLayout.WEST, lbl_points, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lbl_points, -5, SpringLayout.EAST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.NORTH, lst_points_scroll, 10, SpringLayout.SOUTH, lbl_points);
		info_panel_layout.putConstraint(SpringLayout.WEST, lst_points_scroll, 5, SpringLayout.WEST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.EAST, lst_points_scroll, -5, SpringLayout.EAST, info_panel);
		info_panel_layout.putConstraint(SpringLayout.SOUTH, lst_points_scroll, -5, SpringLayout.SOUTH, info_panel);
		
		
		info_panel.setLayout(info_panel_layout);
	}
	
	
	public void addListData(String data){
		lst_data.add(data);
		lst_points.setListData(lst_data.toArray());
		lst_points.revalidate();
		lst_points.repaint();
		JScrollBar vbar = lst_points_scroll.getVerticalScrollBar();
		vbar.setValue(vbar.getMaximum());
	}
	
	public void removeListData(int index){
		if(index>=0 && index<lst_data.size())
		lst_data.remove(index);
		lst_points.setListData(lst_data.toArray());
		lst_points.revalidate();
		lst_points.repaint();
	}
	
	public void removeListData(Object obj){
		lst_data.remove(obj);
		lst_points.setListData(lst_data.toArray());
		lst_points.revalidate();
		lst_points.repaint();
	}
	
	public void clearListData(){
		lst_data.clear();
		lst_points.setListData(lst_data.toArray());
		lst_points.revalidate();
		lst_points.repaint();
	}
	
	
	
	public void setInfoPanelVisible(boolean visible){
		if(!visible){
			this.info_panel.setVisible(false);
			main_panel.setPreferredSize(new Dimension(this.main_panel_width,100));
		}else{
			this.info_panel.setVisible(true);
			main_panel.setPreferredSize(new Dimension(this.main_panel_width, this.main_panel_height));
		}
		
	}
	
	public boolean isInfoPanelVisible(){
		return this.info_panel.isVisible();
		
	}
	
	public void setBufferInfoVisible(boolean visible){
		if(!visible){
			
			if(!lbl_buffer.isVisible() && !progress_buffer_load.isVisible() && !lbl_buffer_load.isVisible()) return;
			
			info_panel_layout.removeLayoutComponent(lbl_buffer);
			info_panel_layout.removeLayoutComponent(progress_buffer_load);
			info_panel_layout.removeLayoutComponent(lbl_buffer_load);
			
			info_panel_layout.removeLayoutComponent(lbl_points);
			info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_points, 15, SpringLayout.SOUTH, lbl_input_fps_average);
			info_panel_layout.putConstraint(SpringLayout.WEST, lbl_points, 5, SpringLayout.WEST, info_panel);
			info_panel_layout.putConstraint(SpringLayout.EAST, lbl_points, -5, SpringLayout.EAST, info_panel);
			
			lbl_buffer.setVisible(false);
			progress_buffer_load.setVisible(false);
			lbl_buffer_load.setVisible(false);
			
			info_panel.paintAll(info_panel.getGraphics());
		}else{
			if(lbl_buffer.isVisible() && progress_buffer_load.isVisible() && lbl_buffer_load.isVisible()) return;
			
			info_panel_layout.removeLayoutComponent(lbl_points);
			info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_buffer, 10, SpringLayout.SOUTH, lbl_input_fps_average);
			info_panel_layout.putConstraint(SpringLayout.WEST, lbl_buffer, 5, SpringLayout.WEST, info_panel);
			info_panel_layout.putConstraint(SpringLayout.NORTH, progress_buffer_load, 10, SpringLayout.SOUTH, lbl_input_fps_average);
			info_panel_layout.putConstraint(SpringLayout.WEST, progress_buffer_load, 3, SpringLayout.EAST, lbl_buffer);
			info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_buffer_load, 10, SpringLayout.SOUTH, lbl_input_fps_average);
			info_panel_layout.putConstraint(SpringLayout.WEST, lbl_buffer_load, 3, SpringLayout.EAST, progress_buffer_load);
			info_panel_layout.putConstraint(SpringLayout.EAST, lbl_buffer_load, -5, SpringLayout.EAST, info_panel);
			info_panel_layout.putConstraint(SpringLayout.NORTH, lbl_points, 15, SpringLayout.SOUTH, lbl_buffer);
			info_panel_layout.putConstraint(SpringLayout.WEST, lbl_points, 5, SpringLayout.WEST, info_panel);
			info_panel_layout.putConstraint(SpringLayout.EAST, lbl_points, -5, SpringLayout.EAST, info_panel);
			
			
			lbl_buffer.setVisible(true);
			progress_buffer_load.setVisible(true);
			lbl_buffer_load.setVisible(true);
			
			info_panel.paintAll(info_panel.getGraphics());
		}
	}
	
	
	public class JMultilineLabel extends JTextArea{
	    private static final long serialVersionUID = 1L;
	    public JMultilineLabel(String text){
	        super(text);
	        setEditable(false);  
	        setCursor(null);  
	        setOpaque(false);  
	        setFocusable(false);  
	        setFont(UIManager.getFont("Label.font"));      
	        setWrapStyleWord(true);  
	        setLineWrap(true);
	    }
	} 
	
	public class JMultilineLabelHtml extends JEditorPane{
	    private static final long serialVersionUID = 1L;
	    public JMultilineLabelHtml(String text){
	    	// create a JEditorPane that renders HTML and defaults to the system font.
	        super(new HTMLEditorKit().getContentType(),text);
	        // set the text of the JEditorPane to the given text.
	        this.setText(text);
	        
	        // add a CSS rule to force body tags to use the default label font
	        // instead of the value in javax.swing.text.html.default.csss
	        Font font = UIManager.getFont("Label.font");
	        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
	                "font-size: " + font.getSize() + "pt; }";
	        ((HTMLDocument)this.getDocument()).getStyleSheet().addRule(bodyRule);

	        this.setOpaque(false);
//	        this.setBorder(null);
	        this.setEditable(false);
	        this.setCursor(null);  
	        this.setFocusable(false);  

	    }
	} 
	
	public class CopyOnSelectionListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent evt) {
			if(evt.getSource()==lst_points &&
					evt.getValueIsAdjusting()==false){
				String myString = (String)lst_points.getSelectedValue();				
				if(myString==null) return;
				
				String chart_type = myString.substring(myString.lastIndexOf(",")+1).trim();
				if(chart_type.equalsIgnoreCase("(Chart Type Y/f)") ||
						chart_type.equalsIgnoreCase("(Chart Type Y/s)")){
					myString = myString.substring(0,myString.lastIndexOf(","));
				}
				
				StringSelection stringSelection = new StringSelection (myString);
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents (stringSelection, null);
			}
			
		}

		
	}
	
	public class CopyAllOnDblClick implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			if(arg0.getClickCount()==2 && lst_data!=null && lst_data.size()>0){
					
				String myString="";
				for(String s:lst_data){
					
					String chart_type = s.substring(s.lastIndexOf(",")+1).trim();
					if(chart_type.equalsIgnoreCase("(Chart Type Y/f)") ||
							chart_type.equalsIgnoreCase("(Chart Type Y/s)")){
						s = s.substring(0,s.lastIndexOf(","));
					}
					
					myString = myString+s+"\n";
				}
				
				StringSelection stringSelection = new StringSelection (myString);
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				try{
				clpbrd.setContents (stringSelection, null);
				}catch(Exception e){}
				
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
}
