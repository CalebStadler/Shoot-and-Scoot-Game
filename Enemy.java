public class Enemy {

    private int row;
    private int column;
    private int hp;
    private int mode;

    public static final int EASY = 0;
    public static final int MEDIUM = 1;
    public static final int HARD = 2;
    public static final int BOSS = 3;

    public Enemy(int row, int column, int mode)
    {
        this.row = row;
        this.column = column;
        this.mode = mode;
        createMode();
    }
    public void createMode()
    {
        if(mode == EASY)
        {
            hp = 10;
        }
        else if(mode == MEDIUM)
        {
            hp = 15;
        }
        else if(mode== HARD)
        {
            hp = 20;
        }
        else if(mode == BOSS)
        {
            hp = 50;
        }
    }
    public int getRow()
    {
        return row;
    }
    public int getColumn()
    {
        return column;
    }
    public int getHP()
    {
        return hp;
    }
    public void decrementHP()
    {
        hp--;
    }
}
