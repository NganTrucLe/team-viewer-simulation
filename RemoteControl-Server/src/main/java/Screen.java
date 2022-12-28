// import com.sun.jna.Library;
// import com.sun.jna.Native;
// import com.sun.jna.Pointer;
// import org.xerial.snappy.Snappy;
// import java.io.*;
// import java.net.Socket;
// import java.net.SocketException;
// import java.util.Vector;
// import java.awt.*;
// import java.awt.image.*;

// public class Screen {
//     private Socket socket;
//     private Rectangle rect;
//     private DataOutputStream dataOutputStream;
//     private ObjectOutputStream objectOutputStream;
//     private int screenWidth, screenHeight;
//     private boolean isCompress = true;
// 	private static int new_Width = 1920;
// 	private static int new_Height = 1080;
//     private BufferedImage screenImage;
//     private int buffersize = 1;
// 	private BufferedImage[] img = new BufferedImage[buffersize];
// 	private Vector<byte[]> imgvec = new Vector<>();
//     public void Screen(Socket mySocket, boolean myIsCompress) throws SocketException {
//         socket = mySocket;
//         isCompress = myIsCompress;
//         screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
//         screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
//         rect = new Rectangle(0, 0, screenWidth, screenHeight);
//         socket.setTcpNoDelay(true);
        
//     }
//     public void ResizeScreen() {
//         try {
//             dataOutputStream = new DataOutputStream(socket.getOutputStream());
//             objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//             dataOutputStream.writeInt(screenWidth);
//             dataOutputStream.writeInt(screenHeight);
//             dataOutputStream.writeInt(new_Width);
//             dataOutputStream.writeInt(new_Height);
//             dataOutputStream.writeBoolean(isCompress);
//         } catch (IOException e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//         }
//     }
//     class ImgDoubleBufferTh extends Thread {
// 		BufferedImage bufferimage;
// 		Robot robot = null;
		
// 		synchronized public void run() {			
// 			try {
// 				robot = new Robot();
// 			} catch (AWTException e) {
// 			}			
// 			while (true) {
// 				bufferimage = robot.createScreenCapture(rect);
// 				bufferimage = getScaledImage(bufferimage, new_Width, new_Height, BufferedImage.TYPE_3BYTE_BGR);
// 				byte[] imageByte = ((DataBufferByte) bufferimage.getRaster().getDataBuffer()).getData();
// 				try {
// 					imgvec.addElement(compress(imageByte));
// 				} catch (IOException e) {
// 					e.printStackTrace();
// 				}
// 				if(imgvec.size()>5)
// 					try {
// 						wait();
// 					} catch (InterruptedException e) {
						
// 					}				
// 			}

// 		}
// 	}
//     public static byte[] compress(byte[] data) throws IOException {
// 		byte[] output = Snappy.compress(data);
// 		return output;
// 	}
//     public BufferedImage getScaledImage(BufferedImage myImage, int width, int height, int type) {
// 		BufferedImage background = new BufferedImage(width, height, type);
// 		Graphics2D g = background.createGraphics();
// 		g.setColor(Color.WHITE);
// 		g.drawImage(myImage, 0, 0, width, height, null);
// 		g.dispose();
// 		return background;
// 	}
//     public void StartMirror(boolean isRunning) {
//         while (isRunning) {
//             try {	
//                 byte[] imageByte = imgvec.get(0);
//                 if(imgvec.size() == 3){
//                     synchronized (th) {
//                         th.notify();
//                     }						
//                 }
//                 if (isCompress) {
//                     imageOutputStream.writeInt(imageByte.length);
//                     imageOutputStream.write(imageByte);
//                     imageOutputStream.flush();
//                 } else {
//                     imageOutputStream.writeInt(imageByte.length);
//                     imageOutputStream.write(imageByte);
//                     imageOutputStream.flush();
//                 }
//             } catch (Exception e) {
//             }
//             if (runtime.totalMemory() / 1024 / 1024 > 500)
//                 System.gc();
//             if (imgvec.size() > 1) {
//                 imgvec.remove(0);
//                 index++;								
//                 if(index == 30){
//                     index=0;
//                     System.gc();
//                 }
//             }
//         }
//     }

    
// }
