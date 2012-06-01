#!/usr/bin/python 

# groups in GTM >=1

import os,sys,operator,glob
from util import *
#from UnionFind2 import UnionFind

debug = False
#debug = True

def is_gtm_fname(str):
    return str.endswith(".gtm") or str.endswith(".gtm2") or \
            str.endswith(".fake_gtm") or str.endswith(".fake_gtm2")

def guess_gtmfile():
    guess =  os.path.basename(os.getcwd()) + ".gtm"
    #sys.stderr.write("guess %s\n" % guess)
    return guess

def guess_gtmfile2(clue="", verbose=False):
    match_count = []
    prefix = os.path.dirname(clue)
    if prefix!="": prefix += os.path.sep
    file_list = glob.glob(prefix + '*.gtm')
    file_list.extend(glob.glob(prefix + '*.gtm2'))
    file_list.sort()
    match_count = [ 0 for x in xrange(len(file_list)) ]
    for findex, filename in enumerate(file_list):
        for i in range(min(len(clue), len(filename))):
            if clue[i] == filename[i]:
                match_count[findex]+=1
            else:
                break
    if max(match_count)>0:
        guess = file_list[match_count.index(max(match_count))]
        if verbose: print>>sys.stderr, "GTM file:", guess
        return guess
    else:
        #sys.stderr.write("clue %s\n"%clue)
        #for i in xrange(len(file_list)):
        #   sys.stderr.write("%s : %d\n"%(file_list[i], match_count[i]))
        raise Exception("Cannot guess GTM filename from '%s'"%clue)

def guess_gtmfile3(clue=""):
    prefix = clue.split('.')[0].split('_')[0].split('-')[0]
    print "prefix", prefix
    if os.path.isfile(prefix + '.gtm'):
        return prefix + '.gtm'
    elif os.path.isfile(prefix + '.gtm2'):
        return prefix + '.gtm2'
    else:
        raise Exception("Cannot guess GTM filename from '%s'"%clue)

