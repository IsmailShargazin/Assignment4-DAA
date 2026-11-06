import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class Benchmark {

    private static final int RUNS = 5;
    private static final int WARMUP = 5;

    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyz";

    private static final long SEED = 42;

    private static volatile long sink;

    private static String randomString(Random random, int length) {
        StringBuilder text = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHABET.length());
            text.append(ALPHABET.charAt(index));
        }

        return text.toString();
    }

    private static double timeDp(IntSupplier algorithm) {
        for (int i = 0; i < WARMUP; i++) {
            sink = algorithm.getAsInt();
        }

        long total = 0;

        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();

            int result = algorithm.getAsInt();

            long elapsed = System.nanoTime() - start;

            sink = result;
            total += elapsed;
        }

        return total / (double) RUNS;
    }

    private static double timeSearch(Supplier<SearchResult> algorithm) {
        for (int i = 0; i < WARMUP; i++) {
            sink = algorithm.get().positions.size();
        }

        long total = 0;

        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();

            SearchResult result = algorithm.get();

            long elapsed = System.nanoTime() - start;

            sink = result.positions.size();
            total += elapsed;
        }

        return total / (double) RUNS;
    }

    private static double timeKasai(
            String text,
            int[] suffixArray
    ) {
        for (int i = 0; i < WARMUP; i++) {
            int[] result = Kasai.buildLcp(text, suffixArray);
            sink = result[result.length - 1];
        }

        long total = 0;

        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();

            int[] result = Kasai.buildLcp(text, suffixArray);

            long elapsed = System.nanoTime() - start;

            sink = result[result.length - 1];
            total += elapsed;
        }

        return total / (double) RUNS;
    }

    private static void searchRow(
            StringBuilder csv,
            String name,
            String type,
            String text,
            String pattern,
            Supplier<SearchResult> algorithm
    ) {
        SearchResult sample = algorithm.get();
        double average = timeSearch(algorithm);

        csv.append(String.format(
                Locale.US,
                "%s,%s,%d,%d,%.2f,%d,%d%n",
                name,
                type,
                text.length(),
                pattern.length(),
                average,
                sample.comparisons,
                sample.positions.size()
        ));
    }

    private static void benchmarkDp(Random random) throws IOException {
        StringBuilder csv = new StringBuilder(
                "algorithm,length,average_ns\n"
        );

        for (int size : new int[]{100, 500, 1000}) {
            String a = randomString(random, size);
            String b = randomString(random, size);

            double lcs = timeDp(() -> LCS.length(a, b));

            double edit = timeDp(
                    () -> EditDistance.distance(a, b)
            );

            csv.append(String.format(
                    Locale.US,
                    "LCS,%d,%.2f%n",
                    size,
                    lcs
            ));

            csv.append(String.format(
                    Locale.US,
                    "EditDistance,%d,%.2f%n",
                    size,
                    edit
            ));
        }

        Files.writeString(
                Path.of("results", "dp.csv"),
                csv
        );
    }

    private static void benchmarkMatching(
            Random random
    ) throws IOException {
        StringBuilder csv = new StringBuilder(
                "algorithm,case,text_length,pattern_length,"
                        + "average_ns,comparisons,matches\n"
        );

        for (int size : new int[]{10000, 50000, 100000}) {
            String text = randomString(random, size);

            String present = text.substring(
                    size / 2,
                    size / 2 + 20
            );

            String absent;

            do {
                absent = randomString(random, 20);
            } while (
                    !StringMatching.naive(text, absent)
                            .positions.isEmpty()
            );

            for (String type : new String[]{"present", "absent"}) {
                String pattern = type.equals("present")
                        ? present : absent;

                searchRow(
                        csv, "Naive", type, text, pattern,
                        () -> StringMatching.naive(text, pattern)
                );

                searchRow(
                        csv, "KMP", type, text, pattern,
                        () -> StringMatching.kmp(text, pattern)
                );

                searchRow(
                        csv, "RabinKarp", type, text, pattern,
                        () -> StringMatching.rabinKarp(text, pattern)
                );
            }
        }

        Files.writeString(
                Path.of("results", "matching.csv"),
                csv
        );
    }

    private static void benchmarkKasai(
            Random random
    ) throws IOException {
        StringBuilder csv = new StringBuilder(
                "length,average_ns\n"
        );

        for (int size : new int[]{1000, 5000, 10000}) {
            String text = randomString(random, size);

            int[] suffixArray = SuffixArray.build(text);

            double average = timeKasai(text, suffixArray);

            csv.append(String.format(
                    Locale.US,
                    "%d,%.2f%n",
                    size,
                    average
            ));
        }

        Files.writeString(
                Path.of("results", "kasai.csv"),
                csv
        );
    }

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("results"));

        Random random = new Random(SEED);

        benchmarkDp(random);
        benchmarkMatching(random);
        benchmarkKasai(random);

        String setup =
                "Seed: " + SEED
                        + "\nAlphabet: " + ALPHABET
                        + "\nTimed runs: " + RUNS
                        + "\nWarmup runs: " + WARMUP
                        + "\nJava: " + System.getProperty("java.version")
                        + "\nOS: " + System.getProperty("os.name")
                        + "\nArchitecture: " + System.getProperty("os.arch")
                        + "\nAvailable processors: "
                        + Runtime.getRuntime().availableProcessors()
                        + "\n";

        Files.writeString(
                Path.of("results", "environment.txt"),
                setup
        );

        System.out.println(
                "Benchmarks finished. Files saved in results/"
        );
    }
}