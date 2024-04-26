import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


public class Board extends JPanel implements MouseListener, MouseMotionListener {

    public void consoleLog(String s) {
        System.out.println(s);
    }

    public enum Mode {
        RECTANGLE, CIRCLE, OVAL, LINE, DRAW, TEXT, ERASER
    }

    private List<DrawingListener> drawingListeners = new ArrayList<>();

    private Mode currentMode = Mode.DRAW;

    private Color currentColor = Color.BLACK;
    private BasicStroke currentStroke = new BasicStroke(12);;
    public List<Shape> shapes = new ArrayList<>();
    public List<Color> shapeColors = new ArrayList<>();
    public List<BasicStroke> shapeStrokes = new ArrayList<>();

    private Point dragStart;

    private Point dragEnd;
    private BufferedImage background;

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
                Font textFont = new Font("Ariel", Font.PLAIN, 12).deriveFont((float) fontSize);
                FontMetrics metrics = getFontMetrics(textFont);
                int x = e.getX();
                int y = e.getY() - metrics.getHeight() / 2 + metrics.getAscent();  // Adjust to center text vertically around the click point

                TextShape textShape = new TextShape(text, x, y, textFont);
                shapes.add(textShape);
                shapeColors.add(currentColor);
                shapeStrokes.add(currentStroke);  // This might not be necessary unless you want to keep track of strokes for text for some reason
                notifyTextDrawn(text, x, y, currentColor, currentStroke.getLineWidth());
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
            notifyShapeDrawn(shape, currentColor, currentStroke.getLineWidth());
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
                    notifyShapeDrawn(line, Color.WHITE, currentStroke.getLineWidth());
                } else {
                    shapeColors.add(currentColor); // Regular drawing color
                    shapeStrokes.add(currentStroke); // Regular stroke
                    notifyShapeDrawn(line, currentColor, currentStroke.getLineWidth());
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
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) g;
        if (background != null) {
            g2d.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
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

    public void clearBoard() {
        shapes.clear();
        shapeColors.clear();
        shapeStrokes.clear();
        dragStart = null;
        dragEnd = null;
        background =null;
        setBackground(Color.WHITE);
        repaint();

    }

    public void saveAsPng(File file) throws IOException {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        this.printAll(g2d);
        g2d.dispose();
        ImageIO.write(image, "PNG", file);
    }

    public void setBackgroundImage(File file) throws IOException {
        background = ImageIO.read(file);
        repaint();
    }

    public void addDrawingListener(DrawingListener drawingListener) {
        drawingListeners.add(drawingListener);
    }

    private void notifyShapeDrawn(Shape shape, Color color, float stroke) {
        for (DrawingListener drawingListener : drawingListeners) {
            drawingListener.shapeDrawn(shape, color, stroke);
        }
    }

    private void notifyTextDrawn(String text, int x, int y, Color color, float stroke) {
        for (DrawingListener drawingListener : drawingListeners) {
            drawingListener.textDrawn(text, x, y, color, stroke);
        }
    }


}
