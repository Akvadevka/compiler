import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

abstract class Node {
	// Список дочерних узлов
	protected List<Node> children = new ArrayList<>();

	// Добавление дочернего узла
	public void addChild(Node child) {
		children.add(child);
	}

	// Метод для красивого вывода дерева
	public void printTree(String indent, boolean last) {
		if (this == null) {
			return; // Предотвращаем вызов на null
		}
		System.out.print(indent);
		if (last) {
			System.out.print("└── ");
			indent += "    ";
		} else {
			System.out.print("├── ");
			indent += "│   ";
		}
		System.out.println(this); // или используйте toString() для вывода
		if (getChildren() != null) {
			for (Node child : getChildren()) {
				if (child != null) {
					child.printTree(indent, false);
				}
			}
		}
	}

	// Визуализация дерева в виде строки
	@Override
	public abstract String toString();

	public List<Node> getChildren() {
		return children;
	}
}

class ProgramNode extends Node {
	private final List<Node> statements;

	public ProgramNode() {
		this.statements = new ArrayList<>();
	}

	public void addStatement(Node statement) {
		this.statements.add(statement);
		addChild(statement); // Добавляем в дочерние узлы
	}

	@Override
	public String toString() {
		return "Program";
	}
}

class ExpressionNode extends Node {
	private final Node leftOperand;
	private final TokenCode operator;
	private final Node rightOperand;

	public ExpressionNode(Node leftOperand, TokenCode operator, Node rightOperand) {
		this.leftOperand = leftOperand;
		this.operator = operator;
		this.rightOperand = rightOperand;

		addChild(leftOperand);
		addChild(rightOperand);
	}

	@Override
	public String toString() {
		return "Expression: " + operator.toString();
	}
}

class ComparisonNode extends Node {
	private final Node leftOperand;
	private final TokenCode operator;
	private final Node rightOperand;

	public ComparisonNode(Node leftOperand, TokenCode operator, Node rightOperand) {
		this.leftOperand = leftOperand;
		this.operator = operator;
		this.rightOperand = rightOperand;

		addChild(leftOperand);
		addChild(rightOperand);
	}

	@Override
	public String toString() {
		return "Comparison: " + operator.toString();
	}
}

abstract class DeclarationNode extends Node {
	// Тип декларации: переменная, функция, тип
	public enum DeclarationType {
		VARIABLE,
		FUNCTION
	}

	protected DeclarationType declarationType;

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	// Метод для печати узла (должен быть реализован в подклассах)
	@Override
	public abstract String toString();
}

abstract class StatementNode extends Node {
	private final String statementType;

	public StatementNode(String statementType) {
		this.statementType = statementType;
	}
	public String getStatementType() {
		return statementType;
	}
	@Override
	public String toString() {
		return "statement";
	}
}


class VariableDeclarationNode extends DeclarationNode {
	String variableName;
	ExpressionNode initializer;

	VariableDeclarationNode(String variableName, ExpressionNode initializer) {
		this.variableName = variableName;
		this.initializer = initializer;

		addChild(initializer); // Добавляем initializer в дочерние узлы
	}

	@Override
	public String toString() {
		return "Variable: " + variableName;
	}
}

class IdentifierNode extends Node {
	private final String name;

	public IdentifierNode(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Identifier: " + name;
	}

	public String getName() {
		return name;
	}
}

class BlockNode extends Node {
	private final List<Node> statements;
	private String name;

	public BlockNode(List<Node> statements, String name) {
		this.name = name;
		this.statements = statements;
		for (Node statement : statements) {
			addChild(statement); // Добавляем все узлы в дочерние
		}
	}

	@Override
	public String toString() {
		return this.name;
	}
}


class FunctionDeclarationNode extends DeclarationNode {
	String functionName;
	List<VariableDeclarationNode> parameters;
	Node functionBody;

	FunctionDeclarationNode(String functionName, List<VariableDeclarationNode> parameters, Node functionBody) {
		this.functionName = functionName;
		this.parameters = parameters;
		this.functionBody = functionBody;

		addChild(functionBody); // Добавляем body в дочерние узлы
		for (Node param : parameters) {
			addChild(param); // Добавляем параметры в дочерние узлы
		}
	}

	@Override
	public String toString() {
		return "Function: " + functionName;
	}
}

class AssignmentNode extends Node {
	private final String variableName;
	private final Node expression;

	public AssignmentNode(String variableName, Node expression) {
		this.variableName = variableName;
		this.expression = expression;

		addChild(expression); // Добавляем expression в дочерние узлы
	}

	@Override
	public String toString() {
		return "Assignment: " + variableName;
	}
}

