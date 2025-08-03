/*
** © Bart Kampers
*/
package run;


public interface ParseTreeExpression extends uml.structure.Expression {

    String type();

    String value();

    Value evaluate(Memory memory) throws StateMachineException;

}
