import org.jgroups.util.Base64;
import org.jgroups.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

abstract class AbstractGridShape extends JComponent {

    /* Position of the shape in the grid */
    public int x = 0 ;
    public int y = 0 ;
    private ExampleDisplay dis;

    public AbstractGridShape(ExampleDisplay display) {
        dis = display ;
        dis.add(this);
        dis.pack(); // somehow needed or add does not work properly
    }

    /*
     * Set the positions of the shape in grid coordinates
     */
    public void setGridPos(int someX,int someY) {
        x = someX ; y = someY ;
    }

    abstract public void drawShape(Graphics g, int x, int y, int w, int h);

    /* delegates drawing proper to drawShape. Transform the grid
     * coordinates of the shape into pixel coordinates, using the cell
     * size of the ExampleDisplay associated with the AbstractGridShape */
    public void paint(Graphics g) {
        this.drawShape(g,
                dis.cellSize/2 + x*dis.cellSize,
                dis.cellSize/2 + y*dis.cellSize,
                dis.cellSize, dis.cellSize);
    }

    public void moveRect(int[] delta) {
        x = (x+delta[0]+dis.gridSize)%dis.gridSize ;
        y = (y+delta[1]+dis.gridSize)%dis.gridSize ;
    }
} // EndClass AbstractGridShape

public class ExampleDisplay extends JFrame implements KeyListener {
    int cellSize = 20 ;
    int gridSize = 20 ;
    int playerId = 0 ;
    Map<Integer,int[]> moveTable = new HashMap<Integer,int[]>() ;
    Rectangle myRectangle = new Rectangle(this) ;
    Container myContainer ;
    int numberOfSweets = 10 ;
    ScoreKeeper scoreKeeper;

    /* gameMap contains the plan of the sweets to collect initialized to
     * null by default */
    Circle[][] gameMap = new Circle[gridSize][gridSize];

    public ExampleDisplay(int playerId, Circle[][] gMap) {
        super();
        this.playerId = playerId;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocation(30, 30);
        myContainer = getContentPane();
        myContainer.setPreferredSize(new Dimension(cellSize * (gridSize + 1), cellSize * (gridSize + 1) ));
        pack();
        scoreKeeper = new ScoreKeeper(1);

        // adding the red circles for a bit of landscape
        Random rand = new Random();

        if(gMap == null) {
            for (int i = 0; i < numberOfSweets; i++) {
                int j, k;
                do {
                    j = rand.nextInt(gridSize);
                    k = rand.nextInt(gridSize);
                } while (gameMap[j][k] != null);

                gameMap[j][k] = new Circle(this);
                gameMap[j][k].setGridPos(j, k);
            }
        } else {
            gameMap = gMap;
        }

        setVisible(true);

        moveTable.put(KeyEvent.VK_DOWN ,new int[] { 0,+1});
        moveTable.put(KeyEvent.VK_UP   ,new int[] { 0,-1});
        moveTable.put(KeyEvent.VK_LEFT ,new int[] {-1, 0});
        moveTable.put(KeyEvent.VK_RIGHT,new int[] {+1, 0});
        addKeyListener(this);

    } // EndConstructor ExampleDisplay

    public void getState(org.jgroups.util.Base64.OutputStream output, int x, int y) throws Exception {

        int[] sweetLocation = new int[2];
        sweetLocation[0] = x;
        sweetLocation[1] = y;

        Util.objectToStream(sweetLocation, new DataOutputStream(output));
    }

    public void setState(Base64.InputStream input) throws Exception {

        int[] sweetLocation = (int[])Util.objectFromStream(new DataInputStream(input));

        gameMap[sweetLocation[0]][sweetLocation[1]]=null;
    }


    /* needed to implement KeyListener */
    public void keyTyped   (KeyEvent ke){}
    public void keyReleased(KeyEvent ke){}

    /* where the real work happens: reacting to key being pressed */
    public void keyPressed (KeyEvent ke){
        int keyCode = ke.getKeyCode();
        if (!moveTable.containsKey(keyCode)) return ;
        myRectangle.moveRect(moveTable.get(keyCode));
        if (gameMap[myRectangle.x][myRectangle.y]!=null) {
            Circle c = gameMap[myRectangle.x][myRectangle.y];
            myContainer.remove(c);
            scoreKeeper.AddOneToScore(playerId);

            // System.out.println(scoreKeeper.getScoreList()[0]);
            pack();
            gameMap[myRectangle.x][myRectangle.y]=null;
            numberOfSweets--;
            if (numberOfSweets==0) {
                int[] victor = scoreKeeper.DeclareVictor();

                System.out.println("Congratulations player " + victor[0] + " ! You won with a score of " + victor[1] + " !");
                System.exit(0);
            }
            System.out.println("Only "+numberOfSweets+" sweet(s) remaining...");
        }
        repaint();
    } // EndMethod keyPressed

} // EndClass ExampleDisplay
