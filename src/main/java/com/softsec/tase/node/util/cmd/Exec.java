package com.softsec.tase.node.util.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.BitSet;


/***************************************************************************************************
 * 
 * 
 * This file is part of WebGoat, an Open Web Application Security Project utility. For details,
 * please see http://www.owasp.org/
 * 
 * Copyright (c) 2002 - 2007 Bruce Mayhew
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * 
 * Getting Source 
 * 
 * Source for this application is maintained at code.google.com, a repository for free software
 * projects.
 * 
 * For details, please see http://code.google.com/p/webgoat/
 * 
 * @author Jeff Williams <a href="http://www.aspectsecurity.com">Aspect Security</a>
 * @created October 28, 2003
 */
public class Exec
{

  /**
   * Description of the Method
   * 
   * @param command
   *            Description of the Parameter
   * @param input
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public static ExecResults execInput(String command, String input)
  {
    return (execOptions(command, input, 0, 0, false));
  }

  /**
   * Description of the Method
   * 
   * @param command
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public static ExecResults execLazy(String command)
  {
    return (execOptions(command, "", 0, 0, true));
  }

  /*
   * Execute an OS command and capture the output in an ExecResults. All exceptions are caught and
   * stored in the ExecResults. @param String command is the OS command to execute @param String
   * input is piped into the OS command @param int successCode is the expected return code if the
   * command completes successfully @param int timeout is the number of milliseconds to wait
   * before interrupting the command @param boolean quit tells the method to exit when there is no
   * more output waiting
   */
  /**
   * Description of the Method
   * 
   * @param command
   *            Description of the Parameter
   * @param input
   *            Description of the Parameter
   * @param successCode
   *            Description of the Parameter
   * @param timeout
   *            Description of the Parameter
   * @param lazy
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public static ExecResults execOptions(String[] command, String input, int successCode, int timeout, boolean lazy)
  {
    Process child = null;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();
    ExecResults results = new ExecResults(command[0], input, successCode, timeout);
    BitSet interrupted = new BitSet(1);
    boolean lazyQuit = false;
    ThreadTimer watcher;

    try
    {
      // start the command
      child = Runtime.getRuntime().exec(command);

      // get the streams in and out of the command
      InputStream processIn = child.getInputStream();
      InputStream processError = child.getErrorStream();
      OutputStream processOut = child.getOutputStream();

      // start the clock running
      if (timeout > 0)
      {
        watcher = new ThreadTimer(child, interrupted, timeout);
        new Thread(watcher).start();
      }

      // Write to the child process' input stream
      if ((input != null) && !input.equals(""))
      {
        try
        {
          processOut.write(input.getBytes());
          processOut.flush();
          processOut.close();
        } catch (IOException e1)
        {
          results.setThrowable(e1);
        }
      }

      // Read from the child process' output stream
      // The process may get killed by the watcher at any time
      int c = 0;

      try
      {
        while (true)
        {
          if (interrupted.get(0) || lazyQuit)
          {
            break;
          }

          // interrupted
          c = processIn.read();

          if (c == -1)
          {
            break;
          }

          // end of stream
          output.write(c);

          if (lazy && (processIn.available() < 1))
          {
            lazyQuit = true;
          }

          // if lazy and nothing then quit (after at least one read)
        }

        processIn.close();
      } catch (IOException e2)
      {
        results.setThrowable(e2);
      } finally
      {
        if (interrupted.get(0))
        {
          results.setInterrupted();
        }

        results.setOutput(output.toString());
      }

      // Read from the child process' error stream
      // The process may get killed by the watcher at any time
      try
      {
        while (true)
        {
          if (interrupted.get(0) || lazyQuit)
          {
            break;
          }

          // interrupted
          c = processError.read();

          if (c == -1)
          {
            break;
          }

          // end of stream
          output.write(c);

          if (lazy && (processError.available() < 1))
          {
            lazyQuit = true;
          }

          // if lazy and nothing then quit (after at least one read)
        }

        processError.close();
      } catch (IOException e3)
      {
        results.setThrowable(e3);
      } finally
      {
        if (interrupted.get(0))
        {
          results.setInterrupted();
        }

        results.setErrors(errors.toString());
      }

      // wait for the return value of the child process.
      if (!interrupted.get(0) && !lazyQuit)
      {
        int returnCode = child.waitFor();
        results.setReturnCode(returnCode);

        if (returnCode != successCode)
        {
          results.setError(ExecResults.BADRETURNCODE);
        }
      }
    } catch (InterruptedException i)
    {
      results.setInterrupted();
    } catch (Throwable t)
    {
      results.setThrowable(t);
    } finally
    {
      if (child != null)
      {
        child.destroy();
      }
    }

    return (results);
  }

  /*
   * Execute an OS command and capture the output in an ExecResults. All exceptions are caught and
   * stored in the ExecResults. @param String command is the OS command to execute @param String
   * input is piped into the OS command @param int successCode is the expected return code if the
   * command completes successfully @param int timeout is the number of milliseconds to wait
   * before interrupting the command @param boolean quit tells the method to exit when there is no
   * more output waiting
   */
  /**
   * Description of the Method
   * 
   * @param command
   *            Description of the Parameter
   * @param input
   *            Description of the Parameter
   * @param successCode
   *            Description of the Parameter
   * @param timeout
   *            Description of the Parameter
   * @param lazy
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public static ExecResults execOptions(String command, String input, int successCode, int timeout, boolean lazy)
  {
    Process child = null;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();
    ExecResults results = new ExecResults(command, input, successCode, timeout);
    BitSet interrupted = new BitSet(1);
    boolean lazyQuit = false;
    ThreadTimer watcher;

    try
    {
      // start the command
      child = Runtime.getRuntime().exec(command);

      // get the streams in and out of the command
      InputStream processIn = child.getInputStream();
      InputStream processError = child.getErrorStream();
      OutputStream processOut = child.getOutputStream();

      // start the clock running
      if (timeout > 0)
      {
        watcher = new ThreadTimer(child, interrupted, timeout);
        new Thread(watcher).start();
      }

      // Write to the child process' input stream
      if ((input != null) && !input.equals(""))
      {
        try
        {
          processOut.write(input.getBytes());
          processOut.flush();
          processOut.close();
        } catch (IOException e1)
        {
          results.setThrowable(e1);
        }
      }

      // Read from the child process' output stream
      // The process may get killed by the watcher at any time
      int c = 0;

      try
      {
        while (true)
        {
          if (interrupted.get(0) || lazyQuit)
          {
            break;
          }

          // interrupted
          c = processIn.read();

          if (c == -1)
          {
            break;
          }

          // end of stream
          output.write(c);

          if (lazy && (processIn.available() < 1))
          {
            lazyQuit = true;
          }

          // if lazy and nothing then quit (after at least one read)
        }

        processIn.close();
      } catch (IOException e2)
      {
        results.setThrowable(e2);
      } finally
      {
        if (interrupted.get(0))
        {
          results.setInterrupted();
        }

        results.setOutput(output.toString());
      }

      // Read from the child process' error stream
      // The process may get killed by the watcher at any time
      try
      {
        while (true)
        {
          if (interrupted.get(0) || lazyQuit)
          {
            break;
          }

          // interrupted
          c = processError.read();

          if (c == -1)
          {
            break;
          }

          // end of stream
          output.write(c);

          if (lazy && (processError.available() < 1))
          {
            lazyQuit = true;
          }

          // if lazy and nothing then quit (after at least one read)
        }

        processError.close();
      } catch (IOException e3)
      {
        results.setThrowable(e3);
      } finally
      {
        if (interrupted.get(0))
        {
          results.setInterrupted();
        }

        results.setErrors(errors.toString());
      }

      // wait for the return value of the child process.
      if (!interrupted.get(0) && !lazyQuit)
      {
        int returnCode = child.waitFor();
        results.setReturnCode(returnCode);

        if (returnCode != successCode)
        {
          results.setError(ExecResults.BADRETURNCODE);
        }
      }
    } catch (InterruptedException i)
    {
      results.setInterrupted();
    } catch (Throwable t)
    {
      results.setThrowable(t);
    } finally
    {
      if (child != null)
      {
        child.destroy();
      }
    }

    return (results);
  }

  /**
   * Description of the Method
   * 
   * @param command
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public static ExecResults execSimple(String[] command)
  {
    return (execOptions(command, "", 0, 0, false));
  }

  /**
   * Description of the Method
   * 
   * @param command
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public static ExecResults execSimple(String command)
  {
    return (execOptions(command, "", 0, 0, false));
  }

  /**
   * Description of the Method
   * 
   * @param command
   *            Description of the Parameter
   * @param args
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public static ExecResults execSimple(String command, String args)
  {
    return (execOptions(command, args, 0, 0, false));
  }

  /**
   * Description of the Method
   * 
   * @param command
   *            Description of the Parameter
   * @param timeout
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public static ExecResults execTimeout(String command, int timeout)
  {
    return (execOptions(command, "", 0, timeout, false));
  }

  /**
   * The main program for the Exec class
   * 
   * @param args
   *            The command line arguments
   */
  public static void main(String[] args)
  {
    ExecResults results;
    String sep = System.getProperty("line.separator");
    System.out.println("-------------------------------------------" + sep + "TEST 1: execSimple");
    results = Exec.execSimple("c:/swarm-2.1.1/bin/whoami.exe");
    System.out.println(results);
    System.out.println("-------------------------------------------" + sep + "TEST 2: execSimple (with search)");
    results = Exec.execSimple("netstat -r");
    System.out.println(results);

    if (results.outputContains("localhost:1031"))
    {
      System.out.println("ERROR: listening on 1031");
    }

    System.out.println("-------------------------------------------" + sep + "TEST 3: execInput");
    results = Exec.execInput("find \"cde\"", "abcdefg1\nhijklmnop\nqrstuv\nabcdefg2");
    System.out.println(results);
    System.out.println("-------------------------------------------" + sep + "TEST 4:execTimeout");
    results = Exec.execTimeout("ping -t 127.0.0.1", 5 * 1000);
    System.out.println(results);
    System.out.println("-------------------------------------------" + sep + "TEST 5:execLazy");
    results = Exec.execLazy("ping -t 127.0.0.1");
    System.out.println(results);
    System.out.println("-------------------------------------------" + sep
        + "TEST 6:ExecTimeout process never outputs");
    results = Exec.execTimeout("c:/swarm-2.1.1/bin/sleep.exe 20", 5 * 1000);
    System.out.println(results);
    System.out.println("-------------------------------------------" + sep
        + "TEST 7:ExecTimeout process waits for input");
    results = Exec.execTimeout("c:/swarm-2.1.1/bin/cat", 5 * 1000);
    System.out.println(results);
  }
}

