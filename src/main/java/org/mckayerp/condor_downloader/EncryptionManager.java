package org.mckayerp.condor_downloader;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

// Taken from https://stackoverflow.com/questions/1132567/encrypt-password-in-configuration-files
public class EncryptionManager
{

    private static SecretKeySpec key = null;

    public EncryptionManager() throws Exception
    {

        // The salt (probably) can be stored along with the encrypted data
        byte[] salt = "12345678".getBytes();
        // Decreasing this speeds down startup time and can be useful during testing, but it also makes it easier for brute force attackers
        int iterationCount = 40000;
        // Other values give me java.security.InvalidKeyException: Illegal key size or default parameters
        int keyLength = 128;
        if (key == null)
        {
            // This isn't very secret as the source will be open. But the user's password to Condor.club will be stored encrypted
            // on their personal machine.  It won't be exposed to casual users, but a malicious power user who has this
            // key could decrypt the password.  I don't want the user to have to enter the password every time
            // they use the program nor do I want to create and manage a key store, so this weak security will have to do.
            key = createSecretKey("SomePassword12345!@".toCharArray(), salt, iterationCount, keyLength);
        }
    }

    private static byte[] base64Decode(String property)
    {
        return Base64.getDecoder().decode(property);
    }

    private SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    public String encrypt(String password) throws GeneralSecurityException
    {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }

    public String decrypt(String encryptedPassword) throws GeneralSecurityException
    {
        String iv = encryptedPassword.split(":")[0];
        String property = encryptedPassword.split(":")[1];
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
        return new String(pbeCipher.doFinal(base64Decode(property)), StandardCharsets.UTF_8);
    }

    private String base64Encode(byte[] bytes)
    {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
