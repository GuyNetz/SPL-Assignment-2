package bgu.spl.mics.application.objects;

import java.util.List;


/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private int id;
    private int frequency;
    private STATUS status; // "Up", "Down", "Error"
    private List<StampedDetectedObjects> detectedObjectsList;

    // Constructor
    public Camera (int id, int frequency, List<StampedDetectedObjects> detectedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.detectedObjectsList = detectedObjectsList;
        this.status = STATUS.UP;
        
    }

    // Methods
    public int getID() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public StampedDetectedObjects getNextObjectToProcess(int currentTick, int frequency) {
    for (int i = 0; i < detectedObjectsList.size(); i++) {
        StampedDetectedObjects stampedObject = detectedObjectsList.get(i);
        
        // Check if the object is ready to be processed
        if (stampedObject.getTime() + frequency <= currentTick) {
            
            // Check for error condition
            if (stampedObject.getDetectedObjects().stream()
                    .anyMatch(detectedObject -> detectedObject.getId().equals("ERROR"))) {
                setStatus(STATUS.ERROR); // Update camera status
                return null; // Indicate an error occurred
            }

            // Remove and return the stamped object for processing
            detectedObjectsList.remove(i);
            return stampedObject;
        }
    }
    return null; // No objects ready to process
}


}
