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

        assertEquals(df.digest("f41deedb1d6fe55fe9551531608a1f76"
                + df.digest(df.digest("3790241e7317f5e4ff1dc534d7a40ddb"
                        + "9161ac0f442d7a76bde25745318760aa")
                        + "34d1d4c1b32853020f548dc1df77f158")),
                "9c584dd7465b64a20506311e5c5f306c");
        assertEquals(df.digest("3790241e7317f5e4ff1dc534d7a40ddb"
                + "9161ac0f442d7a76bde25745318760aa"),
                "f31abf63d3528039e783850f25d93232");
        assertEquals(df.digest("34d1d4c1b32853020f548dc1df77f158"
                + "ae2b1fca515949e5d54fb22b8ed95575"),
                "e7bd23e7e08bba33fbebe5d33f20ee62");
        assertEquals(df.digest("3790241e7317f5e4ff1dc534d7a40ddb"
                + "9161ac0f442d7a76bde25745318760aa"),
                "f31abf63d3528039e783850f25d93232");
        assertEquals(df.digest(df.digest(df
                .digest("3790241e7317f5e4ff1dc534d7a40ddb"
                        + "9161ac0f442d7a76bde25745318760aa")
                + df.digest("34d1d4c1b32853020f548dc1df77f158"
                        + "ae2b1fca515949e5d54fb22b8ed95575"))),
                "15a5bf5aad305ba2b52c3ccccb4c2a24");
        assertEquals(df.digest("3790241e7317f5e4ff1dc534d7a40ddb"
                + "9161ac0f442d7a76bde25745318760aa"),
                "f31abf63d3528039e783850f25d93232");
        assertEquals(df.digest("34d1d4c1b32853020f548dc1df77f158"
                + "ae2b1fca515949e5d54fb22b8ed95575"),
                "e7bd23e7e08bba33fbebe5d33f20ee62");
        assertEquals(df.digest(df.digest("3790241e7317f5e4ff1dc534d7a40ddb"
                + "9161ac0f442d7a76bde25745318760aa")
                + df.digest("34d1d4c1b32853020f548dc1df77f158"
                        + "ae2b1fca515949e5d54fb22b8ed95575")),
                "ee85ff5274bc1c2e3611a0b3aa503084");
        assertEquals(df.digest
                  ("15a5bf5aad305ba2b52c3ccccb4c2a24"
                + df.digest
                     (df.digest(
                          df.digest("3790241e7317f5e4ff1dc534d7a40ddb"
                                + "9161ac0f442d7a76bde25745318760aa")
                        + df.digest("34d1d4c1b32853020f548dc1df77f158"
                                + "ae2b1fca515949e5d54fb22b8ed95575"))
                    + "98690380688095842084618061280777398264")),
                "1eb51b38594ecb2302a11b9140238ef3");
    }
}
