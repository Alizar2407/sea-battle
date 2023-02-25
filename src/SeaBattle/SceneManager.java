package SeaBattle;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;

public class SceneManager {
    private static SceneManager instance = null;
    private static Stage currentStage = null;
    private static Scene currentScene = null;
    private static Scene[] initializedScenes = null;

    private static ArrayList<String> sceneFiles = new ArrayList<>(Arrays.asList("SeaBattleMainScene.fxml"));

    private SceneManager() {
        currentStage = new Stage();
        currentStage.setResizable(false);

        initializedScenes = new Scene[sceneFiles.size()];
        for(int sceneIndex = 0; sceneIndex < sceneFiles.size(); ++sceneIndex) {
            String sceneName = sceneFiles.get(sceneIndex);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneName));
                Pane pane = loader.load();
                initializedScenes[sceneIndex] = new Scene(pane);
            } catch (IOException e) {
                System.out.printf("Cannot initialize scene %s\n", sceneName);
                e.printStackTrace();
                break;
            }
        }
    }

    public static void initialize() {
        new SceneManager();
    }

    public static SceneManager getInstance() {
        if(instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public static void setScene(String sceneName) {
        int sceneIndex = sceneFiles.indexOf(sceneName);
        
        currentScene = initializedScenes[sceneIndex];
        currentStage.setScene(currentScene);
        currentStage.show();
        
        currentStage.setTitle("Sea battle");
    }

    public static Scene currentScene() {
        if (currentScene == null)
            currentScene = initializedScenes[0];
        
        return currentScene;
    }
}
