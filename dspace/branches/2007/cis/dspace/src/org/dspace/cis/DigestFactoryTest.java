package org.dspace.cis;

import static org.junit.Assert.*;

import org.junit.Test;

public class DigestFactoryTest
{

    @Test
    public void testDigest()
    {
        DigestFactory df = new DigestFactory();

        assertEquals(df.digest("testing"), "ae2b1fca515949e5d54fb22b8ed95575");
        assertEquals(df.digest("another testing"),
                "ce8448a77261aea4b6757adff7c59593");
        assertEquals(df
                .digestWithFilePath("D:\\dspace\\assetstore\\10\\19\\89\\"
                        + "101989526470866796896216773458319237289"),
                "9acbebc7935a2ce2a22dfe17cb4843f9");
        assertEquals(df
                .digestWithFilePath("D:\\dspace\\assetstore\\10\\69\\92\\"
                        + "106992281149159398488582827159281833310"),
                "a8d4be2798d7e6e85acc413f59769485");
        assertEquals(df
                .digestWithFilePath("D:\\dspace\\assetstore\\98\\69\\03\\"
                        + "98690380688095842084618061280777398264"),
                "221eb4c1c25de70b4f36c60cf0a7f8f5");
    }
}
