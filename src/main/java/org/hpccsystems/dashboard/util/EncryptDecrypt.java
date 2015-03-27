package org.hpccsystems.dashboard.util;
/**
 * EncryptDecrypt class is responsible for encrypting and decrypting the password.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;

public class EncryptDecrypt {

       private static Cipher dcipher, ecipher;
       
       private static final  Log LOG = LogFactory.getLog(EncryptDecrypt.class);

       // Responsible for setting, initializing this object's encrypter and
       // decrypter Chipher instances
       public EncryptDecrypt(String passPhrase) throws EncryptDecryptException {

              // 8-bytes Salt
              byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
                           (byte) 0x56, (byte) 0x34, (byte) 0xE3, (byte) 0x03 };

              // Iteration count
              int iterationCount = 19;

              try {
                     // Generate a temporary key. In practice, you would save this key
                     // Encrypting with DES Using a Pass Phrase
                     KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt,
                                  iterationCount);
                     SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
                                  .generateSecret(keySpec);

                     ecipher = Cipher.getInstance(key.getAlgorithm());
                     dcipher = Cipher.getInstance(key.getAlgorithm());

                     // Prepare the parameters to the cipthers
                     AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

                     ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
                     dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

              } catch (InvalidAlgorithmParameterException | InvalidKeySpecException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                  LOG.error(Constants.EXCEPTION,e);
                  throw new EncryptDecryptException("EXCEPTION in EncryptDecrypt() "+e.getMessage());
              } 
       }

       // Encrpt Password
       public String encrypt(String str) throws EncryptDecryptException {
              try {
                     // Encode the string into bytes using utf-8
                     byte[] utf8 = str.getBytes("UTF8");
                     // Encrypt
                     byte[] enc = ecipher.doFinal(utf8);
                     // Encode bytes to base64 to get a string
                     return new sun.misc.BASE64Encoder().encode(enc);

              } catch (BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException e) {
                  LOG.error("Exception while Encrypting data"+e);
                  throw new EncryptDecryptException("Exception while Encrypting data "+e.getMessage());
              } 
       }
       // Decrpt password
       // To decrypt the encryted password
    public String decrypt(String str) throws EncryptDecryptException {
        Cipher dcipher = null;
        try {
            byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
                    (byte) 0x56, (byte) 0x34, (byte) 0xE3, (byte) 0x03 };
            int iterationCount = 19;
            String passPhrase = "";
            KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt,
                    iterationCount);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
                    .generateSecret(keySpec);

            dcipher = Cipher.getInstance(key.getAlgorithm());
            // Prepare the parameters to the cipthers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt,
                    iterationCount);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            // Decode base64 to get bytes
            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);
            // Decode using utf-8
            return new String(utf8, "UTF8");
        } catch (InvalidKeySpecException | NoSuchAlgorithmException
                | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IOException
                | IllegalBlockSizeException | BadPaddingException e) {
             LOG.error(Constants.EXCEPTION , e);
            throw new EncryptDecryptException("Exception while Decrypting data" + e.getMessage());
        }
    }
}
