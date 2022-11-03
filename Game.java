import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class Game extends JFrame
{
    private GamePanel gamePanel;
    private BufferedImage[] bia;
    private BufferedImage playerBI;
    
    private int gridSize=18;
    private int cellSize=50;
    private int[][] gameGrid;

    private int playerX = (gridSize*cellSize)/2;
    private int playerY = (gridSize*cellSize)/2; 
    private boolean playerMoving = false;
    private boolean playerUp = false;
    private boolean playerDown = false;
    private boolean playerLeft = false;
    private boolean playerRight = false;

    private GameThread gameThread=null;
    private Toolkit toolkit;

    private static final int NONE=0;
    private static final int WATER=2;
    private static final int PATH=3;

    public Game()
    {
        super("Game");

        KeyHandler kh = new KeyHandler();
        toolkit=getToolkit();

        gamePanel = new GamePanel();
        gamePanel.addKeyListener(kh);
        gamePanel.setFocusable(true);
        gamePanel.setPreferredSize(new Dimension (gridSize*cellSize,gridSize*cellSize));
        add(gamePanel);

        gameGrid=new int[gridSize][gridSize];
        bia = new BufferedImage[4]; 
        try
        {
            playerBI = ImageIO.read(new File("player.png"));
            bia[WATER]=ImageIO.read(new File("water01.png"));
            bia[PATH]=ImageIO.read(new File("path01.png"));
            
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
            for(int i=0;i<gridSize;i++)
            {
                for(int j=0;j<gridSize;j++)
                {
                    int k=gameGrid[i][j];
                    if(k<1) continue;
                    g.drawImage(bia[gameGrid[i][j]],j*cellSize,i*cellSize,null);
                }
            }
            g.setColor(new Color(25,25,25));
            for(int i=0;i<=gridSize;i++)
            {
                g.drawLine(0,i*cellSize,gridSize*cellSize,i*cellSize);
            }
            for(int i=0;i<=gridSize;i++)
            {
                g.drawLine(i*cellSize,0,i*cellSize,gridSize*cellSize);
            }
            g.drawImage(playerBI,playerX,playerY,null);
        }
    }
    private class GameThread extends Thread
    {
        public void run()
        {
            try
            {
                while(true)
                {
                    sleep(20);
                    nextStep();
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
            if(e.getKeyCode()==KeyEvent.VK_W)
            {
                playerMoving = true;
                playerUp = true;
            }
            else if(e.getKeyCode()==KeyEvent.VK_S)
            {
                playerMoving = true;
                playerDown = true;
            }
            else if(e.getKeyCode()==KeyEvent.VK_A)
            {
                playerMoving = true;
                playerLeft = true;
            }
            else if(e.getKeyCode()==KeyEvent.VK_D)
            {
                playerMoving = true;
                playerRight = true;
            }
        }
        public void keyReleased(KeyEvent e)
        {
            if(e.getKeyCode()==KeyEvent.VK_W)
                playerUp = false;
            else if(e.getKeyCode()==KeyEvent.VK_S)
                playerDown = false;
            else if(e.getKeyCode()==KeyEvent.VK_A)
                playerLeft = false;
            else if(e.getKeyCode()==KeyEvent.VK_D)
                playerRight = false;

            if(!playerUp && !playerDown && !playerLeft && !playerRight)
                playerMoving = false;
        }
    }
    
    public static void main(String[] args)
    {
        new Game();
    }
}