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

    /**
      * Gets current game state
      */
    public GameState getState() {
        return state;
    }

    /**
      * Executes the adversarial search task (i.e. adversarial search algorithm)
      */
    public abstract Pair<Integer, GameState> runTask();

    /**
      * Partitiosn the task into smaller, similar subtasks
      */
    public abstract Collection<AdversarialSearchTask> partition();

}
