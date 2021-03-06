package Utility;

public class Config {
    public static int REG_SIZE = 8;
    public static int MAX_MEMORIZATION_NUMBER = 60;
    public static int useAllocator = 2;

    public static boolean useUselessLoopElimination = true;
    public static boolean useLoopConditionOptimization = true;
    public static boolean useMemorization = true;
    public static boolean useBasicBlockOptimization = true;
    public static boolean useSuperlocalValueNumbering = true;
    public static boolean useDeadCodeElimination = true;

    public static boolean printAST = false;
    public static boolean printIR = false;
}
