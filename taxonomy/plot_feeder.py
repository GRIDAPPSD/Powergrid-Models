import json
import matplotlib.pyplot as plt 
import matplotlib.lines as lines
import matplotlib.patches as patches
import networkx as nx
import sys

edgeTypes = {'overhead_line':   {'color':'blue',       'tag':'OHD'},
            'underground_line': {'color':'orange',     'tag':'UG'}, 
            'transformer':      {'color':'gray',       'tag':'XFM'}, 
            'switch':           {'color':'green',      'tag':'SWT'},
            'fuse':             {'color':'lightcoral', 'tag':'FUSE'}, 
            'regulator':        {'color':'magenta',    'tag':'REG'}, 
            'recloser':         {'color':'red',        'tag':'REC'}, 
            'sectionalizer':    {'color':'darkred',    'tag':'SEC'}, 
            'triplex_line':     {'color':'tan',        'tag':'TPX'}, 
            'series_reactor':   {'color':'sienna',     'tag':'RCT'},
            'parent':           {'color':'cyan',       'tag':'MTR'},
            'unknown':          {'color':'black',      'tag':'UNK'}}

nodeTypes = {'load_class_C':    {'color':'green',      'tag':'COM', 'size':5},
             'load_class_I':    {'color':'red',        'tag':'IND', 'size':5},
             'load_class_A':    {'color':'gold',       'tag':'AGR', 'size':3},
             'load_class_R':    {'color':'olive',      'tag':'RES', 'size':3},
             'capacitor':       {'color':'magenta',    'tag':'CAP', 'size':5},
             'other':           {'color':'black',      'tag':'NODE','size':3}}

def get_node_mnemonic(nclass):
    if nclass in nodeTypes:
        return nodeTypes[nclass]['tag']
    return nodeTypes['other']['tag']

def get_node_size(nclass):
    if nclass in nodeTypes:
        return nodeTypes[nclass]['size']
    return nodeTypes['other']['size']

def get_node_color(nclass):
    if nclass in nodeTypes:
        return nodeTypes[nclass]['color']
    print ('unknown node class', nclass)
    return nodeTypes['other']['color']

def get_edge_width(nphs):
    if nphs == 1:
        return 1.0
    if nph == 2:
        return 2.0
    return 3.0

def get_edge_color(eclass):
    if eclass in edgeTypes:
        return edgeTypes[eclass]['color']
    print ('unknown edge class', eclass)
    return edgeTypes['unknown']['color']

def get_edge_mnemonic(eclass):
    if eclass in edgeTypes:
        return edgeTypes[eclass]['tag']
    return edgeTypes['unknown']['tag']

if __name__ == '__main__':
    print ('usage: python3 plot_feeder.py fdr_name nodes')
    print ('  fdr_name like R5-12.47-5')
    print ('  nodes is 1 to plot labels, 0 not to')
    feedername = 'GC-12.47-1'
    feedername = 'R1-12.47-3'
    plotLabels = False
    if len(sys.argv) > 1:
        feedername = sys.argv[1]
        if len(sys.argv) > 2:
            if int(sys.argv[2]) > 0:
                plotLabels = True

    lp = open ('new_' + feedername + '.json').read()
    feeder = json.loads(lp)
    G = nx.readwrite.json_graph.node_link_graph(feeder)
    nbus = G.number_of_nodes()
    nbranch = G.number_of_edges()
    print ('read graph with', nbus, 'nodes and', nbranch, 'edges')

    # build a list of nodes, i.e., capacitors and loads, that modify rendering of their parent
    shuntNodes = {}  # key on the parent node, value will be the nodeType
    for n, data in G.nodes(data=True):
        ndata = data['ndata']
        nclass = data['nclass']
        if nclass == 'capacitor':
            shuntNodes[ndata['parent']] = 'capacitor'
        elif nclass == 'load':
            if 'load_class' in ndata:
                loadclass = 'load_class_' + ndata['load_class']
            else:
                loadclass = 'load_class_R'
            shuntNodes[ndata['parent']] = loadclass

    # extract the XY coordinates available for plotting
    xy = {}
    lblNode = {}
    plotNodes = []
    nodeColors = []
    nodeSizes = []
    for n, data in G.nodes(data=True):
        ndata = data['ndata']
        if 'x' in ndata:
            busx = float(ndata['x'])
            busy = float(ndata['y'])
            xy[n] = [busx, busy]
            plotNodes.append(n)
            if n in shuntNodes:
                nclass = shuntNodes[n]
            else:
                nclass = 'other'
            nodeColors.append (get_node_color (nclass))
            nodeSizes.append (get_node_size (nclass))
            lblNode[n] = n[11:] # skip the first 11 characters, e.g., R5-12-47-5_

    # only plot the edges that have XY coordinates at both ends
    plotEdges = []
    edgeWidths = []
    edgeColors = []
    for n1, n2, data in G.edges(data=True):
        bFound = False
        if n1 in xy:
            if n2 in xy:
                bFound = True
                nph = 3
                # try to identify the phases
                if 'edata' in data:
                    if 'phases' in data['edata']:
                        phs = data['edata']['phases']
                        if 'ABC' in phs:
                            nph = 3
                        elif 'S' in phs:
                            nph = 1
                        else:
                            nph = len(phs)
                            if 'N' in phs:
                                nph -= 1
                plotEdges.append ((n1, n2))
                edgeWidths.append (get_edge_width(nph))
                edgeColors.append (get_edge_color(data['eclass']))
        if not bFound and data['eclass'] != 'parent':
            print ('unable to plot', data['ename'])

    nx.draw_networkx_nodes (G, xy, nodelist=plotNodes, node_color=nodeColors, node_size=nodeSizes)
    nx.draw_networkx_edges (G, xy, edgelist=plotEdges, edge_color=edgeColors, width=edgeWidths, alpha=0.8)
    if plotLabels:
        nx.draw_networkx_labels (G, xy, lblNode, font_size=8, font_color='k')
    plt.title ('Layout of Feeder Power Components for ' + feedername)
    plt.xlabel ('X coordinate [?]')
    plt.ylabel ('Y coordinate [?]')
    plt.grid(linestyle='dotted')
    xdata = [0, 1]
    ydata = [1, 0]
    lns = [lines.Line2D(xdata, ydata, color=get_edge_color(e)) for e in edgeTypes] + \
        [lines.Line2D(xdata, ydata, color=get_node_color(n), marker='o') for n in nodeTypes]
        #        [patches.Circle((0,0), color=get_node_color(n), radius=get_node_size (n)) for n in nodeTypes]
    labs = [get_edge_mnemonic (e) for e in edgeTypes] + [get_node_mnemonic (n) for n in nodeTypes]
    plt.legend(lns, labs, loc='best')
    plt.show()

