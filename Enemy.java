public class Enemy {

    private int row;
    private int column;
    private int hp;
    private int type;

    private static final int EASY = 0;
    private static final int MEDIUM = 1;
    private static final int HARD = 2;

    public Enemy(int row, int column, int hp, int type)
    {
        this.row = row;
        this.column = column;
        this.hp = hp;
        this.type = type;
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
