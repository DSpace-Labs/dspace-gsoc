package org.dspace.cis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.content.*;
//import org.dspace.core.ConfigurationManager;
import org.dspace.core.Utils;

/**
 * a digest method factory including a number of over-load functions making
 * hash-values the input of the function <code>digest</code> could be one of
 * these:
 * <li>String</li>
 * <li>Bitstream</li>
 * <li>Bundle</li>
 * <li>Bundle[]</li>
 * <li>DCValue</li>
 * <li>DCValue[]</li>
 * <li>Item</li>
 * 
 * @author Wang Jiahui
 * 
 */
public class DigestFactory
{
    /**
     * The log instance.
     */
    private static final Logger logger = Logger.getLogger(DigestFactory.class);

    /**
     * The MessageDigest object.
     */
    private MessageDigest md;

    /**
     * The HashAlgorithms instance.
     */
    private HashAlgorithms PRIMITIVE;

    /**
     * The constructor with no argument.
     *
     */
    public DigestFactory()
    {
        // the primitive is set to SHA256 by default
        try
        {
            PRIMITIVE = HashAlgorithms.SHA256;
            md = MessageDigest.getInstance(PRIMITIVE.toString());
        }
        catch (NoSuchAlgorithmException e)
        {
            logger
                    .error("No such algorithm supported when generate MessegeDigest object");
        }
    }

    /**
     * The constructor with a hashAlgorithm argument.
     * @param ha the hashAlgorithm object
     */
    public DigestFactory(HashAlgorithms ha)
    {
        PRIMITIVE = ha;
        try
        {
            md = MessageDigest.getInstance(PRIMITIVE.toString());
        }
        catch (NoSuchAlgorithmException e)
        {
            logger
                    .error("No such algorithm supported when generate MessegeDigest object");
        }
    }

    /**
     * Digest a <code>String</code>.
     * @param string the String
     * @return the hashvalue of a string
     */
    public String digest(String string)
    {
        byte[] fromString = string.getBytes();
        byte[] hash = md.digest(fromString);
        return Utils.toHex(hash);
    }

    /**
     * Digest a bitstream.
     * @param bitstream the bitstream
     * @return Hex format of the hash value of the bitstream or null if
     *         something wrong
     * @see org.dspace.bitstore.BitstreamStoreManager
     */
    public String digest(Bitstream bitstream)
    {

        String filePath = CisUtils.getBitstreamFilePath(bitstream);
        filePath += bitstream.getInternalID();

        return digestWithFilePath(filePath);

    }

    /**
     * Digest a file with file path.
     * @param filePath the file path
     * @return the hashvalue of this the given file
     */
    private String digestWithFilePath(String filePath)
    {
        File assetstore = new File(filePath);

        if (assetstore == null || !assetstore.exists())
        {
            logger.error("Local file not exists or wrong file path!");
            return null;
        }

        InputStream is = null;
        try
        {
            is = new FileInputStream(assetstore);
        }
        catch (FileNotFoundException e)
        {
            logger.error("can't create inputstream with this file:"
                    + assetstore.toString());
            return null;
        }

        // Read through a digest input stream
        DigestInputStream dis = null;

        dis = new DigestInputStream(is, md);

        final int BUFFER_SIZE = 1024 * 4;
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (true)
        {
            try
            {
                if (dis.read(buffer, 0, BUFFER_SIZE) == -1)
                {
                    break;
                }
            }
            catch (IOException e)
            {
                logger.error("IOException when read to buffer");
                return null;
            }
        }

        // the hash value of the bitstream
        String resultHash = Utils.toHex(dis.getMessageDigest().digest());
        return resultHash;
    }

    /**
     * the procedure of the hundle's hash value generation could be described
     * like this:
     * <li>get bitstreams of this bundle</li>
     * <li>digest each bitstream</li>
     * <li>combine the hash balues to a temporary string</li>
     * <li>digest the tempt string as the return value</li>
     * 
     * @param bundle the bundle
     * @return hash value of a givin bundle
     */
    public String digest(Bundle bundle)
    {
        Bitstream[] bitstreams = bundle.getBitstreams();

        String tmp = "";

        for (int i = 0; i < bitstreams.length; i++)
        {
            tmp += digest(bitstreams[i]);
        }

        return digest(tmp);
    }

    /**
     * Digest an array of bundles.
     * @param bundles the bundles
     * @return the hashvalue of bundles
     */
    public String digest(Bundle[] bundles)
    {
        String tmp = "";

        for (int i = 0; i < bundles.length; i++)
        {
            tmp += digest(bundles[i]);
        }

        return digest(tmp);
    }

    /**
     * the procedure of a DCValue's hash value generation just combine it's
     * properties into a temporary string and digest it as the return value
     * 
     * @param dcvalue the dcvalue
     * @return hash value of a DCValue
     */
    public String digest(DCValue dcvalue)
    {
        String tmp = dcvalue.schema + dcvalue.element + dcvalue.qualifier
                + dcvalue.language + dcvalue.value;
        return digest(tmp);
    }

    /**
     * Digest an array of dcvalues.
     * @param dcvalues the dcvalues
     * @return hash value of a DCValue array
     */
    public String digest(DCValue[] dcvalues)
    {
        String tmp = "";

        for (int i = 0; i < dcvalues.length; i++)
        {
            tmp += digest(dcvalues[i]);
        }

        return digest(tmp);
    }

    /**
     * Digest an item.
     * @param item the item
     * @return the hashvalue of the item
     */
    public String digest(Item item)
    {
        // get all dcvalues of the item
        DCValue[] dcvalues = item.getMetadata(Item.ANY, Item.ANY, Item.ANY,
                Item.ANY);

        // get all non-certificate bundles of the item
        Bundle[] bundles = null;
        
        // Sort the dcvalues by their properties.
        Arrays.sort(dcvalues);
        
        try
        {
            bundles = item.getBundlesWithoutCertificate();
            // Sort the bundles by their names.
            Arrays.sort(bundles);
        }
        catch (SQLException e)
        {
            logger.error("SQLException when get item's bundles!");
            return null;
        }

        String tmp = digest(dcvalues) + digest(bundles);

        return digest(tmp);

    }

    /**
     * Get the hashAlgorithm of this factory.
     * @return the hashAlgorithm
     */
    public HashAlgorithms getPRIMITIVE()
    {
        return PRIMITIVE;
    }

    /**
     * Set the hashAlgorithm.
     * @param primitive the hashAlgorithm
     */
    public void setPRIMITIVE(HashAlgorithms primitive)
    {
        PRIMITIVE = primitive;
    }

}
