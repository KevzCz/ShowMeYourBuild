package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

public class StatGroupColumnAssigner {
    private int leftHeight = 0;
    private int rightHeight = 0;

    public int assignColumn(StatGroup group) {
        int height = 1 + group.getChildren().size();

        if (leftHeight <= rightHeight) {
            leftHeight += height;
            return 0;
        } else {
            rightHeight += height;
            return 1;
        }
    }
}
