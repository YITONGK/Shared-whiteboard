import java.awt.*;

public interface DrawingListener {
    void shapeDrawn(Shape shape, Color color, float stroke);
    void textDrawn(String text, int x, int y, Color color, float stroke);
    void clearBoard();
}
