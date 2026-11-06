import java.util.Arrays;

public class SuffixArray {

    public static int[] build(String text) {
        Integer[] indices = new Integer[text.length()];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        Arrays.sort(
                indices,
                (a, b) -> compareSuffixes(text, a, b)
        );

        int[] suffixArray = new int[indices.length];

        for (int i = 0; i < indices.length; i++) {
            suffixArray[i] = indices[i];
        }

        return suffixArray;
    }

    private static int compareSuffixes(String text, int a, int b) {
        int n = text.length();

        while (a < n && b < n) {
            int difference = Character.compare(
                    text.charAt(a),
                    text.charAt(b)
            );

            if (difference != 0) {
                return difference;
            }

            a++;
            b++;
        }

        return Integer.compare(n - a, n - b);
    }
}