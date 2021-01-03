package mirror42.dev.cinemates;

import org.junit.Test;

import mirror42.dev.cinemates.utilities.MyUtilities;
import static org.junit.Assert.*;

public class EncryptionUtilitiesUnitTest {

    @Test
    public void SHAvalue_isValid_a() {
        String provided = "a";
        String expected = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb";
        String actual = MyUtilities.SHAencrypt(provided);

        assertEquals(expected, actual);
    }

    @Test
    public void SHAvalue_isValid_bbb() {
        String provided = "bbb";
        String expected = "3e744b9dc39389baf0c5a0660589b8402f3dbb49b89b3e75f2c9355852a3c677";
        String actual = MyUtilities.SHAencrypt(provided);

        assertEquals(expected, actual);
    }

    @Test
    public void SHAvalue_isValid_emptyString() {
        String provided = "";
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        String actual = MyUtilities.SHAencrypt(provided);

        assertEquals(expected, actual);
    }

    @Test
    public void SHAvalue_isValid_onlyNumbers() {
        String provided = "123";
        String expected = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";
        String actual = MyUtilities.SHAencrypt(provided);

        assertEquals(expected, actual);
    }

    @Test
    public void SHAvalue_isValid_alphaNum() {
        String provided = "123abc";
        String expected = "dd130a849d7b29e5541b05d2f7f86a4acd4f1ec598c1c9438783f56bc4f0ff80";
        String actual = MyUtilities.SHAencrypt(provided);

        assertEquals(expected, actual);
    }

    @Test
    public void SHAvalue_isValid_onlySymbols() {
        String provided = "?!@#$";
        String expected = "9d96e93239c6c5d354fa4e482ed21d7ab3ec8893fdadae2a81a0ffa2c7167d9e";
        String actual = MyUtilities.SHAencrypt(provided);

        assertEquals(expected, actual);
    }

    @Test
    public void SHAvalue_isValid_mixed() {
        String provided = "?!bgrdt@wr5881ty#$";
        String expected = "b2120ec41a90feb5ad36ab6442633d4dd342dbae546ef20b58e5f87e51edba86";
        String actual = MyUtilities.SHAencrypt(provided);

        assertEquals(expected, actual);
    }

    @Test
    public void SHAvalue_isValid_nullString() {
        String provided = null;
        String expected = null;
        String actual = MyUtilities.SHAencrypt(provided);

        assertEquals(expected, actual);
    }

}
