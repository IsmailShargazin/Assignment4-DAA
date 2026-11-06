# Assignment 4: Text Analysis

## 1. Introduction

In this assignment, I implemented algorithms for comparing strings and finding patterns in text.

The project includes:

- Longest Common Subsequence (LCS).
- Edit Distance.
- Naive string matching.
- Knuth-Morris-Pratt (KMP).
- Rabin-Karp.
- Kasai's algorithm for the LCP array.

The goal was to check their correctness, analyse their complexity and compare their execution times.

The application is written in Java. The experiments were run using Java 25.0.1.

## 2. Project structure

| File | Description |
| --- | --- |
| `src/Main.java` | Demonstrates the algorithms using short examples |
| `src/LCS.java` | Calculates the length of the longest common subsequence |
| `src/EditDistance.java` | Calculates Levenshtein distance |
| `src/SearchResult.java` | Stores matching positions and character comparisons |
| `src/StringMatching.java` | Contains Naive, KMP and Rabin-Karp |
| `src/Kasai.java` | Builds the LCP array |
| `src/SuffixArray.java` | Builds suffix arrays for the experiments |
| `src/AlgorithmTests.java` | Checks normal and edge cases |
| `src/Benchmark.java` | Measures execution time and saves results |
| `results/` | Contains CSV results and environment information |
| `plots/` | Contains experimental graphs |
| `plot_results.py` | Generates graphs from the CSV files |

All Java classes are in the default package. No additional Java libraries are required.

## 3. Implementation decisions and assumptions

The implementation follows these rules:

- Input strings must not be `null`.
- Empty strings are supported.
- String matching is case-sensitive.
- Indices start from 0.
- Matching methods return all starting positions in increasing order.
- Overlapping matches are included.
- An empty pattern matches every boundary of the text.
- A pattern longer than the text returns an empty list.
- Character operations and indices use Java UTF-16 `char` units.
- In Edit Distance, insertion, deletion and substitution each cost 1.
- Matching characters have a cost of 0.

For example:

```text
Text: aaaaa
Pattern: aaa
Positions: [0, 1, 2]
```

For an empty pattern:

```text
Text: abc
Pattern: ""
Positions: [0, 1, 2, 3]
```

For an empty text and an empty pattern, the result is `[0]`.

### 3.1. Longest Common Subsequence

LCS finds the length of the longest sequence shared by two strings.

The characters must keep their order, but they do not need to be next to each other.

Example:

```text
A = abcde
B = ace

LCS = ace
Length = 3
```

The program uses a two-dimensional table.

`dp[i][j]` represents the LCS length for the first `i` characters of A and the first `j` characters of B.

Base cases:

```text
dp[0][j] = 0
dp[i][0] = 0
```

Recurrence:

```text
If A[i - 1] == B[j - 1]:

    dp[i][j] = dp[i - 1][j - 1] + 1

Otherwise:

    dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
```

An empty prefix has no common characters.

If the last characters match, an optimal subsequence can include that pair after a solution for the shorter prefixes.

If the characters differ, a common subsequence must skip at least one of them. The recurrence checks both choices and keeps the larger result.

The table is filled in an order where all required smaller results are already known.

The final answer is `dp[n][m]`.

### 3.2. Edit Distance

Edit Distance finds the minimum number of operations needed to change one string into another.

Allowed operations are:

- Insertion.
- Deletion.
- Substitution.

Example:

```text
kitten -> sitten -> sittin -> sitting

Edit Distance = 3
```

`dp[i][j]` stores the minimum cost of changing the first `i` characters of A into the first `j` characters of B.

Base cases:

```text
dp[i][0] = i
dp[0][j] = j
```

Recurrence:

```text
cost = 0 if A[i - 1] == B[j - 1], otherwise 1

dp[i][j] = min(
    dp[i - 1][j] + 1,
    dp[i][j - 1] + 1,
    dp[i - 1][j - 1] + cost
)
```

The three options represent deletion, insertion and substitution or a match.

