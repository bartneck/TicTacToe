package org.minifigure;

import lejos.hardware.lcd.*;
import lejos.hardware.port.*;
import lejos.robotics.geometry.Point2D;
import lejos.robotics.geometry.Rectangle2D;
import lejos.utility.Delay;
import lejos.hardware.device.NXTCam;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Arrays;

public class Camera extends Thread {
	// the refresh rate of the tracking
	final static int INTERVAL = 100; // milliseconds
	// width=along the x-axis, height=along the y=axis
	// x=0,y=0 is at the top left of the camera
	// dimension of the board
	double xBoardMax, yBoardMax, xBoardMin, yBoardMin, fieldWidthX, fieldHeightY;
	// size and locations of each field
	Rectangle2D.Double[][] fieldsArrayGeometry = new Rectangle2D.Double[3][3];
	// name of the files
	String fileNameBoardDimensions = "fieldSize.txt";
	String fileNameFieldsDimensions ="fileNameFields.txt";
	int minBlobDimension = 13; // the minimum side length of a recognized object 
	NXTCam cameraNXT = new NXTCam(SensorPort.S1); // add the camera to port 1
	int numObjects; // the number of objects detected
	// 0=empty, 1=blue=human, 2=red=computer
	int[][] fieldsArrayState = new int [3][3]; 
	int SAMPLESIZE=15; // how many measurements are taken
	boolean stopped=false; // gate to stop the thread
	boolean readBlock=false; // gate to block reading
	int[][] buffer=new int[3][3];
	
	
	public Camera() {
		// configure camera
		cameraNXT.sortBy('A'); // sort objects by size
		cameraNXT.enableTracking(true); // start tracking
		//Arrays.fill(fieldsArrayState, 9);
		reset();
	}
	
	public void reset () {
		for (int[] row: fieldsArrayState) {
		    Arrays.fill(row, 0);
		}
	}
	
	// the main loop for reading the camera
	public void run () {
		while (!stopped) {
			try {
				getBoardFieldInternal(SAMPLESIZE);
			}
			catch (Exception ex) {
				LCD.drawString("camera failed", 0, 8);
				//System.out.println("Error: "+ex);
				//TODO: find a way to restart camera in case it crashes
				//TODO: find a way to test if the camera is operational
			}
			Delay.msDelay(INTERVAL);
		}
		// close everything and finish up
		//cameraNXT.enableTracking(false);
		//cameraNXT.close();
	}
	
	// not used for now
	public void activate() {
		cameraNXT.sortBy('A');
		cameraNXT.enableTracking(true);
	}
	// not used for now
	public void deactivate() {
		cameraNXT.enableTracking(false);
	}
	
