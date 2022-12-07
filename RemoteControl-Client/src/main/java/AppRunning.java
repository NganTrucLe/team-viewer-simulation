import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinUser.HHOOK;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.io.IOException;
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
        // try {
        //     String apps = dataInputStream.readUTF();
        //     System.out.println(apps);
        // } catch (IOException e) {
        //     System.out.println(e.toString());
        // }
		APList = new JTextArea(10, 5);
        JScrollPane scrollPane = new JScrollPane(APList);
        scrollPane.createHorizontalScrollBar();
        scrollPane.setBackground(Color.BLACK);
        scrollPane.setForeground(Color.WHITE);
        scrollPane.setBounds(20,90,600,400);
        add(scrollPane);

        pidText = new JTextField(25);
        pidText.setBackground(Color.BLACK);
        pidText.setForeground(Color.WHITE);

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
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "Get-Process", "|where {$_.MainWindowTitle }","|select ProcessName,Id,Description");
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

    public static void openApp(String src){
        try {
            String[] cmd = new String[]{"powershell.exe",  src.substring(0, src.length()-4)};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.start();
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
            }
        }else if(e.getSource() == openApp){
            Frame fileFrame = new Frame();
            FileDialog fd = new FileDialog(fileFrame, "Select File", FileDialog.LOAD);
            fd.setDirectory("C:\\");
            fd.setVisible(true);

            String path = fd.getDirectory() + fd.getFile();

            fileFrame.dispose();
            sendOpen(path);
        }
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
}