class IfNode extends StatementNode {
	private final Node condition;
	private final BlockNode thenBody;
	private final BlockNode elseBody;

	public IfNode(Node condition, BlockNode thenBody, BlockNode elseBody) {
		super("if");
		this.condition = condition;
		this.thenBody = thenBody;
		this.elseBody = elseBody;

		addChild(condition); // Добавляем condition в дочерние узлы
		addChild(thenBody); // Добавляем thenBody
		if (elseBody != null) {
			addChild(elseBody); // Добавляем elseBody, если он есть
		}
	}

	@Override
	public String toString() {
		return "If";
	}
}



class WhileLoopNode extends StatementNode {
	private final Node condition;
	private final Node body;

	public WhileLoopNode(Node condition, Node body) {
		super("while");
		this.condition = condition;
		this.body = body;

		addChild(condition); // Добавляем condition в дочерние узлы
		addChild(body); // Добавляем body в дочерние узлы
	}

	@Override
	public String toString() {
		return "While";
	}
}

class ForLoopNode extends StatementNode {
	private final Node initialization;
	private final Node start;
	private final Node end;
	private final Node body;

	public ForLoopNode(Node initialization, Node start, Node end, Node body) {
		super("for");
		this.initialization = initialization;
		this.start = start;
		this.end = end;
		this.body = body;

		addChild(initialization); // Добавляем инициализацию в дочерние узлы
		addChild(start); // Добавляем start в дочерние узлы
		addChild(end); // Добавляем end в дочерние узлы
		addChild(body); // Добавляем body в дочерние узлы
	}

	@Override
	public String toString() {
		return "For";
	}
}

class ReturnNode extends StatementNode {
	private final Node expression;

	public ReturnNode(Node expression) {
		super("return");
		this.expression = expression;

		addChild(expression); // Добавляем expression в дочерние узлы
	}

	@Override
	public String toString() {
		return "Return";
	}
}

class PrintNode extends StatementNode {
	private final Node expression;

	public PrintNode(Node expression) {
		super("print");
		this.expression = expression;

		addChild(expression); // Добавляем expression в дочерние узлы
	}

	@Override
	public String toString() {
		return "Print";
	}
}

class LiteralNode extends ExpressionNode {
	private final Object value;

	public LiteralNode(Object value) {
		super(null, null, null);
		this.value = value;
	}

	@Override
	public String toString() {
		return "Literal: " + value.toString();
	}
}

class LogicalNode extends Node {
	private final Node leftOperand;
	private final TokenCode operator;
	private final Node rightOperand;

	public LogicalNode(Node leftOperand, TokenCode operator, Node rightOperand) {
		this.leftOperand = leftOperand;
		this.operator = operator;
		this.rightOperand = rightOperand;

		addChild(leftOperand);
		addChild(rightOperand);
	}

	@Override
	public String toString() {
		return "Logical: " + operator.toString();
	}
}


class NotNode extends Node {
	private final Node operand;

	public NotNode(Node operand) {
		this.operand = operand;
		addChild(operand); // Добавляем операнд в дочерние узлы
	}

	@Override
	public String toString() {
		return "Not";
	}
}




class ParseException extends RuntimeException {
	private final String message;
	private final Token token;

	public ParseException(String message) {
		super(message);
		this.message = message;
		this.token = null; // Неизвестный токен
	}

	public ParseException(String message, Token token) {
		super(message + " at " + token.span);
		this.message = message;
		this.token = token;
	}

	@Override
	public String getMessage() {
		if (token != null) {
			return message + " [Line: " + token.span.lineNum + ", Position: " + token.span.getPosBegin() + "]";
		}
		return message;
	}
}

