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

class ScreenMirror extends JPanel implements Runnable {
	private String myFont = "ClearGothic";
	private Socket socket;
	private Socket cursorSocket;
	private Socket keyboardSocket;
	private BufferedImage screenImage;
	private JFrame frame;
	private JLabel FPSlabel;
	private int FPScount = 0;
	private BufferedImage image;
	private BufferedImage screenShotImage;
	private DataOutputStream mouseOutputStream;
	private DataOutputStream keyboardOutputStream;
	private DataInputStream dataInputStream;
	private ObjectInputStream objectInputStream;
	private BufferedWriter bufferedWriter;
	private int image_Width = 1280;
	private int image_Height = 720;
	private byte imageByte2[] = new byte[6220800];
	private int screen_Width = 1920;
	private int screen_Height = 1080;
	private Boolean isCompress = true;
	private int count = 0;
	private Vector<byte[]> imgVec = new Vector<>();
	ScreenMirror ppp = this;
	User32 lib = User32.INSTANCE;
	User32jna u32 = User32jna.INSTANCE;
	HHOOK hhk = null;
	HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
	Thread thread;
	Object lock = new Object();
	public interface User32jna extends Library {
		User32jna INSTANCE = (User32jna) Native.loadLibrary("user32.dll", User32jna.class);

