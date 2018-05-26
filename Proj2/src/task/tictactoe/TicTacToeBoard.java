package task.tictactoe;

import task.Board;

public class TicTacToeBoard implements Board {

    //TODO: do this in a more elegant way
    public enum Cell {
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

    public boolean isFull() {
        for(Cell[] row : matrix)
            for(Cell col : row)
                if(col == Cell.EMPTY)
                    return false;

        return true;
    }

    public boolean hasLine(Cell cell) {
        // Horizontal line
        for(Cell[] row : matrix)
            if(row[0] == row[1] && row[1] == row[2] && row[0] == cell)
                return true;

        // Vertical line
        for(int j = 0; j < N_COLS; j++)
            if(matrix[j][0] == cell && matrix[j][0] == matrix[j][1] && matrix[j][1] == matrix[j][2])
                return true;

        // Diagonal lines
        if(matrix[0][0] == cell && matrix[0][0] == matrix[1][1] && matrix[1][1] == matrix[2][2])
            return true;
        if(matrix[0][0] == cell && matrix[0][2] == matrix[1][1] && matrix[1][1] == matrix[2][0])
            return true;

        return false;
    }
}
