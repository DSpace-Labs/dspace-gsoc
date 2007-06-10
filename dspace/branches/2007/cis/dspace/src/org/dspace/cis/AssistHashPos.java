package org.dspace.cis;
/**
 * a type-safe enum for AssistHash's position
 * a private constructor to make sure that one cannot build any other instances
 * only <code>leftWit</code> and <code>rightWit</code> accepted
 * @author Administrator
 *
 */
public class AssistHashPos
{
    private String pos;
    
    private AssistHashPos(String pos)
    {
        this.pos = pos;
    }

    public String toString()
    {
        return this.pos;
    }
    public final static AssistHashPos LEFT = new AssistHashPos("LEFT");
    
    public final static AssistHashPos RIGHT = new AssistHashPos("RIGHT");
    

}
