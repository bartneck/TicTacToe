package org.minifigure;

import lejos.hardware.*;
import lejos.hardware.motor.*;
import lejos.hardware.port.*;
import lejos.utility.Delay;

import lejos.hardware.port.MotorPort;

public class Arm {
//	define location for each field, 
//	first table (C, +=left, -=right) then rail (A, +=in,-=out,)

int[][][] locationArray= {
		{
			{644,-6000},
			{728,-3391},
			{750,123}
		},
		{
			{146,-5459},
			{146,-2795},
			{200,0}
		},
		{
			{-361,-6000},
			{-477,-2500},
			{-685,0}
		}
		
};

// initiate motors
EV3LargeRegulatedMotor rail = new EV3LargeRegulatedMotor(MotorPort.A);
EV3LargeRegulatedMotor gate = new EV3LargeRegulatedMotor(MotorPort.B);
EV3MediumRegulatedMotor table = new EV3MediumRegulatedMotor(MotorPort.C);


	public boolean putBall(int x, int y) {
		rail.setSpeed(800);
		this.moveToField(x,y);
		this.releaseBall();
		this.moveToField(0,0); 
		return true;
	}

	public void testArray() {
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				System.out.println(i+","+j+","+ locationArray[i][j][0]+","+locationArray[i][j][1]);
			}
		}
	}
	
	public void moveToField(int x,int y){
		table.rotateTo(locationArray[x][y][0], true);
		rail.rotateTo(locationArray[x][y][1], true);
		while (table.isMoving() || rail.isMoving()) {
			Delay.msDelay(100);
		}
	}
	
	public void releaseBall() {
		gate.rotate(90);
		// wait for ball to leave rail
		Delay.msDelay(1000);
	}
}