/***************************************************************************************************
 * 
 * 
 * This file is part of WebGoat, an Open Web Application Security Project utility. For details,
 * please see http://www.owasp.org/
 * 
 * Copyright (c) 2002 - 2007 Bruce Mayhew
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * 
 * Getting Source 
 * 
 * Source for this application is maintained at code.google.com, a repository for free software
 * projects.
 * 
 * For details, please see http://code.google.com/p/webgoat/
 * 
 * @author Jeff Williams <a href="http://www.aspectsecurity.com">Aspect Security</a>
 */
 class ExecResults
{

  /**
   * Description of the Field
   */
  public final static int BADRETURNCODE = 2;

  /**
   * Description of the Field
   */
  public final static int THROWABLE = 1;

  private String myCommand;

  private boolean myError = false;

  private int myErrorType = 0;

  private String myErrors = null;

  private String myInput;

  private boolean myInterrupted = false;

  private String myOutput = null;

  private int myReturnCode = 0;

  private int mySuccessCode;

  private Throwable myThrowable = null;

  private int myTimeout;

  /**
   * Constructor for the ExecResults object
   * 
   * @param command
   *            Description of the Parameter
   * @param input
   *            Description of the Parameter
   * @param successCode
   *            Description of the Parameter
   * @param timeout
   *            Description of the Parameter
   */
  public ExecResults(String command, String input, int successCode, int timeout)
  {
    myCommand = command.trim();
    myInput = input.trim();
    mySuccessCode = successCode;
    myTimeout = timeout;
  }

