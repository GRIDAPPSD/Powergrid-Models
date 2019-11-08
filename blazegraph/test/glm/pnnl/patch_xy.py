xmin = 1.0e20
ymin = 1.0e20
xmax = -1.0e20
ymax = -1.0e20
basexy = {}

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
fp = open('BusCoords.csv', mode='r')
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
    idx1 = bus.find('_node_')
    if idx1 > 0:
        idx2 = bus.find('_', idx1 + 6)
        if idx2 > idx1:
            tok = bus[idx1+1:idx2]
            if tok in basexy:
                cnt += 1
                basex = basexy[tok]['x']
                basey = basexy[tok]['y']
                basexy[tok]['count'] += 1
                incr = basexy[tok]['count'] * 0.2
                print ('{:s},{:.2f},{:.2f}'.format (bus, basex+incr, basey+incr), file=fp)
#            print (bus, idx1, idx2, tok)

fp.close()

print ('wrote {:d} coordinates to BusCoords.new'.format(len(basexy) + cnt))

