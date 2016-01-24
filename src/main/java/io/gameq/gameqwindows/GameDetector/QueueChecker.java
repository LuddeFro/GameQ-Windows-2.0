package io.gameq.gameqwindows.GameDetector;


import io.gameq.gameqwindows.Structs.Encoding;
import io.gameq.gameqwindows.Structs.Game;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;


import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;
import io.gameq.gameqwindows.Structs.Status;


/**
 * Created by Ludvig Fröberg on 2015-10-03.
 */
public class QueueChecker {

    private GameDetector detector = null;
    private static final int msize = 30;
    private static int[][] m = new int[msize][msize];
    private static String lastWord = "";
    private static boolean initiated = false;
    private static WinDef.RECT aRect = new WinDef.RECT();
    private static Rectangle wRect;
    private static int cbsuccesses = 0;
    private static int cbwaittime = 150;
    private static final double epsilon = 0.80;

    private static final int MAX_TITLE_LENGTH = 1024;


    private static BufferedImage cap;
    //private static Tesseract t = Tesseract.getInstance();

    //private static Robot robot;

    private static HashMap<Game, Boolean> isForeground = new HashMap<Game, Boolean>();
    private static HashMap<Game, Boolean> shouldStop = new HashMap<Game, Boolean>();
    private static HashMap<Game, Boolean> isIngame = new HashMap<Game, Boolean>(); //TODO: SET THIS APPROPRIATELY


    private static void initiate() {
        initiated = true;
        for (int i = 0; i < msize; i++) {
            m[0][i] = i;
            m[i][0] = i;
        }
        /*try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            initiated = false;
        }*/
        //t.setDatapath("src/main/resources");
    }


    public void startMonitor(Game game, GameDetector detector) {
        this.detector = detector;
        shouldStop.put(game, false);
        (new Thread(new QueueTimer(game))).start();
        (new Thread(new ForegroundTimer(game))).start();
    }

    public void stopMonitor(Game game) {
        shouldStop.put(game, true);
    }

    public class ForegroundTimer implements Runnable {

        public Game game;

