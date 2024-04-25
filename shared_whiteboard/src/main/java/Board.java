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

    private Boolean flag = true;
    public void consoleLog(String s) {
        if (flag) {
            System.out.println(s);
        }
    }

    public enum Mode {
        RECTANGLE, CIRCLE, OVAL, LINE, DRAW, TEXT, ERASER
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
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
        if (currentMode == Mode.TEXT) {
            String text = JOptionPane.showInputDialog(this, "Enter Text:", "Text Input", JOptionPane.PLAIN_MESSAGE);
            if (text != null && !text.isEmpty()) {
                // Calculate font size based on stroke width
                int fontSize = (int) currentStroke.getLineWidth() * 4;  // Example scaling factor
                Font textFont = getFont().deriveFont((float) fontSize);
                FontMetrics metrics = getFontMetrics(textFont);
                int x = e.getX();
                int y = e.getY() - metrics.getHeight() / 2 + metrics.getAscent();  // Adjust to center text vertically around the click point

                TextShape textShape = new TextShape(text, x, y, textFont);
                shapes.add(textShape);
                shapeColors.add(currentColor);
                shapeStrokes.add(currentStroke);  // This might not be necessary unless you want to keep track of strokes for text for some reason
                repaint();
            }
        }
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
        Point newPoint = new Point(e.getX(), e.getY());
        if (currentMode == Mode.DRAW || currentMode == Mode.ERASER) {
            if (dragEnd != null) {
                Shape line = new Line2D.Float(dragEnd, newPoint);
                shapes.add(line);
                // Use the background color for eraser mode
                if (currentMode == Mode.ERASER) {
                    shapeColors.add(Color.WHITE);  // Assuming the background is white
                    shapeStrokes.add(new BasicStroke(currentStroke.getLineWidth(), // Keep the line width
                            BasicStroke.CAP_ROUND,  // Round caps for a smoother erase
                            BasicStroke.JOIN_ROUND)); // Round joins for smoother erase
                } else {
                    shapeColors.add(currentColor); // Regular drawing color
                    shapeStrokes.add(currentStroke); // Regular stroke
                }
            }
            dragEnd = newPoint; // Update dragEnd to the new point
        } else {
            dragEnd = new Point(e.getX(), e.getY());
        }

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
            Shape shape = shapes.get(i);
            if (shape instanceof TextShape) {
                TextShape textShape = (TextShape) shape;
                g2d.setFont(textShape.getFont());
                g2d.drawString(textShape.getText(), textShape.getX(), textShape.getY());
            } else {
                g2d.draw(shape);
            }
        }
        if (dragStart != null && dragEnd != null && currentMode != Mode.DRAW) {
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
                case DRAW:

            }
        }
    }


}
