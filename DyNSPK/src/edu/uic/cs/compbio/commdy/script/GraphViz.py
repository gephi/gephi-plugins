import re

def fprint_tab(fd, tab_count, str):
	for i in xrange(tab_count):
		fd.write("    ")
		#fd.write("\t")
	fd.write(str)

class Block:
	def __init__(self, name=None, root=None):
		self.root = self if root is None else root
		self.type = None
		self.name = name
		self.elements = []
		self.prop = []
		self.graph_prop = []
		self.node_prop = []
		self.edge_prop = []

	def getHeader(self):
		return ""

	def setStyle(self, v):
		self.graph_prop.append(("style", v))
		return self

	def setShape(self, v):
		self.graph_prop.append(("shape", v))
		return self

	def setColor(self, v):
		self.graph_prop.append(("color", v))
		return self

	def setFillColor(self, v):
		self.graph_prop.append(("fillcolor", v))
		return self

	def fprint(self, fd, tab_count=0):
		if self.name is None:
			fprint_tab(fd, tab_count, "{\n")
		else:
			if re.match("[0-9]", self.name):
				name = "\"%s\""%self.name
			else:
				name = self.name
			fprint_tab(fd, tab_count, 
				"%s %s {\n"%(self.type, name))
		tab_count+=1
		if self.prop:
			for k,v in self.prop:
				if isinstance(v, str):
					fprint_tab(fd, tab_count, "%s=\"%s\"\n"%(k,v))
				else:
					fprint_tab(fd, tab_count, "%s=%s\n"%(k,v))
		if self.graph_prop:
			fprint_tab(fd, tab_count, "graph [")
			for k,v in self.graph_prop:
				if isinstance(v, str):
					fd.write(" %s=\"%s\""%(k,v))
				else:
					fd.write(" %s=%s"%(k,v))
			fd.write("]\n")
		if self.node_prop:
			fprint_tab(fd, tab_count, "node [")
			for k,v in self.node_prop:
				if isinstance(v, str):
					fd.write(" %s=\"%s\""%(k,v))
				else:
					fd.write(" %s=%s"%(k,v))
			fd.write("]\n")
		if self.edge_prop:
			fprint_tab(fd, tab_count, "edge [")
			for k,v in self.edge_prop:
				if isinstance(v, str):
					fd.write(" %s=\"%s\""%(k,v))
				else:
					fd.write(" %s=%s"%(k,v))
			fd.write("]\n")
		for e in self.elements:
			e.fprint(fd, tab_count)
		tab_count-=1
		fprint_tab(fd, tab_count, "}\n")

	def addBlock(self):
		b = Block(root=self.root)
		self.elements.append(b)
		return b

	def addSubgraph(self, name):
		f = Digraph(name, root=self.root)
		f.type = "subgraph"
		self.elements.append(f)
		return f

	def addNode(self, name, label=None):
		assert name not in self.root.name2node
		n = Node(name)
		self.root.name2node[name] = n
		self.elements.append(n)
		if label is not None:
			n.prop["label"] = label
		return n

	def addEdge(self, u, v):
		#if u not in self.root.name2node:
		#	raise Exception("Node %s does not exist"%u)
		#if v not in self.root.name2node:
		#	raise Exception("Node %s does not exist"%v)
		assert (u,v) not in self.root.name2edge
		e = Edge(u, v)
		self.root.name2edge[u,v] = e
		self.elements.append(e)
		return e

	def getNode(self, name):
		return self.root.getNode(name)

	def getEdge(self, u, v):
		return self.root.getEdge(u,v)

	def addProperty(self, k, v):
		self.prop.append((k,v))
		return self

	def addGraphProperty(self, k, v):
		self.graph_prop.append((k,v))
		return self

	def addGraphProperties(self, m):
		self.graph_prop.extend(m.iteritems())
		return self

	def addNodeProperty(self, k, v):
		self.node_prop.append((k,v))
		return self

	def addNodeProperties(self, m):
		self.node_prop.extend(m.iteritems())
		return self

	def addEdgeProperty(self, k, v):
		self.edge_prop.append((k,v))
		return self

	def addEdgeProperties(self, m):
		self.edge_prop.extend(m.iteritems())
		return self

class Digraph(Block):
	def __init__(self, name, root=None):
		if root is None: root=self
		Block.__init__(self, name, root)
		self.type = "digraph"
		self.name2node = {}
		self.name2edge = {}

	def getHeader(self):
		return "%s \"%s\"" %(self.type, self.name)

	def getNode(self, name):
		return self.root.name2node[name]

	def getEdge(self, u, v):
		return self.root.name2edge[u,v]


class Atom:

	def __init__(self):
		self.prop = {}
		self.style = []

	def setLabel(self, value):
		self.prop["label"] = value
		return self

	def setFillColor(self, color):
		self.prop["fillcolor"] = color
		return self

	def setFontColor(self, color):
		self.prop["fontcolor"] = color
		return self

	def setColor(self, color):
		self.prop["color"] = color
		return self

	def setLineWidth(self, width):
		self.style.append("setlinewidth(%d)"%width)
		return self

	def fprint_properties(self, fd):
		if len(self.prop) or len(self.style):
			fd.write(" [")
			sep=""
			for k,v in self.prop.iteritems():
				if k=="style": continue
				fd.write(sep)
				fd.write(k)
				fd.write("=")
				if isinstance(v, str):
					fd.write("\"%s\""%v)
				else:
					fd.write("%s"%v)
				fd.write("")
				sep=" "
			if "style" in self.prop or len(self.style):
				if "style" in self.prop:
					l = [ self.prop["style"] ]
				else:
					l = []
				l.extend(self.style)
				fd.write(sep)
				fd.write("style=\"")
				fd.write(",".join(l))
				fd.write("\"")
				sep=" "
			fd.write("]")

	def fprint(self, fd, tab_count=0):
		raise Exception("Abstract method")

	def setStyle(self, v):
		self.prop["style"] = v
		return self

	def setProperty(self, key, value):
		self.prop[key] = value
		return self

class Node(Atom):
	def __init__(self, name):
		Atom.__init__(self)
		self.name = name

	def fprint(self, fd, tab_count=0):
		if re.match("[0-9]", self.name):
			name = "\"%s\""%self.name
		else:
			name = self.name
		fprint_tab(fd, tab_count, name)
		self.fprint_properties(fd)
		fd.write("\n")

	def setShape(self, v):
		self.prop["shape"] = v
		return self
				
	def setWidth(self, v):
		self.prop["width"] = v
		return self
				
	def setHeight(self, v):
		self.prop["height"] = v
		return self

	def setFixedSize(self, v):
		self.prop["fixedsize"] = v
		return self
				
class Edge(Atom):
	def __init__(self, u, v):
		Atom.__init__(self)
		self.u = u
		self.v = v
		self.name = "%s -> %s"%(u, v)

	def fprint(self, fd, tab_count=0):
		fprint_tab(fd, tab_count, "")
		if re.match("[0-9]", self.u):
			fd.write("\""+self.u+"\"")
		else:
			fd.write(self.u)
		fd.write(" -> ")
		if re.match("[0-9]", self.v):
			fd.write("\""+self.v+"\"")
		else:
			fd.write(self.v)
		self.fprint_properties(fd)
		fd.write("\n")

