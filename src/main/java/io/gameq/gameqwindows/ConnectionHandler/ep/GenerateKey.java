// $Id$

package io.gameq.gameqwindows.ConnectionHandler.ep;


import io.gameq.gameqwindows.Main;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

public class GenerateKey
{
  public static void generateKey(String path) throws Exception {
    String keyFilename = String.valueOf(path);
    String algorithm = "DES";

    // Generate the key
    SecureRandom sr = new SecureRandom();
    KeyGenerator kg = KeyGenerator.getInstance( algorithm );
    kg.init( sr );
    SecretKey key = kg.generateKey();

    System.out.println(path);
    // Save the raw key bytes in a file
    Util.writeFile(keyFilename, key.getEncoded());
  }
}
