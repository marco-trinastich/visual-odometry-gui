package voGui;

import georegression.struct.point.Vector3D_F64;
import georegression.struct.se.Se3_F64;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import boofcv.abst.feature.tracker.PointTrack;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.sfm.AccessPointTracks3D;
import boofcv.abst.sfm.d3.DepthVisualOdometry;
import boofcv.abst.sfm.d3.MonocularPlaneVisualOdometry;
import boofcv.abst.sfm.d3.StereoVisualOdometry;
import boofcv.abst.sfm.d3.VisualOdometry;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImagePanel;
import boofcv.io.MediaManager;
import boofcv.io.VideoCallBack;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.calib.MonoPlaneParameters;
import boofcv.struct.calib.StereoParameters;
import boofcv.struct.calib.VisualDepthParameters;
import boofcv.struct.image.ImageDataType;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.ImageType.Family;



@SuppressWarnings("rawtypes")
public class VisualOdometryGUI <I extends ImageSingleBand, Depth extends ImageSingleBand> {

	
	//Main Frames and Panels
	private boolean					system_look_and_feel_enabled=false;
	private JFrame					main_frame; 
	private Hashtable<String, Component>	main_components = new Hashtable<String, Component>();
	private JButton 				btn_load;
	private JButton 				btn_save;
	private JButton 				btn_start;
	private JButton 				btn_pause;
	private JButton 				btn_reset;
	private JButton 				btn_stop;
	private JButton 				btn_clear;
	private JButton					btn_stop_capture;

	private	JFrame					chart_frame;
	private ChartScrollPane			chart_xz;
	private ChartScrollPane			chart_y;
	private InfoScrollPane			chart_info;
	private	JFrame					video_input_frame;
	private ImagePanel 				video_input_panel;
	private JFrame					video_vo_frame;
	private ImagePanel				video_vo_panel;
	
	
	
	//Settings Variables
	private String					calib_path=null;			//input settings
	private String					input_source=null;
	private String					video_path=null;
	private String					device_path=null;
	private boolean					device_sust_fps=false;
	private boolean					device_timeout_img=false;
	private boolean					device_keep_format=false;
	private int						device_width=0;
	private int						device_height=0;
	
	private Class<I> 				imgType;					//internal image settings
	private boolean					img_keep_original=true;
	private int						img_resize_width=0;
	private int						img_resize_height=0;
	private int						img_buffer_size=0;
	private boolean					decimate_enabled=false;
	private int						decimate_value=1;
	
	private String					tracker_type=null;			//tracker settings
	private boolean					tracker_show_active_tracks=true;
	private boolean					tracker_show_new_tracks=false;
	private int						klt_templateRadius=0;
	private String					klt_pyramidScaling=null;
	private int						klt_maxFeatures=0;
	private int						klt_radius=0;
	private float					klt_threshold=0;
	private int						surf_maxFeaturesPerScale=0;
	private int						surf_extractRadius=0;
	private int						surf_initialSampleSize=0;
	
	private String					vo_type=null;				//visual odometry settings
	private int						vo_thresholdAdd=0;
	private int						vo_thresholdRetire=0;
	private double					vo_inlierPixelTol=0;
	private int						vo_ransacIterations=0;
	
	private int						chart_type=0;				//chart/output settings
	private double					chart_xz_scale=1.0;
	private double					chart_y_scale=1.0;
	
