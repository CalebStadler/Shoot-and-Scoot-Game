public class Projectile 
{
    private int x;
    private int y;
    private int speed;
    private boolean dirUp;
    private boolean dirDown;
    private boolean dirLeft;
    private boolean dirRight;

    public Projectile(int x,int y,int speed,boolean dirUp,boolean dirDown,boolean dirLeft,boolean dirRight)
    {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.dirUp = dirUp;
        this.dirDown = dirDown;
        this.dirLeft = dirLeft;
        this.dirRight = dirRight;
    }
    public void move()
    {
        if(dirUp)
        {
            y -= speed;
        }
        else if(dirDown)
        {
            y += speed;
        }
        if(dirLeft)
        {
            x -= speed;
        }
        else if(dirRight)
        {
            x += speed;
        }
    }
    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
}
