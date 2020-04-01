import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

class BottomUI extends Node {

    private int n = Mathdoku.getN();

    /**
     * Creating buttons at the bottom of the UI for entering numbers
     *
     * @return bottom ui
     */
    Parent createBottomUI() {

        FlowPane UIPane = new FlowPane(Orientation.HORIZONTAL);
        UIPane.setAlignment(Pos.CENTER);
        UIPane.setHgap(10);
        UIPane.setVgap(5);
        UIPane.setPadding(new Insets(10));

        for (int i = 1; i < n + 1; i++) {

            Button button = new Button(Integer.toString(i));
            UIPane.getChildren().add(button);

            button.setOnMouseClicked(e -> {
                Grid.Box.setSelectedBoxMainText(button.getText());
                Grid.Box.CheckForErrors();
            });
        }

        Button delete = new Button("del");
        Text text = new Text();

        delete.setOnMouseClicked(e -> Grid.Box.setSelectedBoxMainText(""));

        UIPane.getChildren().addAll(delete,text);

        return UIPane;
    }


}
