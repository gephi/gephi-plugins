#!/usr/bin/python

# port of color2dot .c

import sys,re,math
import os.path
from subprocess import *
from util import *
from GtmFile import *
from GtmMap import *
from Color2 import *
from unionfind import UnionFind
from optparse import OptionParser
from types import *
from GraphViz import *
from DefaultPalette import *

#show_ind_label = "index"
show_ind_label = "map"
#show_ind_label = "both"
show_time_labels = False

def IsDarkColor(color):
    return color=="#FF0000" or color=="#000088"

palette = default_palette()

usage = "usage: %prog [options] <color files>"
parser = OptionParser(usage)
parser.add_option("-v", "--verbose",
        action="store_true", dest="verbose")
parser.add_option("-q", "--quiet",
        action="store_false", dest="verbose")
parser.add_option("--gtm", dest="gtm_fname",
        help="gtm filename to use")
parser.add_option("--map", dest="map_fname",
        help="map filename to use")
#parser.add_option("-o", "--output", dest="out_fname",
#       help="output filename (default stdout)")
parser.add_option("-T", dest="out_type",
        help="output type: png (default), dot, ps, pdf",
        choices = ["dot", "png", "ps", "pdf"], default="png")
parser.add_option("-c", dest="cost", default=111, 
        type="int", help="cost setting (sw,ab,vis)")
parser.add_option("--cost", dest="cost",
        type="int", nargs=3, help="cost setting (sw,ab,vis)")
parser.add_option("--ctype", dest="color_type",
        help="type of the input color file: sep or char", 
        choices = ["sep", "char"])
parser.add_option("--wsize", dest="wsize", 
        type="int", help="break time line into small windows")
parser.add_option("--woffset", dest="woffset", 
        type="int", help="break time line into small windows")
parser.add_option("-n", "--offset", dest="offset", default=1, 
        type="int", help="line number of color file to read")
parser.add_option("--whiteout", 
        action="store_true", dest="whiteout")
parser.add_option("--bw", 
        action="store_true", dest="bw")

(options, args) = parser.parse_args()

color_fnames = []
out_fnames = {}
gtm_fname = None
for arg in args:
    if is_gtm_fname(arg):
        gtm_fname = arg
    elif os.path.splitext(arg)[1] in ['png', 'pdf', 'ps', 'dot']:
        out_fnames.append(arg)
    else:
        color_fnames.append(arg)
if len(color_fnames)==0:
    parser.error("Specify a color file")

