# before running this script, in OpenDSS,
#  redirect Master.dss
#  show buses

buses = {}
missing = []
nmanual = 0

# shift the transformer secondary bus by this much from the primary bus
dxt = 5
dyt = 0

mp = open('ManualBuses.xy', mode='w')
print ('// fill these primary bus coordinates in by manual inspection', file=mp)

with open('J1_Buses.Txt', mode='r') as infile:
  for i in range(6):
    next (infile)
  for line in infile:
    row = line.split()
    bus = row[0].strip('"')
    kV = float(row[1])
    if 'NA' not in row[3]:
      x = float(row[3])
      y = float(row[5])
    else:
      x = 'NA'
      y = 'NA'
    if x == 'NA':
      if kV > 0.5:
        print (bus + ',0,0', file=mp)
        nmanual += 1
      else:
        missing.append (bus)
    else:
      buses[bus] = [x, y]

nsecondary = 0
op = open('SecondaryBuses.xy', mode='w')
for bus in missing:
  key = bus.strip('x_')[:-2]  # non-customer bus
  custkey = ''
  custnum = 0
  if '_cust' in key:
    custkey = key[:-6]
    custnum = int(key[-1:])
    print (custkey, custnum)
  if key in buses:
    primary = buses[key]
    x = primary[0] + dxt
    y = primary[1] + dyt
    print (bus.upper() + ',' + '{:.4f}'.format(x) + ',' + '{:.4f}'.format(y), file=op)
    nsecondary += 1
  elif custkey in buses:
    primary = buses[custkey]
    x = primary[0] + custnum
    y = primary[1] + custnum
    print (bus.upper() + ',' + '{:.4f}'.format(x) + ',' + '{:.4f}'.format(y), file=op)
    nsecondary += 1
  else:
    print (bus + ',0,0', file=mp)
    nmanual += 1

print (nsecondary, 'secondary and customer buses with XY coordinates now')
print (nmanual, 'buses need manual coordinates')
op.close()
mp.close()

