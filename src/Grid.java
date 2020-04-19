import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Stack;

/**
 * Create all things related to the grid such as boxes and cages and define their functionality
 */
class Grid {

    private static Box selectedBox;
    private static GridPane gameAreaPane;
    private int n = Mathdoku.getN();
    private static ArrayList<Box> allBoxes = new ArrayList<>();
    private static ArrayList<Cage> allCages = new ArrayList<>();
    private static Stack<StackObject> undo = new Stack<>();
    private static Stack<StackObject> redo = new Stack<>();

    static ArrayList<Box> getAllBoxes() {
        return allBoxes;
    }

    /**
     * Create the play area
     *
     * @return the game grid
     */
    Parent createGrid() {

        gameAreaPane = new GridPane();
        // create the grid and apply padding to it
        gameAreaPane.setPadding(new Insets(25, 25, 25, 40));
        gameAreaPane.setVgap(0);
        gameAreaPane.setHgap(0);
        gameAreaPane.setGridLinesVisible(true);

        // Apply column and row constraints to allow scalability
        for (int i = 0; i < n; i++) {

            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100 / n);
            col.setPrefWidth(50);
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100 / n);
            row.setPrefHeight(50);

            gameAreaPane.getColumnConstraints().add(col);
            gameAreaPane.getRowConstraints().add(row);

        }

        allBoxes.clear();

        // Create a box for each square of the grid
        for (int i = 0; i < n; i++) {

            for (int j = 0; j < n; j++) {

                Box box = new Box(i, j, n);
                gameAreaPane.add(box, j, i);
                allBoxes.add(box);

            }
        }

        selectedBox = getBox(1);

        return gameAreaPane;
    }

    private static GridPane getGameAreaPane() {
        return gameAreaPane;
    }

    /**
     * creates a box object in each square of the grid pane with grid coordinates
     * Locating which box is selected on mouse click
     */
    static class Box extends Pane {

        private int ID;
        private Text mainText = new Text();
        private Text cageText = new Text();
        private String previousStyle = "-fx-background-color: transparent;";

        Box(int x, int y, int n) {

            ID = y + 1 + n * x;

            mainText.setFont(Font.font(20));
            mainText.setTranslateY(this.getHeight() / 2 + 20);
            mainText.setTranslateX(this.getWidth() / 2 + 20);
            this.getChildren().add(mainText);

            this.setMinSize(40, 40);

            // On mouse click select the given box, deselect the previous box
            setOnMouseClicked(e -> {

                selectedBox.setStyle(previousStyle);
                this.previousStyle = this.getStyle();
                selectedBox = this;
                this.setStyle("-fx-background-color: Gainsboro;");

            });

        }

        static void setFirstSelectedBox(){
            selectedBox.previousStyle = selectedBox.getStyle();
            selectedBox.setStyle("-fx-background-color: Gainsboro;");
        }

        /**
         * Set the main text for a given box
         *
         * @param mainText text to set variable to
         */
        void setMainText(String mainText) {
            this.mainText.setText(mainText);
        }

        /**
         * Get ID of a box
         *
         * @return ID
         */
        int getID() {
            return ID;
        }

        /**
         * Set the text in the top corner of a box
         *
         * @param string to set as the top left text
         */
        void SetBoxTopLeftText(String string) {

            // Create text in the top left corner of the box
            cageText = new Text();
            cageText.setText(string);
            cageText.setX(5);
            cageText.setY(15);
            this.getChildren().add(cageText);

        }

        /**
         * Set the main text of the currently selected box
         *
         * @param text to put in the box
         */
        static void setSelectedBoxMainText(String text) {

            if (CheckTextValid(text)) {
                selectedBox.mainText.setText(text);
                selectedBox.mainText.setTranslateX(selectedBox.getWidth() / 2 - 5);
                selectedBox.mainText.setTranslateY(selectedBox.getHeight() / 2 + 10);
            }
        }

        static void setSelectedBoxFromKeyboard(String code) {

            int n = Mathdoku.getN();

            switch (code) {
                case "W":
                    if(selectedBox.getID() > n) {
                        selectedBox.setStyle("-fx-background-color: transparent;");
                        selectedBox = getBox(selectedBox.getID() - n);
                        selectedBox.previousStyle = selectedBox.getStyle();
                        selectedBox.setStyle("-fx-background-color: Gainsboro;");
                    }
                    break;
                case "A":
                    if(!(selectedBox.getID() % Mathdoku.getN() == 1)) {
                        selectedBox.setStyle("-fx-background-color: transparent;");
                        selectedBox = getBox(selectedBox.getID() - 1);
                        selectedBox.previousStyle = selectedBox.getStyle();
                        selectedBox.setStyle("-fx-background-color: Gainsboro;");
                    }
                    break;
                case "S":
                    if(selectedBox.getID() + n < n * n + 1){
                        selectedBox.setStyle("-fx-background-color: transparent;");
                        selectedBox = getBox(selectedBox.getID() + n);
                        selectedBox.previousStyle = selectedBox.getStyle();
                        selectedBox.setStyle("-fx-background-color: Gainsboro;");
                    }
                    break;
                case "D":
                    if(!(selectedBox.getID() % n == 0)) {
                        selectedBox.setStyle("-fx-background-color: transparent;");
                        selectedBox = getBox(selectedBox.getID() + 1);
                        selectedBox.previousStyle = selectedBox.getStyle();
                        selectedBox.setStyle("-fx-background-color: Gainsboro;");
                    }
            }
        }

        /**
         * Get a boxes main text
         *
         * @return string of the boxes text
         */
        Text getMainText() {
            return mainText;
        }

        /**
         * Check that the keyboard input is either a valid number or a backspace
         */
        static boolean CheckTextValid(String text) {

            try {
                Double.parseDouble(text);
                return Double.parseDouble(text) <= Mathdoku.getN() && !(Double.parseDouble(text) == 0);
            } catch (NumberFormatException e) {
                return text.equals("");
            }
        }

        /**
         * reposition the text in boxes when the grid is resized
         * Called when adding text to box avoid code duplication
         */
        static void updateAllBoxTextPositions() {

            for (Box box : Grid.getAllBoxes()) {
                box.mainText.setTranslateX(box.getWidth() / 2 - 8);
                box.mainText.setTranslateY(box.getHeight() / 2 + 10);
            }

        }

        /**
         * Clears every box in the grid
         */
        static void clearAllBoxes() {

            for (Box box : getAllBoxes()) {

                box.mainText.setText("");
                box.setStyle("-fx-background-color: Transparent");
                TopUI.SetRedo(true);
                TopUI.SetUndo(true);

            }
        }

        /**
         * Check the current box values for errors and highlight them accordingly
         * called every time a box is changed or show errors checkbox is changed
         * only runs if show errors is true
         */
        static boolean CheckForErrors() {

            // Array lists to store which columns/rows must be highlighted
            ArrayList<Integer> errorRows = new ArrayList<>();
            ArrayList<Integer> errorColumns = new ArrayList<>();

            int n = Mathdoku.getN();
            boolean error = false;

            // Compare each box to every other box
            for (Box box : Grid.getAllBoxes()) {

                for (Box otherBox : Grid.getAllBoxes()) {

                    // Dont check each box against itself
                    if (!(box == otherBox)) {
                        // if the text is the same and the box is not empty
                        if (box.getMainText().getText().equals(otherBox.getMainText().getText()) && !(box.getMainText().getText().equals(""))) {

                            // Check for row errors, if there are any add that row to errorRows arraylist
                            if ((box.getID() - 1) / n == (otherBox.getID() - 1) / n) {

                                errorRows.add((box.getID() - 1) / n);
                                error = true;
                                break;

                            }

                            // Check for columns errors, if there are any add that column to errorColumn arraylist
                            if (otherBox.getID() % n == box.getID() % n) {

                                errorColumns.add(box.getID() % n);
                                error = true;
                                break;

                            }

                            // if the boxes are not matching set the text to black
                        } else {
                            box.getMainText().setStroke(Color.BLACK);
                        }
                    }
                }
            }
            // Set the text colour to red of all boxes in the columns/rows stored the errorColumns/errorRows
            for (Box box : Grid.getAllBoxes()) {
                if (errorColumns.contains(box.getID() % n) || errorRows.contains((box.getID() - 1) / n)) {
                    if (TopUI.getShowErrors()) {
                        box.getMainText().setStroke(Color.RED);
                    } else {
                        box.getMainText().setStroke(Color.BLACK);
                    }
                }
            }

            // Check for errors in cages
            for (Cage cage : allCages) {

                boolean fullCage = true;
                for (Box box : cage.getBoxes()) {
                    if (box.getMainText().getText().equals("")) {
                        fullCage = false;
                        break;
                    }
                }

                if (fullCage) {

                    // Cages that are addition
                    switch (cage.getOperator()) {
                        case "+":

                            int sum = 0;
                            for (Box box : cage.getBoxes()) {
                                if (!(box.getMainText().getText().equals(""))) {
                                    sum += Integer.parseInt(box.getMainText().getText());
                                }
                            }
                            error = isError(error, cage, sum);

                            break;
                        // Cages that are multiplication
                        case "x":
                            int product = 1;
                            for (Box box : cage.getBoxes()) {
                                if (!(box.getMainText().getText().equals(""))) {

                                    product = product * Integer.parseInt(box.getMainText().getText());
                                }
                            }
                            error = isError(error, cage, product);

                            break;
                        // cages that are subtraction or division
                        case "-":
                        case "÷":

                            ArrayList<Box> tempBoxes = cage.getBoxes();

                            for (int i = 0; i < tempBoxes.size() - 1; i++) {

                                Box b = tempBoxes.get(i);
                                Box next = tempBoxes.get(i + 1);
                                if (Integer.parseInt(b.getMainText().getText()) < Integer.parseInt(next.getMainText().getText())) {
                                    Collections.swap(tempBoxes, i, i + 1);
                                }
                            }

                            int result = Integer.parseInt(tempBoxes.get(0).getMainText().getText());

                            for (int i = 1; i < tempBoxes.size(); i++) {

                                if (cage.getOperator().equals("-")) {

                                    result -= Integer.parseInt(tempBoxes.get(i).getMainText().getText());

                                } else {

                                    result /= Integer.parseInt(tempBoxes.get(i).getMainText().getText());

                                }
                            }
                            error = isError(error, cage, result);

                            break;
                        case "":

                            error = isError(error, cage, Integer.parseInt(cage.getBoxes().get(0).getMainText().getText()));
                            break;
                    }
                }
            }
            return error;
        }

        /**
         * Check if there is an error for any cage
         *
         * @param error  boolean value as to whether an error has been detected yet
         * @param cage   that the check is being done on
         * @param result value obtained from performing the operation on the cages contents
         * @return true if there is an error in a cage
         */
        private static boolean isError(boolean error, Cage cage, int result) {
            if (!(result == cage.getResult())) {

                error = true;

                for (Box box : cage.getBoxes()) {
                    if (TopUI.getShowErrors()) {
                        box.getMainText().setStroke(Color.RED);
                    } else {
                        box.getMainText().setStroke(Color.BLACK);
                    }
                }
            }
            return error;
        }

        /**
         * Check that all boxes are filled in order to detect when the grid is correct
         *
         * @return true if all are filled
         */
        static boolean CheckAllBoxesFilled() {

            for (Box box : Grid.getAllBoxes()) {
                if (box.getMainText().getText().equals("")) {
                    return false;
                }
            }

            return true;

        }

        /**
         * Push the contents of a box to the stack before it changes so it can be reverted to
         */
        static void PushUndo() {

            StackObject stackObject = new StackObject(selectedBox, selectedBox.getMainText().getText());
            undo.push(stackObject);
            TopUI.SetUndo(false);

        }

        /**
         * Pop a value from the undo stack, push that value to the redo stack
         */
        static void PopUndo() {

            if (!undo.empty()) {

                StackObject stackObject = undo.pop();

                StackObject redoStackObject = new StackObject(stackObject.getBox(), stackObject.getBox().getMainText().getText());
                redo.push(redoStackObject);

                stackObject.getBox().getMainText().setText(stackObject.getText());

                TopUI.SetRedo(false);

            }

            if (undo.empty()) {
                TopUI.SetUndo(true);
            }
        }

        /**
         * Pop from the redo stack, push that value to the undo stack
         */
        static void PopRedo() {

            if (!redo.empty()) {

                StackObject stackObject = redo.pop();

                StackObject undoStackObject = new StackObject(stackObject.getBox(), stackObject.getBox().getMainText().getText());
                undo.push(undoStackObject);

                stackObject.getBox().getMainText().setText(stackObject.getText());

                TopUI.SetUndo(false);

            }

            if (redo.empty()) {
                TopUI.SetRedo(true);
            }
        }

        /**
         * clear both stacks
         */
        static void ClearStacks() {

            undo.clear();
            redo.clear();

        }

        /**
         * Uppdate the size of all text in the boxes according to the size selected
         *
         * @param fontSize small, medium or large (selected in choice box)
         */
        static void updateFontSizes(String fontSize) {

            for (Box box : allBoxes) {

                switch (fontSize) {
                    case "Small":
                        box.getMainText().setFont(new Font(10));
                        box.getMainText().setX(box.getPrefWidth() / 2 + 1);
                        box.cageText.setFont(new Font(8));
                        box.cageText.setY(11);
                        break;
                    case "Medium":
                        box.getMainText().setFont(new Font(15));
                        box.getMainText().setX(box.getPrefWidth() / 2);
                        box.cageText.setFont(new Font(10));
                        box.cageText.setY(15);

                        break;
                    case "Large":
                        box.getMainText().setFont(new Font(25));
                        box.getMainText().setX(box.getPrefWidth() / 2 - 2);
                        box.cageText.setFont(new Font(15));
                        box.cageText.setY(17);
                        break;
                }
            }
        }

        /**
         * Play the animation when the grid is correctly filled in
         */
        static void playWinAnimation() {

            ArrayList<Box> Boxes = new ArrayList<>(getAllBoxes());

            final Timer t = new Timer(125, null);
            t.addActionListener(new ActionListener() {

                int i = 0;

                @Override
                public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

                    Boxes.get(i).setStyle("-fx-background-color: rgba(0,154,0,0.70);");
                    Boxes.get(i).previousStyle = "-fx-background-color: rgba(0,154,0,0.70);";

                    if (i + 1 < Boxes.size()) {
                        Boxes.get(i + 1).setStyle("-fx-background-color: rgba(255,123,0,0.7);");
                        Boxes.get(i + 1).previousStyle = "-fx-background-color: rgba(255,123,0,0.7);";
                    }

                    if (i == Boxes.size() - 1) {

                        Platform.runLater(() -> {

                            Alert CompletionAlert = new Alert(Alert.AlertType.INFORMATION, "You completed the puzzle");
                            CompletionAlert.setTitle("Congratulations!");
                            CompletionAlert.setHeaderText("Congratulations!");

                            Optional<ButtonType> result = CompletionAlert.showAndWait();

                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                CompletionAlert.close();
                            }
                        });

                    }

                    if (i > Boxes.size() - 2) {
                        t.stop();
                    }
                    i++;
                }

            });
            t.setRepeats(true);
            t.start();

        }

    }

    /**
     * Cage is a collection of boxes in the grid
     */
    static class Cage {

        private ArrayList<Box> boxes = new ArrayList<>();
        private String operator;
        private int result;

        /**
         * Create a cage
         *
         * @param operator ( +, -, x, ÷)
         * @param result   number that the values in each cage must be when applied to the operator
         * @param boxIDs   Array list of Integers (box IDs) that are in the cage
         */
        Cage(String operator, int result, ArrayList<Integer> boxIDs) {

            boxIDs.forEach(box -> boxes.add(Grid.getBox(box)));
            allCages.add(this);
            this.operator = operator;
            this.result = result;
            DrawCage(this, operator, result);

        }

        String getOperator() {
            return operator;
        }

        int getResult() {
            return result;
        }

        ArrayList<Box> getBoxes() {
            return boxes;
        }
    }

    /**
     * get the box object that is in a given ID value of the grid
     *
     * @param ID of the grid square containing the box
     * @return box object in the given grid square
     */
    static Box getBox(int ID) {

        int n = Mathdoku.getN();

        int X = ID / n;
        int Y = ID % n;

        return (Box) getGameAreaPane().getChildren().get(Y + n * X);

    }

    /**
     * Draws a given cage and outlines it appropriately
     *
     * @param cage     to draw
     * @param operator to write in the top left
     * @param result   to write in the top left
     */
    private static void DrawCage(Cage cage, String operator, int result) {

        int n = Mathdoku.getN();
        Box lowestIDBox = new Box(20, 20, n);

        for (Box box : cage.getBoxes()) {

            BorderStrokeStyle right = BorderStrokeStyle.SOLID;
            BorderStrokeStyle left = BorderStrokeStyle.SOLID;
            BorderStrokeStyle top = BorderStrokeStyle.SOLID;
            BorderStrokeStyle bottom = BorderStrokeStyle.SOLID;

            for (Box otherBox : cage.getBoxes()) {

                if (!(box.getID() == otherBox.getID())) {

                    // if box to the right is in cage
                    if (box.getID() + 1 == otherBox.getID()) {
                        if (!(box.getID() % n == 0)) {
                            right = BorderStrokeStyle.NONE;
                        }
                    }

                    // if the box to the left is in cage
                    if (box.getID() - 1 == otherBox.getID()) {
                        if (!(otherBox.getID() % n == 0)) {
                            left = BorderStrokeStyle.NONE;
                        }
                    }

                    // if the box above is in the cage
                    if (box.getID() - n == otherBox.getID()) {
                        top = BorderStrokeStyle.NONE;
                    }

                    // if the box is below is in the cage
                    if (box.getID() + n == otherBox.getID()) {
                        bottom = BorderStrokeStyle.NONE;
                    }

                }

                if (box.getID() < lowestIDBox.getID()) {
                    lowestIDBox = box;
                }

            }

            box.setBorder(new Border(new BorderStroke(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
                    top, right, bottom, left,
                    CornerRadii.EMPTY, new BorderWidths(2), Insets.EMPTY)));

        }

        lowestIDBox.SetBoxTopLeftText(result + operator);

    }
}