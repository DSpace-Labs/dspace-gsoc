package org.dspace.cis;
/**
 * An assistant hashvalue to form the certificate
 * It includes two parts:the hashvalue and the position information (which could be
 * <code>AssistHashPos.LEFT</code> or <code>AssistHashPos.RIGHT</code>)
 * @author Jiahui Wang
 *
 */
public class AssistHash
{
    
    private String hashValue;
    private AssistHashPos pos;
    
    public AssistHash(String hashvalue, AssistHashPos pos)
    {
  
        this.hashValue = hashvalue; 
        this.pos = pos;
        
    }
    
    public void setPosL()
    {
        pos = AssistHashPos.LEFT;
    }
    public void setPosR()
    {
        pos = AssistHashPos.RIGHT;
    }
    public void setPos(AssistHashPos pos)
    {
        this.pos = pos;
    }
    public AssistHashPos getPos()
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