class Parser {
	private List<Token> tokens;
	private int current;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
		this.current = 0;
	}

	private Token getCurrentToken() {
		return tokens.get(current);
	}

	private void advance() {
		current++;
	}

	private void rewind() {
		current--;
	}

	// Метод для разбора всей программы
	public ProgramNode parseProgram() {
		ProgramNode program = new ProgramNode();
		while (current < tokens.size()) {
			if (getCurrentToken().code == TokenCode.VAR) {
				program.addStatement(parseDeclaration());
			} else if (getCurrentToken().code == TokenCode.PRINT) {
				program.addStatement(parsePrint());
			} else if (getCurrentToken().code == TokenCode.FOR) {
				program.addStatement(parseFor());
			} else if (getCurrentToken().code == TokenCode.WHILE) {
				program.addStatement(parseWhile());
			} else if (getCurrentToken().code == TokenCode.IF) {
				program.addStatement(parseIf());
			}
			else {
				current++;
//                throw new ParseException("Unexpected token: " + getCurrentToken().code);
			}
		}
		return program;
	}

	// Разбор объявления переменных
	private VariableDeclarationNode parseDeclaration() {
		advance(); // Пропускаем 'var'
		Identifier variableName = (Identifier) getCurrentToken(); // Имя переменной
		advance(); // Пропускаем идентификатор

		if (getCurrentToken().code == TokenCode.ASSIGN) {
			advance(); // Пропускаем '='
			ExpressionNode initializer = (ExpressionNode) parseExpression(); // Разбор выражения
			return new VariableDeclarationNode(variableName.identifier, initializer);
		} else {
			return new VariableDeclarationNode(variableName.identifier, null); // Без инициализации
		}
	}

	// Разбор цикла for
	private ForLoopNode parseFor() {
		advance(); // Пропускаем 'for'
		Identifier variableName = (Identifier) getCurrentToken(); // Имя счетчика
		advance(); // Пропускаем идентификатор

		if (getCurrentToken().code == TokenCode.IN) {
			advance(); // Пропускаем 'in'
			Node rangeStart = parseExpression(); // Разбор начала диапазона
			if (getCurrentToken().code == TokenCode.TWO_DOT) {
				advance(); // Пропускаем 'to'
				Node rangeEnd = parseExpression(); // Разбор конца диапазона
				if (getCurrentToken().code == TokenCode.LOOP) {
					advance(); // Пропускаем 'loop'
					Node body = parseStatement(); // Разбор тела цикла
					advance(); // Пропускаем 'end'
					return new ForLoopNode(new AssignmentNode(variableName.identifier, rangeStart), rangeStart, rangeEnd, body);
				}
				throw new ParseException("Expected 'loop', found: " + getCurrentToken());
			}
			throw new ParseException("Expected 'to', found: " + getCurrentToken());
		}
		throw new ParseException("Expected 'in', found: " + getCurrentToken());
	}

	// Разбор цикла while
	private WhileLoopNode parseWhile() {
		advance(); // Пропускаем 'while'
		Node condition = parseExpression(); // Разбор условия

		if (getCurrentToken().code == TokenCode.LOOP) {
			advance(); // Пропускаем 'loop'
			Node body = parseStatement(); // Разбор тела цикла
			advance(); // Пропускаем 'end'
			return new WhileLoopNode(condition, body);
		}
		throw new ParseException("Expected 'loop', found: " + getCurrentToken());
	}

	// Разбор условного оператора
