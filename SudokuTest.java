package sudoku;

import org.junit.Test;

public class SudokuTest {

  private static void printSolution(char[][] board) {
    for (int i = 0; i < 9; i++) System.out.println(java.util.Arrays.toString(board[i]));
  }

  @Test
  public void test() {
    char[][] board =
        new char[][] {
          {'5', '3', '.', '.', '7', '.', '.', '.', '.'},
          {'6', '.', '.', '1', '9', '5', '.', '.', '.'},
          {'.', '9', '8', '.', '.', '.', '.', '6', '.'},
          {'8', '.', '.', '.', '6', '.', '.', '.', '3'},
          {'4', '.', '.', '8', '.', '3', '.', '.', '1'},
          {'7', '.', '.', '.', '2', '.', '.', '.', '6'},
          {'.', '6', '.', '.', '.', '.', '2', '8', '.'},
          {'.', '.', '.', '4', '1', '9', '.', '.', '5'},
          {'.', '.', '.', '.', '8', '.', '.', '7', '9'}
        };

    long t1 = System.nanoTime();
    new SudokuSolver().solveSudoku(board);
    long t2 = System.nanoTime();
    printSolution(board);
    System.out.printf("time:%dms%n", (t2 - t1) / 1000_000);
  }
}
