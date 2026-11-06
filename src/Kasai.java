import java.util.Arrays;

public class Kasai {

    public static int[] buildLcp(String text, int[] suffixArray) {
        int n = text.length();

        if (suffixArray.length != n) {
            throw new IllegalArgumentException(
                    "Wrong suffix array length"
            );
        }

        int[] rank = new int[n];
        Arrays.fill(rank, -1);

        for (int i = 0; i < n; i++) {
            int index = suffixArray[i];

            if (index < 0 || index >= n || rank[index] != -1) {
                throw new IllegalArgumentException(
                        "Invalid suffix array indices"
                );
            }

            rank[index] = i;
        }

        int[] lcp = new int[n];
        int k = 0;

        for (int i = 0; i < n; i++) {

            if (rank[i] == 0) {
                k = 0;
                continue;
            }

            int j = suffixArray[rank[i] - 1];

            while (i + k < n
                    && j + k < n
                    && text.charAt(i + k) == text.charAt(j + k)) {
                k++;
            }

            lcp[rank[i]] = k;

            if (k > 0) {
                k--;
            }
        }

        return lcp;
    }
}