//    private IfNode parseIf() {
//        advance(); // Пропускаем 'if'
//        Node condition = parseComparison(); // Разбор условия
//        if (getCurrentToken().code == TokenCode.THEN) {
//            advance(); // Пропускаем 'then'
//            Node thenBody = parseStatement(); // Разбор тела
//            Node elseBody = null;
//
//            if (getCurrentToken().code == TokenCode.SEMICOLON) {
//                advance(); // Пропускаем 'SEMICOLON'
//            } else {
//                throw new ParseException("Expected ';', found: " + getCurrentToken());
//            }
//
//            if (getCurrentToken().code == TokenCode.ELSE) {
//                advance(); // Пропускаем 'else'
//                elseBody = parseStatement(); // Разбор альтернативного тела
//            }
//            return new IfNode(condition, thenBody, elseBody);
//        }
//        throw new ParseException("Expected 'then', found: " + getCurrentToken());
//    }

	private IfNode parseIf() {
		advance(); // Пропускаем 'if'

		Node condition = parseCondition(); // Используем новый метод для парсинга условия

		if (getCurrentToken().code == TokenCode.THEN) {
			advance(); // Пропускаем 'then'

			List<Node> thenBodyStatements = new ArrayList<>();
			while (getCurrentToken().code != TokenCode.END && getCurrentToken().code != TokenCode.ELSE) {
				if (getCurrentToken().code == TokenCode.SEMICOLON) {
					advance();
				} else {
					thenBodyStatements.add(parseStatement()); // Разбор тела
				}
			}

			BlockNode thenBody = new BlockNode(thenBodyStatements, "Body");

			if (getCurrentToken().code == TokenCode.END) {
				advance(); // Пропускаем 'end'
			}

			List<Node> elseBodyStatements = new ArrayList<>();
			BlockNode elseBody = null;
			if (getCurrentToken().code == TokenCode.ELSE) {
				advance(); // Пропускаем 'else'
				while (getCurrentToken().code != TokenCode.END) {
					if (getCurrentToken().code == TokenCode.SEMICOLON) {
						advance();
					} else {
						elseBodyStatements.add(parseStatement());
					}
				}
				elseBody = new BlockNode(elseBodyStatements, "Else");

				if (getCurrentToken().code == TokenCode.END) {
					advance(); // Пропускаем 'end'
				} else {
					throw new ParseException("Expected 'end', found: " + getCurrentToken());
				}
			}

			return new IfNode(condition, thenBody, elseBody);
		}

		throw new ParseException("Expected 'then', found: " + getCurrentToken().code);
	}

	// Разбор команды print
	private PrintNode parsePrint() {
		advance(); // Пропускаем 'print'
		Node expression = parseExpression(); // Разбор выражения
		return new PrintNode(expression);
	}

	private Node parseIdentifier() {
		if (getCurrentToken().code == TokenCode.IDENTIFIER) {
			Identifier identifierToken = (Identifier) getCurrentToken(); // Получаем имя идентификатора
			String identifierName = identifierToken.identifier; // Получаем имя идентификатора
			advance(); // Пропускаем токен идентификатора

			// Здесь можно добавить логику для обработки идентификаторов,
			// например, создание узла переменной или обработки дополнительных токенов
			return new IdentifierNode(identifierName);
		}

		throw new ParseException("Expected identifier, found: " + getCurrentToken().code);
	}


	private ComparisonNode parseTypeCheck() {
		Node identifier = parseIdentifier(); // Парсим идентификатор (например, x)

		if (getCurrentToken().code == TokenCode.IS) {
			advance(); // Пропускаем 'is'

			Node typeNode = parseType(); // Метод для разбора типа (int, real и т.д.)

			return new ComparisonNode(identifier, TokenCode.IS, typeNode);
		}

		throw new ParseException("Expected 'is', found: " + getCurrentToken());
	}


	private Node parseType() {
		switch (getCurrentToken().code) {
			case INT:
				advance(); // Пропускаем 'int'
				return new LiteralNode("int");
			case REAL:
				advance(); // Пропускаем 'real'
				return new LiteralNode("real");
			case STRING:
				advance(); // Пропускаем 'string'
				return new LiteralNode("string");
			case EMPTY:
				advance(); // Пропускаем 'empty'
				return new LiteralNode("empty");
			default:
				throw new ParseException("Expected a type, found: " + getCurrentToken());
		}
	}


	private Node parseCondition() {
		if (getCurrentToken().code == TokenCode.IDENTIFIER) {
			// Смотрим следующий токен, чтобы определить, является ли это проверкой типа
			advance(); // Пропускаем идентификатор
			if (getCurrentToken().code == TokenCode.IS) {
				rewind();
				return parseTypeCheck(); // Если следующий токен 'is', парсим проверку типа
			} else {
				// Возвращаем обратно, если это не проверка типа
				rewind();// Метод для возврата к предыдущему токену
				return parseComparison(); // Или парсим обычное сравнение
			}
		}

		return parseComparison(); // Если не идентификатор, просто парсим сравнение
	}


	// Разбор выражений

	private Node parseComparison() {
		// Проверяем наличие 'not'
		if (getCurrentToken().code == TokenCode.NOT) {
			advance(); // Пропускаем 'not'
			Node innerComparison = parseComparison(); // Рекурсивно разбираем следующее выражение
			return new NotNode(innerComparison); // Возвращаем узел not
		}

		// Проверяем наличие скобок
		if (getCurrentToken().code == TokenCode.LPAREN) {
			advance(); // Пропускаем '('
			Node innerComparison = parseLogicalExpression(); // Обрабатываем логическое выражение внутри скобок
			if (getCurrentToken().code != TokenCode.RPAREN) {
				throw new ParseException("Expected ')', found: " + getCurrentToken());
			}
			advance(); // Пропускаем ')'
			return innerComparison; // Возвращаем результат
		}

		// Обрабатываем обычное сравнение
		Node leftOperand = parseExpression(); // Начинаем с разбора первого операнда

		// Обрабатываем оператор сравнения
		TokenCode operator = getCurrentToken().code;
		if (operator == TokenCode.LESS || operator == TokenCode.GREATER || operator == TokenCode.EQUAL ||
				operator == TokenCode.LESS_EQUAL || operator == TokenCode.GREATER_EQUAL) {
			advance(); // Пропускаем оператор
			Node rightOperand = parseExpression(); // Получаем правый операнд
			leftOperand = new ComparisonNode(leftOperand, operator, rightOperand); // Создаем узел сравнения
		}

		// Обработка логических операторов
		while (getCurrentToken().code == TokenCode.AND || getCurrentToken().code == TokenCode.OR) {
			TokenCode logicalOperator = getCurrentToken().code;
			advance(); // Пропускаем логический оператор
			Node rightOperand = parseComparison(); // Разбираем следующее выражение
			leftOperand = new LogicalNode(leftOperand, logicalOperator, rightOperand); // Создаем узел логической операции
		}

		return leftOperand; // Возвращаем результат сравнения
	}


	private Node parseLogicalExpression() {
		Node leftOperand = parseComparison(); // Обрабатываем первое сравнение

		while (getCurrentToken().code == TokenCode.AND || getCurrentToken().code == TokenCode.OR) {
			TokenCode operator = getCurrentToken().code;
			advance(); // Пропускаем логический оператор
			Node rightOperand = parseComparison(); // Обрабатываем следующее сравнение
			leftOperand = new LogicalNode(leftOperand, operator, rightOperand); // Создаем узел логической операции
		}

		return leftOperand; // Возвращаем результат логического выражения
	}







	private boolean isComparisonOperator(TokenCode code) {
		return code == TokenCode.GREATER ||
				code == TokenCode.LESS ||
				code == TokenCode.GREATER_EQUAL ||
				code == TokenCode.LESS_EQUAL ||
				code == TokenCode.EQUAL;
	}

	// Метод для разбора выражений с учетом приоритетов
	private Node parseExpression() {
		return parseExpressionWithPrecedence(0);
	}

	// Метод для обработки операторов с учетом их приоритета
	private Node parseExpressionWithPrecedence(int precedence) {
		Node left = parsePrimary(); // Начинаем с первичного выражения (литерал или идентификатор)

		// Обрабатываем операторы, учитывая их приоритет
		while (isOperator(getCurrentToken().code) && precedence < getPrecedence(getCurrentToken().code)) {
			Token operator = getCurrentToken(); // Сохраняем оператор
			advance(); // Пропускаем оператор

			int operatorPrecedence = getPrecedence(operator.code);
			Node right = parseExpressionWithPrecedence(operatorPrecedence); // Рекурсивно обрабатываем правую часть с приоритетом

			left = new ExpressionNode(left, operator.code, right); // Создаем новое бинарное выражение
		}

		// Обрабатываем присваивание в случае, если оператором является ':=' или '='
		if (getCurrentToken().code == TokenCode.ASSIGN) {
			advance(); // Пропускаем ':=' или '='

			// Разбираем правую часть присваивания
			Node right = parseExpression();

			// Создаем VariableDeclarationNode с инициализацией
			if (left instanceof IdentifierNode) {
				return new VariableDeclarationNode(((IdentifierNode) left).getName(), (ExpressionNode) right);
			} else {
				throw new ParseException("Left-hand side of assignment must be an identifier");
			}
		}

		return left; // Возвращаем результат
	}

	// Метод для обработки приоритетов операторов
	private int getPrecedence(TokenCode code) {
		switch (code) {
			case PLUS:
			case MINUS:
				return 1;
			case MULTIPLY:
			case DIVIDE:
				return 2;
			default:
				return 0;
		}
	}

	// Метод для разбора первичных выражений (литералы, идентификаторы, скобки)
	private Node parsePrimary() {
		if (getCurrentToken().code == TokenCode.REAL_LITERAL) {
			RealToken realToken = (RealToken) getCurrentToken();
			advance();
			return new LiteralNode(realToken.value);
		} else if (getCurrentToken().code == TokenCode.INTEGER_LITERAL) {
			IntegerToken integerToken = (IntegerToken) getCurrentToken();
			advance();
			return new LiteralNode(integerToken.value);
		} else if (getCurrentToken().code == TokenCode.STRING_LITERAL) {
			StringToken stringToken = (StringToken) getCurrentToken();
			advance();
			return new LiteralNode(stringToken.value);
		} else if (getCurrentToken().code == TokenCode.LPAREN) {
			advance(); // Пропускаем '('
			Node expression = parseExpression(); // Разбор выражения в скобках
			if (getCurrentToken().code != TokenCode.RPAREN) {
				throw new ParseException("Expected ')', found: " + getCurrentToken());
			}
			advance(); // Пропускаем ')'
			return expression;
		} else if (getCurrentToken().code == TokenCode.IDENTIFIER) {
			Identifier identifierToken = (Identifier) getCurrentToken();
			advance();
			return new IdentifierNode(identifierToken.identifier); // Возвращаем идентификатор как первичное выражение
		}

		throw new ParseException("Unexpected token: " + getCurrentToken());
	}

	// Метод для проверки, является ли текущий токен оператором
	private boolean isOperator(TokenCode code) {
		return code == TokenCode.PLUS || code == TokenCode.MINUS || code == TokenCode.MULTIPLY || code == TokenCode.DIVIDE;
	}



	// Метод для разбора команд (например, блоки кода)
	private Node parseStatement() {
		if (getCurrentToken().code == TokenCode.IF) {
			return parseIf();
		} else if (getCurrentToken().code == TokenCode.WHILE) {
			return parseWhile();
		} else if (getCurrentToken().code == TokenCode.FOR) {
			return parseFor();
		} else if (getCurrentToken().code == TokenCode.PRINT) {
			return parsePrint();
		} else if (getCurrentToken().code == TokenCode.VAR) {
			return parseDeclaration(); // Разбор объявления переменной
		}
		throw new ParseException("Unexpected statement type: " + getCurrentToken().code);
	}
}

