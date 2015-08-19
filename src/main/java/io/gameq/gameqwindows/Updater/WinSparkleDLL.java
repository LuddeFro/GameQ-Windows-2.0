package io.gameq.gameqwindows.Updater;


import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Created by fabianwikstrom on 8/19/2015.
 */

public interface WinSparkleDLL extends Library {
    WinSparkleDLL INSTANCE = (WinSparkleDLL) Native.loadLibrary("WinSparkle", WinSparkleDLL.class);

    //win_sparkle_init()
    void win_sparkle_init();

    //win_sparkle_set_appcast_url()
    void win_sparkle_set_appcast_url(String param);

    //win_sparkle_set_app_details
    void win_sparkle_set_app_details(String companyName, String appName, String appVersion);

    //win_sparkle_cleanup()
    void win_sparkle_cleanup();

    //win_sparkle_set_automatic_check_for_updates
    void win_sparkle_set_automatic_check_for_updates(int param);

    //win_sparkle_set_update_check_interval
    void win_sparkle_set_update_check_interval(int seconds);

    //win_sparkle_check_update_without_ui
    void win_sparkle_check_update_without_ui();

    //win_sparkle_check_update_with_ui
    void win_sparkle_check_update_with_ui();


}
