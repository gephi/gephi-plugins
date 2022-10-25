package helpers;

public class Point
{

    public Point(float x,  float y)
    {
	this.x = x;
	this.y = y;
    }
    public Point()
    {
	this(0,0);
    }
    public float GetX()
    {
	return x;
    }
    public float GetY()
    {
	return y;
    }
    public void SetX(float x)
    {
	this.x = x;
    }
    public void SetY(float y)
    {
	this.y = y;
    }

    public void Print()
    {
	System.out.print("(" + x + "," + y + ")");
    }
    
    private float x;
    private float y;
}