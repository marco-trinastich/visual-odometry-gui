package vogui.main;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.thoughtworks.xstream.XStream;

import vogui.core.Core;
import vogui.parameters.ChartOutputParameters;
import vogui.parameters.DeviceParameters;
import vogui.parameters.InputParameters;
import vogui.parameters.InternalImageParameters;
import vogui.parameters.Parameters;
import vogui.parameters.ProcessingFlags;
import vogui.parameters.ProcessingParameters;
import vogui.parameters.TrackerParameters;
import vogui.parameters.VisualOdometryParameters;
import vogui.userinterface.UIGenerator;

public class VisualOdometryGUIMain {
	
	/**
	 * Visual Odometry GUI Application Main Entry Point
	 * (Tracking and Mapping System based on Visual Odometry)
	 * 
	 * Marco Trinastich
	 *
	 * @param args
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String args[]){
			
			File xmlSave = new File("Parameters.xml");
			File serializedSave = new File("Parameters.dat");
			
			Parameters parameters;
			
			if(xmlSave.exists()){ //If an XML Settings Savefile exists, load Parameters from XML file
				try{
					XStream xstream = new XStream();
					ArrayList loadedArray = (ArrayList)xstream.fromXML(new File("Parameters.xml"));
					parameters = new Parameters((InputParameters)loadedArray.get(0), 
										   		(InternalImageParameters)loadedArray.get(1), 
										   		(TrackerParameters)loadedArray.get(2), 
										   		(VisualOdometryParameters)loadedArray.get(3), 
										   		(ChartOutputParameters)loadedArray.get(4), 
										   		new ProcessingParameters(), 
										   		new ProcessingFlags(), 
										   		new DeviceParameters(),
										   		new HashMap<String, Component>());
				}catch (Exception exc){
					System.err.println("Error loading XML Save file");
					parameters = new Parameters(); //Sets default parameters
				}
			}else if(serializedSave.exists()){ //Else if a Serialized Settings Savefile exists, load Parameters from Serialized file
				try{
					ObjectInputStream objectInputStream = new ObjectInputStream(
							new FileInputStream("Parameters.dat"));
					
					parameters = new Parameters((InputParameters)objectInputStream.readObject(), 
										   		(InternalImageParameters)objectInputStream.readObject(), 
										   		(TrackerParameters)objectInputStream.readObject(),
										   		(VisualOdometryParameters)objectInputStream.readObject(), 
										   		(ChartOutputParameters)objectInputStream.readObject(), 
										   		new ProcessingParameters(), 
										   		new ProcessingFlags(), 
										   		new DeviceParameters(), 
										   		new HashMap<String, Component>());
					objectInputStream.close();
				}catch (Exception exc){
					System.err.println("Error loading Serialized Save file");
					parameters = new Parameters(); //Sets default parameters	
				}
			}else{ //If no Savefile exists
				parameters = new Parameters(); //Creates a new Default Parameters structure
			}
			

			Core core = new Core(parameters); //Creates a new Core algorithm (VO Process executor) based on parameters
			UIGenerator visualOdometryGUIApp = new UIGenerator(core); //Creates a new GUI (through UIGenerator) based 
																	  //on core (that controls core and parameters)
			visualOdometryGUIApp.startApp(); //Starts the App (GUI)

	}	

}
