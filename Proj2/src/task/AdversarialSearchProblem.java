package task;

import java.io.Serializable;
import java.util.Collection;

public interface AdversarialSearchProblem extends Serializable {

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
     * Evaluates the utility of the given state, according to the given maximizing target player
     * @param state
     * @param maximizer
     * @return
     */
    int utilityOfState(GameState state, Player maximizer);
}
