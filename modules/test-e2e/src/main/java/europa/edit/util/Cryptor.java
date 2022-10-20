package europa.edit.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/* 	Author: Satyabrata Das
 * 	Functionality: Password decryptor for security
 */
public class Cryptor {

    private static final String UNICODE_FORMAT = "UTF8";
    private static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private KeySpec ks;
    private SecretKeyFactory skf;
    private Cipher cipher;
    private byte[] arrayBytes;
    private SecretKey key;
    private static final Logger logger = LoggerFactory.getLogger(Cryptor.class);

    public String decrypt(String encryptedString) {
        Configuration config = new Configuration();
        String decryptedText = null;
        String myEncryptionKey = config.getProperty("encKey");
        String myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
        boolean skipDecrypt = Boolean.parseBoolean(config.getProperty("skipDecrypt"));
        if (skipDecrypt) {
            return encryptedString;
        }

        try {
            arrayBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        } catch (UnsupportedEncodingException e1) {
            logger.error(e1.getMessage(), e1);
        }
        try {
            ks = new DESedeKeySpec(arrayBytes);
        } catch (InvalidKeyException e1) {
            logger.error(e1.getMessage(), e1);
        }
        try {
            skf = SecretKeyFactory.getInstance(myEncryptionScheme);
        } catch (NoSuchAlgorithmException e1) {
            logger.error(e1.getMessage(), e1);
        }
        try {
            cipher = Cipher.getInstance(myEncryptionScheme);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
            logger.error(e1.getMessage(), e1);
        }
        try {
            key = skf.generateSecret(ks);
        } catch (InvalidKeySpecException e1) {
            logger.error(e1.getMessage(), e1);
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedText = Base64.decodeBase64(encryptedString);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText = new String(plainText);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return decryptedText;
    }
}