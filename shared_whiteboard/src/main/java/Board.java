import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.util.List;
import java.util.ArrayList;


public class Board extends JPanel implements MouseListener, MouseMotionListener {


    public enum Mode {
        DRAW, RECTANGLE, CIRCLE, OVAL, LINE, TEXT, ERASER
    }

    private Mode currentMode = Mode.DRAW;

    private Color currentColor = Color.BLACK;
    private BasicStroke currentStroke = new BasicStroke(12);;
    private List<Shape> shapes = new ArrayList<>();
    private List<Color> shapeColors = new ArrayList<>();
    private List<BasicStroke> shapeStrokes = new ArrayList<>();

    private Point dragStart;

    private Point dragEnd;

    public Board() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setCurrentMode(Mode mode) {
        this.currentMode = mode;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    public void setCurrentStroke(BasicStroke currentStroke) {
        this.currentStroke = currentStroke;
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragStart = new Point(e.getX(), e.getY());
        dragEnd = dragStart;
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Shape shape = createShape(e);
        if (shape != null) {
            shapes.add(shape);
            shapeColors.add(currentColor);
            shapeStrokes.add(currentStroke);
        }
        repaint();
    }

    private Shape createShape(MouseEvent e) {
        switch (currentMode) {
            case RECTANGLE:
                return new Rectangle2D.Double(
                        Math.min(dragStart.x, e.getX()),
                        Math.min(dragStart.y, e.getY()),
                        Math.abs(dragStart.x - e.getX()),
                        Math.abs(dragStart.y - e.getY())
                );
            case CIRCLE:
                double diameter = Math.max(Math.abs(dragStart.x - e.getX()), Math.abs(dragStart.y - e.getY()));
                return new Ellipse2D.Double(
                        Math.min(dragStart.x, e.getX()),
                        Math.min(dragStart.y, e.getY()),
                        diameter,
                        diameter
                );
            case OVAL:
                return new Ellipse2D.Double(
                        Math.min(dragStart.x, e.getX()),
                        Math.min(dragStart.y, e.getY()),
                        Math.abs(dragStart.x - e.getX()),
                        Math.abs(dragStart.y - e.getY())
                );
            case LINE:
                return new Line2D.Double(dragStart.x, dragStart.y, e.getX(), e.getY());
            default:
                return null;
        }
    }


    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dragEnd = new Point(e.getX(), e.getY());
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        for (int i = 0; i < shapes.size(); i++) {
            g2d.setColor(shapeColors.get(i));
            g2d.setStroke(shapeStrokes.get(i));
            g2d.draw(shapes.get(i));
        }
        if (dragStart != null && dragEnd != null) {
            g2d.setColor(currentColor);
            g2d.setStroke(currentStroke);
            switch (currentMode) {
                case RECTANGLE:
                    g2d.draw(new Rectangle2D.Double(
                            Math.min(dragStart.x, dragEnd.x),
                            Math.min(dragStart.y, dragEnd.y),
                            Math.abs(dragStart.x - dragEnd.x),
                            Math.abs(dragStart.y - dragEnd.y)
                    ));
                    break;
                case CIRCLE:
                    double diameter = Math.max(Math.abs(dragStart.x - dragEnd.x), Math.abs(dragStart.y - dragEnd.y));
                    g2d.draw(new Ellipse2D.Double(
                            Math.min(dragStart.x, dragEnd.x),
                            Math.min(dragStart.y, dragEnd.y),
                            diameter,
                            diameter
                    ));
                    break;
                case OVAL:
                    g2d.draw(new Ellipse2D.Double(
                            Math.min(dragStart.x, dragEnd.x),
                            Math.min(dragStart.y, dragEnd.y),
                            Math.abs(dragStart.x - dragEnd.x),
                            Math.abs(dragStart.y - dragEnd.y)
                    ));
                    break;
                case LINE:
                    g2d.draw(new Line2D.Double(dragStart.x, dragStart.y, dragEnd.x, dragEnd.y));
                    break;
            }
        }
    }


}
