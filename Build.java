import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.*;
import java.util.*;
import java.io.*;

public class Build extends JFrame
{
  private int gridSize=18;
  private int cellSize=50;
  private int[][] gameGrid;
  private JMenuItem fileOpenItem;
  private JMenuItem fileSaveItem;
  private JPanel gamePanel;
  private JPanel typePanel;
  private JButton newButton;
  private JLabel statusLabel;
  private static final int NONE=0;
  private static final int ENEMY=1;
  private static final int WATER=2;
  private static final int PATH=3;
  private static final int SOLID_BRICK=4;
  private static final int LADDER=5;
  private static final int WIRE=6;
  private static final int GOLD=7;
  private static final int MAX=8;
  private static final int STABLE=9;
  private int currType=-1;
  private BufferedImage[] bia;
  private BufferedImage[] playerBia;

  public Build()
  {
    super("Build");

    ActionHandler ah=new ActionHandler();
    MouseHandler mh=new MouseHandler();

    JMenuBar jmb=new JMenuBar();
    setJMenuBar(jmb);

    JMenu openMenu=new JMenu("Open");
    jmb.add(openMenu);

    fileOpenItem=new JMenuItem("File");
    fileOpenItem.addActionListener(ah);
    openMenu.add(fileOpenItem);

    fileSaveItem=new JMenuItem("Save");
    fileSaveItem.addActionListener(ah);
    openMenu.add(fileSaveItem);

    JPanel buttonPanel=new JPanel();
    add(buttonPanel,BorderLayout.NORTH);

    JPanel mainPanel=new JPanel();
    mainPanel.setLayout(new BorderLayout());
    add(mainPanel);

    gamePanel=new JPanel()
    {
      public void paintComponent(Graphics g)
      {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(new Color(50,50,50));
        for(int i=0;i<=gridSize;i++)
        {
          g.drawLine(0,i*cellSize,gridSize*cellSize,i*cellSize);
        }
        for(int i=0;i<=gridSize;i++)
        {
          g.drawLine(i*cellSize,0,i*cellSize,gridSize*cellSize);
        }
        for(int i=0;i<gridSize;i++)
        {
          for(int j=0;j<gridSize;j++)
          {
            if(gameGrid[i][j]==STABLE)
            {
              g.setColor(new Color(50,50,50));
              g.fillRect(j*cellSize,i*cellSize,cellSize,cellSize);
            }
//System.out.println(i+" "+j+" "+gameGrid[j][i]);
            else draw(g,j,i,gameGrid[i][j]);
//g.setColor(Color.GRAY);
//g.drawRect(i*cellSize-offx,j*cellSize-offy,cellSize,cellSize);
          }
        }
      }
    };
    gamePanel.setPreferredSize(new Dimension(gridSize*cellSize
                                ,gridSize*cellSize));
    gamePanel.addMouseListener(mh);
    mainPanel.add(new JScrollPane(gamePanel));

    typePanel=new JPanel()
    {
      public void paintComponent(Graphics g)
      {
        super.paintComponent(g);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0,0,getWidth(),getHeight());
        for(int k=0;k<MAX;k++)
        {
g.setColor(Color.BLACK);
g.drawRect(0,k*cellSize,cellSize,cellSize);
          draw(g,0,k,k);
        }
      }
    };
    typePanel.setPreferredSize(new Dimension(100,-1));
    typePanel.addMouseListener(mh);
    mainPanel.add(new JScrollPane(gamePanel));
    add(typePanel,BorderLayout.EAST);

    JPanel statusPanel=new JPanel();
    add(statusPanel,BorderLayout.SOUTH);

    statusLabel=new JLabel(" ");
    statusPanel.add(statusLabel);

    gameGrid=new int[gridSize][gridSize];
    //loadGameFile(new File("ladders01.txt"));
    bia=new BufferedImage[9];
    try
    {
      bia[ENEMY]=ImageIO.read(new File("enemy.png"));
      //bia[WATER]=ImageIO.read(new File("water01.png"));
      bia[PATH]=ImageIO.read(new File("path01.png"));
      bia[SOLID_BRICK]=ImageIO.read(new File("solidbrick.png"));
      bia[LADDER]=ImageIO.read(new File("ladder.png"));
      bia[WIRE]=ImageIO.read(new File("wire.png"));
      bia[GOLD]=ImageIO.read(new File("gold.png"));
    }
    catch(IOException ioe)
    {
      System.out.println(ioe);
    }

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
//    setSize(800,800);
//    setResizable(false);
    setVisible(true);
    gamePanel.requestFocus();
  }

  private void draw(Graphics g,int j,int i,int k)
  {
      if(k<0||k>=MAX) return;
      g.drawImage(bia[k],j*cellSize,i*cellSize,null);
  }

  private void loadGameFile(File file)
  {
    try
    {
      BufferedReader in=new BufferedReader(new FileReader(file));
      String line;
      for(int i=0;i<gridSize;i++)
      {
        line=in.readLine();
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
  }

  private void saveGameFile(File file)
  {
    try
    {
      PrintWriter out=new PrintWriter(new FileWriter(file));
      for(int i=0;i<gridSize;i++)
      {
        for(int j=0;j<gridSize;j++)
        {
          out.printf("%1d",gameGrid[i][j]);
          if(j<gridSize-1) out.print(",");
        }
        out.println();
      }
      out.close();
    }
    catch(IOException ioe)
    {
      System.out.println(ioe);
    }
  }

  private class ActionHandler implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      if(e.getSource()==fileOpenItem)
      {
        loadGameFile(new File("game01.txt"));
      }
      else if(e.getSource()==fileSaveItem)
      {
        saveGameFile(new File("game01.txt"));
      }
    }
  }

  private class MouseHandler extends MouseAdapter
  {
    public void mousePressed(MouseEvent e)
    {
      if(e.getSource()==gamePanel)
      {
        int gx=e.getX()/cellSize;
        int gy=e.getY()/cellSize;
        gameGrid[gy][gx]=currType;
        gamePanel.repaint();
      }
      else if(e.getSource()==typePanel)
      {
        currType=e.getY()/cellSize;
        if(currType>MAX) currType=MAX;
      }
    }
  }

  public static void main(String[] args)
  {
    new Build();
  }
}
