package org.minifigure;
import lejos.hardware.Sound;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.robotics.RangeFinderAdapter;
import lejos.robotics.objectdetection.RangeFeatureDetector;
import lejos.utility.Delay;

public class HandSensor {
    static final float MAX_DISTANCE = 20;
    static final int DETECTOR_DELAY = 100;
	
	EV3IRSensor handSensor = new EV3IRSensor(SensorPort.S2);
	SampleProvider distance = handSensor.getMode("Distance");
	float[] sample = new float[distance.sampleSize()];
	SampleProvider averager = new MeanFilter(distance,5);
	float[] averageSample = new float[averager.sampleSize()];
	
	RangeFinderAdapter rangeFinder = new RangeFinderAdapter(handSensor.getDistanceMode());	
	RangeFeatureDetector detector = new RangeFeatureDetector(rangeFinder, MAX_DISTANCE, DETECTOR_DELAY);
	

	public void getHandReading() {
		for(int i=0; i<20;i++) {
			averager.fetchSample(averageSample, 0);
			System.out.println(i+". "+ averageSample[0]);
			Delay.msDelay(250);
		}
	}
}
