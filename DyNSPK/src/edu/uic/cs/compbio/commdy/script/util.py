import time
import random

def n_chooses_2(n):
	if getattr(n, '__iter__', False):
	#if isinstance(n, list):
		for i in xrange(len(n)):
			for j in xrange(i+1, len(n)):
				yield n[i],n[j]
	else:
		for i in xrange(n):
			for j in xrange(i+1, n):
				yield i,j

def strip_ext(str, ext = None):
	if ext is None:
		return str[:str.rfind('.')]
	else:
		if str.endswith(ext):
			return str[:len(str)-len(ext)]
		else:
			raise Exception("Filename '%s' does not "
				"end with extension '%s'"%(str, ext));

class TicToc:
	def __init__(self, delay=2):
		self.delay = delay
		self.last_toc = time.clock()
	def toc(self):
		toc = time.clock()
		if toc - self.last_toc >= self.delay:
			self.last_toc += self.delay
			if toc - self.last_toc >= self.delay:
				self.last_toc = toc
			return 1
		else:
			return 0

def min_index(iter):
	min = None
	min_i = None
	for i,x in enumerate(iter):
		if min>x or i==0:
			min = x
			min_i = i
	return min, min_i

def int_reader(fd):
	while not fd.closed:
		ch = fd.read(1)
		while (ch==' '): ch = fd.read(1)
		if ch=='': break
		num = int(ch)
		ch = fd.read(1)
		while ('0'<=ch and ch<='9'):
			num = num*10 + int(ch)
			ch = fd.read(1)
		yield num

def ceil_randomly(v):
    x,y = random.random(), v-int(v)
    if x<y: # randomly round up
        return int(v)+1
    else:
        return int(v)
