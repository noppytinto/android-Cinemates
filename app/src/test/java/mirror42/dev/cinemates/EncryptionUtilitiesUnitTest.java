package mirror42.dev.cinemates;

import org.junit.Test;

import mirror42.dev.cinemates.utilities.MyUtilities;
import static org.junit.Assert.*;

public class EncryptionUtilitiesUnitTest {

    @Test
    public void test_SHA256encrypt_with_singleChar() {
        String testValue = "a";
        String expected = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb";
        String actual = MyUtilities.SHA256encrypt(testValue);

        assertEquals(expected, actual);
    }

    @Test
    public void test_SHA256encrypt_with_alphas() {
        String testValue = "erkjughneojurnhejo";
        String expected = "9375ab31e4734c2473dc748ad2adf649d1782e43f0741943deae52b31c807a88";
        String actual = MyUtilities.SHA256encrypt(testValue);

        assertEquals(expected, actual);
    }

    @Test
    public void test_SHA256encrypt_with_emptyString() {
        String testValue = "";
        String expected = null;
        String actual = MyUtilities.SHA256encrypt(testValue);

        assertEquals(expected, actual);
    }

    @Test
    public void test_SHA256encrypt_with_null() {
        String testValue = null;
        String actual = MyUtilities.SHA256encrypt(testValue);

        assertNull(actual);
    }

    @Test
    public void test_SHA256encrypt_with_onlyNumbers() {
        String testValue = "123";
        String expected = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";
        String actual = MyUtilities.SHA256encrypt(testValue);

        assertEquals(expected, actual);
    }

    @Test
    public void test_SHA256encrypt_with_alphaNum() {
        String testValue = "123abc";
        String expected = "dd130a849d7b29e5541b05d2f7f86a4acd4f1ec598c1c9438783f56bc4f0ff80";
        String actual = MyUtilities.SHA256encrypt(testValue);

        assertEquals(expected, actual);
    }

    @Test
    public void test_SHA256encrypt_with_onlySymbols() {
        String testValue = "?!@#$";
        String expected = "9d96e93239c6c5d354fa4e482ed21d7ab3ec8893fdadae2a81a0ffa2c7167d9e";
        String actual = MyUtilities.SHA256encrypt(testValue);

        assertEquals(expected, actual);
    }

    @Test
    public void test_SHA256encrypt_with_mixedCharacters() {
        String testValue = "?!bgrdt@wr5881ty#$";
        String expected = "b2120ec41a90feb5ad36ab6442633d4dd342dbae546ef20b58e5f87e51edba86";
        String actual = MyUtilities.SHA256encrypt(testValue);

        assertEquals(expected, actual);
    }


}
