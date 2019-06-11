# before running this script, in OpenDSS,
#  redirect Master.dss
#  show buses

buses = {}
missing = []
nmanual = 0

# shift the transformer secondary bus by this much from the primary bus
dxt = 5
dyt = 0

# shift the secondary load bus by this much from the primary bus
dxm = 45
dym = 40

mp = open('ManualBuses.xy', mode='w')
print ('// fill these primary bus coordinates in by manual inspection', file=mp)

with open('IEEE8500new_335_Buses.Txt', mode='r') as infile:
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
  key = 'l' + bus.strip('sxabc')
  if key in buses:
    primary = buses[key]
    if 'sx' in bus.lower():  # load bus
      x = primary[0] + dxm
      y = primary[1] + dym
    else:  # transformer bus
      x = primary[0] + dxt
      y = primary[1] + dyt
    print (bus.upper() + ',' + '{:.4f}'.format(x) + ',' + '{:.4f}'.format(y), file=op)
    nsecondary += 1
  else:
    print (bus + ',0,0', file=mp)
    nmanual += 1

print (nsecondary, 'secondary buses with XY coordinates now')
print (nmanual, 'buses need manual coordinates')
op.close()
mp.close()

