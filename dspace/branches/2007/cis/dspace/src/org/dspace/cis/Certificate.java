package org.dspace.cis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dspace.content.Bitstream;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * A Certificate should like this:
 * <code>[t; (y4, R), (H12, L), (W{i-1}, L)]</code> where <code>t</code>
 * indicates the time when item is submitted to the repository
 * <code>(y4, R)</code>, <code>(H12, L)</code> and <code>(W{i-1}, L)</code>
 * are all assistant hash values to generate the putative witness. The putative
 * witness will be compared to the published witness to determine whether the
 * certificate is validate
 * <p>
 * A assistant hash contains two parts: the hash-value and the position
 * information (which could be L(left) or R(right)). The hash-value could be a
 * single Item's(like <code>y4</code>), a catenated hash value(like
 * <code>H12</code>) or a witness (like <code>W{i-1}</code>)
 * 
 * @author Wang Jiahui
 * 
 */
public class Certificate extends Bitstream
{
    /** Algorithm name for this certificate. */
    private HashAlgorithms algorithm;

    /** Handle of the item this certificate belongs to. */
    private String handle;

    /** List of witnesses of this certificate. */
    private List witnesses = null;

    // /** Starting time of this certificate's time interval. */
    // public Date from;
    //
    // /** Ending time of this certificate's time interval. */
    // public Date to;

    /** Digest time of the item this certificate belongs to. */
    public Date lastModifiedTime;

    public Certificate(Context bContext, TableRow bRow) throws SQLException
    {
        super(bContext, bRow);
        witnesses = new ArrayList();
    }

    // public Certificate() {
    // setInterval();
    // this.internalID = Utils.generateKey();
    // witnesses = new ArrayList<AssistHash>();
    // }

    public HashAlgorithms getAlgorithm()
    {
        return algorithm;
    }

    public String getAlgorithmName()
    {
        return algorithm.toString();
    }

    public void setAlgorithm(String algorithm)
    {
        if (algorithm.equals("MD2"))
            this.algorithm = HashAlgorithms.MD2;
        else if (algorithm.equals("SHA-1"))
            this.algorithm = HashAlgorithms.SHA1;
        else if (algorithm.equals("SHA-256"))
            this.algorithm = HashAlgorithms.SHA256;
        else if (algorithm.equals("SHA-384"))
            this.algorithm = HashAlgorithms.SHA384;
        else if (algorithm.equals("SHA-512"))
            this.algorithm = HashAlgorithms.SHA512;
        else
            this.algorithm = HashAlgorithms.MD5;
    }

    public void setHandle(String handle)
    {
        this.handle = handle;
    }

    public String getHandle()
    {
        return handle;
    }

    // public Date getFrom() {
    // return from;
    // }
    //
    // public void setFrom(Date from) {
    // this.from = from;
    // }
    //
    // public Date getTo() {
    // return to;
    // }
    //
    // public void setTo(Date to) {
    // this.to = to;
    // }

    public Date getLastModifiedTime()
    {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date time)
    {
        this.lastModifiedTime = time;
    }

    public void addWitness(AssistHash witness)
    {
        witnesses.add(witness);
    }

    public AssistHash[] getWitnesses()
    {
        // return (AssistHash[]) witnesses.toArray();
        AssistHash[] wit = new AssistHash[this.witnesses.size()];
        for (int i = 0; i < witnesses.size(); i++)
        {
            wit[i] = (AssistHash) this.witnesses.get(i);
        }
        return wit;
    }

    /**
     * Reverse the witnesses.
     * 
     */
    public void reverse()
    {
        Collections.reverse(witnesses);
    }

    /**
     * Write this certificate in a .xml file and save it in the file system.
     * It's path is derived from the internal_id. With digitsPerLevel 2 and
     * directoryLevels 3, an internal_id like 12345678901234567890 turns into
     * the relative path /12/34/56/12345678901234567890.
     * <p>
     * It should be like this:
     * <p>
     * &lt;certificate algorithm="MD5" handle="123456789/12"&gt;
     * <p>
     * &lt;time-interval&gt;
     * <p>
     * &lt;from&gt;
     * <p>
     * Sat May 26 20:00:00 CST 2007
     * <p>
     * &lt;/from&gt;
     * <p>
     * &lt;to&gt;
     * <p>
     * Sat May 26 21:00:00 CST 2007
     * <p>
     * &lt;/to&gt;
     * <p>
     * &lt;/time-interval&gt;
     * <p>
     * &lt;witness position="LEFT"&gt;
     * <p>
     * 9acbebc7935a2ce2a22dfe17cb4843f9
     * <p>
     * &lt;/witness&gt;
     * <p>
     * &lt;witness position="RIGHT"&gt;
     * <p>
     * a8d4be2798d7e6e85acc413f59769485
     * <p>
     * &lt;/witness&gt;
     * <p>
     * &lt;witness position="LEFT"&gt;
     * <p>
     * 221eb4c1c25de70b4f36c60cf0a7f8f5
     * <p>
     * &lt;/witness&gt;
     * <p>
     * &lt;/certificate&gt;
     * 
     * @throws IOException
     */
    public void archive() throws IOException
    {
        Document doc = new Document();
        Element root = new Element("certificate");
        root.setAttribute("algorithm", this.algorithm.toString());
        root.setAttribute("handle", this.handle);
        Element digestTime = new Element("LastModifiedTime");
        digestTime.addContent(this.lastModifiedTime.toString());
        root.addContent(digestTime);
        Iterator it = this.witnesses.iterator();
        while (it.hasNext())
        {
            AssistHash wit = (AssistHash) it.next();
            root.addContent(new Element("witness").setAttribute("position",
                    wit.getPos().toString()).addContent(wit.getHashvalue()));
        }
        doc.addContent(root);
        doc.setRootElement(root);

        // transform the Document object to .xml file
        XMLOutputter outputter = new XMLOutputter();
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        outputter.setFormat(format);
        String destPath = CisUtils.getBitstreamFilePath(this);
        // Make ths directory for certificate
        new File(destPath).mkdirs();
        FileWriter writer = new FileWriter(destPath + File.separator
                + this.getInternalID());
        outputter.output(doc, writer);
        writer.close();
    }