Changing a prefix into an empty string requires deletions. Creating a prefix from an empty string requires insertions.

For non-empty prefixes, the final step must be one of the options in the recurrence. Each option leaves a smaller problem. Taking the minimum gives the optimal edit cost.

### 3.3. Naive string matching

Naive matching checks the pattern at every possible starting position.

It compares characters until a mismatch occurs or the complete pattern matches.

Example:

```text
Text: abcabcabc
Pattern: abc
Positions: [0, 3, 6]
```

This method is used as the baseline for comparison.

### 3.4. KMP

KMP uses an LPS array.

`lps[i]` is the length of the longest proper prefix of `pattern[0..i]` that is also its suffix.

When a mismatch occurs after `j` matched characters, KMP updates:

```text
j = lps[j - 1]
```

This allows the algorithm to reuse information about a matched prefix. The text index never moves backwards.

After finding a complete match, KMP also uses the LPS array to continue searching. This preserves possible overlapping occurrences.

### 3.5. Rabin-Karp

Rabin-Karp compares the pattern hash with the hash of each text window.

The implementation uses:

```text
Base = 31
Modulus = 1,000,000,007
```

Hash calculations use `long`.

When the window moves, the algorithm removes the outgoing character, multiplies by the base and adds the incoming character.

This updates the hash in constant time.

Different strings can have equal hashes. Therefore, the program compares the characters whenever hashes match.

The collision test uses:

```text
Text: bB
Pattern: aa
```

These strings have the same hash with base 31, but character verification correctly rejects the match.

### 3.6. Kasai and the LCP array

A suffix array contains the starting indices of suffixes in lexicographic order.

The LCP array stores the common-prefix lengths of neighbouring suffixes in this order.

This project uses:

```text
lcp[0] = 0
lcp[i] = LCP(sa[i - 1], sa[i])
```

Manually checked example:

```text
String: banana$

Suffix array: [6, 5, 3, 1, 0, 4, 2]
LCP array:    [0, 0, 1, 3, 0, 0, 2]
```

| Position | Suffix index | Suffix | LCP with previous suffix |
| --- | --- | --- | --- |
| 0 | 6 | `$` | 0 |
| 1 | 5 | `a$` | 0 |
| 2 | 3 | `ana$` | 1 |
| 3 | 1 | `anana$` | 3 |
| 4 | 0 | `banana$` | 0 |
| 5 | 4 | `na$` | 0 |
| 6 | 2 | `nana$` | 2 |

Kasai first builds the inverse array `rank`.

It visits suffixes in text order and compares each suffix with its previous neighbour in suffix-array order.

The variable `k` stores the current common-prefix length. After moving one character forward, the algorithm can reuse at least the previous length minus one.

The total increases and decreases of `k` are linear, so the algorithm runs in O(n) time.

The method checks the suffix-array length, index range and duplicate indices. It assumes the supplied suffix array is sorted correctly.

The code does not require a sentinel. The `$` symbol is used only in the example.

### 3.7. Suffix-array construction

The assignment does not require an efficient suffix-array construction algorithm.

The helper class sorts suffix indices using a character-by-character comparator. It does not create copies of all suffix strings.

Its worst-case time is O(n² log n), and its auxiliary space is O(n).

Suffix-array construction is performed before timing Kasai and is excluded from its measurements.

## 4. Complexity analysis

For LCS and Edit Distance, `n` and `m` are the two string lengths.

For string matching:

- `n` is the text length.
- `m` is the pattern length.
- `z` is the number of returned occurrences.

Matching bounds below assume `1 <= m <= n`.

Auxiliary space excludes the returned output.

