package SeaBattle;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApp extends Application {
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage startStage) throws Exception {
        SceneManager.initialize();
        SceneManager.setScene("SeaBattleMainScene.fxml");
    }
}
