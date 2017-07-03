package org.minifigure;

import lejos.hardware.Sound;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;

public class Alarm implements FeatureListener{

	public void featureDetected(Feature feature, FeatureDetector detector) {
		detector.enableDetection(false);
		// hand detected
		Sound.beep();
		System.out.println("hand detected");
		detector.enableDetection(true);
		System.out.println("do nothing");
	} 
}
