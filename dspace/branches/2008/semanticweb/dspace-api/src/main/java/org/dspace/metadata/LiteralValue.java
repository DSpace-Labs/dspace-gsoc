package org.dspace.metadata;

public interface LiteralValue extends Value
{

    public String getLexicalForm();

    public String getLanguage();

    public String getDatatypeURI();

    public URIResource getDatatypeURIResource();

    public boolean getBoolean();

    public byte getByte();

    public short getShort();

    public int getInt();

    public long getLong();

    public char getChar();

    public float getFloat();

    public double getDouble();

    public String getString();

}
