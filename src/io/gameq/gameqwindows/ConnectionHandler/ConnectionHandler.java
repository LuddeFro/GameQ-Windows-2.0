package io.gameq.gameqwindows.ConnectionHandler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Created by Ludvig on 14/07/15.
 * Copyright GameQ AB 2015
 */
public final class ConnectionHandler {

    private static ConnectionHandler instance = null;


    private static String TAG = "GAMEQ";
    private static String sessionToken = "";
    private static long serverDelay = 0;


    protected ConnectionHandler() {
        // Exists only to defeat instantiation.
    }

    public static void instantiateDataModel() {

    }


    public static String post(String extension, String arguments) {
        String serverURL = "http://server.gameq.io/computer/";
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
            @Override
            public void run() {
                String response = post("logout", "session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                holder.populate(response);
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
            @Override
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
            @Override
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
            @Override
            public void run() {
                String response = post("getStatus", "session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                holder.populate(response);
                mCaller.callback(holder.success, holder.error, holder.status, holder.game, holder.accept_before);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void setStatus(CallbackGeneral caller, int game, int status) {
        final CallbackGeneral mCaller = caller;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            @Override
            public void run() {
                String response = post("setStatus", "session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
                holder.populate(response);
                mCaller.callback(holder.success, holder.error);
                return;
            }
        };
        new Thread(r).start();
    }

    public static void versionControl(CallbackVersionControl caller) {
        final CallbackVersionControl mCaller = caller;
        Runnable r = new Runnable() {
            JSONHolder holder = new JSONHolder();
            @Override
            public void run() {
                String response = post("versionControl", "");
                holder.populate(response);
                mCaller.callback(holder.success, holder.error, holder.current_version, holder.download_link);
                return ;
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
            @Override
            public void run() {
                String response = post("versionControl", "push_token="+mToken+"&session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
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
            @Override
            public void run() {
                String response = post("versionControl", "csv="+mCSV+"&game="+mGame+"&type="+mType+"&session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
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
            @Override
            public void run() {
                String response = post("versionControl", "feedback="+mFeedback+"&session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
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
            @Override
            public void run() {
                String response = post("versionControl", "email="+mEmail+"&password="+mOldPassword+"&new_password="+mNewPassword+"&session_token=" + ConnectionHandler.sessionToken + "&device_id=" + ConnectionHandler.loadDeviceID());
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
                @Override
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


    }
























    // MARK: - DataHandling below


    private static void savePassword(String password) {
        preferences.put("password", password);
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

    public static void saveShouldReceiveNotifications(boolean registered) {
        preferences.put("notifications", String.valueOf(registered));
    }

    private static String loadPassword() {
        if (preferences.getString("password") == null) {
            return "";
        }
        return preferences.getString("password");
    }

    public static String loadEmail() {
        if (preferences.getString("email") == null) {
            return "";
        }
        return preferences.getString("email");
    }

    private static String loadToken() {
        if (preferences.getString("token") == null) {
            return "";
        }
        return preferences.getString("token");
    }

    private static int loadDeviceID() {
        if (preferences.getString("device_id") == null) {
            return 0;
        }
        return Integer.parseInt(preferences.getString("device_id"));
    }

    public static boolean loadShouldReceiveNotifications() {
        return preferences.getString("notifications") == null || preferences.getString("notifications").equals(String.valueOf(true));
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
