import sys;
import re;
import os.path;

glmpath = 'base_taxonomy/'

max208kva = 100.0

# kva, %r, %x, %nll, %imag
three_phase = [[30,1.90,1.77,0.79,4.43],
[45,1.75,2.12,0.70,3.94],
[75,1.60,2.42,0.63,3.24],
[112.5,1.45,2.85,0.59,2.99],
[150,1.30,3.25,0.54,2.75],
[225,1.30,3.52,0.50,2.50],
[300,1.30,4.83,0.46,2.25],
[500,1.10,4.88,0.45,2.26],
[750,0.97,5.11,0.44,1.89],
[1000,0.85,5.69,0.43,1.65],
[1500,0.78,5.70,0.39,1.51],
[2000,0.72,5.70,0.36,1.39],
[2500,0.70,5.71,0.35,1.36],
[3750,0.62,5.72,0.31,1.20],
[5000,0.55,5.72,0.28,1.07],
[7500,0.55,5.72,0.28,1.07],
[10000,0.55,5.72,0.28,1.07]]

# kva, %r, %x, %nll, %imag
single_phase = [[5,2.10,1.53,0.90,3.38],
[10,1.90,1.30,0.68,2.92],
[15,1.70,1.47,0.60,2.53],
[25,1.60,1.51,0.52,1.93],
[37.5,1.45,1.65,0.47,1.74],
[50,1.30,1.77,0.45,1.54],
[75,1.25,1.69,0.42,1.49],
[100,1.20,2.19,0.40,1.45],
[167,1.15,2.77,0.38,1.66],
[250,1.10,3.85,0.36,1.81],
[333,1.00,4.90,0.34,1.97],
[500,1.00,4.90,0.29,1.98]]

def Find1PhaseXfmr (kva):
    for row in single_phase:
        if row[0] >= kva:
#            return row[0], 0.0001, 0.0002, 0.01 * row[3], 0.01 * row[4]
            return row[0], 0.01 * row[1], 0.01 * row[2], 0.01 * row[3], 0.01 * row[4]
#            return row[0], 0.0001, 0.0002, 0.001, 0.001
    return 0,0,0,0,0

def Find3PhaseXfmr (kva):
    for row in three_phase:
        if row[0] >= kva:
#            return row[0], 0.0001, 0.0002, 0.01 * row[3], 0.01 * row[4]
            return row[0], 0.01 * row[1], 0.01 * row[2], 0.01 * row[3], 0.01 * row[4]
#            return row[0], 0.0001, 0.0002, 0.001, 0.001
    return 0,0,0,0,0

#casefiles = [['R1-12.47-3',12470.0, 7200.0]]
#casefiles = [['R1-12.47-1',12470.0, 7200.0],
#             ['R1-12.47-2',12470.0, 7200.0],
#             ['R1-12.47-3',12470.0, 7200.0],
#             ['R1-12.47-4',12470.0, 7200.0],
#             ['R1-25.00-1',24900.0,14400.0],
#             ['R2-12.47-1',12470.0, 7200.0],
#             ['R2-12.47-2',12470.0, 7200.0],
#             ['R2-12.47-3',12470.0, 7200.0],
#             ['R2-25.00-1',24900.0,14400.0],
#             ['R2-35.00-1',34500.0,19920.0],
#             ['R3-12.47-1',12470.0, 7200.0],
#             ['R3-12.47-2',12470.0, 7200.0],
#             ['R3-12.47-3',12470.0, 7200.0],
#             ['R4-12.47-1',13800.0, 7970.0],
#             ['R4-12.47-2',12470.0, 7200.0],
#             ['R4-25.00-1',24900.0,14400.0],
#             ['R5-12.47-1',13800.0, 7970.0],
#             ['R5-12.47-2',12470.0, 7200.0],
#             ['R5-12.47-3',13800.0, 7970.0],
#             ['R5-12.47-4',12470.0, 7200.0],
#             ['R5-12.47-5',12470.0, 7200.0],
#             ['R5-25.00-1',22900.0,13200.0],
#             ['R5-35.00-1',34500.0,19920.0],
#             ['GC-12.47-1',12470.0, 7200.0]]
casefiles = [['R2-25.00-1',24900.0,14400.0],
             ['R2-35.00-1',34500.0,19920.0],
             ['R5-12.47-4',12470.0, 7200.0],
             ['R5-12.47-5',12470.0, 7200.0],
             ['R5-35.00-1',34500.0,19920.0]]

