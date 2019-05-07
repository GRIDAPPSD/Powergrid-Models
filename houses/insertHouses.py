'''
Module to extract EnergyConsumers from the CIM database and replace loads with
houses, HVAC systems, and plug loads. We may also want water heaters.

Created on Jun 1, 2018

@author: thay838
'''
#******************************************************************************
# IMPORTS + PATH
#******************************************************************************
# Standard library:
import logging
import sys
import math
from uuid import uuid4

# Installed packages:
import numpy as np
import pandas as pd
from SPARQLWrapper import SPARQLWrapper2

# Add one level up to the path so we can import from 'Meas'
sys.path.append("..")

# Modules in this repository:
#
# constants.py is used for configuring blazegraph.
from Meas import constants #@UnresolvedImport
from createHouses import createHouses

#******************************************************************************
# CONSTANTS
#******************************************************************************

# We have to convert nominal voltages from three-phase phase-to-phase to phase
# to ground. At the time of writing, even 240V loads use sqrt(3)...
NOMVFACTOR = 3**0.5
# Triplex loads come back as 208V... This is wrong, but that's what's in the
# CIM database.
TRIPLEX_V = 208
# We'll let the user know how many 480 volt loads are in there, even though we 
# won't be adding houses to them.
COMMERCIAL_V = 480

# We need to define which housing properties are enumerations, as this uses 
# a different syntax for sparql updates.
ENUM = {'coolingSystem': 'HouseCooling', 'heatingSystem': 'HouseHeating',
        'thermalIntegrity': 'HouseThermalIntegrity'}

# Do initial log setup
LOG = logging.getLogger(__name__)

#******************************************************************************
# METHODS
#******************************************************************************
def main(fdrid, region, loglevel='INFO', logfile=None, seed=None):
    """
    """
    # Setup log:
    setupLog(logger=LOG, loglevel=loglevel, logfile=logfile)
    # 
    
    # connect to blazegraph, get sparql wrapper for queries.
    sparql = SPARQLWrapper2(constants.blazegraph_url)
    
    # Get the EnergyConsumers, commercial loads, and total magnitude of 
    # residential energy consumer loads.
    ec, comm, magS = getEnergyConsumers(sparql=sparql, fdrid=fdrid)
    LOG.info('Total res. load apparent power magnitude: '
             + '{} MVA'.format(magS/10e06))
    
    # Alert user about loads which will not be converted.
    LOG.info(('There are {} {}V loads totalling {} VA which will not have '
              'houses/buildings added.').format(comm['num'], COMMERCIAL_V,
                                                abs(comm['power'])))
    
    # Initialize a "createHouses" object
    obj = createHouses(region=region, log=LOG, seed=seed)
    
    # Generate houses. NOTE: it may technically be more efficient if the house
    # objects were inserted into the triplstore CIM database 'on the fly' as
    # they're generated, but since this functionality doesn't particularly rely
    # on speed, we'll make things more modular and readable by "double-looping"
    housingDict = obj.genHousesForFeeder(loadDf=ec, magS=magS)
    
    LOG.info('All houses generated. Inserting into database.')
    
    # Get sparql object ready for posting data.
    sparql.method = 'POST'
    
    # Loop over residential energy consumers, push associated houses into 
    # the CIM triplestore.
    for load, tup in housingDict.items():
        # Grab first element of the tuple: the dataframe representing houses.
        houseDf = tup[0]
        
        # Grab the MRID for this load
        mrid = ec.loc[load, 'mrid']
        
        # Loop over the houses and insert into CIM triplestore
        for row, houseData in houseDf.iterrows():
            # Insert into database.
            insertHouse(sparql=sparql, ecName=load, ecID=mrid, houseNum=row,
                        houseData=houseData)
            
    LOG.info('All houses inserted into database.')
    
    print('hooray')
    
def setupLog(logger, loglevel, logfile):
    """Helper function to setup the module's log.
    """
    # level
    level = getattr(logging, loglevel.upper())
    logger.setLevel(level)
    
    # file/stream handler
    if logfile is None:
        h = logging.StreamHandler(sys.stdout)
    else:
        h = logging.FileHandler(filename=logfile)
        
    h.setLevel(level)
    
    # formatting
    fmt = '[%(asctime)s] [%(levelname)s]: %(message)s'
    formatter = logging.Formatter(fmt, datefmt='%H:%M:%S')
    h.setFormatter(formatter)
    
    # add handler
    logger.addHandler(h)
    
