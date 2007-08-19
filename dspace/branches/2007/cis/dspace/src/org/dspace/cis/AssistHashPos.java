package org.dspace.cis;

/**
 * a type-safe enum for AssistHash's position a private constructor to make sure
 * that one cannot build any other instances only <code>leftWit</code> and
 * <code>rightWit</code> accepted
 * 
 * @author Wang Jiahui
 * 
 */
public class AssistHashPos
{
    /**
     * The only atribute of this class.
     */
    private String pos;

    /**
     * The private constructor.
     * @param pos the position
     */
    private AssistHashPos(String pos)
    {
        this.pos = pos;
    }

    /**
     * Get the string presentation of this class.
     */
    public String toString()
    {
        return this.pos;
    }

    /**
     * The constant presenting the LEFT AssistHashPos object.
     */
    public final static AssistHashPos LEFT = new AssistHashPos("LEFT");

    /**
     * The constant presenting the RIGHT AssistHashPos object.
     */
    public final static AssistHashPos RIGHT = new AssistHashPos("RIGHT");

}
