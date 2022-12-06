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

import javax.imageio.ImageIO;

class AppRunning extends JPanel implements Runnable {
    private JTextArea APList;
    private JButton killApp;
    private JButton openApp;
    private JTextField pidText;
	private String myFont = "ClearGothic";
    private Socket socket;
	private JFrame frame;
	private DataOutputStream mouseOutputStream;
	private DataInputStream dataInputStream;
	private ObjectInputStream objectInputStream;
	private BufferedWriter bufferedWriter;
	private int image_Width = 1280;
	private int image_Height = 720;
	private byte imageByte2[] = new byte[6220800];
	private int mouseX = 0, mouseY = 0;
	private int mouseClickCount = 0;
	private int mouseButton = 0;
	private int mousePosition = 0; // 1 == move 2 == click
	private int app_Width = 1920;
	private int app_Height = 1080;
	private Boolean isCompress = true;
	private final int MOUSE_MOVE = 1;
	private final int MOUSE_PRESSD = 2;
	private final int MOUSE_RELEASED = 3;
	private final int MOUSE_DOWN_WHEEL = 4;
	private final int MOUSE_UP_WHEEL = 5;
	private final int KEY_PRESSED = 6;
	private final int KEY_RELEASED = 7;
	private final int KEY_CHANGE_LANGUAGE = 8;
	private int count = 0;
    AppRunning ppp=this;
	HHOOK hhk = null;
	HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
	Thread thread;
	Object lock = new Object();

	public AppRunning(JFrame frame, Socket socket) {
		setLayout(null);
		this.socket = socket;
		this.frame = frame;
		System.out.println(socket.getInetAddress());
		try {
			setLayout(null);
			socket.setTcpNoDelay(true);
			dataInputStream = new DataInputStream(socket.getInputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            String apps = dataInputStream.readUTF();
            display(frame, apps);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		thread = new Thread(this);
		thread.start();
	}
    private void display(JFrame jFrame, String apps){
        // jFrame = new JFrame("App running");
        // jFrame.setMinimumSize(new Dimension(500, 400));
        // jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // jFrame.setLayout(new BorderLayout());
        // jFrame.setBackground(Color.BLACK);
        // jFrame.setForeground(Color.WHITE);

        APList = new JTextArea(apps, 10, 5);
        APList.setEditable(false);
        APList.setCaretPosition(0);
        APList.setFont(new Font("Consolas", Font.PLAIN, 12));
        APList.setBackground(Color.BLACK);
        APList.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(APList);
        scrollPane.createHorizontalScrollBar();
        scrollPane.setBackground(Color.BLACK);
        scrollPane.setForeground(Color.WHITE);

        jFrame.add(scrollPane, BorderLayout.CENTER);

        pidText = new JTextField(25);
        pidText.setBackground(Color.BLACK);
        pidText.setForeground(Color.WHITE);

        killApp = new JButton("Kill");
        killApp.addActionListener(this::actionPerformed);
        killApp.setFocusable(false);
        killApp.setBackground(Color.BLACK);
        killApp.setForeground(Color.WHITE);

        openApp = new JButton("Open");
        openApp.addActionListener(this::actionPerformed);
        openApp.setFocusable(false);
        openApp.setBackground(Color.BLACK);
        openApp.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setForeground(Color.WHITE);

        buttonPanel.add(pidText);
        buttonPanel.add(killApp);
        buttonPanel.add(openApp);
        buttonPanel.setPreferredSize(new Dimension(500, 50));

        jFrame.add(buttonPanel, BorderLayout.SOUTH);

//        Open jFrame in center of screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setLocation(dim.width / 2 - jFrame.getSize().width / 2, dim.height / 2 - jFrame.getSize().height / 2);
        jFrame.setVisible(true);
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