def getEnergyConsumers(sparql, fdrid):
    """Method to get nominal voltages from each 'EnergyConsumer.'
    
    Query source is Powergrid-Models/blazegraph/queries.txt, and it was 
    modified from there.
    
    For now, we'll just get the 'bus' (which is really the object name in
    the GridLAB-D model) and nominal voltage. Nominal voltage is given
    as 3-phase phase-to-phase, even if we're talking about a split-phase.
    I suppose that's what you get when you apply a transmission standard
    to the secondary side of a distribution system...
    
    TODO: Later, we may want to act according to the load connection/phases
    """
    
    query = \
        (constants.prefix +
        r'SELECT ?name ?mrid ?bus ?basev ?p ?q ?conn (group_concat(distinct ?phs;separator=",") as ?phases) '
        "WHERE {{ "
            "?s r:type c:EnergyConsumer. "
            'VALUES ?fdrid {{"{fdrid}"}} '
            "?s c:Equipment.EquipmentContainer ?fdr. "
            "?fdr c:IdentifiedObject.mRID ?fdrid. " 
            "?s c:IdentifiedObject.name ?name. "
            "?s c:IdentifiedObject.mRID ?mrid. "
            "?s c:ConductingEquipment.BaseVoltage ?bv. "
            "?bv c:BaseVoltage.nominalVoltage ?basev. "
            "?s c:EnergyConsumer.p ?p."
            "?s c:EnergyConsumer.q ?q."
            "?s c:EnergyConsumer.phaseConnection ?connraw. "
            'bind(strafter(str(?connraw),"PhaseShuntConnectionKind.") as ?conn) '
            "OPTIONAL {{ "
                "?ecp c:EnergyConsumerPhase.EnergyConsumer ?s. "
                "?ecp c:EnergyConsumerPhase.phase ?phsraw. "
                'bind(strafter(str(?phsraw),"SinglePhaseKind.") as ?phs) '
            "}} "
            "?t c:Terminal.ConductingEquipment ?s. "
            "?t c:Terminal.ConnectivityNode ?cn. " 
            "?cn c:IdentifiedObject.name ?bus "
        "}} "
        "GROUP BY ?name ?mrid ?bus ?basev ?p ?q ?conn "
        "ORDER by ?name "
        ).format(fdrid=fdrid)
    
    # Set and execute the query.
    sparql.setQuery(query)
    ret = sparql.query()
    
    # Initialize output
    ec = pd.DataFrame(columns=['p', 'q', 'magS', 'mrid'])
    comm = {'power': 0+0*1j, 'num': 0}
    totalRes = 0+0*1j
    
    # Loop over the return
    for el in ret.bindings:
        # grab variables
        v = float(el['basev'].value)
        phs = el['phases'].value
        name = el['name'].value
        mrid = el['mrid'].value
        p = float(el['p'].value)
        q = float(el['q'].value)
        
        if v == TRIPLEX_V and ('s1' in phs or 's2' in phs):
            # Triplex (split-phase) load. 
            ec.loc[name, ['p', 'q', 'magS', 'mrid']] = \
                [p, q, math.sqrt((p**2 + q**2)), mrid]

            # Increment counter of total residential power
            totalRes += p + 1j*q
            
        elif v == COMMERCIAL_V:
            # Track commercial/industrial loads
            comm['power'] += p + q*1j
            comm['num'] += 1
        else:
            raise UserWarning(\
                ('Unexpected load from blazegraph: '
                '  name: {}\n  voltage: {}\n  phases: {}'.format(name, v,
                                                                 phs)
                 )
                              )
        
    return ec, comm, abs(totalRes)

def insertHouse(sparql, ecName, ecID, houseNum, houseData):
    """Insert a single house into the CIM triplestore.
    """
    # Initialize query
    q = (constants.prefix + 'INSERT DATA { ')
    
    # Get MRIDs as strings
    ecIDStr = str(ecID)
    hIDStr = str(uuid4())
    # Need an underscore on the ID.
    if hIDStr[0] != '_':
        hIDStr = '_' + hIDStr
    
    # Define strings for the house and energy consumer.
    house = '<' + constants.blazegraph_url + '#' + hIDStr + '>'
    ec = '<' + constants.blazegraph_url + '#' + ecIDStr + '>'
    
    # Define string for attaching house to energy consumer
    q += (\
          house + ' a c:House. ' +
          house + ' c:IdentifiedObject.mRID \"' + hIDStr + '\". ' +
          (house + ' c:IdentifiedObject.name \"' + ecName + '_house_' + 
            str(houseNum) + '\". ') +
          house + ' c:House.EnergyConsumer ' + ec + '. '
        )
    
    # Loop over attributes 
    a = []
    for name, value in houseData.iteritems():
        # If the value is nan, skip it. e.g. if we don't have a cooling system
        # the cooling setpoint will be nan.
        if pd.isnull(value):
            continue
         
        # Enumerations are handled differently
        try:
            # Enumerations are handled differently
            valStr = constants.cim100 + ENUM[name] + '.' + str(value) + '>. ' 
        except KeyError:
            # Non-enumeration
            valStr = '\"' + str(value) + '\". '
            
            
        a.append(house + ' c:House.' + name + ' ' + valStr)
        
    # Update query
    q += ''.join(a) + '}'
    
    # Make update in triplestore.
    sparql.setQuery(q)
    ret = sparql.query()
    pass

if __name__ == "__main__":
    # Get command line arguments
    import argparse
    
    # Initialize parser
    parser = argparse.ArgumentParser(description="Add houses to a CIM model.")
    
    # We'll need a fdrid
    parser.add_argument("fdrid", help=("Full GUID of the feeder "
                                       "to add houses to."))
    
    # Also grab the region qualifier.
    parser.add_argument("region", help=("Climate region the feeder "
                                        "exists in. Valid options "
                                        "are r1, r2, r3, r4, or r5."))
    
    # Get args
    args = parser.parse_args()
    
    # Call main function.
    main(fdrid=args.fdrid, region=args.region)
    
    