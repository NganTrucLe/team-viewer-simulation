import com.sun.jna.Library;
import com.sun.jna.Native;
import org.xerial.snappy.Snappy;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Vector;


public class NetworkScreenServer extends JFrame {
	private final static int SERVER_PORT = 9999;
	private final static int SERVER_KEYBOARD_PORT = SERVER_PORT - 1;
	private DataOutputStream imageOutputStream;
	private ObjectOutputStream objectOutputStream;
	private String myFont = "????";
	private BufferedImage screenImage;
	private Rectangle rect;
	private MainPanel mainPanel = new MainPanel();
	private ServerSocket imageServerSocket = null;
	private ServerSocket keyboardServerSocket = null;
	private Socket imageSocket = null;
	private Socket keyboardSocket = null;
	private Robot robot;
	private int screenWidth, screenHeight;
	private Boolean isRunning = false;
	private Thread imgThread;
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
	private URL cursorURL = getClass().getClassLoader().getResource("cursor.gif");
	private Boolean isCompress = true;
	private JFrame fff = this;
	private final int KEY_PRESSED = 1;
	private final int KEY_RELEASED = 2;
	private final int KEY_CHANGE_LANGUAGE = 8;
	int count = 0, count2 = 0;
	private User32jna u32 = User32jna.INSTANCE;
	private int buffersize = 1;
	private BufferedImage[] img = new BufferedImage[buffersize];
	private Vector<byte[]> imgvec = new Vector<>();

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

	public interface User32jna extends Library {
		User32jna INSTANCE = (User32jna) Native.load("user32.dll", User32jna.class);
		public void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);
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

					imgThread = new Thread(mainPanel);
					//KeyBoardThread keyBoardThread = new KeyBoardThread();
					imgThread.start();
					//keyBoardThread.start();

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
					// imgThread.interrupt();
					stopBtn.setEnabled(false);
					startBtn.setEnabled(true);
					compressTrueRBtn.setEnabled(true);
					compressFalseRBtn.setEnabled(true);

				}
			});
			widthTextfield.transferFocus();
		}

		public void run() {

			try {
				robot = new Robot();
				screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
				screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
				rect = new Rectangle(0, 0, screenWidth, screenHeight);
				imageServerSocket = new ServerSocket(SERVER_PORT);// ImageSERVER
				keyboardServerSocket = new ServerSocket(SERVER_KEYBOARD_PORT);
				imageSocket = imageServerSocket.accept();
				imageSocket.setTcpNoDelay(true);
				imageOutputStream = new DataOutputStream(imageSocket.getOutputStream());
				objectOutputStream = new ObjectOutputStream(imageSocket.getOutputStream());
				imageOutputStream.writeInt(screenWidth);
				imageOutputStream.writeInt(screenHeight);
				imageOutputStream.writeInt(new_Width);
				imageOutputStream.writeInt(new_Height);
				imageOutputStream.writeBoolean(isCompress);
				//cursor = ImageIO.read(cursorURL);
			} catch (Exception e) {

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
			if (!imageServerSocket.isClosed() || keyboardServerSocket.isClosed()) {
				try {
					imageServerSocket.close();
					keyboardServerSocket.close();
				} catch (IOException e) {
					DebugMessage.printDebugMessage(e);
				}
			}
		}
	}

	class KeyBoardThread extends Thread {
		int result;
		int keypress = 0;
		public void run() {
			try {
				keyboardServerSocket = new ServerSocket(SERVER_KEYBOARD_PORT);
				keyboardSocket = keyboardServerSocket.accept();
				DataInputStream dataInputStream = new DataInputStream(keyboardSocket.getInputStream());
				while (true) {
					int keyboardState = dataInputStream.readInt();
				}
			} catch (Exception e1) {
				
			}
		}
	}
	// 	}
	// 	// public void run() {
	// 	// 	try {
	// 	// 		keyboardServerSocket = new ServerSocket(SERVER_KEBOARD_PORT);
	// 	// 		keyboardSocket = keyboardServerSocket.accept();
	// 	// 		DataInputStream dataInputStream = new DataInputStream(keyboardSocket.getInputStream());
	// 	// 		while (true) {
	// 	// 			int keyboardState = dataInputStream.readInt();
	// 	// 			if (keyboardState == KEY_PRESSED) {// KEYBOARD PRESSED
	// 	// 				int keyCode = dataInputStream.readInt();
	// 	// 				// System.out.println(keyCode + "????");
	// 	// 				u32.keybd_event((byte) keyCode, (byte) 0, 0, 0);// ????ffDDDddSS
	// 	// 				// robot.keyPress(keyCode);
	// 	// 			} else if (keyboardState == KEY_RELEASED) {
	// 	// 				int keyCode = dataInputStream.readInt();
	// 	// 				// System.out.println(keyCode + "????");
	// 	// 				u32.keybd_event((byte) keyCode, (byte) 00, (byte) 0x0002, 0);// ??
	// 	// 				// robot.keyRelease(keyCode);
	// 	// 			}
	// 	// 			yield();
	// 	// 		}
	// 	// 	} catch (Exception e) {

	// 	// 	}
	// 	// }
	// }
}