| Algorithm | Main operation | Preprocessing | Best-case time | Worst-case time | Auxiliary space |
| --- | --- | --- | --- | --- | --- |
| LCS | Match: diagonal + 1; otherwise maximum of top and left | Table allocation | Θ(nm) | Θ(nm) | O(nm) |
| Edit Distance | Minimum of deletion, insertion and substitution/match | Table allocation and base cases | Θ(nm) | Θ(nm) | O(nm) |
| Naive | Compare pattern at every offset | O(1) | Θ(n − m + 1) search | Θ((n − m + 1)m) search | O(1) |
| KMP | Build LPS and follow prefix fallbacks | Θ(m) | Θ(n + m) total | Θ(n + m) total | O(m) |
| Rabin-Karp | Update rolling hash and verify candidates | Θ(m) | Θ(n + m) total | O(nm) total | O(1) |
| Kasai | Build rank and reuse the previous prefix length | Θ(n) for rank | Θ(n) total | Θ(n) total | O(n) |

### Explanation of the bounds

**LCS and Edit Distance:** both process all `n × m` non-base cells, with constant work per cell.

The exact table size is `(n + 1)(m + 1)`. The O(nm) bounds describe positive lengths. Empty inputs still require allocation and base handling.

**Naive:** in the best case, each position fails at the first comparison. In the worst case, each position needs all `m` comparisons.

**KMP:** building LPS takes Θ(m) time. Searching takes Θ(n) time because the text index moves forward and the total fallback work is linear.

**Rabin-Karp:** preprocessing takes Θ(m) time, and each hash update takes constant time. Candidate verification adds character comparisons.

On random inputs with few matches and rare collisions, expected total time is O(n + m).

A more explicit upper bound is:

```text
O(n + m + (z + c)m)
```

Here, `c` is the number of false hash candidates.

Many actual overlapping matches or hash collisions can lead to O(nm) worst-case time.

**Kasai:** building the rank array takes Θ(n) time. Reusing prefix lengths keeps the total comparison work linear.

### Output and edge cases

All matching algorithms use O(z) space for returned indices.

An empty pattern requires O(n) time and output space because all `n + 1` boundaries are returned.

A non-empty pattern longer than the text is rejected without searching.

## 5. Testing

The tests use explicit checks. If a check fails, the program throws `AssertionError` with the test name.

JUnit and the `-ea` option are not required.

| Test group | Cases | Checks |
| --- | --- | --- |
| LCS | Identical, different, empty strings and known subsequences | 7 |
| Edit Distance | Identical, empty strings, insertion, deletion, substitution and mixed edits | 8 |
| Fixed matching | Beginning, middle, end, multiple and overlapping matches, absent/empty/long patterns, empty text, fallback examples and collision | 52 |
| Random matching | 100 generated pairs, seed 123, alphabet a-c | 400 |
| Fixed Kasai and suffix array | banana$, empty string, one character and repeated characters | 5 |
| Random Kasai | 100 generated strings, seed 456 | 100 |
| **Total** | | **572** |

Every matching case checks each algorithm against the expected list and verifies agreement between all three algorithms.

Random matching tests use Naive as a reference. Fixed tests provide independently known expected positions.

Random Kasai tests compare its result with direct comparisons of neighbouring suffixes.

Observed output:

```text
ALL TESTS PASSED: 572 checks
```

The demonstration, tests and benchmarks all finished with exit code 0.

## 6. Experimental setup

| Setting | Value |
| --- | --- |
| Java version | 25.0.1 |
| Operating system | Mac OS X |
| Architecture | aarch64 |
| Available processors reported by Java | 10 |
| Random seed | 42 |
| Alphabet | abcdefghijklmnopqrstuvwxyz |
| Warmup runs | 5 per experiment |
| Timed runs | 5 per experiment |
| Timer | System.nanoTime() |

Required workloads:

| Experiment | Input sizes |
| --- | --- |
| LCS | Two strings of length 100, 500 and 1,000 |
| Edit Distance | Two strings of length 100, 500 and 1,000 |
| String matching | Text lengths 10,000, 50,000 and 100,000 |
| Kasai | String lengths 1,000, 5,000 and 10,000 |

The matching pattern length is 20.

### Input generation

One `Random` instance with seed 42 is used. Running the benchmark in the same order generates the same inputs.

LCS and Edit Distance receive the same string pairs.

