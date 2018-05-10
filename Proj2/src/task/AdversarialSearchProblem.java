package task;

import java.util.Collection;

public interface AdversarialSearchProblem {

    /**
     * Evaluates whether a given state is a terminal state.
     * @param state the given state.
     * @return Whether the given state is terminal.
     */
    boolean isStateTerminal(GameState state);

    /**
     * Defines the set of legal moves from the given state.
     * @param state the given state.
     * @return A Collection of legal moves.
     */
    Collection<GameState> successors(GameState state);

    /**
     * Evaluates the utility of the given state.
     * @param state
     * @return
     */
    int utilityOfState(GameState state);
}
