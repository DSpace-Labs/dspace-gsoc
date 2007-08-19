package org.dspace.cis;

/**
 * An assistant hashvalue to form the certificate It includes two parts:the
 * hashvalue and the position information (which could be
 * <code>AssistHashPos.LEFT</code> or <code>AssistHashPos.RIGHT</code>)
 * 
 * @author Wang Jiahui
 * 
 */
public class AssistHash
{

    /**
     * The hashvalue.
     */
    private String hashValue;

    /**
     * The position of this assistHash.
     */
    private AssistHashPos pos;

    /**
     * Constructor of this class.
     * @param hashvalue the hashvalue
     * @param pos the position
     */
    public AssistHash(String hashvalue, AssistHashPos pos)
    {

        this.hashValue = hashvalue;
        this.pos = pos;

    }

    /**
     * Set the pos attribute to "LEFT".
     *
     */
    public void setPosL()
    {
        pos = AssistHashPos.LEFT;
    }

    /**
     * Set the pos attribute to "RIGHT".
     *
     */
    public void setPosR()
    {
        pos = AssistHashPos.RIGHT;
    }

    /**
     * Set the pos attribute.
     * @param pos the position
     */
    public void setPos(AssistHashPos pos)
    {
        this.pos = pos;
    }

    /**
     * Get the position attribute.
     * @return the position
     */
    public AssistHashPos getPos()
    {
        return pos;
    }

    /**
     * Set the hashvalue.
     * @param hashvalue the hashvalue
     */
    public void setHashvalue(String hashvalue)
    {
        this.hashValue = hashvalue;
    }

    /**
     * Get the hashvalue.
     * @return the hashvalue
     */
    public String getHashvalue()
    {
        return hashValue;
    }

}
