package io.gameq.gameqwindows.ConnectionHandler;

/**
 * Created by Ludvig on 14/07/15.
 */
public interface CallbackVersionControl {
    void callback(boolean success, String error, String newestVersion, String link);
}
