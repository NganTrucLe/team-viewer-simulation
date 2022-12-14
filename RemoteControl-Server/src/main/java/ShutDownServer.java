public class ShutDownServer {
    public static void computer(){
        Runtime runtime = Runtime.getRuntime();
      try
      {
         System.out.println("Shutting down the PC after 5 seconds.");
         runtime.exec("shutdown -s -t 5");
      }
     catch (Exception e) {
            DebugMessage.printDebugMessage(e);
        }
    }
}
