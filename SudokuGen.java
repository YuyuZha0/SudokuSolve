package sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SuppressWarnings("WeakerAccess")
public final class SudokuGen {

  private final Random random = new Random();

  public static void main(String[] args) {
    Sudoku sudoku = new SudokuGen().gen(1);
    sudoku.prettyPrintProblem();
    System.out.println(sudoku.getSolutionCount());
    System.out.println(sudoku.getBlankCount());
  }

  public Sudoku gen(int maxSolutions) {
    SudokuSolver solver = new SudokuSolver();
    final int[] randomSudoku;
    while (true) {
      int[] board = new int[81];
      nextNumber(board, 17);
      List<int[]> solutions = new ArrayList<>(1);
      solver.solveSudoku0(board, solutions, 1);
      if (!solutions.isEmpty()) {
        randomSudoku = solutions.get(0);
        break;
      }
    }

    int[] indices = new int[81];
    for (int i = 0; i < indices.length; i++) indices[i] = i;
    shuffle(indices);

    List<int[]> solutions = new ArrayList<>(maxSolutions + 1);
    int count = 0;
    for (int index : indices) {
      int lastFill = randomSudoku[index];
      randomSudoku[index] = SudokuSolver.N_BLANK;

      solutions.clear();
      solver.solveSudoku0(SudokuSolver.copy(randomSudoku), solutions, maxSolutions + 1);
      if (solutions.size() > maxSolutions) {
        randomSudoku[index] = lastFill;
        return new Sudoku(randomSudoku, maxSolutions, count);
      }
      count++;
    }

    throw new IllegalStateException("unreachable code!");
  }

  private void shuffle(int[] a) {
    if (a.length < 2) return;
    for (int i = a.length - 1; i > 0; i--) {
      int n = random.nextInt(i);
      int temp = a[n];
      a[n] = a[i];
      a[i] = temp;
    }
  }

  private void nextNumber(int[] board, int numbersRemaining) {
    if (numbersRemaining == 0) return;
    randomFill(board);
    nextNumber(board, numbersRemaining - 1);
  }

  private void randomFill(int[] board) {

    int[] indexAndBitSet = randomIndex(board);
    int index = indexAndBitSet[0], bitSet = indexAndBitSet[1];
    int[] a = new int[9];
    int cnt = 0;
    for (int p = SudokuSolver.nextValidNumber(bitSet, 0);
        p != 0;
        p = SudokuSolver.nextValidNumber(bitSet, p)) {
      a[p - 1] = p;
      cnt++;
    }
    Arrays.sort(a);
    // System.out.println(Arrays.toString(a));
    board[index] = a[random.nextInt(cnt) + 9 - cnt];
  }

  private int[] randomIndex(int[] board) {
    int index, bitSet;
    for (; ; ) {
      index = random.nextInt(81);
      if (board[index] != 0) continue;
      bitSet = SudokuSolver.getCurrentUniqueBits(index / 9, index % 9, board);
      if (bitSet == 0x1ff) throw new IllegalArgumentException("no suitable number at:" + index);
      return new int[] {index, bitSet};
    }
  }
}
