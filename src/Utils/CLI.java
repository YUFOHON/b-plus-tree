package Utils;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import btree.BTree;

// Command Line Interface (CLI)
public class CLI {

    /**
     * User interface
     * @param bTree
     */

    public static void shell(BTree bTree) {
        // Start monitor user command
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("\n> ");
            String input = in.nextLine();
            if (input.isEmpty()) {
                System.out.println("Command not found.");
                continue;
            }

            String[] tokens = input.split("\\s+");
            switch (tokens[0].toLowerCase().trim()) {
                case "help":
                    helpCommand(tokens);
                    break;
                case "insert":
                    insertCommand(tokens, bTree);
                    break;
                case "delete":
                    deleteCommand(tokens, bTree);
                    break;
                case "search":
                    searchCommand(tokens, bTree);
                    break;
                case "print":
                    printCommand(tokens, bTree);
                    break;
                case "stats":
                    statsCommand(tokens, bTree);
                    break;
                case "updatetest":
                    updateTestCommand(tokens, bTree);
                    break;
                case "querytest":
                    queryTestCommand(tokens, bTree);
                    break;
                case "quit":
                    System.out.println("The program is terminated.");
                    return;
                //for TA only
                case "grade":
                    System.out.println("Grading mode = \"ON\".");
                    gradingMode = true;
                    break;
                default:
                    System.out.println("Invalid input.");
            }
            System.out.println();
        }
    }


    /**
     * Command line argument runner
     * @param tokens command line arguments
     */
    private static void helpCommand(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("Invalid input.");
            return;
        }
        System.out.println("The following commands are supported:");
        System.out.println("insert <key>"); //my data generator assumes key=rid for simplicity // you can assume the same, too
        System.out.println("delete <key>");
        System.out.println("search <key1> <key2>");
        System.out.println("print");
        System.out.println("updateTest <file>");
        System.out.println("queryTest <file>");
        System.out.println("stats");
        System.out.println("quit");
    }

    private static void insertCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 2) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        try {
            Integer key = new Integer(tokens[1]);
            bTree.insert(key);
            //System.out.println("The key " + key + " has been inserted in the B+-tree!");
        } catch (Utils.DuplicateKeyException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 2) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        Integer key = new Integer(tokens[1]);
        try {
            bTree.delete(key);
            //System.out.println("The key " + key + " has been deleted in the B+-tree.");
        } catch (Utils.KeyNotFoundException | Utils.TreeIsEmptyException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void searchCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 3) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        try {
            List<Integer> result = bTree.search(new Integer(tokens[1]), new Integer(tokens[2]));
            if (result.isEmpty()) {
                System.out.println("No result for range " + tokens[1] + " - " + tokens[2]);
                return;
            }

            System.out.println("Result (" + result.size() + " data(s)): ");
            System.out.println(result.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "))
            );
        } catch (Utils.TreeIsEmptyException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void printCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 1) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        try {
            bTree.printTree();
        } catch (Utils.TreeIsEmptyException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void statsCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 1) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        bTree.dumpStatistics();
    }

    private static void updateTestCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 2) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        String updatesFileName = tokens[1];
        String[] stringArray = readFile.readData(updatesFileName);
        //Do the update
        //Just time the update in grading
        if(gradingMode) Clock.start();
        try {
            for (String str : stringArray) {
                String[] words = str.split("\\s+");
                switch (words[0].toLowerCase().trim()) {
                    case "+":
                        insertCommand(words, bTree);
                        break;
                    case "-":
                        deleteCommand(words, bTree);
                        break;
                    default:
                        System.out.println("Invalid input .");
                }

            }
            if(gradingMode) Clock.stop();
            if(gradingMode) System.out.println("Elapsed Time (ms): " + Clock.getElapsedTimeInMilliSec());
        } catch (Utils.TreeIsEmptyException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void queryTestCommand(String[] tokens, BTree bTree) {
        if (tokens.length != 2) {
            System.out.println("Invalid number of arguments.");
            return;
        }

        String queriesFileName = tokens[1];
        String[] stringArray = readFile.readData(queriesFileName);
        try {

            //Do the search
            //Just time the search in grading
            if(gradingMode) Clock.start();
            for (String str : stringArray) {
                String[] words = str.split("\\s+");
                System.out.println(str);
                List<Integer> result = bTree.search(new Integer(words[0]),
                        new Integer(words[1]));

                if (result.isEmpty()) {
                    System.out.println("No result for range " + tokens[1] + " - " + tokens[2]);
                    return;
                }
                if (!gradingMode) {
                    System.out.println("Result (" + result.size() + " data(s)): ");
                    System.out.println(result.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "))
                    );
                }
            }
            if(gradingMode) Clock.stop();
            if(gradingMode) System.out.println("Elapsed Time (ms): " + Clock.getElapsedTimeInMilliSec());
        } catch (Utils.TreeIsEmptyException e) {
            System.out.println(e.getMessage());
        }

    }

    private static boolean gradingMode = false;
    
}
