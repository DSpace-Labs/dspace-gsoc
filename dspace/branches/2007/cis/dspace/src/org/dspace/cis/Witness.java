package org.dspace.cis;
/**
 * a witness class
 * @author Jiahui Wang
 *
 */
public class Witness
{
    
    private String hashValue;
    private WitnessPos pos;
    
    public Witness(String hashvalue, WitnessPos pos)
    {

        
        this.hashValue = hashvalue; 
        
    }
    
    public void setPosL()
    {
        pos = WitnessPos.leftWit;
    }
    
    public void setPosR()
    {
        pos = WitnessPos.rightWit;
    }
    public void setPos(WitnessPos pos)
    {
        this.pos = pos;
    }
    public WitnessPos getPos()
    {
        return pos;
    }
    public void setHashvalue(String hashvalue)
    {
        this.hashValue = hashvalue;
    }
    public String getHashvalue()
    {
        return hashValue;
    }
    
}
