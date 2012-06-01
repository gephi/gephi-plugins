#!/usr/bin/python 

import sys
#import os,sys,operator,glob
#from util import *
#from UnionFind2 import UnionFind

debug = False
#debug = True

class GlpxOutput:

    def __init__(self, out_fname):
        self.out_fname = out_fname
        # open gtm file
        fp = open(out_fname, 'r')
        it = iter(fp)
        line_no = 0

        # read solution attributes
        for line in it:
            line_no+=1
            line = line.strip()
            if line=="": break;
            k,v = line.split(":")
            v = v.strip()
            if k=="Problem":
                self.problem=v
            elif k=="Rows":
                self.rows=v
            elif k=="Columns":
                self.columns=v
            elif k=="Non-zeros":
                self.nonzeros=v
            elif k=="Status":
                self.status=v
            elif k=="Objective":
                self.obj_name, tmp, self.obj_value, self.obj_dir = v.split()
                self.obj_value = float(self.obj_value)
                self.obj_dir = self.obj_dir.strip('(').strip(')')
            else:
                raise Exception("Error parsing line %d: %s"%(line_no, line))

        # skip equations
        for line in it:
            line_no+=1
            line = line.strip()
            if line=="": break;

        line = it.next() # skip header
        #print line
        line = it.next() # skip separater
        # read solutions
        self.edges = []
        for line in it:
            line_no+=1
            line = line.strip()
            if line=="": break;
            f = line.split()
            if len(f)<4:
                line += it.next().strip()
                line = line.strip()
                f = line.split()
            edge = f[1]
            i = edge.find("[")
            j = edge.find("]", i)
            i,j = edge[i+1:j].split(',')
            i = int(i)
            j = int(j)
            value = int(f[3])
            if value==0: continue
            #print line
            #print i, j, value
            #print
            self.edges.append((i, j))

        # debug output
        #print self

    def __str__(self):
        rc = "Problem: " + self.problem + "\n"
        rc += "Direction: " + self.obj_dir + "\n"
        rc += "Value: " + self.obj_value + "\n"
        rc += "Status: " + self.status + "\n"
        rc += "Edges:"
        for i,j in self.edges:
            rc += " (%d,%d)"%(i,j)
        return rc

if __name__ == "__main__": 
    output = GlpxOutput(sys.argv[1])
    print output
