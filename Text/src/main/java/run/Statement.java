package run;

/**
 */
public interface Statement {

    Statement NO_OPERATION = new Statement() {
        @Override
        public String toString() {
            return "@No operation";
        }
    };

}
