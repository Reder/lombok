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
package lombok.installer.eclipse;

import java.util.Arrays;
import java.util.List;

import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.IdeFinder;
import lombok.installer.IdeLocation;

import org.mangosdk.spi.ProviderFor;

/**
 * STS (Springsource Tool Suite) is an eclipse variant.
 * Other than different executable names, it's the same as eclipse, as far as lombok support goes.
 */
@ProviderFor(IdeFinder.class)
public class STSFinder extends EclipseFinder {
	@Override protected IdeLocation createLocation(String guess) throws CorruptedIdeLocationException {
		return new STSLocationProvider().create0(guess);
	}
	
	@Override protected String getDirName() {
		return "sts";
	}
	
	@Override protected String getMacExecutableName() {
		return "STS.app";
	}
	
	@Override protected String getUnixExecutableName() {
		return "STS";
	}
	
	@Override protected String getWindowsExecutableName() {
		return "STS.exe";
	}
	
	@Override protected List<String> getSourceDirsOnWindows() {
		return Arrays.asList("\\", "\\springsource", "\\Program Files", "\\Program Files\\springsource", System.getProperty("user.home", "."), System.getProperty("user.home", ".") + "\\springsource");
	}
	
	@Override protected List<String> getSourceDirsOnMac() {
		return Arrays.asList("/Applications", "/Applications/springsource", System.getProperty("user.home", "."), System.getProperty("user.home", ".") + "/springsource");
	}
	
	@Override protected List<String> getSourceDirsOnUnix() {
		return Arrays.asList(System.getProperty("user.home", "."), System.getProperty("user.home", ".") + "/springsource");
	}
}