        public ForegroundTimer(Game game) {
            this.game = game;
        }

        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (shouldStop.get(game) != null && shouldStop.get(game)) {
                return;
            }
            if (detector.getStatus() == Status.InGame) {
                //do nothing
            } else {
                if (foregroundWindowMatches(game)) {
                    isForeground.put(game, true);
                } else {
                    isForeground.put(game, false);
                }
            }
            run();
        }


    }

    public class QueueTimer implements Runnable {

        public Game game;

        public QueueTimer(Game game) {
            this.game = game;
        }

        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (shouldStop.get(game) != null && shouldStop.get(game)) {
                return;
            }
            if (isIngame.get(game) != null && isIngame.get(game)) {
                //do nothing
            } else if (isForeground.get(game) != null && isForeground.get(game)) {
                if (queuePopped(game)) {
                    //TODO Tell the Connectionshandler
                    detector.updateStatus(Status.GameReady);
                }
            }
            run();
        }
    }

    public static boolean queuePopped(Game game) {
        if (!initiated) {
            //initiate
            QueueChecker.initiate();
        }
        return avgColorCheck(game);

        /*String cmpString = Encoding.getAcceptStringFromGame(game);
        String ocrString = ocrCheck(game);
        return closeEnough(ocrString, cmpString);*/

    }

    private static boolean avgColorCheck(Game game) {
        WinDef.HWND windowHandle = User32DLL.GetForegroundWindow();
        User32DLL.GetWindowRect(windowHandle, aRect);
        wRect = aRect.toRectangle();

//        System.out.println("w h: "+ wRect.getWidth() + " " + wRect.getHeight()) ;
        int numSquares = 4;
        int[] sy = new int[numSquares];
        int[] sx = new int[numSquares];
        int[] sw = new int[numSquares];
        int[] sh = new int[numSquares];
        int[] r = new int[numSquares];
        int[] g = new int[numSquares];
        int[] b = new int[numSquares];
        boolean[] shouldmatch = new boolean[numSquares];


        int extraY = getExtraY(wRect);
        wRect.y = (int) (wRect.getY() + extraY);
        wRect.height = (int) (wRect.getHeight() - extraY);
        switch (game) {
            case Dota2:
                sy[0] = (int) (wRect.getY() + wRect.getHeight()*(585.0/1200.0));
                sh[0] = (int) (wRect.getHeight()*(20.0/1200.0) );
                sx[0] = (int) (wRect.getX() + wRect.getWidth()*(610.0/1920.0));
                sw[0] = (int) (wRect.getWidth()*(20.0/1920.0));
                r[0] = 46;
                g[0] = 78;
                b[0] = 65;
                shouldmatch[0] = true;

                sy[1] = (int) (wRect.getY() + wRect.getHeight()*(585.0/1200.0));
                sh[1] = (int) (wRect.getHeight()*(20.0/1200.0) );
                sx[1] = (int) (wRect.getX() + wRect.getWidth()*(1040/1920.0));
                sw[1] = (int) (wRect.getWidth()*(20.0/1920.0));
                r[1] = 81;
                g[1] = 43;
                b[1] = 42;
                shouldmatch[1] = true;

                numSquares = 2;
                break;
            case CSGO:
                sy[0] = (int) (0 + wRect.getHeight()*(250.0/1200.0));
                sh[0] = (int) (wRect.getHeight()*(20.0/1200.0) );
                sx[0] = (int) (0 + (wRect.getWidth()-(wRect.getHeight()*1.25))/2.0 + (380.0/1080.0)*wRect.getHeight()/2 - 0.23*wRect.getHeight()/2);
                sw[0] = (int) 20;//(0.23*wRect.getHeight());
                r[0] = 28;
                g[0] = 107;
                b[0] = 76;
                shouldmatch[0] = true;

                sy[1] = (int) (0 + wRect.getHeight()*(250.0/1200.0));
                sh[1] = (int) (wRect.getHeight()*(20.0/1200.0) );
                sx[1] = (int) (0 + (wRect.getWidth()-(wRect.getHeight()*1.25))/2.0 + (380.0/1080.0)*wRect.getHeight()/2 + 0.23*wRect.getHeight()/2);
                sw[1] = (int) 20;//(0.23*wRect.getHeight());
                r[1] = 28;
                g[1] = 107;
                b[1] = 76;
                shouldmatch[1] = true;

                numSquares = 2;
                break;
            case LoL:
                sy[0] = (int) (0 + (wRect.getHeight()-167.0)/2.0 + 120);
                sh[0] = (int) 20;
                sx[0] = (int) (0 + (wRect.getWidth()-370.0)/2.0 + 50);
                sw[0] = (int) 20;
                r[0] = 21;
                g[0] = 54;
                b[0] = 94;
                shouldmatch[0] = true;

                sy[1] = (int) (0 + (wRect.getHeight()-167.0)/2.0 + 120);
                sh[1] = (int) 20;
                sx[1] = (int) (0 + (wRect.getWidth()-370.0)/2.0 + 170);
                sw[1] = (int) 2;
                r[1] = 20;
                g[1] = 52;
                b[1] = 92;
                shouldmatch[1] = false;

                sy[2] = (int) (0 + (wRect.getHeight()-167.0)/2.0 + 120);
                sh[2] = (int) 20;
                sx[2] = (int) (0 + (wRect.getWidth()-370.0)/2.0 + 240);
                sw[2] = (int) 20;
                r[2] = 19;
                g[2] = 51;
                b[2] = 91;
                shouldmatch[2] = true;

                sy[3] = (int) (0 + wRect.getHeight()*(15.0/800.0));
                sh[3] = (int) 20;
                sx[3] = (int) (0 + wRect.getWidth()*(565.0/1280.0));
                sw[3] = (int) 20;
                r[3] = 97;
                g[3] = 38;
                b[3] = 11 ; //97 38 11
                shouldmatch[3] = true;

                numSquares = 4;
                break;
            default:
                break;
        }


        try {
            //cap = (BufferedImage)cb.getData(DataFlavor.imageFlavor); //ca 40ms

            cap = capture(User32DLL.GetForegroundWindow());
            for (int i = 0; i < numSquares; i++) {
                BufferedImage scap = cap.getSubimage(sx[i],sy[i],sw[i],sh[i]);
                //dev lines below to see what image is caught
                //File file = new File(Encoding.getStringFromGame(game)+i+"AVGCLRCap.png");
                //ImageIO.write(scap, "png", file);
                boolean cn = closeEnough(scap, r[i], g[i], b[i]);
                if ((cn && !shouldmatch[i]) || (!cn && shouldmatch[i])) {
                    return false;
                    //System.out.println("mismatch"); // for debugging
                }
            }
            return true;
        } catch (Exception e) {
            if (cbwaittime < 200) {
                cbwaittime++;
            }
            e.printStackTrace();
            System.out.println("ojsan");
            return false;
        }
    }

    //TODO if this is put to use again, update capture techniques.
    /*
    private static String ocrCheck(Game game) {
        WinDef.HWND windowHandle = User32DLL.GetForegroundWindow();
        User32DLL.GetWindowRect(windowHandle, aRect);
        wRect = aRect.toRectangle();
        int sy = 0;
        int sx = 0;
        int sw = 0;
        int sh = 0;
        int extraY = getExtraY(wRect);
        wRect.y = (int) (wRect.getY() + extraY);
        wRect.height = (int) (wRect.getHeight() - extraY);
        switch (game) {
            case Dota2:
                sy = (int) (wRect.getY() + wRect.getHeight()*(420.0/1200.0));
                sh = (int) (wRect.getHeight()*(30.0/1200.0) );
                sx = (int) (wRect.getX() + wRect.getWidth()*(830.0/1920.0));
                sw = (int) (wRect.getWidth()*(260.0/1920.0));
                break;
            case CSGO:
                sy = (int) (wRect.getY() + wRect.getHeight()*(190.0/1200.0));
                sh = (int) (wRect.getHeight()*(40.0/1200.0) );
                sx = (int) (wRect.getX() + (wRect.getWidth()-(wRect.getHeight()*1.25))/2.0 + (380.0/1080.0)*wRect.getHeight()/2 - 0.23*wRect.getHeight()/2);
                sw = (int) (0.23*wRect.getHeight());
                break;
            case LoL:
                sy = (int) (wRect.getY() + (wRect.getHeight()-167)/2 + 21);
                sh = (int) 25;
                sx = (int) (wRect.getX() + (wRect.getWidth()-370)/2 + 120);
                sw = (int) 134;
                break;
            default:
                break;
        }
        robot.keyPress(KeyEvent.VK_PRINTSCREEN);
        robot.delay(1); // 1 ms
        robot.keyRelease(KeyEvent.VK_PRINTSCREEN);
        robot.delay(cbwaittime); // 30 ms
        try {
            cap = (BufferedImage)cb.getData(DataFlavor.imageFlavor); //ca 40ms
            cbsuccesses++;
            if (cbsuccesses > 100) {
                cbwaittime--;
            }
            cap = cap.getSubimage(sx,sy,sw,sh);
            String ocrString = t.doOCR(cap);
            //dev lines below to see what image is caught
            File file = new File(Encoding.getStringFromGame(game)+"OCRCap.png");
            ImageIO.write(cap, "png", file);
            System.out.println(ocrString);
            return ocrString.trim();
        } catch (Exception e) {
            cbsuccesses = 0;
            if (cbwaittime < 200) {
                cbwaittime++;
            }
            e.printStackTrace();
            System.out.println("ojsan");
        }
        return "ocr failure";
    }*/

    private static int getExtraY(Rectangle r) {
        if (Math.abs(r.getWidth()/r.getHeight() - 16.0/9.0) > 0.01 ||
                Math.abs(r.getWidth()/r.getHeight() - 16.0/10.0) > 0.01 ||
                Math.abs(r.getWidth()/r.getHeight() - 4.0/3.0) > 0.01) {
            return 0;
        } else {
            return 26;
        }
    }

    public static boolean foregroundWindowMatches(Game game) {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        User32DLL.GetWindowTextW(User32DLL.GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
        String winTitle = Native.toString(buffer);
        //System.out.println("foreground window: "+winTitle);
        return winTitle.equalsIgnoreCase(Encoding.getWindowTitleFromGame(game));
    }

    private static int matchesToIndex(String w1, String w2) {
        int i = 0;
        for (; i < (w1.length() < w2.length() ? w1.length() : w2.length()); i++) {
            if (w1.charAt(i) != w2.charAt(i)) {
                break;
            }
        }
        return i;
    }

    private static boolean closeEnough(String w1, String w2) {
        int tolerance = (int) (epsilon*Math.max(w1.length(), w2.length()));
        if (Math.abs(w1.length()-w2.length()) > tolerance) {
            return false;
        }
        w1 = w1.toLowerCase();
        w2 = w2.toLowerCase();
        int matches = matchesToIndex(w2, lastWord);
        for (int i = 1; i <= w1.length(); i++) {
            for (int j = matches+1; j <= w2.length(); j++) {

                if (w1.charAt(i-1) == w2.charAt(j-1)) { // 0
                    m[i][j] = m[i-1][j-1];
                } else { // 1
                    int y =  Math.min(m[i][j-1], m[i-1][j]);
                    m[i][j] = Math.min(y, m[i-1][j-1]) + 1;
                }

            }
        }
        lastWord = w2;
        if (m[w1.length()][w2.length()] <= tolerance) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean closeEnough(BufferedImage img, long r, long g, long b) {
        long t1 = System.currentTimeMillis();
        int w = img.getWidth();
        int h = img.getHeight();
        int wh = w*h;
        long sumr = 0, sumg = 0, sumb = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Color pixel = new Color(img.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        int num = w * h;
        boolean closeEnough = true;
        r = Math.abs(sumr / wh - r);
        g = Math.abs(sumg / wh - g);
        b = Math.abs(sumb / wh - b);
        if (r > 10 || g > 10 || b > 10) {
            closeEnough = false;
        }

//        System.out.println("getColor time: " + (System.currentTimeMillis() - t1) + " " + sumr / wh + " " + sumg / wh + " " + sumb / wh);
        return closeEnough;
    }




    public static BufferedImage capture(WinDef.HWND hWnd) {

        WinDef.HDC hdcWindow = User32.INSTANCE.GetDC(hWnd);
        WinDef.HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);

        WinDef.RECT bounds = new WinDef.RECT();
        User32Extra.INSTANCE.GetClientRect(hWnd, bounds);

        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;

        WinDef.HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);

        WinNT.HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);
        GDI32Extra.INSTANCE.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, 0, 0, WinGDIExtra.SRCCOPY);

        GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);

        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        Memory buffer = new Memory(width * height * 4);
        GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, buffer.getIntArray(0, width * height), 0, width);

        GDI32.INSTANCE.DeleteObject(hBitmap);
        User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);

        return image;

    }



    /// Supporting DLL Definitions

    public interface WinGDIExtra extends WinGDI {

        public WinDef.DWORD SRCCOPY = new WinDef.DWORD(0x00CC0020);

    }

    public interface User32Extra extends User32 {

        User32Extra INSTANCE = (User32Extra) Native.loadLibrary("user32", User32Extra.class, W32APIOptions.DEFAULT_OPTIONS);

        public WinDef.HDC GetWindowDC(WinDef.HWND hWnd);

        public boolean GetClientRect(WinDef.HWND hWnd, WinDef.RECT rect);

    }

    public interface GDI32Extra extends GDI32 {

        GDI32Extra INSTANCE = (GDI32Extra) Native.loadLibrary("gdi32", GDI32Extra.class, W32APIOptions.DEFAULT_OPTIONS);

        public boolean BitBlt(WinDef.HDC hObject, int nXDest, int nYDest, int nWidth, int nHeight, WinDef.HDC hObjectSource, int nXSrc, int nYSrc, WinDef.DWORD dwRop);

    }

    static class Kernel32 {
        static { Native.register("kernel32"); }
        public static int PROCESS_QUERY_INFORMATION = 0x0400;
        public static int PROCESS_VM_READ = 0x0010;
        public static native int GetLastError();
        public static native Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, Pointer pointer);
    }

    static class User32DLL {
        static { Native.register("user32"); }
        public static native int GetWindowThreadProcessId(WinDef.HWND hWnd, PointerByReference pref);
        public static native WinDef.HWND GetForegroundWindow();
        public static native WinDef.BOOL GetWindowRect(WinDef.HWND hwnd, WinDef.RECT rct);
        public static native int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);
        public static native WinDef.BOOL PrintWindow(WinDef.HWND hWnd, WinDef.HDC hdcBlt, int flags);
    }




    interface GDI32 extends com.sun.jna.platform.win32.GDI32
    {
        GDI32 INSTANCE = (GDI32) Native.loadLibrary(GDI32.class);

        boolean BitBlt(WinDef.HDC hdcDest, int nXDest, int nYDest, int nWidth, int nHeight, WinDef.HDC hdcSrc, int nXSrc, int nYSrc, int dwRop);

        WinDef.HDC GetDC(WinDef.HWND hWnd);

        boolean GetDIBits(WinDef.HDC dc, WinDef.HBITMAP bmp, int startScan, int scanLines, byte[] pixels, WinGDI.BITMAPINFO bi, int usage);

        boolean GetDIBits(WinDef.HDC dc, WinDef.HBITMAP bmp, int startScan, int scanLines, short[] pixels, WinGDI.BITMAPINFO bi, int usage);

        boolean GetDIBits(WinDef.HDC dc, WinDef.HBITMAP bmp, int startScan, int scanLines, int[] pixels, WinGDI.BITMAPINFO bi, int usage);

        int SRCCOPY = 0xCC0020;
    }






}