  /**
   * Description of the Method
   * 
   * @param haystack
   *            Description of the Parameter
   * @param needle
   *            Description of the Parameter
   * @param fromIndex
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  private boolean contains(String haystack, String needle, int fromIndex)
  {
    return (haystack.trim().toLowerCase().indexOf(needle.trim().toLowerCase(), fromIndex) != -1);
  }

  /**
   * Description of the Method
   * 
   * @param value
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public boolean errorsContains(String value)
  {
    return (errorsContains(value, 0));
  }

  /**
   * Description of the Method
   * 
   * @param value
   *            Description of the Parameter
   * @param fromIndex
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public boolean errorsContains(String value, int fromIndex)
  {
    return (contains(myErrors, value, fromIndex));
  }

  /**
   * Gets the error attribute of the ExecResults object
   * 
   * @return The error value
   */
  public boolean getError()
  {
    return (myError);
  }

  /**
   * Gets the errorMessage attribute of the ExecResults object
   * 
   * @return The errorMessage value
   */
  public String getErrorMessage()
  {
    switch (getErrorType())
    {
      case THROWABLE:
        return ("Exception: " + myThrowable.getMessage());

      case BADRETURNCODE:
        return ("Bad return code (expected " + mySuccessCode + ")");

      default:
        return ("Unknown error");
    }
  }