class GtmFile:

    def __init__(self, gtm_fname):
        if gtm_fname.endswith('.color2'):
            gtm_fname = guess_gtmfile2(gtm_fname)

        self._time_list = None
        self._i_t_group = None
        self.ind_count = None
        self.group_count = None
        self.time_count = None

        self.v_g = lambda g: "g%d"%(g)
        self.i_t = lambda i,t: "i%dt%d"%(i,t)
        self.i_g = lambda i,g: "i%dg%d"%(i,g)

        # open gtm file
        self.map = None
        self.time = {}
        self.group = {}
        inds = set()

        self.fname = gtm_fname
        f = open(gtm_fname, 'r')
        if gtm_fname.endswith("gtm2"):
            g=0
            t=1
            for line in f:
                x = line.strip()
                if len(x)==0:
                    t=t+1
                    continue
                if x[0]=='#': continue
                x = map(int, x.split())

                g=g+1
                #x.sort()

                if (t not in self.time):
                    self.time[t] = []

                self.time[t].append(g)
                self.group[g] = x
                for i in x:
                    inds.add(i)
        elif gtm_fname.endswith("gtm"):
            gcount=0
            for line in f:
                x = line.strip()
                if len(x)==0 or x.startswith('#'): continue
                try:
                    x = map(int, x.split())
                except ValueError:
                    sys.stderr.write("ERROR x: %s\n"%" ".join(x.split()))
                    raise
                if len(x)==0: continue
                gcount+=1
                g = x.pop(0)
                if gcount!=g: raise Exception("Gid not in order")
                t = x.pop(0)
                #x.sort()

                if (t not in self.time):
                    self.time[t] = []

                self.time[t].append(g)
                self.group[g] = x
                for i in x:
                    inds.add(i)

            for t,h in self.time.iteritems():
                for g in h: 
                    #self.group[g].sort()
                    pass
        else:
            raise "Unknown file extension:", gtm_fname

        self.inds = list(inds)
        self.inds.sort()
        self.times = self.time.keys()
        self.times.sort()
        self.groups = list(self.group.keys())
        self.groups.sort()

        self.ind_id = {}
        for id,i in enumerate(self.inds):
            self.ind_id[i] = id

        # set group time
        self.group_time=[]
        self.group_time.append(-1) # ignore group 0
        for t,h in self.time.iteritems():
            for g in h:
                if len(self.group_time)!=g:
                    print "ERROR len %d != g %d"%(len(self.group_time), g)
                    raise
                self.group_time.append(t)

        self.group_count = len(self.groups)
        self.ind_count = len(self.inds)
        self.time_count = len(self.times)

        self.it2idx = lambda i,t: self.group_count + (i-1)*self.time_count + t
        self.idx2it = lambda idx: \
            (int((idx-self.group_count-1)/self.time_count)+1,
            (idx-self.group_count-1)%self.time_count+1)
        self.d = self.group_count + self.ind_count*self.time_count
    
    def idx2vertex(self, idx):
        if idx<=self.group_count:
            return idx
        else:
            return self.idx2it(idx)

    def time_list(self):
        if self._time_list is None:
            self._time_list = self.time.keys()
            self._time_list.sort()
        return self._time_list
    
    def _build_i_t_group(self):
        self._i_t_group = {}
        for s,h in self.time.iteritems():
            for g in h:
                for j in self.group[g]:
                    if j not in self._i_t_group:
                        self._i_t_group[j]={}
                    self._i_t_group[j][s]=g

    def i_t_observed(self, i, t):
        if self._i_t_group is None: self._build_i_t_group()
        return (i in self._i_t_group) and (t in self._i_t_group[i])

    # indices start at 1, not 0
    def i_t_group(self, i, t):
        if self._i_t_group is None: self._build_i_t_group()
        if i not in self._i_t_group: return None
        if t not in self._i_t_group[i]: return None
        return self._i_t_group[i][t]
    
    def check(self):
        ok=True
        for t, h in self.time.iteritems():
            mygroup={}
            for g in h:
                for i in self.group[g]:
                    if i in mygroup:
                        sys.stderr.write(("ind %d is in both "+ \
                            "group %d and %d at time %d\n") \
                            %(i,mygroup[i], g, self.group_time[g]))
                        ok=False
                    mygroup[i]=g
        return ok

    def sort(self):
        nextgroup=1
        newgroup={}
        for t in self.time:
            newlist=[ (g,self.group[g]) for g in self.time[t] ]
            #newlist.sort(key=operator.itemgetter(1))
            for (g,members) in newlist:
                newgroup[nextgroup] = members
                nextgroup+=1
        self.group = newgroup

    def group_in_same_time_edges(self):
        time_groups = self.time
        group = self.group
        for t in self.times:
            for g,h in n_chooses_2(time_groups[t]):
                yield g,h

    def group_edges(self):
        time_groups = self.time
        times = self.times
        group = self.group
        for t1,t2 in n_chooses_2(times):
            for g in time_groups[t1]:
                gset = set(group[g])
                for h in time_groups[t2]:
                    hset = set(group[h])
                    inds = gset & hset
                    if len(inds)==0: continue
                    yield g,h
    
    def ind_edges(self):
        time_count = len(self.times)
        for i in self.inds:
            for t1 in xrange(1, time_count):
                for t2 in xrange(t1+1, time_count+1):
                    yield i, t1, i, t2

    def ind_ingroup_edges(self):
        time_groups = self.time
        times = self.times
        group = self.group
        for t in xrange(len(times)):
            for g in time_groups[times[t]]:
                for i in group[g]:
                    yield i, t+1, g

    def ind_notingroup_edges(self):
        time_groups = self.time
        times = self.times
        group = self.group
        all = set(self.inds)
        for t in xrange(len(times)):
            for g in time_groups[times[t]]:
                for i in sorted(all.difference(group[g])):
                    yield i, t+1, g

    def transitive_edges(self):
        time_groups = self.time
        times = self.times
        for i in self.inds:
            for t in self.times[:-1]:
                for g in time_groups[t]:
                    yield i, t+1, g
                for g in time_groups[t+1]:
                    yield i, t, g

    def iig_triangles(self):
        time_groups = self.time
        times = self.times
        group_time = self.group_time
        for g,h in self.group_edges():
            for i in self.inds:
                yield i,group_time[g], i, group_time[h], g
                yield i,group_time[g], i, group_time[h], h
    def igg_triangles(self):
        time_groups = self.time
        times = self.times
        group_time = self.group_time
        for g,h in self.group_edges():
            for i in self.inds:
                yield i,group_time[g], g, h
                yield i,group_time[h], g, h
    
    def groups_between(self, t1=0, t2=None):
        if t2 is None: t2 = self.time_count
        for t in xrange(t1, t2):
            for g in self.time[t+1]:
                yield g-1
    
    def ind_time_pairs(self, t1=0, t2=None):
        if t2 is None: t2 = self.time_count
        for i in xrange(self.ind_count):
            for t in xrange(self.time_count):
                yield i,t

    def time_ind_pairs(self, t1=0, t2=None):
        if t2 is None: t2 = self.time_count
        for t in xrange(self.time_count):
            for i in xrange(self.ind_count):
                yield t,i

    def convert_uf_to_gcolor(self, uf):
        color = {}
        gcolor = []
        for g in self.groups:
            h = uf.find(g)
            if h not in color:
                color[h] = len(color)+1
                #color[h] = g
            gcolor.append(color[h])
        return gcolor

    def compute_cost(self, gcolor, sw, ab, vi, icolors = None):
        assert len(gcolor) == self.group_count

        # check validity of group coloring
        for t,h in self.time.iteritems():
            for g,h in n_chooses_2(h):
                assert gcolor[g-1]!=gcolor[h-1], \
                    "g %d and h %d have same color %d at t %d"%(g, h, gcolor[g-1], t)

        if icolors is not None: del icolors[:]

        t_colors = []
        for t,h in self.time.iteritems():
            s = set()
            for g in h:
                s.add(gcolor[g-1])
            t_colors.append(s)

        total_cost = 0
            
        for i in self.inds:
            # find groups of i and their colors
            i_group = []
            i_gc = []
            for t in self.times:
                g = self.i_t_group(i, t)
                i_group.append(g)
                if g is None:
                    i_group[-1] = -1
                    i_gc.append(-1)
                else:
                    i_gc.append(gcolor[g-1])
            i_colors = set(i_gc)
            assert 0 not in i_colors
            if -1 in i_colors: i_colors.remove(-1)
            i_colors.add(0)
            i_colors = sorted(i_colors)
            c_count = len(i_colors)

            # min cost with min sw, ab, vi respectively
            curr_min_cost = []
            curr_min_sw_count = [ 0 for c in i_colors ]
            curr_min_ab_count = [ 0 for c in i_colors ]
            curr_min_vi_count = [ 0 for c in i_colors ]
            curr_min_color_sw = [ None for c in i_colors ]
            curr_min_color_ab = [ None for c in i_colors ]
            curr_min_color_vi = [ None for c in i_colors ]
            tc_min = [curr_min_cost]
            tc_min_sw_count = [curr_min_sw_count]
            tc_min_ab_count = [curr_min_ab_count]
            tc_min_vi_count = [curr_min_vi_count]
            tc_min_color_sw = [curr_min_color_sw]
            tc_min_color_ab = [curr_min_color_ab]
            tc_min_color_vi = [curr_min_color_vi]

            # base case: time t=0
            gc = i_gc[0]
            for c_index,c in enumerate(i_colors):
                base_cost = 0
                if c!=gc:
                    if gc>=0:
                        base_cost += vi
                        curr_min_vi_count[c_index]+=1
                    if c in t_colors[0]:
                        base_cost += ab
                        curr_min_ab_count[c_index]+=1
                curr_min_cost.append(base_cost)

            #print gc, tc_min[-1], tc_min_color[-1], sorted(t_colors[0])


            # time t=1 ... T-1
            for t in xrange(1, self.time_count):
                gc = i_gc[t]
                prev_min_cost = tc_min[t-1]
                prev_min_sw_count = tc_min_sw_count[t-1]
                prev_min_ab_count = tc_min_ab_count[t-1]
                prev_min_vi_count = tc_min_vi_count[t-1]
                curr_min_cost = []
                curr_min_color_sw = []
                curr_min_color_ab = []
                curr_min_color_vi = []
                curr_min_sw_count = []
                curr_min_ab_count = []
                curr_min_vi_count = []
                for c_index,c in enumerate(i_colors):
                    min_cost = 0x7FFFFFFF
                    base_cost = 0
                    base_ab_count = 0
                    base_vi_count = 0
                    min_d_sw = None
                    min_d_ab = None
                    min_d_vi = None
                    min_sw_count = None
                    min_ab_count = None
                    min_vi_count = None

                    if c!=gc:
                        if gc>=0:
                            base_cost += vi
                            base_vi_count += 1
                        if c in t_colors[t]:
                            base_cost += ab
                            base_ab_count += 1

                    for d_index,d in enumerate(i_colors):
                        cost = base_cost + prev_min_cost[d_index]
                        sw_count = prev_min_sw_count[d_index]
                        ab_count = prev_min_ab_count[d_index]
                        vi_count = prev_min_vi_count[d_index]
                        if c!=d:
                            cost += sw
                            sw_count += 1
                        if cost<min_cost:
                            min_cost=cost
                            min_d_sw = d_index
                            min_d_ab = d_index
                            min_d_vi = d_index
                            min_sw_count = sw_count
                            min_ab_count = ab_count
                            min_vi_count = vi_count
                        elif cost==min_cost:
                            if sw_count<min_sw_count:
                                min_d_sw = d_index
                                min_sw_count = sw_count
                            if ab_count<min_ab_count:
                                min_d_ab = d_index
                                min_ab_count = ab_count
                            if vi_count<min_vi_count:
                                min_d_vi = d_index
                                min_vi_count = vi_count
                    #assert min_d is not None
                    assert min_d_sw is not None
                    assert min_d_ab is not None
                    assert min_d_vi is not None
                    min_ab_count += base_ab_count
                    min_vi_count += base_vi_count
                    curr_min_cost.append(min_cost)
                    #curr_min_color.append(min_d)
                    curr_min_sw_count.append(min_sw_count)
                    curr_min_ab_count.append(min_ab_count)
                    curr_min_vi_count.append(min_vi_count)
                    curr_min_color_sw.append(min_d_sw)
                    curr_min_color_ab.append(min_d_ab)
                    curr_min_color_vi.append(min_d_vi)
                tc_min.append(curr_min_cost)
                #tc_min_color.append(curr_min_color)
                tc_min_color_sw.append(curr_min_color_sw)
                tc_min_color_ab.append(curr_min_color_ab)
                tc_min_color_vi.append(curr_min_color_vi)
                tc_min_sw_count.append(curr_min_sw_count)
                tc_min_ab_count.append(curr_min_ab_count)
                tc_min_vi_count.append(curr_min_vi_count)
                #print gc, tc_min[-1], tc_min_color[-1], sorted(t_colors[t])

            if debug: #debug
                print "ind", i, "gc", [ gc if gc>=0 else None for gc in i_gc ], 
                print "i_colors", i_colors
                for t in xrange(self.time_count):
                    print "t%-3d"%(t+1),
                    for c in xrange(len(i_colors)):
                        print "%3d"%tc_min[t][c], 
                    print "| gc %3d"%i_gc[t], 
                    print "| absent colors:",
                    print sorted(t_colors[t].difference(set([i_gc[t]]))),

                    print "|",
                    for c in xrange(len(i_colors)):
                        print "%3d"%tc_min_sw_count[t][c],
                    print "|",
                    for c in xrange(len(i_colors)):
                        print "%3d"%tc_min_ab_count[t][c],
                    print "|",
                    for c in xrange(len(i_colors)):
                        print "%3d"%tc_min_vi_count[t][c],

                    print

            # find min
            min_cost=0x7FFFFFFF
            min_c_sw=None; min_sw_count=0x7FFFFFFF
            min_c_ab=None; min_ab_count=0x7FFFFFFF
            min_c_vi=None; min_vi_count=0x7FFFFFFF
            last_min=tc_min[self.time_count-1]
            last_sw_count=tc_min_sw_count[self.time_count-1]
            last_ab_count=tc_min_ab_count[self.time_count-1]
            last_vi_count=tc_min_vi_count[self.time_count-1]
            for c_index in xrange(1, c_count):
                if min_cost>last_min[c_index]:
                    min_cost = last_min[c_index]
                    min_c_sw = c_index; min_sw_count=last_sw_count[c_index]
                    min_c_ab = c_index; min_ab_count=last_ab_count[c_index]
                    min_c_vi = c_index; min_vi_count=last_vi_count[c_index]
                elif min_cost==last_min[c_index]:
                    if min_sw_count>last_sw_count[c_index]:
                        min_sw_count=last_sw_count[c_index]
                        min_c_sw = c_index
                    if min_ab_count>last_ab_count[c_index]:
                        min_ab_count=last_ab_count[c_index]
                        min_c_ab = c_index
                    if min_vi_count>last_vi_count[c_index]:
                        min_vi_count=last_vi_count[c_index]
                        min_c_vi = c_index
            #assert min_c is not None
            assert min_c_sw is not None
            assert min_c_ab is not None
            assert min_c_vi is not None

            # pick a ties breaker
            min_count = min(min_sw_count, min_ab_count, min_vi_count)
            if min_sw_count==min_count:
                min_c = min_c_sw
                tc_min_color = tc_min_color_sw
                tie_breaker = "sw"
            elif min_vi_count==min_count:
                min_c = min_c_vi
                tc_min_color = tc_min_color_vi
                tie_breaker = "vi"
            else:
                min_c = min_c_ab
                tc_min_color = tc_min_color_ab
                tie_breaker = "ab"

            if debug:
                print "ind", i, ": break by", tie_breaker, 
                print min_sw_count, min_ab_count, min_vi_count, 
                print "min", min(min_sw_count, min_ab_count, min_vi_count)

            # trace back
            i_color = [i_colors[min_c]]
            for t in xrange(self.time_count-1, 0, -1):
                min_c = tc_min_color[t][min_c]
                i_color.append(i_colors[min_c])
            #print "ind", i, "min_cost", min_cost
            total_cost += min_cost
            if icolors is not None:
                i_color.reverse()
                icolors.append(i_color)
        #if icolors is not None:
        #   for ic in icolors:
        #       for c in ic:
        #           print c,
        #       print
        return total_cost

    def compute_opt_grp_color(self, ti_color, sw, ab, vi):
        import AssignmentProblem
        total_cost = 0
        coloring = []
        for t,groups in self.time.iteritems():
            c_size = {}
            colors = []
            #print "icolor", ti_color[t-1]
            for i in xrange(self.ind_count):
                c = ti_color[t-1][i]
                if c not in c_size:
                    c_size[c] = 0
                    colors.append(c)
                c_size[c]+=1
                if t<self.time_count and ti_color[t][i]!=ti_color[t-1][i]:
                    total_cost += sw # switching cost

            col_grp_cost = [[ 0 for g in groups] for c in c_size]
            #print "size", len(c_size), len(groups)
            for g,grp in enumerate(groups):
                g_size = len(self.group[grp])
                g_c_size = {}
                for i in self.group[grp]:
                    c = ti_color[t-1][i-1]
                    if c not in g_c_size:
                        g_c_size[c] = 0
                    g_c_size[c] += 1
                for c,col in enumerate(colors):
                    count = g_c_size[col] if col in g_c_size else 0
                    cost = vi*(g_size - count) + ab*(c_size[col] - count)

                    #print grp,col,(g_size-g_c_size[col]),(c_size[col]-g_c_size[col])
                    col_grp_cost[c][g] = cost
            #print "time", t
            #for c in col_grp_cost: print c
            cost,assign = AssignmentProblem.Solve(col_grp_cost)
            total_cost += cost
            #print "cost", ap
            #for c,g in assign:
            #    print "col", c, "group", g,
            #    print "cost", col_grp_cost[c][g], "inds", self.group[groups[g]]
            g_colors = [ 0 for g in groups ]
            for c,g in assign:
                g_colors[g]=colors[c]
            coloring += g_colors

        assert len(coloring) == self.group_count

        return total_cost, coloring

    def count_cost_unionfind(self, uf):
        v_g = self.v_g
        i_t = self.i_t
        gtm=self

        sw_count,ab_count,vi_count=0,0,0
        for i in self.inds:
            for t in gtm.times[1:]:
                if not uf.same(i_t(i,t-1), i_t(i,t)):
                    sw_count+=1
        for t in gtm.times:
            group_colors = set()
            for g in gtm.time[t]:
                group_colors.add(uf.find(v_g(g)))
            for g in gtm.time[t]:
                for i in gtm.inds:
                    if i in gtm.group[g]:
                        if not uf.same(i_t(i,t), v_g(g)):
                            vi_count+=1
                    elif uf.find(i_t(i,t)) in group_colors:
                        if uf.same(i_t(i,t), v_g(g)):
                            ab_count+=1

        return sw_count,ab_count,vi_count

