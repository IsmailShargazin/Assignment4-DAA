public class StringMatching {

    private static final long BASE = 31;
    private static final long MOD = 1_000_000_007;

    private static SearchResult emptyPattern(int textLength) {
        SearchResult result = new SearchResult();

        for (int i = 0; i <= textLength; i++) {
            result.positions.add(i);
        }

        return result;
    }

    public static SearchResult naive(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();

        if (m == 0) {
            return emptyPattern(n);
        }

        SearchResult result = new SearchResult();

        for (int i = 0; i <= n - m; i++) {
            int j = 0;

            while (j < m) {
                result.comparisons++;

                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break;
                }

                j++;
            }

            if (j == m) {
                result.positions.add(i);
            }
        }

        return result;
    }

    private static int[] buildLps(String pattern, SearchResult result) {
        int[] lps = new int[pattern.length()];

        int length = 0;
        int i = 1;

        while (i < pattern.length()) {
            result.comparisons++;

            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;

            } else if (length > 0) {
                length = lps[length - 1];

            } else {
                lps[i] = 0;
                i++;
            }
        }

        return lps;
    }

    public static SearchResult kmp(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();

        if (m == 0) {
            return emptyPattern(n);
        }

        SearchResult result = new SearchResult();

        if (m > n) {
            return result;
        }

        int[] lps = buildLps(pattern, result);

        int i = 0;
        int j = 0;

        while (i < n) {
            result.comparisons++;

            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;

                if (j == m) {
                    result.positions.add(i - m);
                    j = lps[j - 1];
                }

            } else if (j > 0) {
                j = lps[j - 1];

            } else {
                i++;
            }
        }

        return result;
    }

    public static SearchResult rabinKarp(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();

        if (m == 0) {
            return emptyPattern(n);
        }

        SearchResult result = new SearchResult();

        if (m > n) {
            return result;
        }

        long power = 1;
        long patternHash = 0;
        long windowHash = 0;

        for (int i = 0; i < m; i++) {
            patternHash = (
                    patternHash * BASE + pattern.charAt(i)
            ) % MOD;

            windowHash = (
                    windowHash * BASE + text.charAt(i)
            ) % MOD;

            if (i < m - 1) {
                power = (power * BASE) % MOD;
            }
        }

        for (int i = 0; i <= n - m; i++) {

            if (patternHash == windowHash) {
                int j = 0;

                while (j < m) {
                    result.comparisons++;

                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        break;
                    }

                    j++;
                }

                if (j == m) {
                    result.positions.add(i);
                }
            }

            if (i < n - m) {
                long first = (text.charAt(i) * power) % MOD;

                windowHash = (windowHash - first + MOD) % MOD;

                windowHash = (
                        windowHash * BASE + text.charAt(i + m)
                ) % MOD;
            }
        }

        return result;
    }
}