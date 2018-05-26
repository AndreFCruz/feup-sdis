package task.tictactoe;

import task.AdversarialSearchProblem;
import task.GameState;

import java.util.Collection;

public class TicTacToe implements AdversarialSearchProblem{
    @Override
    public boolean isStateTerminal(GameState state) {
        return false;
    }

    @Override
    public Collection<GameState> successors(GameState state) {
        return null;
    }

    @Override
    public int utilityOfState(GameState state) {
        return 0;
    }
}
