package org.dspace.cis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Utils;
/**
 * a digest method factory
 * including a number of over-load functions making hash-values
 * the input of the function <code>digest</code> could be one of these:
 * <li>String</li>
 * <li>Bitstream</li>
 * <li>Bundle</li>
 * <li>Bundle[]</li>
 * <li>DCValue</li>
 * <li>DCValue[]</li>
 * <li>Item</li>
 * @author Jiahui Wang
 *
 */
public class DigestFactory
{
    private static final Logger logger = Logger.getLogger(DigestFactory.class);

    private MessageDigest md;

    private HashAlgorithms PRIMITIVE;

    public DigestFactory()
    {
        // the primitive is set to md5 by default
        try
        {
            PRIMITIVE = HashAlgorithms.MD5;
            md = MessageDigest.getInstance(PRIMITIVE.toString());
        }
        catch (NoSuchAlgorithmException e)
        {
            logger
                    .error("No such algorithm supported when generate MessegeDigest object");
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

    public String digest(String string)
    {
        byte[] fromString = string.getBytes();
        byte[] hash = md.digest(fromString);
        return Utils.toHex(hash);
    }

    /**
     * reference to org.dspace.bitstore.BitstreamStoreManager.java
     * 
     * @param bitstream
     * @return Hex format of the hash value of the bitstream or null if
     *         something wrong
     */
    public String digest(Bitstream bitstream)
    {

        String filePath = getBitstreamFilePath(bitstream);

        return digestWithFilePath(filePath);

    }

    // should be private
    protected String digestWithFilePath(String filePath)
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
            e.printStackTrace();
            return null;
        }

        // Read through a digest input stream that will work out the MD5
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
                e.printStackTrace();
                return null;
            }
        }

        // the hash value of the bitstream
        String resultHash = Utils.toHex(dis.getMessageDigest().digest());
        return resultHash;
        // return intermediatePath;
    }

    /**
     * 
     * @param bitstream
     *            the input bitstream
     * @return file's path that the bitstream represents
     */
    private String getBitstreamFilePath(Bitstream bitstream)
    {
        String sAssetstoreDir;
        // Get the store to use
        int storeNumber = bitstream.getStoreNumber();

        // Default to zero ('assetstore.dir') for backwards compatibility
        if (storeNumber == -1)
        {
            storeNumber = 0;
        }
        if (storeNumber == 0)
        {
            // 'assetstore.dir' is always store number 0
            sAssetstoreDir = ConfigurationManager.getProperty("assetstore.dir");
            // else backup store numbers
        }
        else
        {
            sAssetstoreDir = ConfigurationManager.getProperty("assetstore.dir"
                    + "." + (new Integer(storeNumber)).toString());
        }

        String intermediatePath = getIntermediatePath(bitstream.getInternalID());

        StringBuffer bufFilename = new StringBuffer();
        // maybe this will cause some problem with the file separator
        bufFilename.append(sAssetstoreDir);
        bufFilename.append(File.separator);
        bufFilename.append(intermediatePath);

        return bufFilename.toString();

    }

    /**
     * Return the intermediate path derived from the internal_id. This method
     * splits the id into groups which become subdirectories.
     * 
     * @param iInternalId
     *            The internal_id
     * @return The path based on the id without leading or trailing separators
     */
    private static String getIntermediatePath(String iInternalId)
    {
        // These settings control the way an identifier is hashed into
        // directory and file names
        //
        // With digitsPerLevel 2 and directoryLevels 3, an identifier
        // like 12345678901234567890 turns into the relative name
        // /12/34/56/12345678901234567890.
        //
        // You should not change these settings if you have data in the
        // asset store, as the BitstreamStorageManager will be unable
        // to find your existing data.
        int digitsPerLevel = 2;

        int directoryLevels = 3;

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < directoryLevels; i++)
        {
            int digits = i * digitsPerLevel;
            if (i > 0)
            {
                buf.append(File.separator);
            }
            buf.append(iInternalId.substring(digits, digits + digitsPerLevel));
        }
        buf.append(File.separator);
        return buf.toString();
    }

    /**
     * the procedure of the hundle's hash value generation could be described
     * like this:
     * <li>get bitstreams of this bundle</li>
     * <li>digest each bitstream</li>
     * <li>combine the hash balues to a temporary string</li>
     * <li>digest the tempt string as the return value</li>
     * 
     * @param bundle
     * @return hash value of a givin bundle
     */
    public String digest(Bundle bundle)
    {
        Bitstream[] bitstreams = bundle.getBitstreams();

        String tmp = null;

        for (int i = 0; i < bitstreams.length; i++)
        {
            tmp += digest(bitstreams[i]);
        }

        return digest(tmp);
    }

    public String digest(Bundle[] bundles)
    {
        String tmp = null;

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
     * @param dcvalue
     * @return hash value of a DCValue
     */
    public String digest(DCValue dcvalue)
    {
        String tmp = dcvalue.schema + dcvalue.element + dcvalue.qualifier
                + dcvalue.language + dcvalue.value;
        return digest(tmp);
    }

    /**
     * @param dcvalues
     * @return hash value of a DCValue array
     */
    public String digest(DCValue[] dcvalues)
    {
        String tmp = null;

        for (int i = 0; i < dcvalues.length; i++)
        {
            tmp += digest(dcvalues[i]);
        }

        return digest(tmp);
    }

    public String digest(Item item)
    {
        // get all dcvalues of the item
        DCValue[] dcvalues = item.getMetadata(Item.ANY, Item.ANY, Item.ANY,
                Item.ANY);

        // get all bundles of the item
        Bundle[] bundles = null;
        try
        {
            bundles = item.getBundles();
        }
        catch (SQLException e)
        {
            logger.error("SQLException when get item's bundles!");
            e.printStackTrace();
            return null;
        }

        String tmp = null;

        tmp = digest(dcvalues) + digest(bundles);

        return digest(tmp);

    }
    // public static void main(String[] argv){
    //         
    // DigestFactory df = new DigestFactory();
    // String resu =
    // df.digestWithFilePath("D:\\dspace\\assetstore\\10\\42\\03\\104203510823337032602227731452764060832");
    // System.out.println(resu);
    //     
    // }

}
