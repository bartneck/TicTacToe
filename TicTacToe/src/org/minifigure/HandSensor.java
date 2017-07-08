package org.minifigure;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.robotics.RangeFinderAdapter;
import lejos.robotics.objectdetection.RangeFeatureDetector;
import lejos.utility.Delay;

public class HandSensor {
	// maximum distance the sensor scans
    static final float MAX_DISTANCE = 20;
    // frequency of measuring
    static final int DETECTOR_DELAY = 100;
    EV3IRSensor handSensor;
    SampleProvider distance;
	float[] sample;
	SampleProvider averager;
	float[] averageSample;
	RangeFinderAdapter rangeFinder;
	RangeFeatureDetector detector;
	
	public HandSensor() {
		handSensor = new EV3IRSensor(SensorPort.S3);
		distance = handSensor.getMode("Distance");
		sample = new float[distance.sampleSize()];
		averager = new MeanFilter(distance,5);
		averageSample = new float[averager.sampleSize()];
		try {
			rangeFinder = new RangeFinderAdapter(handSensor.getDistanceMode());	
			detector = new RangeFeatureDetector(rangeFinder, MAX_DISTANCE, DETECTOR_DELAY);
		}
		catch (Exception ex) {
			//System.out.println("Error: "+ex);
			//TODO: find a way to restart IR sensor in case it crashes
		}
	}
	
	public boolean getHandReading() {
		for(int i=0; i<20;i++) {
			averager.fetchSample(averageSample, 0);
			System.out.println(i+". "+ averageSample[0]);
			Delay.msDelay(250);
		}
		return true;
	}
}
