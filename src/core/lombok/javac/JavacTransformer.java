/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.javac;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.processing.Messager;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;

public class JavacTransformer {
	private final HandlerLibrary handlers;
	private final Messager messager;
	
	public JavacTransformer(Messager messager) {
		this.messager = messager;
		this.handlers = HandlerLibrary.load(messager);
	}
	
	/**
	 * This must be a fresh context; for example the JavaCompiler in it, if any, must not have been used to compile anything yet.
	 */
	public boolean transform(Context context, List<JCCompilationUnit> compilationUnits) {
		JavacProcessingEnvironment environment = new JavacProcessingEnvironment(context, null);
		Check chk = Check.instance(context);
		chk.compiled.clear();
		com.sun.tools.javac.util.List<JCCompilationUnit> list = com.sun.tools.javac.util.List.nil();
		if (compilationUnits != null && !compilationUnits.isEmpty()) {
			ListIterator<JCCompilationUnit> it = compilationUnits.listIterator(compilationUnits.size());
			while (it.hasPrevious()) list = list.prepend(it.previous());
		}
		JavaCompiler.instance(context).enterTrees(list);
		
		return transform(environment, compilationUnits);
	}
	
	public boolean transform(JavacProcessingEnvironment environment, Iterable<JCCompilationUnit> compilationUnits) {
		List<JavacAST> asts = new ArrayList<JavacAST>();
		
		Context context = environment.getContext();
		
		for (JCCompilationUnit unit : compilationUnits) asts.add(new JavacAST(messager, environment, unit));
		
		handlers.skipPrintAST();
		for (JavacAST ast : asts) {
			ast.traverse(new AnnotationVisitor());
			handlers.callASTVisitors(ast);
			if (ast.isChanged()) {
				JCCompilationUnit top = (JCCompilationUnit) ast.top().get();
				for (JCTree member : top.defs) {
					if (member instanceof JCClassDecl) {
						ClassSymbol sym = ((JCClassDecl)member).sym;
						if (sym != null) Check.instance(context).compiled.remove(sym.flatname);
					}
				}
				top.accept(Enter.instance(context));
			}
		}
		
		handlers.skipAllButPrintAST();
		for (JavacAST ast : asts) {
			ast.traverse(new AnnotationVisitor());
		}
		
		for (JavacAST ast : asts) {
			if (ast.isChanged()) return true;
		}
		return false;
	}
	
	private class AnnotationVisitor extends JavacASTAdapter {
		@Override public void visitAnnotationOnType(JCClassDecl type, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnField(JCVariableDecl field, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnMethod(JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnLocal(JCVariableDecl local, JavacNode annotationNode, JCAnnotation annotation) {
			if (annotationNode.isHandled()) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if (handled) annotationNode.setHandled();
		}
	}
}
