// $Id$

package io.gameq.gameqwindows.ConnectionHandler.ep;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

public class GenerateKey
{
  static public void main( String args[] ) throws Exception {
    String keyFilename = "C:/Users/fabianwikstrom/IdeaProjects/TestEnc5/src/key";
    String algorithm = "DES";

    // Generate the key
    SecureRandom sr = new SecureRandom();
    KeyGenerator kg = KeyGenerator.getInstance( algorithm );
    kg.init( sr );
    SecretKey key = kg.generateKey();

    // Save the raw key bytes in a file
    Util.writeFile( keyFilename, key.getEncoded() );
  }
}
