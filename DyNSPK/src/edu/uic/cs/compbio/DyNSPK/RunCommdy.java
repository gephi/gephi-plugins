/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.compbio.DyNSPK;

/**
 *
 * @author zitterbewegung
 */
// file RunSqPy.java

    import org.python.util.PythonInterpreter;
    import org.python.core.*;

    public class RunCommdy {
        static public void main(String[] args) throws PyException {

            // instantiate the interpreter
            PythonInterpreter interp = new PythonInterpreter();

            // set runtime args as Jython global variables
            interp.set("num", new PyInteger(2));

            // cause the Python program to be run (a fully
            // qualified pathname is allowed)
            interp.execfile("sq.py");

            // get values
            PyObject sq  = interp.get("square");
            PyObject num = interp.get("num");

            // ...and do with them as you please (you may need
            // to cast them depending on usage)
            System.out.println("square of " + num + " is " + sq);
        }
    }