  /**
   * Gets the errorType attribute of the ExecResults object
   * 
   * @return The errorType value
   */
  public int getErrorType()
  {
    return (myErrorType);
  }

  /**
   * Gets the errors attribute of the ExecResults object
   * 
   * @return The errors value
   */
  public String getErrors()
  {
    return (myErrors);
  }

  /**
   * Gets the interrupted attribute of the ExecResults object
   * 
   * @return The interrupted value
   */
  public boolean getInterrupted()
  {
    return (myInterrupted);
  }

  /**
   * Gets the output attribute of the ExecResults object
   * 
   * @return The output value
   */
  public String getOutput()
  {
    return (myOutput);
  }

  /**
   * Gets the returnCode attribute of the ExecResults object
   * 
   * @return The returnCode value
   */
  public int getReturnCode()
  {
    return (myReturnCode);
  }

  /**
   * Gets the throwable attribute of the ExecResults object
   * 
   * @return The throwable value
   */
  public Throwable getThrowable()
  {
    return (myThrowable);
  }

  /**
   * Description of the Method
   * 
   * @param value
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public boolean outputContains(String value)
  {
    return (outputContains(value, 0));
  }

  /**
   * Description of the Method
   * 
   * @param value
   *            Description of the Parameter
   * @param fromIndex
   *            Description of the Parameter
   * @return Description of the Return Value
   */
  public boolean outputContains(String value, int fromIndex)
  {
    return (contains(myOutput, value, fromIndex));
  }

  /**
   * Sets the error attribute of the ExecResults object
   * 
   * @param value
   *            The new error value
   */
  public void setError(int value)
  {
    myError = true;
    myErrorType = value;
  }

  /**
   * Sets the errors attribute of the ExecResults object
   * 
   * @param errors
   *            The new errors value
   */
  public void setErrors(String errors)
  {
    myErrors = errors.trim();
  }

  /**
   * Sets the interrupted attribute of the ExecResults object
   */
  public void setInterrupted()
  {
    myInterrupted = true;
  }

  /**
   * Sets the output attribute of the ExecResults object
   * 
   * @param value
   *            The new output value
   */
  public void setOutput(String value)
  {
    myOutput = value.trim();
  }

