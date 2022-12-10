public class ShutDownServer {
    public static void computer(){
        try {
            ProcessBuilder command = new ProcessBuilder("powershell.exe", "Stop-Computer", "-Force");
            command.start();
        } catch (Exception e) {
            DebugMessage.printDebugMessage(e);
        }
    }
}
