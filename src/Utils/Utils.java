package Utils;

public class Utils {
    // Some typical exceptions here

    /**
     * catch exception when key is not found in the tree
     */
    public static class KeyNotFoundException extends RuntimeException {
        public KeyNotFoundException(String key) {
            super("The key \"" + key + "\" is not in the B+-tree.");
        }
    }

    /**
     * catch exception when key exists in the tree
     */
    public static class DuplicateKeyException extends RuntimeException {
        public DuplicateKeyException(String key) {
            super("The key \"" + key + "\" is already in the B+-tree!");
        }
    }

    /**
     * catch exception when tree is empty
     */
    public static class TreeIsEmptyException extends RuntimeException {
        public TreeIsEmptyException() {
            super("The B+-tree is empty!");
        }
    }

}