	// calculate fields and write them to file
	private void setFields() {
		// create nine rectangles to represent the cells
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				// create the fields based on the dimensions of the board
				fieldsArrayGeometry[x][y]= new Rectangle2D.Double(xBoardMin+fieldWidthX*x, yBoardMin+fieldHeightY*y, fieldWidthX, fieldHeightY);
			}
			//System.out.println(Arrays.deepToString(fieldsArrayGeometry));
		}
		// write fields to file
		try {
            FileWriter fileWriterFields = new FileWriter(fileNameFieldsDimensions,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriterFields);
            bufferedWriter.write("New Fields "+fieldWidthX+" "+fieldHeightY+" "+"\n");
    			for (int x=0;x<3;x++) {
    				for (int y=0;y<3;y++) {
    					bufferedWriter.write(x+","+y+","+
    							(int) fieldsArrayGeometry[x][y].getMinX()+","+
    							(int) fieldsArrayGeometry[x][y].getMaxX()+","+
    							(int) fieldsArrayGeometry[x][y].getMinY()+","+
    							(int) fieldsArrayGeometry[x][y].getMaxY()+"\n");
    				}
    			}
            bufferedWriter.close();        }
        catch(IOException ex2) {
            System.out.println("Error writing to file '"+ fileNameFieldsDimensions + "'");
        }
	}

	// read the field rectangle from text file
	public void readBoardCalibration() {
        String line = null;
        try {
            FileReader fileReader = new FileReader(fileNameBoardDimensions);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            line = bufferedReader.readLine();
            String readFieldSize[]=line.split(",");
            xBoardMin=Double.parseDouble(readFieldSize[0]);
            xBoardMax=Double.parseDouble(readFieldSize[1]);
            yBoardMin=Double.parseDouble(readFieldSize[2]);
            yBoardMax=Double.parseDouble(readFieldSize[3]);
    			fieldWidthX=(xBoardMax-xBoardMin)/3;
    			fieldHeightY=(yBoardMax-yBoardMin)/3;
            bufferedReader.close();
            setFields();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" +fileNameBoardDimensions + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '"+ fileNameBoardDimensions + "'");                  
        }
	}
	
	
	public void calibrateBoard() {
		LCD.clear();
		LCD.refresh();
		
		int numObjectsCal = cameraNXT.getNumberOfObjects();
		Rectangle2D c = cameraNXT.getRectangle(0);
		
		// read the minimum values of the board
		// show data until enter button is pressed
		while(Button.ENTER.isUp()) {
			LCD.clear();
			LCD.drawString("Ball to 0,0", 0, 0);
			c = cameraNXT.getRectangle(0);
			numObjectsCal = cameraNXT.getNumberOfObjects();
			// show data about the tracked object on screen for checking
			LCD.drawString("objects: ", 0, 1);
			LCD.drawInt(numObjectsCal,1,9,1);	
			LCD.drawInt((int) c.getX(), 0, 3);
			LCD.drawInt((int) c.getY(), 4, 3);
			LCD.drawInt((int) c.getWidth(), 0, 4);
			LCD.drawInt((int) c.getHeight(), 4, 4);
			LCD.refresh();
			Delay.msDelay(INTERVAL);
		};
		
		// proceed only if an object of the right size is being tracked 
		while (numObjectsCal==0 || c.getHeight()<minBlobDimension || c.getWidth()<minBlobDimension) {
			c = cameraNXT.getRectangle(0);
			numObjectsCal = cameraNXT.getNumberOfObjects();
		};
		
		// set the minimum values
		xBoardMin=c.getX();//-r.getWidth();
		yBoardMin=c.getY();//-r.getHeight();
		
		// notify the user
		Sound.beep();
		LCD.clear();
		LCD.refresh();
		
		// read the maximum values of the board
		// show data until enter button is pressed
		while(Button.ENTER.isUp()) {
			LCD.clear();
			LCD.drawString("Ball to 2,2", 0, 0);
			c = cameraNXT.getRectangle(0);
			numObjectsCal = cameraNXT.getNumberOfObjects();
			LCD.drawString("objects: ", 0, 1);
			LCD.drawInt(numObjectsCal,1,9,1);	
			LCD.drawInt((int) c.getX(), 0, 3);
			LCD.drawInt((int) c.getY(), 4, 3);
			LCD.drawInt((int) c.getWidth(), 0, 4);
			LCD.drawInt((int) c.getHeight(), 4, 4);
			LCD.refresh();
			Delay.msDelay(INTERVAL);
		};
		
		// proceed only if an object of the right size is being tracked 
		numObjectsCal = cameraNXT.getNumberOfObjects();
		c = cameraNXT.getRectangle(0);	
		while (numObjectsCal==0 || c.getHeight()<minBlobDimension || c.getWidth()<minBlobDimension) {
			c = cameraNXT.getRectangle(0);
			numObjectsCal = cameraNXT.getNumberOfObjects();
		};

		// set the minimum values and the height and width of the fields
		xBoardMax=c.getX()+c.getWidth();
		yBoardMax=c.getY()+c.getHeight();
		fieldWidthX=(xBoardMax-xBoardMin)/3;
		fieldHeightY=(yBoardMax-yBoardMin)/3;

		// notify the user
		Sound.beep();
		// Calculate the dimensions for every field
		setFields();
		LCD.clear();
		LCD.refresh();
		
		// write field sizes to text file
		try {
            // Assume default encoding.
            FileWriter fileWriter = new FileWriter(fileNameBoardDimensions,false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String fieldSize=xBoardMin+","+xBoardMax+","+yBoardMin+","+yBoardMax;
            bufferedWriter.write(fieldSize);
            bufferedWriter.close();
        }
        catch(IOException ex) {
            System.out.println(
                "Error writing to file '"
                + fileNameBoardDimensions + "'");
        }
	}
	
	// find the most frequent number in an integer
	private int findMostFrequent(int[] Array) {
	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
	    for(int element: Array) {
	        Integer frequency = map.get(element);
	        map.put(element, (frequency != null) ? frequency + 1 : 1);      
	    }
	    int mostFrequentItem  = 0;
	    int[] maxFrequencies  = new int[2];
	    maxFrequencies[0]     = Integer.MIN_VALUE;

	    for(Entry<Integer, Integer> entry: map.entrySet())
	    {
	        if(entry.getValue()>= maxFrequencies[0])
	        {
	            mostFrequentItem  = entry.getKey();
	            maxFrequencies[1] = maxFrequencies[0];
	            maxFrequencies[0] = entry.getValue();
	        }
	    }
	    // this is for a tie
	    // TODO: Consider what happens when there is a tie between two occurrences
	    //if(maxFrequencies[1] == maxFrequencies[0])
	    //throw new Exception();//insert whatever exception seems appropriate
	    return mostFrequentItem;
	}
	
	// returns the state of the board
	public int[][] getBoardFields () {
		if (readBlock) {
			return buffer;
		}
		else {
			buffer=fieldsArrayState;
			return fieldsArrayState;
		}
	}
	
	private void getBoardFieldInternal(int SampleSize) {
		
		// array for sampling number of objects tracked
		// int numObjectsArrary[]= {0,0,0,0,0,0,0,0,0,0};
		// initialize sample array for field measurements

		int[][][] fieldsSample = new int[3][3][SampleSize];
		for (int s=0;s<SampleSize;s++) {
			for (int x=0;x<3;x++) {
				for (int y=0;y<3;y++) {
					fieldsSample[x][y][s]=0;
				}
			}	
		}
		
		// initialize center of fields variable
		Point2D.Double centerOfField = new Point2D.Double(0.0,0.0);
		
		// sample number of objects ten times
		/*
		for (int i=0;i<10;i++) {
			numObjectsArrary[i]=cameraNXT.getNumberOfObjects();
			//line = line +","+numObjectsArrary[i];
			Delay.msDelay(100);
		}
		numObjects = findMostFrequent(numObjectsArrary);
		*/
		
		numObjects = cameraNXT.getNumberOfObjects();
		//System.out.println("Nr Objects="+numObjects);
	
		// the main sample loop
		for (int s=0;s<SampleSize;s++) { // take SampleSize number of measurements
			if (numObjects >= 1 && numObjects <= 8) { // if there are 1-8 objects
				for (int i=0;i<numObjects;i++) { // for every object
					Rectangle2D r = cameraNXT.getRectangle(i); // get the rectangle for each object
					// check if the detected object is above the threshold
					if (r.getHeight()>minBlobDimension && r.getWidth()>minBlobDimension) {
						// go through all nine fields
						for (int x=0;x<3;x++) {
							for (int y=0;y<3;y++) {
								// get the center of the current field
								centerOfField.setLocation(r.getCenterX(), r.getCenterY());
								// check if the detected object's center is in the current field
								if (fieldsArrayGeometry[x][y].contains(centerOfField)) {
									// set the fieldsSample to the color of the tracked object
									fieldsSample[x][y][s]=cameraNXT.getObjectColor(i)+1;
								}
							}
						}
					}
				} // end loop of objects
			} // end if objects big enough of objects
			// This takes already long enough, so no delay is necessary
			// Delay.msDelay(INTERVAL);
		} // end main sampling loop
		
		String line="";
		// find the most frequent number for each field from sample
		readBlock=true; // prevent reading of fieldsArrayState while it is written into
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				fieldsArrayState[x][y]=findMostFrequent(fieldsSample[x][y]);
				line=line+"-"+findMostFrequent(fieldsSample[x][y]);
			}
		}
		readBlock=false; // release the read block
		//Sound.setVolume(1);
		//Sound.beep();
		//Sound.setVolume(8);
		//System.out.println(line);
		//LCD.clear();
		//LCD.drawString("Ad:"+cameraNXT.getCurrentMode(), 0, 1);
		//LCD.drawString("Po:"+cameraNXT.getAddress(), 5, 1);
		//LCD.drawString(numObjects+"         ", 0, 0);
		//LCD.refresh();
		//LCD.drawString(line, 0, 1);
		//return fieldsArrayState;
	} // end getting board
} // end class

