// CGenVisitor.java
//
// A visitor which completes any semantic check not performed in the
// earlier passes and which writes human-readable LLVM code to a ll file.
//
// For subtrees which have a value (i.e. the subtree represents an expression)
// the result of a visit is an instance of the LLVMValue class.
// That instance describes where the value of the expression is held on the
// target computer.
// For other kinds of subtrees, the visit methods may return null.
//
// Author: Nigel horspool
// Date: March 2016

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;

public class CGenVisitor extends GooBaseVisitor<LLVMValue> {
	ParseTreeProperty<Scope> scopes;
	ParseTreeProperty<Type> types;
	BlockScope globals;
	Scope currentScope;
	int scopeNestingLevel = 0;
	Map<Symbol,LLVMValue> localVariables = new HashMap<Symbol,LLVMValue>();
	Symbol packageSymbol = null;
	String packageName = null;

    LLVM ll;
	
	// ************** constructors ******************

	// default constructor
	public CGenVisitor( LLVM ll ) {
        this.ll = ll;
	}

	// ******methods for associating data with tree nodes *********

	// associate scope s with parse tree node ctx
	void saveScope(ParserRuleContext ctx, Scope s) {
		scopes.put(ctx, s);
	}

	// obtain current scope previously associated with node ctx
	void lookupScope(ParserRuleContext ctx) {
		currentScope = scopes.get(ctx);
	}

	public void setScopes(ParseTreeProperty<Scope> scopes) {
		this.scopes = scopes;
	}

	public ParseTreeProperty<Scope> getScopes() {
		return scopes;
	}

	// access or set type information associated with a node

	public void setTypes(ParseTreeProperty<Type> types) {
		this.types = types;
	}

	public ParseTreeProperty<Type> getTypes() {
		return types;
	}

	// It may sometimes be convenient to replace the type associated
    // with a parse tree node; this method will do that
	public Type replaceType(ParserRuleContext ctx, Type typ) {
		types.put(ctx, typ);
		return typ;
	}

	public Type lookupType(ParserRuleContext ctx) {
		assert ctx != null;
		Type typ = types.get(ctx);
        if (typ != null) return typ;
        if (ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext)
        	return lookupType((ParserRuleContext)ctx.getChild(0));
        return null;
	}

	// *************** Visit methods *******************
	
	/* Note: 
		Tthese visit methods are in exactly the same order as
	   	the corresponding grammar rules in Goo.g4.
	   	If a visitor method is not needed for a group of rules with
	    the same LHS, a comment listing the rule(s) appears instead.
	   	This helps ensure that no rule has been missed.
	*/ 

	@Override
	public LLVMValue visitType(GooParser.TypeContext ctx) {
	    return visitChildren(ctx);
	}

    @Override
    public LLVMValue visitTypeName(GooParser.TypeNameContext ctx) {
		return visitChildren(ctx);
    }

	// typeLit:   arrayType | structType | pointerType | sliceType ;

    @Override
	public LLVMValue visitArrayType(GooParser.ArrayTypeContext ctx) {
		return visitChildren(ctx);
	}

    @Override
	public LLVMValue visitArrayLength(GooParser.ArrayLengthContext ctx) {
		return visitChildren(ctx);
	}

	// elementType:   type ;

    @Override
	public LLVMValue visitSliceType(GooParser.SliceTypeContext ctx) {
		return visitChildren(ctx);
	}

    @Override
	public LLVMValue visitStructType(GooParser.StructTypeContext ctx) {
		return visitChildren(ctx);
	}

	// fieldDeclList:  /* empty */ |  (fieldDecl ';')* fieldDecl optSemi ;
	
    @Override
	public LLVMValue visitFieldDecl(GooParser.FieldDeclContext ctx) {
		return visitChildren(ctx);
	}

    @Override
	public LLVMValue visitPointerType(GooParser.PointerTypeContext ctx) {
		return visitChildren(ctx);
	}

