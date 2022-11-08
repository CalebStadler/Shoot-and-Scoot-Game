import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class Game extends JFrame
{
    //gamePanel and bia for the grid
    private GamePanel gamePanel;
    private BufferedImage[] bia;
    
    //grid used in creation for the game area
    private int gridSize=18;
    private int cellSize=50;
    private int[][] gameGrid;

    //variables needed for the player
    private BufferedImage playerBI;
    private int playerX = (gridSize*cellSize)/2;
    private int playerY = (gridSize*cellSize)/2; 
    private boolean playerMoving = false;
    private boolean playerUp = false;
    private boolean playerDown = false;
    private boolean playerLeft = false;
    private boolean playerRight = false;
    private int lastDir = 0;

    private Toolkit toolkit;

    //static constants for the game area
    private static final int NONE=0;
    private static final int WATER=2;
    private static final int PATH=3;

    //variables needed for the projectiles
    private BufferedImage[] projectileBIA;
    private ArrayList<Projectile> projectileList;
    private boolean shooting = false;
    private static final int SMALL=1;

    public Game()
    {
        super("Game");

        KeyHandler kh = new KeyHandler();
        toolkit=getToolkit();

        //gamePanel
        gamePanel = new GamePanel();
        gamePanel.addKeyListener(kh);
        gamePanel.setFocusable(true);
        gamePanel.setPreferredSize(new Dimension (gridSize*cellSize,gridSize*cellSize));
        add(gamePanel);

        //creation of game grid and buffered image array
        gameGrid=new int[gridSize][gridSize];
        bia = new BufferedImage[4]; 
        
        //creation of projectile list and buffered image array
        projectileList = new ArrayList<Projectile>();
        projectileBIA = new BufferedImage[4];
        
        //take in all of the files needed for the game
        try
        {
            playerBI = ImageIO.read(new File("player.png"));

            projectileBIA[SMALL] = ImageIO.read(new File("smallBall.png"));

            bia[WATER]=ImageIO.read(new File("water01.png"));
            bia[PATH]=ImageIO.read(new File("path01.png"));
            
            //creation of the game area
            Scanner in = new Scanner(new File ("game01.txt"));
            String line = "";
            for(int i = 0; i < gridSize; i++)
            {
                line=in.nextLine();
                String[] sa=line.split(",");
                for(int j=0;j<gridSize;j++)
                {
                    gameGrid[i][j]=Integer.parseInt(sa[j]);
                }
            }
        }
        catch(IOException ioe)
        {
            System.out.println(ioe);
        }

        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        new GameThread().start();
        gamePanel.requestFocus();
    }

    //movement for the player within the bounds of the play area
    private void nextStep()
    {
        if(playerMoving)
        {
            if(playerUp)
            {
                playerY -= 10;
                if(gameGrid[playerY/cellSize][playerX/cellSize] == NONE)
                    playerY +=10;
            }
            if(playerDown)
            {
                playerY += 10;
                if(gameGrid[(playerY+25)/cellSize][playerX/cellSize] == NONE)
                    playerY -=10;
            }
            if(playerLeft)
            {
                playerX -= 10;
                if(gameGrid[playerY/cellSize][playerX/cellSize] == NONE)
                    playerX +=10;
            }
            if(playerRight)
            {
                playerX += 10;
                if(gameGrid[playerY/cellSize][(playerX+25)/cellSize] == NONE)
                    playerX -=10;
            }
        }
    }

    private class GamePanel extends JPanel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0,0,getWidth(),getHeight());
            //paint the game area
            for(int i=0;i<gridSize;i++)
            {
                for(int j=0;j<gridSize;j++)
                {
                    int k=gameGrid[i][j];
                    if(k<1) continue;
                    g.drawImage(bia[gameGrid[i][j]],j*cellSize,i*cellSize,null);
                }
            }
            //gridlines
            g.setColor(new Color(25,25,25));
            for(int i=0;i<=gridSize;i++)
            {
                g.drawLine(0,i*cellSize,gridSize*cellSize,i*cellSize);
            }
            for(int i=0;i<=gridSize;i++)
            {
                g.drawLine(i*cellSize,0,i*cellSize,gridSize*cellSize);
            }
            //projectiles
            for(int i = 0; i < projectileList.size(); i++)
            {
                Projectile p = projectileList.get(i);
                g.drawImage(projectileBIA[SMALL],p.getX(),p.getY(),null);
            }
            //player
            g.drawImage(playerBI,playerX,playerY,null);
        }
    }
    private class GameThread extends Thread
    {
        public void run()
        {
            try
            {
                //always running
                while(true)
                {
                    sleep(15);
                    //move the player
                    nextStep();
                    //move the projectiles and remove them when the reach edge of play area
                    synchronized(projectileList)
                    {
                        for(int i = 0; i < projectileList.size(); i++)
                        {
                            Projectile p = projectileList.get(i);
                            if(gameGrid[p.getY()/cellSize][p.getX()/cellSize] != NONE)
                                p.move();
                            else
                                projectileList.remove(i);    
                        }
                    }
                    gamePanel.repaint();
                    toolkit.sync();
                }
            }
            catch(InterruptedException ie)
            {}
        }
    }

    private class KeyHandler extends KeyAdapter
    {
        public void keyPressed(KeyEvent e)
        {
            //WASD movement or arrow keys
            //set boolean variables for direction
            if(e.getKeyCode()==KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP)
            {
                playerMoving = true;
                playerUp = true;
            }
            else if(e.getKeyCode()==KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN)
            {
                playerMoving = true;
                playerDown = true;
            }
            else if(e.getKeyCode()==KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT)
            {
                playerMoving = true;
                playerLeft = true;
            }
            else if(e.getKeyCode()==KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT)
            {
                playerMoving = true;
                playerRight = true;
            }
            //shoot projectiles
            if(e.getKeyCode()==KeyEvent.VK_SPACE)
            {
                synchronized(projectileList)
                {
                    //default direction of shooting to where the player was last moving
                    if(!playerMoving && !shooting)
                    {
                        boolean[] a = new boolean [4];
                        a[lastDir] = true;
                        projectileList.add(new Projectile(playerX+10, playerY+10, 12,a[0],
                            a[1],a[2],a[3]));
                        shooting = true;
                    }
                    //shoot to the direction the player is moving
                    else if(!shooting)
                    {
                        projectileList.add(new Projectile(playerX+10, playerY+10, 12,playerUp,
                            playerDown,playerLeft,playerRight));
                        shooting = true;
                    }
                }
            }
        }
        public void keyReleased(KeyEvent e)
        {
            //reset the boolean variables and keep track of last direction
            if(e.getKeyCode()==KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP)
            {
                playerUp = false;
                if(!playerDown && !playerLeft && !playerRight)
                    lastDir = 0;
            }
            else if(e.getKeyCode()==KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN)
            {
                playerDown = false;
                if(!playerUp && !playerLeft && !playerRight)
                    lastDir = 1;
            }
            else if(e.getKeyCode()==KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT)
            {
                playerLeft = false;
                if(!playerDown && !playerUp && !playerRight)
                    lastDir = 2;
            }
            else if(e.getKeyCode()==KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT)
            {
                playerRight = false;
                if(!playerDown && !playerLeft && !playerUp)
                    lastDir = 3;
            }
            if(!playerUp && !playerDown && !playerLeft && !playerRight)
                playerMoving = false;
            //set shooting false to prevent spam shooting
            if(e.getKeyCode()==KeyEvent.VK_SPACE)
                shooting = false;
        }
    }
    
    //run the game
    public static void main(String[] args)
    {
        new Game();
    }
}