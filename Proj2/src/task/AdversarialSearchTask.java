package task;

import java.io.Serializable;

public abstract class AdversarialSearchTask implements Serializable {

    protected static final long serialVersionUID = 101L;

    protected AdversarialSearchProblem problemDefinition;
    protected GameState state;

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

    public abstract int runTask();

}