def obj(parent,model,line,itr,oidh,octr):
    '''
    Store an object in the model structure
    Inputs:
        parent: name of parent object (used for nested object defs)
        model: dictionary model structure
        line: glm line containing the object definition
        itr: iterator over the list of lines
        oidh: hash of object id's to object names
        octr: object counter
    '''
    octr += 1
    # Identify the object type
    m = re.search('object ([^:{\s]+)[:{\s]',line,re.IGNORECASE)
    type = m.group(1)
    # If the object has an id number, store it
    n = re.search('object ([^:]+:[^{\s]+)',line,re.IGNORECASE)
    if n:
        oid = n.group(1)
    line = next(itr)
    # Collect parameters
    oend = 0
    oname = None
    params = {}
    if parent is not None:
        params['parent'] = parent
    while not oend:
        m = re.match('\s*(\S+) ([^;{]+)[;{]',line)
        if m:
            # found a parameter
            param = m.group(1)
            val = m.group(2)
            intobj = 0
            if param == 'name':
                oname = val
            elif param == 'object':
                # found a nested object
                intobj += 1
                if oname is None:
                    print('ERROR: nested object defined before parent name')
                    quit()
                line,octr = obj(oname,model,line,itr,oidh,octr)
            elif re.match('object',val):
                # found an inline object
                intobj += 1
                line,octr = obj(None,model,line,itr,oidh,octr)
                params[param] = 'ID_'+str(octr)
            else:
                params[param] = val
        if re.search('}',line):
            if intobj:
                intobj -= 1
                line = next(itr)
            else:
                oend = 1
        else:
            line = next(itr)
    # If undefined, use a default name
    if oname is None:
        oname = 'ID_'+str(octr)
    oidh[oname] = oname
    # Hash an object identifier to the object name
    if n:
        oidh[oid] = oname
    # Add the object to the model
    if type not in model:
        # New object type
        model[type] = {}
    model[type][oname] = {}
    for param in params:
        model[type][oname][param] = params[param]
    return line,octr

def write_model_class (model, h, t, op):
    if t in model:
        for o in model[t]:
#            print('object ' + t + ':' + o + ' {', file=op)
            print('object ' + t + ' {', file=op)
            print('  name ' + o + ';', file=op)
            for p in model[t][o]:
                if ':' in model[t][o][p]:
                    print ('  ' + p + ' ' + h[model[t][o][p]] + ';', file=op)
                else:
                    print ('  ' + p + ' ' + model[t][o][p] + ';', file=op)
            print('}', file=op)