guess = gtm_fname is None
for input_index,color_fname in enumerate(color_fnames):
    if input_index<len(out_fnames):
        out_fname = out_fnames[input_index]
    else:
        out_fname = strip_ext(color_fname) + "." + options.out_type
    if guess: gtm_fname = guess_gtmfile2(color_fname, False)
    gtm = GtmFile(gtm_fname)
    try:
        gtm.map = GtmMap(gtm_fname)
    except IOError:
        gtm.map = None
    if gtm.map is None or gtm.map.fname is None:
        if show_ind_label == "map" or show_ind_label == "index":
            ind_label = lambda i,t: "%d"%(i)
            time_label = lambda t: "%d"%(t)
        elif show_ind_label == "both":
            # both index
            ind_label = lambda i,t: "%d"%(i)
            time_label = lambda t: "T%d"%(t)
        else:
            raise Exception("ERROR")
    else:
        id_ind = gtm.map.id_individual
        info = gtm.map.id_ind_info
        id_time = gtm.map.id_time

        if show_ind_label == "map":
            # from map file
            ind_label = lambda i,t: "%s%s"%(id_ind[i-1],info[i-1][t-1])
            time_label = lambda t: id_time[t-1]
        elif show_ind_label == "index":
            # by index
            ind_label = lambda i,t: "%s"%i
            time_label = lambda t: "T%s"%t
        elif show_ind_label == "both":
            # both index
            ind_label = lambda i,t: "%s%s\\n(%d)"%(id_ind[i-1],info[i-1][t-1], i)
            time_label = lambda t: "%s\\nT%d"%(id_time[t-1], t)
        else:
            raise Exception("ERROR")

    color_list = Color2List(color_fname, gtm, options.color_type)
    if options.offset>=1 and options.offset-1<len(color_list):
        color = color_list[options.offset-1]
    else:
        parser.error("Offset %d should be between %d and %d"\
                %(options.offset, 1, len(color_list)))
    time_window = -1,-1

    if False:
        print "// group color", 
        print "//", 
        for g in color.group_color: print g,
        print
        print "// ind color", 
        for i_color in color.ind_color:
            print "//", 
            for i in i_color:
                print i,
            print

    ## first and last times of individuals
    ind_first_time = {}
    ind_last_time = {}

    for t in gtm.times:
        for g in gtm.time[t]:
            for i in gtm.group[g]:
                if i not in ind_first_time: ind_first_time[i] = t
                ind_last_time[i] = t

    ### permutation
    perm = []
    if os.path.isfile("perm.txt"):
        f = open("perm.txt", "r")
        for line in f.readlines():
            if line.startswith("#"): continue
            i,j = map(int, line.strip().split(","))
            perm.append((i,j))
        sys.stderr.write("perm: ")
        sys.stderr.write(str(perm))
        sys.stderr.write("\n")

    # decrease by 1 and check range
    for index,p in enumerate(perm):
        i,t = p
        i-=1
        t-=1
        perm[index] = i,t
        assert 0<=i; assert i<gtm.ind_count
        assert 0<=t; assert t<gtm.time_count

    # fill in the rest
    specified = set(perm)
    for i,t in gtm.ind_time_pairs(*time_window):
        if (i,t) not in specified:
            perm.append((i,t))

    if options.whiteout:
    # replace singleton colors with white
        group_colors = set(color.group_color)
        for i,t in gtm.ind_time_pairs():
            if color.ind_color[i][t] not in group_colors:
                color.ind_color[i][t] = 0

    # assign new color based on priority given in perm
    new_color = { 0:0 }
    next_color = 1
    for p in perm:
        i,t = p
        try:
            c = color.ind_color[i][t]
        except IndexError:
            print "i", i, "t", t
            print len(color.ind_color)
            print len(color.ind_color[i])
            exit(0)
        if c not in new_color:
            new_color[c] = next_color
            next_color += 1
    for g in gtm.groups:
        c = color.group_color[g-1]
        if c not in new_color:
            new_color[c] = next_color
            next_color += 1

    # overwrite with identity map
    #for c in xrange(next_color):
    #   new_color[c] = c
    
    # triple it for onw
    org_len = len(palette)
    for i in xrange(1, org_len): palette.append(palette[i])
    for i in xrange(1, org_len): palette.append(palette[i])
    
    # check number of color
    assert max(new_color.keys())<len(palette), \
        "%d colors are not enough. need %d"%(len(palette), max(new_color.keys()))
    # check bijection
    assert len(new_color.keys()) == len(set(new_color.values())), \
        "#keys %d != #values %d"%(len(new_color.keys()), len(set(new_color.values())))

    ###
    dg = Digraph('"%s"'%color_fname)
    #dg.addGraphProperty("ranksep", "0.5")
    dg.addGraphProperty("ranksep", "1")
    dg.addNodeProperties({
        #"fontname":"Sans Serif", 
        "fontname":"Times-Roman", 
        "fontsize":12,
        #"fontsize":24,
        "fixedsize":True, "margin":0 })
    #main = dg.addSubgraph("cluster_main")
    main = dg
    main.addNodeProperty("shape", "circle")
    main.addNodeProperty("style", "filled")
    main.addNodeProperty("fillcolor", "white")
    main.addNodeProperty("width", ".5")
    main.addNodeProperty("height", ".5")
    
    cost_color = "red"
    cost_linewidth = 3
    ###
    time_inds = []
    for t in gtm.times:
        t_inds = set()
        time_inds.append(t_inds)
    
        # time label
        if show_time_labels:
            n = main.addNode("T%s"%t, time_label(t))
            n.setShape("none").setFixedSize(False)
    
        # observed individuals
        #b = main.addBlock().addProperty("rank", "same")
        b = main
        exist_colors = set()
        for g in gtm.time[t]:
            exist_colors.add(color.group_color[g-1])

        for g in gtm.time[t]:
            cluster = b.addSubgraph("cluster_%d"%g)
            #cluster.setColor("black")
            if not options.bw:
                cluster.setFillColor(palette[new_color[color.group_color[g-1]]])
                cluster.setStyle("filled")
            cluster.setShape("box")
            #sys.stderr.write(" ".join(map(str, gtm.group[g])))
            #sys.stderr.write("\n")
            for i in gtm.group[g]:
                t_inds.add(i)
                n = cluster.addNode("%s_%s"%(i,t), ind_label(i, t))

                assert 1<=i and i<=len(color.ind_color), "i %d len %d"%(i, len(color.ind_color))
                assert 1<=t and t<=len(color.ind_color[i-1]), \
                        "t %d len %d"%(t, len(color.ind_color[i-1]))
                color_str = palette[new_color[color.ind_color[i-1][t-1]]]
                if not options.bw:
                    n.setFillColor(color_str)
                    if (IsDarkColor(color_str)):
                        n.setColor("white")
                        n.setFontColor("white")

                if color.ind_color[i-1][t-1]!=color.group_color[g-1]:
                    #print>>sys.stderr,"it", i, t, color.ind_color[i-1][t-1], 
                    #print>>sys.stderr,"g", g, color.group_color[g-1]
                    n.setStyle("filled")
                    n.setColor(cost_color).setLineWidth(cost_linewidth)
                    if color.ind_color[i-1][t-1] in exist_colors:
                        n.setShape("diamond")
                        cluster.setColor(cost_color)

        # unobserved individuals
        #b = main
        b = main.addBlock()
        b.addNodeProperty("shape", "diamond")
        b.addNodeProperty("width", "0.2")
        b.addNodeProperty("height", "0.2")
        for i in gtm.inds:
            if i in t_inds: continue
            if t<ind_first_time[i]-1: continue
            if t>ind_last_time[i]+1: continue
            if color.ind_color[i-1][t-1] in exist_colors:
                sub = b.addSubgraph("cluster_i%dt%d"%(i,t))
                sub.setColor(cost_color)
                sub.setStyle("setlinewidth(3)")
                n = sub.addNode("%s_%s"%(i,t), "")
                if not options.bw:
                    n.setFillColor(palette[new_color[color.ind_color[i-1][t-1]]])
                    #n.setColor(cost_color)
                    #n.setStyle("filled,setlinewidth(3)")
            else:
                n = b.addNode("%s_%s"%(i,t), "")
                if not options.bw:
                    n.setFillColor(palette[new_color[color.ind_color[i-1][t-1]]])

    # add edges between consecutive times
    #b = main.addBlock()
    b = main
    b.addEdgeProperty("dir", "none")
    for t in xrange(1, gtm.time_count):
        for i in gtm.inds:
            if t<ind_first_time[i]-1: continue
            if t>ind_last_time[i]: continue
            e = b.addEdge("%s_%s"%(i,t), "%s_%s"%(i,t+1))
            if color.ind_color[i-1][t-1]!=color.ind_color[i-1][t]:
                e.setColor(cost_color).setLineWidth(cost_linewidth)
    
    if show_time_labels:
    # add edges between time labels
        b = main.addBlock()
        b.addEdgeProperty("style", "invis")
        for t in xrange(1, gtm.time_count):
            b.addEdge("T%s"%t, "T%s"%(t+1))
    
    if options.out_type in ["png", "ps", "pdf"]:
        cmd = ["dot"]
        cmd.append("-T%s"%options.out_type)
        cmd.append("-o %s"%out_fname)
        #cmd.append("2>&1 | grep -v ^Warning")
        p = Popen(" ".join(cmd), shell=True, \
            stdin=PIPE, stdout=PIPE, stderr=PIPE)
        fd = p.stdin
    else:
        p = None
        fd = sys.stdout

    dg.fprint(fd)

    if p is not None:
        out,err = p.communicate()
        if len(out)>0:
            for line in out.split(chr(10)):
                print line
        if len(err)>0:
            for line in err.split(chr(10)):
                if len(line)>0 and not line.startswith("Warning"):
                    print line
        fd.close()
        rc = p.wait()
        if rc != 0:
            raise Exception("ERROR dot returns %d"%rc)
        sys.stderr.write('open %s\n'%out_fname)

