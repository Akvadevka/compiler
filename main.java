class Span {
	public long lineNum;
	public int posBegin, posEnd;

	public Span(long lineNum, int posBegin, int posEnd) {
		this.lineNum = lineNum;
		this.posBegin = posBegin;
		this.posEnd = posEnd;
	}
}

class Token {
	protected Span span;        // диапазон токена
	protected TokenCode code;   // код токена (из TokenCode)


	public Token(TokenCode code, Span span) {
		this.code = code;
		this.span = span;
	}

}


class Identifier extends Token {
	private String identifier;

	public Identifier(String identifier, Span span) {
		super(TokenCode.IDENTIFIER, span);
		this.identifier = identifier;
	}
}


class RealToken extends Token {
	private double value;

	public RealToken(double value, Span span) {
		super(TokenCode.REAL_LITERAL, span);
		this.value = value;
	}
}


class IntegerToken extends Token {
	private int value;

	public IntegerToken(int value, Span span) {
		super(TokenCode.INTEGER_LITERAL, span);
		this.value = value;
	}
}


class StringToken extends Token {
	private String value;

	public StringToken(String value, Span span) {
		super(TokenCode.STRING_LITERAL, span);
		this.value = value;
	}
}


enum TokenCode {
	// Ключевые слова
	VAR, IF, ELSE, WHILE, FOR, RETURN, PRINT, FUNC, LOOP, END, IS, THEN, IN,

	// Булевые литералы и специальное значение
	TRUE, FALSE, EMPTY,

	// Типы данных
	INT, REAL, BOOL, STRING, VECTOR_TYPE, TUPLE_TYPE, FUNCTION_TYPE,

	// Операторы
	ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE,
	AND, OR, XOR, NOT,
	LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL,

	// Специальные операторы
	DOT, LBRACKET, RBRACKET, LPAREN, RPAREN,

	// Литералы
	INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL, TUPLE_LITERAL, ARRAY_LITERAL,

	// Разделители
	SEMICOLON, COMMA, LBRACE, RBRACE,

	// Идентификатор
	IDENTIFIER,

	// Конец файла
	EOF
}


//test