# SudokuSolve
A Java implemention for solving sudoku puzzle.

The problem first comes from [LeetCode#37](https://leetcode.com/problems/sudoku-solver/)

#### A sudoku solution must satisfy all of the following rules:

+ Each of the digits 1-9 must occur exactly once in each row.
+ Each of the digits 1-9 must occur exactly once in each column.
+ Each of the the digits 1-9 must occur exactly once in each of the 9 3x3 sub-boxes of the grid.
+ Empty cells are indicated by the character '.'.

![suduku_puzzle.png](https://upload.wikimedia.org/wikipedia/commons/thumb/f/ff/Sudoku-by-L2G-20050714.svg/250px-Sudoku-by-L2G-20050714.svg.png)

![sudoku_puzzle_solution.png](https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Sudoku-by-L2G-20050714_solution.svg/250px-Sudoku-by-L2G-20050714_solution.svg.png)

#### Note:

+ The given board contain only digits 1-9 and the character '.'.
+ You may assume that the given Sudoku puzzle will have a single unique solution.
+ The given board size is always 9x9.


The basic algorithm is [Backtracking](https://en.wikipedia.org/wiki/Backtracking), several performance optimizations were included:

+ Use a 1-Dimension array to replace the 2-Dimension array to be CPU-cache friendlly.
+ Bits operations to accelerate Set Union.
+ Use an int array to perform  as a stack to aviod recursive.
+ Certain order to reduce search-tree depth.

This solution can beats 90% of the total submissions,
More details please click [Here](https://zhuanlan.zhihu.com/p/50746657),the article is written in Chinese.
![Submission Detail](https://pic4.zhimg.com/80/v2-22457c5b9fca85ebce08dd4f340de6d7_hd.jpg)
