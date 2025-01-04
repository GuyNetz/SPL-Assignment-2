package bgu.spl.mics.application;

import java.util.ArrayList;
import bgu.spl.mics.application.services.*;
import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;





/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Starting the GurionRock Pro Max Ultra Over 9000 simulation...");

        // if (args.length != 1) {
        //     System.err.println("Usage: java GurionRockRunner <configuration file path>");
        //     return;
        // }

        String configFilePath = "/workspaces/spl assignment 2/example input/configuration_file.json";

        try {      
            System.out.println("Parsing configuration file...");
            // Parse the configuration file using GSON
            JsonObject config = parseJsonConfig(configFilePath);

            System.out.println("Initializing services...");
            // Initialize TimeService
            int tickDuration = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();
            TimeService timeService = new TimeService(tickDuration, duration);
            


            /*********************************** Initialize CameraServices ***********************************/
            // Getting the relevant JSON objects
            JsonObject cameras = config.getAsJsonObject("Cameras");
            JsonArray camerasConfigurations = cameras.getAsJsonArray("CamerasConfigurations");
            // String camerasJsonPath = cameras.get("camera_datas_path").getAsString();
            // JsonObject camerasData = parseJsonConfig(camerasJsonPath);
            JsonObject camerasData = parseJsonConfig("/workspaces/spl assignment 2/example input/camera_data.json");

            //loop over all cameras
            CameraService[] cameraServices = new CameraService[camerasConfigurations.size()];
            for (int i = 0; i < camerasConfigurations.size(); i++) {
            
                //getting camera data from config
                JsonObject cameraConfig = camerasConfigurations.get(i).getAsJsonObject();
                int id = cameraConfig.get("id").getAsInt();
                int frequency = cameraConfig.get("frequency").getAsInt();
                String camera_key = cameraConfig.get("camera_key").getAsString();

                //getting current camera data from camera file
                JsonArray currentCamera = camerasData.getAsJsonArray(camera_key);
                JsonObject object = currentCamera.get(i).getAsJsonObject();
                int time = object.get("time").getAsInt();
                JsonArray currentCameraDetectedObjects = object.getAsJsonArray("detected_objects");  

                //creating a list of detected objects and a list of StampedDetectedObjects
                List<DetectedObject> detectedObjectsList = new ArrayList<>();
                List<StampedDetectedObjects> StampedDetectedObjectsList = new ArrayList<>();

                //going over all detected objects and creating a list of StampedDetectedObjects with time
                if(currentCameraDetectedObjects != null){
                    for (int j = 0; (j < currentCameraDetectedObjects.size()); j++) {
                        JsonObject detectedObject = currentCameraDetectedObjects.get(j).getAsJsonObject();
                        String detectedObjectID = detectedObject.get("id").getAsString();
                        String detectedObjectDescription = detectedObject.get("description").getAsString();
                        detectedObjectsList.add(new DetectedObject(detectedObjectID, detectedObjectDescription));
                    } 
                    StampedDetectedObjectsList.add(new StampedDetectedObjects(time, detectedObjectsList));
                }
                //creating a camera and a camera service
                Camera camera = new Camera(id, frequency, StampedDetectedObjectsList);
                cameraServices[i] = new CameraService(camera);
            }

            // Initialize LiDarServices
            JsonObject lidarWorkers = config.getAsJsonObject("LidarWorkers");
            JsonArray lidarConfigurations = lidarWorkers.getAsJsonArray("LidarConfigurations");

            LiDarService[] lidarServices = new LiDarService[lidarConfigurations.size()];
            for (int i = 0; i < lidarConfigurations.size(); i++) {
                JsonObject lidarConfig = lidarConfigurations.get(i).getAsJsonObject();

                LiDarWorkerTracker worker = new LiDarWorkerTracker(
                    lidarConfig.get("id").getAsInt(), 
                    lidarConfig.get("frequency").getAsInt()
                );

                lidarServices[i] = (new LiDarService(worker));
            }

            // Initialize PoseService
            PoseService poseService = null;
            String poseDataPath = config.get("poseJsonFile").getAsString();

            try(FileReader poseReader = new FileReader(poseDataPath)){
                Gson gson = new Gson();
                Type poseListType = new TypeToken<List<Pose>>(){}.getType();
                List<Pose> poseData = gson.fromJson(poseReader, poseListType);

                GPSIMU gpsimu = new GPSIMU();
                for (Pose pose : poseData) {
                    gpsimu.updateTick(pose.getX(), pose.getY(), pose.getYaw(), pose.getTime());
                }            
                poseService = new PoseService(gpsimu);
            }

            // Initialize FusionSlamService
            FusionSlam fusionSlam = FusionSlam.getInstance();
            FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);
            
            System.out.println("Starting simulation...");
            // Start the simulation
            startSimulation(timeService, cameraServices, lidarServices, poseService, fusionSlamService);
            System.out.println("Simulation completed.");

            System.out.println("Building output file...");
            //build the output file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            StatisticalFolder stats = StatisticalFolder.getInstance();
            List<LandMark> landMarks = fusionSlam.getLandMarks();

            OutputData outputData = new OutputData(stats, landMarks);

            try (FileWriter writer = new FileWriter("output.json")) {
                gson.toJson(outputData, writer); 
            }
            System.out.println("Output file created: output.json");

        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred during the simulation.");
        }
    }

    /**
     * Parses the JSON configuration file and returns a JsonObject.
     *
     * @param filePath Path to the JSON configuration file.
     * @return JsonObject representing the configuration.
     * @throws IOException if the file cannot be read.
     */
    private static JsonObject parseJsonConfig(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }


    /**
     * Starts the simulation by running all services.
     *
     * @param timeService      The TimeService instance.
     * @param cameraServices   Array of CameraService instances.
     * @param lidarServices    Array of LiDarWorkerService instances.
     * @param poseService      The PoseService instance.
     * @param fusionSlamService The FusionSlamService instance.
     */
    private static void startSimulation(
        TimeService timeService,
        CameraService[] cameraServices,
        LiDarService[] lidarServices,
        PoseService poseService,
        FusionSlamService fusionSlamService
    ) throws InterruptedException {   
        
        // Create a list of threads for all services
        List<Thread> threads = new ArrayList<>();

        // add camera and LiDAR services to the threads list
        for (CameraService camera : cameraServices) {
            threads.add(new Thread(camera));
        }
        
        for (LiDarService lidar : lidarServices) {
            threads.add(new Thread(lidar));
        }

        // add PoseService and FusionSlamService to the threads list
        threads.add(new Thread(poseService));
        threads.add( new Thread(fusionSlamService));

        // Start all threads except TimeService
        for (Thread thread : threads) {
            thread.start();
        }

        // Add TimeService to the threads list
        Thread timeThread = new Thread(timeService);
        threads.add(timeThread);
        
        // Start TimeService thread
        timeThread.start();

        // Wait for all threads to finish before returning
        for(Thread thread : threads){
            thread.join();
        }  
    }
}

@SuppressWarnings("unused")
class OutputData {
    private final int systemRuntime;
    private final int numDetectedObjects;
    private final int numTrackedObjects;
    private final int numLandmarks;
    private final List<LandMark> landMarks;

    public OutputData(StatisticalFolder stats, List<LandMark> landMarks) {
        this.systemRuntime = stats.getSystemRuntime();
        this.numDetectedObjects = stats.getNumDetectedObjects();
        this.numTrackedObjects = stats.getNumTrackedObjects();
        this.numLandmarks = stats.getNumLandmarks();
        this.landMarks = landMarks;
    }
}

