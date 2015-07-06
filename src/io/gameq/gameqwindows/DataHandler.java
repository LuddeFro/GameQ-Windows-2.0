package io.gameq.gameqwindows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by fabianwikstrom on 7/6/2015.
 */
public class DataHandler {
    private static DataHandler instance = null;
    protected DataHandler() {
        // Exists only to defeat instantiation.
    }
    public static DataHandler getInstance() {
        if(instance == null) {
            instance = new DataHandler();
        }
        return instance;
    }

    private String folderName = "";

    public void logPacket(Packet[] packetQueue){

        String toSave = "";

        for (Packet p : packetQueue){
            toSave = toSave + p.getSrcPort() + "," + p.getDstPort() + "," + p.getPacketLength() + "," + p.getCaptureTime() + "," + "\n";
        }

        File file = new File("C:\\Users\\fabianwikstrom\\Desktop\\GameQ");
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();

        String filename = dateFormat.format((date));
        filename.replace(" ","");

        String saveFile = "C:\\Users\\fabianwikstrom\\Desktop\\GameQ\\" + folderName + "\\" + filename + ".csv";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(saveFile), "utf-8"))) {
            writer.write(toSave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
