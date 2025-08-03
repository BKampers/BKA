/*
** © Bart Kampers
*/
package run;


public interface DyadicOperator {

    Value evaluate(ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) throws StateMachineException;

}