# if triplex load, node or meter, the nominal voltage is 120
#   if the name or parent attribute is found in secmtrnode, we look up the nominal voltage there
#   otherwise, the nominal voltage is vprim
# secmtrnode[mtr_node] = [kva_total, phases, vnom]
#   the transformer phasing was not changed, and the transformers were up-sized to the largest phase kva
#   therefore, it should not be necessary to look up kva_total, but phases might have changed N==>S
# if the phasing did change N==>S, we have to prepend triplex_ to the class, write power_1 and voltage_1
def write_voltage_class (model, h, t, op, vprim, secmtrnode):
    if t in model:
        for o in model[t]:
            name = o # model[t][o]['name']
            phs = model[t][o]['phases']
            vnom = vprim
            parent = ''
            prefix = ''
            if str.find(phs, 'S') >= 0:
                bHadS = True
            else:
                bHadS = False
            if str.find(name, '_tn_') >= 0 or str.find(name, '_tm_') >= 0:
                vnom = 120.0
            if name in secmtrnode:
                vnom = secmtrnode[name][2]
                phs = secmtrnode[name][1]
            if 'parent' in model[t][o]:
                parent = model[t][o]['parent']
                if parent in secmtrnode:
                    vnom = secmtrnode[parent][2]
                    phs = secmtrnode[parent][1]
            if str.find(phs,'S') >= 0:
                bHaveS = True
            else:
                bHaveS = False
            if bHaveS == True and bHadS == False:
                prefix = 'triplex_'
            vstarta = str(vnom) + '+0.0j'
            vstartb = format(-0.5*vnom,'.2f') + format(-0.866025*vnom,'.2f') + 'j'
            vstartc = format(-0.5*vnom,'.2f') + '+' + format(0.866025*vnom,'.2f') + 'j'
            print('object ' + prefix + t + ' {', file=op)
            if len(parent) > 0:
                print('  parent ' + parent + ';', file=op)
            print('  name ' + name + ';', file=op)
            if 'bustype' in model[t][o]:
                print('  bustype ' + model[t][o]['bustype'] + ';', file=op)
            print('  phases ' + phs + ';', file=op)
            print('  nominal_voltage ' + str(vnom) + ';', file=op)
            if 'load_class' in model[t][o]:
                print('  load_class ' + model[t][o]['load_class'] + ';', file=op)
            if 'constant_power_A' in model[t][o]:
                if bHaveS == True:
                    print('  power_1 ' + model[t][o]['constant_power_A'] + ';', file=op)
                else:
                    print('  constant_power_A ' + model[t][o]['constant_power_A'] + ';', file=op)
            if 'constant_power_B' in model[t][o]:
                if bHaveS == True:
                    print('  power_1 ' + model[t][o]['constant_power_B'] + ';', file=op)
                else:
                    print('  constant_power_B ' + model[t][o]['constant_power_B'] + ';', file=op)
            if 'constant_power_C' in model[t][o]:
                if bHaveS == True:
                    print('  power_1 ' + model[t][o]['constant_power_C'] + ';', file=op)
                else:
                    print('  constant_power_C ' + model[t][o]['constant_power_C'] + ';', file=op)
            if 'voltage_A' in model[t][o]:
                if bHaveS == True:
                    print('  voltage_1 ' + vstarta + ';', file=op)
                    print('  voltage_2 ' + vstarta + ';', file=op)
                else:
                    print('  voltage_A ' + vstarta + ';', file=op)
            if 'voltage_B' in model[t][o]:
                if bHaveS == True:
                    print('  voltage_1 ' + vstartb + ';', file=op)
                    print('  voltage_2 ' + vstartb + ';', file=op)
                else:
                    print('  voltage_B ' + vstartb + ';', file=op)
            if 'voltage_C' in model[t][o]:
                if bHaveS == True:
                    print('  voltage_1 ' + vstartc + ';', file=op)
                    print('  voltage_2 ' + vstartc + ';', file=op)
                else:
                    print('  voltage_C ' + vstartc + ';', file=op)
            if 'power_1' in model[t][o]:
                print('  power_1 ' + model[t][o]['power_1'] + ';', file=op)
            if 'power_2' in model[t][o]:
                print('  power_2 ' + model[t][o]['power_2'] + ';', file=op)
            if 'voltage_1' in model[t][o]:
                if str.find(phs, 'A') >= 0:
                    print('  voltage_1 ' + vstarta + ';', file=op)
                    print('  voltage_2 ' + vstarta + ';', file=op)
                if str.find(phs, 'B') >= 0:
                    print('  voltage_1 ' + vstartb + ';', file=op)
                    print('  voltage_2 ' + vstartb + ';', file=op)
                if str.find(phs, 'C') >= 0:
                    print('  voltage_1 ' + vstartc + ';', file=op)
                    print('  voltage_2 ' + vstartc + ';', file=op)
            print('}', file=op)

