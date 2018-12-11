package sudoku;

import java.io.Serializable;
import java.util.Arrays;

 final class Sudoku implements Serializable {

  private final int[] problem;
  private final int solutionCount;
  private final int blankCount;

  Sudoku(int[] problem, int solutionCount, int blankCount) {
    this.problem = problem;
    this.solutionCount = solutionCount;
    this.blankCount = blankCount;
  }

  private static char[][] toCharArray(int[] board0, char emptyChar) {
    char[][] board = new char[9][9];
    for (int i = 0; i < 9; i++)
      for (int j = 0; j < 9; j++) {
        int n = board0[9 * i + j];
        if (n == 0) {
          board[i][j] = emptyChar;
        } else if (n >= 10 || n < 0) {
          throw new IllegalArgumentException(
              String.format("illegal number '%d' at position '%d,%d'", n, i, j));
        } else {
          board[i][j] = (char) (n + '0');
        }
      }
    return board;
  }

  int[] getProblem() {
    return problem;
  }

  int getSolutionCount() {
    return solutionCount;
  }

  int getBlankCount() {
    return blankCount;
  }

  void prettyPrintProblem() {
    for (char[] chars : toCharArray(problem, '.')) System.out.println(Arrays.toString(chars));
  }
}
