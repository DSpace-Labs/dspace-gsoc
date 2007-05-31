package org.dspace.cis;

/**
 * a type-safe enum for one-way hashing algorithms' names 
 * the private constructor will forbid us from constructing extra instances
 * only <code>MD2</code>, <code>MD5</code>, <code>SHA-1</code>, 
 * <code>SHA-256</code>, <code>SHA-384</code>
 * and <code>512</code> accepted
 * 
 * @author Jiahui Wang
 * 
 */
public class HashAlgorithms
{
    private String algrithmName;

    private HashAlgorithms(String an)
    {
        algrithmName = an;
    }

    public String toString()
    {
        return algrithmName;
    }

    public static final HashAlgorithms MD2 = new HashAlgorithms("MD2");

    public static final HashAlgorithms MD5 = new HashAlgorithms("MD5");

    public static final HashAlgorithms SHA1 = new HashAlgorithms("SHA-1");

    public static final HashAlgorithms SHA256 = new HashAlgorithms("SHA-256");

    public static final HashAlgorithms SHA384 = new HashAlgorithms("SHA-384");

    public static final HashAlgorithms SHA512 = new HashAlgorithms("SHA-512");
}
