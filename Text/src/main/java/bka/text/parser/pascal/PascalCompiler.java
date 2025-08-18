/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import bka.text.parser.Node;
import java.util.*;
import java.util.stream.*;
import run.*;
import uml.statechart.*;
import uml.structure.*;


public class PascalCompiler {

    public uml.structure.Class createProgramClass(Node node) {
        if (!"Program".equals(node.getSymbol())) {
            throw new IllegalArgumentException("Program keyword is missing");
        }
        List<Node> nodes = node.getChildren();
        String programName = findNode(node.getChildren(), "Identifier").content();
        UmlClassBuilder builder = new UmlClassBuilder(programName);
        final Node declarationsNode = findNode(nodes, "Declarations");
        addProgramVariables(builder, declarationsNode);
        buildOperations(builder, programName, declarationsNode);
        methods.put(programName, createBody(findNode(findNode(nodes, "CompoundStatement").getChildren(), "Statements")));
        return builder.build();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methods.get(operation.getName().get()));
    }

    private void addProgramVariables(UmlClassBuilder builder, Node declarations) {
        if (!declarations.getChildren().isEmpty()) {
            if ("VariableDeclaration".equals(declarations.getChildren().getFirst().getSymbol())) {
                addVariables(builder, declarations.getChildren().getFirst());
            }
            addProgramVariables(builder, declarations.getChildren().getLast());
        }
    }

    private void addVariables(UmlClassBuilder builder, Node variableDeclaration) {
        createAttributes(builder, variableDeclaration.getChildren().get(variableDeclaration.getChildren().size() - 2));
    }

    private void createAttributes(UmlClassBuilder builder, Node variableDeclarationList) {
        addAttributesFromExpression(builder, variableDeclarationList.getChildren().getFirst());
        if (variableDeclarationList.getChildren().size() > 1) {
            createAttributes(builder, variableDeclarationList.getChildren().getLast());
        }
    }

    private void addAttributesFromExpression(UmlClassBuilder builder, Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChildren().get(2));
        createIdentifiers(variableDeclarationExpression.getChildren().getFirst()).forEach(name -> builder.withAttribute(name, type));
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, Node declarations) {
        if (!declarations.getChildren().isEmpty()) {
            if ("FunctionDeclaration".equals(declarations.getChildren().getFirst().getSymbol())) {
                String functionName = declarations.getChildren().getFirst().getChildren().get(1).content();
                builder.withOperation(functionName, UmlTypeFactory.create(declarations.getChildren().getFirst().getChildren().get(4).content()), Member.Visibility.PRIVATE);
                methods.put(functionName, createBody(declarations.getChildren().getFirst().getChildren().get(7).getChildren().get(1)));
            }
            addPrivateFunctionOperations(builder, declarations.getChildren().getLast());
        }
    }

    private Type createType(Node typeDeclarationExpression) {
        Node expression = typeDeclarationExpression.getChildren().getFirst();
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

    private static Type createEnumerationType(Node identifierList) {
        return UmlTypeFactory.create(createIdentifiers(identifierList).stream().collect(Collectors.joining(", ", "( ", " )")));
    }

    private static Type createArrayType(Node rangeExpression, Node typeExpression) {
        return UmlTypeFactory.create("ARRAY [" + rangeString(rangeExpression) + "] OF " + typeExpression.content());
    }

    private uml.structure.Class createRecordType(Node typeDeclarationExpression) {
        UmlClassBuilder builder = new UmlClassBuilder(typeDeclarationExpression.content());
        addRecordFields(builder, typeDeclarationExpression.getChildren().get(1));
        return builder.build();
    }

    private void addRecordFields(UmlClassBuilder builder, Node variableDeclarationList) {
        addRecordField(builder, variableDeclarationList.getChildren().getFirst());
        if (variableDeclarationList.getChildren().size() > 1) {
            addRecordFields(builder, variableDeclarationList.getChildren().getLast());
        }
    }

    private void addRecordField(UmlClassBuilder builder, Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChildren().get(2));
        createIdentifiers(variableDeclarationExpression.getChildren().getFirst()).forEach(name -> builder.withAttribute(name, type));
    }

    private static String rangeString(Node rangeExpression) {
        return rangeExpression.getChildren().getFirst().content() + " .. " + rangeExpression.getChildren().getLast().content();
    }

    private static List<String> createIdentifiers(Node identifierList) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(identifierList.getChildren().getFirst().content());
        if (identifierList.getChildren().size() > 1) {
            identifiers.addAll(createIdentifiers(identifierList.getChildren().getLast()));
        }
        return identifiers;
    }

    private void buildOperations(UmlClassBuilder builder, String programName, Node declarations) {
        builder.withOperation(programName, Member.Visibility.PUBLIC, UmlStereotypeFactory.createStereotypes("Main"));
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(Node compoundStatement) {
        Collection<Transition<Event, GuardCondition, Action>> body = new ArrayList<>();
        Collection<TransitionSource> leaves = new ArrayList<>(List.of(UmlStateFactory.getInitialState()));
        createStatementSequence(compoundStatement, body, leaves);
        leaves.forEach(leave -> body.add(UmlTransitionFactory.createTransition(leave, UmlStateFactory.getFinalState())));
        return body;
    }

    private void createStatementSequence(Node statements, Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        while (statements != null) {
            Statement statement = new Statement(statements.getChildren().getFirst(), name -> Optional.ofNullable(methods.get(name)));
            statement.getTransitions(transitions, leaves);
            statements = (statements.getChildren().size() > 1) ? statements.getChildren().getLast() : null;
        }
    }

    private static Node findNode(List<Node> nodes, String symbol) {
        return nodes.stream()
            .filter(node -> symbol.equals(node.getSymbol()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException(symbol));
    }

    private final Map<String, Collection<Transition<Event, GuardCondition, Action>>> methods = new HashMap<>();

}
