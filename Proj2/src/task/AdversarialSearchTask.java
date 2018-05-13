package task;

import java.io.Serializable;

public class AdversarialSearchTask implements Serializable {
    private AdversarialSearchProblem problemDefinition;
    private GameState state;

    public AdversarialSearchTask(AdversarialSearchProblem problem, GameState state) {
        this.problemDefinition = problem;
        this.state = state;
    }

    public AdversarialSearchProblem getProblemDefinition() {
        return problemDefinition;
    }

    public GameState getState() {
        return state;
    }

}
