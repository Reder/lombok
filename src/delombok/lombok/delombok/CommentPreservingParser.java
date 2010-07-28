/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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
package lombok.delombok;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import lombok.javac.DeleteLombokAnnotations;
import lombok.javac.JavacTransformer;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.OptionName;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Options;

public class CommentPreservingParser {
	private final String encoding;
	private boolean deleteLombokAnnotations = false;
	private DiagnosticListener<JavaFileObject> diagnostics = null;
	
	public CommentPreservingParser() {
		this("utf-8");
	}
	
	public CommentPreservingParser(String encoding) {
		this.encoding = encoding;
	}
	
	public void setDeleteLombokAnnotations(boolean deleteLombokAnnotations) {
		this.deleteLombokAnnotations = deleteLombokAnnotations;
	}
	
	public void setDiagnosticsListener(DiagnosticListener<JavaFileObject> diagnostics) {
		this.diagnostics = diagnostics;
	}
	
	public ParseResult parse(JavaFileObject source, boolean forceProcessing) throws IOException {
		return doParse(source, forceProcessing);
	}
	
	public ParseResult parse(String fileName, boolean forceProcessing) throws IOException {
		return doParse(fileName, forceProcessing);
	}
	
	private ParseResult doParse(Object source, boolean forceProcessing) throws IOException {
		Context context = new Context();
		
		Options.instance(context).put(OptionName.ENCODING, encoding);
		
		if (diagnostics != null) context.put(DiagnosticListener.class, diagnostics);
		
		CommentCollectingScanner.Factory.preRegister(context);
		
		JavaCompiler compiler = new JavaCompiler(context) {
			@Override
			protected boolean keepComments() {
				return true;
			}
		};
		
		compiler.genEndPos = true;
		
		Comments comments = new Comments();
		context.put(Comments.class, comments);
		if (deleteLombokAnnotations) context.put(DeleteLombokAnnotations.class, new DeleteLombokAnnotations(true));
		
		comments.comments = List.nil();
		
		JCCompilationUnit cu;
		if (source instanceof JavaFileObject) {
			cu = compiler.parse((JavaFileObject) source);
		} else {
			@SuppressWarnings("deprecation")
			JCCompilationUnit unit = compiler.parse((String)source);
			cu = unit;
		}
		
		boolean changed = new JavacTransformer(messager).transform(context, Collections.singletonList(cu));
		return new ParseResult(comments.comments, cu, forceProcessing || changed);
	}
	
	private static final Messager messager = new Messager() {
		@Override public void printMessage(Kind kind, CharSequence msg) {
			System.out.printf("%s: %s\n", kind, msg);
		}
		
		@Override public void printMessage(Kind kind, CharSequence msg, Element e) {
			System.out.printf("%s: %s\n", kind, msg);
		}
		
		@Override public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
			System.out.printf("%s: %s\n", kind, msg);
		}
		
		@Override public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
			System.out.printf("%s: %s\n", kind, msg);
		}
	};
	
	static class Comments {
		List<Comment> comments = List.nil();
		
		void add(Comment comment) {
			comments = comments.append(comment);
		}
	}
	
	public static class ParseResult {
		private final List<Comment> comments;
		private final JCCompilationUnit compilationUnit;
		private final boolean changed;
		
		private ParseResult(List<Comment> comments, JCCompilationUnit compilationUnit, boolean changed) {
			this.comments = comments;
			this.compilationUnit = compilationUnit;
			this.changed = changed;
		}
		
		public void print(Writer out) throws IOException {
			if (!changed) {
				JavaFileObject sourceFile = compilationUnit.getSourceFile();
				if (sourceFile != null) {
					out.write(sourceFile.getCharContent(true).toString());
					return;
				}
			}
			
			out.write("// Generated by delombok at " + new Date() + "\n");
			compilationUnit.accept(new PrettyCommentsPrinter(out, compilationUnit, comments));
		}
		
		public boolean isChanged() {
			return changed;
		}
	}
}
