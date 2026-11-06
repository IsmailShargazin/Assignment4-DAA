import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AlgorithmTests {

    private static int passed;

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }

        passed++;
    }

    private static void checkMatching(
            String text,
            String pattern,
            List<Integer> expected
    ) {
        List<Integer> naive =
                StringMatching.naive(text, pattern).positions;

        List<Integer> kmp =
                StringMatching.kmp(text, pattern).positions;

        List<Integer> rk =
                StringMatching.rabinKarp(text, pattern).positions;

        check(
                naive.equals(expected),
                "Naive: " + text + " / " + pattern
        );

        check(
                kmp.equals(expected),
                "KMP: " + text + " / " + pattern
        );

        check(
                rk.equals(expected),
                "Rabin-Karp: " + text + " / " + pattern
        );

        check(
                naive.equals(kmp) && kmp.equals(rk),
                "Matching agreement"
        );
    }

    private static void testLcs() {
        check(LCS.length("abc", "abc") == 3, "LCS identical");

        check(LCS.length("abc", "xyz") == 0, "LCS different");

        check(LCS.length("", "abc") == 0, "LCS empty first");

        check(LCS.length("abc", "") == 0, "LCS empty second");

        check(LCS.length("", "") == 0, "LCS both empty");

        check(LCS.length("abcde", "ace") == 3, "LCS subsequence");

        check(LCS.length("AGGTAB", "GXTXAYB") == 4, "LCS general");
    }

    private static void testEditDistance() {
        check(
                EditDistance.distance("abc", "abc") == 0,
                "Edit identical"
        );

        check(
                EditDistance.distance("", "abc") == 3,
                "Edit empty first"
        );

        check(
                EditDistance.distance("abc", "") == 3,
                "Edit empty second"
        );

        check(
                EditDistance.distance("", "") == 0,
                "Edit both empty"
        );

        check(
                EditDistance.distance("cat", "cut") == 1,
                "Substitution"
        );

        check(
                EditDistance.distance("cat", "cart") == 1,
                "Insertion"
        );

        check(
                EditDistance.distance("cart", "cat") == 1,
                "Deletion"
        );

        check(
                EditDistance.distance("kitten", "sitting") == 3,
                "Mixed edits"
        );
    }

    private static void testMatching() {
        checkMatching("abcxyz", "abc", List.of(0));
        checkMatching("xxabcxx", "abc", List.of(2));
        checkMatching("xyzabc", "abc", List.of(3));

        checkMatching("abcabcabc", "abc", List.of(0, 3, 6));
        checkMatching("aaaaa", "aaa", List.of(0, 1, 2));

        checkMatching("abcdef", "xyz", List.of());
        checkMatching("ab", "abcd", List.of());

        checkMatching("abc", "", List.of(0, 1, 2, 3));
        checkMatching("", "a", List.of());
        checkMatching("", "", List.of(0));

        checkMatching(
                "ABABDABACDABABCABAB",
                "ABABCABAB",
                List.of(10)
        );

        checkMatching("AAACAAAA", "AAAA", List.of(4));

        checkMatching("bB", "aa", List.of());

        Random random = new Random(123);

        for (int i = 0; i < 100; i++) {
            String text = randomString(random, random.nextInt(41));
            String pattern = randomString(random, random.nextInt(9));

            List<Integer> expected =
                    StringMatching.naive(text, pattern).positions;

            checkMatching(text, pattern, expected);
        }
    }

    private static void testKasai() {
        int[] sa = {6, 5, 3, 1, 0, 4, 2};
        int[] expected = {0, 0, 1, 3, 0, 0, 2};

        check(
                Arrays.equals(SuffixArray.build("banana$"), sa),
                "Suffix array banana"
        );

        check(
                Arrays.equals(Kasai.buildLcp("banana$", sa), expected),
                "Kasai banana"
        );

        check(
                Kasai.buildLcp("", new int[0]).length == 0,
                "Kasai empty"
        );

        check(
                Arrays.equals(
                        Kasai.buildLcp("x", new int[]{0}),
                        new int[]{0}
                ),
                "Kasai one character"
        );

        check(
                Arrays.equals(
                        Kasai.buildLcp("aaaa", new int[]{3, 2, 1, 0}),
                        new int[]{0, 1, 2, 3}
                ),
                "Kasai repeated characters"
        );

        Random random = new Random(456);

        for (int test = 0; test < 100; test++) {
            String text = randomString(random, random.nextInt(41));

            int[] suffixes = SuffixArray.build(text);
            int[] actual = Kasai.buildLcp(text, suffixes);

            int[] reference = new int[text.length()];

            for (int i = 1; i < suffixes.length; i++) {
                int a = suffixes[i - 1];
                int b = suffixes[i];

                while (a + reference[i] < text.length()
                        && b + reference[i] < text.length()
                        && text.charAt(a + reference[i])
                        == text.charAt(b + reference[i])) {

                    reference[i]++;
                }
            }

            check(
                    Arrays.equals(actual, reference),
                    "Kasai random " + test
            );
        }
    }

    private static String randomString(Random random, int length) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < length; i++) {
            text.append((char) ('a' + random.nextInt(3)));
        }

        return text.toString();
    }

    public static void main(String[] args) {
        passed = 0;

        testLcs();
        testEditDistance();
        testMatching();
        testKasai();

        System.out.println(
                "ALL TESTS PASSED: " + passed + " checks"
        );
    }
}