def log_model(model, h):
    for t in model:
        print(t+':')
        for o in model[t]:
            print('\t'+o+':')
            for p in model[t][o]:
                if ':' in model[t][o][p]:
                    print('\t\t'+p+'\t-->\t'+h[model[t][o][p]])
                else:
                    print('\t\t'+p+'\t-->\t'+model[t][o][p])

for c in casefiles:
    fname = glmpath + 'orig_' + c[0] + '.glm'
    print (fname)
    if os.path.isfile(fname):
        ip = open (fname, 'r')
        lines = []
        line = ip.readline()
        while line is not '':
            while re.match('\s*//',line) or re.match('\s+$',line):
                # skip comments and white space
                line = ip.readline()
            lines.append(line.rstrip())
            line = ip.readline()
        ip.close()

        op = open (glmpath + 'new_' + c[0] + '.glm', 'w')
        octr = 0;
        model = {}
        h = {}		# OID hash
        itr = iter(lines)
        for line in itr:
            if re.search('object',line):
                line,octr = obj(None,model,line,itr,h,octr)
            else:
                print (line, file=op)

#        log_model (model, h)

        xfcode = {} # ID, phases, st, vnom (LN)
        # UPDATE: we can't convert single-phase to center-tapped, because they are not all R class loads
        t = 'transformer_configuration'
        for o in model[t]:
            sa = 0
            sb = 0
            sc = 0
            np = 0
            phs = ''
            st = float(model[t][o]['power_rating'])
            v1 = float(model[t][o]['primary_voltage'].split(' ',1)[0])
            v2 = float(model[t][o]['secondary_voltage'].split(' ',1)[0])
            if 'powerA_rating' in model[t][o]:
                sa = float(model[t][o]['powerA_rating'])
                phs += 'A'
                np += 1
            if 'powerB_rating' in model[t][o]:
                sb = float(model[t][o]['powerB_rating'])
                phs += 'B'
                np += 1
            if 'powerC_rating' in model[t][o]:
                sc = float(model[t][o]['powerC_rating'])
                phs += 'C'
                np += 1

            # not actually making any changes N==>S
            if str.find(model[t][o]['connect_type'], 'SINGLE_PHASE_CENTER_TAPPED') >= 0: 
                phs += 'S'
            else:
                phs += 'N'

            if np == 1:
                row = Find1PhaseXfmr (st)
                st = row[0]
                if sa > 0:
                    sa = st
                if sb > 0:
                    sb = st
                if sc > 0:
                    sc = st
            else:
                # make sure the transformer is large enough for the highest-rated phase, and that it's balanced
                smax = sa
                if sb > smax:
                    smax = sb
                if sc > smax:
                    smax = sc
                if str.find(phs, 'A') >= 0:
                    sa = smax
                if str.find(phs, 'B') >= 0:
                    sb = smax
                if str.find(phs, 'C') >= 0:
                    sc = smax
                st = sa + sb + sc
                row = Find3PhaseXfmr (st)
                st = row[0]
                if sa > 0:
                    sa = st / np
                if sb > 0:
                    sb = st / np
                if sc > 0:
                    sc = st / np
            if str.find(phs, 'S') >= 0:
                vsec = 120.0
                vnom = 120.0
            else:
                if st > max208kva:
                    vsec = 480.0
                    vnom = 277.0
                else:
                    vsec = 208.0
                    vnom = 120.0
            print ('object transformer_configuration: {', file=op)
            print ('  name ' + o + ';', file=op)
            print ('  power_rating ' + format(st, '.2f') + ';', file=op)
            print ('  powerA_rating ' + format(sa, '.2f') + ';', file=op)
            print ('  powerB_rating ' + format(sb, '.2f') + ';', file=op)
            print ('  powerC_rating ' + format(sc, '.2f') + ';', file=op)
            if 'install_type' in model[t][o]:
                print ('  install_type ' + model[t][o]['install_type'] + ';', file=op)
            if str.find(phs, 'S') >= 0:
                print ('  connect_type SINGLE_PHASE_CENTER_TAPPED;', file=op)
                print ('  primary_voltage ' + str(c[2]) + ';', file=op)
                print ('  secondary_voltage ' + format(vsec, '.1f') + ';', file=op)
                print ('  resistance ' + format(row[1] * 0.5, '.5f') + ';', file=op)
                print ('  resistance1 ' + format(row[1], '.5f') + ';', file=op)
                print ('  resistance2 ' + format(row[1], '.5f') + ';', file=op)
                print ('  reactance ' + format(row[2] * 0.8, '.5f') + ';', file=op)
                print ('  reactance1 ' + format(row[2] * 0.4, '.5f') + ';', file=op)
                print ('  reactance2 ' + format(row[2] * 0.4, '.5f') + ';', file=op)
                print ('  shunt_resistance ' + format(1.0 / row[3], '.2f') + ';', file=op)
                print ('  shunt_reactance ' + format(1.0 / row[4], '.2f') + ';', file=op)
            else:
                if 'connect_type' in model[t][o]:
                    print ('  connect_type ' + model[t][o]['connect_type'] + ';', file=op)
                print ('  primary_voltage ' + str(c[1]) + ';', file=op)
                print ('  secondary_voltage ' + format(vsec, '.1f') + ';', file=op)
                print ('  resistance ' + format(row[1], '.5f') + ';', file=op)
                print ('  reactance ' + format(row[2], '.5f') + ';', file=op)
                print ('  shunt_resistance ' + format(1.0 / row[3], '.2f') + ';', file=op)
                print ('  shunt_reactance ' + format(1.0 / row[4], '.2f') + ';', file=op)
            xfcode[o] = [st, phs, vnom]
            print('}', file=op)

