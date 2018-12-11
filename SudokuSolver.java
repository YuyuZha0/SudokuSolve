package sudoku;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public final class SudokuSolver {

  static final int N_BLANK = 0;

  static int getCurrentUniqueBits(int row, int col, int[] board) {
    int bitSet = 0;
    for (int i = 0; i < 9; i++) bitSet = setBit(bitSet, i, col, board);
    for (int j = 0; j < 9; j++) bitSet = setBit(bitSet, row, j, board);
    int c_i = getCenter(row), c_j = getCenter(col);
    bitSet = setBit(bitSet, c_i - 1, c_j - 1, board);
    bitSet = setBit(bitSet, c_i, c_j - 1, board);
    bitSet = setBit(bitSet, c_i + 1, c_j - 1, board);
    bitSet = setBit(bitSet, c_i - 1, c_j, board);
    bitSet = setBit(bitSet, c_i, c_j, board);
    bitSet = setBit(bitSet, c_i + 1, c_j, board);
    bitSet = setBit(bitSet, c_i - 1, c_j + 1, board);
    bitSet = setBit(bitSet, c_i, c_j + 1, board);
    bitSet = setBit(bitSet, c_i + 1, c_j + 1, board);
    return bitSet;
  }

  private static int setBit(int bitSet, int i, int j, int[] board) {
    int n = board[9 * i + j];
    if (n != N_BLANK) {
      bitSet |= (1 << (n - 1));
      return bitSet;
    }
    return bitSet;
  }

  private static int getCenter(int n) {
    if (n < 3) return 1;
    else if (n < 6) return 4;
    else return 7;
  }

  static int nextValidNumber(int bitSet, int currentNumber) {

    for (int i = currentNumber + 1; i <= 9; i++) {
      int pos = bitSet >>> (i - 1);
      if ((pos & 1) == 0) {
        return i;
      }
    }
    return N_BLANK;
  }

  static int[] copy(int[] board) {
    int[] copy = new int[board.length];
    System.arraycopy(board, 0, copy, 0, board.length);
    return copy;
  }

  private static int[] convertToOneDimension(char[][] board) {
    int[] board0 = new int[81];
    for (int i = 0; i < 9; i++)
      for (int j = 0; j < 9; j++) {
        char c = board[i][j];
        if (c == '.') board0[9 * i + j] = N_BLANK;
        else board0[9 * i + j] = c - '0';
      }
    return board0;
  }

  private static int[] initSkipTable(int[] board) {

    /*
    | <-- cost : 4bits --> | <-- index: 7bits --> |
     */
    int[] skipTable = new int[81];
    int count = 0;
    for (int i = 0; i < 81; i++) {
      if (board[i] != N_BLANK) continue;
      int row = i / 9, col = i % 9;
      int bitSet = getCurrentUniqueBits(row, col, board);
      int cost = 9 - Integer.bitCount(bitSet);
      count++;
      int skipTableNumber = 0;
      skipTableNumber |= (cost << 7);
      skipTableNumber |= i;
      skipTable[i] = skipTableNumber;
    }
    Arrays.sort(skipTable);
    int[] skipTable0 = new int[count];
    System.arraycopy(skipTable, 81 - count, skipTable0, 0, count);
   // System.out.println(Arrays.toString(skipTable0));
    return skipTable0;
  }

  private static void fillBack(int[] board0, char[][] board) {
    for (int i = 0; i < 9; i++)
      for (int j = 0; j < 9; j++) {
        char c = board[i][j];
        if (c == '.') board[i][j] = (char) (board0[9 * i + j] + '0');
      }
  }

  void solveSudoku0(int[] board0, Collection<int[]> solutions) {
    solveSudoku0(board0, solutions, Integer.MAX_VALUE);
  }

  void solveSudoku0(int[] board0, Collection<int[]> solutions, int maxSize) {
    Objects.requireNonNull(solutions);
    int[] skipTable = initSkipTable(board0);
    /*
    stack policy:
    All local variable can be encoded in a integer number, the policy is described as follow:
    | <--last bitSet : 9 bits --> | <-- last boardNumber : 4 bits --> | <-- last boardIndex : 7 bits --> |

     */
    int[] stack = new int[skipTable.length];
    int stackDepth = -1, skipTableIndex = 0, boardIndex, bitSet, boardNumber;
    boolean regret = false;
    while (true) {
      int skipTableNumber = skipTable[skipTableIndex];
      boardIndex = skipTableNumber & 0x7f;
      if (regret) {
        if (skipTableIndex == 0) return;
        board0[boardIndex] = N_BLANK;
        skipTableIndex--;
        int stackNumber = stack[stackDepth--];
        bitSet = (stackNumber >>> 11) & 0x1ff;
        boardNumber = (stackNumber >>> 7) & 0xf;
        boardIndex = stackNumber & 0x7f;
        regret = false;
      } else {
        int row = boardIndex / 9, col = boardIndex % 9;
        bitSet = getCurrentUniqueBits(row, col, board0);
        if (bitSet == 0x1ff) { // not suitable to fill
          regret = true;
          continue;
        }
        boardNumber = 0;
      }

      boardNumber = nextValidNumber(bitSet, boardNumber);
      if (boardNumber == N_BLANK) {
        regret = true;
        continue;
      }
      board0[boardIndex] = boardNumber;
      int stackNumber = boardIndex;
      stackNumber |= (boardNumber << 7);
      stackNumber |= (bitSet << 11);
      stack[++stackDepth] = stackNumber;

      skipTableIndex++;
      if (skipTableIndex == skipTable.length) {
        solutions.add(copy(board0));
        if (solutions.size() >= maxSize) return;
        regret = true;
        skipTableIndex--;
        stackDepth--;
      }
    }
  }

  public void solveSudoku(char[][] board) {

    int[] board0 = convertToOneDimension(board);
    List<int[]> solutions = new ArrayList<>();
    solveSudoku0(board0, solutions, 10);
    assert solutions.size() == 1;
    fillBack(solutions.get(0), board);
  }
}
