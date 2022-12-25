import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.xerial.snappy.Snappy;

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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Vector;


public class NetworkScreenServer extends JFrame {
	private final static int SERVER_PORT = 9999;
	private final static int SERVER_SCREEN_PORT = SERVER_PORT - 1;
	private final static int SERVER_KEYBOARD_PORT = SERVER_PORT - 2;
	private final static int SERVER_APP_PORT = SERVER_PORT - 3;
	private final static int SERVER_SHUTDOWN_PORT = SERVER_PORT - 4;
	private final static int SERVER_PROCESS_PORT = SERVER_PORT-5;
	private DataOutputStream imageOutputStream;
	private ObjectOutputStream objectOutputStream;
	private String myFont = "????";
	private BufferedImage screenImage;
	private Rectangle rect;
	private MainPanel mainPanel = new MainPanel();
	private ServerSocket serverSocket = null;
	private ServerSocket screenServerSocket = null;
	private ServerSocket keyboardServerSocket = null;
	private ServerSocket appServerSocket = null;
	private ServerSocket shutdownServerSocket = null;
	private ServerSocket processServerSocket = null;
	private Socket socket = null;
	private Socket screenSocket = null;
	private Socket keyboardSocket = null;
	private Socket appSocket = null;
	private Socket shutdownSocket = null;
	private Socket processSocket = null;
	private int screenWidth, screenHeight;
	private Boolean isRunning = false;
	private Thread mainThread;
	private static int new_Width = 1920;
	private static int new_Height = 1080;
	private JButton startBtn;
	private JButton stopBtn;
	private JTextField widthTextfield;
	private JTextField heightTextfield;
	private JRadioButton compressTrueRBtn;
	private JRadioButton compressFalseRBtn;
	private JLabel widthLabel;
	private JLabel heightLabel;
	private JLabel compressLabel;
	private Boolean isCompress = true;
	private JFrame fff = this;
	private final int KEY_PRESSED = 1;
	private final int KEY_RELEASED = 2;
	private final int KEY_CHANGE_LANGUAGE = 8;
	int count = 0, count2 = 0;
	User32 lib = User32.INSTANCE;
	private User32jna u32 = User32jna.INSTANCE;
	HHOOK hhk = null;
	HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
	private int buffersize = 1;
	private BufferedImage[] img = new BufferedImage[buffersize];
	private Vector<byte[]> imgvec = new Vector<>();

