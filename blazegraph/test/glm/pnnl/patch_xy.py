import math

xmin = 1.0e20
ymin = 1.0e20
xmax = -1.0e20
ymax = -1.0e20
basexy = {}

# each primary node can have a service transformer on phases A, B and/or C
# each secondary node can have up to about 30 service drops, 10 feet in length
# we will arrange secondary nodes in a starburst around the primary nodes, and
#   service points in a starbust around their secondary nodes
# the secondary buses will be offset from the primary node XY coordinate as follows:
#   dA = [-50,+50], dB = [+50,+50], dC = [+50,-50]
# the service points then lie on a circle of radius 25 around the secondary node,
#   with angular separation of +9 degrees beginning at the following angles
#   angA0 = 0 degrees, angB0 = -90 degrees, angC0 = -180 degrees
# sample primary node name: node_87
# sample secondary node name: s_node_87_b (on phase B)
# sample service point name: house_agg_3_B_15_node_87_1037 (15th on phase B, node_87)
# if we split these names on '_', then
#   if name starts with 's_node_', then tok[1]_tok[2] identify the primary node, tok[3] is the phase
#   if name starts with 'house_agg' then tok[3] is the phase, tok[5]_tok[6] is the primary node
#     and tok[4] is the local index, i.e., angle should be angB0 + 9 * (tok[4] - 1)

fp = open('IEEE123_busxy.ds', mode='r')
for ln in fp.readlines():
    toks = ln.split(',')
    if len(toks) >= 3:
        bus = 'node_' + toks[0]
        x = float(toks[1])
        y = float(toks[2])
        if x < xmin:
            xmin = x
        if x > xmax:
            xmax = x
        if y < ymin:
            ymin = y
        if y > ymax:
            ymax = y
        basexy[bus] = {'x':x, 'y':y, 'count':0}
fp.close()
print ('xrange [{:.2f} {:.2f}] yrange [{:.2f} {:.2f}]'.format(xmin,xmax,ymin,ymax))

buses = []
fp = open('Buscoords.csv', mode='r')
for ln in fp.readlines():
    toks = ln.split(',')
    if len(toks) >= 3:
        buses.append (toks[0])
fp.close()
print ('{:d} buses with unknown coordinates'.format(len(buses)))

fp = open('BusCoords.new', mode='w')
for key, val in basexy.items():
    print ('{:s},{:.2f},{:.2f}'.format (key, val['x'], val['y']), file=fp)
cnt = 0
for bus in buses:
    toks = bus.split('_')
    if bus.startswith ('s_node_'):
        base_bus = toks[1] + '_' + toks[2]
        phs = toks[3].upper()
        if base_bus in basexy:
            cnt += 1
            basex = basexy[base_bus]['x']
            basey = basexy[base_bus]['y']
            basexy[base_bus]['count'] += 1
            if phs == 'A':
                x = basex - 50
                y = basey + 50
            elif phs == 'B':
                x = basex + 50
                y = basey + 50
            else:  # C
                x = basex + 50
                y = basey - 50
            print ('{:s},{:.2f},{:.2f}'.format (bus, x, y), file=fp)
    elif bus.startswith ('house_agg_'):
        base_bus = toks[5] + '_' + toks[6]
        phs = toks[3].upper()
        idx = int(toks[4])
        if base_bus in basexy:
            cnt += 1
            basex = basexy[base_bus]['x']
            basey = basexy[base_bus]['y']
            basexy[base_bus]['count'] += 1
            if phs == 'A':
                basex -= 50
                basey += 50
                deg = 0 + 9 * (idx - 1)
            elif phs == 'B':
                basex += 50
                basey += 50
                deg = -90 + 9 * (idx - 1)
            else:  # C
                basex += 50
                basey -= 50
                deg = -180 + 9 * (idx - 1)
            rad = math.radians (deg)
            x = basex + 25.0 * math.cos (rad)
            y = basey + 25.0 * math.sin (rad)
            print ('{:s},{:.2f},{:.2f}'.format (bus, x, y), file=fp)

#    idx1 = bus.find('_node_')
#    if idx1 > 0:
#        idx2 = bus.find('_', idx1 + 6)
#        if idx2 > idx1:
#            tok = bus[idx1+1:idx2]
#            if tok in basexy:
#                cnt += 1
#                basex = basexy[tok]['x']
#                basey = basexy[tok]['y']
#                basexy[tok]['count'] += 1
#                incr = basexy[tok]['count'] * 0.2
#                print ('{:s},{:.2f},{:.2f}'.format (bus, basex+incr, basey+incr), file=fp)
##            print (bus, idx1, idx2, tok)

fp.close()

print ('wrote {:d} coordinates to BusCoords.new'.format(len(basexy) + cnt))

