// $Id$

package io.gameq.gameqwindows.ConnectionHandler.ep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util
{
  // Read a file into a byte array
  static public byte[] readFile( String filename ) throws IOException {
    File file = new File( filename );
    long len = file.length();
    byte data[] = new byte[(int)len];
    FileInputStream fin = new FileInputStream( file );
    int r = fin.read( data );
    if (r != len)
      throw new IOException( "Only read "+r+" of "+len+" for "+file );
    fin.close();
    return data;
  }

  // Write byte array to a file
  static public void writeFile( String filename, byte data[] )
      throws IOException {
    FileOutputStream fout = new FileOutputStream( filename );
    fout.write( data );
    fout.close();
  }
}