For each matching comparison, all three methods receive exactly the same text and pattern.

A present pattern is taken from the middle of the text.

An absent pattern is generated from the same alphabet and checked with Naive before timing.

In this run, every present pattern had one occurrence, and every absent pattern had zero occurrences.

### Timing methodology

Each experiment first performs five warmup calls.

Then it performs five timed calls and calculates:

```text
Average time = total elapsed time / 5
```

Input generation, console output, file operations and result consumption through `sink` are outside timed regions.

Algorithm allocations and comparison counters are included.

KMP timing includes LPS construction.

Rabin-Karp timing includes initial hash calculations.

Kasai timing includes rank construction and input-index validation. Suffix-array sorting is excluded.

CSV files store nanoseconds. The report uses milliseconds:

```text
milliseconds = nanoseconds / 1,000,000
```

Table values are rounded to six decimal places.

### Operation counts

Comparison counts are taken from an additional untimed call on the same input.

They count explicit character comparisons:

- Naive counts pattern-to-text comparisons.
- KMP also counts comparisons during LPS construction.
- Rabin-Karp counts comparisons during candidate verification.

Hash arithmetic and hash comparisons are not included.

Therefore, these counts do not represent all operations performed by the algorithms.

### Limitations

Five warmup calls may not be enough for JVM compilation to finish.

Scheduling, garbage collection, caching and CPU frequency changes can also affect timing.

Only averages were saved, so individual-run variability is not available.

## 7. Experimental results

### 7.1. LCS and Edit Distance

| Length of each string | LCS (ms) | Edit Distance (ms) |
| --- | --- | --- |
| 100 | 0.231900 | 0.333283 |
| 500 | 0.541016 | 0.790158 |
| 1,000 | 2.080933 | 2.525033 |

![LCS and Edit Distance execution time](./plots/dp_time.png)

### 7.2. String matching: pattern present

| Text length | Naive (ms) | KMP (ms) | Rabin-Karp (ms) |
| --- | --- | --- | --- |
| 10,000 | 0.120392 | 0.244408 | 0.377741 |
| 50,000 | 0.058508 | 0.500708 | 0.482300 |
| 100,000 | 0.117125 | 0.177792 | 0.767267 |

### 7.3. String matching: pattern absent

| Text length | Naive (ms) | KMP (ms) | Rabin-Karp (ms) |
| --- | --- | --- | --- |
| 10,000 | 0.103942 | 0.091792 | 0.171750 |
| 50,000 | 0.057675 | 0.144742 | 0.376508 |
| 100,000 | 0.117783 | 0.178850 | 0.768325 |

![String matching execution time](./plots/matching_time.png)

### 7.4. Character comparisons

| Text length | Case | Naive | KMP | Rabin-Karp | Matches per algorithm |
| --- | --- | --- | --- | --- | --- |
| 10,000 | present | 10,388 | 10,390 | 20 | 1 |
| 10,000 | absent | 10,400 | 10,419 | 0 | 0 |
| 50,000 | present | 52,067 | 52,027 | 20 | 1 |
| 50,000 | absent | 52,029 | 51,990 | 0 | 0 |
| 100,000 | present | 104,032 | 103,894 | 20 | 1 |
| 100,000 | absent | 104,058 | 103,944 | 0 | 0 |

### 7.5. Kasai

| String length | Kasai (ms) |
| --- | --- |
| 1,000 | 0.071592 |
| 5,000 | 0.135792 |
| 10,000 | 0.164784 |

![Kasai execution time](./plots/kasai_time.png)

## 8. Discussion

### LCS and Edit Distance

Both algorithms become slower as the strings grow.

Between lengths 500 and 1,000, LCS time increases by approximately 3.85 times, while Edit Distance increases by approximately 3.20 times.

The number of table cells increases four times. This part of the measurements is broadly consistent with quadratic growth.

The results do not follow an exact quadratic curve. Fixed overhead and JVM effects may have more influence on small inputs.

Edit Distance is slower than LCS at every tested size in this run. Its transition calculates three possible edit costs.

