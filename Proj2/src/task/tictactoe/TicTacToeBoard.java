package task.tictactoe;

import task.Board;

public class TicTacToeBoard implements Board {

    private enum Cell {
        EMPTY {
            @Override
            public String toString() {
                return " ";
            }
        },
        CROSS {
            @Override
            public String toString() {
                return "X";
            }
        },
        NOUGH {
            @Override
            public String toString() {
                return "O";
            }
        }
    };

    private static final int N_ROWS = 3;
    private static final int N_COLS = 3;

    private Cell[][] matrix;

    public TicTacToeBoard() {
        matrix = new Cell[N_ROWS][N_COLS];

        for(Cell[] row : matrix)
            for(int i = 0; i < row.length; i++)
                row[i] = Cell.EMPTY;
    }

    public void setCell(int row, int col, Cell value) {
        matrix[row][col] = value;
    }

    @Override
    public String display() {
        String result = "/---|---|---\\\n";

        for (Cell[] row : matrix) {
            result += "| " + row[0] + " | " + row[1] + " | " + row[2] + " |\n";
            result += "|-----------|\n";
        }
        result += "/---|---|---\\\n";

        return result;
    }
}
