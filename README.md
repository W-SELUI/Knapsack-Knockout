# Knapsack Knockout

A CS214 project comparing Dynamic Programming (DP) and a Genetic Algorithm (GA) for the 0/1 knapsack problem.

## Current progress

The DP program loads benchmark files, calculates the best value, shows a bordered table, lists the chosen items, and checks the answer against a matching entry in KNOWN_OPTIMA.txt. The GA now completes one full run using a binary population, fitness-based selection, one-point crossover, mutation, elitism, and a fixed generation limit. Repeated experiments and live comparison graphs are still to be added.

## Java files

| File | Job |
|---|---|
| KnapsackDP.java | Start the benchmark program. P01 is the default. |
| Demo.java | Start the camera, speaker, and console example. |
| DPSolver.java | Calculate the DP table and find the chosen items. |
| BenchmarkReader.java | Read .kp files and look up published answers. |
| TablePrinter.java | Print the input, bordered table, and chosen items. |
| ProblemData.java | Hold the item names, weights, values, and bag limit. |
| GAChromosome.java | Store one binary GA choice and calculate its fitness. |
| GASelector.java | Select parents using fitness-based ticket chances. |
| GACrossover.java | Combine two parents at a split point. |
| GAMutation.java | Randomly flip chromosome bits. |
| GAResult.java | Store the best choice from one GA run. |
| GeneticAlgorithm.java | Run the complete GA loop. |
| GeneticAlgorithmRunner.java | Start one complete GA run. |

Both entry points use the same DP calculation and printing code.

## Run in VS Code

Open this Assignment 2 folder in VS Code. Java 11 or later is required.

In the terminal, compile the Java files:

~~~powershell
javac -d out *.java
~~~

Run P01:

~~~powershell
java -cp out KnapsackDP
~~~

Run the small example:

~~~powershell
java -cp out Demo
~~~

Run another small benchmark:

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

Recompile after changing the Java code. Compiled files go in out/. The benchmark files remain in benchmarks/benchmarks/.

On the installed Java 26 runtime, you can also use source-file launching:

~~~powershell
java KnapsackDP.java
java Demo.java
~~~

The existing --demo option is also supported.

## Expected results

- Demo: camera and speaker; weight 5; value 7.
- P01: items 1, 2, 3, 4 and 6; weight 165; value 309.
- P02: items 2, 3 and 4; weight 26; value 51.

The table includes every weight limit, displayed in groups of ten columns. A row labelled "First 6 items" means those items are available to choose from.

The solver calculates its answer from weights and values. It uses KNOWN_OPTIMA.txt afterwards to check that answer.

## Current GA settings

- Population size: 50 choices.
- Stopping rule: 200 generations.
- Mutation rate: 0.05 per bit.
- Overweight handling: penalty fitness of 0. The chromosome stays in the population, but cannot be selected when any valid choice has positive fitness.
- Elitism: the best choice found so far is copied into the next generation.

The GA uses a new random generator for each run, so repeated runs can produce different results. The runner reports the known optimum for comparison but does not assume that GA must find it on every run.

## Development acknowledgement

OpenAI Codex assisted with explanations, implementation, refactoring, and checks of the current Java program.
