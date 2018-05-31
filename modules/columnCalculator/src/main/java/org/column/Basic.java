package org.column;


public class Basic{

	private int a;
	private int b;

	public Basic(int _a, int _b)
	{
		a = _a;
		b = _b;
	}

	public int getA(){ return a;}
	public int getB(){ return b;}
	public void setA(int _a) { a = _a;}	
	public void setB(int _b) { b = _b;}
        
        public static void main (String args[])
        {
                Basic bas = new Basic(4,5);

                int sum = bas.getA() + bas.getB();

                System.out.println(sum);

        }
}


