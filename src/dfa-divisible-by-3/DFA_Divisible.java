import java.util.ArrayList;
import java.util.List;


public class DFA_Divisible{

    enum State{
        q0N, q0Z, q1N, q1Z, q2N, q2Z
    }

    static class DFAResult{
        boolean accepted;
        State finalState;
        List<String> steps;

        DFAResult(boolean accepted, State finalState, List<String> steps){
            this.accepted = accepted;
            this.finalState = finalState;
            this.steps = steps;
        }

    }

    public static DFAResult runDFA(String input){
        State state = State.q0N;
        List<String> steps = new ArrayList<>();

        steps.add("Start state: " + state);

        for(int i = 0; i < input.length(); i++){
            char symbol = input.charAt(i);
            State oldState = state;


            switch(state){
                case q0N:
                    if(symbol == '0'){
                        state = State.q0Z;
                    } else if (symbol == '1'){
                        state = State.q1N;
                    } else {
                        steps.add("Invalid character: " + symbol);
                        return new DFAResult(false, state, steps);
                    }
                    break;
                case q0Z:
                    if(symbol == '0'){
                        state = State.q0Z;
                    } else if (symbol == '1'){
                        state = State.q1N;
                    } else {
                        steps.add("Invalid character: " + symbol);
                        return new DFAResult(false, state, steps);
                    }
                    break;
                case q1N:
                    if(symbol == '0'){
                        state = State.q1Z;
                    } else if (symbol == '1'){
                        state = State.q2N;
                    } else {
                        steps.add("Invalid character: " + symbol);
                        return new DFAResult(false, state, steps);
                    }
                    break;
                case q2Z:
                    if(symbol == '0'){
                        state = State.q2Z;
                    } else if (symbol == '1'){
                        state = State.q0N;
                    } else {
                        steps.add("Invalid character: " + symbol);
                        return new DFAResult(false, state, steps);
                    }
                    break;
                                    case q2N:
                    if(symbol == '0'){
                        state = State.q2Z;
                    } else if (symbol == '1'){
                        state = State.q0N;
                    } else {
                        steps.add("Invalid character: " + symbol);
                        return new DFAResult(false, state, steps);
                    }
                    break;

            }
            steps.add("Read " + symbol + ", transition from " + oldState + " to " + state);
        }

        boolean accepted = state == State.q0Z;
        return new DFAResult(accepted, state, steps);


    }


     public static void main(String[] args) {

        String input = "11101110";

        DFAResult result = runDFA(input);

        for (String step : result.steps) {
            System.out.println(step);
        }

        System.out.println("Final state: " + result.finalState);
        System.out.println("Accepted ? --> " + result.accepted);
    }
}