		// User32jna INSTANCE = (User32jna)
		// Native.loadLibrary("user32.dll",User32jna.class);
		public void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);
	}

	public ScreenMirror(JFrame frame, Socket socket, Socket cursorSocket, Socket keyboardSocket) {
		setLayout(null);
		this.socket = socket;
		this.cursorSocket = cursorSocket;
		this.keyboardSocket = keyboardSocket;
		this.frame = frame;
		FPSlabel = new JLabel("FPS : " + Integer.toString(FPScount));
		FPSlabel.setFont(new Font(myFont, Font.BOLD, 20));
		FPSlabel.setBounds(20, 370, 100, 50);
		add(FPSlabel);
		JButton screenShotBtn = new JButton("Screen shot");
		screenShotBtn.setBounds(550, 20, 150, 50);
		screenShotBtn.setFont(new Font(myFont, Font.PLAIN, 20));
		add(screenShotBtn);
		screenShotBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				screenShotImage = image;
				try {
					final JFileChooser fc = new JFileChooser();
					fc.setDialogTitle("Save screenshot");
					fc.showSaveDialog(screenShotBtn);
					File saveFile = fc.getSelectedFile();
					ImageIO.write(screenShotImage,"png",saveFile);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		System.out.println(socket.getInetAddress());
		try {
			setLayout(null);
			socket.setTcpNoDelay(true);
			keyboardOutputStream = new DataOutputStream(keyboardSocket.getOutputStream());
			dataInputStream = new DataInputStream(socket.getInputStream());
			objectInputStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			DebugMessage.printDebugMessage(e);
		}
		thread = new Thread(this);
		thread.start();
		//KeyboardThread keyboardThread = new KeyboardThread();
		//keyboardThread.start();
		FPSCheckThread fpsCheckThread = new FPSCheckThread();
		fpsCheckThread.start();
		showThread ss = new showThread();
		//showThread ss2 = new showThread();
		ss.start();
		//ss2.start();
		// lib.UnhookWindowsHookEx(hhk);

		/*
		 * addKeyListener(new KeyAdapter() {
		 * 
		 * @Override public void keyTyped(KeyEvent e) {
		 * //System.out.println("type" + e.getKeyCode() + "  " + e.getKeyChar()
		 * + "  " + e.getID() + "  " + e.getModifiers()+ "  "+
		 * e.getKeyLocation() + "  " + e.getExtendedKeyCode());
		 * 
		 * }
		 * 
		 * @Override public void keyPressed(KeyEvent e) { try {
		 * //System.out.println("press" + e.getKeyCode() + "  " + e.getKeyChar()
		 * + "  " + e.getID() + "  " + e.getModifiers()+ "  "+
		 * e.getKeyLocation() + "  " + e.getExtendedKeyCode());
		 * if(e.getKeyCode() !=0){ dataOutputStream.writeInt(KEY_PRESSED);
		 * dataOutputStream.writeInt(e.getKeyCode()); } } catch (IOException e1)
		 * { DebugMessage.printDebugMessage(e1); } }
		 * 
		 * @Override public void keyReleased(KeyEvent e) { try {
		 * //System.out.println("released" + e.getKeyCode() + "  " +
		 * e.getKeyChar() + "  " + e.getID() + "  " + e.getModifiers()+ "  "+
		 * e.getKeyLocation() + "  " + e.getExtendedKeyCode());
		 * if(e.getKeyCode() ==0){ if(count >= 1){ count = 0; return; }
		 * //System.out.println(t.getLocale().toString() + "  " +
		 * t.getLocale().getCountry() + "  " +
		 * t.getLocale().getDisplayCountry());
		 * //System.out.println("한글키 눌림-보냄"); count = 1; u32.keybd_event((byte)
		 * 0x15, (byte)0, 0, 0);//누름ffDDDddSS u32.keybd_event((byte) 0x15,
		 * (byte)00, (byte)0x0002, 0);// 누름 해제
		 * dataOutputStream.writeInt(KEY_CHANGE_LANGUAGE);
		 * 
		 * } else{ dataOutputStream.writeInt(KEY_RELEASED);
		 * dataOutputStream.writeInt(e.getKeyCode()); }
		 * 
		 * } catch (Exception e1) { DebugMessage.printDebugMessage(e1); } } });
		 */

		//requestFocus();
	}

	public void run() {
		try {
			screen_Width = dataInputStream.readInt();
			screen_Height = dataInputStream.readInt();
			image_Width = dataInputStream.readInt();
			image_Height = dataInputStream.readInt();
			isCompress = dataInputStream.readBoolean();
		} catch (IOException e1) {
			DebugMessage.printDebugMessage(e1);
		}

		while (true) {

			try {// 172.30.1.54
				if (dataInputStream.available() > 0) {
					int length = 0;
					if (isCompress) {

						length = dataInputStream.readInt();

						byte imageByte[] = new byte[length];

						dataInputStream.readFully(imageByte, 0, length);

						imgVec.addElement(imageByte);
						if (imgVec.size() > 1) {
							synchronized (lock) {
								
								lock.wait();								
							}
						}

					} else {
						length = dataInputStream.readInt();
						dataInputStream.readFully(imageByte2, 0, length);
						screenImage = new BufferedImage(image_Width, image_Height, BufferedImage.TYPE_3BYTE_BGR);
						screenImage.setData(Raster.createRaster(screenImage.getSampleModel(),
								new DataBufferByte(imageByte2, imageByte2.length), new Point()));
					}
				}
				
			} catch (Exception e) {
				DebugMessage.printDebugMessage(e);
			}

		}
	}

	class showThread extends Thread {
		byte imageByte[];
		byte uncompressImageByte[];		
		public void run() {
			
			while (true) {
				try {

					imageByte = imgVec.get(0);
					uncompressImageByte = Snappy.uncompress(imageByte);
					if (imgVec.size() > 0){
						imgVec.remove(0);
						//System.out.println(imgVec.size());
					}
					screenImage = new BufferedImage(image_Width, image_Height, BufferedImage.TYPE_3BYTE_BGR);
					screenImage.setData(Raster.createRaster(screenImage.getSampleModel(),
							new DataBufferByte(uncompressImageByte, uncompressImageByte.length), new Point()));	
					if (imgVec.size() == 1) {
						synchronized (lock) {
								lock.notify();						
						}
					}
					if(screenImage != null){
						image = screenImage;
						FPScount++;
						repaint();
					}					
					
				} catch (Exception e) {

				}
			}
		}
	}

	class FPSCheckThread extends Thread {
		public void run() {
			while (true) {
				try {
					sleep(1000);
					FPSlabel.setText("FPS : " + Integer.toString(FPScount));
					FPScount = 0;
				} catch (InterruptedException e) {
				}
			}
		}
	}
	/*
	class KeyboardThread extends Thread {
		int result;
		int keypress = 0;

		public void run() {
			LowLevelKeyboardProc rr = new LowLevelKeyboardProc() {

				@Override
				public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
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
								keyboardOutputStream.writeInt(KEY_RELEASED);
								keyboardOutputStream.writeInt(info.vkCode);
								break;
							case WinUser.WM_KEYDOWN:
								keyboardOutputStream.writeInt(KEY_PRESSED);
								keyboardOutputStream.writeInt(info.vkCode);
								break;
							case WinUser.WM_SYSKEYUP:
								keyboardOutputStream.writeInt(KEY_RELEASED);
								keyboardOutputStream.writeInt(info.vkCode);
								break;
							case WinUser.WM_SYSKEYDOWN:
								keyboardOutputStream.writeInt(KEY_PRESSED);
								keyboardOutputStream.writeInt(info.vkCode);
								break;
							}

						}
					} catch (Exception e) {
						DebugMessage.printDebugMessage(e);
					}

					Pointer ptr = info.getPointer();

					long peer = Pointer.nativeValue(ptr);

					return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(peer));
				}
			};
			hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, rr, hMod, 0);
			MSG msg = new MSG();
			while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
				if (result == -1) {
					System.err.println("error in get message");
					break;
				} else {
					System.err.println("got message");
					lib.TranslateMessage(msg);
					lib.DispatchMessage(msg);
				}
			}
		}
	}
	*/
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 20, 20, Math.round(getWidth()/3*2), Math.round(getHeight()/3*2), this);
	}
}