#        print (xfcode)

        secnode = {} # Node, st, phases, vnom
        t = 'transformer'
        for o in model[t]:
            row = xfcode [h[model[t][o]['configuration']]]
            model[t][o]['phases'] = row[1]
            secnode[model[t][o]['to']] = row

#        print (secnode)

        write_model_class (model, h, 'regulator_configuration', op)
        write_model_class (model, h, 'overhead_line_conductor', op)
        write_model_class (model, h, 'line_spacing', op)
        write_model_class (model, h, 'line_configuration', op)
        write_model_class (model, h, 'triplex_line_conductor', op)
        write_model_class (model, h, 'triplex_line_configuration', op)
        write_model_class (model, h, 'underground_line_conductor', op)

        write_model_class (model, h, 'fuse', op)
        write_model_class (model, h, 'switch', op)
        write_model_class (model, h, 'recloser', op)
        write_model_class (model, h, 'sectionalizer', op)

        write_model_class (model, h, 'overhead_line', op)
        write_model_class (model, h, 'underground_line', op)
        write_model_class (model, h, 'triplex_line', op)
        write_model_class (model, h, 'series_reactor', op)

        write_model_class (model, h, 'regulator', op)
        write_model_class (model, h, 'transformer', op)
        write_model_class (model, h, 'capacitor', op)

        write_voltage_class (model, h, 'node', op, c[2], secnode)
        write_voltage_class (model, h, 'meter', op, c[2], secnode)
        write_voltage_class (model, h, 'load', op, c[2], secnode)
        write_voltage_class (model, h, 'triplex_node', op, c[2], secnode)
        write_voltage_class (model, h, 'triplex_meter', op, c[2], secnode)
        write_voltage_class (model, h, 'triplex_load', op, c[2], secnode)

        op.close()



