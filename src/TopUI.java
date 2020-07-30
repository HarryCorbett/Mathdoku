import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

class TopUI extends Node {

    private static Boolean showErrors = false;
    private static Button undo;
    private static Button redo;

    static Boolean getShowErrors() {
        return showErrors;
    }

    /**
     * Creating buttons at the top of the application for controls
     *
     * @return flowPane containing the buttons
     */
    Parent createTopUI() {

        FlowPane UIPane = new FlowPane(Orientation.HORIZONTAL);
        UIPane.setAlignment(Pos.CENTER);
        UIPane.setHgap(10);
        UIPane.setVgap(5);
        UIPane.setPadding(new Insets(10));

        undo = new Button("Undo");
        redo = new Button("Redo");
        Button clear = new Button("Clear");

        Button loadFromFile = new Button("load From File");
        Button loadFromText = new Button("load From Text");
        loadFromFile.setDisable(true);
        loadFromText.setDisable(true);

        final CheckBox[] showErrorsCheckbox = {new CheckBox("Show errors")};
        Button randomGame = new Button("Generate random game");

        HBox fontSizePane = new HBox();
        fontSizePane.setSpacing(5);
        Label fontSizeLabel = new Label("Font size:");
        fontSizeLabel.setTranslateY(4);
        ChoiceBox<String> fontSize = new ChoiceBox<>();
        fontSize.getItems().addAll("Small", "Medium", "Large");
        fontSize.setValue("Medium");
        fontSizePane.getChildren().addAll(fontSizeLabel, fontSize);

        undo.setDisable(true);
        redo.setDisable(true);

        undo.setOnAction(e -> Grid.Box.PopUndo());
        redo.setOnAction(e -> Grid.Box.PopRedo());

        undo.setPrefWidth(100);
        redo.setPrefWidth(100);
        clear.setPrefWidth(100);
        loadFromFile.setPrefWidth(100);
        loadFromText.setPrefWidth(100);

        UIPane.getChildren().addAll(undo, redo, clear, loadFromFile, loadFromText, showErrorsCheckbox[0], fontSizePane, randomGame);

        fontSize.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) ->
                Grid.Box.updateFontSizes(fontSize.getItems().get((Integer) number2)));


        showErrorsCheckbox[0].selectedProperty().addListener((observable, oldValue, newValue) -> {

            showErrors = !showErrors;
            Grid.Box.CheckForErrors();

        });

        clear.setOnAction(event -> {

            Alert confirmClearAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear the grid?");

            confirmClearAlert.setTitle("Clear");
            confirmClearAlert.setHeaderText("Are you sure?");

            Optional<ButtonType> result = confirmClearAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Grid.Box.clearAllBoxes();
                Grid.Box.ClearStacks();
            }
        });

        loadFromFile.setOnAction(actionEvent -> {

            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle("Choose file");
            FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text files", "*.txt");
            fileChooser.getExtensionFilters().add(txtFilter);

            File file = fileChooser.showOpenDialog(Mathdoku.Main.getStage());

            boolean errorDetected = false;
            int count = 0;

            // Read to work out N and create grid ready for cages and check for errors
            if (file != null && file.exists() && file.canRead()) {

                try {
                    BufferedReader buffered = new BufferedReader(new FileReader(file));
                    String line;
                    ArrayList<String> usedBoxes = new ArrayList<>();

                    while ((line = buffered.readLine()) != null) {

                        String[] resultAndBoxes;

                        if (line.contains(" ")) {
                            resultAndBoxes = line.split(" ");
                        } else {

                            errorDetected = true;
                            break;
                        }

                        String[] stringBoxes;
                        if (resultAndBoxes[1].contains(",")) {
                            stringBoxes = (resultAndBoxes[1].split(","));
                        } else {
                            stringBoxes = resultAndBoxes[1].split("");
                        }
                        count += stringBoxes.length;

                        errorDetected = checkInputForErrors(stringBoxes, usedBoxes, errorDetected);

                    }
                    buffered.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Read to get cage information and create cages
            if (file != null && file.exists() && file.canRead()) {
                if (!errorDetected) {

                    Mathdoku.Main.NewGrid(count);

                    try {
                        BufferedReader buffered = new BufferedReader(
                                new FileReader(file));
                        String line;
                        while ((line = buffered.readLine()) != null) {

                            String[] resultAndBoxes = line.split(" ");

                            generateCage(resultAndBoxes);

                        }
                        buffered.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {

                    displayFileError();

                }
            }
        });

        loadFromText.setOnAction(actionEvent -> {

            Stage textInput = new Stage();
            textInput.initModality(Modality.APPLICATION_MODAL);
            textInput.setTitle("Load from Text");

            Label enterTextLabel = new Label("Enter your text:");
            enterTextLabel.setPadding(new Insets(5));

            TextArea text = new TextArea();
            text.setPrefSize(300, 200);

            Button cancel = new Button("close");
            cancel.setOnAction(e -> textInput.close());

            Button submit = new Button("submit");
            submit.setOnAction(e -> {

                readFromText(text.getText());
                textInput.close();

            });

            HBox hBox = new HBox(5);
            hBox.getChildren().addAll(cancel, submit);
            hBox.setPadding(new Insets(5));

            BorderPane borderPane = new BorderPane();
            borderPane.setPadding(new Insets(10));

            borderPane.setCenter(text);
            borderPane.setTop(enterTextLabel);
            borderPane.setBottom(hBox);

            Scene enterTextPopup = new Scene(borderPane);
            textInput.setScene(enterTextPopup);

            textInput.showAndWait();

        });

        randomGame.setOnAction(actionEvent -> {

            Stage randomGameMenu = new Stage();
            randomGameMenu.initModality(Modality.APPLICATION_MODAL);
            randomGameMenu.setTitle("Generate Random Game");

            Label sizeLabel = new Label("Grid size:");
            sizeLabel.setPadding(new Insets(5));
            Slider sizeSlider = new Slider(2, 8, 5);
            sizeSlider.setBlockIncrement(1);
            sizeSlider.setMajorTickUnit(1);
            sizeSlider.setMinorTickCount(0);
            sizeSlider.setShowTickLabels(true);
            sizeSlider.setSnapToTicks(true);
            sizeSlider.setShowTickMarks(true);

            Label difficultyLabel = new Label("Difficulty:");
            difficultyLabel.setPadding(new Insets(5));
            ChoiceBox<String> difficultyChoice = new ChoiceBox<>();
            difficultyChoice.getItems().addAll("Easy","Medium","Hard");
            difficultyChoice.setValue("Medium");

            Button cancel = new Button("close");
            cancel.setOnAction(e -> randomGameMenu.close());

            Button submit = new Button("submit");
            submit.setOnAction(e -> {

                // difficultyInt represents the maximum cage size for the random grid
                int difficultyInt = 4;

                switch (difficultyChoice.getValue()){

                    case "Easy":
                        difficultyInt = 3;
                        break;
                    case "Hard":
                        difficultyInt = 5;
                        break;
                }

                GenerateRandomGrid((int) Math.round(sizeSlider.getValue()),difficultyInt);
                randomGameMenu.close();

            });

            VBox vBox = new VBox(5);
            vBox.getChildren().addAll(sizeLabel,sizeSlider,difficultyLabel,difficultyChoice);
            vBox.setPadding(new Insets(5));

            HBox hBox = new HBox(5);
            hBox.getChildren().addAll(cancel, submit);
            hBox.setPadding(new Insets(5));

            BorderPane borderPane = new BorderPane();
            borderPane.setPadding(new Insets(10));

            borderPane.setBottom(hBox);
            borderPane.setCenter(vBox);

            borderPane.setPrefWidth(300);

            Scene generateRandomGamePopup = new Scene(borderPane);
            randomGameMenu.setScene(generateRandomGamePopup);

            randomGameMenu.showAndWait();

        });

        GenerateRandomGrid(6,4);

        return UIPane;
    }

    private void readFromText(String text) {

        String[] string = text.split("\\n");

        int count = 0;
        ArrayList<String> usedBoxes = new ArrayList<>();
        boolean errorDetected = false;
        String[] resultAndBoxes;

        // search for errors
        for (String strings : string) {

            if (strings.contains(" ")) {
                resultAndBoxes = strings.split(" ");
            } else {
                errorDetected = true;
                break;
            }

            String[] stringBoxes;
            if (resultAndBoxes[1].contains(",")) {
                stringBoxes = (resultAndBoxes[1].split(","));
            } else {
                stringBoxes = resultAndBoxes[1].split("");
            }
            count += stringBoxes.length;

            errorDetected = checkInputForErrors(stringBoxes, usedBoxes, errorDetected);
        }

        // If no errors, create the grid
        if (!errorDetected) {

            Mathdoku.Main.NewGrid(count);

            for (String strings : string) {

                resultAndBoxes = strings.split(" ");
                generateCage(resultAndBoxes);

            }
        } else {

            displayFileError();

        }
    }

    /**
     * Check that each line of an input does not contain any errors, and the entire grid has no errors as a whole
     *
     * @param stringBoxes   IDs of boxes given on the current line
     * @param usedBoxes     Arraylist of boxes already used to check the same box isn't in multiple cages
     * @param errorDetected boolean for if an error was found
     * @return errorDetected
     */
    private boolean checkInputForErrors(String[] stringBoxes, ArrayList<String> usedBoxes, boolean errorDetected) {

        // Check no boxes are used multiple times
        if (stringBoxes.length == 0)
            errorDetected = true;

        for (String stringBox : stringBoxes) {

            if (!usedBoxes.contains(stringBox)) {

                usedBoxes.add(stringBox);

            } else {

                errorDetected = true;
                break;
            }
        }

        // Check the boxes are adjacent
        ArrayList<Integer> boxIDArrayList = new ArrayList<>();
        for (String boxID : stringBoxes) {
            boxIDArrayList.add(Integer.parseInt(boxID));
        }

        for (int boxID : boxIDArrayList) {

            if (!(boxIDArrayList.contains(boxID + 1) || (boxIDArrayList.contains(boxID - 1))
                    || (boxIDArrayList.contains(boxID + Mathdoku.getN())) || (boxIDArrayList.contains(boxID - Mathdoku.getN())))) {

                errorDetected = true;
                break;
            }
        }
        return errorDetected;
    }

    /**
     * Display an error message when the input has an error
     */
    private void displayFileError() {

        Alert error = new Alert(Alert.AlertType.INFORMATION, "Please check the file/text and try again");

        error.setTitle("Error detected");
        error.setHeaderText("An error was detected in the file/text");

        error.showAndWait();

    }

    /**
     * generate a  new cage based on input
     *
     * @param resultAndBoxes IDs of boxes to create a cage with
     */
    private void generateCage(String[] resultAndBoxes) {

        int value = Integer.parseInt(resultAndBoxes[0].replaceAll("\\D+", ""));
        String operator;
        if (resultAndBoxes[0].contains("+"))
            operator = "+";
        else if (resultAndBoxes[0].contains("-"))
            operator = "-";
        else if (resultAndBoxes[0].contains("x"))
            operator = "x";
        else if (resultAndBoxes[0].contains("÷"))
            operator = "÷";
        else
            operator = "";

        String[] stringBoxes = (resultAndBoxes[1].split(","));
        ArrayList<Integer> boxes = new ArrayList<>();

        for (String string : stringBoxes) {

            boxes.add(Integer.parseInt(string));

        }

        new Grid.Cage(operator, value, boxes);

    }

    static void SetUndo(Boolean value) {

        undo.setDisable(value);

    }

    static void SetRedo(Boolean value) {

        redo.setDisable(value);

    }

    private static void GenerateRandomGrid(int gridSize,int difficulty) {

        Mathdoku.setN(gridSize);

        Random random = new Random();
        int N = Mathdoku.getN();

        // Create a new grid
        Mathdoku.Main.NewGrid(N * N);

        // create a 2D array to represent the grid to make randomising simpler
        int[][] grid = new int[N + 1][N + 1];
        ArrayList<Grid.Box> Boxes = Grid.getAllBoxes();

        for (Grid.Box box : Boxes) {

            int X = (box.getID() - 1) / N;
            int Y = (box.getID() - 1) % N;

            int value = ((X + Y) % N) + 1;

            grid[X][Y] = value;

        }

        // Shuffle rows
        int numOfShuffles = random.nextInt(5) + 5;

        for (int i = 0; i < numOfShuffles; i++) {

            int row1 = random.nextInt(N);
            int row2 = random.nextInt(N);

            while (row1 == row2) {
                row2 = random.nextInt(N);
            }

            System.arraycopy(grid[row1], 0, grid[N], 0, grid.length);
            System.arraycopy(grid[row2], 0, grid[row1], 0, grid.length);
            System.arraycopy(grid[N], 0, grid[row2], 0, grid.length);

        }

        // Shuffle columns
        numOfShuffles = random.nextInt(5) + 5;

        for (int i = 0; i < numOfShuffles; i++) {

            int col1 = random.nextInt(N);
            int col2 = random.nextInt(N);

            while (col1 == col2) {
                col2 = random.nextInt(N);
            }

            for (int j = 0; j < grid.length; j++) {

                grid[j][N] = grid[j][col1];
                grid[j][col1] = grid[j][col2];
                grid[j][col2] = grid[j][N];

            }

        }

        // Set the box values to the shuffled array so cages can be made
        ArrayList<Grid.Box> boxes = Grid.getAllBoxes();

        for (int i = 0; i < boxes.size(); i++) {

            int X = i / N;
            int Y = i % N;
            boxes.get(i).setMainText(String.valueOf(grid[X][Y]));

        }

        // Create cages from randomised boxes
        ArrayList<Integer> usedBoxesForCage = new ArrayList<>();

        for (Grid.Box box : boxes) {

            if (!(usedBoxesForCage.contains(box.getID()))) {

                ArrayList<Integer> currentCageArray = new ArrayList<>();
                int cageSize = random.nextInt(difficulty - 1) + 1;

                usedBoxesForCage.add(box.getID());
                currentCageArray.add(box.getID());

                Grid.Box currentBox = box;
                Grid.Box nextBox = currentBox;

                for (int i = 0; i < cageSize; i++) {

                    int escapeCounter = 0;

                    while (nextBox == currentBox) {

                        int num = random.nextInt(4);

                        switch (num) {

                            case 0:
                                if ((currentBox.getID() + 1) < N * N + 1 && (currentBox.getID() - 1) / N == (currentBox.getID() + 1 - 1) / N && !usedBoxesForCage.contains(currentBox.getID() + 1)) {
                                    nextBox = Grid.getBox(currentBox.getID() + 1);
                                    break;
                                }
                            case 1:
                                if ((currentBox.getID() + N) < N * N + 1 && !usedBoxesForCage.contains(currentBox.getID() + N)) {
                                    nextBox = Grid.getBox(currentBox.getID() + N);
                                    break;
                                }
                            case 2:
                                if ((currentBox.getID() - 1) > 0 && (currentBox.getID() - 1 - 1) / N == (currentBox.getID() - 1) / N && !usedBoxesForCage.contains(currentBox.getID() - 1)) {
                                    nextBox = Grid.getBox(currentBox.getID() - 1);
                                    break;
                                }
                            case 3:
                                if ((currentBox.getID() - N) > 0 && !usedBoxesForCage.contains(currentBox.getID() - N)) {
                                    nextBox = Grid.getBox(currentBox.getID() - N);
                                    break;
                                }
                        }

                        escapeCounter += 1;

                        // escape the loop if all adjacent boxes are already part of a cage
                        if(escapeCounter > 10)
                            break;

                    }

                    // only add to arrays if they dont already contain that box
                    if(!usedBoxesForCage.contains(nextBox.getID()))
                        usedBoxesForCage.add(nextBox.getID());

                    if(!currentCageArray.contains(nextBox.getID()))
                        currentCageArray.add(nextBox.getID());

                    currentBox = nextBox;

                }

                // remove operator for singleton cages
                String operator = getRandomOperator(currentCageArray);

                if(currentCageArray.size() == 1){
                    operator = "";
                }

                // Create the cage
                if (!currentCageArray.isEmpty()) {

                    int result = 0;

                    if(currentCageArray.size() == 1){
                        operator = "";
                        result = Integer.parseInt(Grid.getBox(currentCageArray.get(0)).getMainText().getText());
                    }else{

                        switch (operator) {

                            case "x":
                                result = 1;
                                for (Integer boxID : currentCageArray) {
                                    result = result * Integer.parseInt(Grid.getBox(boxID).getMainText().getText());
                                }
                                break;

                            case "÷":

                                ArrayList<Double> boxValuesDivision = getBoxValuesFromIDArray(currentCageArray);
                                double valueDivision = Collections.max(boxValuesDivision);
                                boxValuesDivision.remove(valueDivision);

                                for(double boxValue: boxValuesDivision){
                                    valueDivision = valueDivision / boxValue;
                                }
                                result = (int) Math.round(valueDivision);
                                break;

                            case "-":

                                ArrayList<Double> boxValuesSubtraction = getBoxValuesFromIDArray(currentCageArray);
                                double valueSubtraction = Collections.max(boxValuesSubtraction);
                                boxValuesSubtraction.remove(valueSubtraction);

                                for(double boxValue: boxValuesSubtraction){
                                    valueSubtraction = valueSubtraction - boxValue;
                                }
                                result = (int) Math.round(valueSubtraction);


                                break;

                            case "+":
                                result = 0;
                                for (Integer boxID : currentCageArray) {
                                    result = result + Integer.parseInt(Grid.getBox(boxID).getMainText().getText());
                                }

                                break;
                        }

                    }

                    new Grid.Cage(operator, result, currentCageArray);
                }

            }

        }

        Grid.Box.clearAllBoxes();

        //Display solution to random puzzle (un-comment to output solution in command line)
//        for (int j = 0; j < grid.length; j++) {
//            grid[j][N] = 0;
//            grid[N][j] = 0;
//        }
//        System.out.println();
//        for (int[] i : grid) {
//            System.out.println(Arrays.toString(i));
//        }

    }

    /**
     * Choose a random operator for the cage
     *
     * @return a string containing the operators symbol
     */
    private static String getRandomOperator(ArrayList<Integer> currentCageArray) {

        Random random = new Random();

        while(true) {
            switch (random.nextInt(4)) {

                case 0:
                    return "x";
                case 1:

                    ArrayList<Double> boxValuesDivision = getBoxValuesFromIDArray(currentCageArray);
                    double valueDivision = Collections.max(boxValuesDivision);
                    boxValuesDivision.remove(valueDivision);

                    for(double boxValue: boxValuesDivision){
                           valueDivision = valueDivision / boxValue;
                    }
                    if (valueDivision == (int)valueDivision){
                        return "÷";
                    }

                case 2:
                    return "+";

                case 3:

                    ArrayList<Double> boxValuesSubtraction = getBoxValuesFromIDArray(currentCageArray);
                    double valueSubtraction = Collections.max(boxValuesSubtraction);
                    boxValuesSubtraction.remove(valueSubtraction);

                    for(double boxValue: boxValuesSubtraction){
                        valueSubtraction = valueSubtraction - boxValue;
                    }
                    if (valueSubtraction > 0){
                        return "-";
                    }
            }
        }

    }

    private static ArrayList<Double> getBoxValuesFromIDArray(ArrayList<Integer> list){

        ArrayList<Double> boxValuesToCheck = new ArrayList<>();
        for (Integer boxID : list) {
            boxValuesToCheck.add(Double.valueOf(Grid.getBox(boxID).getMainText().getText()));
        }
        return boxValuesToCheck;

    }

}
