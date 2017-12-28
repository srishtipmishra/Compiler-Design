package cop5556fa17;
import java.util.ArrayList;
import java.util.List;

import cop5556fa17.Scanner.Kind;

public class PredictSets{
	List<Kind> Selector = new ArrayList<Kind>();
	List<Kind> Declaration = new ArrayList<Kind>();
	List<Kind> VariableDec =  new ArrayList<Kind>();
	List<Kind> VariableType = new ArrayList<Kind>();
	List<Kind> SourceSinkDec = new ArrayList<Kind>();
	List<Kind> Source = new ArrayList<Kind>();
	List<Kind> SourceSinkType = new ArrayList<Kind>();
	List<Kind> ImageDec = new ArrayList<Kind>();
	List<Kind> Statement = new ArrayList<Kind>();
	List<Kind> Assignment = new ArrayList<Kind>();
	List<Kind> ImageIn = new ArrayList<Kind>();
	List<Kind> ImageOut = new ArrayList<Kind>();
	List<Kind> Sink = new ArrayList<Kind>();
	List<Kind> Expression = new ArrayList<Kind>();
	List<Kind> OrExpression = new ArrayList<Kind>();
	List<Kind> AndExpression = new ArrayList<Kind>();
	List<Kind> EqExpression = new ArrayList<Kind>();
	List<Kind> RelExpression = new ArrayList<Kind>();
	List<Kind> AddExpression = new ArrayList<Kind>();
	List<Kind> MulExpression =  new ArrayList<Kind>();
	List<Kind> UnaryExpression =  new ArrayList<Kind>();
	List<Kind> UnaryNotPlusMinus = new ArrayList<Kind>();
	List<Kind> Primary = new ArrayList<Kind>();
	List<Kind> IdentOrPix =  new ArrayList<Kind>();
	List<Kind> Lhs = new ArrayList<Kind>();
	List<Kind> FunctionApplication = new ArrayList<Kind>();
	List<Kind> FunctionName =  new ArrayList<Kind>();
	List<Kind> LhsSelector = new ArrayList<Kind>();
	List<Kind> XySelector = new ArrayList<Kind>();
	List<Kind> RaSelector =  new ArrayList<Kind>();
	List<Kind> Program = new ArrayList<Kind>();
	
	PredictSets(){
		PsFunctionName();
		PsFunctionApplication();
		PsRaSelector();
		PsXySelector();
		PsLhsSelector();
		PsLhs();
		PsIdentOrPix();
		PsPrimary();
		PsUnaryNotPlusMinus();
		PsUnary();
		PsMultExp();
		PsAddExp();
		PsRelExp();
		PsEqExp();
		PsAndExp();
		PsOrExp();
		PsExpression();
		PsSelector();
		PsAssignment();
		PsImageIn();
		PsSink();
		PsImageOut();
		PsStatement();
		PsImageDec();
		PsSourceSinkType();
		PsSource();
		PsSourceSinkDec();
		PsVariableType();
		PsVariableDec();
		PsDeclaration();
		PsProgram();		
	}
	
	private void PsFunctionName()
	{
		this.FunctionName.add(Kind.KW_sin);
		this.FunctionName.add(Kind.KW_atan);
		this.FunctionName.add(Kind.KW_cos);
		this.FunctionName.add(Kind.KW_abs);
		this.FunctionName.add(Kind.KW_cart_x);
		this.FunctionName.add(Kind.KW_cart_y);
		this.FunctionName.add(Kind.KW_polar_a);
		this.FunctionName.add(Kind.KW_polar_r);
	}
	
	private void PsFunctionApplication()
	{
		this.FunctionApplication.addAll(this.FunctionName);
	}
	
	private void PsRaSelector()
	{
		this.RaSelector.add(Kind.KW_r);
	}

	private void PsXySelector()
	{
		this.XySelector.add(Kind.KW_x);
	}

	private void PsLhsSelector()
	{
		this.LhsSelector.add(Kind.LSQUARE);
	}
	
	private void PsLhs()
	{
		this.Lhs.add(Kind.IDENTIFIER);
	}
	
	private void PsIdentOrPix()
	{
		this.IdentOrPix.add(Kind.IDENTIFIER);
	}
	
