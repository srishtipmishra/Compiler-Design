package cop5556fa17;
import cop5556fa17.AST.*;

import java.net.URL;
import java.util.HashMap;

import cop5556fa17.Scanner.Kind;

//import com.sun.javafx.fxml.expression.Expression;

import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
//import sun.security.provider.JavaKeyStore.CaseExactJKS;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	
	public HashMap<String, Declaration> symTable  = new HashMap<String,Declaration>();
		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;
			
		
			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}	

		}		
		
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		Expression node=null;
		String decname= declaration_Variable.name;
		if(declaration_Variable.e!= null)
		{
			node = declaration_Variable.e;
			node.visit(this, arg);
		}
		
		if(symTable.get(decname) != null)
		{
			throw new SemanticException(declaration_Variable.firstToken," message");
		}
		else 
		{
			symTable.put(decname, declaration_Variable);
			declaration_Variable.myType = TypeUtils.getType(declaration_Variable.firstToken);
		}
		if(node != null&&(!(declaration_Variable.myType==node.myType)))
		{
			throw new SemanticException(declaration_Variable.firstToken," message");
		}
		else 
		{
			return null;
		}
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Expression e0=null;
		Expression e1=null;
		if(expression_Binary.e0 != null)
		{
			e0=  expression_Binary.e0;
			e0.visit(this, arg);
		}
		Kind expOp = expression_Binary.op;
		
		if(expression_Binary.e1 != null)
		{
			e1=  expression_Binary.e1;
			e1.visit(this, arg);
		}
		 //e1 = expression_Binary.e1;
		//e1.visit(this, arg);
		
		if((e0.myType != e1.myType && expression_Binary.myType == null))
		{
			throw new SemanticException(expression_Binary.firstToken, "invalid type");
		}
		switch(expOp)
		{
			case OP_EQ:
			case OP_NEQ:
			{
				expression_Binary.myType = Type.BOOLEAN;
				break;
			}
		
			
			case OP_GE:
			case OP_GT:
			case OP_LE:
			case OP_LT:
			{
				if(e0.myType == Type.INTEGER )
				{
					expression_Binary.myType = Type.BOOLEAN;
					break;
				}
			}
			
			case OP_AND:
			case OP_OR:
			{
				if(e0.myType == Type.BOOLEAN || e0.myType == Type.INTEGER)
				{
					expression_Binary.myType = e0.myType;
					break;
				}
			}
			
			case OP_DIV:
			case OP_MOD:
			case OP_MINUS:
			case OP_PLUS:
			case OP_TIMES:
			case OP_POWER:
			{
				if(e0.myType == Type.INTEGER)
				{
					expression_Binary.myType = Type.INTEGER;
					break;
				}
			}
			
			default:
			{
				expression_Binary.myType = null;
			}
			break;
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		/*Expression_Unary.Type <=
		let t = Expression.Type in 
                                   if op ∈ {EXCL} && (t == BOOLEAN || t == INTEGER) then t
                                  else if op {PLUS, MINUS} && t == INTEGER then INTEGER
		    else Ʇ
                              REQUIRE:  Expression_ Unary.Type ≠ Ʇ   
*/
		Expression node =  null;
		if(expression_Unary.e != null) {
			node = expression_Unary.e;
			node.visit(this, arg);
		}
		Type tempType = node.myType;
		if(expression_Unary.op == Kind.OP_EXCL &&(tempType == Type.BOOLEAN || tempType == Type.INTEGER))
		{
			expression_Unary.myType = tempType;
		}
		else if((expression_Unary.op == Kind.OP_PLUS ||  expression_Unary.op == Kind.OP_MINUS) && (tempType ==  Type.INTEGER))
		{
			expression_Unary.myType = Type.INTEGER;
		}
		if(expression_Unary.myType == null)
			throw new SemanticException(expression_Unary.firstToken, "invalid type");
		//throw new UnsupportedOperationException();
		return null;

	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		/*REQUIRE: Expression0.Type == INTEGER &&  Kind.Type == INTEGER
	Index.isCartesian <= !(Expression0 == KW_r && Expression1 == KW_a)
 
*/
		Expression e0 = null,e1=null;
		e0 = index.e0;
		if(e0!=null)
		e0.visit(this, arg);
		
		e1 = index.e1;
		if(e1!=null)
		e1.visit(this, arg);
		
		if(e0.myType == Type.INTEGER && e1.myType == Type.INTEGER)
		{
			index.setCartesian(!(e0.firstToken.kind.equals(Kind.KW_r) && e1.firstToken.kind.equals(Kind.KW_a)));
		}
		else
			throw new SemanticException(index.firstToken, "invalid");
		return null;
			
				//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		/*name.Type <= SymbolTable.lookupType(name)
	Expression_PixelSelector.Type <=  if name.Type == IMAGE then INTEGER 
                                    else if Index == null then name.Type
                                    else  Ʇ
              REQUIRE:  Expression_PixelSelector.Type ≠ Ʇ
*/
		Index expIndex = null;
		String expname=  expression_PixelSelector.name; 
		if(expression_PixelSelector.index != null)
		{
			expIndex = expression_PixelSelector.index;
			expIndex.visit(this, arg);
		}
		Declaration tempType = symTable.get(expname);
		Type dext;
		if(tempType ==null)
		{
			dext= null;	
		}
		else
			dext=tempType.myType;
		if(dext == Type.IMAGE)
		{
			expression_PixelSelector.myType = Type.INTEGER;
		}
		else if(expIndex == null)
		{
			expression_PixelSelector.myType = dext;
		}
		else {
			expression_PixelSelector.myType = null;
		}
		if(expression_PixelSelector.myType != null)
		{
			
		return null;
		}
		throw new SemanticException(expression_PixelSelector.firstToken, "invalid type");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		/*REQUIRE:  Expressioncondition.Type == BOOLEAN &&  
                Expressiontrue.Type ==Expressionfalse.Type
Expression_Conditional.Type <= Expressiontrue.Type*/

		Expression expCond = expression_Conditional.condition;
		expCond.visit(this, arg);
		
		Expression expTrue = expression_Conditional.trueExpression;
		expTrue.visit(this, arg);
		
		Expression expFalse = expression_Conditional.falseExpression;
		expFalse.visit(this, arg);
		
		if(expCond.myType == Type.BOOLEAN && expTrue.myType == expFalse.myType)
		{
			expression_Conditional.myType = expTrue.myType;
		}
		
		else throw new SemanticException(expression_Conditional.firstToken,"invalid type");
		
		return null;
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		/*REQUIRE:  symbolTable.lookupType(name) = Ʇ
               symbolTable.insert(name, Declaration_Image)
	Declaration_Image.Type <= IMAGE   
*/
		Expression xsize = declaration_Image.xSize,ysize = declaration_Image.ySize;
		Source source = null;
		if(declaration_Image.source != null)
		{
			source = declaration_Image.source;
			source.visit(this, arg);
		}
		
		if(symTable.get(declaration_Image.name) != null)
		{
			throw new SemanticException(declaration_Image.firstToken, "invalid type");
					}
		symTable.put(declaration_Image.name, declaration_Image);
		declaration_Image.myType = Type.IMAGE;

		
		//throw new UnsupportedOperationException();
		
		if(xsize  != null  && ysize != null )
		{
			xsize.visit(this, arg);
			ysize.visit(this, arg);
		
		if((xsize.myType  != Type.INTEGER && ysize.myType != Type.INTEGER))
		{
			throw new SemanticException(declaration_Image.firstToken, "invalid type");
		}
		}
		else if( xsize != null&& ysize==null)
		{
			throw new SemanticException(declaration_Image.firstToken, "invalid type");	
		}
		return null;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Boolean check =CheckURL(source_StringLiteral.fileOrUrl) ;
		source_StringLiteral.myType = check== true ? Type.URL:Type.FILE;
		return null;
		//throw new UnsupportedOperationException();
	}
	
	public boolean CheckURL(String url)
	{
		try 
		{
			URL url2 = new URL(url);
			return true;
		}
		catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Expression cmdExp = source_CommandLineParam.paramNum;
		cmdExp.visit(this, arg);
		
		source_CommandLineParam.myType = null;
		if(source_CommandLineParam.paramNum.myType != Type.INTEGER)
		{
			throw new SemanticException(source_CommandLineParam.firstToken, "invalid type");
		}
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Declaration myTe= symTable.get(source_Ident.name);
		Type identtype=null;
		if(myTe ==null)
		{
			identtype=null;
		}
		else {
			identtype=myTe.myType;
		}
		//Type identtype =myTe
		source_Ident.myType = identtype;
				
		if(source_Ident.myType == Type.FILE || source_Ident.myType == Type.URL)
		{
			return null;
		}
		else throw new SemanticException(source_Ident.firstToken, "Invalid type");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		/*REQUIRE:  symbolTable.lookupType(name) = Ʇ
               symbolTable.insert(name, Declaration_SourceSink)
	Declaration_SourceSink.Type <= Type
               REQUIRE Source.Type == Declaration_SourceSink.Type
		 */
		Source decSource = declaration_SourceSink.source;
		decSource.visit(this, arg);
		if(symTable.get(declaration_SourceSink.name) == null)
		{
			symTable.put(declaration_SourceSink.name, declaration_SourceSink);
			declaration_SourceSink.myType = TypeUtils.getType(declaration_SourceSink.firstToken);
		}
		else{
			throw new SemanticException(declaration_SourceSink.firstToken, "invalid type");
		}
		
		if(decSource.myType != declaration_SourceSink.myType && decSource.myType != null)
		{
			throw new SemanticException(declaration_SourceSink.firstToken, "invalid type");
		}
			return null;
		
		
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		expression_IntLit.myType = Type.INTEGER;
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Expression exp = expression_FunctionAppWithExprArg.arg;
		exp.visit(this, arg);
		
		if(exp.myType == Type.INTEGER)
		{
			expression_FunctionAppWithExprArg.myType = Type.INTEGER;
		}
		else throw new SemanticException(expression_FunctionAppWithExprArg.firstToken,"invalid type");
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Index expIndex = expression_FunctionAppWithIndexArg.arg;
		expIndex.visit(this, arg);
		expression_FunctionAppWithIndexArg.myType = Type.INTEGER;
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.myType = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Sink stOut = statement_Out.sink;
		if(stOut != null)
		stOut.visit(this, arg);
		
		statement_Out.setDec(symTable.get(statement_Out.name));
		if(symTable.get(statement_Out.name) != null)
		{
			if((symTable.get(statement_Out.name).myType == Type.BOOLEAN ||
					symTable.get(statement_Out.name).myType ==Type.INTEGER)
					&& statement_Out.sink.myType == Type.SCREEN 
					|| (symTable.get(statement_Out.name).myType==Type.IMAGE && (statement_Out.sink.myType == Type.FILE 
					|| statement_Out.sink.myType == Type.SCREEN)))
			{
				return null;
			}
			else throw new SemanticException(statement_Out.firstToken, "invalid type");
		}
		else throw new SemanticException(statement_Out.firstToken, "invalid type");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Source stIn = statement_In.source;
		stIn.visit(this, arg);
		
		statement_In.setDec(symTable.get(statement_In.name));
			return null;
		
		
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		LHS lhsNode = null;
		Expression expNode =null;
		if(statement_Assign.lhs != null)
		{
			lhsNode = statement_Assign.lhs;
			lhsNode.visit(this, arg);
		}
		if(statement_Assign.e !=null)
		{
		expNode = statement_Assign.e;
		expNode.visit(this, arg);
		}
		
		if(lhsNode.myType == expNode.myType || (lhsNode.myType == Type.IMAGE  &&  expNode.myType == Type.INTEGER))
		{
			statement_Assign.setCartesian(lhsNode.isCartesian);
		}
		else
			throw new SemanticException(statement_Assign.firstToken , "invalid type");
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Index lhsIndex = null;
		if(lhs.index != null)
		{
			lhsIndex = lhs.index;
			lhsIndex.visit(this, arg);
		}
			lhs.myDec = (Declaration)symTable.get(lhs.name);
			if(lhs.myDec!=null)
				lhs.myType = lhs.myDec.myType;
			
			if(lhsIndex !=null)
				lhs.setCartesian(lhsIndex.isCartesian());
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_SCREEN.myType = Type.SCREEN;
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_Ident.myType = symTable.get(sink_Ident.name).myType;
		if(sink_Ident.myType == Type.FILE)
		{
			return null;
		}
		else throw new SemanticException(sink_Ident.firstToken, "invalid type");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.myType = Type.BOOLEAN;
		return null;
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		if(symTable.get(expression_Ident.name) != null)
		{
			expression_Ident.myType = symTable.get(expression_Ident.name).myType;
		}
		else {
			expression_Ident.myType=null;
		}
		//expression_Ident.myType = symTable.get(expression_Ident.name).myType;
		return null;
		//throw new UnsupportedOperationException();
	}

}
