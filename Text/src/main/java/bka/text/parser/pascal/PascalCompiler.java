/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import java.util.*;
import java.util.stream.*;
import run.*;
import uml.statechart.*;
import uml.structure.*;


public class PascalCompiler {

    public java.lang.Object createObject(List<PascalParser.Node> tree) {
        return switch (tree.getFirst().getSymbol()) {
            case "PROGRAM\\b" ->
                createProgramClass(tree);
            case "IdentifierList" ->
                createIdentifiers(tree.getFirst());
            case "Identifier" ->
                tree.getFirst().getChildren().getFirst().content();
            default ->
                tree.getFirst().content();
        };
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methods.get(operation.getName().get()));
    }

    private uml.structure.Class createProgramClass(List<PascalParser.Node> nodes) {
        UmlClassBuilder builder = new UmlClassBuilder(nodes.get(1).content());
        addProgramVariables(builder, nodes.get(3));
        String programName = nodes.get(1).content();
        buildOperations(builder, programName, nodes.get(3));
        methods.put(programName, createBody(nodes.get(4).getChildren().get(1)));
        return builder.build();
    }

    private void addProgramVariables(UmlClassBuilder builder, PascalParser.Node declarations) {
        if (!declarations.getChildren().isEmpty()) {
            if ("VariableDeclaration".equals(declarations.getChildren().getFirst().getSymbol())) {
                addVariables(builder, declarations.getChildren().getFirst());
            }
            addProgramVariables(builder, declarations.getChildren().getLast());
        }
    }

    private void addVariables(UmlClassBuilder builder, PascalParser.Node variableDeclaration) {
        createAttributes(builder, variableDeclaration.getChildren().get(variableDeclaration.getChildren().size() - 2));
    }

    private void createAttributes(UmlClassBuilder builder, PascalParser.Node variableDeclarationList) {
        addAttributesFromExpression(builder, variableDeclarationList.getChildren().getFirst());
        if (variableDeclarationList.getChildren().size() > 1) {
            createAttributes(builder, variableDeclarationList.getChildren().getLast());
        }
    }

    private void addAttributesFromExpression(UmlClassBuilder builder, PascalParser.Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChildren().get(2));
        createIdentifiers(variableDeclarationExpression.getChildren().getFirst()).forEach(name -> builder.withAttribute(name, type));
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, PascalParser.Node declarations) {
        if (!declarations.getChildren().isEmpty()) {
            if ("FunctionDeclaration".equals(declarations.getChildren().getFirst().getSymbol())) {
                String functionName = declarations.getChildren().getFirst().getChildren().get(1).content();
                builder.withOperation(functionName, UmlTypeFactory.create(declarations.getChildren().getFirst().getChildren().get(4).content()), Member.Visibility.PRIVATE);
                methods.put(functionName, createBody(declarations.getChildren().getFirst().getChildren().get(7).getChildren().get(1)));
            }
            addPrivateFunctionOperations(builder, declarations.getChildren().getLast());
        }
    }

    private Type createType(PascalParser.Node typeDeclarationExpression) {
        final PascalParser.Node expression = typeDeclarationExpression.getChildren().getFirst();
        return switch (expression.getSymbol()) {
            case "TypeExpression" ->
                UmlTypeFactory.create(expression.getChildren().getFirst().content());
            case "RangeExpression" ->
                UmlTypeFactory.create(rangeString(expression));
            case "\\(" ->
                createEnumerationType(typeDeclarationExpression.getChildren().get(1));
            case "ARRAY\\b" ->
                createArrayType(typeDeclarationExpression.getChildren().get(2), typeDeclarationExpression.getChildren().get(5));
            case "RECORD\\b" ->
                createRecordType(typeDeclarationExpression);
            default ->
                throw new IllegalStateException("UnsupportedType " + typeDeclarationExpression.getChildren().getFirst().getSymbol());
        };
    }

    private static Type createEnumerationType(PascalParser.Node identifierList) {
        return UmlTypeFactory.create(createIdentifiers(identifierList).stream().collect(Collectors.joining(", ", "( ", " )")));
    }

    private static Type createArrayType(PascalParser.Node rangeExpression, PascalParser.Node typeExpression) {
        return UmlTypeFactory.create("ARRAY [" + rangeString(rangeExpression) + "] OF " + typeExpression.content());
    }

    private uml.structure.Class createRecordType(PascalParser.Node typeDeclarationExpression) {
        UmlClassBuilder builder = new UmlClassBuilder(typeDeclarationExpression.content());
        addRecordFields(builder, typeDeclarationExpression.getChildren().get(1));
        return builder.build();
    }

    private void addRecordFields(UmlClassBuilder builder, PascalParser.Node variableDeclarationList) {
        addRecordField(builder, variableDeclarationList.getChildren().getFirst());
        if (variableDeclarationList.getChildren().size() > 1) {
            addRecordFields(builder, variableDeclarationList.getChildren().getLast());
        }
    }

    private void addRecordField(UmlClassBuilder builder, PascalParser.Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChildren().get(2));
        createIdentifiers(variableDeclarationExpression.getChildren().getFirst()).forEach(name -> builder.withAttribute(name, type));
    }

    private static String rangeString(PascalParser.Node rangeExpression) {
        return rangeExpression.getChildren().getFirst().content() + " .. " + rangeExpression.getChildren().getLast().content();
    }

    private static List<String> createIdentifiers(PascalParser.Node identifierList) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(identifierList.getChildren().getFirst().content());
        if (identifierList.getChildren().size() > 1) {
            identifiers.addAll(createIdentifiers(identifierList.getChildren().getLast()));
        }
        return identifiers;
    }

    private void buildOperations(UmlClassBuilder builder, String programName, PascalParser.Node declarations) {
        builder.withOperation(programName, Member.Visibility.PUBLIC, UmlStereotypeFactory.createStereotypes("Main"));
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(PascalParser.Node compoundStatement) {
        Collection<Transition<Event, GuardCondition, Action>> body = new ArrayList<>();
        Collection<TransitionSource> leaves = new ArrayList<>(List.of(UmlStateFactory.getInitialState()));
        createStatementSequence(compoundStatement, body, leaves);
        leaves.forEach(leave -> body.add(UmlTransitionFactory.createTransition(leave, UmlStateFactory.getFinalState())));
        return body;
    }

    private void createStatementSequence(PascalParser.Node statements, Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        while (statements != null) {
            Statement statement = new Statement(statements.getChildren().getFirst(), name -> Optional.ofNullable(methods.get(name)));
            statement.getTransitions(transitions, leaves);
            statements = (statements.getChildren().size() > 1) ? statements.getChildren().getLast() : null;
        }
    }

    private final Map<String, Collection<Transition<Event, GuardCondition, Action>>> methods = new HashMap<>();

}