	private void PsUnaryNotPlusMinus()
	{
		this.UnaryNotPlusMinus.add(Kind.OP_EXCL);
		this.UnaryNotPlusMinus.addAll(this.Primary);
		this.UnaryNotPlusMinus.addAll(IdentOrPix);
		this.UnaryNotPlusMinus.add(Kind.IDENTIFIER);
		this.UnaryNotPlusMinus.add(Kind.KW_x);
		this.UnaryNotPlusMinus.add(Kind.KW_y);
		this.UnaryNotPlusMinus.add(Kind.KW_X);
		this.UnaryNotPlusMinus.add(Kind.KW_Y);
		this.UnaryNotPlusMinus.add(Kind.KW_Z);
		this.UnaryNotPlusMinus.add(Kind.KW_r);
		this.UnaryNotPlusMinus.add(Kind.KW_a);
		this.UnaryNotPlusMinus.add(Kind.KW_A);
		this.UnaryNotPlusMinus.add(Kind.KW_DEF_X);
		this.UnaryNotPlusMinus.add(Kind.KW_DEF_Y);
		this.UnaryNotPlusMinus.add(Kind.KW_R);
	}
	
	private void PsUnary()
	{
		this.UnaryExpression.add(Kind.OP_PLUS);
		this.UnaryExpression.add(Kind.OP_MINUS);
		this.UnaryExpression.addAll(this.UnaryNotPlusMinus);
	}
	
	private void PsPrimary()
	{
		//Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL

		this.Primary.add(Kind.INTEGER_LITERAL);
		this.Primary.add(Kind.LPAREN);
		this.Primary.addAll(this.FunctionApplication);
		this.Primary.add(Kind.BOOLEAN_LITERAL);
	}
	private void PsMultExp()
	{
		this.MulExpression.addAll(this.UnaryExpression);
	}

	private void PsAddExp()
	{
		this.AddExpression.addAll(this.MulExpression);
	}

	private void PsRelExp()
	{
		this.RelExpression.addAll(this.AddExpression);
	}

	private void PsEqExp()
	{
		this.EqExpression.addAll(this.RelExpression);
	}

	private void PsAndExp()
	{
		this.AndExpression.addAll(this.EqExpression);
	}

	private void PsOrExp()
	{
		this.OrExpression.addAll(this.AndExpression);
	}
	
	private void PsExpression()
	{
		this.Expression.addAll(this.OrExpression);
	}
	
	private void PsSelector()
	{
		this.Selector.addAll(this.Expression);
		
	}
	
	private void PsAssignment()
	{
		this.Assignment.addAll(this.Lhs);
	}

	private void PsImageIn()
	{
		this.ImageIn.add(Kind.IDENTIFIER);
	}
	
	private void PsSink() {
		// TODO Auto-generated method stub
		this.Sink.add(Kind.IDENTIFIER); //check for file type only
		this.Sink.add(Kind.KW_SCREEN);
	}
	
	private void PsImageOut()
	{
		this.ImageOut.add(Kind.IDENTIFIER);
	}
	
	private void PsStatement()
	{
		this.Statement.addAll(this.Assignment);
		this.Statement.addAll(this.ImageOut);
		this.Statement.addAll(this.ImageIn);
	}
	
	private void PsImageDec()
	{
		this.ImageDec.add(Kind.KW_image);
	}
	
	private void PsSourceSinkType()
	{
		this.SourceSinkType.add(Kind.KW_url);
		this.SourceSinkType.add(Kind.KW_file);
	}

	private void PsSource()
	{
		this.Source.add(Kind.STRING_LITERAL);
		this.Source.add(Kind.OP_AT);
		this.Source.add(Kind.IDENTIFIER);
	}
	
	private void PsSourceSinkDec()
	{
		this.SourceSinkDec.addAll(this.SourceSinkType);
	}
	
	private void PsVariableType()
	{
		this.VariableType.add(Kind.KW_int);
		this.VariableType.add(Kind.KW_boolean);
	}
	
	private void PsVariableDec()
	{
		this.VariableDec.add(Kind.KW_int);
		this.VariableDec.add(Kind.KW_boolean);
	}
	
	private void PsDeclaration()
	{
		this.Declaration.addAll(this.VariableDec);
		this.Declaration.addAll(this.SourceSinkDec);
		this.Declaration.addAll(this.ImageDec);
	}

	private void PsProgram()
	{
		this.Program.add(Kind.IDENTIFIER);
	}
	
	



































}
