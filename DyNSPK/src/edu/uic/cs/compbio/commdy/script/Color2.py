#!/usr/bin/python 

import sys
import inspect
from GtmFile import *

# read the first coloring
class Color2:

    #gtm = None

    def __init__(self, color2_fname, gtmdata=None, typesep=None):

        if gtmdata is None:
            gtmdata = GtmFile(color2_fname)
            assert gtmdata is not None

        self.gtm = gtmdata
        if color2_fname == '-':
            f = sys.stdin
        else:
            f = open(color2_fname, 'r')

        num = len(gtmdata.group) + len(gtmdata.inds)*len(gtmdata.times)

        line = f.readline()
        assert line != "", "First line in %s is empty"%color2_fname
        tmp = line.split()

        if typesep is None:
            try:
                x = int(tmp[0])
                typesep=(x==1)
            except ValueError:
                typesep=False

        #print "typesep", typesep
        if not typesep: # convert 1char to sep
            tmp2=[]
            for field in tmp:
                for c in field:
                    try:
                        x = int(c)
                    except ValueError:
                        raise Exception("Invalid color '%s'"%c)
                        x = ord(c) - ord('A')
                        if (x<0):
                            x = ord(c) - ord('a')
                        assert x>=0
                        x+=10
                    tmp2.append(x)
            tmp = tmp2

        if len(tmp)==0:
            self.group_color = None
            self.ind_color = None
            #self.max_exist_color = None
        else:
            if len(tmp)<num:
                raise Exception, "coloring %s is shorter than gtm file. " \
                    "%d colors while %d objects" %(color2_fname, len(tmp), num)
            
            tmp = map(int, tmp)
            self.max_exist_color = max(tmp);

            assert len(tmp) == gtmdata.group_count + \
                gtmdata.ind_count*gtmdata.time_count, \
                "len in color file %d != len in gtm file %d" %\
                    (len(tmp), gtmdata.group_count + \
                    gtmdata.ind_count*gtmdata.time_count)

            offset=len(gtmdata.group)
            time_count = len(gtmdata.times)
            self.group_color = tmp[0:offset]
            self.ind_color = {}
            for i in gtmdata.inds:
                self.ind_color[i-1] = tmp[offset:offset+time_count]
                offset+=time_count
            
            if offset!=len(tmp):
                raise Exception, "coloring is longer than gtm file. " \
                    "len(tmp)==%d while offset==%d" %(len(tmp), offset)

    def compute_cost(self, switch_cost, absence_cost, visit_cost):
        time = self.gtm.time
        group = self.gtm.group
        inds = self.gtm.inds
        times = self.gtm.times
        group_color = self.group_color
        ind_color = self.ind_color

        switch_count=0
        absence_count=0
        visit_count=0

        for i in ind_color:
            i_color = ind_color[i]
            for index,c in enumerate(i_color):
                if index==0: continue
                if c != i_color[index-1]:
                    switch_count+=1

        for index,t in enumerate(times):
            # add absence costs
            for g in time[t]:
                gc = group_color[g-1]
                for i,i_color in ind_color.iteritems():
                    if i_color[t-1]==gc:
                        absence_count+=1
            
            # add visit costs/remove absence costs
            for g in time[t]:
                gc = group_color[g-1]
                for i in group[g]:
                    if ind_color[i-1][t-1]!=gc:
                        visit_count+=1
                    else:
                        absence_count-=1
            

        cost = switch_count * switch_cost
        cost += absence_count * absence_cost
        cost += visit_count * visit_cost

        return switch_cost, absence_cost, visit_cost, \
            switch_count, absence_count, visit_count, \
            cost

    def toTicolor(self):
        ind_count = self.gtm.ind_count 
        time_count = self.gtm.time_count
        ticolor = [ [0]*ind_count for t in xrange(time_count) ]
        ind_color = self.ind_color
        for i in xrange(ind_count):
            for t in xrange(time_count):
                ticolor[t][i] = ind_color[i][t]
        return ticolor


