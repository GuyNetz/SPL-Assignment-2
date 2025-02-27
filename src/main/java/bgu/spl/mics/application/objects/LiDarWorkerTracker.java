package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    //Fields
    private int id;
    private int frequency;
    private STATUS status; // "Up", "Down", "Error"
    private List<TrackedObject> trackedObjectsList;
    private TrackedObject errorObject;


    //Constructor
    public LiDarWorkerTracker(int id, int frequency, List<TrackedObject> trackedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.trackedObjectsList = trackedObjectsList;
        this.status = STATUS.UP;
        
    }

    //Methods
    public int getID() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public TrackedObject getErrorObject() {
        return errorObject;
    }

    public List<TrackedObject> getTrackedObjectsList() {
        return trackedObjectsList;
    }
    
    public void setStatus(STATUS status) {
        this.status = status;
    }



    
}
