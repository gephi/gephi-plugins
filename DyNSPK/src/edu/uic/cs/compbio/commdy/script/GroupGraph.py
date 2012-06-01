import sys,operator,threading

class PrintStatus(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self._finished = threading.Event()
        self._interval = 5
        self.percent=0
    
    def shutdown(self):
        self._finished.set()
    def run(self):
        while 1:
            if self._finished.isSet(): return
            if self.percent>=1:
                sys.stderr.write("creating group graph ... %.2g%%\n"%self.percent)
            self._finished.wait(self._interval)

class GroupGraph:
    def __init__(self, gtm, debug=False):

        # create group graph
        group = dict(gtm.group)
        groups = gtm.groups
        self.group = group
        self.group_count = len(groups)
        self.edges = []
        self.edgemember = []
        self.in_neighbors = None
        self.out_neighbors = None

        if (debug): sys.stderr.write("finding first group\n")

        # find first/last group of i
        self.ifirstgroup = ifirstgroup = {}
        self.ilastgroup = ilastgroup = {}
        for g in range(1, self.group_count+1):
            for i in group[g]:
                if i not in ifirstgroup: ifirstgroup[i] = g
                ilastgroup[i] = g
        #print "first", ifirstgroup
        #print "last", ilastgroup

        beginw = {} # dummy groups at the first time step
        endw = {}    # dummy groups at the last time step
        time_count = len(gtm.times)
        assert gtm.times[-1]<=time_count
        group_time = list(gtm.group_time)
        for i in gtm.inds:
            g = ifirstgroup[i]
            if group_time[g]>1:
                if g not in beginw: beginw[g] = set()
                beginw[g].add(i)
            else:
                assert group_time[g]==1, \
                    "group_time %d time_count %d"%(group_time[g], time_count)
            g = ilastgroup[i]
            if group_time[g]<time_count:
                if g not in endw: endw[g] = set()
                endw[g].add(i)
            else:
                assert group_time[g]==time_count, \
                    "group_time %d time_count %d"%(group_time[g], time_count)

        if (debug): sys.stderr.write("check beginw\n")
        # check beginw
        tmp=set()
        for g in gtm.time[gtm.times[0]]: tmp.update(group[g])
        for g,s in beginw.iteritems():
            if len(s.intersection(tmp))>1:
                raise Exception("len(s.intersection(tmp))>1")
            tmp.update(s)
        if len(tmp)!=len(gtm.inds):
            print>>sys.stderr, "tmp-inds =", sorted(tmp-set(gtm.inds))
            print>>sys.stderr, "inds-tmp =", sorted(set(gtm.inds)-tmp)
            raise Exception("len(tmp) %d !=len(gtm.inds) %d"%(len(tmp),len(gtm.inds)))
            
        if (debug): sys.stderr.write("check endw\n")
        # check endw
        tmp=set()
        for g in gtm.time[gtm.times[-1]]: tmp.update(group[g])
        for g,s in endw.iteritems():
            if len(s.intersection(tmp))>1:
                raise Exception("len(s.intersection(tmp))>1")
            tmp.update(s)
        if len(tmp)!=len(gtm.inds):
            print>>sys.stderr, "tmp-inds =", sorted(tmp-set(gtm.inds))
            print>>sys.stderr, "inds-tmp =", sorted(set(gtm.inds)-tmp)
            raise Exception("len(tmp)!=len(gtm.inds)")
            
        #print "begin", beginw
        #print "end", endw

        if (debug): sys.stderr.write("adding dummy groups\n")
        # add dummy groups
        self.lastdummy = self.group_count
        dummytime = 1
        for g in sorted(beginw): 
            self.lastdummy+=1
            self.edges.append((self.lastdummy,g,len(beginw[g])))
            self.edgemember.append(beginw[g])
            self.group[self.lastdummy]=beginw[g]
            group_time.append(dummytime)
        dummytime = len(gtm.times)
        for g in sorted(endw): 
            self.lastdummy+=1
            self.edges.append((g,self.lastdummy,len(endw[g])))
            self.edgemember.append(endw[g])
            self.group[self.lastdummy]=endw[g]
            group_time.append(dummytime)

        if (debug): sys.stderr.write("finishing up\n")

        self.groups = xrange(1, len(self.group.keys())+1)

        self.group_time=group_time
        self.groups_intime = list(self.groups)
        self.sortbytime(self.groups_intime)

        # add edges between real groups 
        #t = PrintStatus()
        #t.start()
        for g_index in range(self.group_count-1):
            g = self.groups[g_index]
            setg = set(group[g])
        #    t.percent=g*100.0/len(groups)
            for h_index in range(g_index+1, self.group_count):
                h = self.groups[h_index]
                try:
                    seth = set(group[h])
                    setgh = setg.intersection(seth)
                    if len(setgh)>0:
                        # found an edge g,h on which the members in setgh flows
                        w=len(setgh)
                        self.edges.append((g,h,w))
                        self.edgemember.append(setgh)
                        setg = setg.difference(setgh)
                        if len(setg)==0:
                            break
                except KeyError:
                    print "self.group_count", self.group_count
                    print "self.lastdummy", self.lastdummy
                    for k in group:
                        print k, group[k]
                    raise
        #t.shutdown()

        #for g,h,weight in self.edges:
        #    print g, h, weight
        

    def sortbytime(self, l):
        tmp = [ (g,self.group_time[g]) for g in l ]
        tmp.sort(key=operator.itemgetter(1, 0))
        for index,pair in enumerate(tmp):
            g,t=pair
            l[index]=g

    def is_dummy_group(self, g):
        if g<1 or g>self.lastdummy:
            raise ValueError("g %d is not in the range [1, %d]"%\
                (g, self.lastdummy))
        return g>self.group_count
    
    def find_neighbors(self):
        self.out_neighbors = {}
        self.in_neighbors = {}
        for v in self.groups:
            self.out_neighbors[v] = []
            self.in_neighbors[v] = []
        for e in self.edges:
            (u,v,weight) = e
            self.out_neighbors[u].append(v)
            self.in_neighbors[v].append(u)
        #for u in self.out_neighbors.keys():
        #    print u, ":",
        #    for v in self.out_neighbors[u]:
        #        print v,
        #    print

    def get_out_neighbors(self, u):
        if self.out_neighbors is None:
            self.find_neighbors()
        return self.out_neighbors[u]

    def get_in_neighbors(self, u):
        if self.in_neighbors is None:
            self.find_neighbors()
        return self.out_neighbors[u]

