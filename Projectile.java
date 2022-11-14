public class Projectile 
{
    private int x;
    private int y;
    private int speed;
    private boolean dirUp;
    private boolean dirDown;
    private boolean dirLeft;
    private boolean dirRight;

    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private float deltaX;
    private float deltaY;
    private int step=0;
    private int steps;

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
    public Projectile(float startX, float startY, float endX, float endY)
    {
        this.startX=startX;
        this.startY=startY;
        this.endX=endX;
        this.endY=endY;
        steps=(int)((Math.sqrt((this.endX-this.startX)*(this.endX-this.startX)+(this.endY-this.startY)*(this.endY-this.startY)))/3f);
        deltaX=(endX-startX)/steps;
        deltaY=(endY-startY)/steps;
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
    public void setX(int x)
    {
        this.x = x;
    }
    public void setY(int y)
    {
        this.y = y;
    }
    public float getStartX()
    {
        return startX;
    }
    public float getStartY()
    {
        return startY;
    }
    public float getDeltaX()
    {
        return deltaX;
    }
    public float getDeltaY()
    {
        return deltaY;
    }
    public int getSteps()
    {
        return steps;
    }
    public int getStep()
    {
        return step;
    }
    public void setStep(int step)
    {
        this.step = step;
    }
}
