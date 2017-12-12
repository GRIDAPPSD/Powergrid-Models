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
[5000,0.55,5.72,0.28,1.07]]

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
            return row[0], 0.01 * row[1], 0.01 * row[2], 0.01 * row[3], 0.01 * row[4]
    return 0,0,0,0,0

def Find3PhaseXfmr (kva):
    for row in three_phase:
        if row[0] >= kva:
            return row[0], 0.01 * row[1], 0.01 * row[2], 0.01 * row[3], 0.01 * row[4]
    return 0,0,0,0,0

casefiles = [['R1-12.47-3',12470.0, 7200.0]]
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
            print('object ' + t + ':' + o + ' {', file=op)
            for p in model[t][o]:
                if ':' in model[t][o][p]:
                    print ('  ' + p + ' ' + h[model[t][o][p]] + ';', file=op)
                else:
                    print ('  ' + p + ' ' + model[t][o][p] + ';', file=op)
            print('}', file=op)

def write_voltage_class (model, h, t, op, vprim):
    if t in model:
        for o in model[t]:
            print('object ' + t + ':' + o + ' {', file=op)
            name = o # model[t][o]['name']
            phs = model[t][o]['phases']
            print('  name ' + name + ';', file=op)
            print('  phases ' + phs + ';', file=op)
            if str.find(name, 'load') >= 0 or str.find(name, 'meter') >= 0 or str.find(name, '_tn_') >= 0 or str.find(name, '_tm_') >= 0:
                print('  nominal_voltage 120.0;', file=op)
            else:
                print('  nominal_voltage ' + str(vprim) + ';', file=op)
            if 'parent' in model[t][o]:
                print('  parent ' + model[t][o]['parent'] + ';', file=op)
            if 'load_class' in model[t][o]:
                print('  load_class ' + model[t][o]['load_class'] + ';', file=op)
            if 'constant_power_A' in model[t][o]:
                print('  constant_power_A ' + model[t][o]['constant_power_A'] + ';', file=op)
            if 'constant_power_B' in model[t][o]:
                print('  constant_power_B ' + model[t][o]['constant_power_B'] + ';', file=op)
            if 'constant_power_C' in model[t][o]:
                print('  constant_power_C ' + model[t][o]['constant_power_C'] + ';', file=op)
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

        log_model (model, h)

        t = 'transformer_configuration'
        for o in model[t]:
            sa = 0
            sb = 0
            sc = 0
            np = 0
            st = float(model[t][o]['power_rating'])
            v1 = float(model[t][o]['primary_voltage'].split(' ',1)[0])
            v2 = float(model[t][o]['secondary_voltage'].split(' ',1)[0])
            if 'powerA_rating' in model[t][o]:
                sa = float(model[t][o]['powerA_rating'])
            if 'powerB_rating' in model[t][o]:
                sb = float(model[t][o]['powerB_rating'])
            if 'powerC_rating' in model[t][o]:
                sc = float(model[t][o]['powerC_rating'])
            if sa > 0:
                np = np + 1
            if sb > 0:
                np = np + 1
            if sc > 0:
                np = np + 1
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
                row = Find3PhaseXfmr (st)
                st = row[0]
                if sa > 0:
                    sa = st / np
                if sb > 0:
                    sb = st / np
                if sc > 0:
                    sc = st / np
            print('object transformer_configuration:' + o + ' {', file=op)
            print ('  power_rating ' + str(st) + ';', file=op)
            print ('  powerA_rating ' + str(sa) + ';', file=op)
            print ('  powerB_rating ' + str(sb) + ';', file=op)
            print ('  powerC_rating ' + str(sc) + ';', file=op)
            if 'install_type' in model[t][o]:
                print ('  install_type ' + model[t][o]['install_type'] + ';', file=op)
            if np == 1:
                print ('  connect_type SINGLE_PHASE_CENTER_TAPPED;', file=op)
                print ('  primary_voltage ' + str(c[2]) + ';', file=op)
                print ('  secondary_voltage 120.0;', file=op)
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
                if st > max208kva:
                    print ('  secondary_voltage 480.0;', file=op)
                else:
                    print ('  secondary_voltage 208.0;', file=op)
                print ('  resistance ' + format(row[1], '.5f') + ';', file=op)
                print ('  reactance ' + format(row[2], '.5f') + ';', file=op)
                print ('  shunt_resistance ' + format(1.0 / row[3], '.2f') + ';', file=op)
                print ('  shunt_reactance ' + format(1.0 / row[4], '.2f') + ';', file=op)
            print('}', file=op)

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

        write_model_class (model, h, 'line', op)
        write_model_class (model, h, 'underground_line', op)
        write_model_class (model, h, 'triplex_line', op)
        write_model_class (model, h, 'series_reactor', op)

        write_model_class (model, h, 'regulator', op)
        write_model_class (model, h, 'transformer', op)
        write_model_class (model, h, 'capacitor', op)

        write_voltage_class (model, h, 'node', op, c[2])
        write_voltage_class (model, h, 'meter', op, c[2])
        write_voltage_class (model, h, 'load', op, c[2])
        write_voltage_class (model, h, 'triplex_node', op, c[2])
        write_voltage_class (model, h, 'triplex_meter', op, c[2])
        write_voltage_class (model, h, 'triplex_load', op, c[2])

        op.close()



