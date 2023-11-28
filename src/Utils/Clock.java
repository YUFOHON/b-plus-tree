package Utils;

public class Clock {
    // we use static variables here
    // your program can only have ONE clock to measure its elapsed time.
    private static long startTime;
    private static long stopTime;
        
    public static void start() {
        startTime = System.nanoTime();
    }

    public static void stop() {
        stopTime = System.nanoTime();
    }

    public static long getElapsedTime() {
        return stopTime - startTime;
    }

    public static long getElapsedTimeInMilliSec() {
        return (stopTime - startTime)/(1000*1000);
    }

}
