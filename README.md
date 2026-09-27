# Knapsack Knockout

A CS214 project comparing Dynamic Programming (DP) and a Genetic Algorithm (GA) for the 0/1 knapsack problem.

## Current progress

The DP program loads benchmark files, calculates the exact best value, shows a bordered table, lists the chosen items, and checks the answer against a matching entry in KNOWN_OPTIMA.txt. The GA completes one full run using a binary population, fitness-based selection, one-point crossover, mutation, elitism, and a fixed generation limit. BenchmarkRunner performs the Q2 check across P01-P08 in one launch. ExperimentRunner performs the Q3 experiment with 30 seeded GA runs for each P01-P08 problem and saves raw and summary CSV files. GraphRunner opens the Q4 Swing dashboard with live DP/GA traces and controls for choosing the problem and run mode.

## Java files

| File | Job |
|---|---|
| KnapsackDP.java | Start the DP benchmark program. P01 is the default. |
| DPSolver.java | Calculate the DP table and find the chosen items. |
| BenchmarkReader.java | Read .kp files and look up published answers. |
| TablePrinter.java | Print the input, bordered table, and results. |
| ProblemData.java | Hold the item names, weights, values, and bag limit. |
| GeneticAlgorithm.java | Complete GA solver and its related chromosome, selection, crossover, mutation, and result classes. |
| GeneticAlgorithmRunner.java | Start one complete GA run. |
| BenchmarkRunner.java | Run a compact DP and GA comparison for P01-P08. |
| ExperimentRunner.java | Run the Q3 statistical experiment and save CSV results. |
| GraphRunner.java | Start the Q4 dashboard (kept as the VS Code entry point). |
| GraphDashboard.java | Provide the Q4 problem selector, run controls, result cards, and live execution. |
| LiveGraphPanel.java | Draw the live DP and GA profit lines. |

The `--demo` option on either runner uses the small camera, speaker, and console example.

## Run in VS Code

Open this Assignment 2 folder in VS Code. Java 11 or later is required.

Compile all Java source files into the `out` folder:

~~~powershell
javac -d out *.java
~~~

Run DP on P01:

~~~powershell
java -cp out KnapsackDP
~~~

Run DP on the small example:

~~~powershell
java -cp out KnapsackDP --demo
~~~

Run DP on another benchmark:

~~~powershell
java -cp out KnapsackDP benchmarks/benchmarks/p02.kp
~~~

Run the complete GA on P01:

~~~powershell
java -cp out GeneticAlgorithmRunner
~~~

Run the complete GA on the small example:

~~~powershell
java -cp out GeneticAlgorithmRunner --demo
~~~

Run the Q2 benchmark check for P01-P08:

~~~powershell
java -cp out BenchmarkRunner
~~~

The benchmark runner prints one summary row per problem. Its DP summary uses a compact one-dimensional table so that the large capacity in P08 can be tested without printing a massive DP table. Each GA row is one random run.

Run the Q3 experiment for all eight problems:

~~~powershell
java -cp out ExperimentRunner
~~~

The experiment runs the GA 30 times per problem (240 GA runs total). It saves one row per run in `results/q3_run_results.csv` and one summary row per problem in `results/q3_summary.csv`. The GA uses a different recorded seed for each run. The NFC definitions are one chromosome fitness evaluation for GA and one DP table-cell calculation for DP.

Run the Q4 live graph dashboard:

~~~powershell
java -cp out GraphRunner
~~~

The dashboard lets the user choose P01-P08 and run DP only, GA only, or both together. It also includes an animation-speed control, a clear button, a known-optimum reference line, and result cards showing profit, weight, NFC, and time. To open the dashboard with a particular benchmark selected, pass its path:

~~~powershell
java -cp out GraphRunner benchmarks/benchmarks/p08.kp
~~~

The graph uses NFC on the horizontal axis and best-so-far profit on the vertical axis. DP reports progress after each item row, while GA reports progress after each generation. The animation-speed delay is only for making the movement visible; it is not used in the Q3 measurements.

Recompile after changing Java code. All compiled `.class` files go in `out/`; the benchmark files remain in `benchmarks/benchmarks/`.

## Run buttons in VS Code

Open the Run and Debug panel and choose one of the saved configurations: **Run DP - P01**, **Run DP - Demo**, **Run GA - P01**, **Run GA - Demo**, **Run Q3 Experiment**, or **Run Q4 Dashboard**. The Q4 configuration opens the dashboard, where the problem and run mode can be changed without editing the launch settings. These configurations compile all Java files into `out/` before starting. The single-file Code Runner button is not configured for this multi-file project.

## Current GA settings

- Population size: 50 choices.
- Stopping rule: 200 generations.
- Mutation rate: 0.05 per bit.
- Overweight handling: penalty fitness of 0. The chromosome stays in the population, but cannot be selected when any valid choice has positive fitness.
- Elitism: the best choice found so far is copied into the next generation.

The GA uses a new random generator for each run, so repeated runs can produce different results. The Q3 experiment supplies a different recorded seed for each run so that the experiment can be repeated. The runner reports the known optimum for comparison but does not assume that GA must find it on every run.

## Expected results

- DP demo: camera and speaker; weight 5; value 7.
- DP P01: items 1, 2, 3, 4 and 6; weight 165; value 309.
- GA demo: a feasible choice with value up to 7.
- GA P01: a feasible choice and its comparison with the known optimum.

The DP table includes every weight limit, displayed in groups of ten columns. A row labelled "First 6 items" means those items are available to choose from.

## Development acknowledgement

OpenAI Codex assisted with explanations, implementation, refactoring, and checks of the current Java program.


