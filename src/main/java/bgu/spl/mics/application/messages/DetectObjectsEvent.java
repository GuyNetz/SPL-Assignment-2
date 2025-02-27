package bgu.spl.mics.application.messages;

import java.util.List;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.Event;

public class DetectObjectsEvent implements Event<List<DetectedObject>> {

    // The time at which the objects were detected.
    private final int detectionTime;

    // A list of detected objects, each containing an ID and a description.
    private final StampedDetectedObjects stampedDetectedObjects;

    // Constructor
    public DetectObjectsEvent(int detectionTime, StampedDetectedObjects stampedDetectedObjects) {
        this.detectionTime = detectionTime;
        this.stampedDetectedObjects = stampedDetectedObjects;
    }

    // Getters
    public int getDetectionTime() {
        return detectionTime;
    }

    public StampedDetectedObjects getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }
}
