package task.tictactoe;

import task.Player;

public class TicTacToePlayer implements Player {

    private String representation;
    private TicTacToeBoard.Cell value;

    public TicTacToePlayer(String rep, TicTacToeBoard.Cell value) {
        this.representation = rep;
        this.value = value;
    }

    @Override
    public String getRepresentation() {
        return representation;
    }

    public TicTacToeBoard.Cell getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return representation.equals(((TicTacToePlayer) o).getRepresentation());
    }

}
