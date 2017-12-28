package cop5556fa17;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.*;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	Scanner scanner;
	Token t;
	PredictSets psGrammar;

	Parser(Scanner scanner) {
		psGrammar = new PredictSets();
		this.scanner = scanner;
		if(scanner.hasTokens())
			t = scanner.nextToken();
		
	}
	
	private Token consume() throws SyntaxException {
		Token tmp = t; //System.out.println("consuming " + t.kind);
		t = scanner.nextToken();
		return tmp;
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p =program();
		matchEOF();
		//psGrammar = new PredictSets();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		//IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
		ArrayList<ASTNode> astList = new ArrayList<ASTNode>();
		boolean flag = true;
		Token firstToken = t;
		if(t.kind == IDENTIFIER)
		{
			consume();
			while(flag)
			{
				if(psGrammar.Declaration.contains(t.kind))
				{
					astList.add(declaration());
					
					if(t.kind == Scanner.Kind.SEMI)
						consume();
					else throw new SyntaxException(t, "missing Semi");
				}
				else if(psGrammar.Statement.contains(t.kind))
				{
					astList.add(statement());
					//astList.add(s);
					if(t.kind == Scanner.Kind.SEMI)
						consume();
					else throw new SyntaxException(t, "missing semi");
				}
				else flag = false;
			}
		}
		else throw new SyntaxException(t, "missing ident");	
		return new Program(firstToken,firstToken, astList);
		//matchEOF();
		}
		

	Statement statement() throws SyntaxException{
		// TODO Auto-generated method stub
		//AssignmentStatement | ImageOutStatement    | ImageInStatement    
		Token firstToken = t;
		Statement s1;
		Token next;
		Expression_Ident exIdent;
		Statement_Out stOut;
		Statement_In stIn;
		Statement_Assign stAsn;
		boolean flag=true;
		if(t.kind == Scanner.Kind.IDENTIFIER)
		{
			next = scanner.peek();
			if(next.kind == Scanner.Kind.OP_LARROW)
			{
				consume();
				stIn = imageIn(firstToken);
				flag = false;
				return stIn;
			}
			else if(next.kind == Scanner.Kind.OP_RARROW)
			{
				consume();
				stOut = imageOut(firstToken);
				flag=false;
				return stOut;
			}
			else
			{
				stAsn = assignment();
				return stAsn;
			}
		}
		else
			throw new SyntaxException(t,"Illegal token");	
	}

	Statement_Out imageOut(Token tk) throws SyntaxException{
		// TODO Auto-generated method stub
		//IDENTIFIER OP_RARROW Sink 
		 Statement_Out stOut;
		 Token firstToken = t,tname;
		 Sink s1;
		
			if(t.kind == Scanner.Kind.OP_RARROW)
			{
				tname = t;
				consume();
				if(psGrammar.Sink.contains(t.kind))
				{
					s1 = sink();
					stOut = new Statement_Out(tk, tk,s1);
				}
				else
					throw new SyntaxException(t,"missing sink");
			}
			else throw new SyntaxException(t, "missing OP_Rarraw");
		return stOut;
	}

	Sink sink() throws SyntaxException{
		// TODO Auto-generated method stub
		//IDENTIFIER | KW_SCREEN  //ident must be file
		//todo : check for file ident
		Token firstToken = t;
		Sink_Ident s1=null;
		Sink_SCREEN s2 = null;
		Sink s3;
		if(t.kind == Scanner.Kind.IDENTIFIER)
		{
			s1 = new Sink_Ident(firstToken,t);
			consume();
			return s1;
		}
		else if(t.kind == Scanner.Kind.KW_SCREEN)
		{
			s2 = new Sink_SCREEN(firstToken);
			consume();
			return s2;
		}
		else
			throw new SyntaxException(t, "missing sink");
	}

	Statement_In imageIn(Token tk) throws SyntaxException{
		// TODO Auto-generated method stub
		//IDENTIFIER OP_LARROW Source
		Token firstToken = t,tname;
		Statement_In stIn;
		Source s1;
			if(t.kind == Scanner.Kind.OP_LARROW)
			{
				consume();
				if(psGrammar.Source.contains(t.kind))
				{
					s1 = source();
					stIn = new Statement_In(tk,tk, s1);
				}
				else throw new SyntaxException(t,"missing source");
			}
			else throw new SyntaxException(t, "mising Larraw");
	
		return stIn;
	}

	Statement_Assign assignment() throws SyntaxException{
		// TODO Auto-generated method stub
		//Lhs OP_ASSIGN Expression
		Token firstToken = t;
		LHS l1;
		Statement_Assign stAssign;
		Expression e1;
		if(psGrammar.Lhs.contains(t.kind))
		{
			l1 = lhs();
			if(t.kind == Scanner.Kind.OP_ASSIGN)
			{
				consume();
				if(psGrammar.Expression.contains(t.kind))
				{
					e1 = expression();
					stAssign = new Statement_Assign(firstToken,l1,e1);
				}
				else
					throw new SyntaxException(t, "expression missing");
			}
			else
				throw new SyntaxException(t, "OP_ASSIGN missing");
		}
		else
			throw new SyntaxException(t, "lhs missing");
		return stAssign;
	}

	LHS lhs() throws SyntaxException{
		// TODO Auto-generated method stub
		//IDENTIFIER ( LSQUARE LhsSelector RSQUARE   | ε )
		LHS lhs;
		Token firstToken = t;
		Index lhsSel = null;
		if(t.kind == Scanner.Kind.IDENTIFIER)
		{
			consume();
			if(t.kind == Scanner.Kind.LSQUARE)
			{
				consume();
				if(psGrammar.LhsSelector.contains(t.kind))
				{
					lhsSel = lhsSelector();
					if(t.kind == Scanner.Kind.RSQUARE)
						consume();
					else throw new SyntaxException(firstToken, "no Rsquare");
				}
				else throw new SyntaxException(t, "missing lhsSelector");
			}
			else {
				return new LHS(firstToken,firstToken,null);
			}
		}
		else
			throw new SyntaxException(t, "missing identidier");
		lhs = new LHS(firstToken,firstToken,lhsSel);
		return lhs;
	}

	Index lhsSelector() throws SyntaxException{
		// TODO Auto-generated method stub
		// LSQUARE  ( XySelector  | RaSelector  )   RSQUARE
		Index Sel = null;
	
		Token firstToken = t;
		if(t.kind == Scanner.Kind.LSQUARE)
		{
			consume();
			if(psGrammar.XySelector.contains(t.kind) || psGrammar.RaSelector.contains(t.kind))
			{
				if(psGrammar.XySelector.contains(t.kind))
				{
					Sel = xySelector();
					//return Sel;
				}
				else if(psGrammar.RaSelector.contains(t.kind))
				{
					Sel = raSelector();
					//return raSel;
				}
				//else throw new SyntaxException(firstToken, "no selector");
				
				if(t.kind == Scanner.Kind.RSQUARE)
					consume();
				else
					throw new SyntaxException(t, "missing rsquare");
			}
			else
				throw new SyntaxException(t,"missing selector");	
		}
		else
			throw new SyntaxException(t,"missing Lsquare");	
		return Sel;
	}

	Index raSelector() throws SyntaxException{
		// TODO Auto-generated method stub
		//KW_r COMMA KW_A
		Token firstToken = t;
		Index i;
		Expression_PredefinedName kwr,kwa;
		if(t.kind == Scanner.Kind.KW_r)
		{
			kwr = new Expression_PredefinedName(firstToken,t.kind);
			consume();
			if(t.kind == Scanner.Kind.COMMA)
			{
				consume();
				if(t.kind == Scanner.Kind.KW_a)
				{
					kwa = new Expression_PredefinedName(firstToken,t.kind);
					consume();
					i = new Index(firstToken,kwr,kwa);
				}
				else throw new SyntaxException(t, "missing KW_r");
			}
			else throw new SyntaxException(t, "missing comma");
		}
		else throw new SyntaxException(t, "mising KW_A");
		return i;
	}

	Index xySelector() throws SyntaxException{
		// TODO Auto-generated method stub
		//KW_x COMMA KW_y
		Token firstToken = t;
		Index i;
		Expression_PredefinedName kwx,kwy;
		if(t.kind == Scanner.Kind.KW_x)
		{
			kwx = new Expression_PredefinedName(firstToken,t.kind);
			consume();
			if(t.kind == Scanner.Kind.COMMA)
			{
				consume();
				if(t.kind == Scanner.Kind.KW_y)
				{
					kwy = new Expression_PredefinedName(firstToken,t.kind);
					consume();
					i = new Index(firstToken,kwx,kwy);
				}
				else throw new SyntaxException(t, "missing KW_y");
			}
			else throw new SyntaxException(t, "mising Comma");
		}
		else throw new SyntaxException(t, "missing KW_x");
		return i;
	}
	
	Declaration declaration() throws SyntaxException {
		//VariableDeclaration     |    ImageDeclaration   |   SourceSinkDeclaration  
		//check this one
		Declaration d;
		Declaration_Variable d1;
		Declaration_Image d2;
		//Declaration_SourceSink ss;
		Declaration d3;
		if(psGrammar.VariableDec.contains(t.kind))
		{
			d1 = variableDec();
			return d1;
		}
		else if(psGrammar.ImageDec.contains(t.kind))
		{
			d2 = imageDeclaration();
			return d2;
		}
		else if(psGrammar.SourceSinkDec.contains(t.kind))
		{
			d3 = sourceSinkDec();
			return d3;	
		}
		else throw new SyntaxException(t,"missing declaration");
	}
	

	Declaration_SourceSink sourceSinkDec() throws SyntaxException{
		// TODO Auto-generated method stub
		//SourceSinkType IDENTIFIER  OP_ASSIGN  Source
		//check this for sourcesinktype
		Token firstToken = t,tname;
		Declaration_SourceSink dec;
		Source s1;
		if(psGrammar.SourceSinkType.contains(t.kind))
		{
			Token ttype = sourceSinkType();
			if(t.kind == Scanner.Kind.IDENTIFIER)
			{
				tname = t;
				consume();
				if(t.kind ==  Scanner.Kind.OP_ASSIGN)
				{
					consume();
					if(psGrammar.Source.contains(t.kind))
					{
						s1 = source();
						dec = new Declaration_SourceSink(firstToken,ttype,tname, s1);
						return dec;
					}
				}
				else
					throw new SyntaxException(t,"Assign missing");
			}
			throw new SyntaxException(t,"Identifier missing");
		}
		throw new SyntaxException(t,"Source type missing");
	}

	Source source() throws SyntaxException{
		// TODO Auto-generated method stub
		// STRING_LITERAL  |OP_AT Expression |IDENTIFIER  
		Token firstToken = t;
		Expression e1;
		Source_CommandLineParam s2;
		Source_StringLiteral soStLit;
		Source_Ident idt;
		if(t.kind == Scanner.Kind.STRING_LITERAL)
		{
			soStLit = new Source_StringLiteral(firstToken, t.getText());
			consume();
			return soStLit;
		}
		else if(t.kind == Scanner.Kind.OP_AT)
		{
			consume();
			if(psGrammar.Expression.contains(t.kind))
			{
				e1 = expression();
				s2 = new Source_CommandLineParam(firstToken,e1);
				return s2;
			}
			else throw new SyntaxException(t, "missing expression");
		}
		
		else if(t.kind == Scanner.Kind.IDENTIFIER)
		{
			idt = new Source_Ident(firstToken,t);
			consume();
			return idt;
		}
		
		else
			throw new SyntaxException(t, "missing source syntax");
	}

	Token sourceSinkType() throws SyntaxException{
		// TODO Auto-generated method stub
		//SourceSinkType KW_url | KW_file
		Token firstToken = t;
		Token kwurl = null,kwfile = null;
		if(t.kind == Scanner.Kind.KW_url)
		{
			kwurl = firstToken; 
			consume();
			return kwurl;
		}
			
		else if(t.kind == Scanner.Kind.KW_file)
		{
			kwfile = t;
			consume();
			return firstToken;
		}
		else throw new SyntaxException(t,"missing source sink type");
	}

	Declaration_Image imageDeclaration() throws SyntaxException{
		// TODO Auto-generated method stub 
		//KW_image  (LSQUARE Expression COMMA Expression RSQUARE | ε) IDENTIFIER ( OP_LARROW Source | ε ) 
		Token firstToken = t,tname;
		Expression e1 = null ,e2 =null;
		Declaration_Image img = null;
		Source s1=null;
		if(t.kind == Scanner.Kind.KW_image)
		{
			consume();
			if(t.kind == Scanner.Kind.LSQUARE)
			{
				consume();
				if(psGrammar.Expression.contains(t.kind))
				{
					e1 = expression();
					if(t.kind == Scanner.Kind.COMMA)
					{
						consume();
						if(psGrammar.Expression.contains(t.kind))
						{
							e2 = expression();
							if(t.kind == Scanner.Kind.RSQUARE)
							{
								consume();
							}
							else throw new SyntaxException(t, "missing rsquare");
						}
						else
							throw new SyntaxException(t, "missing Expression");
					}
					else
						throw new SyntaxException(t, "missing Comma");
				}
				else
					throw new SyntaxException(t, "missing Expression");
			}
			Token z= scanner.peek();
			if(t.kind == Scanner.Kind.IDENTIFIER)
			{
				
				//if(z.kind.equals(OP_LARROW))
				//{
				tname = t;
				consume();
				if(t.kind == Scanner.Kind.OP_LARROW)
				{
					
					consume();
					if(psGrammar.Source.contains(t.kind))
					{
						s1 = source();
						img = new Declaration_Image(firstToken,e1,e2,tname,s1);
					}
					else throw new SyntaxException(t,"missing source");
				}
				else
					return new Declaration_Image(firstToken,e1,e2,tname,s1);
			}
			else throw new SyntaxException(t,"missing identifier");
			//}
			//else
			//	return new Declaration_Image(tname, e1, e2, z.name, null);
		}
		else
			throw new SyntaxException(t, "missing KW_image");
		return img;
	}

	Declaration_Variable variableDec() throws SyntaxException{
		// TODO Auto-generated method stub
		//VarType IDENTIFIER  (  OP_ASSIGN  Expression  | ε )
		Token firstToken = t;
		Declaration_Variable v2 = null;
		Expression e1;
		Token tname;
		Token v1;
		if(psGrammar.VariableType.contains(t.kind)) 
		{
			//v1 = varType();
			consume();
			if(t.kind == Scanner.Kind.IDENTIFIER)
			{
				tname = t; 
				consume();
				if(t.kind == Scanner.Kind.OP_ASSIGN)
				{
					consume();
					if(psGrammar.Expression.contains(t.kind))
					{
						e1 = expression();
						v2 = new Declaration_Variable(firstToken,firstToken,tname,e1);
						return v2;
					}
					else throw new SyntaxException(t, "missing exp");
				}
				else
					return new Declaration_Variable(firstToken,firstToken,tname,null);
			}
			else throw new SyntaxException(t, "missing identfier");
		}
		throw new SyntaxException(t, "missing vartype"); 
	}

	Token varType() throws SyntaxException{
		// TODO Auto-generated method stub
		//KW_int | KW_boolean
		Token firstToken = t;
		Token kw;
		if(t.kind == Scanner.Kind.KW_int)
		{
			consume();
			return firstToken;
		}
		else if(t.kind == Scanner.Kind.KW_boolean)
		{
			consume();
			return firstToken;
		}
		else
			throw new SyntaxException(t, "missing varType");
	}

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	
	Expression expression() throws SyntaxException {
		//TODO implement this.
			//OrExpression  OP_Q  Expression OP_COLON Expression| OrExpression
		Token firstToken = t;
		 //Expression_Binary e1;
		 Expression e2 = null,e1 = null,e3=null,e=null;
		 Expression_Conditional exp=null;
			if(psGrammar.OrExpression.contains(t.kind))
			{
				e1 = orExpression();
				if(t.kind == Scanner.Kind.OP_Q)
				{
					consume();
					if(psGrammar.Expression.contains(t.kind))
					{
						e2 = expression();
						if(t.kind == Scanner.Kind.OP_COLON) 
						{
							consume();
							if(psGrammar.Expression.contains(t.kind))
							{
								e3 = expression();
								exp = new Expression_Conditional(firstToken,e1,e2,e3);
								return exp;
							}
							else throw new SyntaxException(t, "missing expression/orexpression");
						}
						else throw new SyntaxException(t, "missing op_colon");
					}
					else throw new SyntaxException(t, "misiing expression");
				}
				else
					return e1;
			}
			else throw new SyntaxException(t, "missing ORexpression");
			
	 }

	Expression orExpression() throws SyntaxException{
		// TODO Auto-generated method stub
		//AndExpression   (  OP_OR  AndExpression)*
		//ArrayList<AndExpression> andList  = new ArrayList<AndExpression>();
		Token firstToken = t,op=null;
		Expression e0 = null,e1 =null;
		boolean flag = true;
		if(psGrammar.AndExpression.contains(t.kind))
		{
			e0 = andExpression();
			while(flag)
			{
				if(t.kind == Scanner.Kind.OP_OR)
				{
					op = t;
					consume();
					if(psGrammar.AndExpression.contains(t.kind))
					{
						e1 = andExpression();	
						e0 = new Expression_Binary(firstToken,e0,op,e1);
					}
					else throw new SyntaxException(t, "missing andexpression");
				}
				else 
					flag = false;				
			}
			if(e1 != null) return e0;
			return e0;
		}
		else throw new SyntaxException(t, "missing and expression");
	}
	
	Expression andExpression() throws SyntaxException{
		//EqExpression ( OP_AND  EqExpression )*
		
		//ArrayList<EqExpression> eqList = new ArrayList<EqExpression>();
		Token firstToken = t,op=null;
		Expression e0,e1=null; 
		boolean flag = true;
		if(psGrammar.EqExpression.contains(t.kind))
		{
			e0 = eqExpression();
			while(flag)
			{
				if(t.kind == Scanner.Kind.OP_AND)
				{
					op = t;
					consume();
					if(psGrammar.EqExpression.contains(t.kind))
					{
						e1 = eqExpression();
						e0 = new Expression_Binary(firstToken,e0,op,e1);
					}
					else throw new SyntaxException(t, "missing eq exp");
				}
				else flag = false;
			}
			if(e1 != null) return e0;
			return e0;		}
		else throw new SyntaxException(t, "missing eqExp");
	}
	
	Expression unaryExpression() throws SyntaxException{
		//OP_PLUS UnaryExpression | OP_MINUS UnaryExpression | UnaryExpressionNotPlusMinus
		Token firstToken=t;
		Expression exp1,exp=null;
		Expression exp2;
		if(t.kind == Scanner.Kind.OP_PLUS)
		{
			Token op = t;
			consume();
			if(psGrammar.UnaryExpression.contains(t.kind))
			{
				exp1 = unaryExpression();
				exp = new Expression_Unary(firstToken,op,exp1);
			}
		}
		else if(t.kind == Scanner.Kind.OP_MINUS)
		{
			Token op = t;
			consume();
			if(psGrammar.UnaryExpression.contains(t.kind))
			{
				exp1 = unaryExpression();
				exp = new  Expression_Unary(firstToken,op,exp1);
			}
		}
		else if(psGrammar.UnaryNotPlusMinus.contains(t.kind))
		{
			exp = unaryNotPlusMinus();
			//return exp2;
		}
		else
			throw new SyntaxException(t,"missing unary exp");
		return exp;
	}

	Expression unaryNotPlusMinus() throws SyntaxException{
		// TODO Auto-generated method stub
		//OP_EXCL  UnaryExpression  | Primary | IdentOrPixelSelectorExpression 
		//| KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
		ArrayList<Kind> checkList = new ArrayList<Kind>();
		Collections.addAll(checkList, Scanner.Kind.KW_x, Scanner.Kind.KW_y, Scanner.Kind.KW_r, Scanner.Kind.KW_a,
				Scanner.Kind.KW_X, Scanner.Kind.KW_Y, Scanner.Kind.KW_Z, Scanner.Kind.KW_A, Scanner.Kind.KW_R,
				Scanner.Kind.KW_DEF_X, Scanner.Kind.KW_DEF_Y);
		Token firstToken= t;
		Expression exUr;
		Expression_Unary unaryNotPlus;
		Expression_PredefinedName preDef;
		Expression prim;
		if(t.kind == Scanner.Kind.OP_EXCL)
		{
			Token op = t; 
			consume();
			if(psGrammar.UnaryExpression.contains(t.kind))
			{
				exUr = unaryExpression();
				unaryNotPlus = new Expression_Unary(firstToken, op, exUr);
				return unaryNotPlus;
			}
		}
		else if(psGrammar.Primary.contains(t.kind))
		{
			prim = primary();
			return prim;
		}
		else if(psGrammar.IdentOrPix.contains(t.kind))
		{
			prim = identOrPix();
			return prim;
		}
		else if(checkList.contains(t.kind))
		{
			preDef = new Expression_PredefinedName(firstToken,t.kind);
			consume();
			return preDef;
		}
	    throw new SyntaxException(t, "missing op_exc orprimary or unary or checklist");
	}

	Expression identOrPix() throws SyntaxException{
		// TODO Auto-generated method stub
		//IDENTIFIER LSQUARE Selector RSQUARE   | IDENTIFIER
		Token firstToken = t,tname;
		Expression_PixelSelector exPixSel;
		Expression_Ident exIdent;
		Index sel;
		if(t.kind == Scanner.Kind.IDENTIFIER)
		{
			tname = t;
			consume();
			if(t.kind == Scanner.Kind.LSQUARE)
			{
				consume();
				if(psGrammar.Selector.contains(t.kind))
				{
					sel = selector();
					if(t.kind == Scanner.Kind.RSQUARE)
					{
						consume();
						exPixSel = new Expression_PixelSelector(firstToken,tname,sel);
						return exPixSel;
					}
					else throw new SyntaxException(t, "missing RSquare");
				}
				else throw new SyntaxException(t,"missing selector");
			}
			else
			{
				exIdent = new Expression_Ident(firstToken,tname);
				return exIdent;
			}
		}
		throw new SyntaxException(t,"missing ident");
	}

	Index selector() throws SyntaxException{
		// TODO Auto-generated method stub
		//Expression COMMA Expression  
		Token firstToken = t;
		Index sel;
		Expression e1, e2;
		if(psGrammar.Expression.contains(t.kind))
		{
			e1 = expression();
			if(t.kind == Scanner.Kind.COMMA)
			{
				consume();
				if(psGrammar.Expression.contains(t.kind))
				{
					e2 = expression();
					sel = new Index(firstToken,e1,e2);
				}
				else throw new SyntaxException(t, "missing expression");
			}
			else throw new SyntaxException(t, "missing comma");
		}
		else throw new SyntaxException(t, "missing expression");
		return sel;
	}

	Expression primary() throws SyntaxException{
		// TODO Auto-generated method stub
		// INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BoolLit
		Token firstToken = t;
		Expression_IntLit intLit;
		
		Expression e1,e2,prim;
		
		Expression_BooleanLit boolLit;
		if(t.kind == Scanner.Kind.INTEGER_LITERAL)
		{
			intLit = new Expression_IntLit(firstToken,t.intVal());
			consume();
			return intLit;
		}
		else if(t.kind == Scanner.Kind.LPAREN)
		{
			consume();
			if(psGrammar.Expression.contains(t.kind))
			{
				e1 = expression();
				if(t.kind == Scanner.Kind.RPAREN)
				{
					consume();
					return e1;
				}
				else throw new SyntaxException(t, "missing Rparen");
			}
			else throw new SyntaxException(t, "missing expression");
		}
		
		else if(psGrammar.FunctionApplication.contains(t.kind))
		{
			e2 = functionApplication();
			return e2;
		}
		else if(t.kind == Scanner.Kind.BOOLEAN_LITERAL)
		{
			boolLit = new Expression_BooleanLit(firstToken,t.getText().equals("true"));
			consume();
			return boolLit;
		}
		else throw new SyntaxException(t, "missing intlit or funApp or lparen" );
	}

	Expression functionApplication() throws SyntaxException{
		// TODO Auto-generated method stub
		//FunctionName LPAREN Expression RPAREN  | FunctionName  LSQUARE Selector RSQUARE 
		Expression funcName,exp,exp1;
		Token firstToken = t;
		Expression_FunctionAppWithExprArg arg1;
		Expression_FunctionAppWithIndexArg arg2;
		Index sel;
		if(psGrammar.FunctionName.contains(t.kind))
		{
			funcName = functionName();
			if(t.kind == Scanner.Kind.LPAREN)
			{
				consume();
				if(psGrammar.Expression.contains(t.kind))
				{
					exp1 = expression();
					if(t.kind == Scanner.Kind.RPAREN)
						consume();
					else throw new SyntaxException(t, "missing RParen");
				}
				else throw new SyntaxException(t, "missing expression");
				arg1 = new Expression_FunctionAppWithExprArg(firstToken, firstToken.kind, exp1);
				return arg1;
			}
			
			else if(t.kind == Scanner.Kind.LSQUARE)
			{
				consume();
				if(psGrammar.Selector.contains(t.kind))
				{
					sel = selector();
					if(t.kind == Scanner.Kind.RSQUARE)
						consume();
					else throw new SyntaxException(t, "missing RSquare");
				}
				else throw new SyntaxException(t, "missing selector");
				arg2 = new Expression_FunctionAppWithIndexArg(firstToken,firstToken.kind, sel);
				return arg2;
			}
		}
		throw new SyntaxException(t, "missing funcName");
	}
	
	Expression functionName() throws SyntaxException{
		//KW_sin | KW_cos | KW_atan | KW_abs 
		//| KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r
		Token firstToken = t;
		Expression_PredefinedName fnName;
		ArrayList<Kind> checkList = new ArrayList<Kind>();
		Collections.addAll(checkList, Scanner.Kind.KW_sin, Scanner.Kind.KW_cos, Scanner.Kind.KW_atan,
				Scanner.Kind.KW_abs, Scanner.Kind.KW_cart_x, Scanner.Kind.KW_cart_y, Scanner.Kind.KW_polar_a,
				Scanner.Kind.KW_polar_r);
		if(checkList.contains(t.kind))
		{
			fnName = new Expression_PredefinedName(firstToken,t.kind);
			consume();
		}
		else
			throw new SyntaxException(t, "missing function name");
		return fnName;
	}
	
	Expression multExpression() throws SyntaxException{
		//UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
		boolean flag = true;
		Token firstToken = t;
		Expression e0=null,e1=null;
		Token op = null;
		//Expression_Binary exBin;
		//ArrayList<Epression_Unary> unExList = new ArrayList<Epression_Unary>();
		if(psGrammar.UnaryExpression.contains(t.kind))
		{
			e0 = unaryExpression();
			while(flag) {
				if(t.kind == Scanner.Kind.OP_TIMES || t.kind == Scanner.Kind.OP_DIV || t.kind == Scanner.Kind.OP_MOD)
				{
					if(t.kind == Scanner.Kind.OP_TIMES)
					{
						op = t;
						consume();
					}
					else if(t.kind == Scanner.Kind.OP_DIV)
					{
						op = t;
						consume();
					}
					else if(t.kind == Scanner.Kind.OP_MOD)
					{
						op = t;
						consume();
					}
					if(psGrammar.UnaryExpression.contains(t.kind))
					{
						e1 = unaryExpression();
						e0 = new Expression_Binary(firstToken,e0,op,e1);
					}
					else throw new SyntaxException(t, "missing unary expression");
				}
				else
					flag = false;			
			}
			if(e1 != null) return e0;
			
			return e0;
		}
		throw new SyntaxException(t, "missing unary exp");

	}
	
	Expression addExpression() throws SyntaxException{
		//MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*
		boolean flag = true;
		Token firstToken = t;
		Expression e0,e1=null;
		Token op = null;
		ArrayList<Expression_Binary> multList = new ArrayList<Expression_Binary>();
		if(psGrammar.MulExpression.contains(t.kind))
		{
			e0 = multExpression();
			while(flag) 
			{
				if(t.kind == Scanner.Kind.OP_PLUS || t.kind == Scanner.Kind.OP_MINUS)
				{
					if(t.kind == Scanner.Kind.OP_PLUS)
					{
						op = t;
						consume();
					}
					else if(t.kind == Scanner.Kind.OP_MINUS)
					{
						op = t;
						consume();
					}
					if(psGrammar.MulExpression.contains(t.kind))
					{
						e1 = multExpression();
						e0 = new Expression_Binary(firstToken,e0,op,e1);
					}
					else throw new SyntaxException(t, "missing multexpression");
				}
				else flag = false;
			}
			
			if(e1 != null) return e0;
			return e0;
		}
		else throw new SyntaxException(t, "missing multexpression");
		
	}
	
	Expression relExpression() throws SyntaxException{
		//AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
		boolean flag= true;
		Token firstToken = t, op=null;
		Expression e0,e1=null;
		//ArrayList<Expression_Binary> addList = new ArrayList<Expression_Binary>();
		
		if(psGrammar.AddExpression.contains(t.kind))
		{
			e0 = addExpression();
			while(flag) 
			{
				if(t.kind == Scanner.Kind.OP_LT || t.kind == Scanner.Kind.OP_GT || 
						t.kind == Scanner.Kind.OP_LE || t.kind == Scanner.Kind.OP_GE)
				{
					if(t.kind == Scanner.Kind.OP_LT)
					{
						op = t;
						consume();
					}
					else if(t.kind == Scanner.Kind.OP_LE)
					{
						op = t;
						consume();
					}
					else if(t.kind == Scanner.Kind.OP_GT)
					{
						op = t;
						consume();
					}
					else if(t.kind == Scanner.Kind.OP_GE)
					{
						op = t;
						consume();
					}
					
					if(psGrammar.AddExpression.contains(t.kind))
					{
						e1 = addExpression();
						e0 = new Expression_Binary(firstToken,e0,op,e1);
					}
					else throw new SyntaxException(t, "missing add expression");
				}
				else flag = false;
			}
			if(e1 != null) return e0;
			return e0;
		}
		else throw new SyntaxException(t, "missing addexp");
		
	}
	
	Expression eqExpression() throws SyntaxException{
		//RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*
		boolean flag = true;
		Token firstToken = t,op=null;
		Expression e1 = null,e0;
		//ArrayList<Expression_Binary> relList = ArrayList<Expression_Binary>();
		if(psGrammar.RelExpression.contains(t.kind))
		{
			e0 = relExpression();
			while(flag)
			{
				if(t.kind == Scanner.Kind.OP_EQ || t.kind == Scanner.Kind.OP_NEQ)
				{
					if(t.kind == Scanner.Kind.OP_EQ)
					{
						op = t;
						consume();

					}
					else if(t.kind == Scanner.Kind.OP_NEQ)
					{
						op = t;
						consume();
					}
					if(psGrammar.RelExpression.contains(t.kind))
					{
						e1 = relExpression();
						e0 = new Expression_Binary(firstToken,e0,op,e1);
					}
					else throw new SyntaxException(t, "missing relexp");
				}
				else flag = false;
			}
			if(e1 != null) return e0;
			return e0;
		}
		else throw new SyntaxException(t, "missing relexp");
		
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
