package com.isti.traceview.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ShellCommandExecutor {
	protected static Logger lg = Logger.getLogger(ShellCommandExecutor.class);
	private static boolean paused = false;

	/**
	 * @return the paused
	 */
	public static boolean isPaused() {
		return paused;
	}

	public static void execParseCommand(String command, DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
		lg.debug("execParseCommand: Executing " + command);
		InputStream stdIn = null;
		BufferedReader stdErr = null;
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		// lg.debug("execParseCommand: sync masterProps on");
		try {
			Process process = Runtime.getRuntime().exec(command);
			stdIn = process.getInputStream();
			sp.parse(stdIn, handler);
			stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			// read any errors from the attempted command
			String err = null, s;
			while ((s = stdErr.readLine()) != null) {
				err = err + s;
			}
			if (err != null) {
				throw new RuntimeException("Can not execute '" + command + "': " + err);
			}
			int extVal = process.waitFor();
		} catch (InterruptedException e) {
		} catch (SAXParseException e) {
			if (e.getMessage().contains("Premature end of file.")) {
				paused = true;
				JOptionPane.showMessageDialog(null, "Remote SSH server returns an error. Check your ssh connection.", "Warning", JOptionPane.WARNING_MESSAGE);
				paused = false;
			} else {
				throw e;
			}
		} finally {
			try {
				stdErr.close();
				stdIn.close();
			} catch (Exception e) {
			}
		}
		// lg.debug("execParseCommand: sync masterProps off");
	}

	public static String execCommand(String command, int timeout, boolean checkErrors) throws IOException {
		BufferedReader stdIn = null;
		BufferedReader stdErr = null;
		int extVal;
		String out = "";
		String err = "";
		// lg.debug("execCommand: sync masterProps on");

		try {
			Process process = Runtime.getRuntime().exec(command);
			if (timeout <= 0) {
				extVal = process.waitFor();
			} else {
				long now = System.currentTimeMillis();
				long timeoutInMillis = 1000L * timeout;
				long finish = now + timeoutInMillis;
				while (isAlive(process) && (System.currentTimeMillis() < finish)) {
					Thread.sleep(10);
				}
				if (isAlive(process)) {
					throw new InterruptedException("Command " + command + " was timed out after " + timeout + " seconds");
				}
				extVal = process.exitValue();
			}
			stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			// read any errors from the attempted command
			String s;
			int i = 0;
			while ((s = stdErr.readLine()) != null) {
				if (i == 0) {
					if (err.length() > 0) {
						err = err + "\n";
					}
					err = err + s;
					i++;
				}
			}

			// read command output
			while ((s = stdIn.readLine()) != null) {
				if (out.length() > 0) {
					out = out + "\n";
				}
				out = out + s;
			}

			if (extVal == 0) {
				lg.debug("Command executed: " + command);
			} else {
				out = null;
				if (checkErrors) {
					throw new RuntimeException("Can not execute command:\n'" + command + "'\nextVal = " + extVal + "\n" + err);
				} else {
					lg.debug("Command executed with errors: " + command);
				}
			}

		} catch (InterruptedException e) {
			lg.error(e.getMessage());
		} finally {
			try {
				if (stdErr != null)
					stdErr.close();
				if (stdIn != null)
					stdIn.close();
			} catch (IOException e1) {
			}
		}
		// lg.debug("execCommand: sync masterProps off");
		return out;
	}

	public static boolean isAlive(Process p) {
		try {
			p.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

	public static String execApacheCommand(String command, int timeout, boolean checkErrors) throws IOException {
		OutputStream stdOut = new ByteArrayOutputStream();
		OutputStream stdErr = new ByteArrayOutputStream();
		int extVal;
		Date now = null;
		try {
			CommandLine cmdLine = CommandLine.parse(command);
			DefaultExecutor executor = new DefaultExecutor();
			executor.setExitValue(0);
			ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout * 1000);
			executor.setWatchdog(watchdog);
			PumpStreamHandler pump = new PumpStreamHandler(stdOut, stdErr);
			executor.setStreamHandler(pump);
			now = new Date();
			extVal = executor.execute(cmdLine);
			if (extVal == 0) {
				lg.debug("Command executed: " + command);
			} else {
				if (checkErrors) {
					throw new RuntimeException("Can not execute command: " + command + ", extVal = " + extVal + ", " + stdErr.toString());
				} else {
					lg.debug("Command executed with errors: " + command);
					return null;
				}
			}
			return stdOut.toString();
		} catch (ExecuteException e){
			if(new Date().getTime() - now.getTime()>timeout * 1000){
				lg.debug("Command was not executed by timeout: " + command + e.toString());
				return null;
			} else {
				throw new RuntimeException("Can not execute command: " + command + ", " + e.toString() + ", " + stdErr.toString());
			}
		} finally {
			try {
				if (stdErr != null)
					stdErr.close();
				if (stdOut != null)
					stdOut.close();
			} catch (IOException e1) {
			}
		}
	}
}
