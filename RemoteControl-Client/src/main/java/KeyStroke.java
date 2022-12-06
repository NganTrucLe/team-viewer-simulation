import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;
import org.xerial.snappy.Snappy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

class KeyStroke extends JPanel implements Runnable {
    private String myFont = "ClearGothic";
    private Socket socket;
    private JFrame frame;
    private DataInputStream dataInputStream;
    public KeyStroke(JFrame frame, Socket socket) {
        setLayout(null);
        this.socket = socket;
        this.frame = frame;
        JButton hookBtn = new JButton("Hook");
        hookBtn.setBounds(50,20,150,50);
        hookBtn.setFont(new Font(myFont, Font.PLAIN, 20));
        add(hookBtn);
        JButton unHookBtn = new JButton("Hook");
        unHookBtn.setBounds(220,20,150,50);
        unHookBtn.setFont(new Font(myFont, Font.PLAIN, 20));
        add(hookBtn);

    }
    public void run() {

    }
}
