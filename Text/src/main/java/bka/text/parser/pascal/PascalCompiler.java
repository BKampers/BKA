/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package bka.text.parser.pascal;

import bka.text.parser.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import run.*;
import uml.statechart.*;
import uml.structure.*;

/**
 * Creates an executable state machine from a pascal parse tree.
 */
public class PascalCompiler {

    public PascalCompiler() {
        declaredTypes.add(UmlTypeFactory.create("boolean"));
        declaredTypes.add(UmlTypeFactory.create("integer"));
        declaredTypes.add(UmlTypeFactory.create("real"));
        declaredTypes.add(UmlTypeFactory.create("char"));
        declaredTypes.add(UmlTypeFactory.create("string"));
    }

    public uml.structure.Class createProgramClass(Node node) {
        if (!"Program".equals(node.getSymbol())) {
            throw new IllegalArgumentException("Program keyword is missing");
        }
        String programName = identifier(node);
        UmlClassBuilder builder = new UmlClassBuilder(programName);
        Node declarationsNode = node.getChild("Declarations");
        createTypes(declarationsNode);
        addProgramVariables(builder, declarationsNode);
        buildOperations(builder, programName, declarationsNode);
        methodBodies.put(programName, createBody(programName, node.getChild("CompoundStatement").getChild("Statements")));
        return builder.build();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methodBodies.get(operation.getName().get()));
    }

    public void createTypes(Node declarations) {
        declarations.findChild("TypeDeclaration")
            .ifPresent(typeDeclaration -> declaredTypes.add(createType(typeDeclaration)));
        declarations.findChild("Declarations")
            .ifPresent(next -> createTypes(next));
    }

    private Type createType(Node typeDeclaration) {
        return createDeclaredType(identifier(typeDeclaration), typeDeclaration.getChild("TypeDeclarationExpression"), Member.Visibility.PUBLIC);
    }

    private void addProgramVariables(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("VariableDeclaration")
            .ifPresent(declaration -> addVariables(builder, declaration));
        declarations.findChild("Declarations")
            .ifPresent(next -> addProgramVariables(builder, next));
    }

    private void addVariables(UmlClassBuilder builder, Node variableDeclaration) {
        createAttributes(builder, variableDeclaration.getChild("VariableDeclarationList"), Member.Visibility.PRIVATE);
    }

    private void createAttributes(UmlClassBuilder builder, Node variableDeclarationList, Member.Visibility visibility) {
        addAttributesFromExpression(builder, variableDeclarationList.getChild("VariableDeclarationExpression"), visibility);
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> createAttributes(builder, next, visibility));
    }

    private void addAttributesFromExpression(UmlClassBuilder builder, Node variableDeclarationExpression, Member.Visibility visibility) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), visibility);
        createIdentifiers(variableDeclarationExpression.getChild("IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type, visibility));
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("ProcedureDeclaration")
            .ifPresent(procedureDeclaration -> addMethod(builder, VOID_TYPE, procedureDeclaration));
        declarations.findChild("FunctionDeclaration")
            .ifPresent(functionDeclaration -> addMethod(builder, getType(functionDeclaration.getChild("TypeExpression")), functionDeclaration));
        declarations.findChild("Declarations")
            .ifPresent(next -> addPrivateFunctionOperations(builder, next));
    }

    private void addMethod(UmlClassBuilder builder, Type type, Node declaration) {
        String methodName = identifier(declaration);
        List<Parameter> parameters = createParameterList(declaration.getChild("ParameterDeclaration"));
        methodParameters.put(methodName, parameters);
        methodTypes.put(methodName, type);
        methodLocals.put(methodName, createLocals(declaration.getChild("Declarations")));
        methodBodies.put(methodName, createBody(methodName, declaration.getChild("CompoundStatement").getChild("Statements")));
        builder.withOperation(methodName, parameters, type, Member.Visibility.PRIVATE);
    }


    private Collection<uml.structure.Object> createLocals(Node declarations) {
        if (declarations.getChildren().isEmpty()) {
            return Collections.emptyList();
        }
        return createVariables(declarations.getChild("VariableDeclaration").getChild("VariableDeclarationList"));
    }

    private List<Parameter> createParameterList(Node parameterDeclaration) {
        if (parameterDeclaration.getChildren().isEmpty()) {
            return Collections.emptyList();
        }
        List<Parameter> parameters = new ArrayList<>();
        Node parameterList = parameterDeclaration.getChild("ParameterList");
        do {
            parameters.add(createParameter(parameterList));
            parameterList = parameterList.findChild("ParameterList").orElse(null);
        } while (parameterList != null);
        return parameters;
    }

    private Parameter createParameter(Node parameterList) {
        Node parameterExpression = parameterList.getChild("ParameterExpression");
        return UmlParameterFactory.create(
            (parameterExpression.findChild("VAR\\b").isPresent()) ? Parameter.Direction.INOUT : Parameter.Direction.IN,
            createParameterType(parameterExpression.getChild("ParameterTypeExpression")),
            identifier(parameterExpression));
    }

    private Type createParameterType(Node parameterTypeExpression) {
        if (parameterTypeExpression.findChild("ARRAY\\b").isPresent()) {
            return new ArrayType(createType(parameterTypeExpression.getChild("TypeExpression")), 0, 0);
        }
        return getType(parameterTypeExpression.getChild("TypeExpression"));
    }

    private Type createType(Node typeDeclarationExpression, Member.Visibility visibility) {
        Node expression = typeDeclarationExpression.getChildren().getFirst();
        return switch (expression.getSymbol()) {
            case "TypeExpression" ->
                getType(expression);
            case "RangeExpression" ->
                UmlTypeFactory.create(rangeString(expression));
            case "\\(" ->
                createEnumerationType(typeDeclarationExpression.getChild("IdentifierList"));
            case "ARRAY\\b" ->
                createArrayType(null, typeDeclarationExpression.getChild("RangeExpression"), typeDeclarationExpression.getChild("TypeDeclarationExpression"));
            case "RECORD\\b" ->
                createDeclaredType(null, typeDeclarationExpression, visibility);
            default ->
                throw new IllegalStateException("UnsupportedType " + typeDeclarationExpression.getChildren().getFirst().getSymbol());
        };
    }

    private static Predicate<Type> typeNameEquals(Node identifier) {
        return type -> type.getName().get().equalsIgnoreCase(identifier.content());
    }

    private static Type createEnumerationType(Node identifierList) {
        return UmlTypeFactory.create(createIdentifiers(identifierList).stream().collect(Collectors.joining(", ", "( ", " )")));
    }

    private static int intValue(Node node) {
        return switch (node.getSymbol()) {
            case "IntegerLiteral" ->
                Integer.parseInt(node.content());
            case "Identifier" ->
                throw new IllegalStateException("Const not supported yet"); // TODO evaluate consts fitrst
            default ->
                throw new IllegalStateException(node.content() + " is cannot be evaluated to an integer.");
        };
    }

    private Type createDeclaredType(String name, Node typeDeclarationExpression, Member.Visibility visibility) {
        if (typeDeclarationExpression.startsWith("RECORD\\b")) {
            UmlClassBuilder builder = (name == null) ? new UmlClassBuilder() : new UmlClassBuilder(name);
            addRecordFields(builder, typeDeclarationExpression.getChild("VariableDeclarationList"), visibility);
            return builder.build();
        }
        if (typeDeclarationExpression.startsWith("ARRAY\\b")) {
            return createArrayType(name, typeDeclarationExpression.getChild("RangeExpression"), typeDeclarationExpression.getChild("TypeDeclarationExpression"));
        }
        if (typeDeclarationExpression.startsWith("TypeExpression")) {
            return getType(typeDeclarationExpression.getChild("TypeExpression"));
        }
        throw new IllegalStateException("Invalid type declaration expression: " + typeDeclarationExpression.content());
    }

    private Type getType(Node typeExpression) {
        return declaredTypes.stream()
            .filter(typeNameEquals(typeExpression))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No such type: " + typeExpression.content()));
    }

    private Type createArrayType(String identifier, Node rangeExpression, Node typeExpression) {
        if (identifier == null) {
            return new ArrayType(
                createDeclaredType(null, typeExpression, Member.Visibility.PUBLIC),
                intValue(rangeExpression.getChildren().getFirst()),
                intValue(rangeExpression.getChildren().getLast()));
        }
        return new ArrayType(
            identifier,
            createDeclaredType(null, typeExpression, Member.Visibility.PUBLIC),
            intValue(rangeExpression.getChildren().getFirst()),
            intValue(rangeExpression.getChildren().getLast()));
    }

    private void addRecordFields(UmlClassBuilder builder, Node variableDeclarationList, Member.Visibility visibility) {
        addRecordField(builder, variableDeclarationList.getChild("VariableDeclarationExpression"), visibility);
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> addRecordFields(builder, next, visibility));
    }

    private void addRecordField(UmlClassBuilder builder, Node variableDeclarationExpression, Member.Visibility visibility) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), visibility);
        createIdentifiers(variableDeclarationExpression.getChild("IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type, visibility));
    }

    private Collection<uml.structure.Object> createVariables(Node variableDeclarationList) {
        Collection<uml.structure.Object> variables = new ArrayList<>();
        variables.addAll(createObjects(variableDeclarationList.getChild("VariableDeclarationExpression")));
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> variables.addAll(createVariables(next)));
        return variables;
    }

    private Collection<uml.structure.Object> createObjects(Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), Member.Visibility.PRIVATE);
        return createIdentifiers(variableDeclarationExpression.getChild("IdentifierList")).stream()
            .map(identifier -> UmlObjectFactory.create(identifier, type))
            .collect(Collectors.toList());
    }

    private static String rangeString(Node rangeExpression) {
        return rangeExpression.getChildren().getFirst().content() + " .. " + rangeExpression.getChildren().getLast().content();
    }

    private static List<String> createIdentifiers(Node identifierList) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(identifier(identifierList));
        identifierList.findChild("IdentifierList")
            .ifPresent(next -> identifiers.addAll(createIdentifiers(next)));
        return identifiers;
    }

    private void buildOperations(UmlClassBuilder builder, String programName, Node declarations) {
        builder.withOperation(programName, Member.Visibility.PUBLIC, UmlStereotypeFactory.createStereotypes("Main"));
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(String methodName, Node compoundStatement) {
        ActivityDiagramBuilder diagram = new ActivityDiagramBuilder();
        createStatementSequence(methodName, compoundStatement, diagram);
        diagram.addFinalState();
        return diagram.getTransitions();
    }

    private void createStatementSequence(String methodName, Node statements, ActivityDiagramBuilder diagram) {
        Node statementNode = statements;
        do {
            // TODO determine statement type
//            Type type = null;
//            Optional<Node> assignable = statementNode.getChild("Statement").findChild("Assignable");
//            if (assignable.isPresent()) {
//                String identifier = identifier(assignable.get());
//                if (identifier.equalsIgnoreCase(methodName)) {
//                    type = methodTypes.get(methodName);
//                }
//                if (type == null && methodParameters.containsKey(methodName)) {
//                    Optional<Parameter> parameter = methodParameters.get(methodName).stream()
//                        .filter(p -> identifier.equalsIgnoreCase(p.getName().get()))
//                        .findAny();
//                    if (parameter.isPresent()) {
//                        type = parameter.get().getType().get();
//                    }
//                }
//                if (type == null && methodLocals.containsKey(methodName)) {
//                    Optional<uml.structure.Object> local = methodLocals.get(methodName).stream()
//                        .filter(object -> object.getName().get().equalsIgnoreCase(identifier))
//                        .findAny();
//                    if (local.isPresent()) {
//                        type = local.get().getType().get();
//                    }
//                }
//            }
            Statement statement = new Statement(statementNode.getChild("Statement"), new MethodProperties());
            statement.createTransitions(diagram);
            statementNode = statementNode.findChild("Statements").orElse(null);
        } while (statementNode != null);
    }

    private static String identifier(Node node) {
        return node.getChild("Identifier").content().toLowerCase();
    }


    public class MethodProperties {

        public Collection<Transition<Event, GuardCondition, Action>> getBody(String name) {
            return methodBodies.get(name);
        }

        public List<Parameter> getParameters(String name) {
            return Collections.unmodifiableList(methodParameters.get(name));
        }

        public Type getType(String name) {
            return methodTypes.get(name);
        }

        public Collection<uml.structure.Object> getLocals(String name) {
            return Collections.unmodifiableCollection(methodLocals.get(name));
        }

        public boolean isProcedure(String name) {
            return VOID_TYPE.equals(methodTypes.get(name));
        }

    }

    private final Map<String, Collection<Transition<Event, GuardCondition, Action>>> methodBodies = new HashMap<>();
    private final Map<String, List<Parameter>> methodParameters = new HashMap<>();
    private final Map<String, Collection<uml.structure.Object>> methodLocals = new HashMap<>();
    private final Map<String, Type> methodTypes = new HashMap<>();
    private final Collection<Type> declaredTypes = new ArrayList<>();

    private static final Type VOID_TYPE = UmlTypeFactory.create("void");

}
