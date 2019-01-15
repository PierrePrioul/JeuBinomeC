import java.awt.*;

class Rectangle extends AbstractGridShape {
  public Rectangle(ExampleDisplay display) {
    super(display);
  }
  public void drawShape(Graphics g, int x, int y, int w, int h) {
    g.setColor(Color.BLUE);
    g.fillRect(x,y,w,h);
  }
} // EndClass Rectangle
