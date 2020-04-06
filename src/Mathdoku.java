import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Mathdoku{

    public static void main(String[] args) {
        Main.main(args);
    }

    private static int n = 6;
    private static BorderPane gui = new BorderPane();

    static void setN(int n) {
        Mathdoku.n = n;
    }

    static int getN() {
        return n;
    }

    public static class Main extends Application {

        private Parent createScene() {

            // Create Grid
            Grid grid = new Grid();
            gui.setCenter(grid.createGrid());
            // Create top UI buttons
            TopUI topUI = new TopUI();
            gui.setTop(topUI.createTopUI());
            // Create BottomUI
            BottomUI bottomUI = new BottomUI();
            gui.setBottom(bottomUI.createBottomUI());

            return gui;

        }

        private static Stage primaryStage;

        static Stage getStage() {
            return primaryStage;
        }

        @Override
        public void start(Stage stage) throws Exception{

            primaryStage = stage;

            Scene scene = new Scene(createScene());
            stage.setScene(scene);
            stage.setTitle("Mathdoku");
            stage.show();

            // Set the text of the selected box when a keyboard input is entered
            scene.setOnKeyPressed(e -> {

                Grid.Box.PushUndo();
                Grid.Box.setSelectedBoxMainText(e.getText());

                if (!(Grid.Box.CheckForErrors()) && Grid.Box.CheckAllBoxesFilled()) {

                    Grid.Box.playWinAnimation();

                }
            });

            //Detect window resize and adjust text positions accordingly
            scene.widthProperty().addListener(e -> Grid.Box.updateAllBoxTextPositions());
            scene.heightProperty().addListener(e -> Grid.Box.updateAllBoxTextPositions());

        }

        public static void main(String[] args) {
            launch(args);
        }

        static void NewGrid(int n) {

            setN((int) Math.sqrt(n));
            Grid newGrid = new Grid();
            gui.setCenter((newGrid.createGrid()));
            BottomUI bottomUI = new BottomUI();
            gui.setBottom(bottomUI.createBottomUI());
        }

    }

}