	private boolean					fullres_preview=false;
	private boolean					only_vo_preview=false;
	private boolean					internal_image_preview=false;
	
	
	//Processing variables
	private MediaManager 			media = DefaultMediaManager.INSTANCE;
	private MonoPlaneParameters 	calibration;
	private StereoParameters		calibration_stereo;
	private VisualDepthParameters	calibration_depth;
	private SimpleImageSequence<I> 	video;
	private I						left_img;
	private PointTracker<I> 		tracker;
	private VisualOdometry<I>		visual_odometry;
	
	
	//Processing flags
	private boolean					pause_flag=false;
	private boolean					reset_vo_flag=false;
	private boolean					stop_flag=false;
	private boolean					clear_flag=false;
	private boolean					processing_flag=false;
	
	
	//Device Variables and Flags (Camera capture flags)
	private ArrayList<BufferedImage> buffer=new ArrayList<BufferedImage>();;
	private boolean 				stop_capture=false;		
	private boolean					isStopped_capture=true;
	
	
	
	
	public static void main(String args[]){
		VisualOdometryGUI app = new VisualOdometryGUI();
		app.createGUI();
	}	
	
	
	@SuppressWarnings("unchecked")
	public void createGUI(){
		
		 try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			system_look_and_feel_enabled = true;			
		}  catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			system_look_and_feel_enabled = false;
        }

		 

		SpringLayout frame_layout = null;
		SpringLayout panel_layout = null;
		
		
		//MAIN FRAME INIT		
		main_frame = new JFrame("Visual Odometry");		
		main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		

		//Components Initialization 
		
		//TITLE LABEL			
		final JLabel lbl_title = new JLabel("<html><b>Visual Odometry GUI (BoofCV)</b></html>");
			
								
		//INPUT SETTINGS PANEL / INIT+LAYOUT			
		final JPanel panel_input_settings;		
		{ 
		
			final JLabel lbl_calib = new JLabel("<html><b>Calibration:</b></html>");
			String calibs[] = {"../data/applet/vo/drc/mono_plane.xml"};
			
			final JComboBox<String>	txt_calib = new JComboBox<String>(calibs);
			txt_calib.setEditable(true);
			
			final JTextComponent txt_calib_tc = (JTextComponent)txt_calib.getEditor().getEditorComponent();
			txt_calib_tc.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent evt) {
					calib_path = txt_calib_tc.getText();
				}
				
				@Override
				public void insertUpdate(DocumentEvent evt) {
					calib_path = txt_calib_tc.getText();
				}
				
				@Override
				public void changedUpdate(DocumentEvent evt) {
					calib_path = txt_calib_tc.getText();
				}
			});
			main_components.put("txt_calib", txt_calib);
			calib_path = (String) txt_calib.getSelectedItem();
			
			
			
			final JButton btn_browsecalib = new JButton("...");
			btn_browsecalib.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					String path = (String)txt_calib.getSelectedItem();
					
					JFileChooser browse = new JFileChooser(path);
					browse.setDialogTitle("Open Calibration");
					browse.setFileFilter(new FileFilter() {
						
						@Override
						public String getDescription() {
							return "XML Camera Calibration File (*.xml)";
						}
						
						@Override
						public boolean accept(File file) {
							return file.getName().endsWith(".xml")||file.isDirectory();
						}
					});
					
					
					if(browse.showOpenDialog(main_frame)==0){
					File choice = browse.getSelectedFile();					
					txt_calib_tc.setText(choice.getAbsolutePath());
					}
					
				}
			});
			
			
			
			
			final JLabel lbl_input = new JLabel("<html><b>Source</b></html>");
			
			final JRadioButton opt_input_video = new JRadioButton("<html><b>Video:</b></html>");
			opt_input_video.setSelected(true);		
			main_components.put("opt_input_video", opt_input_video);
			
			
			String videos[] = {"../data/applet/vo/drc/left.mjpeg","/home/marco/Scrivania/smart400.mjpeg","/Documents/Videos/esterno.mjpeg"};
			final JComboBox<String>	txt_video = new JComboBox<String>(videos);		
			txt_video.setEditable(true);
			
			final JTextComponent txt_video_tc = (JTextComponent) txt_video.getEditor().getEditorComponent();
			txt_video_tc.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent evt) {
					video_path = txt_video_tc.getText();
				}
				
				@Override
				public void insertUpdate(DocumentEvent evt) {
					video_path = txt_video_tc.getText();
				}
				
				@Override
				public void changedUpdate(DocumentEvent evt) {
					video_path = txt_video_tc.getText();
				}
			});
			main_components.put("txt_video", txt_video);
			video_path = (String) txt_video.getSelectedItem();
			
			
			
			final JButton btn_browsevideo = new JButton("...");
			btn_browsevideo.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					String path = (String)txt_video.getSelectedItem();
					
					JFileChooser browse = new JFileChooser(path);
					browse.setDialogTitle("Open Video");
					browse.setFileFilter(new FileFilter() {
						
						@Override
						public String getDescription() {
							return "Motion JPEG Video (*.mjpeg)";
						}
						
						@Override
						public boolean accept(File file) {
							return file.getName().endsWith(".mjpeg")||file.isDirectory();
						}
					});
					
					if(browse.showOpenDialog(main_frame)==0){
					File choice = browse.getSelectedFile();
					txt_video_tc.setText(choice.getAbsolutePath());
					}
					
				}
			});
			
			
			final JRadioButton opt_input_device = new JRadioButton("<html>Device:</html>");
			main_components.put("opt_input_device", opt_input_device);
			
			
			String devices[] = {"/dev/video0","/dev/video1"};
			final JComboBox<String>	txt_device = new JComboBox<String>(devices);		
			txt_device.setEditable(true);
			txt_device.setEnabled(false);
			
			final JTextComponent txt_device_tc = (JTextComponent) txt_device.getEditor().getEditorComponent();
			txt_device_tc.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent evt) {
					device_path = txt_device_tc.getText();
				}
				
				@Override
				public void insertUpdate(DocumentEvent evt) {
					device_path = txt_device_tc.getText();
				}
				
				@Override
				public void changedUpdate(DocumentEvent evt) {
					device_path = txt_device_tc.getText();
				}
			});
			main_components.put("txt_device", txt_device);
			device_path = (String) txt_device.getSelectedItem();
			

			
			
			final JLabel lbl_device_width = new JLabel("<html>Width:</html>");
			lbl_device_width.setEnabled(false);
			
			final JTextField txt_device_width = new JTextField("320",4);
			txt_device_width.setHorizontalAlignment(JTextField.CENTER);
			txt_device_width.setEnabled(false);

			txt_device_width.addFocusListener(new FocusListener() {

				String	last_size;
				
				@Override
				public void focusLost(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						int curr_size = Integer.parseInt(txt_device_width.getText());
						
						if(curr_size>0){ device_width = curr_size; }
						else if(curr_size==0){ txt_device_width.setText(last_size); }
						else if(curr_size<0){ 
							txt_device_width.setText(String.valueOf(-curr_size)); 
							device_width = -curr_size;
						}
						
					}catch(Exception e){
						txt_device_width.setText(last_size);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						Integer.parseInt(txt_device_width.getText());
						last_size = txt_device_width.getText();
					}catch(Exception e){
						last_size = String.valueOf(device_width);
					}
				}
			});
			main_components.put("txt_device_width", txt_device_width);
			device_width = Integer.parseInt(txt_device_width.getText());
			

			
			final JLabel lbl_device_height = new JLabel("<html>Height:</html>");
			lbl_device_height.setEnabled(false);
			
			final JTextField txt_device_height = new JTextField("240",4);
			txt_device_height.setHorizontalAlignment(JTextField.CENTER);
			txt_device_height.setEnabled(false);
			
			txt_device_height.addFocusListener(new FocusListener() {

				String	last_size;
				
				@Override
				public void focusLost(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						int curr_size = Integer.parseInt(txt_device_height.getText());
						
						if(curr_size>0){ device_height = curr_size; }
						else if(curr_size==0){ txt_device_height.setText(last_size); }
						else if(curr_size<0){ 
							txt_device_height.setText(String.valueOf(-curr_size)); 
							device_height = -curr_size;
						}
						
					}catch(Exception e){
						txt_device_height.setText(last_size);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						Integer.parseInt(txt_device_height.getText());
						last_size = txt_device_height.getText();
					}catch(Exception e){
						last_size = String.valueOf(device_height);
					}
				}
			});
			main_components.put("txt_device_height", txt_device_height);
			device_height = Integer.parseInt(txt_device_height.getText());


			
			final JCheckBox chk_device_sust_fps = new JCheckBox("<html>Sustain framerate</html>");
			chk_device_sust_fps.setSelected(false);
			chk_device_sust_fps.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					device_sust_fps = chk_device_sust_fps.isSelected();
					chk_device_sust_fps.setText(device_sust_fps ? "<html><b>Sustain framerate</b></html>" : "<html>Sustain framerate</html>");
				}
			});
			main_components.put("chk_device_sust_fps", chk_device_sust_fps);
			device_sust_fps = chk_device_sust_fps.isSelected();
			
			
			final JCheckBox chk_device_timeout_img = new JCheckBox("<html>Timeout image I/O</html>");
			chk_device_timeout_img.setSelected(false);
			chk_device_timeout_img.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					device_timeout_img = chk_device_timeout_img.isSelected();
					chk_device_timeout_img.setText(device_timeout_img ? "<html><b>Timeout image I/O</b></html>" : "<html>Timeout image I/O</html>");
				}
			});
			main_components.put("chk_device_timeout_img", chk_device_timeout_img);
			device_timeout_img = chk_device_timeout_img.isSelected();
			
			
			final JCheckBox chk_device_keep_format = new JCheckBox("<html>Keep format</html>");
			chk_device_keep_format.setSelected(false);	
			chk_device_keep_format.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					device_keep_format = chk_device_keep_format.isSelected();
					chk_device_keep_format.setText(device_keep_format ? "<html><b>Keep format</b></html>" : "<html>Keep format</html>");
				}
			});
			main_components.put("chk_device_keep_format", chk_device_keep_format);
			device_keep_format = chk_device_keep_format.isSelected();
			
			
			
			
			final JPanel panel_device_adj = new JPanel();
			panel_device_adj.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html>Device Adjustments</html>"),
					  BorderFactory.createEmptyBorder(5,5,5,5)));
			panel_device_adj.setEnabled(false);
			chk_device_sust_fps.setEnabled(false);
			chk_device_timeout_img.setEnabled(false);
			chk_device_keep_format.setEnabled(false);
			
			
			panel_device_adj.add(chk_device_sust_fps);
			panel_device_adj.add(chk_device_timeout_img);
			panel_device_adj.add(chk_device_keep_format);
			panel_device_adj.add(lbl_device_width);
			panel_device_adj.add(txt_device_width);
			panel_device_adj.add(lbl_device_height);
			panel_device_adj.add(txt_device_height);
			
			panel_layout = new SpringLayout();
			
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_device_width, 3,SpringLayout.NORTH, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_device_width, 3, SpringLayout.WEST, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_device_width, 0,SpringLayout.NORTH, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.WEST, txt_device_width, 3, SpringLayout.EAST, lbl_device_width);
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_device_height, 3,SpringLayout.NORTH, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_device_height, 3, SpringLayout.EAST, txt_device_width);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_device_height, 0,SpringLayout.NORTH, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.WEST, txt_device_height, 3, SpringLayout.EAST, lbl_device_height);
			panel_layout.putConstraint(SpringLayout.NORTH, chk_device_sust_fps, 3, SpringLayout.NORTH, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.WEST, chk_device_sust_fps, 3, SpringLayout.EAST, txt_device_height);
			
			panel_layout.putConstraint(SpringLayout.NORTH, chk_device_timeout_img, 8,SpringLayout.SOUTH, lbl_device_width);
			panel_layout.putConstraint(SpringLayout.WEST, chk_device_timeout_img, 3, SpringLayout.WEST, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.NORTH, chk_device_keep_format, 8,SpringLayout.SOUTH, lbl_device_width);
			panel_layout.putConstraint(SpringLayout.WEST, chk_device_keep_format, 0, SpringLayout.WEST, chk_device_sust_fps);
			
			panel_layout.putConstraint(SpringLayout.SOUTH, panel_device_adj, 0, SpringLayout.SOUTH, chk_device_timeout_img);
			
			panel_device_adj.setLayout(panel_layout);
			
			
			
			
			
			
			input_source = "video";
			//Radio Options Action Listeners
			opt_input_video.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					opt_input_video.setSelected(true);
					opt_input_video.setText("<html><b>Video:</b></html>");
					txt_video.setEnabled(true);
					btn_browsevideo.setEnabled(true);
					
					opt_input_device.setSelected(false);
					opt_input_device.setText("<html>Device:</html>");
					txt_device.setEnabled(false);
					panel_device_adj.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html>Device Adjustments</html>"),
							  BorderFactory.createEmptyBorder(5,5,5,5)));
					panel_device_adj.setEnabled(false);
					for(Component comp: panel_device_adj.getComponents()){
						comp.setEnabled(false);
					}
					lbl_device_width.setText("<html>Width:</html>");
					lbl_device_height.setText("<html>Height:</html>");
					
					main_components.get("lbl_img_buff_size").setEnabled(false);
					main_components.get("txt_img_buff_size").setEnabled(false);
					
					input_source = "video";
				}
			});
			
			opt_input_device.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					opt_input_video.setSelected(false);
					opt_input_video.setText("<html>Video:</html>");
					txt_video.setEnabled(false);
					btn_browsevideo.setEnabled(false);
					
					opt_input_device.setSelected(true);
					opt_input_device.setText("<html><b>Device:</b></html>");
					txt_device.setEnabled(true);
					panel_device_adj.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Device Adjustments</b></html>"),
							  BorderFactory.createEmptyBorder(5,5,5,5)));
					panel_device_adj.setEnabled(true);
					for(Component comp: panel_device_adj.getComponents()){
						comp.setEnabled(true);
					}		
					lbl_device_width.setText("<html><b>Width:</b></html>");
					lbl_device_height.setText("<html><b>Height:</b></html>");
					
					main_components.get("lbl_img_buff_size").setEnabled(true);
					main_components.get("txt_img_buff_size").setEnabled(true);
					
					input_source = "device";	
				}
			});

			
			
			
			
			
			final JCheckBox chk_fullres_preview = new JCheckBox("Full-Resolution Preview");
			chk_fullres_preview.setSelected(false);
			
			chk_fullres_preview.addActionListener(new ActionListener() {
			
				@Override
				public void actionPerformed(ActionEvent evt) {
					fullres_preview = chk_fullres_preview.isSelected();
					chk_fullres_preview.setText(fullres_preview ? "<html><b>Full-Resolution Preview</b></html>" : "<html>Full-Resolution Preview</html>");
				}
			});
			main_components.put("chk_fullres_preview", chk_fullres_preview);
			fullres_preview = chk_fullres_preview.isSelected();
			
			
			
			final JCheckBox chk_only_vo_preview = new JCheckBox("<html>VO Preview Only (Faster)</html>");
			chk_only_vo_preview.setSelected(false);
			
			chk_only_vo_preview.addActionListener(new ActionListener() {
			
				@Override
				public void actionPerformed(ActionEvent evt) {
					only_vo_preview = chk_only_vo_preview.isSelected();
					chk_only_vo_preview.setText(only_vo_preview ? "<html><b>VO Preview Only (Faster)</b></html>" : "<html>VO Preview Only (Faster)</html>");
				}
			});
			main_components.put("chk_only_vo_preview", chk_only_vo_preview);
			only_vo_preview = chk_only_vo_preview.isSelected();
			

			
			panel_input_settings = new JPanel();
			panel_input_settings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Input Settings</b></html>"),
																		  BorderFactory.createEmptyBorder(5,5,5,5)));
			
			
			panel_input_settings.add(lbl_calib);
			panel_input_settings.add(txt_calib);
			panel_input_settings.add(btn_browsecalib);
			panel_input_settings.add(lbl_input);
			panel_input_settings.add(opt_input_video);
			panel_input_settings.add(txt_video);
			panel_input_settings.add(btn_browsevideo);
			panel_input_settings.add(opt_input_device);
			panel_input_settings.add(txt_device);
			panel_input_settings.add(panel_device_adj);
			panel_input_settings.add(chk_fullres_preview);
			panel_input_settings.add(chk_only_vo_preview);
			
			
			panel_layout = new SpringLayout();
			
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_calib, 0, SpringLayout.NORTH, panel_input_settings);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_calib, 3, SpringLayout.WEST, panel_input_settings);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_calib, -3,SpringLayout.NORTH, lbl_calib);
			panel_layout.putConstraint(SpringLayout.WEST, txt_calib, 3, SpringLayout.EAST, lbl_calib);
			panel_layout.putConstraint(SpringLayout.EAST, txt_calib, -35, SpringLayout.EAST, panel_input_settings);
			panel_layout.putConstraint(SpringLayout.NORTH, btn_browsecalib, 0, SpringLayout.NORTH, txt_calib);
			panel_layout.putConstraint(SpringLayout.WEST, btn_browsecalib, -30, SpringLayout.EAST, btn_browsecalib);
			panel_layout.putConstraint(SpringLayout.EAST, btn_browsecalib, -3, SpringLayout.EAST, panel_input_settings);
			panel_layout.putConstraint(SpringLayout.SOUTH, btn_browsecalib, 0, SpringLayout.SOUTH, txt_calib);
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_input, 10, SpringLayout.SOUTH, lbl_calib);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_input, 3, SpringLayout.WEST, panel_input_settings);
	
			panel_layout.putConstraint(SpringLayout.NORTH, opt_input_video, 5, SpringLayout.SOUTH, lbl_input);
			panel_layout.putConstraint(SpringLayout.WEST, opt_input_video, 10, SpringLayout.WEST, lbl_input);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_video, 1,SpringLayout.NORTH, opt_input_video);
			panel_layout.putConstraint(SpringLayout.WEST, txt_video, 0, SpringLayout.WEST, txt_device);	// lato sx di txt_video associato al sx di txt_device perchè lbl_video è più corta
			panel_layout.putConstraint(SpringLayout.EAST, txt_video, -35, SpringLayout.EAST, panel_input_settings);
			panel_layout.putConstraint(SpringLayout.NORTH, btn_browsevideo, 0, SpringLayout.NORTH, txt_video);
			panel_layout.putConstraint(SpringLayout.WEST, btn_browsevideo, -30, SpringLayout.EAST, btn_browsevideo);
			panel_layout.putConstraint(SpringLayout.EAST, btn_browsevideo, -3, SpringLayout.EAST, panel_input_settings);
			panel_layout.putConstraint(SpringLayout.SOUTH, btn_browsevideo, 0, SpringLayout.SOUTH, txt_video);
			
			panel_layout.putConstraint(SpringLayout.NORTH, opt_input_device, 7, SpringLayout.SOUTH, opt_input_video);
			panel_layout.putConstraint(SpringLayout.WEST, opt_input_device, 10, SpringLayout.WEST, lbl_input);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_device, 1,SpringLayout.NORTH, opt_input_device);
			panel_layout.putConstraint(SpringLayout.WEST, txt_device, 3, SpringLayout.EAST, opt_input_device);
			panel_layout.putConstraint(SpringLayout.EAST, txt_device, -3, SpringLayout.EAST, panel_input_settings);
	
			
			panel_layout.putConstraint(SpringLayout.NORTH, panel_device_adj, 7, SpringLayout.SOUTH, opt_input_device);
			panel_layout.putConstraint(SpringLayout.WEST, panel_device_adj, 5, SpringLayout.WEST, opt_input_device);
			panel_layout.putConstraint(SpringLayout.EAST, panel_device_adj, -5, SpringLayout.EAST, panel_input_settings);
			
			panel_layout.putConstraint(SpringLayout.NORTH, chk_fullres_preview, 5,SpringLayout.SOUTH, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.WEST, chk_fullres_preview, 0, SpringLayout.WEST, lbl_input);
			panel_layout.putConstraint(SpringLayout.NORTH, chk_only_vo_preview, 5,SpringLayout.SOUTH, panel_device_adj);
			panel_layout.putConstraint(SpringLayout.WEST, chk_only_vo_preview, 3, SpringLayout.EAST, chk_fullres_preview);

			
			panel_layout.putConstraint(SpringLayout.SOUTH, panel_input_settings, 0, SpringLayout.SOUTH, chk_fullres_preview);
	
			panel_input_settings.setLayout(panel_layout);
			
			
		}

		
		//INTERNAL IMAGE SETTINGS PANEL / INIT+LAYOUT
		final JPanel panel_image_settings;
		{
			final JLabel lbl_imgtype = new JLabel("<html><b>Image Type:</b></html>");
			String imgtypes[] = {"ImageUInt8 (8bit Int Unsigned)","ImageFloat32 (32bit Float)"};
			final JComboBox<String>	txt_imgtype = new JComboBox<String>(imgtypes);		
			txt_imgtype.setSelectedIndex(1);
			
			txt_imgtype.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					// TODO Auto-generated method stub
					if(txt_imgtype.getSelectedIndex()==0){
						imgType = ((Class<I>) ImageUInt8.class);
						
					}else if(txt_imgtype.getSelectedIndex()==1){
						imgType = ((Class<I>) ImageFloat32.class);
					}	
				}
			});
			main_components.put("txt_imgtype", txt_imgtype);
			imgType = ((Class<I>) ImageFloat32.class);
			
			
			final JLabel lbl_imgdim = new JLabel("<html><b>Resize:</b></html>");
		
			
			final JLabel lbl_img_width = new JLabel("<html>Width:</html>");
			
			final JTextField txt_img_width = new JTextField("400",4);
			txt_img_width.setHorizontalAlignment(JTextField.CENTER);
			txt_img_width.setEnabled(false);
			
			txt_img_width.addFocusListener(new FocusListener() {

				String	last_size;
				
				@Override
				public void focusLost(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						int curr_size = Integer.parseInt(txt_img_width.getText());
						
						if(curr_size>0){ img_resize_width = curr_size; }
						else if(curr_size==0){ txt_img_width.setText(last_size); }
						else if(curr_size<0){ 
							txt_img_width.setText(String.valueOf(-curr_size)); 
							img_resize_width = -curr_size;
						}
						
					}catch(Exception e){
						txt_img_width.setText(last_size);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						Integer.parseInt(txt_img_width.getText());
						last_size = txt_img_width.getText();
					}catch(Exception e){
						last_size = String.valueOf(img_resize_width);
					}
				}
			});
			main_components.put("txt_img_width", txt_img_width);
			img_resize_width = Integer.parseInt(txt_img_width.getText());
			

			
			final JLabel lbl_img_height = new JLabel("<html>Height:</html>");
			
			final JTextField txt_img_height = new JTextField("400",4);
			txt_img_height.setHorizontalAlignment(JTextField.CENTER);
			txt_img_height.setEnabled(false);
			
			txt_img_height.addFocusListener(new FocusListener() {

				String	last_size;
				
				@Override
				public void focusLost(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						int curr_size = Integer.parseInt(txt_img_height.getText());
						
						if(curr_size>0){ img_resize_height = curr_size; }
						else if(curr_size==0){ txt_img_height.setText(last_size); }
						else if(curr_size<0){ 
							txt_img_height.setText(String.valueOf(-curr_size)); 
							img_resize_height = -curr_size;
						}
						
					}catch(Exception e){
						txt_img_height.setText(last_size);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						Integer.parseInt(txt_img_height.getText());
						last_size = txt_img_height.getText();
					}catch(Exception e){
						last_size = String.valueOf(img_resize_height);
					}
				}
			});
			main_components.put("txt_img_height", txt_img_height);
			img_resize_height = Integer.parseInt(txt_img_height.getText());
			
			
			final JCheckBox chk_img_keep_original = new JCheckBox("<html><b>Keep original aspect</b></html>");
			chk_img_keep_original.setSelected(true);		
			main_components.put("chk_img_keep_original", chk_img_keep_original);
			img_keep_original = chk_img_keep_original.isSelected();
		
			chk_img_keep_original.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					// TODO Auto-generated method stub
					img_keep_original = chk_img_keep_original.isSelected();
					if(chk_img_keep_original.isSelected()){
						chk_img_keep_original.setText("<html><b>Keep original aspect</b></html>");
						lbl_img_width.setText("<html>Width:</html>");
						lbl_img_height.setText("<html>Height:</html>");
						txt_img_width.setEnabled(false);
						txt_img_height.setEnabled(false);					
					}else{
						chk_img_keep_original.setText("<html>Keep original aspect</html>");
						lbl_img_width.setText("<html><b>Width:</b></html>");
						lbl_img_height.setText("<html><b>Height:</b></html>");
						txt_img_width.setEnabled(true);
						txt_img_height.setEnabled(true);
					}
				}
			});
			
			
			
			final JCheckBox chk_internal_image_preview = new JCheckBox("<html>Preview Internal Image (Slower)</html>");
			chk_internal_image_preview.setSelected(false);
			
			chk_internal_image_preview.addActionListener(new ActionListener() {
			
				@Override
				public void actionPerformed(ActionEvent evt) {
					internal_image_preview = chk_internal_image_preview.isSelected();
					chk_internal_image_preview.setText(internal_image_preview ? "<html><b>Preview Internal Image (Slower)</b></html>" : "<html>Preview Internal Image (Slower)</html>");
				}
			});
			main_components.put("chk_internal_image_preview", chk_internal_image_preview);
			internal_image_preview = chk_internal_image_preview.isSelected();
			
			
			
			
			final JLabel lbl_img_buff_size = new JLabel("<html>Image Buffer Size (Device Only):</html>");
			lbl_img_buff_size.setEnabled(false);
			
			final JTextField txt_img_buff_size = new JTextField("infinity",4);
			txt_img_buff_size.setHorizontalAlignment(JTextField.CENTER);
			txt_img_buff_size.setEditable(true);
			txt_img_buff_size.setEnabled(false);
			
			txt_img_buff_size.addFocusListener(new FocusListener() {

				String	last_buff_size;
				
				@Override
				public void focusLost(FocusEvent evt) {
					lbl_img_buff_size.setText("<html>Image Buffer Size (Device Only):</html>");
					try{
						int curr_buff_size = Integer.parseInt(txt_img_buff_size.getText());
						
						if(curr_buff_size>0){ img_buffer_size = curr_buff_size; }
						else if(curr_buff_size==0){
							txt_img_buff_size.setText("infinity");
							img_buffer_size = 0;
						}
						else if(curr_buff_size<0){ 
							txt_img_buff_size.setText(String.valueOf(-curr_buff_size));
							img_buffer_size = -curr_buff_size;
						}
						
					}catch(Exception e){
						if(txt_img_buff_size.getText().equals("") || txt_img_buff_size.getText().equals("infinity")){
							txt_img_buff_size.setText("infinity");
							img_buffer_size = 0;
						}else{
							if(last_buff_size.equals("0")){
								txt_img_buff_size.setText("infinity");
							}else{
								txt_img_buff_size.setText(last_buff_size);
							}
						}
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					lbl_img_buff_size.setText("<html><b>Image Buffer Size</b> (Device Only):</html>");
					try{
						Integer.parseInt(txt_img_buff_size.getText());
						last_buff_size = txt_img_buff_size.getText();
					}catch(Exception e){						
						last_buff_size = String.valueOf(img_buffer_size);
					}
				}
			});
			main_components.put("lbl_img_buff_size", lbl_img_buff_size);
			main_components.put("txt_img_buff_size", txt_img_buff_size);
			
			
			
			
			//DECIMATE FRAMES
			final JCheckBox chk_decimate = new JCheckBox("<html>Decimate frames by</html>");
			{
				chk_decimate.addActionListener(new ActionListener(){
						
					@Override
					public void actionPerformed(ActionEvent evt) {
						//DECIMATE FRAMES
						if(chk_decimate.isSelected()){
							chk_decimate.setText("<html><b>Decimate frames by</b></html>");
							decimate_enabled=true;
						}else{
							chk_decimate.setText("<html>Decimate frames by</html>");
							decimate_enabled=false;
						}
					}
									
				});
			}
			
			final JTextField txt_decimate = new JTextField("1",3);
			txt_decimate.setHorizontalAlignment(JTextField.CENTER);
			
			txt_decimate.addFocusListener(new FocusListener() {

				String	last_value;
				
				@Override
				public void focusLost(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						int curr_value = Integer.parseInt(txt_decimate.getText());
						
						if(curr_value>0){ 
							decimate_value = curr_value; 
						}else if(curr_value==0){
							txt_decimate.setText(last_value);
						}else if(curr_value<0){ 
							txt_decimate.setText(String.valueOf(-curr_value));
							decimate_value = -curr_value;
						}
						
					}catch(Exception e){
						txt_decimate.setText(last_value);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					// TODO Auto-generated method stub
					try{
						Integer.parseInt(txt_decimate.getText());
						last_value = txt_decimate.getText();
					}catch(Exception e){
						last_value = String.valueOf(decimate_value);
					}
				}
			});
			main_components.put("txt_decimate", txt_decimate);


			
			
			
			panel_image_settings = new JPanel();
			panel_image_settings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Internal Images Settings:</b></html>"),
																		  BorderFactory.createEmptyBorder(5,5,5,5)));
			
			panel_image_settings.add(lbl_imgtype);
			panel_image_settings.add(txt_imgtype);
	
			panel_image_settings.add(lbl_imgdim);
			panel_image_settings.add(chk_img_keep_original);
			panel_image_settings.add(chk_internal_image_preview);
			
			panel_image_settings.add(lbl_img_width);
			panel_image_settings.add(txt_img_width);
			panel_image_settings.add(lbl_img_height);
			panel_image_settings.add(txt_img_height);
			
			panel_image_settings.add(chk_decimate);
			panel_image_settings.add(txt_decimate);
			
			panel_image_settings.add(lbl_img_buff_size);
			panel_image_settings.add(txt_img_buff_size);
			
			
			panel_layout = new SpringLayout();
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_imgtype, 0, SpringLayout.NORTH, panel_image_settings);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_imgtype, 3, SpringLayout.WEST, panel_image_settings);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_imgtype, -3,SpringLayout.NORTH, lbl_imgtype);
			panel_layout.putConstraint(SpringLayout.WEST, txt_imgtype, 3, SpringLayout.EAST, lbl_imgtype);
			panel_layout.putConstraint(SpringLayout.EAST, txt_imgtype, -3, SpringLayout.EAST, panel_image_settings);
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_imgdim, 10, SpringLayout.SOUTH, lbl_imgtype);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_imgdim, 3, SpringLayout.WEST, panel_image_settings);
			
			panel_layout.putConstraint(SpringLayout.NORTH, chk_img_keep_original, 5, SpringLayout.SOUTH, lbl_imgdim);
			panel_layout.putConstraint(SpringLayout.WEST, chk_img_keep_original, 10, SpringLayout.WEST, lbl_imgdim);
			panel_layout.putConstraint(SpringLayout.NORTH, chk_internal_image_preview, 5, SpringLayout.SOUTH, lbl_imgdim);
			panel_layout.putConstraint(SpringLayout.WEST, chk_internal_image_preview, 3, SpringLayout.EAST, chk_img_keep_original);
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_img_width, 5,SpringLayout.SOUTH, chk_img_keep_original);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_img_width, 10, SpringLayout.WEST, lbl_imgdim);	
			panel_layout.putConstraint(SpringLayout.NORTH, txt_img_width, -1,SpringLayout.NORTH, lbl_img_width);
			panel_layout.putConstraint(SpringLayout.WEST, txt_img_width, 3, SpringLayout.EAST, lbl_img_width);	
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_img_height, 0,SpringLayout.NORTH, lbl_img_width);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_img_height, 3, SpringLayout.EAST, txt_img_width);	
			panel_layout.putConstraint(SpringLayout.NORTH, txt_img_height, -1,SpringLayout.NORTH, lbl_img_height);
			panel_layout.putConstraint(SpringLayout.WEST, txt_img_height, 3, SpringLayout.EAST, lbl_img_height);	
	
			panel_layout.putConstraint(SpringLayout.NORTH, chk_decimate, 15, SpringLayout.SOUTH, lbl_img_width);
			panel_layout.putConstraint(SpringLayout.WEST, chk_decimate, 3, SpringLayout.WEST, panel_image_settings);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_decimate, 0, SpringLayout.NORTH, chk_decimate);
			panel_layout.putConstraint(SpringLayout.WEST, txt_decimate, 5, SpringLayout.EAST, chk_decimate);
	
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_img_buff_size, 10, SpringLayout.SOUTH, chk_decimate);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_img_buff_size, 3, SpringLayout.WEST, panel_image_settings);	
			panel_layout.putConstraint(SpringLayout.NORTH, txt_img_buff_size, -1,SpringLayout.NORTH, lbl_img_buff_size);
			panel_layout.putConstraint(SpringLayout.WEST, txt_img_buff_size, 3, SpringLayout.EAST, lbl_img_buff_size);	
			
			
			panel_layout.putConstraint(SpringLayout.SOUTH, panel_image_settings, 0, SpringLayout.SOUTH, lbl_img_buff_size);
	
			panel_image_settings.setLayout(panel_layout);
		}
		
		
		//TRACKER SETTINGS PANEL / INIT+LAYOUT
		final JPanel panel_tracker_settings=new JPanel();
		{
			final JLabel lbl_tracker = new JLabel("<html><b>Tracker Type:</b></html>");
			
			String trackers[] = {"KLT (Standard)", "KLT (Two Pass)", "Surf (Standard)", "Surf (Dda Two Pass)", "Default Tracker (KLT)"};
			final JComboBox<String>	txt_tracker = new JComboBox<String>(trackers);		
			txt_tracker.setSelectedIndex(1);
			
			txt_tracker.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					SpringLayout currLayout = (SpringLayout)panel_tracker_settings.getLayout();
					JPanel panel_klt = (JPanel)main_components.get("panel_klt");
					JPanel panel_surf = (JPanel)main_components.get("panel_surf");
					JCheckBox chk_tracker_show_active_tracks = (JCheckBox) main_components.get("chk_tracker_show_active_tracks");
					
					switch(txt_tracker.getSelectedIndex()){
					case 0:
					case 1:
						if(!panel_klt.isVisible()){
							
							panel_klt.setVisible(true);
							panel_surf.setVisible(false);
							
							currLayout.removeLayoutComponent(panel_surf);
							currLayout.removeLayoutComponent(chk_tracker_show_active_tracks);
							currLayout.putConstraint(SpringLayout.NORTH, panel_klt, 5, SpringLayout.SOUTH, lbl_tracker);
							currLayout.putConstraint(SpringLayout.WEST, panel_klt, 3, SpringLayout.WEST, panel_tracker_settings);
							currLayout.putConstraint(SpringLayout.EAST, panel_klt, 0, SpringLayout.EAST, panel_tracker_settings);
							currLayout.putConstraint(SpringLayout.NORTH, chk_tracker_show_active_tracks, 2, SpringLayout.SOUTH, panel_klt);
							currLayout.putConstraint(SpringLayout.WEST, chk_tracker_show_active_tracks, 0, SpringLayout.WEST, lbl_tracker);
							
							panel_tracker_settings.paintAll(panel_tracker_settings.getGraphics());
						}
						
						tracker_type = txt_tracker.getSelectedIndex()==0 ? "klt" : "klt2";
						break;
					case 2:
					case 3:
						if(!panel_surf.isVisible()){
							
							panel_surf.setVisible(true);
							panel_klt.setVisible(false);
							
							currLayout.removeLayoutComponent(panel_klt);
							currLayout.removeLayoutComponent(chk_tracker_show_active_tracks);
							currLayout.putConstraint(SpringLayout.NORTH, panel_surf, 5, SpringLayout.SOUTH, lbl_tracker);
							currLayout.putConstraint(SpringLayout.WEST, panel_surf, 3, SpringLayout.WEST, panel_tracker_settings);
							currLayout.putConstraint(SpringLayout.EAST, panel_surf, 0, SpringLayout.EAST, panel_tracker_settings);
							currLayout.putConstraint(SpringLayout.NORTH, chk_tracker_show_active_tracks, 2, SpringLayout.SOUTH, panel_surf);
							currLayout.putConstraint(SpringLayout.WEST, chk_tracker_show_active_tracks, 0, SpringLayout.WEST, lbl_tracker);
							
							panel_tracker_settings.paintAll(panel_tracker_settings.getGraphics());
						}

						tracker_type = txt_tracker.getSelectedIndex()==2 ? "surf" : "surf2";
						break;
					case 4:
					default:
						if(!panel_klt.isVisible()){
							
							panel_klt.setVisible(true);
							panel_surf.setVisible(false);
							
							currLayout.removeLayoutComponent(panel_surf);
							currLayout.removeLayoutComponent(chk_tracker_show_active_tracks);
							currLayout.putConstraint(SpringLayout.NORTH, panel_klt, 5, SpringLayout.SOUTH, lbl_tracker);
							currLayout.putConstraint(SpringLayout.WEST, panel_klt, 3, SpringLayout.WEST, panel_tracker_settings);
							currLayout.putConstraint(SpringLayout.EAST, panel_klt, 0, SpringLayout.EAST, panel_tracker_settings);
							currLayout.putConstraint(SpringLayout.NORTH, chk_tracker_show_active_tracks, 2, SpringLayout.SOUTH, panel_klt);
							currLayout.putConstraint(SpringLayout.WEST, chk_tracker_show_active_tracks, 0, SpringLayout.WEST, lbl_tracker);
							
							panel_tracker_settings.paintAll(panel_tracker_settings.getGraphics());
						}

						tracker_type = "default";
						break;
					}
				}
			});
			main_components.put("txt_tracker", txt_tracker);
			tracker_type = "klt2";
			
			
			
			final JLabel lbl_klt_templateRadius = new JLabel("<html>Template Radius:</html>");
			
			final JTextField txt_klt_templateRadius = new JTextField("3",5);
			txt_klt_templateRadius.setHorizontalAlignment(JTextField.CENTER);
			
			txt_klt_templateRadius.addFocusListener(new FocusListener() {

				String	last;
				
				@Override
				public void focusLost(FocusEvent evt) {
					try{
						int curr = Integer.parseInt(txt_klt_templateRadius.getText());						
						klt_templateRadius = curr;						
					}catch(Exception e){
						txt_klt_templateRadius.setText(last);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					try{
						Integer.parseInt(txt_klt_templateRadius.getText());
						last = txt_klt_templateRadius.getText();
					}catch(Exception e){
						last = String.valueOf(klt_templateRadius);
					}
				}
			});
			main_components.put("txt_klt_templateRadius", txt_klt_templateRadius);
			klt_templateRadius = Integer.parseInt(txt_klt_templateRadius.getText());
			
			
			
			final JLabel lbl_klt_pyramidScaling = new JLabel("<html>Pyramid Scaling:</html>");
			
			final JTextField txt_klt_pyramidScaling = new JTextField("1,2,4,8",5);		
			txt_klt_pyramidScaling.setHorizontalAlignment(JTextField.CENTER);
			
			txt_klt_pyramidScaling.addFocusListener(new FocusListener() {


				@Override
				public void focusLost(FocusEvent evt) {
					klt_pyramidScaling = txt_klt_pyramidScaling.getText();						
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
				}
			});
			main_components.put("txt_klt_pyramidScaling", txt_klt_pyramidScaling);
			klt_pyramidScaling = txt_klt_pyramidScaling.getText();
			
			
			
			final JLabel lbl_klt_maxFeatures = new JLabel("<html>Max Features:</html>");
			
			final JTextField txt_klt_maxFeatures = new JTextField("600",5);		
			txt_klt_maxFeatures.setHorizontalAlignment(JTextField.CENTER);
			
			txt_klt_maxFeatures.addFocusListener(new FocusListener() {

				String	last;
				
				@Override
				public void focusLost(FocusEvent evt) {
					try{
						int curr = Integer.parseInt(txt_klt_maxFeatures.getText());						
						klt_maxFeatures = curr;						
					}catch(Exception e){
						txt_klt_maxFeatures.setText(last);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					try{
						Integer.parseInt(txt_klt_maxFeatures.getText());
						last = txt_klt_maxFeatures.getText();
					}catch(Exception e){
						last = String.valueOf(klt_maxFeatures);
					}
				}
			});
			main_components.put("txt_klt_maxFeatures", txt_klt_maxFeatures);
			klt_maxFeatures = Integer.parseInt(txt_klt_maxFeatures.getText());
			
			
			
			final JLabel lbl_klt_radius = new JLabel("<html>Radius:</html>");
			
			final JTextField txt_klt_radius = new JTextField("3",5);		
			txt_klt_radius.setHorizontalAlignment(JTextField.CENTER);
			
			txt_klt_radius.addFocusListener(new FocusListener() {

				String	last;
				
				@Override
				public void focusLost(FocusEvent evt) {
					try{
						int curr = Integer.parseInt(txt_klt_radius.getText());						
						klt_radius = curr;						
					}catch(Exception e){
						txt_klt_radius.setText(last);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					try{
						Integer.parseInt(txt_klt_radius.getText());
						last = txt_klt_radius.getText();
					}catch(Exception e){
						last = String.valueOf(klt_radius);
					}
				}
			});
			main_components.put("txt_klt_radius", txt_klt_radius);
			klt_radius = Integer.parseInt(txt_klt_radius.getText());
			
			
			
			final JLabel lbl_klt_threshold = new JLabel("<html>Threshold:</html>");
			
			final JTextField txt_klt_threshold = new JTextField("1.00",5);		
			txt_klt_threshold.setHorizontalAlignment(JTextField.CENTER);
			
			txt_klt_threshold.addFocusListener(new FocusListener() {

				String	last;
				
				@Override
				public void focusLost(FocusEvent evt) {
					try{
						float curr = Float.parseFloat(txt_klt_threshold.getText());						
						klt_threshold = curr;						
					}catch(Exception e){
						txt_klt_threshold.setText(last);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					try{
						Float.parseFloat(txt_klt_threshold.getText());
						last = txt_klt_threshold.getText();
					}catch(Exception e){
						last = String.valueOf(klt_threshold);
					}
				}
			});
			main_components.put("txt_klt_threshold", txt_klt_threshold);
			klt_threshold = Float.parseFloat(txt_klt_threshold.getText());
			
			
			
			final JPanel panel_klt = new JPanel();
			main_components.put("panel_klt",panel_klt);
			
			panel_klt.add(lbl_klt_templateRadius);
			panel_klt.add(txt_klt_templateRadius);
			panel_klt.add(lbl_klt_pyramidScaling);
			panel_klt.add(txt_klt_pyramidScaling);
			panel_klt.add(lbl_klt_maxFeatures);
			panel_klt.add(txt_klt_maxFeatures);
			panel_klt.add(lbl_klt_radius);
			panel_klt.add(txt_klt_radius);
			panel_klt.add(lbl_klt_threshold);
			panel_klt.add(txt_klt_threshold);
			
			panel_layout = new SpringLayout();
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_klt_templateRadius, 5, SpringLayout.NORTH, panel_klt);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_klt_templateRadius, 0, SpringLayout.WEST, panel_klt);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_klt_templateRadius, -1,SpringLayout.NORTH, lbl_klt_templateRadius);
			panel_layout.putConstraint(SpringLayout.WEST, txt_klt_templateRadius, 3, SpringLayout.EAST, lbl_klt_templateRadius);
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_klt_pyramidScaling, 0, SpringLayout.NORTH, lbl_klt_templateRadius);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_klt_pyramidScaling, 3, SpringLayout.EAST, txt_klt_templateRadius);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_klt_pyramidScaling, -1,SpringLayout.NORTH, lbl_klt_pyramidScaling);
			panel_layout.putConstraint(SpringLayout.WEST, txt_klt_pyramidScaling, 3, SpringLayout.EAST, lbl_klt_pyramidScaling);
			
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_klt_maxFeatures, 10, SpringLayout.SOUTH, lbl_klt_templateRadius);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_klt_maxFeatures, 0, SpringLayout.WEST, panel_klt);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_klt_maxFeatures, -1,SpringLayout.NORTH, lbl_klt_maxFeatures);
			panel_layout.putConstraint(SpringLayout.WEST, txt_klt_maxFeatures, 0, SpringLayout.WEST, txt_klt_templateRadius);
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_klt_radius, 0, SpringLayout.NORTH, lbl_klt_maxFeatures);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_klt_radius, 3, SpringLayout.EAST, txt_klt_maxFeatures);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_klt_radius, -1,SpringLayout.NORTH, lbl_klt_radius);
			panel_layout.putConstraint(SpringLayout.WEST, txt_klt_radius, 3, SpringLayout.EAST, lbl_klt_radius);
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_klt_threshold, 0, SpringLayout.NORTH, lbl_klt_radius);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_klt_threshold, 3, SpringLayout.EAST, txt_klt_radius);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_klt_threshold, -1,SpringLayout.NORTH, lbl_klt_threshold);
			panel_layout.putConstraint(SpringLayout.WEST, txt_klt_threshold, 3, SpringLayout.EAST, lbl_klt_threshold);
	
			panel_layout.putConstraint(SpringLayout.SOUTH, panel_klt, 0, SpringLayout.SOUTH, txt_klt_maxFeatures);
			
			panel_klt.setLayout(panel_layout);
			
			
			

			
			final JLabel lbl_surf_maxFeaturesPerScale = new JLabel("<html>Max Features Per Scale:</html>");
			
			final JTextField txt_surf_maxFeaturesPerScale = new JTextField("200",5);		
			txt_surf_maxFeaturesPerScale.setHorizontalAlignment(JTextField.CENTER);
			
			txt_surf_maxFeaturesPerScale.addFocusListener(new FocusListener() {

				String	last;
				
				@Override
				public void focusLost(FocusEvent evt) {
					try{
						int curr = Integer.parseInt(txt_surf_maxFeaturesPerScale.getText());						
						surf_maxFeaturesPerScale = curr;						
					}catch(Exception e){
						txt_surf_maxFeaturesPerScale.setText(last);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					try{
						Integer.parseInt(txt_surf_maxFeaturesPerScale.getText());
						last = txt_surf_maxFeaturesPerScale.getText();
					}catch(Exception e){
						last = String.valueOf(surf_maxFeaturesPerScale);
					}
				}
			});
			main_components.put("txt_surf_maxFeaturesPerScale", txt_surf_maxFeaturesPerScale);
			surf_maxFeaturesPerScale = Integer.parseInt(txt_surf_maxFeaturesPerScale.getText());
			
			

			final JLabel lbl_surf_extractRadius = new JLabel("<html>Extract Radius:</html>");
			
			final JTextField txt_surf_extractRadius = new JTextField("3",5);		
			txt_surf_extractRadius.setHorizontalAlignment(JTextField.CENTER);
			
			txt_surf_extractRadius.addFocusListener(new FocusListener() {

				String	last;
				
				@Override
				public void focusLost(FocusEvent evt) {
					try{
						int curr = Integer.parseInt(txt_surf_extractRadius.getText());						
						surf_extractRadius = curr;						
					}catch(Exception e){
						txt_surf_extractRadius.setText(last);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					try{
						Integer.parseInt(txt_surf_extractRadius.getText());
						last = txt_surf_extractRadius.getText();
					}catch(Exception e){
						last = String.valueOf(surf_extractRadius);
					}
				}
			});
			main_components.put("txt_surf_extractRadius", txt_surf_extractRadius);
			surf_extractRadius = Integer.parseInt(txt_surf_extractRadius.getText());
			
			
			

			final JLabel lbl_surf_initialSampleSize = new JLabel("<html>Initial Sample Size:</html>");
			
			final JTextField txt_surf_initialSampleSize = new JTextField("2",5);		
			txt_surf_initialSampleSize.setHorizontalAlignment(JTextField.CENTER);
			
			txt_surf_initialSampleSize.addFocusListener(new FocusListener() {

				String	last;
				
				@Override
				public void focusLost(FocusEvent evt) {
					try{
						int curr = Integer.parseInt(txt_surf_initialSampleSize.getText());						
						surf_initialSampleSize = curr;						
					}catch(Exception e){
						txt_surf_initialSampleSize.setText(last);
					}
				}
				
				@Override
				public void focusGained(FocusEvent evt) {
					try{
						Integer.parseInt(txt_surf_initialSampleSize.getText());
						last = txt_surf_initialSampleSize.getText();
					}catch(Exception e){
						last = String.valueOf(surf_initialSampleSize);
					}
				}
			});
			main_components.put("txt_surf_initialSampleSize", txt_surf_initialSampleSize);
			surf_initialSampleSize = Integer.parseInt(txt_surf_initialSampleSize.getText());
			
			
			
			final JPanel panel_surf = new JPanel();
			panel_surf.setVisible(false);
			main_components.put("panel_surf",panel_surf);
			
			panel_surf.add(lbl_surf_maxFeaturesPerScale);
			panel_surf.add(txt_surf_maxFeaturesPerScale);
			panel_surf.add(lbl_surf_extractRadius);
			panel_surf.add(txt_surf_extractRadius);
			panel_surf.add(lbl_surf_initialSampleSize);
			panel_surf.add(txt_surf_initialSampleSize);
			
			panel_layout = new SpringLayout();
			
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_surf_maxFeaturesPerScale, 5, SpringLayout.NORTH, panel_surf);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_surf_maxFeaturesPerScale, 0, SpringLayout.WEST, panel_surf);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_surf_maxFeaturesPerScale, -1,SpringLayout.NORTH, lbl_surf_maxFeaturesPerScale);
			panel_layout.putConstraint(SpringLayout.WEST, txt_surf_maxFeaturesPerScale, 3, SpringLayout.EAST, lbl_surf_maxFeaturesPerScale);
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_surf_extractRadius, 0, SpringLayout.NORTH, lbl_surf_maxFeaturesPerScale);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_surf_extractRadius, 3, SpringLayout.EAST, txt_surf_maxFeaturesPerScale);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_surf_extractRadius, -1,SpringLayout.NORTH, lbl_surf_extractRadius);
			panel_layout.putConstraint(SpringLayout.WEST, txt_surf_extractRadius, 3, SpringLayout.EAST, lbl_surf_extractRadius);
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_surf_initialSampleSize, 10, SpringLayout.SOUTH, lbl_surf_maxFeaturesPerScale);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_surf_initialSampleSize, 0, SpringLayout.WEST, panel_surf);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_surf_initialSampleSize, -1,SpringLayout.NORTH, lbl_surf_initialSampleSize);
			panel_layout.putConstraint(SpringLayout.WEST, txt_surf_initialSampleSize, 3, SpringLayout.EAST, lbl_surf_initialSampleSize);
	
			panel_layout.putConstraint(SpringLayout.SOUTH, panel_surf, 0, SpringLayout.SOUTH, txt_surf_initialSampleSize);
			
			panel_surf.setLayout(panel_layout);
			
			
			

			
			
			final JCheckBox chk_tracker_show_active_tracks = new JCheckBox("<html><b>Show Active Tracks</b></html>");
			chk_tracker_show_active_tracks.setSelected(true);		
			main_components.put("chk_tracker_show_active_tracks", chk_tracker_show_active_tracks);
			tracker_show_active_tracks = chk_tracker_show_active_tracks.isSelected();
		
			chk_tracker_show_active_tracks.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					tracker_show_active_tracks = chk_tracker_show_active_tracks.isSelected();
					if(chk_tracker_show_active_tracks.isSelected()){
						chk_tracker_show_active_tracks.setText("<html><b>Show Active Tracks</b></html>");					
					}else{
						chk_tracker_show_active_tracks.setText("<html>Show Active Tracks</html>");
					}
				}
			});
	
			final JCheckBox chk_tracker_show_new_tracks = new JCheckBox("<html>Show New Tracks</html>");
			chk_tracker_show_new_tracks.setSelected(false);		
			main_components.put("chk_tracker_show_new_tracks", chk_tracker_show_new_tracks);
			tracker_show_new_tracks = chk_tracker_show_new_tracks.isSelected();
		
			chk_tracker_show_new_tracks.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent evt) {
					tracker_show_new_tracks = chk_tracker_show_new_tracks.isSelected();
					if(chk_tracker_show_new_tracks.isSelected()){
						chk_tracker_show_new_tracks.setText("<html><b>Show New Tracks</b></html>");					
					}else{
						chk_tracker_show_new_tracks.setText("<html>Show New Tracks</html>");
					}
				}
			});
	
			
	
			
			panel_tracker_settings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Tracker Settings:</b></html>"),
																		  BorderFactory.createEmptyBorder(5,5,5,0)));
			
			panel_tracker_settings.add(lbl_tracker);
			panel_tracker_settings.add(txt_tracker);
			
			panel_tracker_settings.add(panel_klt);
			panel_tracker_settings.add(panel_surf);
			
			panel_tracker_settings.add(chk_tracker_show_active_tracks);
			panel_tracker_settings.add(chk_tracker_show_new_tracks);
			
			panel_layout = new SpringLayout();
			
			panel_layout.putConstraint(SpringLayout.NORTH, lbl_tracker, 0, SpringLayout.NORTH, panel_tracker_settings);
			panel_layout.putConstraint(SpringLayout.WEST, lbl_tracker, 3, SpringLayout.WEST, panel_tracker_settings);
			panel_layout.putConstraint(SpringLayout.NORTH, txt_tracker, -3,SpringLayout.NORTH, lbl_tracker);
			panel_layout.putConstraint(SpringLayout.WEST, txt_tracker, 3, SpringLayout.EAST, lbl_tracker);
			panel_layout.putConstraint(SpringLayout.EAST, txt_tracker, -3, SpringLayout.EAST, panel_tracker_settings);
	
			panel_layout.putConstraint(SpringLayout.NORTH, panel_klt, 5, SpringLayout.SOUTH, lbl_tracker);
			panel_layout.putConstraint(SpringLayout.WEST, panel_klt, 3, SpringLayout.WEST, panel_tracker_settings);
			panel_layout.putConstraint(SpringLayout.EAST, panel_klt, 0, SpringLayout.EAST, panel_tracker_settings);
			
					
			panel_layout.putConstraint(SpringLayout.NORTH, chk_tracker_show_active_tracks, 2, SpringLayout.SOUTH, panel_klt);
			panel_layout.putConstraint(SpringLayout.WEST, chk_tracker_show_active_tracks, 0, SpringLayout.WEST, lbl_tracker);
			panel_layout.putConstraint(SpringLayout.NORTH, chk_tracker_show_new_tracks, 0, SpringLayout.NORTH, chk_tracker_show_active_tracks);
			panel_layout.putConstraint(SpringLayout.WEST, chk_tracker_show_new_tracks, 3, SpringLayout.EAST, chk_tracker_show_active_tracks);
			
			
			panel_layout.putConstraint(SpringLayout.SOUTH, panel_tracker_settings, 0, SpringLayout.SOUTH, chk_tracker_show_active_tracks);
			
			panel_tracker_settings.setLayout(panel_layout);
			
			
			//templateRadius 3
			//pyramidScaling new int[]{1,2,4,8}
			//int maxFeatures 200/600, int radius 3, float threshold 1
			
		}
	
