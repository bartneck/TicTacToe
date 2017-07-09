package org.minifigure;

import lejos.hardware.motor.*;
import lejos.utility.Delay;
import lejos.hardware.port.MotorPort;

public class Arm {
	EV3LargeRegulatedMotor rail;
	EV3LargeRegulatedMotor gate;
	EV3MediumRegulatedMotor table;
	//	define location for each field, 
	//	first table (C, +=left, -=right) then rail (A, +=in,-=out,)
	int[][][] locationArray= {
			{
				{644,-6000},		// x=0,y=0
				{146,-5459},		// x=0,y=1
				{-361,-6000},	// x=0,y=2
			},
			{
				{728,-3391},		// x=1,y=0
				{146,-2795},		// x=1,y=1
				{-477,-2500},	// x=1,y=2
			},
			{
				{750,123},		// x=2,y=0
				{200,0},			// x=2,y=1
				{-685,0}			// x=2,y=2
			}
	};
	
	public Arm(){
		// initiate motors
		rail = new EV3LargeRegulatedMotor(MotorPort.A);
		gate = new EV3LargeRegulatedMotor(MotorPort.B);
		table = new EV3MediumRegulatedMotor(MotorPort.C);
		rail.setSpeed(800);
	}
	
	public void putBall(int x, int y) {
		// move arm to target field
		table.rotateTo(locationArray[x][y][0], true);
		rail.rotateTo(locationArray[x][y][1], true);
		// rail.waitComplete();
		while (table.isMoving() || rail.isMoving()) {}
		// release ball
		gate.rotate(90);
		// wait for ball to leave rail
		Delay.msDelay(1000);
		// go home
		table.rotateTo(0, true);
		rail.rotateTo(0, true);
		// rail.waitComplete();
		while (table.isMoving() || rail.isMoving()) {}
	}
	
	/*
	public void testArray() {
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				System.out.println(i+","+j+","+ locationArray[i][j][0]+","+locationArray[i][j][1]);
			}
		}
	}
	*/
}

