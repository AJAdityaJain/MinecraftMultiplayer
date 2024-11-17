package network;

public class Logger {
    public static final byte INFO = 0;
    public static final byte WARNING = 1;
    public static final byte ERROR = 2;

    public static void log(String message, byte level){
        switch (level){
            case 0:
                System.out.print("\u001B[34m[INFO]          ");
                break;
            case 1:
                System.out.print("\u001B[33m[WARNING]       ");
                break;
            case 2:
                System.out.print("\u001B[31m[ERROR]         ");
                break;
        }
        System.out.println(message);
    }

}
