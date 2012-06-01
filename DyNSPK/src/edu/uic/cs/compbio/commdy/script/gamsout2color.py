#!/usr/bin/python

import sys
from unionfind import UnionFind
from GtmFile import *

out_fname = None
gtm_fname = None
for arg in sys.argv[1:]:
	if arg.endswith('.out'):
		out_fname = arg
	elif arg.endswith('.gtm'):
		gtm_fname = arg
	else:
		print 'Unknown file extension in', arg
		exit(1)

if out_fname is None:
	f = sys.stdin
else:
	f = open(out_fname, 'r');

if gtm_fname is None:
	gtm_fname=guess_gtmfile2(out_fname)
	sys.stderr.write("guessing %s\n" % gtm_fname)
	# check
	try:
		g = open(gtm_fname, 'r')
		g.close()
	except IOError:
		gtm_fname=None

	if gtm_fname is None:
		raise
#		if out_fname is None:
#			sys.stderr.write("Specify a gtm file\n")
#			exit(1)
#		else:
#			for i in range(1, len(out_fname)):
#				gtm_fname = "%s.gtm"%(out_fname[:-i])
#				try:
#					g = open(gtm_fname, 'r')
#					g.close()
#				except IOError:
#					gtm_fname = None
#				if gtm_fname is not None: break
#			if gtm_fname is None:
#				sys.stderr.write("Specify a gtm file\n")
#				exit(1)
#			sys.stderr.write("using " + gtm_fname + "\n")

gtm = GtmFile(gtm_fname)
group_count = len(gtm.groups)

# find connected components which have to be paths
# wanna check if they are paths?
uf = UnionFind()
for line in f.readlines():
	line = line.strip()
	if line.startswith("# y"): break
	if line=="" or line.startswith("#"): continue
	if line.find(',')>=0: line = line.split(',')
	elif line.find(' ')>=0: line = line.split(' ')
	else: raise Exception("Invalid line: "+line)
	if len(line)>=2:
		u,v = int(line[0]), int(line[1])
	else:
		raise Exception("ERROR line: %s"%line)
	uf.union(u,v)

# make lists of groups (no dummies) in each component
vertices = range(1, group_count+1)
component = {}
for v in vertices:
	l = uf.find(v)
	if l not in component:
		component[l] = list()
	component[l].append(v)
for l in component:
	component[l].sort()
component = sorted(component.values())

# build color-conflict graph
adj_list = {}
for t in xrange(len(gtm.times)):
	groups = gtm.time[t+1]
	for i in xrange(len(groups)):
		g = uf.find(groups[i])
		for j in xrange(i+1, len(groups)):
			h = uf.find(groups[j])
			if g not in adj_list: adj_list[g] = set()
			adj_list[g].add(h)
			if h not in adj_list: adj_list[h] = set()
			adj_list[h].add(g)

#for g in sorted(adj_list):
#	groups = adj_list[g]
#	groups = sorted(groups)
#	adj_list[g] = groups
#	if len(groups)==0: continue
#	print g, ":", 
#	for h in groups:
#		print h,
#	print
#exit(0)

#for i, c in enumerate(component):
#	print uf.find(c[0]), ":", 
#	for j in c:
#		print j,
#	print

if True:
	# assign color to component (straightforward way)
	leader_color = {}
	for i, c in enumerate(component):
		c = list(c)
		leader_color[uf.find(c[0])] = i+1
else:
	# reassign color to groups
	leader_color = {}
	maxcolor = 0
	for t in gtm.times:
		for g in gtm.time[t]:
			leader = uf.find(g)
			if leader in leader_color: continue
			# find a recyclable color
			forbidden = set()
			if leader in adj_list:
				for n in adj_list[leader]:
					l2 = uf.find(n)
					if l2 in leader_color:
						forbidden.add(leader_color[l2])
			ok = set(xrange(1, maxcolor+1))
			ok.difference_update(forbidden)
			if len(ok)==0:
				maxcolor+=1
				leader_color[leader] = maxcolor
			else:
				leader_color[leader] = ok.pop()

# print group color
for v in vertices:
	print leader_color[uf.find(v)], 
print
	
