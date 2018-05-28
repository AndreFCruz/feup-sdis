package task;

import javafx.util.Pair;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
  * Specific adversarial search algorithm
  */
public class MinimaxSearchTask extends AdversarialSearchTask {

    private Player maximizing;

    public MinimaxSearchTask(AdversarialSearchProblem problem, GameState state, Player maximizing) {
        super(problem, state);
        this.maximizing = maximizing;
    }

    /**
      * Minimax task. Executes the minimax algorithm
      */
    @Override
    public Pair<Integer, GameState> runTask() {
        if(problemDefinition.isStateTerminal(state)) {
            int score = problemDefinition.utilityOfState(state, maximizing);
            return new Pair<>(score, state);
        }

        // Maximizing player
        if(state.getCurrentPlayer().equals(maximizing)) {
            int bestScore = -2;
            GameState bestState = null;

            Collection<AdversarialSearchTask> successorTasks = partition();
            for(AdversarialSearchTask task : successorTasks) {
                Pair<Integer, GameState> taskPair = task.runTask();
                int taskScore = taskPair.getKey();

                if(taskScore > bestScore) {
                    bestScore = taskScore;
                    bestState = task.getState();
                }
            }

            return new Pair<Integer, GameState>(bestScore, bestState);
        }

        // Minimizing player
        else {
            int bestScore = 2;
            GameState bestState = null;

            Collection<AdversarialSearchTask> successorTasks = partition();
            for(AdversarialSearchTask task : successorTasks) {
                Pair<Integer, GameState> taskPair = task.runTask();
                int taskScore = taskPair.getKey();

                if(taskScore < bestScore) {
                    bestScore = taskScore;
                    bestState = task.getState();
                }
            }

            return new Pair<Integer, GameState>(bestScore, bestState);
        }

    }

    @Override
    public Collection<AdversarialSearchTask> partition() {
        Collection<GameState> successors = problemDefinition.successors(state);
        Collection<AdversarialSearchTask> successorTasks = new ArrayList<>();

        for(GameState successor : successors) {
            AdversarialSearchTask task = new MinimaxSearchTask(problemDefinition, successor, maximizing);
            successorTasks.add(task);
        }

        return successorTasks;
    }

}
