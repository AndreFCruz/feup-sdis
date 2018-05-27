package task.tictactoe;

import task.Board;
import task.GameState;
import task.Player;

import java.io.Serializable;

public class TicTacToeState extends GameState {

    private Player crosses;
    private Player noughts;

    public TicTacToeState() {
        board = new TicTacToeBoard();

        crosses = new TicTacToePlayer("CROSSES", TicTacToeBoard.Cell.CROSS);
        noughts = new TicTacToePlayer("NOUGHTS", TicTacToeBoard.Cell.NOUGH);
        currentPlayer = crosses;
    }

    public TicTacToeState(Board board, TicTacToeBoard.Cell currentCell) {
        this.board = board;
        crosses = new TicTacToePlayer("CROSSES", TicTacToeBoard.Cell.CROSS);
        noughts = new TicTacToePlayer("NOUGHTS", TicTacToeBoard.Cell.NOUGH);

        if(currentCell.equals(TicTacToeBoard.Cell.CROSS))
            currentPlayer = crosses;
        else
            currentPlayer = noughts;
    }

    public boolean currentPlayerWins() {
        return playerWins(currentPlayer);
    }

    public boolean opponentPlayerWins() {
        Player opponent = getOpponent();
        return playerWins(opponent);
    }

    public boolean playerWins(Player player) {
        TicTacToeBoard tBoard = (TicTacToeBoard) board;
        TicTacToeBoard.Cell currentCell = getPlayerCell(player);

        return tBoard.hasLine(currentCell);
    }

    public TicTacToeBoard.Cell getPlayerCell(Player player) {
        if(player.getRepresentation().equals("CROSSES"))
            return TicTacToeBoard.Cell.CROSS;

        return TicTacToeBoard.Cell.NOUGH;
    }

    private Player getOpponent() {
        if(currentPlayer == crosses)
            return noughts;

        return crosses;
    }

    public Player getOpponent(Player player) {
        if(player.equals(player))
            return noughts;

        return crosses;
    }

    public boolean isBoardFull() {
        return ((TicTacToeBoard) board).isFull();
    }

}
