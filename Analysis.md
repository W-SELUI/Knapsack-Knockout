# CS214 Assignment 2 Analysis

## 1. Purpose

This project compares two ways of solving the 0/1 knapsack problem:

- **Dynamic Programming (DP):** a deterministic method that always returns the exact best profit.
- **Genetic Algorithm (GA):** a non-deterministic search method that may produce different results on different runs.

The supplied benchmark files P01–P08 are used. The Pisinger instances are not included in this comparison because the course coordinator clarified that Q2 uses only P01–P08.

## 2. Algorithm choices

### Dynamic Programming

The DP table stores the best profit possible for each item prefix and each capacity. For a problem with `n` items and capacity `W`, the normal DP work is `O(nW)`.

The detailed DP output uses a full two-dimensional table so that the selected items can be traced backwards. The benchmark and experiment runners use a compact one-dimensional table so that the large capacity in P08 can be processed without storing a very large two-dimensional table. It is still the same DP recurrence and still performs `O(nW)` work.

The DP NFC counter records one count for each capacity calculation. Therefore, the benchmark NFC is `n × W` using the counter definition chosen for this project.

### Genetic Algorithm

Each chromosome is a binary string. A `1` means that the item is selected, and a `0` means that it is not selected.

The GA uses:

- Population size: 50 chromosomes
- 200 generations
- Fitness-proportionate parent selection
- One-point crossover
- Mutation rate: 0.05 per bit
- Elitism, which keeps the best chromosome found
- Fitness `0` for an overweight chromosome

The GA NFC counter records one count whenever a chromosome's fitness is evaluated. With elitism, the total for one run is:

```text
50 initial evaluations + (200 generations × 49 new evaluations)
= 9,850 GA NFC
```

The GA NFC is therefore a count of fitness evaluations. It is not exactly the same physical operation as one DP table-cell calculation, so NFC is most useful for observing each algorithm's work growth and progress.

## 3. Q2 benchmark check

The Q2 runner successfully reads and processes all eight supplied benchmark files. DP reached the published optimum for every problem, and every GA result produced by the benchmark runner was valid (its weight did not exceed the capacity).

The DP results were:

| Problem | Capacity | Known optimum | DP profit | DP weight | DP NFC |
|---|---:|---:|---:|---:|---:|
| P01 | 165 | 309 | 309 | 165 | 1,650 |
| P02 | 26 | 51 | 51 | 26 | 130 |
| P03 | 190 | 150 | 150 | 190 | 1,140 |
| P04 | 50 | 107 | 107 | 50 | 350 |
| P05 | 104 | 900 | 900 | 104 | 832 |
| P06 | 170 | 1,735 | 1,735 | 169 | 1,190 |
| P07 | 750 | 1,458 | 1,458 | 749 | 11,250 |
| P08 | 6,404,180 | 13,549,094 | 13,549,094 | 6,402,560 | 153,700,320 |

The selected weight does not need to equal the capacity. It only needs to be less than or equal to the capacity. This explains the weights of 749 for P07 and 6,402,560 for P08.

P08 requires far more DP calculations because its capacity is very large. This demonstrates why DP becomes less efficient as `W` increases, even though the compact version can still complete this supplied instance.

## 4. Q3 experiment

Each problem was run 30 times with the GA. The same seeded settings were used for each problem so that the experiment can be repeated. DP was run once per problem because it is deterministic and returns the same answer each time.

The summary results are:

| Problem | DP profit | GA best | GA mean | GA worst | GA success rate |
|---|---:|---:|---:|---:|---:|
| P01 | 309 | 309 | 309.00 | 309 | 100% |
| P02 | 51 | 51 | 51.00 | 51 | 100% |
| P03 | 150 | 150 | 150.00 | 150 | 100% |
| P04 | 107 | 107 | 107.00 | 107 | 100% |
| P05 | 900 | 900 | 900.00 | 900 | 100% |
| P06 | 1,735 | 1,735 | 1,735.00 | 1,735 | 100% |
| P07 | 1,458 | 1,458 | 1,457.57 | 1,455 | 80% |
| P08 | 13,549,094 | 13,521,334 | 13,435,519.87 | 13,330,392 | 0% |

### Interpretation

- P01–P06 are small or easier instances for these GA settings. All 30 runs reached the known optimum.
- P07 is more difficult. The GA reached the optimum in 24 of 30 runs. Even when it missed, its mean profit was very close to the optimum.
- P08 is the most difficult instance. None of the 30 GA runs reached the exact optimum. The best GA result was only about 0.205% below the optimum, while the mean was about 0.838% below it. This shows that a GA can find a very good answer without guaranteeing the exact answer.
- DP returned the same exact answer for every problem. This demonstrates DP's reliability, while the changing GA results demonstrate its random nature.

The GA used 9,850 NFC in every run. Its work budget was fixed, so the harder problems had to be solved with the same number of fitness evaluations as the easier problems. The CSV files contain the individual run results and the per-run timing measurements.

## 5. Q4 live graph

The Q4 dashboard allows the user to select P01–P08 and run DP only, GA only, or both algorithms together.

- The horizontal axis is NFC, representing the amount of counted algorithm work.
- The vertical axis is the best valid profit found so far.
- The dashed line is the published known optimum.
- DP reports progress after each item row.
- GA reports progress after each generation.

An upward section means that a better item combination has been found. A horizontal section means that the algorithm is still working but has not found a better best-so-far solution. GA commonly rises quickly and then becomes horizontal. DP commonly rises in more systematic steps because it processes the table row by row.

The animation delay exists only to make the progress visible in the GUI. It is not used in the Q3 timing experiment.

## 6. Overall conclusion

DP is deterministic and exact, but its work increases with the capacity `W`. The P08 NFC count shows the cost of a large capacity.

GA uses a fixed search budget and can find excellent solutions quickly. It reached every optimum on P01–P06, reached the optimum in most P07 runs, and produced near-optimal but not exact results on P08. Its success rate and final answer can change because the search contains random choices.

Therefore, the comparison shows the main trade-off:

> DP provides a guaranteed exact answer when its `n × W` work is manageable, while GA can handle the search with a fixed budget but does not guarantee the optimum.