    /**
     * Instantiate a <code>Certificate</code> instance by reading a
     * certificate file.
     * 
     * @param path
     *            the certificate file's path
     * @param context
     *            the context
     * @return the <code>Certificate</code> instance
     * @throws FileNotFoundException
     * @throws JDOMException
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     */
    public static Certificate readFile(String path, Context context)
            throws FileNotFoundException, JDOMException, IOException,
            SQLException, ParseException
    {
        // Get the certificate instance responding to the file.
        String internal_id = (new File(path)).getName();
        TableRow tr = DatabaseManager.findByUnique(context, "bitstream",
                "internal_id", internal_id);
        Certificate cer = new Certificate(context, tr);

        // Prepare to read the certificate file (xml document).
        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(new FileInputStream(path));
        Element root = doc.getRootElement();
        // Set handle and algorithm
        cer.setHandle(root.getAttributeValue("handle"));
        cer.setAlgorithm(root.getAttributeValue("algorithm"));
        // Set last modified time.
        DateFormat df = DateFormat.getDateTimeInstance();
        cer
                .setLastModifiedTime(df.parse(root
                        .getChildText("LastModifiedTime")));
        // Set witness hash values.
        List elements = root.getChildren("witness");
        String pos = null;
        for (int i = 0; i < elements.size(); i++)
        {
            pos = ((Element) elements.get(i)).getAttributeValue("position");
            if (pos.equals("LEFT"))
            {
                cer.addWitness(new AssistHash(((Element) elements.get(i))
                        .getText(), AssistHashPos.LEFT));
            }
            else
            {
                cer.addWitness(new AssistHash(((Element) elements.get(i))
                        .getText(), AssistHashPos.RIGHT));
            }
        }
        return cer;
    }

    // public static void main(String[] argv) throws IOException {
    // File file = new
    // File("/dspace/assetstore/36/47/31/36473117764695350162253083493927900144");
    // if (!file.exists())
    // {
    // System.out.println("The path is wrong");
    // return;
    // }
    // System.out.println("The file's name is" + file.getName());
    // SAXBuilder sb = new SAXBuilder();
    // Document doc;
    // try
    // {
    // doc = sb.build(new FileInputStream(file));
    // Element root = doc.getRootElement();
    // DateFormat df = DateFormat.getDateTimeInstance();
    // System.out.println(root.getChildText("LastModifiedTime"));
    // Date date = df.parse(root.getChildText("LastModifiedTime"));
    // System.out.println(date);
    // List elements = root.getChildren("witness");
    // AssistHash ah = null;
    // List witnesses = new ArrayList();
    // for (int i = 0; i < elements.size(); i++)
    // {
    // String pos = ((Element)elements.get(i)).getAttributeValue("position");
    // if (pos.equals("LEFT"))
    // {
    // ah = new AssistHash(((Element)elements.get(i)).getText(),
    // AssistHashPos.LEFT);
    // } else {
    // ah = new AssistHash(((Element)elements.get(i)).getText(),
    // AssistHashPos.RIGHT);
    // }
    // witnesses.add(ah);
    // }
    // for (int i = 0; i < witnesses.size(); i++)
    // {
    // System.out.println(((AssistHash)witnesses.get(i)).getPos().toString() +
    // ":" + ((AssistHash)witnesses.get(i)).getHashvalue());
    // }
    // }
    // catch (Exception e)
    // {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // Document doc = new Document();
    // Element root = new Element("certificate");
    // root.setAttribute("algorithm", "MD5");
    // root.setAttribute("handle", "123456789/12");
    // Element timeInterval = new Element("time-interval");
    // Element from = new Element("from");
    // from.addContent("Sat May 26 20:00:00 CST 2007");
    // Element to = new Element("to");
    // to.addContent("Sat May 26 21:00:00 CST 2007");
    // timeInterval.addContent(from);
    // timeInterval.addContent(to);
    // root.addContent(timeInterval);
    // int i = 0;
    // while (i < 3) {
    // root.addContent(new Element("witness").setAttribute("position",
    // "LEFT").addContent("9acbebc7935a2ce2a22dfe17cb4843f9"));
    // i++;
    // }
    //
    // doc.addContent(root);
    // doc.setRootElement(root);
    //
    // XMLOutputter outputter = new XMLOutputter();
    // Format format = Format.getPrettyFormat();
    // format.setEncoding("UTF-8");
    // outputter.setFormat(format);
    // FileWriter writer = new FileWriter(new File("d://text.xml"));
    // outputter.output(doc, writer);
    // writer.close();

    // Calendar c = Calendar.getInstance();
    // int year = c.get(Calendar.YEAR);
    // int month = c.get(Calendar.MONTH);
    // int date = c.get(Calendar.DAY_OF_MONTH);
    // int hour = c.get(Calendar.HOUR_OF_DAY);
    //        
    // c.set(year, month, date, hour, 0, 0);
    // Date from = c.getTime();
    //        
    // long miliSeconds = from.getTime();
    // miliSeconds += 3600000;
    //      
    // Date to = new Date(miliSeconds);
    //        
    // System.out.println(from);
    // System.out.println(to);
    // }

}
