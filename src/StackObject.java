class StackObject {

    private Grid.Box box;
    private String text;

    StackObject(Grid.Box box, String text) {
        this.box = box;
        this.text = text;
    }

    Grid.Box getBox() {
        return box;
    }

    String getText() {
        return text;
    }
}