  /**
   * Sets the returnCode attribute of the ExecResults object
   * 
   * @param value
   *            The new returnCode value
   */
  public void setReturnCode(int value)
  {
    myReturnCode = value;
  }

  /**
   * Sets the throwable attribute of the ExecResults object
   * 
   * @param value
   *            The new throwable value
   */
  public void setThrowable(Throwable value)
  {
    setError(THROWABLE);
    myThrowable = value;
  }

  /**
   * Description of the Method
   * 
   * @return Description of the Return Value
   */
  public String toString()
  {
    String sep = System.getProperty("line.separator");
    StringBuffer value = new StringBuffer();
    value.append("ExecResults for \'" + myCommand + "\'" + sep);

    if ((myInput != null) && !myInput.equals(""))
    {
      value.append(sep + "Input..." + sep + myInput + sep);
    }

    if ((myOutput != null) && !myOutput.equals(""))
    {
      value.append(sep + "Output..." + sep + myOutput + sep);
    }

    if ((myErrors != null) && !myErrors.equals(""))
    {
      value.append(sep + "Errors..." + sep + myErrors + sep);
    }

    value.append(sep);

    if (myInterrupted)
    {
      value.append("Command timed out after " + (myTimeout / 1000) + " seconds " + sep);
    }

    value.append("Returncode: " + myReturnCode + sep);

    if (myError)
    {
      value.append(getErrorMessage() + sep);
    }

    return (value.toString());
  }
}
//
// /***************************************************************************************************
//  * 
//  * 
//  * This file is part of WebGoat, an Open Web Application Security Project utility. For details,
//  * please see http://www.owasp.org/
//  * 
//  * Copyright (c) 2002 - 2007 Bruce Mayhew
//  * 
//  * This program is free software; you can redistribute it and/or modify it under the terms of the
//  * GNU General Public License as published by the Free Software Foundation; either version 2 of the
//  * License, or (at your option) any later version.
//  * 
//  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
//  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//  * General Public License for more details.
//  * 
//  * You should have received a copy of the GNU General Public License along with this program; if
//  * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
//  * 02111-1307, USA.
//  * 
//  * Getting Source 
//  * 
//  * Source for this application is maintained at code.google.com, a repository for free software
//  * projects.
//  * 
//  * For details, please see http://code.google.com/p/webgoat/
//  * 
//  * @author jwilliams@aspectsecurity.com
//  * @created November 6, 2002
//  */
//  class ThreadWatcher implements Runnable
// {
//
//   // time to live in milliseconds
//   private BitSet myInterrupted;
//
//   private Process myProcess;
//
//   private int myTimeout;
//
//   /**
//    * Constructor for the ThreadWatcher object
//    * 
//    * @param p
//    *            Description of the Parameter
//    * @param interrupted
//    *            Description of the Parameter
//    * @param timeout
//    *            Description of the Parameter
//    */
//   public ThreadWatcher(Process p, BitSet interrupted, int timeout)
//   {
//     myProcess = p;
//
//     // thread used by whoever constructed this watcher
//     myTimeout = timeout;
//     myInterrupted = interrupted;
//   }
//
//   /*
//    * Interrupt the thread by marking the interrupted bit and killing the process
//    */
//
//   /**
//    * Description of the Method
//    */
//   public void interrupt()
//   {
//     myInterrupted.set(0);
//
//     // set interrupted bit (bit 0 of the bitset) to 1
//     myProcess.destroy();
//
//     /*
//      * try { myProcess.getInputStream().close(); } catch( IOException e1 ) { / do nothing --
//      * input streams are probably already closed } try { myProcess.getErrorStream().close(); }
//      * catch( IOException e2 ) { / do nothing -- input streams are probably already closed }
//      * myThread.interrupt();
//      */
//   }
//
//   /**
//    * Main processing method for the ThreadWatcher object
//    */
//   public void run()
//   {
//     try
//     {
//       Thread.sleep(myTimeout);
//     } catch (InterruptedException e)
//     {
//       // do nothing -- if watcher is interrupted, so is thread
//     }
//
//     interrupt();
//   }
// }