if __name__ == "__main__": 
    
    import sys
    import re
    gtm_fname = None
    color_fname = None
    force=False
    for arg in sys.argv[1:]:
        p = re.compile('-[a-z]+')
        if p.match(arg):
            if arg=='-f':
                force=True
        else:
            if gtm_fname is None:
                gtm_fname = arg
            elif color_fname is None:
                color_fname = arg
            else:
                print 'Too many file names'
                exit(1)

    if gtm_fname is None:
        print 'A GTM file is not specified'
        exit(1)

    data = GtmFile(gtm_fname)
    ##data.sort()
    #for t, h in data.time.iteritems():
    #   for g in h:
    #       print g, t, " ".join(map(str, data.group[g]))
    ##data.check()

    try:
        f = open(gtm_fname[:-1])
    except IOError:
        f = None
    if f is None:
        f = open(gtm_fname[:-1], 'w')
    else:
        if not force:
            sys.stderr.write("File %s exists. Overwrite [y/n]? "%(gtm_fname[:-1]))
            answer = sys.stdin.readline().strip().lower()
        if force or answer == 'y':
            f = open(gtm_fname[:-1], 'w')
        else:
            f = None
    if f is not None:
        for t, h in data.time.iteritems():
            for g in h:
                f.write("%d %d %s\n"%(g,t, \
                    " ".join(map(str, data.group[g]))))

