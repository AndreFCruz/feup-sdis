package task;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class MinimaxSearchTask extends AdversarialSearchTask {

    private Player maximizing;

    public MinimaxSearchTask(AdversarialSearchProblem problem, GameState state, Player maximizing) {
        super(problem, state);
        this.maximizing = maximizing;
    }

    @Override
    //TODO: refactor this
    public int runTask() {
        if(problemDefinition.isStateTerminal(state)) {
            int score = problemDefinition.utilityOfState(state, maximizing);
            return score;
        }

        // Maximizing player
        if(state.getCurrentPlayer().equals(maximizing)) {
            int bestScore = -2;

            Collection<GameState> successors = problemDefinition.successors(state);
            Collection<AdversarialSearchTask> successorTasks = new ArrayList<>();

            for(GameState successor : successors) {
                //TODO: how to divide and send them to the peers?
                AdversarialSearchTask task = new MinimaxSearchTask(problemDefinition, successor, maximizing);
                successorTasks.add(task);
            }

            for(AdversarialSearchTask task : successorTasks) {
                int taskScore = task.runTask();
                bestScore = max(taskScore, bestScore);
            }

            return bestScore;
        }

        // Minimizing player
        else {
            int bestScore = 2;

            Collection<GameState> successors = problemDefinition.successors(state);
            Collection<AdversarialSearchTask> successorTasks = new ArrayList<>();

            for(GameState successor : successors) {
                //TODO: how to divide and send them to the peers?
                AdversarialSearchTask task = new MinimaxSearchTask(problemDefinition, successor, maximizing);
                successorTasks.add(task);
            }

            for(AdversarialSearchTask task : successorTasks) {
                int taskScore = task.runTask();
                bestScore = min(taskScore, bestScore);
            }

            return bestScore;
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
