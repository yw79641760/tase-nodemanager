/**
 * 
 */
package com.softsec.tase.node.util.cmd;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.softsec.tase.node.domain.ProcessResult;
import com.softsec.tase.node.util.StringUtils;

/**
 * ProcessHandler.java
 * @author yanwei
 * @date 2013-2-8 下午3:37:18
 * @description
 */
public class ProcessHandler {
	
	/**
	 * manage process 's input and output
	 * <br /> this method record every exception into ProcessResult
	 * but do not throws exception
	 * @param process
	 * @param input
	 * @param successCode
	 * @param timeout
	 * @param charset
	 * @return
	 */
	public ProcessResult launchContainer(Process process, String command, String input, int successCode, long timeout, String charset) {
		
		// init process result
		ProcessResult result = new ProcessResult(command, input, successCode, timeout);
		
		// launch the time watcher
		if (timeout > 0L) {
			ThreadTimer timer = new ThreadTimer(process, result.getProcessFlagSet(), timeout);
			new Thread(timer).start();
		}

		StringBuilder outputBuilder = new StringBuilder();
		StringBuilder errorBuilder = new StringBuilder();
		Reader stdOutput = null;
		Reader stdError = null;

		try {
			
			// get the standard output and error stream
			if (StringUtils.isEmpty(charset)) {
				stdOutput = new InputStreamReader(process.getInputStream());
				stdError = new InputStreamReader(process.getErrorStream());
			} else {
				try {
					stdOutput = new InputStreamReader(process.getInputStream(), charset);
					stdError = new InputStreamReader(process.getErrorStream(), charset);
				} catch (UnsupportedEncodingException uee) {
					result.addThrowable(uee);
				}
			}
			
			// write the parameter into the input stream
			if (!StringUtils.isEmpty(input)) {
				OutputStream stdIn = process.getOutputStream();
				try {
					stdIn.write(input.getBytes());
					// FIXME method flush() will invoke IOException, which makes me confused.
//					stdIn.flush();
					stdIn.close();
				} catch (IOException ioe) {
					System.out.println(result);
					result.addThrowable(ioe);
				}
			}
			
			char[] buffer = new char[1024];
			
			boolean isCompleted = false;
			boolean stdoutClosed = false;
			boolean stderrClosed = false;
			
			// read standard output & error & exitCode
			try {
				
				while (!isCompleted
						&& !result.getProcessFlagCode(ProcessResult.INTERRUPT_CODE_BIT) 
						&& !result.getProcessFlagCode(ProcessResult.EXIT_CODE_BIT)) {
					
					boolean readable = false;
					// read from the process 's standard output
					if (!stdoutClosed && stdOutput.ready()) {
						readable = true;
						int read = stdOutput.read(buffer, 0, buffer.length);
						if (read < 0) {
							readable = false;
							stdoutClosed = true;
						} else if (read > 0) {
							readable = true;
							outputBuilder.append(buffer, 0, read);
						}
					}
					
					// read from process 's standard error
					if (!stderrClosed && stdError.ready()) {
						int read = stdError.read(buffer, 0, buffer.length);
						if (read < 0) {
							readable = false;
							stderrClosed = true;
						} else if (read > 0) {
							readable = true;
							errorBuilder.append(buffer, 0, read);
						}
					}
					
					// check exit status only haven't read anything
					// if something has been read, the process is obviously not dead yet
					if (!readable) {
						try {
							
							int retCode = process.waitFor();
							result.setRetCode(retCode);
							if (retCode != successCode) {
								result.setProcessFlagCode(ProcessResult.EXIT_CODE_BIT);
							}
							isCompleted = true;
							
						} catch (IllegalThreadStateException itse) {
							
							result.addThrowable(itse);
							try {
								Thread.sleep(100);
							} catch (InterruptedException ie) {
								result.setProcessFlagCode(ProcessResult.INTERRUPT_CODE_BIT);
								process.destroy();
							}
							
						} catch (InterruptedException ie) {
							result.addThrowable(ie);
						}
					}
				}
				
			} catch (IOException ioe) {
				result.addThrowable(ioe);
			} finally {
				result.setOutput(outputBuilder.toString());
				result.setError(errorBuilder.toString());
			}
			
		} catch (NullPointerException npe) {
			result.addThrowable(npe);
		} finally {
		}
		

		return result;
	}
	
	public ProcessResult launchContainer(Process process, String command, String input, long timeout) {
		return launchContainer(process, command, input, 0, timeout, null);
	}
	
	public ProcessResult launchContainer(Process process, String command) {
		return launchContainer(process, command, null, 0, 0, null);
	}
}
