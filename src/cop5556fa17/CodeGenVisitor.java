package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fVisitor;
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);	
		fVisitor =cw.visitField(ACC_STATIC,"x", "I", null, new Integer(0));
		fVisitor.visitEnd();
		// if GRADE, generates code to add string to log
		fVisitor=cw.visitField(ACC_STATIC,"y", "I", null, new Integer(0));
		fVisitor.visitEnd();
		fVisitor =cw.visitField(ACC_STATIC,"X", "I", null, new Integer(0));
		fVisitor.visitEnd();
		fVisitor =cw.visitField(ACC_STATIC,"Y", "I", null, new Integer(0));
		fVisitor.visitEnd();
		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		String decVar = declaration_Variable.name;
		switch(declaration_Variable.myType)
		{
		case BOOLEAN:
		{
			fVisitor = cw.visitField(ACC_STATIC, decVar, "Z", null, false);
		}
		break;
		case INTEGER:
		{
			fVisitor = cw.visitField(ACC_STATIC, decVar, "I", null, 0);
		}
		break;
		}
		fVisitor.visitEnd();
		Expression dexExprVar = declaration_Variable.e;
		if(dexExprVar != null)
		{
			dexExprVar.visit(this, arg);
			switch(declaration_Variable.myType)
			{
			case INTEGER: {
				mv.visitFieldInsn(PUTSTATIC, className, decVar, "I");
			}
			break;
			case BOOLEAN:
			{
				mv.visitFieldInsn(PUTSTATIC, className, decVar, "Z");
			}
			break;
			}
		}
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		
		Label beginJump= new Label(),endJump = new Label();
		Kind binKind = expression_Binary.op;
		if(binKind == Kind.OP_EQ || binKind==Kind.OP_NEQ)
		{
			if(expression_Binary.e0.myType.equals(Type.INTEGER) || expression_Binary.e0.myType.equals(Type.BOOLEAN))
			{
				if(binKind.equals(Kind.OP_EQ))
				{
					mv.visitJumpInsn(IF_ICMPEQ, beginJump);
					mv.visitLdcInsn(false);
				}
				else if(expression_Binary.op.equals(Kind.OP_NEQ)) 
				{
					mv.visitJumpInsn(IF_ICMPNE, beginJump);
					mv.visitLdcInsn(false);
				}
			}
			else
			{
				if(expression_Binary.op.equals(Kind.OP_EQ))
				{
					mv.visitJumpInsn(IF_ACMPEQ, beginJump);
					mv.visitLdcInsn(false);
				}
				else if(expression_Binary.op.equals(Kind.OP_NEQ))
				{
					mv.visitJumpInsn(IF_ACMPNE, beginJump);
					mv.visitLdcInsn(false);
				}
			}
		}
		else
		{
			switch(binKind)
			{
				case OP_GE: 
					{
						mv.visitJumpInsn(IF_ICMPGE, beginJump);
						mv.visitLdcInsn(false);	
					}
					break;
				case OP_GT:
				{
					mv.visitJumpInsn(IF_ICMPGT, beginJump);
					mv.visitLdcInsn(false);	
				}
				break;
				case OP_LT:
				{
					mv.visitJumpInsn(IF_ICMPLT, beginJump);
					mv.visitLdcInsn(false);	
				}
				break;
				case OP_LE:
				{
					mv.visitJumpInsn(IF_ICMPLE, beginJump);
					mv.visitLdcInsn(false);	
				}
				break;
				case OP_AND:
				{
					mv.visitInsn(IAND);
				}
				break;
				case OP_OR:
				{
					mv.visitInsn(IOR);
				}
				break;
				case OP_DIV:
				{
					mv.visitInsn(IDIV);
				}
				break;
				case OP_MINUS:
				{
					mv.visitInsn(ISUB);
				}
				break;
				case OP_MOD:
				{
					mv.visitInsn(IREM);
				}
				break;
				case OP_PLUS:
				{
					mv.visitInsn(IADD);
				}
				break;
				case OP_TIMES:
				{
					mv.visitInsn(IMUL);
				}
				break;
			}
		}
		mv.visitJumpInsn(GOTO, endJump);
		mv.visitLabel(beginJump);
		mv.visitLdcInsn(true);
		mv.visitLabel(endJump);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		expression_Unary.e.visit(this, null);
		if(expression_Unary.op.equals(Kind.OP_EXCL) && (expression_Unary.e.myType.equals(Type.BOOLEAN) || expression_Unary.e.myType.equals(Type.INTEGER)))
		{
			if(expression_Unary.e.myType.equals(Type.BOOLEAN))
			{
				Label condA = new Label();
				Label condB = new Label();
				
				mv.visitJumpInsn(IFEQ, condB);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, condA);
				
				mv.visitLabel(condB);
				mv.visitLdcInsn(true);
				
				mv.visitLabel(condA);
			}
			else
			{
				mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitInsn(IXOR);
			}
		}
		else if((expression_Unary.op.equals(Kind.OP_PLUS) || expression_Unary.op.equals(Kind.OP_MINUS)) && (expression_Unary.e.myType.equals(Type.INTEGER)))
		{
			if(expression_Unary.op.equals(Kind.OP_MINUS))
			{
				mv.visitInsn(INEG);
			}
		}
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(!index.isCartesian())
		{
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_x", RuntimeFunctions.cart_xSig,false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_y", RuntimeFunctions.cart_ySig,false);
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageDesc);
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getPixel", ImageSupport.getPixelSig,false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		expression_Conditional.condition.visit(this, arg);
		Label condA = new Label();
		Label condB = new Label();
		mv.visitJumpInsn(IFEQ, condB);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, condA);
		
		mv.visitLabel(condB);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitLabel(condA);
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		if(declaration_Image.myType == TypeUtils.Type.IMAGE)
		{
			fVisitor=cw.visitField(ACC_STATIC,declaration_Image.name, ImageSupport.ImageDesc, null, null);
			fVisitor.visitEnd();
			if(declaration_Image.source == null)
			{
				if(declaration_Image.xSize != null && declaration_Image.ySize != null)
				{
					declaration_Image.xSize.visit(this, arg);
					declaration_Image.ySize.visit(this, arg);					
				}
				else
				{
					mv.visitLdcInsn(256);
					mv.visitLdcInsn(256);
				}	
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"makeImage", ImageSupport.makeImageSig,false);
			}
			else 
			{
				
				declaration_Image.source.visit(this, arg);
				if(declaration_Image.xSize != null && declaration_Image.ySize != null)
				{
					declaration_Image.xSize.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					declaration_Image.ySize.visit(this, arg);	
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}
				else
				{
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ACONST_NULL);
				}
			
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"readImage", ImageSupport.readImageSig,false);
			}
		}
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		return null;
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);	
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, ImageSupport.StringDesc);
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		fVisitor=cw.visitField(ACC_STATIC,declaration_SourceSink.name, ImageSupport.StringDesc, null, null);
		fVisitor.visitEnd();
		if(declaration_SourceSink.source!=null)
		{
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC,className,declaration_SourceSink.name, ImageSupport.StringDesc);
		}
		
		return null;
	}
	
	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		//throw new UnsupportedOperationException();
		mv.visitLdcInsn(expression_IntLit.value);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		Scanner.Kind tempFunc = expression_FunctionAppWithExprArg.function;
		if(tempFunc == Kind.KW_log)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"log", RuntimeFunctions.logSig,false);
		}
		else if(tempFunc == Kind.KW_abs)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"abs", RuntimeFunctions.absSig,false);
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		
		Scanner.Kind tempFunc = expression_FunctionAppWithIndexArg.function;
		if(tempFunc == Kind.KW_polar_r)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"polar_r", RuntimeFunctions.polar_rSig,false);
		}
		else if(tempFunc == Kind.KW_cart_x)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_x", RuntimeFunctions.cart_xSig,false);
		}
		else if(tempFunc == Kind.KW_cart_y)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"cart_y", RuntimeFunctions.cart_ySig,false);
		}
		else if(tempFunc == Kind.KW_polar_a)
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"polar_r", RuntimeFunctions.polar_rSig,false);
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		//throw new UnsupportedOperationException();
		Scanner.Kind tempPre = expression_PredefinedName.kind;
	
		
		if(tempPre == Kind.KW_x)
		{
			mv.visitFieldInsn(GETSTATIC,className,"x","I");
		}
		else if(tempPre == Kind.KW_y)
		{	
			mv.visitFieldInsn(GETSTATIC,className,"y","I");
		}
		else if(tempPre == Kind.KW_DEF_X)
		{
			mv.visitLdcInsn(256);
		}
		else if(tempPre == Kind.KW_DEF_Y)
		{
			mv.visitLdcInsn(256);
		}
		else if(tempPre == Kind.KW_a)
		{	mv.visitFieldInsn(GETSTATIC,className,"x","I");
			mv.visitFieldInsn(GETSTATIC,className,"y","I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"polar_a", RuntimeFunctions.polar_aSig,false);
		}
		else if(tempPre == Kind.KW_r)
		{	mv.visitFieldInsn(GETSTATIC,className,"x","I");
			mv.visitFieldInsn(GETSTATIC,className,"y","I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"polar_r", RuntimeFunctions.polar_rSig,false);
		}
		else if(tempPre == Kind.KW_X)
		{
			mv.visitFieldInsn(GETSTATIC,className,"X","I");
			
		}
		else if(tempPre == Kind.KW_Y)
		{
			mv.visitFieldInsn(GETSTATIC,className,"Y","I");
		}
		
		else if( tempPre == Kind.KW_Z)
		{
			mv.visitLdcInsn(16777215);
		}
		
		return null;	
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		//throw new UnsupportedOperationException();
		switch(statement_Out.getDec().myType)
		{
		case BOOLEAN:
		{
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().myType);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V",false);
		}
		break;
		case INTEGER:
		{
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().myType);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V",false);
		}
		break;
		case IMAGE:
		{
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().myType);			
			statement_Out.sink.visit(this, arg);
		}
		break;
		}

		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		//throw new UnsupportedOperationException();
		statement_In.source.visit(this, arg);
		switch(statement_In.getDec().myType)
		{
			case INTEGER:
			{
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
				mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
			}
			break;
			case BOOLEAN:
			{
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
				mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
			}
			break;
			case IMAGE:
			{
					Declaration_Image declaration_Image = (Declaration_Image) statement_In.getDec();
					if(declaration_Image.xSize != null && declaration_Image.ySize != null)
					{
						mv.visitFieldInsn(GETSTATIC,className,statement_In.name, ImageSupport.ImageDesc);
						mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getX", ImageSupport.getXSig,false);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
						mv.visitFieldInsn(GETSTATIC,className,statement_In.name, ImageSupport.ImageDesc);
						mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getY", ImageSupport.getYSig,false);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					}
					else
					{
						mv.visitInsn(ACONST_NULL);
						mv.visitInsn(ACONST_NULL);
					}
					mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"readImage", ImageSupport.readImageSig,false);
					mv.visitFieldInsn(PUTSTATIC,className,statement_In.name, ImageSupport.ImageDesc);
					
			}
			break;
		}
		return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		switch(lhs.myType)
		{
		case BOOLEAN: 
		{
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
		}
		break;
		case INTEGER:
		{
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
		}
		break;
		case IMAGE:
		{			
			mv.visitFieldInsn(GETSTATIC,className,lhs.name,ImageSupport.ImageDesc);
			mv.visitFieldInsn(GETSTATIC,className,"x","I");
			mv.visitFieldInsn(GETSTATIC,className,"y","I");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"setPixel", ImageSupport.setPixelSig,false);

		}
		break;
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		//throw new UnsupportedOperationException();
		mv.visitMethodInsn(INVOKESTATIC, ImageFrame.className,"makeFrame", ImageSupport.makeFrameSig,false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		//throw new UnsupportedOperationException();
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"write",ImageSupport.writeSig,false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		//throw new UnsupportedOperationException();
		mv.visitLdcInsn(expression_BooleanLit.value);
		return null;
		
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		//throw new UnsupportedOperationException();
		switch(expression_Ident.myType)
		{
			case BOOLEAN: 
			{
				mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
			}
			break;
			case INTEGER:
			{
				mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
			}
			break;
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.myType);
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		// TODO Auto-generated method stub
	if(statement_Assign.lhs.myType.equals(TypeUtils.Type.INTEGER)||statement_Assign.lhs.myType.equals(TypeUtils.Type.BOOLEAN))
	{
		statement_Assign.e.visit(this, arg);
		statement_Assign.lhs.visit(this, arg);
	}
		else if(statement_Assign.lhs.myType.equals(TypeUtils.Type.IMAGE))
		{
			
			
			mv.visitFieldInsn(GETSTATIC,className,statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitInsn(DUP);
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getX", ImageSupport.getXSig,false);
			mv.visitFieldInsn(PUTSTATIC, className,"X", "I");
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getY", ImageSupport.getYSig,false);
			mv.visitFieldInsn(PUTSTATIC, className,"Y", "I");
			
			Label start1 =  new Label();
			Label end1 =  new Label();
			Label start2=  new Label();
			Label end2=  new Label();
			
			mv.visitLdcInsn(0);
			
			startOuterloop(statement_Assign, start2);
			mv.visitJumpInsn(IF_ICMPEQ, end2);
			mv.visitLdcInsn(0);
			startInnerLoop(statement_Assign, start1, end1);		
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);	
			incrementInnerandOuterLoop(start1, end1);
			mv.visitJumpInsn(GOTO, start2);
			mv.visitLabel(end2);
			mv.visitInsn(POP);
			
		}
		return null;
	}

	private void incrementInnerandOuterLoop(Label innerstart, Label innerend) {
		mv.visitJumpInsn(GOTO, innerstart);
		
		mv.visitLabel(innerend);
		mv.visitInsn(POP);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IADD);
	}

	private void startInnerLoop(Statement_Assign statement_Assign, Label innerstart, Label innerend) {
		mv.visitLabel(innerstart);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		mv.visitFieldInsn(PUTSTATIC,className,"y", "I");
		mv.visitFieldInsn(GETSTATIC,className,statement_Assign.lhs.name, ImageSupport.ImageDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getY", ImageSupport.getYSig,false);
		mv.visitJumpInsn(IF_ICMPEQ, innerend);
	}

	private void startOuterloop(Statement_Assign statement_Assign, Label Outerstart) {
		mv.visitLabel(Outerstart);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		mv.visitFieldInsn(PUTSTATIC,className,"x", "I");
		mv.visitFieldInsn(GETSTATIC,className,statement_Assign.lhs.name, ImageSupport.ImageDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getX", ImageSupport.getXSig,false);
	}

}
