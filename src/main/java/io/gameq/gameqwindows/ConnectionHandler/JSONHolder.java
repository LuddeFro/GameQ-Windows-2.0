package io.gameq.gameqwindows.ConnectionHandler;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by Ludvig on 15/07/15.
 */
public class JSONHolder {
    boolean success;
    String error;
    int device_id;
    String session_token;
    String current_version;
    String download_link;
    int game;
    int status;
    long accept_before;
    long time;

    public JSONHolder() {
        success = false;
    }

    public void populate(String jsonString) {
        String oJsonString = jsonString;
        if (jsonString == null) {
            jsonString = "";
            success = false;
            error = "Connection Failure";
            status = 0;
            game = 0;
            accept_before = 0;
            device_id = 0;
            session_token = "";
            current_version = "";
            download_link = "";
            time = System.currentTimeMillis() / 1000L;
            return;
        }

        JsonObject jObject = null;
        try {
            JsonElement jelement = new JsonParser().parse(jsonString);
            jObject = jelement.getAsJsonObject();
        } catch (Exception e) {
            success = false;
            error = "Connection Failure: " + oJsonString;
            status = 0;
            game = 0;
            accept_before = 0;
            device_id = 0;
            session_token = "";
            current_version = "";
            download_link = "";
            time = System.currentTimeMillis() / 1000L;
            return;
        }

        if (jObject == null) {
            success = false;
            error = "Connection Failure: " + oJsonString;
            status = 0;
            game = 0;
            accept_before = 0;
            device_id = 0;
            session_token = "";
            current_version = "";
            download_link = "";
            time = System.currentTimeMillis() / 1000L;
            return;
        }

        try {
            success = jObject.get("success").getAsInt() != 0;
        } catch (Exception e) {
            System.out.println("Json parse fail");
            success = false;
        }

        try {
            error = jObject.get("error").getAsString();
        } catch (Exception e) {
            error = "";
        }

        try {
            status = jObject.get("status").getAsInt();
        } catch (Exception e) {
            status = 0;
        }
        try {
            game = jObject.get("game").getAsInt();
        } catch (Exception e) {
            game = 0;
        }
        try {
            accept_before = jObject.get("accept_before").getAsInt();
        } catch (Exception e) {
            accept_before = 0;
        }
        try {
            device_id = jObject.get("device_id").getAsInt();
        } catch (Exception e) {
            device_id = 0;
        }
        try {
            session_token = jObject.get("session_token").getAsString();
        } catch (Exception e) {
            session_token = "";
        }
        try {
            current_version = jObject.get("current_version").getAsString();
        } catch (Exception e) {
            current_version = "";
        }
        try {
            download_link = jObject.get("download_link").getAsString();
        } catch (Exception e) {
            download_link = "";
        }
        try {
            time = jObject.get("time").getAsLong();
        } catch (Exception e) {
            time = System.currentTimeMillis() / 1000L;
        }
    }
}