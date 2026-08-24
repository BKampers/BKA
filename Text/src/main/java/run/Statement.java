package run;

/**
 */
public interface Statement {

    void execute(Execution execution, ObjectScope scope);

    Statement NO_OPERATION = new Statement() {
        @Override
        public void execute(Execution execution, ObjectScope scope) {
            // No operation
        }

        @Override
        public String toString() {
            return "@No operation";
        }
    };

}
