/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
 */
package run;


/**
 */
public sealed interface Statement permits ExpressionStatement, CompoundStatement, BranchStatement, LoopStatement, NoOperation {

    void execute(Execution execution, ObjectScope scope);

}
