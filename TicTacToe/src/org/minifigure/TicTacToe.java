package org.minifigure;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.*;
import lejos.utility.TextMenu;

public class TicTacToe {
	private static final int SAMPLESIZE=20;
	Arm myArm = new Arm();
	Camera myCamera = new Camera();
	
	private void calibration () {
		String[] menuItems={"Load","New"};
		TextMenu menu=new TextMenu(menuItems,1,"Calibrate Field:");
		int menu_item;
		menu_item=menu.select();
	    	switch (menu_item) {
			case 0: myCamera.readCalibration();
			break;
			case 1: myCamera.calibrate();
			break;
	    }
	}
	
	private void go () {
		myArm.testArray();
		//System.out.println("arm created");
		//HandSensor myHandSensor = new HandSensor();
		//System.out.println("handsensor created");
		//myHandSensor.detector.enableDetection(true);
		//System.out.println("enabled detection");
		//Alarm myAlarm = new Alarm();
		//System.out.println("alarm created");
		//myHandSensor.detector.addListener(myAlarm);
		//System.out.println("alarm registered");

		//System.out.println("camera created");
		
		calibration();
	    	int myBoard[][]=myCamera.getBoardFields(SAMPLESIZE);
		//System.out.println("recording started");
		
		// myHandSensor.getHandReading();
		// testArm(myArm);
		
		// infinity loop to keep program alive
		while(Button.ESCAPE.isUp()) Thread.yield();	
	}
	
	public static void main(String[] args) {
		TicTacToe ttt = new TicTacToe();
		ttt.go();
	}
	
	private void testArm(Arm armToTest) { 
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				armToTest.putBall(i,j);
				Sound.beep();
				LCD.drawInt(i, 0, 2);
			}
		}
	}
}
