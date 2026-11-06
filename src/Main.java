import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        System.out.println("Assignment 4: Text Analysis");

        System.out.println(
                "LCS length: " + LCS.length("abcde", "ace")
        );

        System.out.println(
                "Edit distance: "
                        + EditDistance.distance("kitten", "sitting")
        );

        String text = "abcabcabc";
        String pattern = "abc";

        System.out.println(
                "Naive: "
                        + StringMatching.naive(text, pattern).positions
        );

        System.out.println(
                "KMP: "
                        + StringMatching.kmp(text, pattern).positions
        );

        System.out.println(
                "Rabin-Karp: "
                        + StringMatching.rabinKarp(text, pattern).positions
        );

        String word = "banana$";
        int[] suffixArray = {6, 5, 3, 1, 0, 4, 2};

        System.out.println(
                "Suffix array: " + Arrays.toString(suffixArray)
        );

        System.out.println(
                "LCP: "
                        + Arrays.toString(Kasai.buildLcp(word, suffixArray))
        );
    }
}