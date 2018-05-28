package task;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.Collection;

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

    public abstract Pair<Integer, GameState> runTask();

    public abstract Collection<AdversarialSearchTask> partition();

}
