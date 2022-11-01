import java.awt.*;
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

    private static final int NONE=0;
    private static final int WATER=2;
    private static final int PATH=3;

    public Game()
    {
        super("Game");
        gamePanel = new GamePanel();
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
            g.drawImage(playerBI,0,0,null);
        }
    }

    public static void main(String[] args)
    {
        new Game();
    }
}