class LexerException extends RuntimeException {
	public LexerException(String message) {
		super(message);
	}
}

class Span {
	public long lineNum;
	public int posBegin, posEnd;

	public Span(long lineNum, int posBegin, int posEnd) {
		this.lineNum = lineNum;
		this.posBegin = posBegin;
		this.posEnd = posEnd;
	}

	public int getPosBegin() {
		return posBegin + 1;
	}

	public int getPosEnd() {
		return posEnd + 1;
	}
}

class Token {
	protected Span span;        // диапазон токена
	protected TokenCode code;   // код токена (из TokenCode)


	public Token(TokenCode code, Span span) {
		this.code = code;
		this.span = span;
	}


	public void print() {
		System.out.print(code + " ");
	}

	public void fullPrint() {
		System.out.println(code + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}

}


class Identifier extends Token {
	public String identifier;

	public Identifier(String identifier, Span span) {
		super(TokenCode.IDENTIFIER, span);
		this.identifier = identifier;
	}

	public void print() {
		System.out.print(code + "[" + this.identifier + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.identifier + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}


class RealToken extends Token {
	public double value;

	public RealToken(double value, Span span) {
		super(TokenCode.REAL_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}

class BooleanToken extends Token {
	public boolean value;

	public BooleanToken(boolean value, Span span) {
		super(TokenCode.BOOLEAN_LITERAL, span);
		this.value = value;
	}


	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}

class IntegerToken extends Token {
	public int value;

	public IntegerToken(int value, Span span) {
		super(TokenCode.INTEGER_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}


class StringToken extends Token {
	public String value;

	public StringToken(String value, Span span) {
		super(TokenCode.STRING_LITERAL, span);
		this.value = value;
	}


	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}


enum TokenCode {
	// Ключевые слова
	VAR, IF, ELSE, WHILE, FOR, RETURN, PRINT, FUNC,
	LOOP, END, IS, THEN, IN, READ_INT, READ_REAL, READ_STRING, LENGTH,

	// Булевые литералы и специальное значение
	TRUE, FALSE, EMPTY,

	// Типы данных
	INT, REAL, BOOL, STRING,

	// Операторы
	ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE,
	AND, OR, XOR, NOT,
	LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL, IMPLICATION,

	// Специальные операторы
	DOT, LBRACKET, RBRACKET, LPAREN, RPAREN, TWO_DOT, QUOTE, DOUBLE_QUOTE,

	// Литералы
	INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL, TUPLE_LITERAL, ARRAY_LITERAL,

	// Разделители
	SEMICOLON, COMMA, LBRACE, RBRACE,

	// Идентификатор
	IDENTIFIER,

	// Конец файла
	EOF
}


class Lexer {
	private final String code; // Код который мы чекаем
	private int lineNum = 1; // На какой сейчас строчке стоит поинт (для span)
	private int currentCharNum = 0; // На каком индексе сейчас находимся

	List<Character> symbolList = Arrays.asList('(', ')', ',', '+', '/', '-', '=', ':', ';', '>', '<', '[', ']', '{', '}', '.');

	public Lexer (String code) {
		this.code = code;
	}

	private void spacesDelete() {
		while (this.currentCharNum < this.code.length() && Character.isWhitespace(this.code.charAt(this.currentCharNum))) {
			if (this.code.charAt(this.currentCharNum) == '\n') {
				lineNum++;
				System.out.println();
			}

			currentCharNum++;
		}
	}

	private void findWordEnd() {
		while(this.currentCharNum < this.code.length() && !Character.isWhitespace(this.code.charAt(this.currentCharNum)) && (!specSymbolCheck(this.currentCharNum))) {
			currentCharNum++;
		}
	}


	private boolean digitCheck(Span span) {
		for (int i = span.posBegin; i < span.posEnd; i++) {
			if (!(Character.isDigit(this.code.charAt(i)) || this.code.charAt(i) == '.' || this.code.charAt(i) == ',')) {
				return false;
			}
		}
		return true;
	}

	private int numberDelimCounter(Span span) {
		int delNum = 0;
		for (int i = span.posBegin; i < span.posEnd; i++) {
			if (this.code.charAt(i) == '.' || this.code.charAt(i) == ',') delNum++;
		}
		return delNum;
	}

	private Token scanNumber(Span span) {
		if (numberDelimCounter(span) > 1) {
			throw new LexerException("Invalid real number format at line " + this.lineNum);
		} else if (numberDelimCounter(span) == 1) {
			double value = Double.valueOf(this.code.substring(span.posBegin, span.posEnd).replace(',', '.'));
			return new RealToken(value, span);
		} else {
			int value = Integer.valueOf(this.code.substring(span.posBegin, span.posEnd).replace(',', '.'));
			return new IntegerToken(value, span);
		}
	}

	private boolean specSymbolCheck(int num) {
		return symbolList.contains(this.code.charAt(num));
	}

	private Token stringTokenFind() {
		this.currentCharNum++;
		int startNum = this.currentCharNum;
		while (code.charAt(this.currentCharNum) != '"') {
			if (code.charAt(this.currentCharNum) == '\n') {
				lineNum++;
				System.out.println();
			}
			currentCharNum++;
		}
		String value = code.substring(startNum, currentCharNum);
		Span span = new Span(lineNum, startNum, currentCharNum);
		currentCharNum++;
		return new StringToken(value, span);
	}


	public Token tokenFind() {
		spacesDelete();

		if (this.currentCharNum >= code.length() ) {
			return new Token(TokenCode.EOF, new Span(lineNum, currentCharNum, currentCharNum));
		}

		int firstCharNum = currentCharNum;

		if (code.charAt(this.currentCharNum) == '"') {
			return stringTokenFind();
		}

		if (specSymbolCheck(this.currentCharNum)) {
			int offset = 1;
			if (this.currentCharNum+1 < code.length() && specSymbolCheck(this.currentCharNum+1)) offset++;
			TokenCode token = getKeywordTokenCode(this.code.substring(this.currentCharNum, this.currentCharNum+offset));
			if (token == TokenCode.IDENTIFIER) {
				offset = 1;
				token = getKeywordTokenCode(this.code.substring(this.currentCharNum, this.currentCharNum+offset));
			}
			currentCharNum += offset;
			return new Token(token, new Span(lineNum, this.currentCharNum, this.currentCharNum+offset));
		}

		findWordEnd();

		Span span = new Span(lineNum, firstCharNum, currentCharNum);

		if (digitCheck(span)) {
			return scanNumber(span);

		}
		String word = this.code.substring(span.posBegin, span.posEnd).toLowerCase();
		TokenCode token = getKeywordTokenCode(word);

		if (token == TokenCode.IDENTIFIER) {
			return new Identifier(this.code.substring(span.posBegin, span.posEnd), span);
		}

		if (token == TokenCode.TRUE) {
			return new BooleanToken(true, span);
		}

		if (token == TokenCode.FALSE) {
			return new BooleanToken(false, span);
		}

		return new Token(token, span);

	}

	public List<Token> start() {
		List<Token> tokenList = new ArrayList<>();
		while (true) {
			Token token = tokenFind();
			tokenList.add(token);
			token.print();
			if (token.code == TokenCode.EOF) {
				return tokenList;
			}
		}
	}


	private TokenCode getKeywordTokenCode(String str) {
		if (str.length() == 1) {
			if (str.equals("<")){
				return TokenCode.LESS;
			}
			else if (str.equals(">")){
				return TokenCode.GREATER;
			}
			else if (str.equals("=")){
				return TokenCode.EQUAL;
			}
			else if (str.equals("*")){
				return TokenCode.MULTIPLY;
			}
			else if (str.equals("/")){
				return TokenCode.DIVIDE;
			}
			else if (str.equals("-")){
				return TokenCode.MINUS;
			}
			else if (str.equals("+")){
				return TokenCode.PLUS;
			}
			else if (str.equals("(")){
				return TokenCode.LPAREN;
			}
			else if (str.equals(")")){
				return TokenCode.RPAREN;
			}
			else if (str.equals("[")){
				return TokenCode.LBRACKET;
			}
			else if (str.equals("]")){
				return TokenCode.RBRACKET;
			}
			else if (str.equals("{")){
				return TokenCode.LBRACE;
			}
			else if (str.equals("}")){
				return TokenCode.RBRACE;
			}
			else if (str.equals(".")){
				return TokenCode.DOT;
			}
			else if (str.equals(",")){
				return TokenCode.COMMA;
			}
			else if (str.equals(";")){
				return TokenCode.SEMICOLON;
			}
			else if (str.equals("'")){
				return TokenCode.QUOTE;
			}
			else if (str.equals("\"")){
				return TokenCode.DOUBLE_QUOTE;
			}

		}
		if (str.length() == 2) {
			if (str.equals("<=")){
				return TokenCode.LESS_EQUAL;
			}
			else if (str.equals("..")){
				return TokenCode.TWO_DOT;
			}
			else if (str.equals(">=")){
				return TokenCode.GREATER_EQUAL;
			}
			else if (str.equals("=>")){
				return TokenCode.IMPLICATION;
			}
			else if (str.equals(":=")){
				return TokenCode.ASSIGN;
			}
			else if (str.equals("/=")){
				return TokenCode.NOT_EQUAL;
			}
			else if (str.equals("or")){
				return TokenCode.OR;
			}
			else if (str.equals("if")){
				return TokenCode.IF;
			}
			else if (str.equals("in")){
				return TokenCode.IN;
			}
			else if (str.equals("is")){
				return TokenCode.IS;
			}
		}
		if (str.length() == 3) {
			if (str.equals("and")) {
				return TokenCode.AND;
			}
			else if (str.equals("for")) {
				return TokenCode.FOR;
			}
			else if (str.equals("xor")) {
				return TokenCode.XOR;
			}
			else if (str.equals("not")) {
				return TokenCode.NOT;
			}
			else if (str.equals("int")) {
				return TokenCode.INT;
			}
			else if (str.equals("var")) {
				return TokenCode.VAR;
			}
			else if (str.equals("end")) {
				return TokenCode.END;
			}
		}
		if (str.length() == 4) {
			if (str.equals("bool")) {
				return TokenCode.BOOL;
			}
			else if (str.equals("real")) {
				return TokenCode.REAL;
			}
			else if (str.equals("true")) {
				return TokenCode.TRUE;
			}
			else if (str.equals("else")) {
				return TokenCode.ELSE;
			}
			else if (str.equals("func")) {
				return TokenCode.FUNC;
			}
			else if (str.equals("loop")) {
				return TokenCode.LOOP;
			}
			else if (str.equals("then")) {
				return TokenCode.THEN;
			}
		}
		if (str.length() == 5) {
			if (str.equals("false")) {
				return TokenCode.FALSE;
			}
			else if (str.equals("empty")) {
				return TokenCode.EMPTY;
			}
			else if (str.equals("print")) {
				return TokenCode.PRINT;
			}
			else if (str.equals("while")) {
				return TokenCode.WHILE;
			}
		}
		if (str.length() == 6) {
			if (str.equals("string")) {
				return TokenCode.STRING;
			}
			else if (str.equals("return")) {
				return TokenCode.RETURN;
			}
			else if (str.equals("length")) {
				return TokenCode.LENGTH;
			}
		}
		if (str.length() == 7) {
			if (str.equals("readInt")) {
				return TokenCode.READ_INT;
			}
		}
		if (str.length() == 8) {
			if (str.equals("readReal")) {
				return TokenCode.READ_REAL;
			}
		}
		if (str.length() == 10) {
			if (str.equals("readString")) {
				return TokenCode.READ_STRING;
			}
		}
		return TokenCode.IDENTIFIER;
	}

	public static void printTree(Node node, int depth) {
		if (node == null) return;

		// Добавляем отступы
		for (int i = 0; i < depth; i++) {
			System.out.print("  "); // 2 пробела для каждого уровня
		}

		// Выводим информацию о текущем узле (можно заменить на нужный формат)
		System.out.println(node.getClass().getSimpleName());

		// Рекурсивно выводим дочерние узлы
		for (Node child : node.getChildren()) {
			printTree(child, depth + 1);
		}
	}

	public static void main(String[] args) {
		// Путь к файлу

		for (int i = 0; i <= 9; i++) {
			String filePath = "test" + i + ".d";

			System.out.println();
			System.out.println();
			System.out.println("-----------------  " + "test" + i + ".d" + "  --------------------");
			System.out.println();
			System.out.println();

			try {
				// Чтение содержимого файла в строку
				String str = new String(Files.readAllBytes(Paths.get(filePath)));

				// Передача строки в конструктор Lexer
				Lexer lexer = new Lexer(str);
				List<Token> tokenList = lexer.start();
				System.out.println();
				System.out.println();
				System.out.println();
				Parser parser = new Parser(tokenList); // Предположим, что у тебя есть класс Parser
				System.out.println(123);
				Node ast = parser.parseProgram();
				System.out.println(ast);// Метод для парсинга
				ast.printTree("", true);
//                printTree(ast);
//                System.out.println(ast.toString(5)); // Вывод AST


				// Ваши действия с объектом Lexer

			} catch (IOException e) {
				// Обработка ошибки, если файл не найден или произошла ошибка ввода/вывода
				System.out.println("Ошибка чтения файла: " + e.getMessage());
			}
		}
	}
}


