/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
  */

package cop5556fa17;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	
	public static enum State{
		START, IDENTIFIER, AFTER_EQ, NOT_EQ ,AFTER_MUL,STRING_LIT,
		AFTER_LT, AFTER_MINUS, AFTER_GT, IS_DIGIT, INTEGER_LIT, AFTER_DIV, COMMENT, CRLF;
	}

	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  



	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
		
		//Create list of known keywords
		
		
	}
	
	public void AddKeywords()
	{
		keywords.put("x", Kind.KW_x);
		keywords.put("X",Kind.KW_X);
		keywords.put("y", Kind.KW_y);
		keywords.put("Y", Kind.KW_Y);
		keywords.put("r", Kind.KW_r);
		keywords.put("R", Kind.KW_R);
		keywords.put("a", Kind.KW_a);
		keywords.put("A", Kind.KW_A);
		keywords.put("Z", Kind.KW_Z);
		keywords.put("DEF_X", Kind.KW_DEF_X);
		keywords.put("DEF_Y", Kind.KW_DEF_Y);
		keywords.put("SCREEN", Kind.KW_SCREEN);
		keywords.put("cart_x",Kind.KW_cart_x);
		keywords.put("cart_y", Kind.KW_cart_y);
		keywords.put("polar_a", Kind.KW_polar_a);
		keywords.put("polar_r", Kind.KW_polar_r);
		keywords.put("abs", Kind.KW_abs);
		keywords.put("sin", Kind.KW_sin);
		keywords.put("cos", Kind.KW_cos);
		keywords.put("atan", Kind.KW_atan);
		keywords.put("log", Kind.KW_log);
		keywords.put("image", Kind.KW_image);
		keywords.put("int", Kind.KW_int);
		keywords.put("boolean", Kind.KW_boolean);
		keywords.put("url", Kind.KW_url);
		keywords.put("file", Kind.KW_file);
	}
	
	public void AddOpKeywords()
	{
		opKeywords.put("=", Kind.OP_ASSIGN);
		opKeywords.put(">", Kind.OP_GT);
		opKeywords.put("<", Kind.OP_LT);
		opKeywords.put("!",Kind.OP_EXCL);
		opKeywords.put("?",Kind.OP_Q);
		opKeywords.put(":",Kind.OP_COLON);
		opKeywords.put("==",Kind.OP_EQ);
		opKeywords.put("!=",Kind.OP_NEQ);
		opKeywords.put(">=",Kind.OP_GE);
		opKeywords.put("<=",Kind.OP_LE);
		opKeywords.put("&",Kind.OP_AND);
		opKeywords.put("|",Kind.OP_OR);
		opKeywords.put("+",Kind.OP_PLUS);
		opKeywords.put("-",Kind.OP_MINUS);
		opKeywords.put("*",Kind.OP_TIMES);
		opKeywords.put("/",Kind.OP_DIV);
		opKeywords.put("%",Kind.OP_MOD);
		opKeywords.put("**",Kind.OP_POWER);
		opKeywords.put("@",Kind.OP_AT);
		opKeywords.put("->",Kind.OP_RARROW);
	}
	
	public void AddSeparators()
	{
		separator.put("(",Kind.LPAREN);
		separator.put(")",Kind.RPAREN);
		separator.put("[",Kind.RSQUARE);
		separator.put("]",Kind.LSQUARE);
		separator.put(",",Kind.COMMA);
		separator.put(";",Kind.SEMI);
		separator.put("[", Kind.LSQUARE);
		separator.put("]", Kind.RSQUARE);
	}


	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		AddKeywords();
		AddOpKeywords();
		AddSeparators();
		
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		int tokenStart = 0;
		State state = State.START;
		int ipLength = chars.length;
		char ch;
		while(chars[pos] != EOFchar)
		{
			//ch = (int) (pos < ipLength ? chars[pos]:0);
			ch=chars[pos];
			
			//handles CR and Newline since switch case cant identify these
			if((chars[pos] == '\n' || chars[pos]=='\r') && state != State.COMMENT && state != State.STRING_LIT && state !=State.IS_DIGIT && state != State.IDENTIFIER)
			{
				if(chars[pos] == '\r' &&  chars[pos+1] == '\n')
				{
					pos+=2;
					line++;
					posInLine = 1;
				}
				else
				{
					pos+=1;
					posInLine = 1;
					line++;
				}
				//state = State.START;
				continue;
			}
			
			switch(state) {
				case START:{
					if(pos == ipLength)
						ch = EOFchar;
					//skip white spaces if any before token begins
					for (int i = pos; i < ipLength && Character.isWhitespace(chars[i]) && chars[i] != '\n' && chars[i] != '\r' ; i++, pos++,posInLine++); 
					ch = chars[pos];
					if(ch == EOFchar)
						break;
					
					if((chars[pos] == '\n' || chars[pos]=='\r') && state != State.COMMENT && state != State.STRING_LIT && state !=State.IS_DIGIT && state != State.IDENTIFIER)
					{
						if(chars[pos] == '\r' && (chars[pos+1] == '\n' || chars[pos+1] == '\r'))
						{
							pos+=2;
							line++;
							posInLine = 1;
						}
						else
						{
							pos++;
							posInLine = 1;
							line++;
						}
						//state = State.START;
						continue;
					}
					
					tokenStart = pos;
					switch(ch) {
						
						case EOFchar:{
							tokens.add(new Token(Kind.EOF,tokenStart,0,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '(':{
							tokens.add(new Token(Kind.LPAREN,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case ')':{
							tokens.add(new Token(Kind.RPAREN,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '[':{
							tokens.add(new Token(Kind.LSQUARE,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case ']':{
							tokens.add(new Token(Kind.RSQUARE,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '?':{
							tokens.add(new Token(Kind.OP_Q,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '@':{
							tokens.add(new Token(Kind.OP_AT,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case ',':{
							tokens.add(new Token(Kind.COMMA,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case ';':{
							tokens.add(new Token(Kind.SEMI,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '/':{
							if(chars[pos+1] =='/')
							{
								state = State.AFTER_DIV;
								pos++;
								posInLine++;
							}
							else {
								tokens.add(new Token(Kind.OP_DIV,tokenStart,1,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
							}
							}
						break;
						
						case '*':{
							if(chars[pos+1] == '*')
							{
								state = State.AFTER_MUL;
								pos++;
								posInLine++;
							}
							else {
								tokens.add(new Token(Kind.OP_TIMES,tokenStart,1,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
							}
						}
						break;
						
						case '+':{
							tokens.add(new Token(Kind.OP_PLUS,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '%':{
							tokens.add(new Token(Kind.OP_MOD,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '&':{
							tokens.add(new Token(Kind.OP_AND,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '!':{
							if(chars[pos+1] == '=')
							{
								state = State.NOT_EQ;
								pos++;
								posInLine++;
							}
							else
							{
								tokens.add(new Token(Kind.OP_EXCL,tokenStart,1,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
							}
						}
						break;
						
						case '=':{
							if(chars[pos+1] == '=')
							{
								state = State.AFTER_EQ;
								pos++;
								posInLine++;
							}
							else {
								tokens.add(new Token(Kind.OP_ASSIGN,tokenStart,1,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
							}
						}
						break;
						
						case ':':{
							tokens.add(new Token(Kind.OP_COLON,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '|':{
							tokens.add(new Token(Kind.OP_OR,tokenStart,1,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
						}
						break;
						
						case '>':{
							if(chars[pos+1] == '=') {
								state =  State.AFTER_GT;
								pos++;
								posInLine++;
							}
							else {
								tokens.add(new Token(Kind.OP_GT,tokenStart,1,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
							}
						}
						break;
						
						case '<':{
							if(chars[pos+1] == '=' || chars[pos+1] == '-')
							{
								state = State.AFTER_LT;
								pos++;
								posInLine++;
							}
							else
							{
								tokens.add(new Token(Kind.OP_LT,tokenStart,1,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
							}
						}
						break;
						
						case '-':{
							if(chars[pos+1] == '>')
							{
								state = State.AFTER_MINUS;
								pos++;
								posInLine++;
							}
							else
							{
								tokens.add(new Token(Kind.OP_MINUS,tokenStart,1,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
							}
						}
						break;
						
						case '\"':{
							if(chars[pos+1] != EOFchar)
							{
								state = State.STRING_LIT;
								pos++;
								posInLine++;
							}
							else {
								//tokens.add(new Token(Kind.IDENTIFIER,tokenStart,1,line,posInLine-(pos-tokenStart)));
								//pos++;
								//posInLine++;
								throw new LexicalException("illegal character",pos);
							}
						}
						break;
						
						//Integer literal & character literal
						default:{
							if(Character.isDigit(ch))
							{
								if(chars[pos+1] != EOFchar)
								{
									state = State.IS_DIGIT;
									pos++;
									posInLine++;
								}
								else
								{
									tokens.add(new Token(Kind.INTEGER_LITERAL,tokenStart,1,line,posInLine-(pos-tokenStart)));
									pos++;
									posInLine++;
								}
							}
							else if(Character.isJavaIdentifierStart(ch))
								{
									if(chars[pos+1] != EOFchar)
									{
										state = State.IDENTIFIER;
										pos++;
										posInLine++;
									}
									else
									{
										String ident = new String(chars, tokenStart, pos-tokenStart+1);
										ident.toString();
										if(ident.equals("true")||ident.equals("false"))
										{
											tokens.add(new Token(Kind.BOOLEAN_LITERAL,tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
										}
										else if(keywords.containsKey(ident))
											tokens.add(new Token(keywords.get(ident), tokenStart, pos-tokenStart+1,line, posInLine-(pos-tokenStart)));
										else if(opKeywords.containsKey(ident))
											tokens.add(new Token(opKeywords.get(ident),tokenStart,pos-tokenStart+1,line,posInLine-(pos-tokenStart)));
										else if(separator.containsKey(ident))
											tokens.add(new Token(separator.get(ident),tokenStart,pos-tokenStart+1,line,posInLine-(pos-tokenStart)));
										else
											tokens.add(new Token(Kind.IDENTIFIER,tokenStart, pos-tokenStart+1,line, posInLine-(pos-tokenStart)));
										pos++;
										posInLine++;
										state = State.START;
									}
								}
							else {
								throw new LexicalException("character is illegal",pos);
							}
						}
						break;
					}
				}
				break;
				
				case STRING_LIT:{
					
					if(chars[pos+1] == EOFchar )
					{
							if(chars[pos] != '\"')
							{
								pos++;
								posInLine++;
								throw new LexicalException("illegal character",pos);
							}
							pos++;
							posInLine++;
							tokens.add(new Token(Kind.STRING_LITERAL,tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
							state = State.START;
						
					}
					
					else {
						switch(ch) {
						case '\\':{
							if(chars[pos+1] == 'n' || chars[pos+1] == 'r' || chars[pos+1] == 'b' || chars[pos+1] == 't' || chars[pos+1]=='f' 
									|| chars[pos+1] == '\"' || chars[pos+1] == '\'' || chars[pos+1] == '\\')
							{
								pos+=2;
								posInLine+=2;
							}
							else if(chars[pos+1] != '\n' || chars[pos+1] != '\r' || chars[pos+1] != 'b' || chars[pos+1] != 't' || chars[pos+1]!='f' 
									|| chars[pos+1] != '\"' || chars[pos+1] != '\'' || chars[pos+1] != '\\')
							{
								pos++;
								posInLine++;
								throw new LexicalException("illegal character",pos);
							}
						}
						break;
						
						case '\n':{
							if(chars[pos+1] == '\r')
							{	
								pos+=2;
								posInLine+=2;
							}
							else{
								throw new LexicalException("illegal character",pos);
							}
							throw new LexicalException("illegal character",pos);
						}
						
						case '\r':{
							if(chars[pos+1] == '\n')
							{	pos+=2;
								posInLine+=2;
							}
						else{
							throw new LexicalException("illegal character",pos);
						}
						throw new LexicalException("illegal character",pos);
						}
						
						case '\"':
						{
							pos++;
							posInLine++;
							tokens.add(new Token(Kind.STRING_LITERAL,tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
							state = State.START;
						}
						break;
						
						default:{
							if(chars[pos] != EOFchar)
							{
								pos++;
								posInLine++;
							}
						}
					}
						
				}
				}
				break;
				
				case AFTER_DIV:{
					switch(ch) {
					case '/':{
						if(state == State.AFTER_DIV)
						{
							state=State.COMMENT;
							pos++;
							posInLine++;
						}
					}
					break;
					}
				}
				break;
				
				case COMMENT:{
					switch(ch) {
					case '\n':{
						state = State.START;
					}
					break;
					
					case '\r' : {
						state = State.START;
					}
					break;
					
					default:{
						pos++;
						posInLine++;
					}
					break;
					}
				}
				break;
				
				case AFTER_GT:{
					switch(ch) {
					case '=':{
						if(state == State.AFTER_GT)
						{
							tokens.add(new Token(Kind.OP_GE,tokenStart,2,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
							state = State.START;
						}
						else
						{
							throw new LexicalException("Illegal character after greater _than sign", pos);
						}
					}
					break;
					}
				}
				break;
				
				case AFTER_MINUS:{
					switch(ch){
						case '>':{
							if(state == State.AFTER_MINUS)
							{
								tokens.add(new Token(Kind.OP_RARROW,tokenStart,2,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
								state = State.START;
							}
							else
							{
								throw new LexicalException("Illegal character after minus sign",pos);
							}
						}
						break;
					}
				}
				break;
				
				case AFTER_LT:{
					switch(ch) {
						case '=':{
								tokens.add(new Token(Kind.OP_LE,tokenStart,2,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
								state = State.START;
						}
						break;
						
						case '-':{
								tokens.add(new Token(Kind.OP_LARROW,tokenStart,2,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
								state = State.START;
						}
						break;
						
						default:{
							if(state == State.AFTER_LT)
							{
								throw new LexicalException("Illegal character afer less_than",pos);

							}
						}
						break;
					}
				}
				break;
				
				case NOT_EQ:{
					switch(ch){
					case '=':{
						if(state == State.NOT_EQ)
						{
							tokens.add(new Token(Kind.OP_NEQ,tokenStart,2,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
							state = State.START;

						}
						else
						{
							throw new LexicalException("Illegal char after not_eq",pos);
						}
					}
					break;
					}
				}
				break;
				
				case AFTER_MUL:{
					switch(ch) {
					case '*':{
						if(state == State.AFTER_MUL)
						{
							tokens.add(new Token(Kind.OP_POWER,tokenStart,2,line,posInLine-(pos-tokenStart)));
							pos++;
							posInLine++;
							state = State.START;
						}
						else
						{
							throw new LexicalException("Illegal character",pos);
						}
					}
					break;
					}
				}
				break;
			
				case AFTER_EQ:{
					{
						switch(ch) {
						case '=':{
							if(state == State.AFTER_EQ)
							{
								tokens.add(new Token(Kind.OP_EQ,tokenStart,2,line,posInLine-(pos-tokenStart)));
								pos++;
								posInLine++;
								state = State.START;
							}
							else
							{
								throw new LexicalException("Illegal character '=' must be assignment or equal",pos);
							}
						}
						break;
						/*default:{
							tokens.add(new Token(Kind.OP_ASSIGN,tokenStart,1,line,posInLine-(pos-tokenStart)));
							//pos++;
							state = State.START;
						}*/
						}
					}
				}
				break;
			
			case IS_DIGIT:{
				if(Character.isDigit(ch) && chars[pos]!= EOFchar)
				{
					int num;
					pos++;	
					posInLine++;
					
				}
				
				else {
					int num;
					String stringNum = new String(chars, tokenStart, pos-tokenStart);
					try {
						num = Integer.parseInt(stringNum);
					}
					catch(NumberFormatException ex)
					{
						throw new LexicalException("Illegal string format of number at:",tokenStart);
					}
					if(num <= Integer.MAX_VALUE && num >= Integer.MIN_VALUE)
					{
						tokens.add(new Token(Kind.INTEGER_LITERAL,tokenStart, pos-tokenStart, line, posInLine-(pos-tokenStart)));
						//pos++;
					}
					else
					{
						throw new LexicalException("Illegal number, overflows bounds at:", tokenStart);
					}
					state = State.START;
				}
				
				 if(chars[pos]==EOFchar)
				{
					String stringNum = new String(chars, tokenStart, pos-tokenStart);
					//check if string is a valid number
					int num =0;
					try {
					num = Integer.parseInt(stringNum);
					}
					catch(NumberFormatException ex)
					{
						throw new LexicalException("Illegal string format of number", tokenStart);
					}
					//check for number overflow
					if(num <= Integer.MAX_VALUE || num >= Integer.MIN_VALUE)
					{
						tokens.add(new Token(Kind.INTEGER_LITERAL,tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
						//pos++;
						state = State.START;
					}
					else
						throw new LexicalException("Illegal number, overflows bounds at:",tokenStart);
				}
				
			}
			break;
			case IDENTIFIER:{
				String ident;
				if(Character.isJavaIdentifierPart(ch) && chars[pos] != EOFchar)
				{
					pos++;
					posInLine++;
				}
					else
					{
						ident = new String(chars, tokenStart, pos - tokenStart);
						if(ident.equals("true")||ident.equals("false"))
						{
							tokens.add(new Token(Kind.BOOLEAN_LITERAL,tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
						}
						else if(keywords.containsKey(ident))
							tokens.add(new Token(keywords.get(ident), tokenStart, pos-tokenStart,line, posInLine-(pos-tokenStart)));
						else if(opKeywords.containsKey(ident))
							tokens.add(new Token(opKeywords.get(ident),tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
						else if(separator.containsKey(ident))
							tokens.add(new Token(separator.get(ident),tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
						else
							tokens.add(new Token(Kind.IDENTIFIER,tokenStart, pos-tokenStart,line, posInLine-(pos-tokenStart)));
						//pos++; 
						//posInLine++;
						state = State.START;
					}
			
			if(chars[pos]==EOFchar)
			{
				ident = new String(chars, tokenStart, pos-tokenStart);
				ident.toString();
				if(ident.equals("true")||ident.equals("false"))
				{
					tokens.add(new Token(Kind.BOOLEAN_LITERAL,tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
				}
				else if(keywords.containsKey(ident))
					tokens.add(new Token(keywords.get(ident), tokenStart, pos-tokenStart,line, posInLine-(pos-tokenStart)));
				else if(opKeywords.containsKey(ident))
					tokens.add(new Token(opKeywords.get(ident),tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
				else if(separator.containsKey(ident))
					tokens.add(new Token(separator.get(ident),tokenStart,pos-tokenStart,line,posInLine-(pos-tokenStart)));
				else
					tokens.add(new Token(Kind.IDENTIFIER,tokenStart, pos-tokenStart,line, posInLine-(pos-tokenStart)));
				state = State.START;
				
			}
			}
			break;
			}
			
		}
		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		return this;

	}
	
	HashMap<String,Kind> keywords = new HashMap<String,Kind>();
	HashMap<String,Kind> opKeywords = new HashMap<String, Kind>();
	HashMap<String, Kind> separator = new HashMap<String,Kind>();

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}
	

}
