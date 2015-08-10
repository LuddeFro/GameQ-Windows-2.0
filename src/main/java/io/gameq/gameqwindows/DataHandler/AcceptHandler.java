package io.gameq.gameqwindows.DataHandler;

import io.gameq.gameqwindows.Structs.Encoding;
import io.gameq.gameqwindows.Structs.Game;

import java.io.*;
import java.net.URL;

/**
 * Created by Ludvig on 14/07/15.
 * Copyright GameQ AB 2015
 */
public final class AcceptHandler {

    public static void acceptMatch(int game) {
        if(Encoding.getGameFromInt(game) == Game.HoN || Encoding.getGameFromInt(game) == Game.HoTS) {
            //do nothing
        } else {
            String localFilename = "";
            if(Encoding.getGameFromInt(game) == Game.Dota2) {
                if (isDotaReborn()) {
                    localFilename = "DotaReborn.exe";
                } else {
                    localFilename = "Dota.exe";
                }
            } else if(Encoding.getGameFromInt(game) == Game.LoL) {
                localFilename = "LoL.exe";
            } else if(Encoding.getGameFromInt(game) == Game.CSGO) {
                localFilename = "CSGO.exe";
            }

            File tempFile = null;
            try {
                tempFile = File.createTempFile(Long.toString(System.currentTimeMillis()), localFilename);
                tempFile.deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }


            URL url = AcceptHandler.class.getClassLoader().getResource("executables/"+localFilename);
            FileOutputStream output = null;

            try {
                if (tempFile != null) {
                    output = new FileOutputStream(tempFile);
                    InputStream input;
                    if (url != null) {
                        input = url.openStream();
                        byte [] buffer = new byte[4096];
                        int bytesRead = input.read(buffer);
                        while (bytesRead != -1) {
                            output.write(buffer, 0, bytesRead);
                            bytesRead = input.read(buffer);
                        }
                        output.close();
                        input.close();
                    }
                }
                if(tempFile != null) {
                    Runtime.getRuntime().exec(tempFile.getAbsolutePath(), null, new File(tempFile.getParent()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isDotaReborn() {
        try {
            String line;
            Process p = Runtime.getRuntime().exec
                    (System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {

                if(line.contains("dota2.exe")){
                    input.close();
                    return true;
                } else if (line.contains("dota.exe")){
                    input.close();
                    return false;
                }
            }
            input.close();
            return false;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return false;
    }
}