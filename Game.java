import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class Game extends JFrame
{
    //views
    private static final int MAIN = 0;
    private static final int PLAYING = 1;
    private static final int RULES = 2;
    private static final int HIGHSCORE = 3;
    private static final int WINNER = 4;
    private static final int LOSER = 5;
    private static final int LEVELSELECT = 6;
    private int view = 0;

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
    private int playerHP = 10;
    private int lives = 3;

    //variables needed for the enemies
    private ArrayList<Enemy> enemyList;
    private ArrayList<Projectile> enemyProjectileList;
    private int wait = 0;
    
    private Toolkit toolkit;

    //static constants for the game area
    private static final int NONE=0;
    private static final int ENEMY=1;
    private static final int WATER=2;
    private static final int PATH=3;
    private static final int NEXT_LEVEL=4;
    private static final int START=5;
    private static final int TRIPLE=6;
    private static final int GOLD=7;

    //variables for controlling the levels
    private String[] levels;
    private int currLevel = 0;
    private boolean levelComplete = false;

    //variable for the running of the game thread
    private boolean running = false;

    //variable for time
    private String time = "";
    private int timeScore = 0;

    //variables needed for the projectiles
    private BufferedImage[] projectileBIA;
    private ArrayList<Projectile> projectileList;
    private boolean shooting = false;
    private static final int SMALL=0;
    private static final int BIG=1;

    //variables for powerups
    private boolean tripleP = false;

    //variables for gold
    private int gold = 0;

    //variables for total score
    private int score = 0;
    private int hpDealtScore = 0;
    private int enemiesKilled = 0;
    private int complete = 0;
    private File highScores;
    private int[] highScoreA = new int[10];

    public Game()
    {
        super("Shoot & Scoot");

        KeyHandler kh = new KeyHandler();
        toolkit=getToolkit();

        //gamePanel
        gamePanel = new GamePanel();
        gamePanel.addKeyListener(kh);
        gamePanel.setFocusable(true);
        gamePanel.setPreferredSize(new Dimension (gridSize*cellSize,gridSize*cellSize));
        //no cursor on the screen 
        Image image=toolkit.createImage(new byte[]{});
        Cursor myCursor=toolkit.createCustomCursor(image,new Point(0,0),"");
        gamePanel.setCursor(myCursor);
        add(gamePanel);

        //creation of game grid and buffered image array
        gameGrid=new int[gridSize][gridSize];
        bia = new BufferedImage[8]; 

        //array for the level files
        levels = new String[10];
        for (int i = 0; i < levels.length; i++)
        {
            levels[i] = "Levels/game" + (i+1) + ".txt";
        }
        
        //creation of projectile list and buffered image array
        projectileList = new ArrayList<Projectile>();
        projectileBIA = new BufferedImage[2];
        
        //creation of enemy list
        enemyList = new ArrayList<Enemy>();
        enemyProjectileList = new ArrayList<Projectile>();

        //take in all of the files needed for the game
        try
        {
            playerBI = ImageIO.read(new File("Images/player.png"));
            projectileBIA[SMALL] = ImageIO.read(new File("Images/smallBall.png"));
            projectileBIA[BIG] = ImageIO.read(new File("Images/bigBall.png"));
            highScores = new File("scores.txt");
        }
        catch(IOException ioe)
        {
            System.out.println(ioe);
        }

        takeInHighScores();

        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        gamePanel.requestFocus();
    }
    //resets the game back to the initial state
    private void resetGame()
    {
        //reset all variables
        currLevel = 0; playerHP = 10; lives = 3; gold = 0; 
        hpDealtScore = 0; timeScore= 0; enemiesKilled = 0; complete = 0;
        levelComplete = false;
        playerUp = false; playerDown = false; playerLeft = false; playerRight = false; 
        lastDir = 0; playerMoving = false; tripleP = false;
        projectileList.clear(); enemyList.clear(); enemyProjectileList.clear();
        createLevel();
        running = true;
    }
    //called by level selection
    private void levelSelect()
    {
        view = PLAYING;
        running = true;
        enemyList.clear();
        createLevel();
        gamePanel.repaint();
        new GameThread().start();
        new TimerThread().start();
    }
    private void calcScore()
    {
        score = (lives*2000) + (playerHP*100) + hpDealtScore + (enemiesKilled*100) -
            timeScore + (gold*100) + (currLevel * 1000) + complete;
        if(score < 0 )
            score = 0;
    }
    private void takeInHighScores()
    {
        try
        {
            Scanner in = new Scanner(highScores);
            int i = 0;
            while(in.hasNextInt())
            {
                highScoreA[i] = in.nextInt();
                i++;                
            }
            Arrays.sort(highScoreA);
            in.close();
        }
        catch(IOException ioe)
        {
            System.out.println(ioe);
        }
    }
    private void addToHighScores()
    {
        int min = highScoreA[0];
        int minIndex = 0;
        try
        {
            PrintWriter out = new PrintWriter(highScores);
            for(int i = 1; i < highScoreA.length; i++)
            {
                if(highScoreA[i] < min)
                {
                    min = highScoreA[i];
                    minIndex = i;
                }
            }
            if(score > min)
                highScoreA[minIndex] = score;
            Arrays.sort(highScoreA);
            for (int i = 0; i < highScoreA.length; i++)
                out.println(highScoreA[i]);
            out.close();
        }
        catch(IOException ioe)
        {
            System.out.println(ioe);
        }
    }
    //creates each level
    private void createLevel()
    {
        //take in all of the files needed for the game
        try
        {
            bia[ENEMY]=ImageIO.read(new File("Images/enemy.png"));
            bia[WATER]=ImageIO.read(new File("Images/water.png"));
            bia[PATH]=ImageIO.read(new File("Images/path.png"));
            bia[NEXT_LEVEL]=ImageIO.read(new File("Images/nextLevel.png"));
            bia[START]=ImageIO.read(new File("Images/start.png"));
            bia[TRIPLE]=ImageIO.read(new File("Images/tripleShot.png"));
            bia[GOLD]=ImageIO.read(new File("Images/gold.png"));
            
            //creation of the game area
            Scanner in = new Scanner(new File (levels[currLevel]));
            String line = "";
            for(int i = 0; i < gridSize; i++)
            {
                line=in.nextLine();
                String[] sa=line.split(",");
                for(int j=0;j<gridSize;j++)
                {
                    gameGrid[i][j]=Integer.parseInt(sa[j]);
                    //if square is NEXT_LEVEL, hide it with a NONE square
                    if(Integer.parseInt(sa[j]) == NEXT_LEVEL && !levelComplete)
                        gameGrid[i][j] = NONE;
                    //if square is START set the player's x and y there
                    if(Integer.parseInt(sa[j]) == START && !levelComplete)
                    {
                        playerX = j * cellSize;
                        playerY = i * cellSize;
                    }
                    if(Integer.parseInt(sa[j]) == TRIPLE && tripleP)
                    {
                        gameGrid[i][j] = PATH;
                    }
                    if(Integer.parseInt(sa[j]) == GOLD && levelComplete)
                    {
                        gameGrid[i][j] = PATH;
                    }
                    //if ENEMY square create an enemy object
                    if(Integer.parseInt(sa[j]) == ENEMY && !levelComplete)
                    {
                        //set difficulty based on level
                        int difficulty = Enemy.EASY;
                        if(currLevel + 1 > 3 && currLevel + 1 < 7)
                            difficulty = Enemy.MEDIUM;
                        else if(currLevel + 1 > 6 && currLevel + 1 < 10)
                            difficulty = Enemy.HARD;
                        else if(currLevel + 1 == 10)
                            difficulty = Enemy.BOSS;
    
                        synchronized(enemyList)
                        {
                            Enemy e = new Enemy(i,j, difficulty);
                            enemyList.add(e);
                        }

                    }
                    //if ENEMY and the level is complete make the ENEMY a PATH
                    else if(Integer.parseInt(sa[j]) == ENEMY && levelComplete)
                    {
                        gameGrid[i][j] = PATH;
                    }
                }
            }
        }
        catch(IOException ioe)
        {
            System.out.println(ioe);
        }
    }
    //movement for the player within the bounds of the play area
    private void nextStep()
    {
        if(playerMoving)
        {
            if(playerUp)
            {
                playerY -= 7;
                //collision with walls, water, and enemies
                if(gameGrid[playerY/cellSize][(playerX+25)/cellSize] == NONE ||
                    gameGrid[playerY/cellSize][playerX/cellSize] == NONE ||
                    gameGrid[playerY/cellSize][(playerX+25)/cellSize] == ENEMY ||
                    gameGrid[playerY/cellSize][playerX/cellSize] == ENEMY || 
                    gameGrid[playerY/cellSize][(playerX+25)/cellSize] == WATER ||
                    gameGrid[playerY/cellSize][playerX/cellSize] == WATER)
                    playerY +=7;
            }
            if(playerDown)
            {
                playerY += 7;
                //collision with walls, water, and enemies
                if(gameGrid[(playerY+25)/cellSize][(playerX+25)/cellSize] == NONE ||
                    gameGrid[(playerY+25)/cellSize][playerX/cellSize] == NONE ||
                    gameGrid[(playerY+25)/cellSize][(playerX+25)/cellSize] == ENEMY ||
                    gameGrid[(playerY+25)/cellSize][playerX/cellSize] == ENEMY ||
                    gameGrid[(playerY+25)/cellSize][(playerX+25)/cellSize] == WATER ||
                    gameGrid[(playerY+25)/cellSize][playerX/cellSize] == WATER)
                    playerY -=7;
            }
            if(playerLeft)
            {
                playerX -= 7;
                //collision with walls, water, and enemies
                if(gameGrid[(playerY+25)/cellSize][playerX/cellSize] == NONE ||
                    gameGrid[playerY/cellSize][playerX/cellSize] == NONE ||
                    gameGrid[(playerY+25)/cellSize][playerX/cellSize] == ENEMY ||
                    gameGrid[playerY/cellSize][playerX/cellSize] == ENEMY ||
                    gameGrid[(playerY+25)/cellSize][playerX/cellSize] == WATER ||
                    gameGrid[playerY/cellSize][playerX/cellSize] == WATER)
                    playerX +=7;
            }
            if(playerRight)
            {
                playerX += 7;
                //collision with walls, water, and enemies
                if(gameGrid[(playerY+25)/cellSize][(playerX+25)/cellSize] == NONE ||
                    gameGrid[playerY/cellSize][(playerX+25)/cellSize] == NONE ||
                    gameGrid[(playerY+25)/cellSize][(playerX+25)/cellSize] == ENEMY ||
                    gameGrid[playerY/cellSize][(playerX+25)/cellSize] == ENEMY ||
                    gameGrid[(playerY+25)/cellSize][(playerX+25)/cellSize] == WATER ||
                    gameGrid[playerY/cellSize][(playerX+25)/cellSize] == WATER)
                    playerX -=7;
            }
            //if the player is in the NEXt_LEVEL area change levels or end game
            if(gameGrid[playerY/cellSize][playerX/cellSize] == NEXT_LEVEL)
            {
                currLevel++;
                //if final level end game
                if(currLevel >= levels.length)
                {
                    running = false;
                    view = WINNER;
                    complete = 5000;
                    calcScore();
                    addToHighScores();
                    resetGame();
                }
                //else move to next level
                else 
                {
                    levelComplete = false;
                    createLevel();
                }
            }
            if(gameGrid[playerY/cellSize][playerX/cellSize] == TRIPLE)
            {
                tripleP = true;
                gameGrid[playerY/cellSize][playerX/cellSize] = PATH;
            }
            if(gameGrid[playerY/cellSize][playerX/cellSize] == GOLD)
            {
                gold++;
                gameGrid[playerY/cellSize][playerX/cellSize] = PATH;
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
            //Main Menu view
            if(view == MAIN)
            {
                g.setColor(Color.WHITE);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 100));
                g.drawString("Shoot & Scoot",100,100);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 50));
                g.drawString("Press P to play",275,300);
                g.drawString("Press R for the rules",200,400);
                g.drawString("Press H for the highscores",135,500);
                g.drawString("Press L for the Level Selection",90,600);
            }
            //Rules view
            else if(view == RULES)
            {
                g.setColor(Color.WHITE);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 75));
                g.drawString("Rules",350,100);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 25));
                g.drawString("1. Use WASD or the arrow keys to move.",50,200);
                g.drawString("2. Shoot with the spacebar.",50,260);
                g.drawString("3. Defeat the enemies to move on to next level.",50,320);
                g.drawString("4. Enemies get harder as you progress.",50,380);
                g.drawString("5. Orange zones are safe zones, cannot shoot nor be shot.",50,440);
                g.drawString("6. Blue zones are water, can be shot over, but not moved over.",50,500);
                g.drawString("7. Yellow zones are used to move to the next level.",50,560);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 50));
                g.drawString("Press B to go back to main menu",50,700);
            }
            //Highscore view
            else if(view == HIGHSCORE)
            {
                g.setColor(Color.WHITE);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 75));
                g.drawString("Highscores",250,100);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 50));
                for(int i = 0; i < highScoreA.length; i++)
                {
                    g.drawString("" + highScoreA[i],380,125 + (highScoreA.length - i) * 50);
                }
                g.drawString("Press B to go back to main menu",50,700);
            }
            //level selction view
            else if(view == LEVELSELECT)
            {
                g.setColor(Color.WHITE);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 75));
                g.drawString("Levels",330,100);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 25));
                g.drawString("Use arrow keys to move to desired level",200,150);
                for(int i = 0; i < levels.length; i++)
                    g.drawString("Level " + (i + 1), 400, 200 + (i*50));
                g.drawRect(395,175 + (currLevel * 50),110,30);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 50));
                g.drawString("Press B to go back to main menu",50,700);
            }
            //Winner view
            else if(view == WINNER)
            {
                g.setColor(Color.GREEN);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 75));
                g.drawString("WINNER",290,450);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 25));
                g.drawString("Score: " + score,350,500);
                score = 0;
                g.drawString(time,370,550);
                g.drawString("Press R to restart",325,600);
                g.drawString("Press M for main menu",300,650);
            }
            //Loser view
            else if (view == LOSER)
            {
                g.setColor(Color.RED);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 75));
                g.drawString("YOU ARE DEAD",165,450);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 25));
                g.drawString("Score: " + score,350,500);
                score = 0;
                g.drawString(time,370,550);
                g.drawString("Press R to restart",325,600);
                g.drawString("Press M for main menu",300,650);
            }
            //Play view
            else if(view == PLAYING)
            {
                //paint the game area
                for(int i=0;i<gridSize;i++)
                {
                    for(int j=0;j<gridSize;j++)
                    {
                        if(gameGrid[i][j]<1) continue;
                        g.drawImage(bia[gameGrid[i][j]],j*cellSize,i*cellSize,null);
                        if (gameGrid[i][j] == ENEMY)
                        {
                            g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 20));
                            //find which enemy and print their current HP
                            for(int k = 0; k < enemyList.size(); k ++)
                            {
                                if(enemyList.get(k).getRow() == i && enemyList.get(k).getColumn() == j)
                                    g.drawString("" + enemyList.get(k).getHP(),j*cellSize + 15,i*cellSize + 35);
                            }
                        }
                    }
                }
                //gridlines that will be commented out at the end
                /*
                g.setColor(new Color(25,25,25));
                for(int i=0;i<=gridSize;i++)
                {
                    g.drawLine(0,i*cellSize,gridSize*cellSize,i*cellSize);
                }
                for(int i=0;i<=gridSize;i++)
                {
                    g.drawLine(i*cellSize,0,i*cellSize,gridSize*cellSize);
                }
                */
                //projectiles
                for(int i = 0; i < projectileList.size(); i++)
                {
                    Projectile p = projectileList.get(i);
                    g.drawImage(projectileBIA[SMALL],p.getX(),p.getY(),null);
                }
                for(int i = 0; i < enemyProjectileList.size(); i++)
                {
                    Projectile p = enemyProjectileList.get(i);
                    if(currLevel+1 == 10)
                        g.drawImage(projectileBIA[BIG],p.getX(),p.getY(),null);
                    else
                        g.drawImage(projectileBIA[SMALL],p.getX(),p.getY(),null);
                }
                //player
                g.drawImage(playerBI,playerX,playerY,null);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 15));
                //player HP
                g.drawString("" + playerHP, playerX+6, playerY+20);

                //draw level on screen and number of lives left
                g.setColor(Color.WHITE);
                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 30));
                g.drawString("Level: " + (currLevel + 1),170,35);  
                g.drawString(time,300,35);
                g.drawString("Lives: " + lives,490,35);   
                g.drawString("Gold: " + gold, 630,35);  
            }
        }
    }
    private class GameThread extends Thread
    {
        public void run()
        {
            try
            {
                while(running && view == PLAYING)
                {
                    sleep(15);
                    wait++;
                    //move the player
                    nextStep();

                    //enemies shoot every 1.02 seconds
                    if(wait % 70 == 0)
                    {
                        for(int i = 0; i < enemyList.size(); i++)
                        {
                            Enemy e = enemyList.get(i);
                            Projectile p = new Projectile((float)e.getColumn()*cellSize+25,(float)e.getRow()*cellSize+25, (float)playerX + 10, (float)playerY + 10);
                            enemyProjectileList.add(p);
                            if(e.getMode() == Enemy.BOSS)
                            {
                                gameGrid[e.getRow()][e.getColumn()] = PATH;
                                e.setColumn(e.getColumn() + e.getBossDirX());
                                e.setRow(e.getRow() + e.getBossDirY());
                                gameGrid[e.getRow()][e.getColumn()] = ENEMY;
                                if(gameGrid[e.getRow() + e.getBossDirY()][e.getColumn()] == NONE ||
                                gameGrid[e.getRow() + e.getBossDirY()][e.getColumn()] == GOLD ||
                                gameGrid[e.getRow() + e.getBossDirY()][e.getColumn()] == WATER ||
                                gameGrid[e.getRow() + e.getBossDirY()][e.getColumn()] == START || 
                                gameGrid[e.getRow() + e.getBossDirY()][e.getColumn()] == ENEMY)
                                {
                                    e.setBossDirY(e.getBossDirY() * -1);
                                }
                                if(gameGrid[e.getRow()][e.getColumn() + e.getBossDirX()] == NONE ||
                                gameGrid[e.getRow()][e.getColumn() + e.getBossDirX()] == GOLD ||
                                gameGrid[e.getRow()][e.getColumn() + e.getBossDirX()] == WATER ||
                                gameGrid[e.getRow()][e.getColumn() + e.getBossDirX()] == START ||
                                gameGrid[e.getRow()][e.getColumn() + e.getBossDirX()] == ENEMY)
                                {
                                    e.setBossDirX(e.getBossDirX() * -1);
                                }
                            }
                        }
                    }
                    //Enemy projectile movement 
                    for(int i = 0; i < enemyProjectileList.size(); i++)
                    {
                        Projectile p = enemyProjectileList.get(i);
                        p.setX((int)(p.getStartX() + p.getStep() * p.getDeltaX()));
                        p.setY((int)(p.getStartY() + p.getStep() * p.getDeltaY()));
                        p.setStep(p.getStep() + 1);
                    }
                    //Remove if hitting something
                    for(int i = enemyProjectileList.size() - 1; i >= 0; i--)
                    {
                        Projectile p = enemyProjectileList.get(i);
                        if(gameGrid[p.getY()/cellSize][p.getX()/cellSize] == NONE ||
                            gameGrid[p.getY()/cellSize][p.getX()/cellSize] == START) 
                        {
                            enemyProjectileList.remove(i);
                        }
                        if(currLevel + 1 == 10)
                        {
                            if((p.getY() >= playerY && p.getY() <= playerY + 30 && p.getX() >= playerX && p.getX() <= playerX + 30) ||
                            (p.getY() + 20 >= playerY && p.getY() + 20 <= playerY + 30 && p.getX() >= playerX && p.getX() <= playerX + 30) ||
                            (p.getY() >= playerY && p.getY() <= playerY + 30 && p.getX() + 20 >= playerX && p.getX() + 20 <= playerX + 30))
                            {
                                enemyProjectileList.remove(i);
                                playerHP--;
                            }
                        }
                        else if(p.getY() >= playerY && p.getY() <= playerY + 30 && p.getX() >= playerX && p.getX() <= playerX + 30)
                        {
                            enemyProjectileList.remove(i);
                            playerHP--;
                        }
                    }
                    
                    //move the projectiles and remove them when the reach edge of play area
                    synchronized(projectileList)
                    {
                        //move through the projectile list
                        for(int i = projectileList.size() - 1; i >= 0; i--)
                        {
                            //create a projectile
                            Projectile p = projectileList.get(i);
                            //if not hitting none or enemy keep going
                            if(gameGrid[(p.getY()+5)/cellSize][p.getX()/cellSize] != NONE && 
                                gameGrid[(p.getY()+5)/cellSize][p.getX()/cellSize] != ENEMY)
                                p.move();
                            //if its an enemy
                            else if(gameGrid[(p.getY()+5)/cellSize][p.getX()/cellSize] == ENEMY)
                            {
                                projectileList.remove(i);
                                //find which enemy
                                synchronized(enemyList)
                                {
                                    for(int j = enemyList.size() - 1; j >= 0;j--)
                                    {
                                        if((p.getY()+5)/cellSize == enemyList.get(j).getRow() && 
                                            p.getX()/cellSize == enemyList.get(j).getColumn())
                                        {
                                            //decrement the hp and if its at 0 it is dead and turns to PATH
                                            enemyList.get(j).decrementHP();
                                            if(tripleP)
                                                hpDealtScore += 10;
                                            else
                                                hpDealtScore += 15;
                                            
                                            if(enemyList.get(j).getHP() == 0)
                                            {
                                                gameGrid[(p.getY()+5)/cellSize][p.getX()/cellSize] = PATH;
                                                enemyList.remove(j);
                                                enemiesKilled++;
                                            }
                                        }
                                    }
                                }
                            }
                            //remove if its hit the edge of the area
                            else
                                projectileList.remove(i);    
                        }
                    }
                    //if no enemies left, the level is complete
                    if (enemyList.isEmpty() && !levelComplete)
                    {
                        levelComplete = true;
                        projectileList.clear();
                        enemyProjectileList.clear();
                        createLevel();
                    }
                    //if play has no HP and no lives, lose
                    if (playerHP <= 0)
                    {
                        lives--;
                        if(lives == 0)
                        {
                            running = false;
                            view = LOSER;
                            calcScore();
                            addToHighScores();
                            resetGame();
                        }
                        else 
                            playerHP = 10;
                    }
                    gamePanel.repaint();
                    toolkit.sync();
                }
            }
            catch(InterruptedException ie)
            {}
        }
    }

    private class TimerThread extends Thread
    {
        public void run()
        {
            long base=System.currentTimeMillis();
            try
            {
                while(running && view == PLAYING)
                {
                    sleep(15);
                    long curr=System.currentTimeMillis();
                    int delt=(int)(curr-base)/100;
                    int mins=(int)(delt/600);
                    int secs=(int)((delt/10)%60);
                    time = "Time: "+String.format("%3d:%02d"
                           ,mins,secs);
                    timeScore = mins * 120 + secs * 2;
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
            //Main menu keys
            if(view == MAIN)
            {
                if(e.getKeyCode()==KeyEvent.VK_P)
                {
                    view = PLAYING;
                    createLevel();
                    gamePanel.repaint();
                    running = true;
                    new GameThread().start();
                    new TimerThread().start();
                }
                else if(e.getKeyCode()==KeyEvent.VK_R)
                {
                    view = RULES;
                    gamePanel.repaint();
                }
                else if(e.getKeyCode()==KeyEvent.VK_H)
                {
                    view = HIGHSCORE;
                    takeInHighScores();
                    gamePanel.repaint();
                }
                else if(e.getKeyCode()==KeyEvent.VK_L)
                {
                    view = LEVELSELECT;
                    currLevel = 0;
                    gamePanel.repaint();
                }
            }
            //level selection keys
            else if(view == LEVELSELECT)
            {
                if(e.getKeyCode() == KeyEvent.VK_UP)
                {
                    if(currLevel > 0)
                        currLevel--;
                    gamePanel.repaint();
                }
                else if(e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    if(currLevel < 9)
                        currLevel++;
                    gamePanel.repaint();
                }
                else if(e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    levelSelect();
                }
                if(e.getKeyCode()==KeyEvent.VK_B)
                {
                    view = MAIN;
                    currLevel = 0;
                    gamePanel.repaint();
                }
            }
            //Rules and highscore keys
            else if(view == RULES || view == HIGHSCORE)
            {
                if(e.getKeyCode()==KeyEvent.VK_B)
                {
                    view = MAIN;
                    gamePanel.repaint();
                }
            }
            //loser and winner keys   
            else if(view == LOSER || view == WINNER)
            {
                if(e.getKeyCode()==KeyEvent.VK_R)
                {
                    view = PLAYING;
                    gamePanel.repaint();
                    running = true;
                    new GameThread().start();
                    new TimerThread().start();
                }
                else if(e.getKeyCode()==KeyEvent.VK_M)
                {
                    view = MAIN;
                    gamePanel.repaint();
                }
            }
            //Play keys         
            else if(view == PLAYING)
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
                else if(e.getKeyCode()==KeyEvent.VK_B)
                {
                    view = MAIN;
                    gamePanel.repaint();
                }
                //shoot projectiles
                if(e.getKeyCode()==KeyEvent.VK_SPACE && gameGrid[playerY/cellSize][playerX/cellSize] != START)
                {
                    synchronized(projectileList)
                    {
                        //default direction of shooting to where the player was last moving
                        if(!playerMoving && !shooting)
                        {
                            boolean[] a = new boolean [4];
                            a[lastDir] = true;
                            if(tripleP)
                            {
                                projectileList.add(new Projectile(playerX, playerY, 10,a[0],
                                    a[1],a[2],a[3]));
                                projectileList.add(new Projectile(playerX-10, playerY-10, 10,a[0],
                                    a[1],a[2],a[3]));
                            }
                            projectileList.add(new Projectile(playerX+10, playerY+10, 10,a[0],
                                a[1],a[2],a[3]));
                            shooting = true;
                        }
                        //shoot to the direction the player is moving
                        else if(!shooting)
                        {
                            if(tripleP)
                            {
                                projectileList.add(new Projectile(playerX, playerY, 10, playerUp,
                                    playerDown,playerLeft,playerRight));
                                projectileList.add(new Projectile(playerX-10, playerY-10, 10,playerUp,
                                    playerDown,playerLeft,playerRight));
                            }
                            projectileList.add(new Projectile(playerX+10, playerY+10, 10,playerUp,
                                playerDown,playerLeft,playerRight));
                            shooting = true;
                        }
                    }
                }                
            }
        }
        public void keyReleased(KeyEvent e)
        {
            //only if playing
            if(view == PLAYING)
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
    }
    
    //run the game
    public static void main(String[] args)
    {
        new Game();
    }
}