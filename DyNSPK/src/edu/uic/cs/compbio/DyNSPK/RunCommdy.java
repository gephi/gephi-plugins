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
/*
 * .PHONY: all clean
.SECONDARY:
script:=../script
src:=../src

all: sw4_ipc-c111.png sw4_bb5-c111.png

%.png: %.color2
	color3dot.py $<

%_ipc-c111.color2: %_ipc.gcolor %.gtm $(src)/color_ind2
	color_ind2 -cost 111 $*.gtm <$< >$@
	
%_bb5-c111.color2: %_bb5-c111.gcolor %.gtm $(src)/color_ind2
	color_ind2 -cost 111 $*.gtm <$< >$@
	
%_ipc.gcolor: %_ipc.out
	gamsout2color.py $< >$@

%_bb5-c111.gcolor: %.gtm $(src)/bb5
	$(src)/bb5 $*.gtm -cost 111 > $*_bb5-c111.log
	$(script)/bb5_get_min_gcolor.sh $*_bb5-c111.log > $@

%_ipc.out: %.gtm
	$(script)/ipc-glpk.py $<

%.gtm:
	test -s $@
	
$(src)/%:
	test -x $@ || make -C $(src) $*

clean:
	rm -rf *.out *.gcolor *.color2 *.png *.log

 */
    public class RunCommdy {
        static public void main(String[] args) throws PyException {

            // instantiate the interpreter
            PythonInterpreter interp = new PythonInterpreter();

            // set runtime args as Jython global variables
            interp.set("num", new PyInteger(2));

            // cause the Python program to be run (a fully
            // qualified pathname is allowed)
            interp.execfile("color3dot.py");

            // get values
            PyObject sq  = interp.get("square");
            PyObject num = interp.get("num");

            // ...and do with them as you please (you may need
            // to cast them depending on usage)
            System.out.println("square of " + num + " is " + sq);
        }
    }
