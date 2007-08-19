package org.dspace.cis;

/**
 * a type-safe enum for one-way hashing algorithms' names the private
 * constructor will forbid us from constructing extra instances only
 * <code>MD2</code>, <code>MD5</code>, <code>SHA-1</code>,
 * <code>SHA-256</code>, <code>SHA-384</code> and <code>512</code>
 * accepted
 * 
 * @author Wang Jiahui
 * 
 */
public class HashAlgorithms
{
    /**
     * The algorithmName
     */
    private String algorithmName;

    /**
     * The primitive constructor.
     * @param an the algorithm's name
     */
    private HashAlgorithms(String an)
    {
        algorithmName = an;
    }

    /**
     * Get the String presentation of this class.
     */
    public String toString()
    {
        return algorithmName;
    }

    /**
     * The constant presenting the MD2 hash algorithm.
     */
    public static final HashAlgorithms MD2 = new HashAlgorithms("MD2");

    /**
     * The constant presenting the MD5 hash algorithm.
     */
    public static final HashAlgorithms MD5 = new HashAlgorithms("MD5");

    /**
     * The constant presenting the SHA-1 hash algorithm.
     */
    public static final HashAlgorithms SHA1 = new HashAlgorithms("SHA-1");

    /**
     * The constant presenting the SHA-256 hash algorithm.
     */
    public static final HashAlgorithms SHA256 = new HashAlgorithms("SHA-256");

    /**
     * The constant presenting the SHA-384 hash algorithm.
     */
    public static final HashAlgorithms SHA384 = new HashAlgorithms("SHA-384");

    /**
     * The constant presenting the SHA-512 hash algorithm.
     */
    public static final HashAlgorithms SHA512 = new HashAlgorithms("SHA-512");
}
