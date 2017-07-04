package org.minifigure;

import lejos.hardware.Sound;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;


public class Alarm implements FeatureListener{
	boolean handDetected=false;
	
	public void featureDetected(Feature feature, FeatureDetector detector) {
		detector.enableDetection(false);
		// hand detected
		handDetected=true;
		detector.enableDetection(true);
		handDetected=false;
	}
	
	public boolean handDetected() {
		return handDetected;
	}
}