//		FactoryVisualOdometry.monoPlaneInfinity(thresholdAdd, thresholdRetire, inlierPixelTol, ransacIterations, tracker, imageType)
//		FactoryVisualOdometry.monoPlaneOverhead(cellSize, maxCellsPerPixel, mapHeightFraction, inlierGroundTol, ransacIterations, thresholdRetire, absoluteMinimumTracks, respawnTrackFraction, respawnCoverageFraction, tracker, imageType)
//		FactoryVisualOdometry.stereoDepth(inlierPixelTol, thresholdAdd, thresholdRetire, ransacIterations, refineIterations, doublePass, sparseDisparity, tracker, imageType)
//		FactoryVisualOdometry.stereoDualTrackerPnP(thresholdAdd, thresholdRetire, inlierPixelTol, epipolarPixelTol, ransacIterations, refineIterations, trackerLeft, trackerRight, descriptor, imageType)
//		FactoryVisualOdometry.stereoQuadPnP(inlierPixelTol, epipolarPixelTol, maxDistanceF2F, maxAssociationError, ransacIterations, refineIterations, detector, imageType)
//		FactoryVisualOdometry.depthDepthPnP(inlierPixelTol, thresholdAdd, thresholdRetire, ransacIterations, refineIterations, doublePass, sparseDepth, tracker, visualType, depthType)
	
		//VISUAL ODOMETRY SETTINGS PANEL / INIT+LAYOUT
				final JPanel panel_vo_settings;
				{

					final JLabel lbl_vo_type = new JLabel("<html><b>VO Type:</b></html>");
					
					String vodometry[] = {"monoPlaneInfinity", "monoPlaneOverhead", "stereoDepth", "stereoDualTrackerPnP", "stereoQuadPnP", "depthDepthPnP","Default Visual Odometry (monoPlaneInfinity)"};
					final JComboBox<String>	txt_vo_type = new JComboBox<String>(vodometry);		
					txt_vo_type.setSelectedIndex(0);
					
					txt_vo_type.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent evt) {
							// TODO Auto-generated method stub
							switch(txt_vo_type.getSelectedIndex()){
							case 0:
								vo_type = "monoPlaneInfinity";
								break;
							case 1:
								vo_type = "monoPlaneOverhead";
								break;
							case 2:
								vo_type = "stereoDepth";
								break;
							case 3:
								vo_type = "stereoDualTrackerPnP";
								break;
							case 4:
								vo_type = "stereoQuadPnP";
								break;
							case 5:
								vo_type = "depthDepthPnP";
								break;
							case 6:
							default:
								vo_type = "default";
								break;
							}
						}
					});
					main_components.put("txt_vo_type", txt_vo_type);
					vo_type = "monoPlaneInfinity";
					
					
					final JLabel lbl_vo_thresholdAdd = new JLabel("<html>thresholdAdd:</html>");
					
					final JTextField txt_vo_thresholdAdd = new JTextField("75",5);
					txt_vo_thresholdAdd.setHorizontalAlignment(JTextField.CENTER);
					
					txt_vo_thresholdAdd.addFocusListener(new FocusListener() {

						String	last;
						
						@Override
						public void focusLost(FocusEvent evt) {
							try{
								int curr = Integer.parseInt(txt_vo_thresholdAdd.getText());						
								vo_thresholdAdd = curr;						
							}catch(Exception e){
								txt_vo_thresholdAdd.setText(last);
							}
						}
						
						@Override
						public void focusGained(FocusEvent evt) {
							try{
								Integer.parseInt(txt_vo_thresholdAdd.getText());
								last = txt_vo_thresholdAdd.getText();
							}catch(Exception e){
								last = String.valueOf(vo_thresholdAdd);
							}
						}
					});
					main_components.put("txt_vo_thresholdAdd", txt_vo_thresholdAdd);
					vo_thresholdAdd = Integer.parseInt(txt_vo_thresholdAdd.getText());
					
					
					
					final JLabel lbl_vo_thresholdRetire = new JLabel("<html>thresholdRetire:</html>");
					
					final JTextField txt_vo_thresholdRetire = new JTextField("2",5);		
					txt_vo_thresholdRetire.setHorizontalAlignment(JTextField.CENTER);
					
					txt_vo_thresholdRetire.addFocusListener(new FocusListener() {

						String	last;
						
						@Override
						public void focusLost(FocusEvent evt) {
							try{
								int curr = Integer.parseInt(txt_vo_thresholdRetire.getText());						
								vo_thresholdRetire = curr;						
							}catch(Exception e){
								txt_vo_thresholdRetire.setText(last);
							}
						}
						
						@Override
						public void focusGained(FocusEvent evt) {
							try{
								Integer.parseInt(txt_vo_thresholdRetire.getText());
								last = txt_vo_thresholdRetire.getText();
							}catch(Exception e){
								last = String.valueOf(vo_thresholdRetire);
							}
						}
					});
					main_components.put("txt_vo_thresholdRetire", txt_vo_thresholdRetire);
					vo_thresholdRetire = Integer.parseInt(txt_vo_thresholdRetire.getText());
					
					
					
					final JLabel lbl_vo_inlierPixelTol = new JLabel("<html>inlierPixelTol:</html>");
					
					final JTextField txt_vo_inlierPixelTol = new JTextField("1.5",5);		
					txt_vo_inlierPixelTol.setHorizontalAlignment(JTextField.CENTER);
					
					txt_vo_inlierPixelTol.addFocusListener(new FocusListener() {

						String	last;
						
						@Override
						public void focusLost(FocusEvent evt) {
							try{
								double curr = Double.parseDouble(txt_vo_inlierPixelTol.getText());						
								vo_inlierPixelTol = curr;						
							}catch(Exception e){
								txt_vo_inlierPixelTol.setText(last);
							}
						}
						
						@Override
						public void focusGained(FocusEvent evt) {
							try{
								Double.parseDouble(txt_vo_inlierPixelTol.getText());
								last = txt_vo_inlierPixelTol.getText();
							}catch(Exception e){
								last = String.valueOf(vo_inlierPixelTol);
							}
						}
					});
					main_components.put("txt_vo_inlierPixelTol", txt_vo_inlierPixelTol);
					vo_inlierPixelTol = Double.parseDouble(txt_vo_inlierPixelTol.getText());
					
					
					
					final JLabel lbl_vo_ransacIterations = new JLabel("<html>ransacIterations:</html>");
					
					final JTextField txt_vo_ransacIterations = new JTextField("200",5);		
					txt_vo_ransacIterations.setHorizontalAlignment(JTextField.CENTER);
					
					txt_vo_ransacIterations.addFocusListener(new FocusListener() {

						String	last;
						
						@Override
						public void focusLost(FocusEvent evt) {
							try{
								int curr = Integer.parseInt(txt_vo_ransacIterations.getText());						
								vo_ransacIterations = curr;						
							}catch(Exception e){
								txt_vo_ransacIterations.setText(last);
							}
						}
						
						@Override
						public void focusGained(FocusEvent evt) {
							try{
								Integer.parseInt(txt_vo_ransacIterations.getText());
								last = txt_vo_ransacIterations.getText();
							}catch(Exception e){
								last = String.valueOf(vo_ransacIterations);
							}
						}
					});
					main_components.put("txt_vo_ransacIterations", txt_vo_ransacIterations);
					vo_ransacIterations = Integer.parseInt(txt_vo_ransacIterations.getText());
					
					
					
					panel_vo_settings = new JPanel();
					panel_vo_settings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Visual Odometry Settings:<b></html>"),
																				  BorderFactory.createEmptyBorder(5,5,5,5)));
					
					panel_vo_settings.add(lbl_vo_type);
					panel_vo_settings.add(txt_vo_type);
					panel_vo_settings.add(lbl_vo_thresholdAdd);
					panel_vo_settings.add(txt_vo_thresholdAdd);
					panel_vo_settings.add(lbl_vo_thresholdRetire);
					panel_vo_settings.add(txt_vo_thresholdRetire);
					panel_vo_settings.add(lbl_vo_inlierPixelTol);
					panel_vo_settings.add(txt_vo_inlierPixelTol);
					panel_vo_settings.add(lbl_vo_ransacIterations);
					panel_vo_settings.add(txt_vo_ransacIterations);
					
					
					panel_layout = new SpringLayout();
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_vo_type, 0, SpringLayout.NORTH, panel_vo_settings);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_vo_type, 3, SpringLayout.WEST, panel_vo_settings);
					panel_layout.putConstraint(SpringLayout.NORTH, txt_vo_type, -3,SpringLayout.NORTH, lbl_vo_type);
					panel_layout.putConstraint(SpringLayout.WEST, txt_vo_type, 3, SpringLayout.EAST, lbl_vo_type);
					panel_layout.putConstraint(SpringLayout.EAST, txt_vo_type, -3, SpringLayout.EAST, panel_vo_settings);
			
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_vo_thresholdAdd, 10, SpringLayout.SOUTH, lbl_vo_type);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_vo_thresholdAdd, 0, SpringLayout.WEST, lbl_vo_type);
					panel_layout.putConstraint(SpringLayout.NORTH, txt_vo_thresholdAdd, -1,SpringLayout.NORTH, lbl_vo_thresholdAdd);
					panel_layout.putConstraint(SpringLayout.WEST, txt_vo_thresholdAdd, 3, SpringLayout.EAST, lbl_vo_thresholdAdd);
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_vo_thresholdRetire, 0, SpringLayout.NORTH, lbl_vo_thresholdAdd);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_vo_thresholdRetire, 3, SpringLayout.EAST, txt_vo_thresholdAdd);
					panel_layout.putConstraint(SpringLayout.NORTH, txt_vo_thresholdRetire, -1,SpringLayout.NORTH, lbl_vo_thresholdRetire);
					panel_layout.putConstraint(SpringLayout.WEST, txt_vo_thresholdRetire, 0, SpringLayout.WEST, txt_vo_ransacIterations);
					
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_vo_inlierPixelTol, 10, SpringLayout.SOUTH, lbl_vo_thresholdAdd);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_vo_inlierPixelTol, 0, SpringLayout.WEST, lbl_vo_type);
					panel_layout.putConstraint(SpringLayout.NORTH, txt_vo_inlierPixelTol, -1,SpringLayout.NORTH, lbl_vo_inlierPixelTol);
					panel_layout.putConstraint(SpringLayout.WEST, txt_vo_inlierPixelTol, 3, SpringLayout.EAST, lbl_vo_inlierPixelTol);
					panel_layout.putConstraint(SpringLayout.WEST, txt_vo_inlierPixelTol, 0, SpringLayout.WEST, txt_vo_thresholdAdd);
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_vo_ransacIterations, 0, SpringLayout.NORTH, lbl_vo_inlierPixelTol);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_vo_ransacIterations, 3, SpringLayout.EAST, txt_vo_inlierPixelTol);
					panel_layout.putConstraint(SpringLayout.NORTH, txt_vo_ransacIterations, -1, SpringLayout.NORTH, lbl_vo_ransacIterations);
					panel_layout.putConstraint(SpringLayout.WEST, txt_vo_ransacIterations, 3, SpringLayout.EAST, lbl_vo_ransacIterations);
					
					
					panel_layout.putConstraint(SpringLayout.SOUTH, panel_vo_settings, 0, SpringLayout.SOUTH, txt_vo_ransacIterations);
					
					panel_vo_settings.setLayout(panel_layout);
					
				}
				
				
				
				
				//CHART SETTINGS PANEL / INIT+LAYOUT
				final JPanel panel_chart_settings;
				{

					final JLabel lbl_chart_type = new JLabel("<html><b>Chart Type:</b></html>");
					
					String chart_types[] = {"X/Z (translation) and Y/frames (altitude per frame)", "X/Z (translation) and Y/seconds (altitude per second)"};
					final JComboBox<String>	txt_chart_type = new JComboBox<String>(chart_types);		
					txt_chart_type.setSelectedIndex(0);			
					
					txt_chart_type.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent evt) {
							// TODO Auto-generated method stub
							chart_type = txt_chart_type.getSelectedIndex();
						}
					});
					main_components.put("txt_chart_type", txt_chart_type);
					chart_type = txt_chart_type.getSelectedIndex();
					
					
					
					final JLabel lbl_chart_xz = new JLabel("<html><b>Chart X/Z</b></html>");
					final JLabel lbl_chart_xz_scale = new JLabel("<html>Scale: </html>");
					
					final JTextField txt_chart_xz_scale = new JTextField("1.00",5);
					txt_chart_xz_scale.setHorizontalAlignment(JTextField.CENTER);
										
					txt_chart_xz_scale.addFocusListener(new FocusListener() {

						String	last;
						
						@Override
						public void focusLost(FocusEvent evt) {
							// TODO Auto-generated method stub
							try{
								double curr = Double.parseDouble(txt_chart_xz_scale.getText());						
								if(curr!=0)chart_xz_scale = curr;
								else txt_chart_xz_scale.setText(last);
							}catch(Exception e){
								txt_chart_xz_scale.setText(last);
							}
						}
						
						@Override
						public void focusGained(FocusEvent evt) {
							// TODO Auto-generated method stub
							try{
								Double.parseDouble(txt_chart_xz_scale.getText());
								last = txt_chart_xz_scale.getText();
							}catch(Exception e){
								last = String.valueOf(chart_xz_scale);
							}
						}
					});
					main_components.put("txt_chart_xz_scale", txt_chart_xz_scale);
					chart_xz_scale = Float.parseFloat(txt_chart_xz_scale.getText());
					
					
					final JButton btn_chart_xz_apply = new JButton("Apply");
					{
						btn_chart_xz_apply.addActionListener(new ActionListener(){
				
							@Override
							public void actionPerformed(ActionEvent evt) {
								//CHANGE XZ SCALE
								chart_xz.setChartScalingFactor(chart_xz_scale);
								chart_xz.resetSize();
							}
							
						});
					}

					
					//MOVE XZ CHART TO ORIGIN (BUTTON)
					final JButton btn_chart_xz_moveorig = new JButton("Origin");
					{
						btn_chart_xz_moveorig.addActionListener(new ActionListener(){
						
							@Override
							public void actionPerformed(ActionEvent evt) {
								//MOVE X/Z CHART TO ORIGIN
								chart_xz.moveToOrigin();
							}
										
						});
					}

					
					//MOVE XZ CHART TO LAST POINT (BUTTON)
					final JButton btn_chart_xz_movelast = new JButton("Last");
					{
						btn_chart_xz_movelast.addActionListener(new ActionListener(){
						
							@Override
							public void actionPerformed(ActionEvent evt) {
								//MOVE X/Z CHART TO LAST POINT
								chart_xz.moveToLast();
							}
										
						});
					}
					
					//3D XZ Points
					final JCheckBox chk_chart_3D_points = new JCheckBox("<html>3D Chart Points</html>");
					{
						chk_chart_3D_points.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent arg0) {
								if(chk_chart_3D_points.isSelected()){
									chk_chart_3D_points.setText("<html><b>3D Chart Points</b></html>");
									chart_xz.setThickPoints(true);
									chart_xz.repaint();
								}else{
									chk_chart_3D_points.setText("<html>3D Chart Points</html>");
									chart_xz.setThickPoints(false);
									chart_xz.repaint();
								}
								
							}
						});
					}
					main_components.put("chk_chart_3D_points", chk_chart_3D_points);

					
					
					
					final JLabel lbl_chart_y = new JLabel("<html><b>Chart Y</b></html>");
					final JLabel lbl_chart_y_scale = new JLabel("<html>Scale: </html>");
					
					final JTextField txt_chart_y_scale = new JTextField("1.00",5);
					txt_chart_y_scale.setHorizontalAlignment(JTextField.CENTER);
										
					txt_chart_y_scale.addFocusListener(new FocusListener() {

						String	last;
						
						@Override
						public void focusLost(FocusEvent evt) {
							try{
								double curr = Double.parseDouble(txt_chart_y_scale.getText());						
								if(curr!=0)chart_y_scale = curr;
								else txt_chart_y_scale.setText(last);
							}catch(Exception e){
								txt_chart_y_scale.setText(last);
							}
						}
						
						@Override
						public void focusGained(FocusEvent evt) {
							try{
								Double.parseDouble(txt_chart_y_scale.getText());
								last = txt_chart_y_scale.getText();
							}catch(Exception e){
								last = String.valueOf(chart_y_scale);
							}
						}
					});
					main_components.put("txt_chart_y_scale", txt_chart_y_scale);
					chart_y_scale = Float.parseFloat(txt_chart_y_scale.getText());
					
					
					final JButton btn_chart_y_apply = new JButton("Apply");
					{
						btn_chart_y_apply.addActionListener(new ActionListener(){
				
							@Override
							public void actionPerformed(ActionEvent evt) {
								//CHANGE Y SCALE
								chart_y.setChartScalingFactor(chart_y_scale);
								chart_y.resetSize();
							}
							
						});
					}

					//MOVE Y CHART TO ORIGIN (BUTTON)
					final JButton btn_chart_y_moveorig = new JButton("Origin");
					{
						btn_chart_y_moveorig.addActionListener(new ActionListener(){
						
							@Override
							public void actionPerformed(ActionEvent evt) {
								//MOVE Y CHART TO ORIGIN
								chart_y.moveToOrigin();
							}
										
						});
					}

					//MOVE Y CHART TO LAST POINT (BUTTON)
					final JButton btn_chart_y_movelast = new JButton("Last");
					{
						btn_chart_y_movelast.addActionListener(new ActionListener(){
						
							@Override
							public void actionPerformed(ActionEvent evt) {
								//MOVE Y CHART TO LAST POINT
								chart_y.moveToLast();
							}
										
						});
					}

					
					
					panel_chart_settings = new JPanel();
					panel_chart_settings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Chart Settings:</b></html>"),
																				  BorderFactory.createEmptyBorder(5,5,5,5)));
					
					panel_chart_settings.add(lbl_chart_type);
					panel_chart_settings.add(txt_chart_type);
					panel_chart_settings.add(lbl_chart_xz);
					panel_chart_settings.add(lbl_chart_xz_scale);
					panel_chart_settings.add(txt_chart_xz_scale);
					panel_chart_settings.add(btn_chart_xz_apply);
					panel_chart_settings.add(btn_chart_xz_moveorig);
					panel_chart_settings.add(btn_chart_xz_movelast);
					panel_chart_settings.add(chk_chart_3D_points);
					
					panel_chart_settings.add(lbl_chart_y);
					panel_chart_settings.add(lbl_chart_y_scale);
					panel_chart_settings.add(txt_chart_y_scale);
					panel_chart_settings.add(btn_chart_y_apply);
					panel_chart_settings.add(btn_chart_y_moveorig);
					panel_chart_settings.add(btn_chart_y_movelast);
					
					
					
					panel_layout = new SpringLayout();
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_chart_type, 0, SpringLayout.NORTH, panel_chart_settings);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_chart_type, 3, SpringLayout.WEST, panel_chart_settings);
					panel_layout.putConstraint(SpringLayout.NORTH, txt_chart_type, -3,SpringLayout.NORTH, lbl_chart_type);
					panel_layout.putConstraint(SpringLayout.WEST, txt_chart_type, 3, SpringLayout.EAST, lbl_chart_type);
					panel_layout.putConstraint(SpringLayout.EAST, txt_chart_type, -3, SpringLayout.EAST, panel_chart_settings);
			
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_chart_xz, 8, SpringLayout.SOUTH, lbl_chart_type);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_chart_xz, 0, SpringLayout.WEST, lbl_chart_type);
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_chart_xz_scale, 7,SpringLayout.SOUTH, lbl_chart_xz);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_chart_xz_scale, 5, SpringLayout.WEST, lbl_chart_xz);
					panel_layout.putConstraint(SpringLayout.NORTH, txt_chart_xz_scale, -3,SpringLayout.NORTH, lbl_chart_xz_scale);
					panel_layout.putConstraint(SpringLayout.WEST, txt_chart_xz_scale, 3, SpringLayout.EAST, lbl_chart_xz_scale);
					panel_layout.putConstraint(SpringLayout.NORTH, btn_chart_xz_apply, -1,SpringLayout.NORTH, txt_chart_xz_scale);
					panel_layout.putConstraint(SpringLayout.WEST, btn_chart_xz_apply, 3, SpringLayout.EAST, txt_chart_xz_scale);
					panel_layout.putConstraint(SpringLayout.NORTH, btn_chart_xz_moveorig, 0,SpringLayout.NORTH, btn_chart_xz_apply);
					panel_layout.putConstraint(SpringLayout.WEST, btn_chart_xz_moveorig, 3, SpringLayout.EAST, btn_chart_xz_apply);
					panel_layout.putConstraint(SpringLayout.NORTH, btn_chart_xz_movelast, 0,SpringLayout.NORTH, btn_chart_xz_moveorig);
					panel_layout.putConstraint(SpringLayout.WEST, btn_chart_xz_movelast, 3, SpringLayout.EAST, btn_chart_xz_moveorig);
					panel_layout.putConstraint(SpringLayout.NORTH, chk_chart_3D_points, 0,SpringLayout.NORTH, btn_chart_xz_movelast);
					panel_layout.putConstraint(SpringLayout.WEST, chk_chart_3D_points, 3, SpringLayout.EAST, btn_chart_xz_movelast);
					
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_chart_y, 8, SpringLayout.SOUTH, lbl_chart_xz_scale);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_chart_y, 0, SpringLayout.WEST, lbl_chart_type);
					
					panel_layout.putConstraint(SpringLayout.NORTH, lbl_chart_y_scale, 7,SpringLayout.SOUTH, lbl_chart_y);
					panel_layout.putConstraint(SpringLayout.WEST, lbl_chart_y_scale, 5, SpringLayout.WEST, lbl_chart_y);
					panel_layout.putConstraint(SpringLayout.NORTH, txt_chart_y_scale, -3,SpringLayout.NORTH, lbl_chart_y_scale);
					panel_layout.putConstraint(SpringLayout.WEST, txt_chart_y_scale, 3, SpringLayout.EAST, lbl_chart_y_scale);
					panel_layout.putConstraint(SpringLayout.NORTH, btn_chart_y_apply, -1,SpringLayout.NORTH, txt_chart_y_scale);
					panel_layout.putConstraint(SpringLayout.WEST, btn_chart_y_apply, 3, SpringLayout.EAST, txt_chart_y_scale);
					panel_layout.putConstraint(SpringLayout.NORTH, btn_chart_y_moveorig, 0,SpringLayout.NORTH, btn_chart_y_apply);
					panel_layout.putConstraint(SpringLayout.WEST, btn_chart_y_moveorig, 3, SpringLayout.EAST, btn_chart_y_apply);
					panel_layout.putConstraint(SpringLayout.NORTH, btn_chart_y_movelast, 0,SpringLayout.NORTH, btn_chart_y_moveorig);
					panel_layout.putConstraint(SpringLayout.WEST, btn_chart_y_movelast, 3, SpringLayout.EAST, btn_chart_y_moveorig);
					

					panel_layout.putConstraint(SpringLayout.SOUTH, panel_chart_settings, 0, SpringLayout.SOUTH, lbl_chart_y_scale);
					
					panel_chart_settings.setLayout(panel_layout);
					
				}

				
		//MAIN PANEL LAYOUT
		JPanel main_panel = new JPanel();
		main_panel.add(lbl_title);
		main_panel.add(panel_input_settings);
		main_panel.add(panel_image_settings);
		main_panel.add(panel_tracker_settings);
		main_panel.add(panel_vo_settings);
		main_panel.add(panel_chart_settings);
		
		frame_layout = new SpringLayout();

		//Adjust constraints
		frame_layout.putConstraint(SpringLayout.NORTH, lbl_title, 5, SpringLayout.NORTH, main_panel);
		frame_layout.putConstraint(SpringLayout.WEST, lbl_title, 5, SpringLayout.WEST, main_panel);
		frame_layout.putConstraint(SpringLayout.EAST, lbl_title, -5, SpringLayout.EAST, main_panel);

		frame_layout.putConstraint(SpringLayout.NORTH, panel_input_settings, 2, SpringLayout.SOUTH, lbl_title);
		frame_layout.putConstraint(SpringLayout.WEST, panel_input_settings, 5, SpringLayout.WEST, main_panel);
		frame_layout.putConstraint(SpringLayout.EAST, panel_input_settings, -5, SpringLayout.EAST, main_panel);

		frame_layout.putConstraint(SpringLayout.NORTH, panel_image_settings, 1, SpringLayout.SOUTH, panel_input_settings);
		frame_layout.putConstraint(SpringLayout.WEST, panel_image_settings, 5, SpringLayout.WEST, main_panel);
		frame_layout.putConstraint(SpringLayout.EAST, panel_image_settings, -5, SpringLayout.EAST, main_panel);

		frame_layout.putConstraint(SpringLayout.NORTH, panel_tracker_settings, 1, SpringLayout.SOUTH, panel_image_settings);
		frame_layout.putConstraint(SpringLayout.WEST, panel_tracker_settings, 5, SpringLayout.WEST, main_panel);
		frame_layout.putConstraint(SpringLayout.EAST, panel_tracker_settings, -5, SpringLayout.EAST, main_panel);
		
		frame_layout.putConstraint(SpringLayout.NORTH, panel_vo_settings, 1, SpringLayout.SOUTH, panel_tracker_settings);
		frame_layout.putConstraint(SpringLayout.WEST, panel_vo_settings, 5, SpringLayout.WEST, main_panel);
		frame_layout.putConstraint(SpringLayout.EAST, panel_vo_settings, -5, SpringLayout.EAST, main_panel);
		
		frame_layout.putConstraint(SpringLayout.NORTH, panel_chart_settings, 1, SpringLayout.SOUTH, panel_vo_settings);
		frame_layout.putConstraint(SpringLayout.WEST, panel_chart_settings, 5, SpringLayout.WEST, main_panel);
		frame_layout.putConstraint(SpringLayout.EAST, panel_chart_settings, -5, SpringLayout.EAST, main_panel);
		frame_layout.putConstraint(SpringLayout.SOUTH, panel_chart_settings, 1, SpringLayout.SOUTH, main_panel);


		
		main_panel.setLayout(frame_layout);
		if(!system_look_and_feel_enabled){main_panel.setPreferredSize(new Dimension(480,805));}
		else{main_panel.setPreferredSize(new Dimension(480,915));}
		
		JScrollPane main_scroll = new JScrollPane(main_panel);
		main_scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		main_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		

		
		
		
		//LOAD SETTINGS BUTTON
		btn_load = new JButton("Load Settings");
		{
			btn_load.addActionListener(new ActionListener(){
			
				@Override
				public void actionPerformed(ActionEvent evt) {
					//LOAD SETTINGS
				}
							
			});
		}
				
				
		//SAVE SETTINGS BUTTON
		btn_save = new JButton("Save Settings");
		{
			btn_save.addActionListener(new ActionListener(){
			
				@Override
				public void actionPerformed(ActionEvent evt) {
					//SAVE SETTINGS
					
				}
								
			});
		}

				
				
		//START VO BUTTON
		btn_start = new JButton("Start");
		{ //Button Start Action Listener
			btn_start.addActionListener(new ActionListener(){
			
				@Override
				public void actionPerformed(ActionEvent evt) {

				Thread thread = new Thread(new Runnable(){
			
					@Override
					public void run() {
						start();
						btn_start.setEnabled(true);
						btn_pause.setEnabled(false);
						btn_reset.setEnabled(false);
						btn_stop.setEnabled(false);
						if(chart_xz!=null && chart_xz.getAllPoints().size()<=0) btn_clear.setEnabled(false);
						
					}
							
				});
				btn_start.setEnabled(false);
				btn_pause.setEnabled(true);
				btn_reset.setEnabled(true);
				btn_stop.setEnabled(true);
				btn_clear.setEnabled(true);
				if(input_source.equalsIgnoreCase("device")){
					btn_stop_capture.setEnabled(true);
					final JTextField txt = (JTextField)main_components.get("txt_stop_capture");
					txt.setEnabled(true);
				}
				thread.start();
				}
					
			});
		}
	
			
			
			
		//PAUSE VO BUTTON
		btn_pause = new JButton("Pause");
		btn_pause.setEnabled(false);
		{
			btn_pause.addActionListener(new ActionListener(){
					
				@Override
				public void actionPerformed(ActionEvent evt) {
					//PAUSE PROCESSING
					pause_flag=!pause_flag;
					btn_pause.setText(pause_flag ? "Resume" : "Pause");
					
				}
								
			});
		}
			
		
						
		//RESET VO BUTTON
		btn_reset = new JButton("Reset VO");
		btn_reset.setEnabled(false);
		{
			btn_reset.addActionListener(new ActionListener(){
					
				@Override
				public void actionPerformed(ActionEvent evt) {
					//RESET VO CONTEXT
					reset_vo_flag=true;
					chart_info.lbl_status.setText("<html><b>Status:</b> VO Context Reset requested.</html>");
				}
									
			});
		}
							
				
		
				
		//STOP VO BUTTON
		btn_stop = new JButton("Stop");
		btn_stop.setEnabled(false);
		{
			btn_stop.addActionListener(new ActionListener(){
			
				@Override
				public void actionPerformed(ActionEvent evt) {

					//STOP PROCESSING
						pause_flag = false;
						btn_pause.setText("Pause");
						stop_flag = true;
						
						btn_start.setEnabled(true);
						btn_pause.setEnabled(false);
						btn_reset.setEnabled(false);
						btn_stop.setEnabled(false);
						btn_stop_capture.setEnabled(false);
						final JTextField txt = (JTextField)main_components.get("txt_stop_capture");
						txt.setEnabled(false);
				}
								
			});
		}
					
				
		
				
		//CLEAR BUTTON
		btn_clear = new JButton("Clear");
		btn_clear.setEnabled(false);
		{
			btn_clear.addActionListener(new ActionListener(){
					
				@Override
				public void actionPerformed(ActionEvent evt) {
						//STOP PROCESSING AND CLEAR ALL
						if(processing_flag){
							pause_flag = false;
							btn_pause.setText("Pause");
							stop_flag = false;
							clear_flag = true;
						}else{
							chart_xz.clearAllPoints();
							chart_xz.resetSize();
							chart_y.clearAllPoints();
							chart_y.resetSize();
							chart_info.lbl_status.setText("<html><b>Status: </b>Cleared.</html>");
							chart_info.setInfoPanelVisible(false);
							chart_info.clearListData();
						}
						
						btn_start.setEnabled(true);
						btn_pause.setEnabled(false);
						btn_reset.setEnabled(false);
						btn_stop.setEnabled(false);
						btn_clear.setEnabled(false);
						btn_stop_capture.setEnabled(false);
						final JTextField txt = (JTextField)main_components.get("txt_stop_capture");
						txt.setEnabled(false);
				}
							
			});
		}
		
		
		//STOP CAPTURE AFTER N SEC
		btn_stop_capture = new JButton("Stop capture after:");
		btn_stop_capture.setEnabled(false);
		{
			btn_stop_capture.addActionListener(new ActionListener(){
					
				@Override
				public void actionPerformed(ActionEvent evt) {
					//STOP CAPTURE AFTER (TXT_STOP_CAPTURE) Seconds
					btn_stop_capture.setEnabled(false);
					final JTextField txt = (JTextField)main_components.get("txt_stop_capture");
					txt.setEnabled(false);
					final int seconds = Integer.parseInt(txt.getText());
					
					Thread StopAfter = new Thread(new Runnable(){

						@Override
						public void run() {
								final long start = System.currentTimeMillis();
								int count=0;
								while(System.currentTimeMillis()<start+(seconds*1000)){
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									if(!processing_flag){txt.setText(String.valueOf(seconds));return;}
									count++;
									txt.setText(String.valueOf(seconds-count));
									
								}
								stop_capture = true;
								txt.setText(String.valueOf(seconds));
						}
							
					});
					
					StopAfter.start();
						
				}
							
			});
		}
		
		final JTextField txt_stop_capture = new JTextField("10",3);
		txt_stop_capture.setHorizontalAlignment(JTextField.CENTER);
		txt_stop_capture.setEnabled(false);
		
		txt_stop_capture.addFocusListener(new FocusListener() {

			String	last_value;
			
			@Override
			public void focusLost(FocusEvent evt) {
				// TODO Auto-generated method stub
				try{
					int curr_value = Integer.parseInt(txt_stop_capture.getText());
					
					if(curr_value<0){ 
						txt_stop_capture.setText(String.valueOf(-curr_value)); 
					}
					
				}catch(Exception e){
					txt_stop_capture.setText(last_value);
				}
			}
			
			@Override
			public void focusGained(FocusEvent evt) {
				// TODO Auto-generated method stub
				try{
					Integer.parseInt(txt_stop_capture.getText());
					last_value = txt_stop_capture.getText();
				}catch(Exception e){
					last_value = String.valueOf("10");
				}
			}
		});
		main_components.put("txt_stop_capture", txt_stop_capture);
		
		
		
		main_frame.add(main_scroll);
		main_frame.add(btn_load);
		main_frame.add(btn_save);
		main_frame.add(btn_start);
		main_frame.add(btn_pause);
		main_frame.add(btn_reset);
		main_frame.add(btn_stop);
		main_frame.add(btn_clear);
		main_frame.add(btn_stop_capture);
		main_frame.add(txt_stop_capture);
		
		frame_layout = new SpringLayout();

		Container contentPane = main_frame.getContentPane();
		//Adjust constraints
		frame_layout.putConstraint(SpringLayout.NORTH, main_scroll, 0, SpringLayout.NORTH, contentPane);
		frame_layout.putConstraint(SpringLayout.WEST, main_scroll, 0, SpringLayout.WEST, contentPane);
		frame_layout.putConstraint(SpringLayout.EAST, main_scroll, 0, SpringLayout.EAST, contentPane);
		frame_layout.putConstraint(SpringLayout.SOUTH, main_scroll, -5, SpringLayout.NORTH, btn_load);

		frame_layout.putConstraint(SpringLayout.NORTH, btn_load, -65, SpringLayout.SOUTH, contentPane);
		frame_layout.putConstraint(SpringLayout.WEST, btn_load, 5, SpringLayout.WEST, contentPane);
		frame_layout.putConstraint(SpringLayout.NORTH, btn_save, 0, SpringLayout.NORTH, btn_load);
		frame_layout.putConstraint(SpringLayout.WEST, btn_save, 5, SpringLayout.EAST, btn_load);
		
		
		frame_layout.putConstraint(SpringLayout.NORTH, btn_start, 3, SpringLayout.SOUTH, btn_load);
		frame_layout.putConstraint(SpringLayout.WEST, btn_start, 5, SpringLayout.WEST, contentPane);
		frame_layout.putConstraint(SpringLayout.NORTH, btn_pause, 0, SpringLayout.NORTH, btn_start);
		frame_layout.putConstraint(SpringLayout.WEST, btn_pause, 5, SpringLayout.EAST, btn_start);
		frame_layout.putConstraint(SpringLayout.NORTH, btn_reset, 0, SpringLayout.NORTH, btn_start);
		frame_layout.putConstraint(SpringLayout.WEST, btn_reset, 5, SpringLayout.EAST, btn_pause);
		frame_layout.putConstraint(SpringLayout.NORTH, btn_stop, 0, SpringLayout.NORTH, btn_start);
		frame_layout.putConstraint(SpringLayout.WEST, btn_stop, 5, SpringLayout.EAST, btn_reset);
		frame_layout.putConstraint(SpringLayout.NORTH, btn_clear, 0, SpringLayout.NORTH, btn_start);
		frame_layout.putConstraint(SpringLayout.WEST, btn_clear, 5, SpringLayout.EAST, btn_stop);

		frame_layout.putConstraint(SpringLayout.NORTH, btn_stop_capture, 0, SpringLayout.NORTH, btn_start);
		frame_layout.putConstraint(SpringLayout.WEST, btn_stop_capture, 5, SpringLayout.EAST, btn_clear);
		frame_layout.putConstraint(SpringLayout.NORTH, txt_stop_capture, 1, SpringLayout.NORTH, btn_start);
		frame_layout.putConstraint(SpringLayout.WEST, txt_stop_capture, 5, SpringLayout.EAST, btn_stop_capture);

		
		main_frame.setLayout(frame_layout);
		
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int frame_height;
		int frame_width;
		if((int)(screenSize.getWidth())>=1030){
			frame_width=530;
		}else{
			frame_width=((int)(screenSize.getWidth()/3));
		}
		if((int)(screenSize.getHeight())>=930){
		 frame_height=930;
		}else{
		 frame_height = (int)(screenSize.getHeight());
		}
		
		
		main_frame.setBounds(frame_width+65, 0, frame_width, frame_height);
		main_frame.setVisible(true);


		
		//CHART VIEWER/INFO
		chart_frame = new JFrame("Chart");
		{
			chart_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//chart_frame.setLocationRelativeTo(null);//frame.setLocationByPlatform(true); //alternative location auto-defining
			
			
			

			chart_xz = new ChartScrollPane(Color.blue);
			chart_xz.setChartColor(Color.blue);
			chart_xz.setAxisColor(Color.black);
			chart_xz.setAxisNames("X", "Z");
			chart_xz.setAxisNamesColor(Color.blue);
			chart_xz.setAxisUnitsColor(Color.blue);
			chart_xz.setShowLegend(true);
			
			chart_xz.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("X/Z Chart Viewer"),
					  BorderFactory.createEmptyBorder(5,5,5,5)));
			chart_xz.setPreferredSize(new Dimension(400,400));
			
			
			chart_y = new ChartScrollPane(20,85,false,true,Color.blue);
			chart_y.setChartColor(Color.blue);
			chart_y.setAxisColor(Color.black);
			chart_y.setAxisNames("frame", "Y");
			chart_y.setAxisNamesColor(Color.blue);
			chart_y.setAxisUnitsColor(Color.blue);
			
			chart_y.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Y Chart Viewer"),
					  BorderFactory.createEmptyBorder(5,5,5,5)));
			chart_y.setPreferredSize(new Dimension(400,200));
			
			
			
			
			chart_info = new InfoScrollPane(Color.blue);
			chart_info.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Chart/Elaboration Info"),
					  BorderFactory.createEmptyBorder(5,5,5,5)));
			chart_info.setPreferredSize(new Dimension(400,400));
			chart_info.setInfoPanelVisible(false);
			chart_info.setBufferInfoVisible(false);
			chart_info.lbl_status.setText("<html><b>Status:</b> Ready.</html>");
			chart_info.lst_points.addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent evt) {
					if(evt.getSource()==chart_info.lst_points && 
							evt.getValueIsAdjusting()==false){
						String selected = (String)chart_info.lst_points.getSelectedValue();
						if(selected==null) return;
						if(selected.indexOf("X:")==-1 || selected.indexOf("Z:")==-1) return;
						
						String XtoEnd = selected.substring(selected.indexOf("X:")+2).trim();
						String X = XtoEnd.substring(0,XtoEnd.indexOf(","));
						String ZtoEnd = selected.substring(selected.indexOf("Z:")+2).trim();
						String Z = ZtoEnd.substring(0,ZtoEnd.indexOf(","));
						
						chart_xz.moveToPoint(Double.parseDouble(X), Double.parseDouble(Z));
						
						String chart_type = selected.substring(selected.lastIndexOf(",")+1).trim();
						if(chart_type.equalsIgnoreCase("(Chart Type Y/f)")
								&& selected.indexOf("Y:")!=-1){
							String YtoEnd = selected.substring(selected.indexOf("Y:")+2).trim();
							String Y = YtoEnd.substring(0,YtoEnd.indexOf(","));
							String FrameToEnd = selected.substring(selected.indexOf("Frame:")+6).trim();
							String Frame = FrameToEnd.substring(0,FrameToEnd.indexOf(","));
							
							chart_y.moveToPoint(Double.parseDouble(Frame), Double.parseDouble(Y));
							
						}else if(chart_type.equalsIgnoreCase("(Chart Type Y/s)")
								&& selected.indexOf("Y:")!=-1){
							String YtoEnd = selected.substring(selected.indexOf("Y:")+2).trim();
							String Y = YtoEnd.substring(0,YtoEnd.indexOf(","));
							String TimeToEnd = selected.substring(selected.indexOf("El. Time:")+9).trim();
							Double Time;
							//try{
							//	String strTime = TimeToEnd.substring(0,TimeToEnd.indexOf(","));
							//	Time = Double.parseDouble(strTime);
							//}catch(Exception e){
								String strTime = TimeToEnd.substring(0,TimeToEnd.indexOf("s"));
								Time = Double.parseDouble(strTime);
							//}
							chart_y.moveToPoint(Time, Double.parseDouble(Y));
						}
						
						
					}				
				}
			});
			
			
			chart_frame.getContentPane().add(chart_xz);
			chart_frame.getContentPane().add(chart_y);
			chart_frame.getContentPane().add(chart_info);
			
			
			Container chart_frame_contentPane = chart_frame.getContentPane();
			final SpringLayout chart_layout = new SpringLayout();
	
			
			Spring height_proportional = new Spring() {
				
				
				@Override
				public void setValue(int direction) {
					// TODO Auto-generated method stub					
				}
				
				@Override
				public int getValue() {
					// TODO Auto-generated method stub
					return (int)Math.round(11*chart_frame.getHeight()/30);
				}
				
				@Override
				public int getPreferredValue() {
					// TODO Auto-generated method stub
					return (int)Math.round(11*chart_frame.getHeight()/30);
				}
				
				@Override
				public int getMinimumValue() {
					// TODO Auto-generated method stub
					return (int)Math.round(11*chart_frame.getHeight()/30);
				}
				
				@Override
				public int getMaximumValue() {
					// TODO Auto-generated method stub
					return (int)Math.round(11*chart_frame.getHeight()/30);
				}
			};
			
			//Adjust constraints
			chart_layout.putConstraint(SpringLayout.NORTH, chart_xz, 5, SpringLayout.NORTH, chart_frame_contentPane);
			chart_layout.putConstraint(SpringLayout.WEST, chart_xz, 5, SpringLayout.WEST, chart_frame_contentPane);
			chart_layout.putConstraint(SpringLayout.EAST, chart_xz, -5, SpringLayout.EAST, chart_frame_contentPane);
			chart_layout.putConstraint(SpringLayout.SOUTH, chart_xz, height_proportional, SpringLayout.NORTH, chart_frame_contentPane);
			
			
			chart_layout.putConstraint(SpringLayout.NORTH, chart_y, 5, SpringLayout.SOUTH, chart_xz);
			chart_layout.putConstraint(SpringLayout.WEST, chart_y, 5, SpringLayout.WEST, chart_frame_contentPane);
			chart_layout.putConstraint(SpringLayout.EAST, chart_y, -5, SpringLayout.EAST, chart_frame_contentPane);
			chart_layout.putConstraint(SpringLayout.SOUTH, chart_frame_contentPane, height_proportional, SpringLayout.SOUTH, chart_y);
			
			chart_layout.putConstraint(SpringLayout.NORTH, chart_info, 5, SpringLayout.SOUTH, chart_y);
			chart_layout.putConstraint(SpringLayout.WEST, chart_info, 5, SpringLayout.WEST, chart_frame_contentPane);
			chart_layout.putConstraint(SpringLayout.EAST, chart_info, -5, SpringLayout.EAST, chart_frame_contentPane);
			chart_layout.putConstraint(SpringLayout.SOUTH, chart_info, -5, SpringLayout.SOUTH, chart_frame_contentPane);
			
			
			
			chart_frame.setLayout(chart_layout);
			
			chart_frame.setBounds(0, 0, frame_width, frame_height);
			chart_frame.setVisible(true);
			
			

			chart_xz.addMouseListener(new MaximizeOnDblClick(chart_xz, chart_frame));
			chart_y.addMouseListener(new MaximizeOnDblClick(chart_y, chart_frame));
			chart_info.addMouseListener(new MaximizeOnDblClick(chart_info, chart_frame));
			
		}


		
		{	//VIDEO INPUT FRAME
			video_input_panel = new ImagePanel();

			video_input_frame = new JFrame("Video Input");
			video_input_frame.setLocationRelativeTo(null);//frame.setLocationByPlatform(true); //alternative location auto-defining
			video_input_frame.setLocation((frame_width*2)+65, 0);
		
			video_input_frame.getContentPane().add(video_input_panel);
		}

		{	//VO VIDEO INPUT FRAME
			video_vo_panel = new ImagePanel();

			video_vo_frame = new JFrame("VO Video Input");
			video_vo_frame.setLocationRelativeTo(null);//frame.setLocationByPlatform(true); //alternative location auto-defining
			video_vo_frame.setLocation((frame_width*2)+65, (frame_height/2));
		
			video_vo_frame.getContentPane().add(video_vo_panel);
		}

		
		
		}
	
	
	private void start(){
		
		String[] options = {"OK"};
		boolean check = check_Settings();
		
		
		chart_info.lbl_status.setText("<html><b>Status: </b>Initializing Elaboration...</html>");
		
		if(!check){
			chart_info.lbl_status.setText("<html><b>Status: </b>Settings Error. Could not start Elaboration.</html>");
			return;
		}
		chart_info.lbl_status.setText("<html><b>Status: </b>Settings Check passed.</html>");
		
		if(!openCalib()){
			JOptionPane.showOptionDialog(main_frame, "Calibration File path is wrong or the specified file doesn't exist!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			chart_info.lbl_status.setText("<html><b>Status: </b>Error opening Calibration file. Could not start Elaboration.</html>");
			return;
		}
		chart_info.lbl_status.setText("<html><b>Status: </b>Calibration opened succesfully.</html>");
		
		switch(input_source){
			case "video":
				if(!openVideo()){
					JOptionPane.showOptionDialog(main_frame, "Video path is wrong or the specified file doesn't exist!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
					chart_info.lbl_status.setText("<html><b>Status: </b>Error opening Video file. Could not start Elaboration.</html>");
					return;
				}
			chart_info.lbl_status.setText("<html><b>Status: </b>Video opened succesfully.</html>");
				break;
			case "device":
				if(!openDevice()){
					JOptionPane.showOptionDialog(main_frame, "Device path is wrong, or the specified device doesn't exist\nor doesn't support selected Adjustments!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
					chart_info.lbl_status.setText("<html><b>Status: </b>Error opening Device. Could not start Elaboration.</html>");
					return;
				}
				chart_info.lbl_status.setText("<html><b>Status: </b>Device opened succesfully.</html>");
				break;
			default:
				return;
		}

		if(!setupTracker()){
			JOptionPane.showOptionDialog(main_frame, "Error setting up the Tracker!\nCheck out Tracker Settings","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			chart_info.lbl_status.setText("<html><b>Status: </b>Error setting up the Tracker. Could not start Elaboration.</html>");
			return;
		}
		chart_info.lbl_status.setText("<html><b>Status: </b>Tracker Setup passed.</html>");
		
		
		if(!setupVisualOdometry()){
			JOptionPane.showOptionDialog(main_frame, "Error setting up the Visual Odometry!\nCheck out Visual Odometry Settings","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			chart_info.lbl_status.setText("<html><b>Status: </b>Error setting up the Visual Odometry. Could not start Elaboration.</html>");
			return;
		}
		chart_info.lbl_status.setText("<html><b>Status: </b>Visual Odometry Setup passed.</html>");
		
		if(!process()){
			JOptionPane.showOptionDialog(main_frame, "An error has occurred during the elaboration!\nCheck out your Visual Odometry/Tracker Settings.\nOtherwise your input video may be invalid or not estimable.", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return;
		}	
		
	}
	
	
	

	private boolean openCalib(){

		//load camera description
		try{
			
			switch(vo_type){
			case "monoPlaneInfinity":
			case "monoPlaneOverhead":
				calibration = BoofMiscOps.loadXML(media.openFile(calib_path));
				break;
			case "stereoDepth":
			case "stereoDualTrackerPnP":
			case "stereoQuadPnP":
				calibration_stereo = BoofMiscOps.loadXML(media.openFile(calib_path));
				break;
			case "depthDepthPnP":
				calibration_depth = BoofMiscOps.loadXML(media.openFile(calib_path));
				break;
			case "default":					
			default:
				calibration = BoofMiscOps.loadXML(media.openFile(calib_path));
				break;
			}
			
			
			return true;
		}catch(Exception e){
			return false;
		}

	}
	
	
	
	
	private boolean openVideo(){
		
		//load the video sequence
		try{
			video = media.openVideo(video_path, ImageType.single(imgType));
			return true;
		}catch(Exception e){
			return false;
		}

	}
	
	
	
	
		
	private boolean openDevice(){
		
		
		V4l4jVideo<I> cam = new V4l4jVideo<I>();
		
	try{
		cam.setConvertBufferedImage(false);
		cam.activateControls(device_sust_fps, device_timeout_img, device_keep_format);
		cam.start(device_path, device_width, device_height, new VideoCallBack<I>() {
			
			int numframe=0;
			long start_time;
			int current_fps=0;
			long partial_time;
	    	
			@Override
			public ImageType<I> getImageDataType() {
				
				if(imgType.equals(ImageUInt8.class)){
					return new ImageType<I>(ImageType.Family.SINGLE_BAND, ImageDataType.U8,1);
				}else{
					return new ImageType<I>(ImageType.Family.SINGLE_BAND, ImageDataType.F32,1);	
				}
			}

			@Override
			public void init(int width, int height) {
				
				stop_capture = false;
				isStopped_capture = false;
				if(buffer.size()>0)buffer.clear();
				
				if(fullres_preview){
					video_input_panel.setPreferredSize(new Dimension(width, height));
					video_input_frame.setTitle("Device Input ("+width+"x"+height+") Preview@Full-Res");
					
					if(img_keep_original){
						video_vo_panel.setPreferredSize(new Dimension(width,height));
						video_vo_frame.setTitle("VO Processing ("+width+"x"+height+") Preview@Full-Res");
					}else{
						video_vo_panel.setPreferredSize(new Dimension(img_resize_width,img_resize_height));
						video_vo_frame.setTitle("VO Processing (Resized: "+img_resize_width+"x"+img_resize_height+") Preview@Full-Res");
					}
				}else{
					video_input_panel.setPreferredSize(new Dimension(400,400));
					video_input_frame.setTitle("Device Input ("+width+"x"+height+") Preview@400x400");

					if(img_keep_original){
						video_vo_panel.setPreferredSize(new Dimension(400,400));
						video_vo_frame.setTitle("VO Processing ("+width+"x"+height+") Preview@400x400");
					}else{
						video_vo_panel.setPreferredSize(new Dimension(400,400));
						video_vo_frame.setTitle("VO Processing (Resized: "+img_resize_width+"x"+img_resize_height+") Preview@400x400");
					}
				}				
				video_input_frame.pack();
				video_vo_frame.pack();
				
				left_img = this.getImageDataType().createImage(width, height);
				
			}

			@Override
			public <O> void nextFrame(I left, Object sourceData, long timeStamp) {
				
				if(numframe==0){
					start_time = System.currentTimeMillis();
					partial_time = System.currentTimeMillis();
				}
				
				
				
				BufferedImage orig = (BufferedImage)sourceData;
				
				
				//Fill Buffer and print buffer load				
				if(img_buffer_size==0){ //Infinite Buffer
					buffer.add(deepCopy(orig));
				}else if(img_buffer_size>0 && buffer.size()<img_buffer_size){
					buffer.add(deepCopy(orig));
				}

				
				
				// tell the Video GUI to update
				if(!only_vo_preview){
					if(!video_input_frame.isVisible())video_input_frame.setVisible(true);
					video_input_panel.setBufferedImage(orig);
					video_input_panel.repaint();
				}else{
					if(video_input_frame.isVisible())video_input_frame.setVisible(false);
				}
				
				
					
				//Update Average and Current device FPS info
				numframe++;
				long current_time = System.currentTimeMillis();
				BigDecimal current_seconds =round_BigDecimal((float)(current_time-start_time)/1000,1);
				
				float average_fps = 0;
				try{
					average_fps = round((numframe/current_seconds.floatValue()),2);
				}catch(Exception e){}			 
				
				chart_info.lbl_input_fps_average.setText("<html><b>Average FPS:</b> "+average_fps+ " fps\n");
				
				
				if(current_time-partial_time>=1000){
					current_fps++;
					chart_info.lbl_input_fps_current.setText("<html><b>Current FPS:</b> "+current_fps+ " fps\n");
					
					current_fps=0;		
					partial_time=System.currentTimeMillis();
				}else{
					current_fps++;
				}
					

			}

			@Override
			public boolean stopRequested() {
		
				if(stop_capture){
					stop_capture = false;
					return true;
				}else{
					return false;
				}
			
			}

			@Override
			public void stopped() {
				if(!processing_flag && buffer.size()>0)buffer.clear();
				isStopped_capture=true;
			}
		});
		
		
		Thread BufferMonitor = new Thread(new Runnable(){

			@Override
			public void run() {
				chart_info.setBufferInfoVisible(true);
				while(!isStopped_capture || processing_flag){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(buffer==null) continue;

					
					//Update Buffer ProgressBar
					if(chart_info.progress_buffer_load.getMinimum()!=0) 
						{chart_info.progress_buffer_load.setMinimum(0);}
					if(img_buffer_size==0){//0 is for infinite buffer
						if(chart_info.progress_buffer_load.getMaximum()!=10000)//(3000)Java Heap Space limit
							chart_info.progress_buffer_load.setMaximum(10000);
					}else{
						if(chart_info.progress_buffer_load.getMaximum()!=img_buffer_size)
							chart_info.progress_buffer_load.setMaximum(img_buffer_size);
					}
					
					chart_info.progress_buffer_load.setValue(buffer.size());
					int load_percent = chart_info.progress_buffer_load.getValue()*100/chart_info.progress_buffer_load.getMaximum();
					chart_info.progress_buffer_load.setString(load_percent+"%");
					chart_info.progress_buffer_load.setStringPainted(true);


					//Update Buffer Info Label
					if(buffer.size()==0){
						chart_info.lbl_buffer_load.setText("<html>"+buffer.size()+"/"+(img_buffer_size==0 ? "inf." : img_buffer_size)
															+" <b>Buffer Underrun</b></html>");
					}else if(img_buffer_size!=0 && buffer.size()>0 
							&& buffer.size()<img_buffer_size){
						chart_info.lbl_buffer_load.setText("<html>"+buffer.size()+"/"+img_buffer_size+"</html>");
					}else if(img_buffer_size==0 && buffer.size()>0){
						chart_info.lbl_buffer_load.setText("<html>"+buffer.size()+"/inf.</html>");
					}else{					
						chart_info.lbl_buffer_load.setText("<html>"+buffer.size()+"/"+img_buffer_size+" <b>Buffer Overrun</b></html>");
					}
										
					//String infinite = Character.valueOf('\u221e').toString();
				}
				
				chart_info.setBufferInfoVisible(false);
			}
			
		});		
		BufferMonitor.start();
		
		
		return true;
		
	}catch(Exception e){
		isStopped_capture = true;
		return false;
	}
	
	
	}
	
	
	@SuppressWarnings("unchecked")
	private boolean setupTracker(){
		
		TrackerGenerator tracker_generator = new TrackerGenerator(imgType);
		int[] extracted_pyramidScaling = null;

		switch(tracker_type){
		
			case "klt":
			case "klt2":
				extracted_pyramidScaling = extract_IntArray(klt_pyramidScaling);
				
				if(extracted_pyramidScaling==null||extracted_pyramidScaling[0]==0){
					int choice = JOptionPane.showConfirmDialog(main_frame, "Pyramid Scaling is in a wrong format!\nUse default value (1,2,4,8)?","Error", JOptionPane.ERROR_MESSAGE & JOptionPane.OK_CANCEL_OPTION);
					if(choice==1){return false;}
					else {
						((JTextField) main_components.get("txt_klt_pyramidScaling")).setText("1,2,4,8");
						klt_pyramidScaling = "1,2,4,8";
						extracted_pyramidScaling = extract_IntArray(klt_pyramidScaling);
					}
				}
				
				try{
					if(tracker_type.equalsIgnoreCase("klt")){
						tracker = tracker_generator.createKLT(klt_templateRadius, extracted_pyramidScaling, klt_maxFeatures, klt_radius, klt_threshold);
					}else if(tracker_type.equalsIgnoreCase("klt2")){
						tracker = tracker_generator.createKLT_TwoPass(klt_templateRadius, extracted_pyramidScaling, klt_maxFeatures, klt_radius, klt_threshold);
					}
				}catch(Exception e){
					return false;
				}

				break;				
			case "surf":
				try{
					tracker = tracker_generator.createSURF(surf_maxFeaturesPerScale, surf_extractRadius, surf_initialSampleSize);
				}catch(Exception e){
					return false;
				}
				break;
			case "surf2":
				try{
					tracker = tracker_generator.createSURF_TwoPass(surf_maxFeaturesPerScale, surf_extractRadius, surf_initialSampleSize);
				}catch(Exception e){
					return false;
				}
				break;
			case "default":
				try{
					tracker = tracker_generator.create_default();
				}catch(Exception e){
					return false;
				}
				break;
			default:			
				return false;	
		}
		
		return true;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private boolean setupVisualOdometry(){
		VisualOdometryGenerator visual_odometry_generator = new VisualOdometryGenerator(tracker, imgType, null);
		visual_odometry_generator.setMonoCalibration(calibration);
		visual_odometry_generator.setStereoCalibration(calibration_stereo);
		visual_odometry_generator.setDepthCalibration(calibration_depth);
		
		switch(vo_type){
			case "monoPlaneInfinity":
				try{
					visual_odometry = visual_odometry_generator.create_monoPlaneInfinity(vo_thresholdAdd, vo_thresholdRetire, vo_inlierPixelTol, vo_ransacIterations);
					if(visual_odometry==null) return false;
				}catch(Exception e){
					return false;
				}
				break;
			case "monoPlaneOverhead":
				return false;
			case "stereoDepth":
				return false;
			case "stereoDualTrackerPnP":
				return false;
			case "stereoQuadPnP":
				return false;
			case "depthDepthPnP":
				return false;
			case "default":
				try{
					visual_odometry = visual_odometry_generator.create_monoPlaneInfinity(75, 2, 1.5, 200);
					if(visual_odometry==null) return false;
				}catch(Exception e){
					return false;
				}
				break;
			default:
				return false;
		}
		
		return true;
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	private boolean process(){

		if(vo_type.equalsIgnoreCase("monoPlaneInfinity") || vo_type.equalsIgnoreCase("default")){
		
			MonocularPlaneVisualOdometry mono_vo = (MonocularPlaneVisualOdometry) visual_odometry;
			return process_mono(mono_vo);
		}
		else if(vo_type.equalsIgnoreCase("monoPlaneOverhead")){
			
			MonocularPlaneVisualOdometry mono_vo = (MonocularPlaneVisualOdometry) visual_odometry;
			return process_mono(mono_vo);			
		}
		else if(vo_type.equalsIgnoreCase("stereoDepth")){
			
			StereoVisualOdometry stereo_vo = (StereoVisualOdometry) visual_odometry;
			return process_stereo(stereo_vo);
		}			
		else if(vo_type.equalsIgnoreCase("stereoDualTrackerPnP")){
			StereoVisualOdometry stereo_vo = (StereoVisualOdometry) visual_odometry;
			return process_stereo(stereo_vo);
		}
		else if(vo_type.equalsIgnoreCase("stereoQuadPnP")){
			StereoVisualOdometry stereo_vo = (StereoVisualOdometry) visual_odometry;
			return process_stereo(stereo_vo);
		}
		else if(vo_type.equalsIgnoreCase("depthDepthPnP")){
			DepthVisualOdometry depth_vo = (DepthVisualOdometry) visual_odometry;
			return process_depth(depth_vo);
		}
		else{
			return false;
		}		
	}
	
	
	
	
	private boolean process_mono(MonocularPlaneVisualOdometry<I> mono_vo){

		
	
		processing_flag = true;
		
		if(input_source.equalsIgnoreCase("video")){
			
			left_img = video.next();	//If we have Video input, input is acquired here and we can resize Input and VO Panel
			
			if(fullres_preview){
				video_input_panel.setPreferredSize(new Dimension(left_img.getWidth(),left_img.getHeight()));
				video_input_frame.setTitle("Video Input ("+left_img.getWidth()+"x"+left_img.getHeight()+") Preview@Full-Res");
			
				if(img_keep_original){
					video_vo_panel.setPreferredSize(new Dimension(left_img.getWidth(),left_img.getHeight()));
					video_vo_frame.setTitle("VO Processing ("+left_img.getWidth()+"x"+left_img.getHeight()+") Preview@Full-Res");
				}else{
					video_vo_panel.setPreferredSize(new Dimension(img_resize_width,img_resize_height));
					video_vo_frame.setTitle("VO Processing (Resized: "+img_resize_width+"x"+img_resize_height+") Preview@Full-Res");
				}

			}else{
				video_input_panel.setPreferredSize(new Dimension(400,400));
				video_input_frame.setTitle("Video Input ("+left_img.getWidth()+"x"+left_img.getHeight()+") Preview@400x400");

				if(img_keep_original){
					video_vo_panel.setPreferredSize(new Dimension(400,400));
					video_vo_frame.setTitle("VO Processing ("+left_img.getWidth()+"x"+left_img.getHeight()+") Preview@400x400");
				}else{
					video_vo_panel.setPreferredSize(new Dimension(400,400));
					video_vo_frame.setTitle("VO Processing (Resized: "+img_resize_width+"x"+img_resize_height+") Preview@400x400");
				}
			}
		
			video_input_frame.pack();
			video_vo_frame.pack();
		}
	
		
		
		
		//Copy settings that have to remain static during Process
		final String selected_input_source = input_source;
		
		final Class<I> selected_imgType = imgType;
		final boolean selected_img_keep_original = img_keep_original;
		final int selected_img_resize_width = img_resize_width;
		final int selected_img_resize_height = img_resize_height;

		final int selected_chart_type = chart_type;	
		chart_y.setAxisNames(selected_chart_type==0 ? "frame" : "seconds", "Y");
		
		
		int numframe=0;
		long start_time = System.currentTimeMillis();
    	long partial_time = System.currentTimeMillis();
    	int current_fps=0;
    	int decimate_counter=0;
    	
    	String listData;
    	listData="Chart "+(chart_xz.getChartsCount()+1);
    	chart_info.addListData(listData);
    	
    	while(processing_flag){
    		
    		if(selected_input_source.equalsIgnoreCase("video")){
    			if(!video.hasNext()){processing_flag=false; continue;}
    		}else if (selected_input_source.equalsIgnoreCase("device")){
    			if(isStopped_capture && buffer.size()<=0){processing_flag=false; continue;}
    		}else{
    			processing_flag=false;return false;
    		}
    		
    		//Update Status
    		if(!chart_info.lbl_status.getText().equalsIgnoreCase("<html><b>Status: </b>Processing...</html>")){
    			chart_info.lbl_status.setText("<html><b>Status: </b>Processing...</html>");
    		}
    			
    		
			while(pause_flag){
				try {
					chart_info.lbl_status.setText("<html><b>Status: </b>Processing paused.</html>");
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if(reset_vo_flag){
				reset_vo_flag=false;
				mono_vo.reset();
			}
			
			if(stop_flag) {				
				chart_xz.addEndPoint();
				chart_y.addEndPoint();
				
				chart_info.addListData("End Chart "+(chart_xz.getChartsCount()+1));
				chart_info.lbl_status.setText("<html><b>Status: </b>Processing stopped.</html>");
				
				stop_flag = false; 
				processing_flag = false;				
				
				if(selected_input_source.equalsIgnoreCase("video"))video.close();
				else if(selected_input_source.equalsIgnoreCase("device")){					
					if(!isStopped_capture)stop_capture = true;				
					if(buffer.size()>0)buffer.clear();
				}
				return true;				
			}
			

    		if(clear_flag) {
    			chart_xz.clearAllPoints();
				chart_xz.resetSize();
				chart_y.clearAllPoints();
				chart_y.resetSize();
				
				chart_info.clearListData();
				chart_info.lbl_status.setText("<html><b>Status: </b>Cleared.</html>");
				chart_info.setInfoPanelVisible(false);
    			
    			clear_flag = false;
    			processing_flag = false;
    			
    			if(selected_input_source.equalsIgnoreCase("video"))video.close();
    			else if(selected_input_source.equalsIgnoreCase("device")){					
					if(!isStopped_capture)stop_capture = true;				
					if(buffer.size()>0)buffer.clear();
				}
    			return true;
    		}
    		
    		
    		
    		decimate_counter++;
    		BufferedImage orig;
    		
    		//Read original Input DATA (from Video file or Device Buffer)
    		if(selected_input_source.equalsIgnoreCase("video")){
    			if(decimate_enabled && decimate_counter%decimate_value!=0){video.next();continue;}
    			left_img = video.next();
    			orig = video.getGuiImage();
    		}else{
    			if(left_img==null || buffer==null ||  
    					buffer.size()<=0){
    				try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    				
    				continue;
    				}
    			
    			if(decimate_enabled && numframe%decimate_value!=0){buffer.remove(0);continue;}
    			orig = buffer.get(0);
    			buffer.remove(0);
    			//left_img is already initialized empty in camera init to the correct format
    			//here we convert buffered image into left_img
    			ConvertBufferedImage.convertFrom(orig,left_img,true);
    		}
    		
    		//Prepare processing Input DATA
    		I process_img;
    		BufferedImage process_orig;
			
    		if(selected_img_keep_original){
    			if(selected_input_source.equalsIgnoreCase("video") 
    					&& !internal_image_preview && !only_vo_preview) process_orig = deepCopy(orig);
    			else process_orig = orig;
    			
    			process_img = left_img;
    		}else{
    			
    			if(selected_imgType.equals(ImageUInt8.class)){
    				ImageType<I> factory = new ImageType<I>(Family.SINGLE_BAND, ImageDataType.U8,1);
    				process_img = factory.createImage(selected_img_resize_width, selected_img_resize_height);
    			}else{
    				ImageType<I> factory = new ImageType<I>(Family.SINGLE_BAND, ImageDataType.F32,1);
    				process_img = factory.createImage(selected_img_resize_width, selected_img_resize_height);
    			}
    			
    			process_orig = resizeBufferedImage(orig, selected_img_resize_width, selected_img_resize_height);
    			ConvertBufferedImage.convertFrom(process_orig,process_img,true);
			}
    		
    		if(internal_image_preview){
    			process_orig = new BufferedImage ( process_img.getWidth(), process_img.getHeight(), BufferedImage.TYPE_INT_ARGB );
				Graphics2D g = process_orig.createGraphics();
				g.setColor( new Color ( 0, 0, 0, 0 ));
				g.fillRect(0, 0, process_img.getWidth(), process_img.getHeight());
				g.dispose();
				ConvertBufferedImage.convertTo(process_img,process_orig,true);
    		}
    		
    		try{
    			if( !mono_vo.process(process_img) ) {
    				chart_info.lbl_status.setText("<html><b>Status: </b>VO Failed.</html>");
    				//throw new RuntimeException("VO Failed!");
    			}
    		}catch(Exception e){
    			chart_info.lbl_status.setText("<html><b>Status: </b>Visual odometry processing error. Check parameters.</html>");
    			e.printStackTrace();
    			return false;
    		}

			Se3_F64 leftToWorld = mono_vo.getCameraToWorld();
			Vector3D_F64 T = leftToWorld.getT();
			
			
			
			//VIDEO INPUT and VO VIDEO INPUT RENDERING
			Graphics2D g2 = process_orig.createGraphics();
			
			if(tracker_show_active_tracks){
			// draw active tracks as blue dots
			for( PointTrack p : tracker.getActiveTracks(null) ) {
				VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.blue);
			}
			}
			
			if(tracker_show_new_tracks){
			// draw tracks which have just been spawned as green dots
			for( PointTrack p : tracker.getNewTracks(null) ) {
				VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.green);
			}
			}
		
			
			// tell the Video GUI to update
			
			if(selected_input_source.equalsIgnoreCase("video")){
				if(!only_vo_preview){
					if(!video_input_frame.isVisible())video_input_frame.setVisible(true);
					video_input_panel.setBufferedImage(orig);
					video_input_panel.repaint();
				}else{
					if(video_input_frame.isVisible())video_input_frame.setVisible(false);
				}
			}
			
			if(!video_vo_frame.isVisible())video_vo_frame.setVisible(true);
			video_vo_panel.setBufferedImage(process_orig);
			video_vo_panel.repaint();
			
			
			numframe++;
			long current_time = System.currentTimeMillis();
			BigDecimal current_seconds =round_BigDecimal((float)(current_time-start_time)/1000,1);
			
						
			//Update Informations
			if(!chart_info.isInfoPanelVisible()) chart_info.setInfoPanelVisible(true);
			if(selected_input_source.equalsIgnoreCase("video") && chart_info.progress_buffer_load.isVisible()){
				chart_info.setBufferInfoVisible(false);
			}
			
			chart_info.lbl_processed_file.setText("<html><b>Processed Video: </b>"+video_path+"</html>");
			chart_info.lbl_processed_frame.setText("<html><b>Processed Frame: </b>"+numframe+"</html>");
			if(current_seconds.floatValue()<=60){
				chart_info.lbl_elapsed_time.setText("<html><b>Elapsed Time: </b>"+current_seconds+" sec</html>");
			}else{
				int current_minutes =(int)current_seconds.floatValue()/60;
				BigDecimal partial_seconds = round_BigDecimal(current_seconds.floatValue()%60,1);
				chart_info.lbl_elapsed_time.setText("<html><b>Elapsed Time: </b>"+current_minutes+" min : "+partial_seconds+" sec</html>");
			}
			chart_info.lbl_xpos.setText("<html><b>X: </b>"+T.x+"</html>");
			chart_info.lbl_ypos.setText("<html><b>Y: </b>"+T.y+"</html>");
			chart_info.lbl_zpos.setText("<html><b>Z: </b>"+T.z+"</html>");
			
			int inliers = countInliers(mono_vo);
			int newtracks = countNewTracks(mono_vo);
			int totaltracks = countTotalTracks(mono_vo);
			BigDecimal inliers_percent = new BigDecimal("0");
			try{
				inliers_percent = round_BigDecimal(100.0f*inliers/totaltracks,3);
			}catch(Exception e){}
			chart_info.lbl_tracks.setText("<html><b>Total tracked features:</b> "+totaltracks+" (inliers: "+inliers+" , new tracks: "+newtracks+")</html>");
			chart_info.lbl_inliers.setText("<html><b>Inliers (matches):</b> "+inliers_percent+"%</html>");
			
			
			float average_fps = 0;
			try{
				average_fps = round((numframe/current_seconds.floatValue()),2);
			}catch(Exception e){}			 
			if(selected_input_source.equalsIgnoreCase("video")){
				chart_info.lbl_input_fps_average.setText("<html><b>Average FPS:</b> "+average_fps+ " fps\n");
			}
			chart_info.lbl_vo_fps_average.setText("<html><b>Average FPS:</b> "+average_fps+ " fps\n");


			
			
			
			//CHART RENDERING // POINTS LOG // CURRENT FPS CALC

				//chart_xz.setBackgroundColor(Color.white);
				//chart_y.setBackgroundColor(Color.white);

			//ADDING FOUND POINTS TO CHART XZ (2D TRANSLATION)
			if(chart_xz.getChartScalingFactor()!=chart_xz_scale){
				chart_xz.setChartScalingFactor(chart_xz_scale);
				chart_xz.resetSize();
			}
	    	chart_xz.addPoint(T.x, T.z);
			
			/*IF CHART_TYPE=0 -> Add found Y and numframe TO CHART Y/f (ALTITUDE PER FRAME) 
	    						 And all points (x,y,z) to Points List in Info Panel*/
			if(selected_chart_type==0){
				chart_y.addPoint(numframe, T.y);
				
				listData = "Frame: "+numframe+", El. Time: "+current_seconds.floatValue()+" s, "+
							"Location X: "+round_BigDecimal((float)T.x,2)+", Y: "+round_BigDecimal((float)T.y,2)+", Z: "+round_BigDecimal((float)T.z,2)+
							", inliers: "+inliers_percent+"%, (Chart Type Y/f)";
				chart_info.addListData(listData);
			}
			
			
			if((current_time-partial_time)>=1000){
				
				current_fps++;
				
				//IF CHART_TYPE=1 -> Add found Y and current_seconds TO CHART Y (ALTITUDE PER SECOND)
				//					 And all points (x,y,z) to Points List in Info Panel
				if(selected_chart_type==1){
					chart_y.addPoint(current_seconds.floatValue(), T.y);
					listData = "Frame: "+numframe+", El. Time: "+current_seconds.floatValue()+" s, Location X: "+round_BigDecimal((float)T.x,2)+", Y: "+round_BigDecimal((float)T.y,2)+", Z: "+round_BigDecimal((float)T.z,2)+
					", inliers: "+inliers_percent+"%, (Chart Type Y/s)";
					chart_info.addListData(listData);
				}
				
				
				if(selected_input_source.equalsIgnoreCase("video")){
				chart_info.lbl_input_fps_current.setText("<html><b>Current FPS:</b> "+current_fps+ " fps\n");
				}
				chart_info.lbl_vo_fps_current.setText("<html><b>Current FPS:</b> "+current_fps+ " fps\n");
				
				
				current_fps=0;		
				partial_time=System.currentTimeMillis();
				
			}else{
				current_fps++;
				if(selected_chart_type==1){//Add found X,Z coord without Y to Points List in info panel also if has not passed 1 sec, if chart y type is Y/s
					listData = "Frame: "+numframe+", El. Time: "+current_seconds.floatValue()+" s, Location X: "+round_BigDecimal((float)T.x,2)+", Z: "+round_BigDecimal((float)T.z,2)+ 
							", inliers: "+inliers_percent+"%, (Chart Type Y/s)";
					chart_info.addListData(listData);
				}
			}

			
		}
		
    	
    	if(selected_input_source.equalsIgnoreCase("video"))video.close();

		chart_xz.addEndPoint();
		chart_y.addEndPoint();

		chart_info.addListData("End Chart "+(chart_xz.getChartsCount()+1));
		chart_info.lbl_status.setText("<html><b>Status: </b>Processing completed.</html>");
		
		return true;
	}
	
	
	
////OLD PROCESS System.out.println
	
////DURING CYCLE (EACH FRAME/CONTINUOUS PRINT)
//	System.out.printf("Location %8.2f %8.2f %8.2f      inliers %s\n", T.x, T.y, T.z, inlierPercent(mono_vo));
////DURING CYCLE (EACH SECOND PRINT THIS)    	
//	System.out.printf("Current Framerate: " + current_fps + " fps\n");
//	System.out.printf("\nAverage Framerate: " + numframe/((current_time-start_time)/1000) + " fps\n");

/////AFTER CYCLE PRINTS
//	float elapsed_seconds = round((float)(System.currentTimeMillis()-start_time)/1000,1);
//	float elapsed_minutes =round((float)(System.currentTimeMillis()-start_time)/1000/60,2);
//	System.out.printf("\n\nElapsed Time: "+ elapsed_seconds +" sec (="+ elapsed_minutes +" min)");
//	System.out.printf("\n\nTotal frames: "+ numframe);
//	System.out.printf("\n\nMedium framerate: "+ numframe/elapsed_seconds + " fps");
	
	
	
	private boolean process_stereo(StereoVisualOdometry<I> odometry){
		return true;
	}
	
	
	
	
	private boolean process_depth(DepthVisualOdometry<I, Depth> odometry){
		return true;
	}
	
	
	
	public static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	public static BufferedImage resizeBufferedImage(BufferedImage img, int newW, int newH) {  
	    int w = img.getWidth();  
	    int h = img.getHeight();  
	    BufferedImage dimg = new BufferedImage(newW, newH, img.getType());  
	    Graphics2D g = dimg.createGraphics();  
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	    RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
	    g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
	    g.dispose();  
	    return dimg;  
	}  
	
	/**
	 * If the algorithm implements AccessPointTracks3D, then count the number of inlier features
	 * and return a string.
	 */
	public static String inlierPercent(VisualOdometry<?> alg) {
		if( !(alg instanceof AccessPointTracks3D))
			return "";

		AccessPointTracks3D access = (AccessPointTracks3D)alg;
		
		int count = 0;
		int N = access.getAllTracks().size();
		for( int i = 0; i < N; i++ ) {
			if( access.isInlier(i) )
				count++;
		}
		
		return String.format("%%%5.3f", 100.0 * count / N);
	}

	public static int countInliers(VisualOdometry<?> alg) {
		if( !(alg instanceof AccessPointTracks3D))
			return 0;

		AccessPointTracks3D access = (AccessPointTracks3D)alg;
		
		int count = 0;
		int N = access.getAllTracks().size();
		for( int i = 0; i < N; i++ ) {
			if( access.isInlier(i) )
				count++;
		}
		
		return count;
	}
	
	public static int countNewTracks(VisualOdometry<?> alg) {
		if( !(alg instanceof AccessPointTracks3D))
			return 0;

		AccessPointTracks3D access = (AccessPointTracks3D)alg;
		
		int count = 0;
		int N = access.getAllTracks().size();
		for( int i = 0; i < N; i++ ) {
			if( access.isNew(i) )
				count++;
		}
		
		return count;
	}

	public static int countTotalTracks(VisualOdometry<?> alg) {
		if( !(alg instanceof AccessPointTracks3D))
			return 0;

		AccessPointTracks3D access = (AccessPointTracks3D)alg;
		
		return access.getAllTracks().size();
		
	}
	
	
	public static float round(float d, int decimalPlace) { //float return type hides decimal zeros (ex. 2.30 will be shown as 2.3), to preserve decimal zeros return type must be BigDecimal itself
		BigDecimal bd = new BigDecimal(Float.toString(d));
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd.floatValue();
	}
	
	public static BigDecimal round_BigDecimal(float d, int decimalPlace) { 
		BigDecimal bd = new BigDecimal(Float.toString(d));
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd;
	}
	
	
	//extracts an array of integer from strings formatted this way: 1,2,3,4 (comma separated numbers)
	private int[] extract_IntArray(String string){
				
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

	
	
	private boolean check_Settings(){
		
		 String[] options = {"OK"};
	

		if(calib_path==null || calib_path.equalsIgnoreCase("")){
			JOptionPane.showOptionDialog(main_frame, "Calibration Path is empty!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}
		
		if(input_source==null || input_source.equalsIgnoreCase("")){
			JOptionPane.showOptionDialog(main_frame, "Select an input source!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}
		
		if(input_source.equalsIgnoreCase("video")){			
			if (video_path==null || video_path.equalsIgnoreCase("")){
			JOptionPane.showOptionDialog(main_frame, "Video Path is empty!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
			}
		}else if(input_source.equalsIgnoreCase("device")){		
			if(device_path==null || device_path.equalsIgnoreCase("")){
				JOptionPane.showOptionDialog(main_frame, "Device Path is empty!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				return false;
			}
			if(device_width<=0){
				JOptionPane.showOptionDialog(main_frame, "Device Acquisition Width is negative or zero!\nUse only positive values","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				return false;
			}
			if(device_height<=0){
				JOptionPane.showOptionDialog(main_frame, "Device Acquisition Height is negative or zero!\nUse only positive values","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				return false;
			}
		}else{
			JOptionPane.showOptionDialog(main_frame, "Wrong input source type selected!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}
		
		
		
		if(imgType==null){
			JOptionPane.showOptionDialog(main_frame, "Select an image type!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}

		if(img_resize_width<=0){
			JOptionPane.showOptionDialog(main_frame, "Internal Image resize Width is negative or zero!\nUse only positive values","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}

		if(img_resize_height<=0){
			JOptionPane.showOptionDialog(main_frame, "Internal Image resize Height is negative or zero!\nUse only positive values","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}
		
		if(img_buffer_size<0){
			JOptionPane.showOptionDialog(main_frame, "Image Buffer Size is negative!\nUse only values >= 0 (0 stands for infinite)","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}		
		
		
		
		if(tracker_type==null || tracker_type.equalsIgnoreCase("")){
			JOptionPane.showOptionDialog(main_frame, "Select a Tracker Type!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}		

		if(klt_pyramidScaling==null || klt_pyramidScaling.equalsIgnoreCase("")){
			int choice = JOptionPane.showConfirmDialog(main_frame, "Pyramid Scaling is empty!\nUse default value (1,2,4,8)?","Error", JOptionPane.ERROR_MESSAGE & JOptionPane.OK_CANCEL_OPTION);
			if(choice==1){return false;}
			else {
				((JTextField) main_components.get("txt_klt_pyramidScaling")).setText("1,2,4,8");
				klt_pyramidScaling = "1,2,4,8";
			}
		}
		
		
		
		if(vo_type==null || vo_type.equalsIgnoreCase("")){
			JOptionPane.showOptionDialog(main_frame, "Select a Visual Odometry type!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}

		
		
		if(chart_type!=0 && chart_type!=1){
			JOptionPane.showOptionDialog(main_frame, "Select a correct output Chart Type!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}

		if(chart_xz_scale==0){
			JOptionPane.showOptionDialog(main_frame, "Insert an XZ Chart scaling factor different from zero!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}
		
		if(chart_y_scale==0){
			JOptionPane.showOptionDialog(main_frame, "Insert an Y Chart scaling factor different from zero!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			return false;
		}
		
		return true;

	}
	



	
	class MaximizeOnDblClick implements MouseListener{

		
		JComponent comp;
		JFrame container;
		SpringLayout maximize_comp = new SpringLayout();
		LayoutManager oldLayout=null;
		boolean full_screen=false;

		public MaximizeOnDblClick(JComponent comp, JFrame container){
			this.comp = comp;
			this.container = container;
			
			
			maximize_comp.putConstraint(SpringLayout.NORTH, comp, 5, SpringLayout.NORTH, container.getContentPane());
			maximize_comp.putConstraint(SpringLayout.WEST, comp, 5, SpringLayout.WEST, container.getContentPane());
			maximize_comp.putConstraint(SpringLayout.EAST, comp, -5, SpringLayout.EAST, container.getContentPane());
			maximize_comp.putConstraint(SpringLayout.SOUTH, comp, -5, SpringLayout.SOUTH, container.getContentPane());
			
		}
		
		
		@Override
		public void mouseClicked(MouseEvent evt) {
			
			
			if(evt.getClickCount()==2){
				if(!full_screen){
					full_screen=true;
					
					oldLayout = container.getContentPane().getLayout();
					
					for(int i=0;i<container.getContentPane().getComponentCount();i++){
						JComponent another_comp = (JComponent) container.getContentPane().getComponent(i);
						if(!another_comp.equals(comp)){
							another_comp.setVisible(false);
						}
					}
					
					container.getContentPane().setLayout(maximize_comp);					
				}else{
					full_screen=false;
					
					for(int i=0;i<container.getContentPane().getComponentCount();i++){
						Component another_comp = container.getContentPane().getComponent(i);
						if(!another_comp.equals(comp)){
							another_comp.setVisible(true);
						}
					}
					
					container.getContentPane().setLayout(oldLayout);
				}
				
				container.revalidate();
				comp.repaint();
				try{	//If we are maximizing a ChartScrollPane this provides ViewPort repainting (Axis names (eg.X,Y) repaint)
				ChartScrollPane chart = ((ChartScrollPane) comp);
				chart.getViewport().paintComponents(chart.getViewport().getGraphics());
				}catch(Exception e){}
			}
		}
		@Override
		public void mouseEntered(MouseEvent evt) {
			
		}
		@Override
		public void mouseExited(MouseEvent evt) {
		}
		@Override
		public void mousePressed(MouseEvent evt) {
			
		}
		@Override
		public void mouseReleased(MouseEvent evt) {
			
		}
	}
	
}