### Naive and KMP

Random text over 26 letters usually produces early mismatches.

Because of this, Naive makes approximately n comparisons on these inputs, although its worst-case complexity is O(nm).

For a text of length 100,000 with the pattern present:

```text
Naive comparisons: 104,032
KMP comparisons:   103,894
```

The difference is small because these inputs contain few long repeated prefixes.

Naive is faster at the two larger sizes in both cases. KMP is faster for the absent pattern at size 10,000.

This does not change KMP's O(n + m) worst-case guarantee.

### Rabin-Karp

Rabin-Karp performs only 20 character comparisons for each present pattern and zero for each absent pattern.

However, it still updates and compares hashes across the text.

At length 100,000, its time is approximately 0.767 ms for the present pattern and 0.768 ms for the absent pattern.

This shows why fewer character comparisons do not necessarily mean faster execution. Hash arithmetic is extra work that the character-comparison counter does not record.

### Timing irregularities

Some larger inputs run faster than smaller inputs.

For example, KMP with the pattern present takes approximately:

```text
50,000 characters:  0.501 ms
100,000 characters: 0.178 ms
```

Naive also becomes faster between 10,000 and 50,000 characters.

JVM warmup and measurement noise are possible explanations. The saved averages do not identify the exact cause.

The report keeps these values as measured.

Comparison counts show a clearer near-linear trend on these random inputs.

More warmup, separate benchmark processes and individual timing records would improve a follow-up experiment.

### Kasai

Kasai time increases from approximately 0.072 ms at length 1,000 to 0.165 ms at length 10,000.

This increase is not proportional to input size in this small run.

Its O(n) complexity follows from the analysis of the reused prefix length. Three measured points alone do not prove the bound.

The measurements include only LCP construction with a supplied suffix array.

The graph lines connect measured points. They are not fitted complexity curves.

Also, random text with a fixed pattern length of 20 does not demonstrate the worst cases of Naive or Rabin-Karp.

## 9. Conclusion

The DP table approach provides clear solutions for LCS and Edit Distance, but requires O(nm) memory.

LCS finds shared character order. Edit Distance measures how many changes are needed between strings.

Naive is simple and can be fast when mismatches happen early.

KMP provides predictable linear total time and is useful for repetitive inputs.

Rabin-Karp reuses hashes, but needs character verification for correctness. Hash calculations can also make it slower on simple random data.

Kasai builds the LCP array in linear time after a suffix array is available.

The main trade-offs are memory use, preprocessing cost, worst-case guarantees and constant factors.

## 10. Compile and run

From the project root:

```bash
javac --release 17 -d out src/*.java
java -cp out Main
java -cp out AlgorithmTests
java -cp out Benchmark
```

The `--release 17` option also works with JDK 25 and keeps the compiled classes compatible with Java 17.

In IntelliJ IDEA, select the installed JDK and run:

1. `Main`
2. `AlgorithmTests`
3. `Benchmark`

Set the working directory to the project root.

The benchmark creates:

```text
results/dp.csv
results/matching.csv
results/kasai.csv
results/environment.txt
```

Running the benchmark again produces the same inputs, but execution times may change.

If the measurements change, regenerate the plots and update the report tables and discussion.

## 11. Generate graphs

The Python script is used only for plotting. The Java application does not depend on Python.

On macOS, run these commands from the project root:

```bash
python3 -m venv .venv
source .venv/bin/activate
python3 -m pip install matplotlib
python3 plot_results.py
```

If the virtual environment already exists, start from:

```bash
source .venv/bin/activate
```

The script creates:

```text
plots/dp_time.png
plots/matching_time.png
plots/kasai_time.png
```

Keep the `plots` folder next to `README.md` so the images display correctly.

## 12. Submission

The GitHub repository includes:

- Java source code.
- Tests.
- Experimental CSV files.
- Graphs.
- This README report.
- The plotting script.

Submit the GitHub repository link through Moodle.

This README is the full report. A separate PDF report is not required.