class Color2Item:
    group_color = None
    ind_color = None
    def __init__(self, fields=None, gtm=None):
        if fields is None: return
        self.gtm = gtm
        group_count = len(gtm.groups)
        ind_count = len(gtm.inds)
        time_count = len(gtm.times)
        self.group_color = []
        it = iter(fields)
        for g in xrange(group_count):
            self.group_color.append(it.next())
        #self.group_color = fields[0:group_count]
        self.ind_color = []
        for i in xrange(ind_count):
            icolor = []
            for t in xrange(time_count):
                #icolor.append(fields[group_count + i*time_count + t])
                icolor.append(it.next())
            self.ind_color.append(icolor)

    @staticmethod
    def clone(copy): # clone
        self = Color2Item()
        self.gtm = copy.gtm
        self.group_color = list(copy.group_color)
        self.ind_color = list(copy.ind_color)
        for i in xrange(len(self.ind_color)):
            self.ind_color[i] = list(self.ind_color[i])
        return self

    def __str__(self):
        g = " ".join(map(str, self.group_color))
        i = " ".join([ " ".join(map(str, icolor)) for icolor in self.ind_color ])
        return g + " " + i
        #return " ".join(map(str, self.ind_color[0]))
    
    def __getitem__(self, i):
        return self.ind_color[i]

    def recolor_by_connected_components(self):
        from unionfind import UnionFind
        uf = UnionFind()

        for t in self.gtm.times:
            for g in self.gtm.time[t]:
                uf.find(g)
            for i in self.gtm.inds:
                uf.find((i,t))

            for g in self.gtm.time[t]:
                for i in self.gtm.group[g]:
                    if self.group_color[g-1]==self.ind_color[i-1][t-1]:
                        uf.union(g, (i,t))
                        leader = uf.find(g)
            if t>1:
                for i in self.gtm.inds:
                    if self.ind_color[i-1][t-1]==self.ind_color[i-1][t-2]:
                        uf.union((i,t-1), (i,t))
                        leader = uf.find((i,t-1))

        new_color = {}
        for t in self.gtm.times:
            for g in self.gtm.time[t]:
                leader = uf.find(g)
                if leader not in new_color:
                    new_color[leader] = len(new_color)+1
            for i in self.gtm.inds:
                leader = uf.find((i,t))
                if leader not in new_color:
                    new_color[leader] = len(new_color)+1

        for g in self.gtm.groups:
            self.group_color[g-1] = new_color[uf.find(g)]
        for i in self.gtm.inds:
            for t in self.gtm.times:
                self.ind_color[i-1][t-1] = new_color[uf.find((i,t))]
    
# read all colorings in the file
class Color2List:

    gtm = None
    group_count = None
    ind_count = None
    time_count = None

    def __init__(self, color2_fname, gtmdata, typesep=None,
            line_start=0, line_end=0x7FFFFFFF):

        self.color_list = []
        self.gtm = gtmdata
        if color2_fname == '-':
            f = sys.stdin
        else:
            f = open(color2_fname, 'r')

        self.group_count = len(gtmdata.groups)
        self.ind_count = len(gtmdata.inds)
        self.time_count = len(gtmdata.times)
        num = self.group_count + self.ind_count * self.time_count

        #print "strip"
        line = f.readline().strip()
        #print "split"
        fields = line.split()
        #print "done"

        # detect type
        if typesep is None:
            if len(fields)==num:
                typesep = True
            elif len(fields)==self.ind_count+1:
                typesep = False
            elif len(fields)==0:
                raise Exception("Color file is empty")
            else:
                print>>sys.stderr, "gtmfile:", gtmdata.fname
                raise Exception("Color file is not compatible with " + \
                "the GTM file\n"+ \
                "group_count %d ind_count %d time_count %d NF %d != %d"%\
                (self.group_count, self.ind_count, self.time_count, num, \
                len(fields)))
        if typesep:
            def sep_tokenizer(line):
                length = len(line)
                #print "tokenizing line: length", length
                i=0
                while i<length:
                    j=i
                    while j<length and line[j]!=' ': j+=1
                    if i<j:
                        yield int(line[i:j])
                    i=j+1
                #return map(int, line.split())
            tokenize = sep_tokenizer
        else:
            def char_tokenizer(line):
                for c in line:
                    if c==' ':
                        continue
                    elif ord('0')<=ord(c) and ord(c)<=ord('9'):
                        yield ord(c) - ord('0')
                    elif ord('a')<=ord(c) and ord(c)<=ord('z'):
                        yield ord(c) - ord('a')
                    elif ord('A')<=ord(c) and ord(c)<=ord('Z'):
                        yield ord(c) - ord('A')
                    else:
                        raise Exception("ERROR converting char '%c' to color" % c)
            tokenize = char_tokenizer

        # skip some of the first lines
        line_no=0
        while (line_no<line_start):
            f.readline()
            line_no+=1

        if len(line)==0 or line_start+line_no>=line_end: return
        self.color_list.append(Color2Item(tokenize(line), gtmdata))

        while f:
            line = f.readline().strip()
            line_no+=1
            if len(line)==0 or line_start+line_no>=line_end: return
            self.color_list.append(Color2Item(tokenize(line), gtmdata))

    def __len__(self):
        return len(self.color_list)
    def __getitem__(self, i):
        return self.color_list[i]


if __name__ == "__main__": 
    
    import sys
    import re
    from GtmFile import GtmFile

    gtm_fname = None
    color_fname = None

    for arg in sys.argv[1:]:
        p = re.compile('-[a-z]+')
        if p.match(arg):
            pass
        else:
            if arg.endswith(".gtm"):
                gtm_fname = arg
            elif arg.endswith(".color2"):
                color_fname = arg
            else:
                raise Exception('Invalid file extension: %s'%arg)

    if gtm_fname is None:
        gtm_fname = guess_gtmfile2(color_fname)
        sys.stderr.write("guessing %s\n"%gtm_fname)
    gtm = GtmFile(gtm_fname)
    color_list = Color2List(color_fname, gtm)
    color = color_list[0]

    color2 = Color2Item.clone(color)
    color2.recolor_by_connected_components()
    print color
    print color2

