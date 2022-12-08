import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinUser.HHOOK;


import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import java.lang.ProcessBuilder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;


class AppRunning extends JPanel implements Runnable {
    private JTextArea APList;
    private JButton killApp;
    private JButton openApp;
    private JTextField pidText;
	private String myFont = "ClearGothic";
    private Socket socket;
	private JFrame frame;
	private DataInputStream dataInputStream;
	private ObjectInputStream objectInputStream;
    AppRunning ppp=this;
	HHOOK hhk = null;
	HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
	Thread thread;
	Object lock = new Object();
    public interface User32jna extends Library {
        AppRunning.User32jna INSTANCE = null;
        User32jna INSTACE = (User32jna) Native.loadLibrary("user32.dll", User32jna.class);
    }
	public AppRunning(JFrame frame, Socket socket) {
		setLayout(null);
		this.socket = socket;
		this.frame = frame;
        String apps="";
        try {
            this.socket=socket;
            dataInputStream=new DataInputStream(socket.getInputStream());
            apps = dataInputStream.readUTF();
            System.out.println(apps);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
		APList = new JTextArea(apps,200, 100);

        JScrollPane scrollPane = new JScrollPane(APList);
        scrollPane.createHorizontalScrollBar();
        scrollPane.setBackground(Color.BLACK);
        scrollPane.setForeground(Color.WHITE);
        scrollPane.setBounds(20,90,600,400);
        add(scrollPane);

        pidText = new JTextField(25);
        pidText.setBackground(Color.BLACK);
        pidText.setForeground(Color.WHITE);
        //pidText.setBounds(390,20,150,50);

        killApp = new JButton("Kill");
        killApp.addActionListener(this::actionPerformed);
        killApp.setFocusable(false);
        killApp.setBackground(Color.BLACK);
        killApp.setForeground(Color.WHITE);
        killApp.setBounds(50,20,150,50);

        openApp = new JButton("Open");
        openApp.addActionListener(this::actionPerformed);
        openApp.setFocusable(false);
        openApp.setBackground(Color.BLACK);
        openApp.setForeground(Color.WHITE);
        openApp.setBounds(220,20,150,50);

        add(pidText);
        add(killApp);
        add(openApp);
        System.out.println(socket.getInetAddress());
		thread = new Thread(this);
		thread.start();
	}
    public static void sendApp(Socket s) throws Exception{
        DataOutputStream cout = new DataOutputStream(s.getOutputStream());
        cout.writeUTF(GetApplication());
    }

    private void sendKill(int k){
        try {
            DataOutputStream cout = new DataOutputStream(this.socket.getOutputStream());
            cout.writeUTF("KP"+ String.valueOf(k));
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void sendOpen(String prs){
        try {
            DataOutputStream cout = new DataOutputStream(this.socket.getOutputStream());
            cout.writeUTF("OA"+ prs);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private static String GetApplication() {
        StringBuilder sb = new StringBuilder();
        try {
//            Run command and print to the console
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "gps", "|where {$_.MainWindowTitle }", "|select Name,Id");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));
            String s;
            while ((s = stdInput.readLine()) != null) {
                try {
                    sb.append(s);
                    sb.append("\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sb = new StringBuilder("Error Table");
        }
        return sb.toString();
    }

    // public static void openApp(String src){
    //     try {
    //         //String[] cmd = new String[]{"powershell.exe",  src.substring(0, src.length()-4)};
    //         ProcessBuilder pb = new ProcessBuilder("powershell.exe",  src.substring(0, src.length()-4));
    //         pb.start();
    //     } catch (IOException e) {
    //         throw new RuntimeException(e);
    //     }

    // }
    public static void KillApp(int processPID) {
        try {
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "Stop-Process", "-Id", Integer.toString(processPID));
            pb.start();
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }
    public static void StartApp(String src) {
        try {
            String[] cmd = new String[]{"powershell.exe", "&",  src, ""};
            //String[] cmd = new String[]{"powershell.exe",  src.substring(0, src.length()-4)};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.start();
            //Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void actionPerformed(ActionEvent e) {
        if (e.getSource() == killApp) {
            try {
                sendKill(Integer.parseInt(pidText.getText()));

            }catch (Exception k){
                JOptionPane.showMessageDialog(null, "Vui lòng chọn id process");
                pidText.setBounds(390,20,150,50);
            }
        }else if(e.getSource() == openApp){
            Frame fileFrame = new Frame();
            FileDialog fd = new FileDialog(fileFrame, "Select File", FileDialog.LOAD);
            fd.setDirectory("C:\\");
            fd.setVisible(true);
            String path=fd.getDirectory() + fd.getFile();
            sendOpen(path);
            // try{
            //     PrintWriter writer=new PrintWriter(this.socket.getOutputStream());
            //     String open= JOptionPane.showInputDialog("Enter name: ");
            //     writer.println(open);
            //     writer.flush();
            //     String path="C:\\"+System.getProperty(open)+"\\"+open;
            //     sendOpen(path);
            // }catch(IOException e1){
            //     System.out.println(e1);
            // }
            // PrintWriter writer=new PrintWriter(this.socket.getOutputStream());
            // String open= JOptionPane.showInputDialog("Enter name: ");
            // writer.println(open);
            // writer.flush();
            // String path=System.getProperty(open);
            //String path = fd.getDirectory() + open.getFile();
            //String path=fd.getDirectory() + System.getProperty(fd.getFile());
            //sendOpen(path);
            
        }
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
}