package io.gameq.gameqwindows.ConnectionHandler;

import io.gameq.gameqwindows.ConnectionHandler.ep.EncryptedPreferences;
import io.gameq.gameqwindows.ConnectionHandler.ep.GenerateKey;
import io.gameq.gameqwindows.ConnectionHandler.ep.Util;
import io.gameq.gameqwindows.DataHandler.AcceptHandler;
import io.gameq.gameqwindows.Main;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;


/**
 * Created by Ludvig on 14/07/15.
 * Copyright GameQ AB 2015
 */
public final class ConnectionHandler {
    private static ConnectionHandler instance = null;
    private static String TAG = "GAMEQ";
    private static String sessionToken = "";
    private static long serverDelay = 0;
    static private final String algorithm = "DES";
    static private Preferences preferences;
    private static boolean isLoggedIn = false;
    private static int lastStatus = 0;
    private static int lastGame = 0;
    private static Timer statusTimer = null;

    static {
        byte rawKey[] = new byte[0];
        try {
            String s = ConnectionHandler.class.getResource("").getPath();
            File f = new File(String.valueOf(ConnectionHandler.class.getResource("/asdasd")));
            if(f.exists() && !f.isDirectory()) {
                rawKey = Util.readFile(String.valueOf(ConnectionHandler.class.getResource("/asdasd")));
            }
            else{
                GenerateKey.generateKey(String.valueOf(ConnectionHandler.class.getResource("/asdasd")));
                rawKey = Util.readFile(String.valueOf(ConnectionHandler.class.getResource("/asdasd")));
            }
            DESKeySpec dks = new DESKeySpec( rawKey );
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
            SecretKey secretKey = keyFactory.generateSecret(dks);
            preferences = EncryptedPreferences.userNodeForPackage(ConnectionHandler.class, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void instantiateDataModel() {
    }


    public static String post(String extension, String arguments) {
        String serverURL = "http://server.gameq.io:8080/computer/";
        String url = serverURL + extension + "?";
        arguments = arguments + "&key=68440fe0484ad2bb1656b56d234ca5f463f723c3d3d58c3398190877d1d963bb";
        URL obj;
        String returnString = null;
        try {
            obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            StringBuilder response = new StringBuilder();

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            byte[] buf = arguments.getBytes("UTF-8");
            wr.write(buf, 0, buf.length);
            //wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            returnString = response.toString();



        } catch (MalformedURLException e) {
            System.out.println("URL Error (MalformedURLException) for " + url );
        } catch (IOException e) {
            System.out.println("URL Error (IOException) for " + url );
        }

        if (returnString == null) {
            System.out.println("URL Error (nullResponse) for " + url );
            return null;
        } else {
            return returnString;
        }

    }

    public static void logout(CallbackGeneral caller) {
        final CallbackGeneral mCaller = caller;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("logout", "session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                holder.populate(response);
                ConnectionHandler.isLoggedIn = false;
                ConnectionHandler.saveEmail("");
                ConnectionHandler.savePassword("");
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();
    }


    public static void login(CallbackGeneral caller, String email, String password) {
        final CallbackGeneral mCaller = caller;
        final String mEmail = email;
        final String mPassword = ConnectionHandler.hashSHA256(password);
        final int device = ConnectionHandler.loadDeviceID();
        String deviceStringTmp = "";
        if (device != 0) {
            deviceStringTmp = "&device_id=" + device;
        }
        final String deviceString = deviceStringTmp;

        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("login", "email="+mEmail+"&password="+mPassword + "&push_token=" + ConnectionHandler.loadToken() + deviceString);
                System.out.println(response);
                holder.populate(response);
                if (holder.success) {
                    ConnectionHandler.saveEmail(mEmail);
                    ConnectionHandler.savePassword(mPassword);
                    sessionToken = holder.session_token;
                    ConnectionHandler.isLoggedIn = true;

                    if (holder.device_id != 0) {
                        ConnectionHandler.saveDeviceID(holder.device_id);
                    }
                    serverDelay = (System.currentTimeMillis() / 1000L) - holder.time;
                }
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();

    }

    public static void register(CallbackGeneral caller, String email, String password) {
        final CallbackGeneral mCaller = caller;
        final String mEmail = email;
        final String mPassword = ConnectionHandler.hashSHA256(password);
        final int device = ConnectionHandler.loadDeviceID();
        String deviceStringTmp = "";
        if (device != 0) {
            deviceStringTmp = "&device_id=" + device;
        }
        final String deviceString = deviceStringTmp;

        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("login", "email="+mEmail+"&password="+mPassword + "&push_token=" + ConnectionHandler.loadToken() + deviceString);
                holder.populate(response);
                if (holder.success) {
                    ConnectionHandler.saveEmail(mEmail);
                    ConnectionHandler.savePassword(mPassword);
                    sessionToken = holder.session_token;
                    if (holder.device_id != 0) {
                        ConnectionHandler.saveDeviceID(holder.device_id);
                    }
                    serverDelay = (System.currentTimeMillis() / 1000L) - holder.time;
                }
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void getStatus(CallbackGetStatus caller) {
        final CallbackGetStatus mCaller = caller;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("getStatus", "session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                holder.populate(response);
                mCaller.callback(holder.success, holder.error, holder.status, holder.game, holder.accept_before);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void needsUpdate(){
        if(isLoggedIn){
            setStatus((success, error) -> {
            }, lastGame, lastStatus);
        }
    }

    private static void resetStatusUpdateTimer(int game, int status){
        lastStatus = status;
        lastGame = game;
        if(statusTimer != null){
            statusTimer.cancel();
            statusTimer.purge();
            statusTimer = null;
        }
        statusTimer = new Timer();
        statusTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                needsUpdate();
            }
        }, 120 * 1000, 120 * 1000);
    }

    public static void setStatus(CallbackGeneral caller, int game, int status) {
        final CallbackGeneral mCaller = caller;
        resetStatusUpdateTimer(game, status);
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("setStatus", "status="+status + "&game="+ game + "&session_token=" +
                        ConnectionHandler
                                .sessionToken +"&device_id=" + ConnectionHandler.loadDeviceID());
                System.out.println("response " + response);
                holder.populate(response);
                System.out.println("error: " + holder.error);
                if (holder.success && holder.error.equals("accept")) {
                    AcceptHandler.acceptMatch(true, game);
                } else if (holder.success && holder.error.equals("auto")) {
                    try {
                        Thread.sleep(1000);                 //1000 milliseconds is one second.
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        System.out.println("I tried");
                        AcceptHandler.acceptMatch(true, game);
                    }
                } else if (holder.success && holder.error.equals("decline")) {
                    AcceptHandler.acceptMatch(false, game);
                }

                mCaller.callback(holder.success, holder.error);
            }
        };
        new Thread(r).start();
    }

    public static void versionControl(CallbackVersionControl caller) {
        final CallbackVersionControl mCaller = caller;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("versionControl", "");
                holder.populate(response);
                mCaller.callback(holder.success, holder.error, holder.current_version, holder.download_link);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void updateToken(CallbackGeneral caller, String token) {
        final CallbackGeneral mCaller = caller;
        final String mToken = token;
        saveToken(mToken);
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("updateToken", "push_token="+mToken+"&session_token=" + ConnectionHandler
                        .sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                holder.populate(response);
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void submitCSV(CallbackGeneral caller, String csv, int game, int type) {
        final CallbackGeneral mCaller = caller;
        final String mCSV = csv;
        final int mGame = game;
        final int mType = type;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("submitCSV", "csv="+mCSV+"&game="+mGame+"&type="+mType+"&session_token=" +
                        ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                holder.populate(response);
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void submitFeedback(CallbackGeneral caller, String feedback) {
        final CallbackGeneral mCaller = caller;
        final String mFeedback = feedback;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("submitFeedback", "feedback="+mFeedback+"&session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                System.out.println(response);
                holder.populate(response);
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void updatePassword(CallbackGeneral caller, String email, String newPassword, String oldPassword) {
        final CallbackGeneral mCaller = caller;
        final String mEmail = email;
        final String mOldPassword = oldPassword;
        final String mNewPassword = newPassword;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            public void run() {
                String response = post("updatePassword",
                        "email="+mEmail+"&password="+mOldPassword+"&new_password="+mNewPassword+"&session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                System.out.println(response);
                holder.populate(response);
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void loginWithRememberedDetails(CallbackGeneral caller) {
        final CallbackGeneral mCaller = caller;
        final String mEmail = ConnectionHandler.loadEmail();
        final String mPassword = ConnectionHandler.loadPassword();
        final int device = ConnectionHandler.loadDeviceID();
        String deviceStringTmp = "";
        if (device != 0) {
            deviceStringTmp = "&device_id=" + device;
        }
        final String deviceString = deviceStringTmp;
        if (mEmail == null || mPassword == null || !mEmail.matches("^[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,4}$") || mPassword.length() < 6) {
            mCaller.callback(false, "invalid login details");
        } else {
            Runnable r = new Runnable() {
                JSONHolder holder = new JSONHolder();
                public void run() {
                    String response = post("login", "email="+mEmail+"&password="+mPassword + "&push_token=" + ConnectionHandler.loadToken() + deviceString);
                    System.out.println(response);
                    holder.populate(response);
                    if (holder.success) {
                        ConnectionHandler.saveEmail(mEmail);
                        ConnectionHandler.savePassword(mPassword);
                        sessionToken = holder.session_token;
                        isLoggedIn = true;
                        if (holder.device_id != 0) {
                            ConnectionHandler.saveDeviceID(holder.device_id);
                        }
                        serverDelay = (System.currentTimeMillis() / 1000L) - holder.time;
                    }
                    mCaller.callback(holder.success, holder.error);
                    return;
                }
            };
            new Thread(r).start();
        }


    }

    public static void submitForgotPassword(CallbackGeneral caller, String email) {
        final CallbackGeneral mCaller = caller;
        final String mEmail = email;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();

            public void run() {
                String response = post("forgotPassword", "email=" + mEmail);
                holder.populate(response);
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();
    }

    // MARK: - DataHandling below

    private static void savePassword(String password) {
        preferences.put("pw", password);
    }

    private static void saveEmail(String email) {
        preferences.put("email", email);
    }

    private static void saveToken(String token) {
        preferences.put("token", token);
    }

    private static void saveDeviceID(int id) {
        preferences.put("device_id", String.valueOf(id));
    }

    private static void saveShouldReceiveNotifications(boolean registered) {
        preferences.put("notifications", String.valueOf(registered));
    }

    private static String loadPassword() {
        return preferences.get("pw", "");
    }

    public static String loadEmail() {
        return preferences.get("email", "");
    }

    private static String loadToken() {
        return preferences.get("token", "");
    }

    private static int loadDeviceID() {
        String bajs = preferences.get("device_id", "");
        // System.out.println("bajs"+bajs);
        if(bajs.equals("")){return 0;}
        else{return Integer.parseInt(bajs);}
    }

    public static boolean loadShouldReceiveNotifications() {

        String bajs = preferences.get("notifications", "");
        if(bajs.equals("") || bajs.equals(Boolean.parseBoolean(bajs))){return true;}
        else{return false;}
    }

    private static String hashSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());

            byte byteData[] = md.digest();
            return bytesToHex(byteData);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: " + e);
            return null;
        }
    }

    private static String bytesToHex(byte[] b) {
        char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder buf = new StringBuilder();
        for (byte aB : b) {
            buf.append(hexDigit[(aB >> 4) & 0x0f]);
            buf.append(hexDigit[aB & 0x0f]);
        }
        return buf.toString().toLowerCase(Locale.US);
    }

    private static String generatePreferenceStore() {
        String a = "aAzpAau2sdn3vpAuis1298370tAhu1baaAAdk";
        a = a.replaceFirst("d+","mAx45");
        return hashSHA256(a);

    }

    private static String generateStoreKey() {
        String a = "nu9y7TvrC56e4xwQ2za3w4scVtf7gBY8cdr5x4es";
        a = a.toUpperCase();
        return hashSHA256(a);
    }
}
