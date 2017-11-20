import sys;
import re;
import os.path;

glmpath = 'c:\\gridapps-d\\powergrid-models\\taxonomy\\base_taxonomy\\'

max208kva = 100.0

casefiles = [['dummy',12470.0, 7200.0]]
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
#             ['R5-35.00-1',34500.0,19920.0]]

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
    print('processing', line)
    # Collect parameters
    oend = 0
    oname = None
    params = {}
    if parent is not None:
        params['parent'] = parent
    while not oend:
        m = re.match('\s*(\S+) ([^;\s]+)[;\s]',line)
        if m:
            # found a parameter
            param = m.group(1)
            val = m.group(2)
            if param == 'name':
                print('found oname from', param, val)
                oname = val
            elif param == 'object':
                # found a nested object
                print('found nested object from', param, val)
                if oname is None:
                    print('ERROR: nested object defined before parent name')
                    quit()
                line,octr = obj(oname,model,line,itr,oidh,octr)
            elif val == 'object':
                # found an inline object
                print('found inline object from', param, val)
                line,octr = obj(None,model,line,itr,oidh,octr)
                params[param] = 'OBJECT_'+str(octr)
            else:
                print('found param from', param, val)
                params[param] = val
        if re.search('}',line):
            print('found end')
            oend = 1
        else:
            line = next(itr)
            print('processing', line)
    # If undefined, use a default name
    if oname is None:
        oname = 'OBJECT_'+str(octr)
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
    # Return the 
    return line,octr

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
        op.close()

        for t in model:
            print(t+':')
            for o in model[t]:
                print('\t'+o+':')
                for p in model[t][o]:
                    if ':' in model[t][o][p]:
                        print('\t\t'+p+'\t-->\t'+h[model[t][o][p]])
                    else:
                        print('\t\t'+p+'\t-->\t'+model[t][o][p])

