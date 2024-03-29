package kr.co.ibl.util;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.EncoderException;

public class EncryptUtil {
	
	  public static String alg = "AES/CBC/PKCS5Padding";
	  private final String key = "01234567890123456789012345678901";
	  private final String iv = key.substring(0, 16); // 16byte

	  public String encrypt(String text) throws Exception {
		  
	        Cipher cipher = Cipher.getInstance(alg);
	        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
	        IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
	        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

	        byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));
	        return Base64.getEncoder().encodeToString(encrypted);
	  }

	  public String decrypt(String cipherText) throws Exception {
		  
	        Cipher cipher = Cipher.getInstance(alg);
	        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
	        IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
	        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);

	        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
	        byte[] decrypted = cipher.doFinal(decodedBytes);
	        return new String(decrypted, "UTF-8");
	  }
	    
	 static HashMap<String, String> createKeypairAsString() {
	        HashMap<String, String> stringKeypair = new HashMap<String,String>();
	        
	        try {
	            SecureRandom secureRandom = new SecureRandom();
	            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	            keyPairGenerator.initialize(1024, secureRandom);
	            KeyPair keyPair = keyPairGenerator.genKeyPair();

	            PublicKey publicKey = keyPair.getPublic();
	            PrivateKey privateKey = keyPair.getPrivate();

	            
	            String stringPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
	            String stringPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
	            
	            stringKeypair.put("publicKey", stringPublicKey);
	            stringKeypair.put("privateKey", stringPrivateKey);

	        } catch (NoSuchAlgorithmException e) {
	            
	        } catch(Exception e){
	        	
	        }
	        return stringKeypair;
	    }

	    /**
	     * 암호화
	     */
	    static String encode(String plainData, String stringPublicKey) {
	        String encryptedData = null;

	        try {
	            //평문으로 전달받은 공개키를 공개키객체로 만드는 과정
	            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	            byte[] bytePublicKey = Base64.getDecoder().decode(stringPublicKey.getBytes());
	            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytePublicKey);
	            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);


	            //만들어진 공개키객체를 기반으로 암호화모드로 설정하는 과정

	            Cipher cipher = Cipher.getInstance("RSA");
	            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

	            //평문을 암호화하는 과정
	            byte[] byteEncryptedData = cipher.doFinal(plainData.getBytes());
	            encryptedData = Base64.getEncoder().encodeToString(byteEncryptedData);

	        } catch (NoSuchAlgorithmException e) {
	            
	        } catch(Exception e){
	        	
	        }
	        return encryptedData;
	    }

	    /**
	     * 복호화
	     */
	    static String decode(String encryptedData, String stringPrivateKey) {

	        String decryptedData = null;
	        try {
	            //평문으로 전달받은 개인키를 개인키객체로 만드는 과정

	            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	            byte[] bytePrivateKey = Base64.getDecoder().decode(stringPrivateKey.getBytes());
	            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytePrivateKey);
	            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);


	            //만들어진 개인키객체를 기반으로 암호화모드로 설정하는 과정
	            Cipher cipher = Cipher.getInstance("RSA");
	            cipher.init(Cipher.DECRYPT_MODE, privateKey);

	            //암호문을 평문화하는 과정

	            byte[] byteEncryptedData = Base64.getDecoder().decode(encryptedData.getBytes());
	            byte[] byteDecryptedData = cipher.doFinal(byteEncryptedData);
	            decryptedData = new String(byteDecryptedData);

	        } catch (NoSuchAlgorithmException e) {
	            
	        } catch(Exception e){
	        	
	        }        
	        return decryptedData;
	    }

}
