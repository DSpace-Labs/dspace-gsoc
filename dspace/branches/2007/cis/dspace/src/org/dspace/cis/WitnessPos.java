package org.dspace.cis;
/**
 * a type-safe enum for witness's position
 * a private constructor to make sure that one cannot build any other instances
 * only <code>leftWit</code> and <code>rightWit</code> accepted
 * @author Administrator
 *
 */
public class WitnessPos
{
    private String pos;
    
    private WitnessPos(String pos)
    {
        this.pos = pos;
    }

    public String toString()
    {
        return this.pos;
    }
    public final static WitnessPos leftWit = new WitnessPos("LEFT");
    
    public final static WitnessPos rightWit = new WitnessPos("RIGHT");
    

}
