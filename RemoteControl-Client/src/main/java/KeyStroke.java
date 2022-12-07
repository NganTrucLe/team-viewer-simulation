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
    KeyStroke ppp = this;
    User32 lib = User32.INSTANCE;
	User32jna u32 = User32jna.INSTANCE;
    HHOOK hhk = null;
	HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
	Thread thread;
	Object lock = new Object();
    public interface User32jna extends Library {
        KeyStroke.User32jna INSTANCE = null;
        User32jna INSTACE = (User32jna) Native.loadLibrary("user32.dll", User32jna.class);
    }
    public KeyStroke(JFrame frame, Socket socket) {
        setLayout(null);
        this.socket = socket;
        this.frame = frame;
        JButton hookBtn = new JButton("Hook");
        hookBtn.setBounds(50,20,150,50);
        hookBtn.setFont(new Font(myFont, Font.PLAIN, 20));
        add(hookBtn);
        JButton unHookBtn = new JButton("Unhook");
        unHookBtn.setBounds(220,20,150,50);
        unHookBtn.setFont(new Font(myFont, Font.PLAIN, 20));
        add(unHookBtn);
        JTextArea display = new JTextArea ( 16, 58 );
        display.setEditable ( false ); // set textArea non-editable
        String str = "";
        for (int i = 0; i < 50; ++i)
            str += "Some text\n";
        display.setText(str);
        JScrollPane scroll = new JScrollPane ( display );
        scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        scroll.setBounds(20, 90, 550, 250);  
        add(scroll);
        //setLocation(null);
        thread = new Thread(this);
        thread.start();
    }
    public void run() {

    }
}
