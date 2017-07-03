package org.minifigure;

import lejos.hardware.lcd.*;
import lejos.hardware.port.*;
import lejos.robotics.geometry.Point2D;
import lejos.robotics.geometry.Rectangle2D;
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

public class Camera {
	// the refresh rate of the tracking
	final static int INTERVAL = 100; // milliseconds
	double xMax, yMax, xMin, yMin, cellWidth, cellHeight;
	Rectangle2D.Double[][] fields = new Rectangle2D.Double[3][3];
	String fileName = "fieldSize.txt";
	String fileNameFields ="fileNameFields.txt";
	int minRectangleSide = 13;
	NXTCam camera = new NXTCam(SensorPort.S1); // add the camera to port 1
	String objects = "Objects: ";
	int numObjects;
	int[][] fieldSet = {{9,9,9},{9,9,9},{9,9,9}};
	
	public Camera() {
		// configure camera
		camera.sendCommand('A'); // sort objects by size
		camera.sendCommand('E'); // start tracking
	}
	
	private void setFields() {
		// create nine rectangles to represent the cells
		for (int y=0;y<3;y++) {
			for (int x=0;x<3;x++) {
				fields[x][y]= new Rectangle2D.Double(xMin+cellWidth*x, yMin+cellHeight*y, cellWidth, cellHeight);
			}
		}
		// write fields to file
		try {
            FileWriter fileWriterFields = new FileWriter(fileNameFields,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriterFields);
            bufferedWriter.write("New Fields "+cellWidth+" "+cellHeight+" "+"\n");
    			for (int y=0;y<3;y++) {
    				for (int x=0;x<3;x++) {
    					bufferedWriter.write(x+","+y+","+
    							(int) fields[x][y].getMinX()+","+
    							(int) fields[x][y].getMaxX()+","+
    							(int) fields[x][y].getMinY()+","+
    							(int) fields[x][y].getMaxY()+"\n");
    				}
    			}
            bufferedWriter.close();        }
        catch(IOException ex2) {
            System.out.println("Error writing to file '"+ fileNameFields + "'");
        }
	}