	public interface User32jna extends Library {
		User32jna INSTANCE = (User32jna) Native.loadLibrary("user32.dll", User32jna.class);
		public void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);
	}
	public NetworkScreenServer() {
		setTitle("Network Screen Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setContentPane(mainPanel);
		setSize(490, 160);
		setVisible(true);
		setResizable(false);
		widthTextfield.requestFocus();
	}


	class MainPanel extends JPanel implements Runnable {

		public MainPanel() {
			setLayout(null);

			startBtn = new JButton("Start");
			stopBtn = new JButton("Stop");
			widthTextfield = new JTextField(Integer.toString(new_Width), 5);
			heightTextfield = new JTextField(Integer.toString(new_Height), 5);
			widthLabel = new JLabel("Width");
			heightLabel = new JLabel("Height");
			compressLabel = new JLabel("<html>&nbsp&nbsp&nbsp<span>Image<br>Compress</span></html>");
			compressTrueRBtn = new JRadioButton("True");
			compressFalseRBtn = new JRadioButton("False");

			startBtn.setBounds(0, 0, 150, 130);
			stopBtn.setBounds(150, 0, 150, 130);
			widthLabel.setBounds(327, 8, 50, 15);
			widthTextfield.setBounds(300, 30, 90, 35);
			heightLabel.setBounds(325, 70, 50, 15);
			heightTextfield.setBounds(300, 90, 90, 35);
			compressLabel.setBounds(405, -10, 100, 50);
			compressTrueRBtn.setBounds(390, 30, 80, 30);
			compressFalseRBtn.setBounds(390, 90, 80, 30);

			ButtonGroup group = new ButtonGroup();
			group.add(compressTrueRBtn);
			group.add(compressFalseRBtn);

			widthLabel.setFont(new Font(myFont, Font.PLAIN, 15));
			heightLabel.setFont(new Font(myFont, Font.PLAIN, 15));

			compressLabel.setFont(new Font(myFont, Font.PLAIN, 10));
			startBtn.setFont(new Font(myFont, Font.PLAIN, 20));
			stopBtn.setFont(new Font(myFont, Font.PLAIN, 20));
			compressTrueRBtn.setFont(new Font(myFont, Font.PLAIN, 20));
			compressFalseRBtn.setFont(new Font(myFont, Font.PLAIN, 20));

			compressTrueRBtn.setSelected(true);

			add(startBtn);
			add(stopBtn);
			add(widthLabel);
			add(widthTextfield);
			add(heightLabel);
			add(heightTextfield);
			add(compressLabel);
			add(compressTrueRBtn);
			add(compressFalseRBtn);
			stopBtn.setEnabled(false);
			System.out.println("Hello");
			
			KeyboardThread kbThread = new KeyboardThread();
			System.out.println("Start thread");
			kbThread.start();
			startBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (isRunning)
						return;
					try {
						new_Height = Integer.parseInt(heightTextfield.getText());
						new_Width = Integer.parseInt(widthTextfield.getText());
					} catch (Exception e1) {
						return;
					}
					heightTextfield.setEditable(false);
					widthTextfield.setEditable(false);
					isRunning = true;
					startBtn.setEnabled(false);
					stopBtn.setEnabled(true);
					if (compressTrueRBtn.isSelected()) {
						isCompress = true;
					} else if (compressFalseRBtn.isSelected()) {
						isCompress = false;
					}
					compressTrueRBtn.setEnabled(false);
					compressFalseRBtn.setEnabled(false);
					mainThread = new Thread(mainPanel);
					mainThread.start();
				}
			});
			stopBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!isRunning)
						return;
					heightTextfield.setEditable(true);
					widthTextfield.setEditable(true);
					isRunning = false;
					ServerSocketCloseThread closeThread = new ServerSocketCloseThread();
					closeThread.start();
					// mainThread.interrupt();
					stopBtn.setEnabled(false);
					startBtn.setEnabled(true);
					compressTrueRBtn.setEnabled(true);
					compressFalseRBtn.setEnabled(true);

				}
			});
			widthTextfield.transferFocus();
			requestFocus();
		}

		public void run() {
			System.out.println("in run");
			try {
				serverSocket = new ServerSocket(SERVER_PORT);
				socket = serverSocket.accept();
				ShutDownThread shutDownThread = new ShutDownThread();
				shutDownThread.start();
				AppRunningThread appRunningThread = new AppRunningThread();
				appRunningThread.start();
				ScreenThread screenThread = new ScreenThread();
				screenThread.start();
				ProcessRunningThread processRunningThread=new ProcessRunningThread();
				processRunningThread.start();;
						
			} catch (Exception e) {
				DebugMessage.printDebugMessage(e);
			}
		}
		class ShutDownThread extends Thread{
			public void run() {
				try {
					shutdownServerSocket = new ServerSocket(SERVER_SHUTDOWN_PORT);
					shutdownSocket = shutdownServerSocket.accept();
					while(isRunning){
						ShutDownServer.computer();
					}
				} catch (Exception e) {
					DebugMessage.printDebugMessage(e);
				}
			}
            
        } 
		class AppRunningThread extends Thread {
			public void run() {
				try {
					appServerSocket = new ServerSocket(SERVER_APP_PORT);
					appSocket = appServerSocket.accept();
					DataInputStream cin = new DataInputStream(appSocket.getInputStream());
					AppRunning.sendApp((appSocket));
					while(isRunning){
						String msg=cin.readUTF();
						switch(msg){
							case "RS":
								AppRunning.sendApp(appSocket);
							default:
								break;
						}
						switch (msg.substring(0, 2)){
							case "KP":
								AppRunning.KillApp(Integer.parseInt(msg.substring(2)));
								break;
							case "OA":
								AppRunning.StartApp(msg.substring(2));
							default:
								break;
						}
					}
				} catch (Exception e) {
					DebugMessage.printDebugMessage(e);
				}
			}
		} 
		class ProcessRunningThread extends Thread {
			public void run() {
				try {
					processServerSocket = new ServerSocket(SERVER_PROCESS_PORT);
					processSocket =processServerSocket.accept();
					DataInputStream cin = new DataInputStream(processSocket.getInputStream());
					ProcessRunning.sendProcess((processSocket));
					while(isRunning){
						String msg=cin.readUTF();
						switch(msg){
							case "RS":
								ProcessRunning.sendProcess(processSocket);
							default:
								break;
						}
						switch (msg.substring(0, 2)){
							case "KP":
								ProcessRunning.KillProcess(Integer.parseInt(msg.substring(2)));
								break;
							case "OA":
								ProcessRunning.StartProcess(msg.substring(2));
							default:
								break;
						}
					}
				} catch (Exception e) {
					DebugMessage.printDebugMessage(e);
				}
			}
		} 
		public void KeyStroke() {
			try {
				keyboardServerSocket = new ServerSocket(SERVER_KEYBOARD_PORT);
				System.out.println("Waiting to connect");
				keyboardSocket = keyboardServerSocket.accept();
				DataOutputStream dataOutputStream = new DataOutputStream(keyboardSocket.getOutputStream());
			}
			catch (Exception e){
				DebugMessage.printDebugMessage(e);
			}
			
		}
		class ScreenThread extends Thread {
			public void run() {
				try {
					screenServerSocket = new ServerSocket(SERVER_SCREEN_PORT);
					screenSocket = screenServerSocket.accept();
					screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
					screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
					rect = new Rectangle(0, 0, screenWidth, screenHeight);
					screenSocket.setTcpNoDelay(true);
					imageOutputStream = new DataOutputStream(screenSocket.getOutputStream());
					objectOutputStream = new ObjectOutputStream(screenSocket.getOutputStream());
					imageOutputStream.writeInt(screenWidth);
					imageOutputStream.writeInt(screenHeight);
					imageOutputStream.writeInt(new_Width);
					imageOutputStream.writeInt(new_Height);
					imageOutputStream.writeBoolean(isCompress);
				} catch (Exception e) {
					DebugMessage.printDebugMessage(e);
				}
				ImgDoubleBufferTh th = new ImgDoubleBufferTh();
				th.start();			
				int index = 0;
				Runtime runtime = Runtime.getRuntime();
				while (isRunning) {
					try {	
						byte[] imageByte = imgvec.get(0);
						if(imgvec.size() == 3){
							synchronized (th) {
								th.notify();
							}						
						}
						if (isCompress) {
							imageOutputStream.writeInt(imageByte.length);
							imageOutputStream.write(imageByte);
							imageOutputStream.flush();
						} else {
							imageOutputStream.writeInt(imageByte.length);
							imageOutputStream.write(imageByte);
							imageOutputStream.flush();
						}
					} catch (Exception e) {
					}
					if (runtime.totalMemory() / 1024 / 1024 > 500)
						System.gc();
					if (imgvec.size() > 1) {
						imgvec.remove(0);
						index++;								
						if(index == 30){
							index=0;
							System.gc();
						}
					}
				}
			}
		}
	}

	class ImgDoubleBufferTh extends Thread {
		BufferedImage bufferimage;
		Robot robot = null;
		
		synchronized public void run() {			
			try {
				robot = new Robot();
			} catch (AWTException e) {
			}			
			while (true) {
				bufferimage = robot.createScreenCapture(rect);
				bufferimage = getScaledImage(bufferimage, new_Width, new_Height, BufferedImage.TYPE_3BYTE_BGR);
				byte[] imageByte = ((DataBufferByte) bufferimage.getRaster().getDataBuffer()).getData();
				try {
					imgvec.addElement(compress(imageByte));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(imgvec.size()>5)
					try {
						wait();
					} catch (InterruptedException e) {
						
					}				
			}

		}
	}

	public static byte[] compress(byte[] data) throws IOException {
		byte[] output = Snappy.compress(data);
		return output;
	}

	public BufferedImage getScaledImage(BufferedImage myImage, int width, int height, int type) {
		BufferedImage background = new BufferedImage(width, height, type);
		Graphics2D g = background.createGraphics();
		g.setColor(Color.WHITE);
		g.drawImage(myImage, 0, 0, width, height, null);
		g.dispose();
		return background;
	}

	class ServerSocketCloseThread extends Thread {
		public void run() {
			if (!serverSocket.isClosed()) {
				try {
					screenServerSocket.close();
					keyboardServerSocket.close();
					appSocket.close();
					shutdownSocket.close();
					processSocket.close();
				} catch (IOException e) {
					DebugMessage.printDebugMessage(e);
				}
			}
		}
	}
	
	class KeyboardThread extends Thread {
		int result;
		int keypress = 0;
		synchronized public void run() {
			LowLevelKeyboardProc rr = new LowLevelKeyboardProc() {
				@Override
				public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
					System.out.println("in thread");
					try {
						 System.out.println(info.vkCode);
						if (info.vkCode == 21) {
							System.out.println("한영");
							if (keypress == 0) {
								u32.keybd_event((byte) 0x15, (byte) 0, 0, 0);// 누름ffDDDddSS
								u32.keybd_event((byte) 0x15, (byte) 00, (byte) 0x0002, 0);// 땜
								keypress++;
							} else {
								keypress = 0;
							}

						}
						if (nCode >= 0) {

							switch (wParam.intValue()) {
							case WinUser.WM_KEYUP:
								System.out.println(KEY_RELEASED);
								System.out.println(info.vkCode);
								break;
							case WinUser.WM_KEYDOWN:
								System.out.println(KEY_PRESSED);
								System.out.println(info.vkCode);
								break;
							case WinUser.WM_SYSKEYUP:
								System.out.println(KEY_RELEASED);
								System.out.println(info.vkCode);
								break;
							case WinUser.WM_SYSKEYDOWN:
								System.out.println(KEY_PRESSED);
								System.out.println(info.vkCode);
								break;
							}

						}
					} catch (Exception e) {
						System.out.println("Error");
						DebugMessage.printDebugMessage(e);
					}

			 		Pointer ptr = info.getPointer();

			 		long peer = Pointer.nativeValue(ptr);

			 		return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(peer));
				}
			};
			//hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, rr, hMod, 0);
			// MSG msg = new MSG();
			// while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
			// 	if (result == -1) {
			// 		System.err.println("error in get message");
			// 		break;
			// 	} else {
			// 		System.err.println("got message");
			// 		lib.TranslateMessage(msg);
			// 		lib.DispatchMessage(msg);
			// 	}
			// }
		}
	}
	@Override
	public synchronized void addKeyListener(KeyListener l) {
		// TODO Auto-generated method stub
		super.addKeyListener(l);
	}
}