# Theta check — empirical constants

Ratio f(n)/g(n) over the measured sizes; c1 and c2 are the smallest and largest
ratio observed for n >= n0, so c1*g(n) <= f(n) <= c2*g(n) holds on that range.

| algorithm | input | g(n) | n0 | c1 | c2 | c2/c1 |
|---|---|---|---|---|---|---|
| MergeSort | random | n·log2 n | 10 000 | 0.956 | 0.998 | 1.04 |
| MergeSort | sorted | n·log2 n | 10 000 | 0.446 | 0.455 | 1.02 |
| MergeSort | duplicates | n·log2 n | 10 000 | 0.916 | 0.950 | 1.04 |
| MergeSort | reversed | n·log2 n | 10 000 | 0.705 | 0.728 | 1.03 |
| QuickSort | random | n·log2 n | 10 000 | 1.271 | 1.375 | 1.08 |
| QuickSort | sorted | n·log2 n | 10 000 | 1.152 | 1.264 | 1.10 |
| QuickSort | duplicates | n·log2 n | 10 000 | 0.186 | 0.278 | 1.50 |
| QuickSort | reversed | n·log2 n | 10 000 | 1.191 | 1.252 | 1.05 |
| QuickSelect | random | n | 10 000 | 2.551 | 5.237 | 2.05 |
| QuickSelect | sorted | n | 10 000 | 2.011 | 2.678 | 1.33 |
| QuickSelect | duplicates | n | 10 000 | 1.000 | 2.981 | 2.98 |
| QuickSelect | reversed | n | 10 000 | 2.187 | 2.840 | 1.30 |
| Median-of-Medians | random | n | 10 000 | 8.325 | 8.529 | 1.02 |
| Median-of-Medians | sorted | n | 10 000 | 6.803 | 7.218 | 1.06 |
| Median-of-Medians | duplicates | n | 10 000 | 3.090 | 3.101 | 1.00 |
| Median-of-Medians | reversed | n | 10 000 | 7.817 | 8.100 | 1.04 |
| Closest pair | random points | n·log2 n | 10 000 | 0.998 | 1.011 | 1.01 |
| InsertionSort | random | n² | 1 000 | 0.248 | 0.252 | 1.02 |
