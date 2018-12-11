(function () {
    'use strict';

    class Sudoku {

        constructor(problem, solutionCount, blankCount) {
            this.problem = problem;
            this.solutionCount = solutionCount;
            this.blankCount = blankCount;
        }

        getProblemAsStringArray() {
            return Sudoku.toStringArray(this.problem);
        }

        getDifficulty() {
            return ((this.blankCount - 17) / 64) * Math.log10(1001 - Math.min(1000, this.solutionCount)) / 3;
        }
    }

    //solve begins

    const N_BLANK = 0;
    const S_BLANK = '.';

    const setBit = function (bitSet, i, j, board) {
        const n = board[9 * i + j];
        if (n !== N_BLANK) {
            bitSet |= (1 << (n - 1));
            return bitSet;
        }
        return bitSet;
    };


    const currentUniqueBits = function (row, col, board) {
        let bitSet = 0;
        for (let i = 0; i < 9; i++) bitSet = setBit(bitSet, i, col, board);
        for (let j = 0; j < 9; j++) bitSet = setBit(bitSet, row, j, board);
        const c_i = boxCenter(row), c_j = boxCenter(col);
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
    };

    const boxCenter = function (n) {
        if (n < 3) return 1;
        else if (n < 6) return 4;
        else return 7;
    };

    const nextValidNumber = function (bitSet, currentNumber) {
        for (let i = currentNumber + 1; i <= 9; i++) {
            const pos = bitSet >>> (i - 1);
            if ((pos & 1) === 0) {
                return i;
            }
        }
        return N_BLANK;
    };

    const bitCount = function (n) {
        n = n - ((n >> 1) & 0x55555555);
        n = (n & 0x33333333) + ((n >> 2) & 0x33333333);
        return ((n + (n >> 4) & 0xF0F0F0F) * 0x1010101) >> 24;
    };

    const initSkipTable = function (board) {
        /*
            | <-- cost : 4bits --> | <-- index: 7bits --> |
             */
        const skipTable = new Array(81).fill(0);
        let count = 0;
        for (let i = 0; i < 81; i++) {
            if (board[i] !== N_BLANK) continue;
            const row = Math.trunc(i / 9), col = i % 9;
            const bitSet = currentUniqueBits(row, col, board);
            const cost = 9 - bitCount(bitSet);
            count++;
            let skipTableNumber = 0;
            skipTableNumber |= (cost << 7);
            skipTableNumber |= i;
            skipTable[i] = skipTableNumber;
        }
        skipTable.sort();
        return skipTable.slice(81 - count, 81);
    };

    const isValidBoard = function (board0) {
        return Array.isArray(board0) && board0.length === 81;
    };


    Sudoku.solve = function (board0, solutions, maxSolutions, maxIterations) {
        if (!isValidBoard(board0)) {
            throw new Error('board0 is not a valid array:' + board0);
        }
        if (!Array.isArray(solutions)) {
            throw new Error('an array to put solutions is required!');
        }
        if (!Number.isInteger(maxSolutions) || maxSolutions <= 0) {
            maxSolutions = Number.MAX_SAFE_INTEGER;
        }
        if (!Number.isInteger(maxIterations) || maxIterations <= 0) {
            maxIterations = Number.MAX_SAFE_INTEGER;
        }


        const skipTable = initSkipTable(board0);
        /*
        stack policy:
        All local variable can be encoded in a integer number, the policy is described as follow:
        | <--last bitSet : 9 bits --> | <-- last boardNumber : 4 bits --> | <-- last boardIndex : 7 bits --> |

         */
        const stack = [];
        let skipTableIndex = 0, boardIndex, bitSet, boardNumber;
        let regret = false;
        let count = 0;
        while (count++ < maxIterations) {
            let skipTableNumber = skipTable[skipTableIndex];
            boardIndex = skipTableNumber & 0x7f;
            if (regret) {
                if (skipTableIndex === 0) return;
                board0[boardIndex] = N_BLANK;
                skipTableIndex--;
                const stackNumber = stack.pop();
                bitSet = (stackNumber >>> 11) & 0x1ff;
                boardNumber = (stackNumber >>> 7) & 0xf;
                boardIndex = stackNumber & 0x7f;
                regret = false;
            } else {
                const row = Math.trunc(boardIndex / 9), col = boardIndex % 9;
                bitSet = currentUniqueBits(row, col, board0);
                if (bitSet === 0x1ff) { // not suitable to fill
                    regret = true;
                    continue;
                }
                boardNumber = 0;
            }

            boardNumber = nextValidNumber(bitSet, boardNumber);
            if (boardNumber === N_BLANK) {
                regret = true;
                continue;
            }
            board0[boardIndex] = boardNumber;
            let stackNumber = boardIndex;
            stackNumber |= (boardNumber << 7);
            stackNumber |= (bitSet << 11);
            stack.push(stackNumber);

            skipTableIndex++;
            if (skipTableIndex === skipTable.length) {
                solutions.push(board0.slice());
                if (solutions.length >= maxSolutions) return;
                regret = true;
                skipTableIndex--;
                stack.pop();
            }
        }
    };

    Sudoku.solveAsync = function (board, maxSolutions, maxIterations) {
        return new Promise(function (resolve, reject) {
            const solutions = [];
            const originalBoard = board.slice();
            try {
                Sudoku.solve(board, solutions, maxSolutions, maxIterations);
                resolve(solutions, originalBoard);
            } catch (e) {
                reject(e);
            }
        });
    };

    Sudoku.toStringArray = function (board0) {
        if (!isValidBoard(board0)) {
            throw new Error('board0 is not a valid array:' + board0);
        }
        const board = new Array(9);
        for (let i = 0; i < 9; i++) {
            board[i] = Array(9);
            for (let j = 0; j < 9; j++) {
                const n = board0[9 * i + j];
                if (n === 0) {
                    board[i][j] = S_BLANK;
                } else if (n >= 10 || n < 0) {
                    throw new Error(
                        `illegal number '${n}' at position '${i},${j}'`);
                } else {
                    board[i][j] = n.toString();
                }
            }
        }
        return board;
    };

    Sudoku.fromStringArray = function (board) {
        if (!Array.isArray(board) || board.length !== 9) {
            throw new Error('invalid board!');
        }
        const board0 = new Array(81).fill(N_BLANK);
        for (let i = 0; i < 9; i++) {
            if (!Array.isArray(board[i]) || board[i].length !== 9)
                throw new Error('invalid board!');
            for (let j = 0; j < 9; j++) {
                const s = board[i][j];
                if (s === S_BLANK) board0[9 * i + j] = N_BLANK;
                else board0[9 * i + j] = parseInt(s);
            }
        }
        return board0;
    };

    //gen begins here
    const randomInt = function (n) {
        return Math.trunc(Math.random() * n);
    };

    const shuffle = function (a) {
        if (a.length < 2) return;
        for (let i = a.length - 1; i > 0; i--) {
            let n = randomInt(i);
            let temp = a[n];
            a[n] = a[i];
            a[i] = temp;
        }
    };

    const randomIndex = function (board) {

        let index, bitSet;
        for (; ;) {
            index = randomInt(81);
            if (board[index] !== 0) continue;
            bitSet = currentUniqueBits(Math.trunc(index / 9), index % 9, board);
            if (bitSet === 0x1ff) throw new Error("no suitable number at:" + index);
            return {index: index, bitSet: bitSet};
        }
    };

    const randomFill = function (board) {
        const o = randomIndex(board);
        let index = o.index, bitSet = o.bitSet;
        const a = new Array(9).fill(0);
        let cnt = 0;
        for (let p = nextValidNumber(bitSet, 0);
             p !== 0;
             p = nextValidNumber(bitSet, p)) {
            a[p - 1] = p;
            cnt++;
        }
        a.sort();
        board[index] = a[randomInt(cnt) + 9 - cnt];
    };

    function randomFillRecursive(board, numbersRemaining) {
        if (numbersRemaining <= 0) return;
        randomFill(board);
        randomFillRecursive(board, numbersRemaining - 1);
    }

    Sudoku.gen = function (maxSolutions) {
        if (!Number.isInteger(maxSolutions) || maxSolutions <= 0) {
            maxSolutions = 1;
        }
        const solutions = [];
        let randomSudoku;
        while (true) {
            let board = new Array(81).fill(N_BLANK);
            randomFillRecursive(board, 17);
            solutions.length = 0;
            Sudoku.solve(board, solutions, 1);
            if (solutions.length > 0) {
                randomSudoku = solutions[0];
                break;
            }
        }

        const indices = new Array(81);
        for (let i = 0; i < indices.length; i++) indices[i] = i;
        shuffle(indices);


        for (let i = 0; i < indices.length; i++) {
            const index = indices[i];
            const lastFill = randomSudoku[index];
            randomSudoku[index] = N_BLANK;

            solutions.length = 0;
            Sudoku.solve(randomSudoku.slice(), solutions, maxSolutions + 1, 1000000);
            if (solutions.length > maxSolutions || solutions.length === 0) {
                randomSudoku[index] = lastFill;
                return new Sudoku(randomSudoku, maxSolutions, i + 1);
            }
        }

        throw new Error("unreachable code!");
    };

    Sudoku.genAsync = function (maxSolutions) {
        return new Promise(function (resolve, reject) {
            try {
                const sudoku = Sudoku.gen(maxSolutions);
                resolve(sudoku);
            } catch (e) {
                reject(e);
            }
        });
    };


    if (window.Sudoku) {
        console.warn("the Sudoku attribute already registered:" + window.Sudoku);
    }
    window.Sudoku = Sudoku;
})();