	// baseType:   type ;
	
    @Override
	public LLVMValue visitSignature(GooParser.SignatureContext ctx) {
		return visitChildren(ctx);
	}

	// result:   type ;

	// parameters:   '(' ( parameterList ','? )? ')' ;

	// parameterList:   parameterDecl (',' parameterDecl)*  ;

    @Override
	public LLVMValue visitParameterDecl(GooParser.ParameterDeclContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public LLVMValue visitMethodName(GooParser.MethodNameContext ctx) {
	    return visitChildren(ctx);
	}

	@Override
	public LLVMValue visitBlock(GooParser.BlockContext ctx) {
	    scopeNestingLevel++;
	    lookupScope(ctx);
		visitChildren(ctx);
		currentScope = currentScope.getEnclosingScope();
		scopeNestingLevel--;
		return null;
	}

	// statementList :	/* empty */ | (statement ';')* statement optSemi ;

	// declaration:   constDecl | typeDecl | varDecl ;

	// topLevelDeclList:    /* empty */ | (topLevelDecl ';')* topLevelDecl optSemi ;
	@Override
	public LLVMValue visitTopLevelDeclList(GooParser.TopLevelDeclListContext ctx) {
		visitChildren(ctx);
		return null;
	}	

	// topLevelDecl:   declaration | functionDecl ;

	// constDecl:   CONST constSpec | CONST '(' constSpecList ')' ;

	// constSpecList:   /* empty */ | (constSpec ';')* constSpec optSemi ;

    @Override
	public LLVMValue visitConstSpec(GooParser.ConstSpecContext ctx) {
	    List<Token> ids = ctx.identifierList().idl;
	    LLVMValue.LLVMValueList cvals = (LLVMValue.LLVMValueList)visit(ctx.constSpecRem());
	    mutipleDeclarations(ctx,ids,cvals,true);
		return null;
	}

	@Override
	public LLVMValue visitConstSpecRem(GooParser.ConstSpecRemContext ctx) {
		return visit(ctx.expressionList());
	}

	// identifierList:   idl+=Identifier (',' idl+=Identifier)*  ;

	// expressionList:   exl+=expression (',' exl+=expression)*  ;
	// Note: this visit method returns a different result type than other
	// visit methods ... it's effectively a list of LLVMValue instances.
	@Override
	public LLVMValue visitExpressionList(GooParser.ExpressionListContext ctx) {
	    List<GooParser.ExpressionContext> exps = ctx.exl;
	    LLVMValue.LLVMValueList rslt = LLVMValue.newLLVMValueList();
	    for( GooParser.ExpressionContext exp : exps ) {
	        rslt.expressionList.add(visit(exp));
	    }
	    return rslt;
	}

	// typeDecl:   TYPE typeSpec | TYPE '(' typeSpecList ')' ;

	// typeSpecList:   /* empty */ | (typeSpec ';')* typeSpec optSemi ;

    @Override
	public LLVMValue visitTypeSpec(GooParser.TypeSpecContext ctx) {
		return visitChildren(ctx);
	}

	// varDecl:   VAR varSpec | VAR '(' varSpecList ')' ;

	// varSpecList:   /* empty */ | (varSpec ';')* varSpec optSemi ;

    @Override
	public LLVMValue visitVarSpec(GooParser.VarSpecContext ctx) {
	    List<Token> ids = ctx.identifierList().idl;
	    LLVMValue.LLVMValueList initVals = (LLVMValue.LLVMValueList)visit(ctx.varSpecRem());
	    mutipleDeclarations(ctx,ids,initVals,false);
		return null;
	}

    @Override
	public LLVMValue visitVarSpecRem(GooParser.VarSpecRemContext ctx) {
		if (ctx.expressionList() != null)
			return visit(ctx.expressionList());
		else
			return LLVMValue.newLLVMValueList();
	}

	@Override
	public LLVMValue visitShortVarDecl(GooParser.ShortVarDeclContext ctx) {
	    List<Token> ids = ctx.identifierList().idl;
	    LLVMValue.LLVMValueList vals = (LLVMValue.LLVMValueList)visit(ctx.expressionList());
	    mutipleDeclarations(ctx,ids,vals,false);
		return null;
	}

    @Override
	public LLVMValue visitFunctionDecl(GooParser.FunctionDeclContext ctx) {
	    GooParser.FunctionContext fn = ctx.function();
	    if (fn == null) return null;
		Token funcId = ctx.functionName().Identifier().getSymbol();
		String funcName = funcId.getText();
		FunctionSymbol function = (FunctionSymbol)currentScope.resolveInCurrent(funcName);
		currentScope = function;		// enter the new scope
	    Type.Function sig = (Type.Function)lookupType(ctx);
	    Type[] results = sig.getResults();
	    String retType = "void";
	    if (results != null && results.length > 0)
	        retType = ll.createTypeDescriptor(results[0]);
	    ll.resetNumbering();
	    ll.println("; Function Attrs: nounwind uwtable");
	    ll.printf("define %s @%s(", retType, funcName );
	    localVariables.clear();
	    boolean notFirst = false;
	    StringBuilder sb = new StringBuilder();
	    for( Symbol parm : function.getParameters() ) {
	    	String ptyp = ll.createTypeDescriptor(parm.getType());
	    	int llalign = ll.getAlignment(ptyp);
	    	String llname = "%"+parm.getName();
	    	if (notFirst)
	    		ll.print(", ");
	    	else
	    		notFirst = true;
	    	ll.printf("%s %s", ptyp, llname);
	    	String llnameref = llname + ".addr";
	    	sb.append(String.format("  %s = alloca %s, align %d\n",
	    		llnameref, ptyp, llalign));
	    	sb.append(String.format("  store %s %s, %s* %s, align %d\n",
	    		ptyp, llname, ptyp, llnameref, llalign));
	    	localVariables.put(parm, new LLVMValue(ptyp, llnameref, true));
	    }
	    ll.println(") {");
	    ll.println("entry:");
	    ll.print(sb.toString());
	    visit(fn);
	    // force a ret instruction to appear
	    if (retType == "void")
	    	ll.println("  ret void");
	    else {
	    	ll.printf("  ret %s %s\n", retType, "0"); 
	    }
		currentScope = currentScope.getEnclosingScope();  // exit scope
		ll.println("}");
		localVariables.clear();
		return null;
	}

	// functionName:   Identifier ;

    @Override
    public LLVMValue visitFunction(GooParser.FunctionContext ctx) {
	    Type signature = lookupType(ctx);
        visit(ctx.functionBody());
        return null;
    }

	// functionBody:   block ;

	// operand:   literal | operandName | '(' expression ')' ;
	@Override
	public LLVMValue visitOperand(GooParser.OperandContext ctx) {
		if (ctx.expression() != null)
			return visit(ctx.expression());
	    return visitChildren(ctx);
	}

	// literal:   basicLit | compositeLit ;

	@Override
	public LLVMValue visitBasicLit(GooParser.BasicLitContext ctx) {
		Type typ = lookupType(ctx);
		String text = ctx.getText();
		if (ctx.IntLit() != null)
			return new LLVMValue("i32", text, false);
		else if (ctx.FloatLit() != null)
			return new LLVMValue("double", text, false);
		else if (ctx.RuneLit() != null)
			return ll.newCharLit(text);
		else if (ctx.StringLit() != null)
			return ll.newStringLit(text);
		ReportError.error(ctx, "failure");
		return new LLVMValue("i32", "0", false);
	}

    @Override
    public LLVMValue visitOperandName(GooParser.OperandNameContext ctx) {
    	if (ctx.qualifiedIdent() != null) 
        	return visit(ctx.qualifiedIdent());
        String id = ctx.Identifier().getText();
        Symbol sy = currentScope.resolve(id);
        assert(sy != null);
        LLVMValue rv = localVariables.get(sy);
        if (rv == null)
        	rv = LLVMExtras.lookupGlobal(ll,sy);
        return rv;
    }

	// this visit method has the side-effect of setting packageSymbol
	@Override
	public LLVMValue visitQualifiedIdent(GooParser.QualifiedIdentContext ctx) {
		String pkgName = ctx.packageName().getText();
		String memberName = ctx.Identifier().getText();
		packageSymbol = null;
		packageName = null;
		Symbol pkg = currentScope.resolve(pkgName);
		if (pkg != null && pkg instanceof Packages.PackageSymbol) {
			packageSymbol = ((Packages.PackageSymbol)pkg).getMember(memberName);
			packageName = pkgName;
			if (packageSymbol == null)
				ReportError.error(ctx, pkgName + "." + memberName + " not found");
		} else
			ReportError.error(ctx, "package " + pkgName + " not found");
		return null;
	}

	@Override
	public LLVMValue visitCompositeLit(GooParser.CompositeLitContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public LLVMValue visitLiteralType(GooParser.LiteralTypeContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public LLVMValue visitLiteralValue(GooParser.LiteralValueContext ctx) {
		return visitChildren(ctx);
	}

	// elementList:   exl+=element (',' exl+=element)*  ;

	// element:   value ;

	// value:   expression | literalValue ;

	@Override
	public LLVMValue visitPrimaryExpr(GooParser.PrimaryExprContext ctx) {
		if (ctx.arguments() != null) {
			// generate code for a function call
			LLVMValue.LLVMValueList valsList = (LLVMValue.LLVMValueList)visit(ctx.arguments());
			ArrayList<LLVMValue> argVals = valsList.expressionList;
			int k = 0;
			for( LLVMValue exp : argVals ) {
				exp = ll.dereference(exp);
				if (exp.getValue().charAt(0)=='c')
					exp = ll.forceStringReference(exp);
				argVals.set(k++, exp);
				
			}
			String funcName = ctx.primaryExpr().getText();
			Symbol funcSym = currentScope.resolve(funcName);
			if (funcSym != null && funcSym.getKind() == Symbol.Kind.TypeName) {
        	    Type toType = funcSym.getType();
        	    return LLVMExtras.typeConversion(ll, toType, argVals.get(0));
			}
			Symbol sym;
			if (funcName.indexOf('.')>0) {
				// it's a package member ... have to use member symbol
				visit(ctx.primaryExpr());
				sym = packageSymbol;
			} else
				sym = currentScope.resolve(funcName);
			LLVMValue result = null;
			if (sym != null) {
				result = CGenFunctionCall.genCall(ll, ctx, sym, packageName, argVals);
				packageSymbol = null;
				packageName = null;
			} else
			    ReportError.error(ctx, "function not found: "+funcName);
			return result;
		}
		if (ctx.selector() != null) {
			// Handle an ambiguity between x.y parsing as both
			// a qualifiedIdent and as a field selection from a struct value
			String pkgName = ctx.primaryExpr().getText();
			Symbol pkg = currentScope.resolve(pkgName);
			if (pkg != null && pkg instanceof Packages.PackageSymbol) {
				String memberName = ctx.selector().getText().substring(1);
				packageSymbol = ((Packages.PackageSymbol)pkg).getMember(memberName);
				packageName = pkgName;
				return null;
			}
			// it's a field selection in a struct
			assert false; // unimplemented
		}
		if (ctx.index() != null) {
		    // create reference to an array element
		    LLVMValue arrPtr = visit(ctx.primaryExpr());
		    LLVMValue index = visit(ctx.index());
		    Type.Array arrType = (Type.Array)lookupType(ctx.primaryExpr());
		    return LLVMExtras.elementReference(ll, arrType, arrPtr, index);
		}
		return visitChildren(ctx);
	}

	// selector:   '.' Identifier ;

	@Override
	public LLVMValue visitIndex(GooParser.IndexContext ctx) {
	    return visit(ctx.expression());
	}

	@Override
	public LLVMValue visitSlice(GooParser.SliceContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public LLVMValue visitArguments(GooParser.ArgumentsContext ctx) {
		if (ctx.expressionList() != null)
			return visit(ctx.expressionList());
		else
			return LLVMValue.newLLVMValueList();
	}

	@Override
	public LLVMValue visitUnExp(GooParser.UnExpContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public LLVMValue visitNumExp(GooParser.NumExpContext ctx) {
		Type typ = lookupType(ctx);
		boolean isFloat = typ instanceof Type.Flt;
		String text = ctx.addOp() != null?
					ctx.addOp().getText() : ctx.mulOp().getText();
		LLVMValue lhs = visit(ctx.expression(0));
		LLVMValue rhs = visit(ctx.expression(1));
		if (isFloat) {
			String op = selectLLVMFltOperator(ctx, text);
			return ll.writeFltInst(op, lhs, rhs);
		} else {
			boolean isSigned = !(typ instanceof Type.Uint);
			String op = selectLLVMIntOperator(ctx, text, isSigned);
			return ll.writeIntInst(op, lhs, rhs);
		}
	}

	@Override
	public LLVMValue visitRelExp(GooParser.RelExpContext ctx) {
		LLVMValue lhs = visit(ctx.expression(0));
		LLVMValue rhs = visit(ctx.expression(1));
		String text = ctx.relOp().getText();
		if (lhs.getType() == "float" || lhs.getType() == "double") {
			String relop = selectLLVMFltComparison(ctx, text);
			return ll.writeFCompInst(relop, lhs, rhs);
		} else {
			Type lhsType = lookupType(ctx.expression(0));
			boolean isSigned = !(lhsType instanceof Type.Uint);
			String relop = selectLLVMIntComparison(ctx, text, isSigned);
			return ll.writeCompInst(relop, lhs, rhs);
		}
	}
    
    private LLVMValue visitAndOrExp (GooParser.BoolExpContext ctx, boolean a) {
        String cont=ll.createBBLabel("continue");
        String fail=ll.createBBLabel("fail");
        String succeed=ll.createBBLabel("succeed");
        String end=ll.createBBLabel("end");
        String result=ll.nextTemporary();
        LLVMValue lhs=visit(ctx.expression(0));
        ll.writeCondBranch(lhs,a ? cont : succeed,a ? fail : cont);
        ll.writeLabel(cont);
        LLVMValue rhs=visit(ctx.expression(1));
        ll.writeCondBranch(rhs,succeed,fail);
        ll.writeLabel(succeed);
        ll.writeBranch(end);
        ll.writeLabel(fail);
        ll.writeBranch(end);
        ll.writeLabel(end);
        ll.printf("%s = phi i1 [ 0, %%%s ], [ 1, %%%s ]\n",result,fail,succeed);
        return new LLVMValue("i1",result,false);
    }
    
    @Override
    public LLVMValue visitBoolExp(GooParser.BoolExpContext ctx) {
        return visitAndOrExp(ctx,ctx.children.get(1).getText().equals("&&"));
    }

	@Override
	public LLVMValue visitUnaryExpr(GooParser.UnaryExprContext ctx) {
        if (ctx.primaryExpr()!=null) return visit(ctx.primaryExpr());
        if (ctx.unaryOp().getText().equals("+")) return visit(ctx.unaryExpr());
        if (ctx.unaryOp().getText().equals("-")) {
            LLVMValue operand=visit(ctx.unaryExpr());
            boolean flt=operand.getType().equals("float") || operand.getType().equals("double");
            LLVMValue neg1=new LLVMValue(operand.getType(),flt ? "-1.0" : "-1",false);
            return flt ? ll.writeFltInst("fmul",operand,neg1) : ll.writeIntInst("mul",operand,neg1);
        }
        if (ctx.unaryOp().getText().equals("&")) {
            LLVMValue operand=visit(ctx.unaryExpr());
            return new LLVMValue(operand.getType()+"*",operand.getValue(),false);
        }
        if (ctx.unaryOp().getText().equals("*")) {
            LLVMValue operand=visit(ctx.unaryExpr());
            String t=operand.getType();
            String demoted=t.substring(0,t.length()-1);
            if (operand.isReference()) operand=ll.dereference(operand);
            return new LLVMValue(demoted,operand.getValue(),true);
        }
        if (ctx.unaryOp().getText().equals("!")) {
            LLVMValue operand = visit(ctx.unaryExpr());
            LLVMValue o = ll.dereference(operand);
            assert(o.getType() == "i1");
            String rv = ll.nextTemporary();
            ll.printf("  %s = xor i1 %s, 1\n", rv, o.getValue());
            return new LLVMValue(o.getType(), rv, false);
        }
		return visitChildren(ctx);
	}

	// relOp:     '==' | '!=' | '<' | '<=' | '>' | '>=' ;
	// addOp:     '+' | '-' | '|' | '^' ;
	// mulOp:     '*' | '/' | '%' | '<<' | '>>' | '&' | '&^' ;
	// unaryOp:   '+' | '-' | '!' | '^' | '*' | '&' ;

    // Careful, conversions can have the same syntax as function calls
	@Override
	public LLVMValue visitConversion(GooParser.ConversionContext ctx) {
	    Type toType = lookupType(ctx.type());
	    LLVMValue exp = visit(ctx.expression());
	    return LLVMExtras.typeConversion(ll, toType, exp);
	}

	// statement:   declaration | labeledStmt | simpleStmt
	//          |   returnStmt | breakStmt | continueStmt
	//          |   gotoStmt | block | ifStmt | forStmt ;

	// simpleStmt:   emptyStmt | expressionStmt
	//          |   incDecStmt | assignment | shortVarDecl ;

	// emptyStmt: ;

	@Override
	public LLVMValue visitLabeledStmt(GooParser.LabeledStmtContext ctx) {
		return visitChildren(ctx);
	}

	// label:   Identifier ;

	// expressionStmt:   expression ;

	@Override
	public LLVMValue visitIncDecStmt(GooParser.IncDecStmtContext ctx) {
		return visitChildren(ctx);
	}

	// assignment :   expressionList assignOp expressionList
	@Override
	public LLVMValue visitAssignment(GooParser.AssignmentContext ctx) {
		LLVMValue.LLVMValueList left  = (LLVMValue.LLVMValueList)visit(ctx.expressionList(0));
		LLVMValue.LLVMValueList right = (LLVMValue.LLVMValueList)visit(ctx.expressionList(1));
		String op = ctx.assignOp().getText();
		int len = left.size();
		assert len == right.size();
		for( int k=0; k<len; k++ ) {
			LLVMValue src = ll.dereference(right.expressionList.get(k));
			LLVMValue dest = left.expressionList.get(k);
			String llop;
			switch(op) {
			case "+=":
			case "-=":
			case "*=":
			case "/=":
			case "%=":
				String operator = op.substring(0,1);
				if (dest.getType() == "float" || dest.getType() == "double") {
					llop = selectLLVMFltOperator(ctx, operator);
					src = ll.writeFltInst(llop, dest, src);
				} else {
					boolean isSigned = !(lookupType(ctx.expressionList(0).exl.get(k)) instanceof Type.Uint);
					llop = selectLLVMIntOperator(ctx, operator, isSigned);
					src = ll.writeIntInst(llop, dest, src);
				}
				break;
			case "=":
				break;
			default:
				ReportError.error(ctx, "unrecognized assignment operator: "+op);
			    break;
			}
			ll.store(src, dest);
		}
		return null;
	}

	// assignOp:   '=' | addOp '=' | mulOp '=' ;

	@Override
	public LLVMValue visitIfStmt(GooParser.IfStmtContext ctx) {
		if (ctx.simpleStmt() != null)
			visit(ctx.simpleStmt());
		LLVMValue cond = visit(ctx.expression());
		String thenLab = ll.createBBLabel("then");
		String elseLab = ll.createBBLabel("else");
		String endLab  = ll.createBBLabel("endif");
		ll.writeCondBranch(cond, thenLab, elseLab);
		ll.writeLabel(thenLab);
		visit(ctx.block());
		ll.writeBranch(endLab);
		ll.writeLabel(elseLab);
		visit(ctx.elsePart());
		ll.writeBranch(endLab);
		ll.writeLabel(endLab);
		return null;
	}

	@Override
	public LLVMValue visitElsePart(GooParser.ElsePartContext ctx) {
		return visitChildren(ctx);
	}

	// forStmt:   FOR condition block | FOR forClause block ;
    
    @Override
    public LLVMValue visitForStmt(GooParser.ForStmtContext ctx) {
        GooParser.ForClauseContext c=ctx.forClause();
        if (c!=null) {
            String incLabel=ll.createBBLabel("inc");
            String condLabel=ll.createBBLabel("cond");
            String bodyLabel=ll.createBBLabel("body");
            String endLabel=ll.createBBLabel("end");
            if (c.initStmt()!=null) visit(c.initStmt());
            ll.writeBranch(condLabel);
            ll.writeLabel(incLabel);
            if (c.postStmt()!=null) visit(c.postStmt());
            ll.writeBranch(condLabel);
            ll.writeLabel(condLabel);
            LLVMValue cond=(c.condition()!=null) ? visit(c.condition()) : null;
            if (cond!=null) ll.writeCondBranch(cond,bodyLabel,endLabel);
            else ll.writeBranch(bodyLabel);
            ll.writeLabel(bodyLabel);
            visit(ctx.block());
            ll.writeBranch(incLabel);
            ll.writeLabel(endLabel);
            return null;
        }
        //  START
        //
        //  Evaluate condition
        //  Branch to BODY if true, END if false
        //
        //  BODY
        //
        //  Loop body here
        //  Branch to START
        //
        //  END
        String startLabel=ll.createBBLabel("start");
        String bodyLabel=ll.createBBLabel("body");
        String endLabel=ll.createBBLabel("end");
        ll.writeBranch(startLabel);
        ll.writeLabel(startLabel);
        LLVMValue cond=visit(ctx.condition());
        ll.writeCondBranch(cond,bodyLabel,endLabel);
        ll.writeLabel(bodyLabel);
        visit(ctx.block());
        ll.writeBranch(startLabel);
        ll.writeLabel(endLabel);
        return null;
    }

	@Override
	public LLVMValue visitCondition(GooParser.ConditionContext ctx) {
		return visit(ctx.expression());
	}

	// forClause:   initStmt ';' condition? ';' postStmt ;

	// initStmt:   simpleStmt ;

	// postStmt:   simpleStmt ;

	@Override
	public LLVMValue visitReturnStmt(GooParser.ReturnStmtContext ctx) {
		LLVMValue returnValue = null;
		if (ctx.expressionList() != null) {
			LLVMValue.LLVMValueList retVals = (LLVMValue.LLVMValueList)visit(ctx.expressionList());
			returnValue = retVals.expressionList.get(0);
		}
		ll.writeReturnInst(returnValue);
		return null;
	}

	// breakStmt:   BREAK | BREAK label ;

	// continueStmt:   CONTINUE | CONTINUE label ;

	// gotoStmt:   GOTO label ;

    @Override
	public LLVMValue visitSourceFile(GooParser.SourceFileContext ctx) {
	    lookupScope(ctx);
	    return visitChildren(ctx);
	}

	// packageClause:   PACKAGE packageName ;
	
	// packageName:   Identifier ;

	// importDeclList:   (importDecl ';')*  ;

	// importDecl:   IMPORT importSpec | IMPORT '(' importSpecList ')' ;

	// importSpecList:  /* empty */ | (importSpec ';')* importSpec optSemi ;

	// importSpec:   importPath ;

	// importPath:   StringLit ;


// ********************** utility methods ********************************

	// scans up enclosing scopes to find current function
	// CRASHES IF CALLED WHEN CURRENT SCOPE IS PACKAGE LEVEL !! 
	private FunctionSymbol currentFunction() {
		Scope scope = currentScope;
		while( !(scope instanceof FunctionSymbol) )
			scope = scope.getEnclosingScope();
		return (FunctionSymbol)scope;
	}

    private void mutipleDeclarations(ParserRuleContext ctx, List<Token> ids, 
    				LLVMValue.LLVMValueList vals, boolean isConst) {
    	boolean valueProvided = true;
    	if (vals.size() == 0)
    		valueProvided = false;
    	else
	    if (ids.size() != vals.size()) {
	    	ReportError.error(ctx, "ids/exps length mismatch: "+ids.size()+", "+vals.size());
	    	assert(false);
	    }
	    int k = 0;
	    for( Token id : ids ) {
	        String name = id.getText();
	        Symbol sy = currentScope.resolveInCurrent(name);
	        if (sy == null) {
	            ReportError.error(ctx, "variable/constant "+name+" not found in symbol table");
	            continue;
	        }
	        LLVMValue cv = null;
	        if (valueProvided)
	        	cv = vals.expressionList.get(k++);
	        if (scopeNestingLevel == 0) {
	        	if (valueProvided)
	            	LLVMExtras.writeGlobalDecl(ll,sy,isConst,cv.getValue());
	            else
	            	LLVMExtras.writeGlobalDecl(ll,sy);
	        } else {
	            LLVMValue ref = LLVMExtras.writeLocalDecl(ll,sy);
	            localVariables.put(sy, ref);
	            if (valueProvided) {
		            if (cv.getValue().charAt(0)=='c') {
		                // a local string variable is to be initialized
		                LLVMExtras.writeAssignment(ll, ll.forceStringReference(cv), ref);
		            } else {
		                LLVMExtras.writeAssignment(ll, cv, ref);
		            }
	            }
	        }
	    }
    }
    
    private String selectLLVMFltOperator( ParserRuleContext ctx, String text ) {
		switch(text) {
			case "+":	return "fadd";
			case "-":   return "fsub";
			case "*":	return "fmul";
			case "/":   return "fdiv";
			case "%":   return "frem";
		}
		ReportError.error(ctx, "unhandled operator: "+text);
		return "xxx";
    }

    private String selectLLVMIntOperator( ParserRuleContext ctx, String text, boolean isSigned ) {
		switch(text) {
			case "+":	return "add";
			case "-":   return "sub";
			case "*":	return "mul";
			case "/":   return isSigned? "sdiv" : "udiv";
			case "%":   return isSigned? "srem" : "urem";
		}
		ReportError.error(ctx, "unhandled operator: "+text);
		return "xxx";
    }

	private String selectLLVMIntComparison( ParserRuleContext ctx, String text, boolean isSigned ) {
		switch(text) {
		case "==":	return "eq";
		case "!=":  return "ne";
		case ">":	return isSigned? "sgt" : "ugt";
		case ">=":	return isSigned? "sge" : "uge";
		case "<":	return isSigned? "slt" : "ult";
		case "<=":	return isSigned? "sge" : "uge";
		}
		ReportError.error(ctx, "unhandled comparison: "+text);
		return "xxx";
	}

	private String selectLLVMFltComparison( ParserRuleContext ctx, String text ) {
		switch(text) {
		case "==":	return "oeq";
		case "!=":  return "one";
		case ">":	return "ogt";
		case ">=":	return "oge";
		case "<":	return "olt";
		case "<=":	return "oge";
		}
		ReportError.error(ctx, "unhandled comparison: "+text);
		return "xxx";
	}
}