	// read the field rectangle from text file
	public void readCalibration() {
        String line = null;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            line = bufferedReader.readLine();
            String readFieldSize[]=line.split(",");
            xMin=Double.parseDouble(readFieldSize[0]);
            xMax=Double.parseDouble(readFieldSize[1]);
            yMin=Double.parseDouble(readFieldSize[2]);
            yMax=Double.parseDouble(readFieldSize[3]);
    			cellWidth=(xMax-xMin)/3;
    			cellHeight=(yMax-yMin)/3;
            bufferedReader.close();
            setFields();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" +fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '"+ fileName + "'");                  
        }
	}
	
	
	public void calibrate() {
		LCD.clear();
		LCD.refresh();
		
		int numObjects = camera.getNumberOfObjects();
		Rectangle2D c = camera.getRectangle(0);
		
		// read the minimum values of the board
		// show data until enter button is pressed
		while(Button.ENTER.isUp()) {
			LCD.clear();
			LCD.drawString("Ball to 0,0", 0, 0);
			c = camera.getRectangle(0);
			numObjects = camera.getNumberOfObjects();
			// show data about the tracked object for checking
			LCD.drawString("objects: ", 0, 1);
			LCD.drawInt(numObjects,1,9,1);	
			LCD.drawInt((int) c.getX(), 0, 3);
			LCD.drawInt((int) c.getY(), 4, 3);
			LCD.drawInt((int) c.getWidth(), 0, 4);
			LCD.drawInt((int) c.getHeight(), 4, 4);
			LCD.refresh();
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		
		// proceed only if an object of the right size is being tracked 
		while (numObjects==0 || c.getHeight()<minRectangleSide || c.getWidth()<minRectangleSide) {
			c = camera.getRectangle(0);
			numObjects = camera.getNumberOfObjects();
		};
		
		// set the minimum values
		xMin=c.getX();//-r.getWidth();
		yMin=c.getY();//-r.getHeight();
		
		// notify the user
		Sound.beep();
		LCD.clear();
		LCD.refresh();
		
		// read the maximum values of the board
		// show data until enter button is pressed
		while(Button.ENTER.isUp()) {
			LCD.drawString("Ball to 2,2", 0, 0);
			LCD.clear();
			c = camera.getRectangle(0);
			numObjects = camera.getNumberOfObjects();
			LCD.drawString("objects: ", 0, 1);
			LCD.drawInt(numObjects,1,9,1);	
			LCD.drawInt((int) c.getX(), 0, 3);
			LCD.drawInt((int) c.getY(), 4, 3);
			LCD.drawInt((int) c.getWidth(), 0, 4);
			LCD.drawInt((int) c.getHeight(), 4, 4);
			LCD.refresh();
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		
		// proceed only if an object of the right size is being tracked 
		numObjects = camera.getNumberOfObjects();
		c = camera.getRectangle(0);	
		while (numObjects==0 || c.getHeight()<minRectangleSide || c.getWidth()<minRectangleSide) {
			c = camera.getRectangle(0);
			numObjects = camera.getNumberOfObjects();
		};

		// set the minimum values and the height and width of the fields
		xMax=c.getX()+c.getWidth();
		yMax=c.getY()+c.getHeight();
		cellWidth=(xMax-xMin)/3;
		cellHeight=(yMax-yMin)/3;

		// notify the user
		Sound.beep();
		// Calculate the dimensions for every field
		setFields();
		LCD.clear();
		LCD.refresh();
		
		// write field size to text file
		try {
            // Assume default encoding.
            FileWriter fileWriter = new FileWriter(fileName,false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String fieldSize=xMin+","+xMax+","+yMin+","+yMax;
            bufferedWriter.write(fieldSize);
            bufferedWriter.close();
        }
        catch(IOException ex) {
            System.out.println(
                "Error writing to file '"
                + fileName + "'");
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
	    //if(maxFrequencies[1] == maxFrequencies[0])
	    //throw new Exception();//insert whatever exception seems appropriate
	    return mostFrequentItem;
	}
	
	// returns the state of the board
	// requires the number of measurement samples
	public int[][] getBoardFields(int SampleSize) {
		int[][][] fieldSetSample = new int[3][3][SampleSize];
		for (int s=0;s<SampleSize;s++) {
			for (int y=0;y<3;y++) {
				for (int x=0;x<3;x++) {
					fieldSetSample[x][y][s]=9;
				}
			}	
		}
		Point2D.Double center = new Point2D.Double(0.0,0.0);
		
		// the main loop
		for (int s=0;s<SampleSize;s++) {
			numObjects = camera.getNumberOfObjects();
			if (numObjects >= 1 && numObjects <= 8) {
				for (int i=0;i<numObjects;i++) {
					Rectangle2D r = camera.getRectangle(i);
					if (r.getHeight()>minRectangleSide && r.getWidth()>minRectangleSide) {
						for (int y=0;y<3;y++) {
							for (int x=0;x<3;x++) {
								center.setLocation(r.getCenterX(), r.getCenterY());
								if (fields[x][y].contains(center)) {
									fieldSetSample[x][y][s]=camera.getObjectColor(i);
								}
							}
						}
					}
				} // end loop of objects
			} // end if objects big enough of objects
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} // end main loop
		String line="";
		
		// find the most frequent number for each field
		for (int y=0;y<3;y++) {
			for (int x=0;x<3;x++) {
				for (int s=0;s<SampleSize;s++) {
					line = line +","+fieldSetSample[x][y][s];
				}
				int Array[]=fieldSetSample[x][y];
				System.out.println(line);
				System.out.println(findMostFrequent(Array));
				line="";
				fieldSet[x][y]=findMostFrequent(Array);
			}
		}
		return fieldSet;
	} // end start recording
} // end class

