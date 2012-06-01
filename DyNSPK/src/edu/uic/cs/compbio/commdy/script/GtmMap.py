#!/usr/bin/python 

import os,sys,operator,glob
from GtmFile import guess_gtmfile2

class GtmMap:

    #_time_list = None
    #_i_t_group = None

    def __init__(self, map_fname):
        # guess the filename from filename
        if map_fname.endswith(".color2"):
            map_fname = guess_gtmfile2(map_fname, False)
        if map_fname.endswith("gtm"):
            map_fname = map_fname[:-3] + "map"
        elif map_fname.endswith("gtm2"):
            map_fname = map_fname[:-4] + "map"

        self.time_id = {}
        self.group_id = {}
        self.individual_id = {}
        self.id_time = []
        self.id_group = []
        self.id_individual = []
        self.id_ind_info = []
        self.t_offset = 0
        self.i_offset = 0
        self.g_offset = 0

        if not os.path.isfile(map_fname):
            self.fname = None
            return
        self.fname = map_fname

        # open map file
        fd = open(map_fname, 'r')
        zone=None
        line_no = 0
        for line in fd.readlines():
            line_no += 1
            line = line.strip()
            #print line_no, ">>", line
            if line == "":
                zone=None
            elif line == "time -> new id":
                zone="time"
                self.t_offset=0
            elif line == "group -> new id":
                zone="group"
                self.g_offset=0
            elif line == "individual -> new id":
                zone="individual"
                self.i_offset=0
            elif zone is None:
                pass
            elif zone == "time":
                name,id = line.split()
                id = int(id)
                if id==0: self.t_offset=1
                id += self.t_offset
                self.time_id[name] = id
                if id>=0:
                    self.id_time.append(name)
                    assert len(self.id_time)==id
            elif zone == "group":
                name,id = line.split()
                id = int(id)
                if id==0: self.g_offset=1
                id += self.g_offset
                self.group_id[name] = id
                self.id_group.append(name)
                assert len(self.id_group)==id
            elif zone == "individual":
                name,id,info = line.split()
                id = int(id)
                if id==0: self.i_offset=1
                id += self.i_offset
                self.individual_id[name] = id
                self.id_individual.append(name)
                assert len(self.id_individual)==id
                if info=='|':
                    self.id_ind_info.append(None)
                else:
                    info = info.split('|')
                    #for x in range(len(info), len(self.id_time)):
                    #    info.append('')
                    self.id_ind_info.append(info)
            else:
                print "Unexpected error: zone is", zone
                raise
        fd.close()
        empty_label = [ "" for t in xrange(len(self.time_id))]
        for i in xrange(len(self.id_ind_info)):
            if self.id_ind_info[i] is None:
                self.id_ind_info[i] = empty_label

class DefaultMap:

    #_time_list = None
    #_i_t_group = None

    def __init__(self, gtm):

        self.time_id = {}
        self.group_id = {}
        self.individual_id = {}
        self.id_time = []
        self.id_group = []
        self.id_individual = []
        self.id_ind_info = []
        self.t_offset = 0
        self.i_offset = 0
        self.g_offset = 0

        for index,t in enumerate(gtm.times):
            self.time_id[t] = index
            self.id_time.append(t)
        for index,i in enumerate(gtm.inds):
            self.individual_id[i] = index
            self.id_individual.append(i)
            self.id_ind_info.append([ "" for t in gtm.times])
    
if __name__ == "__main__": 
    import re
    map_fname = None
    for arg in sys.argv[1:]:
        p = re.compile('-[a-z]+')
        if p.match(arg):
            pass
        else:
            if map_fname is None:
                map_fname = arg
            else:
                print 'Too many file names'
                exit(1)

    if map_fname is None:
        print 'A map file is not specified'
        exit(1)

    data = GtmMap(map_fname)
    print "time -> new id"
    for id,name in enumerate(data.id_time):
        print name, id+1
    print

    print "group -> new id"
    for id,name in enumerate(data.id_group):
        print name, id+1
    print

    print "individual -> new id"
    for id,name in enumerate(data.id_individual):
        print name, id+1, "|".join(data.id_ind_info[id])
    print

