'''
Created on Jun 25, 2018

@author: thay838
'''
#******************************************************************************
# IMPORTS + PATH
#******************************************************************************
# Standard library:
import logging
import os

# Installed packages:
import numpy as np
import pandas as pd

# Modules in this repository:
#
# Import some EIA RECS data constants from housingData.py.
from eia_recs.housingData import OUTFILE, TYPEHUQ, CLIMATE_REGION_PUB

# Get path to this directory
THIS_DIR = os.path.dirname(os.path.realpath(__file__))
# Folder in THIS_DIR which contains our datafile.
DATA_DIR = 'eia_recs'

#******************************************************************************
# CONSTANTS
#******************************************************************************

# Tuning parameter used in guessing how many houses there are per transformer.
# This is a more or less "out of the air" guess at the moment.
VA_PER_SQFT = 3

# We need to specify a power factor for the HVAC system. This could use a bit
# more research, here's something that seems reasonable:
HVAC_PF = (0.85, 0.95)

# Create a reversed dictionary of TYPEHUQ for lookup by string instead of code.
TYPEHUQ_REV = {v:k for k, v in TYPEHUQ.items()}

#******************************************************************************
# CLASS DEFINITION: createHouses
#******************************************************************************
class createHouses():
    """Class which uses data derived from EIA RECS to generate houses.
    
    EIA RECS data is used to create various distributions, and then house
    properties are drawn from these distributions.
    """
    
    def __init__(self, region, log=None, seed=None):
        """Reads derived EIA RECS data, set up log, seed random generator.
        
        INPUTS:
        region: String or integer indicating one of the five climate zones. If
            using a string, case doesn't matter. Valid options are:
            1: 'Marine'
            2: 'Cold/Very Cold'
            3: 'Hot-Dry/Mixed-Dry'
            4: 'Mixed-Humid'
            5: 'Hot-Humid'
        log: logging.Logger instance. Passing None does a simple setup.
        seed: integer between 0 and (2^32 - 1), inclusive.
            See numpy.random.RandomState.
        numHomesGuess: A guess at the upper bound for number of housing units.
            A good rule of thumb could be 5x number of secondary transformers?
        
        OUTPUTS:
        self: createHouses instance with the following properties:
            log: logging.Logger instance for logging
            data: pandas DataFrame holding the derived EIA RECS data for the
                given region.
            housing: Set as None in constructor, but destined to be a pandas
                Series which will be used as the population for drawing housing
                types.
            rand: numpy.random.RandomState object. Making our own instance for
                thread safety.
        """
        # Check region input, get valid string.
        regionStr = self._getRegionStr(region=region)
        
        # Read data.
        data = pd.read_json(THIS_DIR + os.sep + DATA_DIR + os.sep + OUTFILE)
        
        # Extract data only for the given region.
        self.data = data.loc[:, regionStr]
        
        # Convert TYPEHUQ dictionary (distribution of housing types) to a
        # pandas series
        self.data['TYPEHUQ'] = pd.Series(self.data['TYPEHUQ'])
        
        # Setup the log.
        if log is None:
            # No log
            self.log = logging.getLogger(__name__)
        else:
            # Use the given log.
            self.log = log
            
        # Create a numpy.random RandomState
        self.rand = np.random.RandomState(seed=seed)
        
        # Initialize 'housing' variable, which is set later.
        self.housing = None
        
    def _getRegionStr(self, region):
        """Helper to validate given region and return the appropriate string.
        """
        if isinstance(region, int):
            if region > len(CLIMATE_REGION_PUB) or region < 1:
                raise UserWarning("If providing an integer input for "
                                  + "'region,' it must be between "
                                  + "1 and {}".format(len(CLIMATE_REGION_PUB)))
            
            # Grab the region as a string.
            regionStr = CLIMATE_REGION_PUB[region-1]
            
        elif isinstance(region, str):
            # Cast to lower case.
            regionStr = region.lower()
            
            # Create a list of numbers as strings. Command line arguments are
            # parsed as strings, so we need this case.
            nums = [str(x+1) for x in range(len(CLIMATE_REGION_PUB))]
            
            inRegion = regionStr in CLIMATE_REGION_PUB
            inNums = regionStr in nums
            
            # Check.
            if not (inRegion or inNums):
                raise UserWarning(("Bad region string: {}. Should be "
                                  + "in set {}.").format(region, nums + \
                                                         CLIMATE_REGION_PUB))
            
            # If it's a number, cast to int and index into CLIMATE_REGION_PUB
            if inNums:
                regionStr = CLIMATE_REGION_PUB[int(regionStr)-1]
            
        else:
            raise UserWarning("'region' input must be an integer or string! "
                              + "Given a {}.".format(type(region)))
            
        return regionStr
    
    def genHousesForFeeder(self, loadDf, magS):
        """'Main' function for class. Generates houses for a given set of loads
        
        NOTE: The intention is that this function is called by the insertHouses
            module, and so the inputs are taken in a form they exist there.
        
        INPUTS:
        loadDf: pandas dataframe, indices are names of loads, and column 'magS'
            indicates apaprent power magnitude under peak conditions
        magS: total magnitude of all loads.
        
        OUTPUT:
        housingDict: dictionary with nodes/loads (index from loadDf) as keys.
            Each key maps to a two-element tuple - the first is a dataframe
            containing housing data and the second is the chosen housing type
            code (see eia_recs.housingData.TYPEHUQ). Using the code instead of
            the full name for efficiency.
            
        note: We may want to use a different datatype other than a dictionary.
            When we loop back over the items, this will be inefficient as 
            the key will have to be looked up every time.
        """
        # First, estimate how many houses we'll be placing.
        guess = self.estimateTotalHouses(magS=magS)
        
        # Create a pandas Series to be used for updating the distribution with
        # each housing type selection.
        housing = self.data['TYPEHUQ'].copy(deep=True)
        
        self.housing = housing * guess
        
        # Track how many loads we handled.
        self.loadCount = 1
        self.houseCount = pd.Series(data=np.zeros(len(self.data['TYPEHUQ'])),
                                    index=self.data['TYPEHUQ'].index)
        # Initialize flag so we aren't throwing too many warnings.
        self.warnFlag = False
        
        # Since the number of houses per load/node is variable, we're best
        # suited to use a dictionary here.
        housingDict = {}
        
        # Loop over each load and add houses.
        for loadName, data in loadDf.iterrows():
            
            # Draw a housing type from the distribution and then draw square
            # footages for each house that will be added to the load/xfmr.
            housingType, floorArea = self.typeAndSQFTForLoad(data.loc['magS'])
            
            # Number of houses is the length of the floorArea
            n = len(floorArea)
            
            # Initialize a DataFrame for these houses. We'll be mapping 
            # EIA RECS codes into the codes used for the house model in 
            # PNNL's CIM extension.
            houseDf = pd.DataFrame(data=floorArea, columns=['floorArea'])
            
            # Add cooling (AC) information:
            coolingSystem, coolingSetpoint = self.drawAC(housingType, n)
            houseDf['coolingSystem'] = coolingSystem
            houseDf['coolingSetpoint'] = coolingSetpoint
            # Using the above syntax because apparently assign returns a copy,
            # which feels very inefficient...
            '''
            houseDf.assign(coolingSystem=coolingSystem,
                           coolingSetpoint=coolingSetpoint)
            '''
            
            # Add heating information:
            heatingSystem, heatingSetpoint = self.drawHeating(coolingSystem,
                                                              housingType, n)
            houseDf['heatingSystem'] = heatingSystem
            houseDf['heatingSetpoint'] = heatingSetpoint
            
            # Draw HVAC power system, using nan if there's neither electric 
            # heating nor cooling
            hvacPowerFactor = pd.Series(self.rand.uniform(low=HVAC_PF[0],
                                        high=HVAC_PF[1], size=n))
            # Use np.nan when both heating and cooling are absent.
            hvacPowerFactor[(houseDf['coolingSystem'] == 'none') & \
                            (houseDf['heatingSystem'] == 'none')] = np.nan
            # Use unity(1) when there's no cooling, but heating is resistive.
            hvacPowerFactor[((houseDf['coolingSystem'] == 'none') & \
                            (houseDf['heatingSystem'] == 'resistance'))] = 1
            houseDf['hvacPowerFactor'] = hvacPowerFactor
            
            # Draw number of stories.
            houseDf['numberOfStories'] = self.drawNumStories(housingType, n)
            
            # Draw thermal integrity.
            houseDf['thermalIntegrity'] = \
                self.drawThermalIntegrity(housingType, n)
            
            # Lookup housing code and assign houseDf and code to dictionary.
            hCode = TYPEHUQ_REV[housingType]
            housingDict[loadName] = (houseDf, hCode)
            
            self.loadCount += 1
            self.houseCount[housingType] += n
            
        # Print totals to the log. 
        self.log.info(('{} loads were accounted for, totaling {} '
                       + 'housing units').format(self.loadCount,
                                                 self.houseCount.sum())
                       )
        
        # Print final distribution to the log.
        self.log.info('Final housing breakdown:\n{}'.format(self.houseCount))
                        
        # Done!
        return housingDict
    
    def drawThermalIntegrity(self, housingType, n):
        """Decide thermal integrity levels for 'n' houses.
        
        housingType: string (from self.data['TYPEHUQ'].index) representing
            housing type.
        n: number of houses to determine properties for.
        
        OUTPUTS:
        thermalIntegrity: pandas Series indicating thermal integrity level for
            each of the 'n' housing units. Options (from GridLAB-D,
            http://gridlab-d.shoutwiki.com/wiki/Thermal_integrity_level) are:
            
            'veryLittle': old, uninsulated
            'little': old, insulated
            'belowNormal': old, weatherized
            'normal': old, retrofit upgraded
            'aboveNormal': moderately insulated
            'good': very well insulated
            'veryGood': extermely well insulated
            'unknown': unkown. "Built-in defaults or user-specified values are
                used."
                
        NOTES:
        We'll be using three parameters to make a decision here:
        YEARMADERANGE, ADQINSUL, and DRAFTY.
        
        year made will decide if house is 'old' or not, adequate insulation
        will determine 'insulated,' and 'drafty' will determine weatherized.
        
        TODO: I don't love this one... The mapping is too.. hand-wavy. Maybe
            instead of this intersection method we could come up with a 'score'
            where equal points are possible from the three categories?
        TODO: Get someone to review and weigh in.
        """
        # Grab pointer to relevant data for readability.
        data = self.data[housingType]
        
        # For each house, draw each characterstic from the relevant
        # distributions.
        
        # Year made range.
        (optionsY, pY) = zip(*data['YEARMADERANGE'].items())
        year = pd.Series(self.rand.choice(a=optionsY, size=n,
                                          p=pY).astype(int))
        
        # Insulation.
        (optionsI, pI) = zip(*data['ADQINSUL'].items())
        insul = pd.Series(self.rand.choice(a=optionsI, size=n,
                                           p=pI).astype(int))
        
        # Drafty.
        (optionsD, pD) = zip(*data['DRAFTY'].items())
        draft = pd.Series(self.rand.choice(a=optionsD, size=n,
                                           p=pD).astype(int))
        
        # Initialize return.
        thermalIntegrity = pd.Series(index=year.index)
        
        # Loop and select thermalIntegrity based on the combination of year,
        # insul, and draft.
        for index, y in year.iteritems():
            # Grab other information.
            i = insul.iloc[index]
            d = draft.iloc[index]
            
            # Start with insulation we'll narrow from there.
            if i == 1:
                # Well insulated.
                choicesI = ['veryGood', 'good', 'aboveNormal']
            elif i == 2:
                # Adequately insulated.
                choicesI = ['aboveNormal', 'normal']
            elif i == 3:
                # Poorly insulated.
                choicesI = ['normal', 'belowNormal', 'little']
            elif i == 4:
                # Not insulated.
                # Here we're going to take a shortcut and just assign
                # 'veryLittle'
                thermalIntegrity.iloc[index] = 'veryLittle'
                continue
            
            # Define more options based on draftiness
            if d == 1:
                # All the time
                choicesD = ['little', 'veryLittle']
            elif d == 2:
                # Most of the time
                choicesD = ['belowNormal', 'little', 'veryLittle']
            elif d == 3: 
                # Some of the time
                choicesD = ['aboveNormal', 'normal']
            elif d == 4:
                # Never
                choicesD = ['veryGood', 'good']
                
            # Find common items in choicesI and choicesD.
            common = list(set(choicesI).intersection(choicesD))
            
            # If the length of the list is 1, assign and move on.
            if len(common) == 1:
                thermalIntegrity.iloc[index] = common[0]
                continue
            
            # Define options based on year of construction. This is uber hand-
            # wavy. I've made it so there's only a single overlap between cases
            # as you work your way down the if/else cases
            if y in [1, 2, 3]:
                # Pre-1950 to 1969
                choicesY = ['belowNormal', 'little', 'veryLittle']
            elif y in [4, 5, 6]:
                # 1970 to 1999
                choicesY = ['good', 'aboveNormal', 'normal',  'belowNormal']
            else:
                # 2000 to 2015
                choicesY = ['veryGood', 'good']
            
            # Get common options
            common = list(set(common).intersection(choicesY))
            
            # Pick thermal integrity value.
            if len(common) == 1:
                # Use what's leftover
                val = common[0]
            elif len(common) == 0:
                # Fall back and draw from insulation.
                # TODO: maybe we should bias the draw based on insulation?
                val = self.rand.choice(choicesI)
            elif len(common) > 1:
                # Randomly draw from remaining items..
                val = self.rand.choice(list(common))
            
            # Assign.
            thermalIntegrity.iloc[index] = val
        
        # Done!
        return thermalIntegrity
        
        
    def drawNumStories(self, housingType, n):
        """Draw number of stories for 'n' houses.
        
        INPUTS:
        housingType: string (from self.data['TYPEHUQ'].index) representing
            housing type.
        n: number of houses to determine properties for.
        
        OUTPUTS:
        numberOfStories: pandas Series indicating the number of stories for
            each of the 'n' housing units. Note that mobile homes and
            apartments will always be 1 story.
        """
        # If housing type has one story be definition, set and return.
        if housingType in ['Mobile home',
                           'Apartment in a building with 2 to 4 units',
                           'Apartment in a building with 5 or more units']:
            numberOfStories = pd.Series(np.ones(n))
            return numberOfStories
        
        # Draw number of stories for single family housing.
        
        # Grab pointer to relevant data for readability.
        data = self.data[housingType]
        
        # Grab distribution of number of stories.
        (options, p) = zip(*data['STORIES'].items()) 
        numberOfStories = pd.Series(self.rand.choice(a=options, size=n,
                                              p=p).astype(int))
        
        # Map results to number of stories (see definition in housingData.py).
        # Notes: 'Four or more stories' is being mapped to 4, and 'Split-level'
        # is being mapped to 2.
        numberOfStories = numberOfStories.map({10: 1, 20: 2, 31: 3, 32: 4,
                                               40: 2})
        
        # Done.
        return numberOfStories
        
    
    def drawHeating(self, coolingSystem, housingType, n):
        """Draw all heating related parameters for 'n' houses.
        
        INPUTS:
        coolingSystem: one of the returns from "drawAC." Used for heat 
            pumps.
        housingType: string (from self.data['TYPEHUQ'].index) representing
            housing type.
        n: number of houses to determine properties for.
        
        OUTPUTS:
        heatingSystem: pandas Series with a string for each of the 'n' housing
            units. Options are 'none,' 'electric,' 'heatPump,' or 'gas.'
        heatingSetpoint: pandas Series with a number for each of the 'n' 
            housing units. Represents heating setpoint in degrees F. If the
            corresponding heating system is 'none,' the setpoint will be
            np.nan 
        """
        
        # Grab pointer to relevant data for readability.
        data = self.data[housingType]
        
        # First, determine whether or not the houses have heating
        (options, p) = zip(*data['HEATHOME'].items()) 
        hasHeat = pd.Series(self.rand.choice(a=options, size=n,
                                             p=p).astype(int))
        
        # Draw heating system type (see eia_recs.housingData.EQUIPM for
        # options). To keep things simple, we'll just draw even if hasHeat is
        # false. While this isn't optimally efficient, it's silly to chase
        # micro-optimizations early on, especially when it'll make the code
        # less readable.
        (optionsHT, pHT) = zip(*data['EQUIPM'].items())
        heatType = pd.Series(self.rand.choice(a=optionsHT, size=n,
                                              p=pHT).astype(int))
        
        # Map combination of hasHeat and heatType into 'none,' 'gas,'
        # 'heatPump,' or 'resistance'
        heatingSystem = pd.Series(index=heatType.index)
        for index, hH in hasHeat.iteritems():
            # Grab type
            hT = heatType.iloc[index]
            
            # Handle different heating type cases.
            if coolingSystem.iloc[index] == 'heatPump':
                # If the cooling system is heat pump, force the heating system
                # to be a heat pump. While this may cause slight diversions
                # from the distribution, theoretically anyone who has a heat
                # pump will use it for heating as well as cooling... They're
                # too damned expensive to do something else (personal
                # experience)
                h = 'heatPump'
            elif not hH:
                # No heating system
                h = 'none'
            else:
                # Handle heat pumps.
                if hT == 4:
                    # heat pump. We need to draw until we get a non-heat pump.
                    itCount = 0
                    while hT != 4:
                        hT = pd.Series(self.rand.choice(a=optionsHT, size=1,
                                                        p=pHT).astype(int))
                        
                        # Infinite loop protection.
                        itCount += 1
                        if itCount > 100:
                            raise UserWarning("Looks like we're stuck in a loop.")
                        
                # Map.
                h = self.mapHeating(code=hT)
            
            # Assign.
            heatingSystem.iloc[index] = h
            
            
        # Draw heating setpoint.
        heatingSetpoint = self.drawFromDist(\
            pmf=data['TEMPHOME']['pmf'],
            bin_edges=data['TEMPHOME']['bin_edges'], num=n)
        
        # Make all the coolingSetpoints nan if there's no coolingSystem
        heatingSetpoint[heatingSystem == 'none'] = np.nan
        
        return heatingSystem, heatingSetpoint
    
    @staticmethod
    def mapHeating(code):
        """Helper function for mapping heating codes to values.
        """
        if (code == 5) or (code == 10):
            # 'Built-in electric units installed in walls, ceilings,
            # baseboards, or floors' or 'Portable electric heaters'
            h = 'resistance'
        else:
            # Cast everything else into "gas"
            h = 'gas'
        
        return h
        
    
    def drawAC(self, housingType, n):
        """Draw all AC related parameters for 'n' houses.
        
        INPUTS:
        housingType: string (from self.data['TYPEHUQ'].index) representing
            housing type.
        n: number of houses to determine properties for.
        
        OUTPUTS:
        coolingSystem: pandas Series which containes either 'electric' for 
            standard AC, 'none' if there's no cooling, or 'heatPump' if the AC
            is from a heatpump.
        coolingSetpoint: pandas Series containing a thermostat cooling setpoint
            (in degrees F) for each home. np.nan will be used for homes with
            'none' in coolingSystem
        """
        # Grab pointer to relevant data for readability.
        data = self.data[housingType]
        
        # First, determine whether or not the object has AC
        (options, p) = zip(*data['AIRCOND'].items()) 
        hasAC = pd.Series(self.rand.choice(a=options, size=n, p=p).astype(int))
        
        # Draw cooling system type (either heatpump or AC). To keep things
        # simple, we'll just draw even if hasAC is false. While this isn't
        # optimally efficient, it's silly to chase micro-optimizations early
        # on, especially when it'll make the code less readable.
        (options, p) = zip(*data['CENACHP'].items())
        isHP = pd.Series(self.rand.choice(a=options, size=n, p=p).astype(int))
        
        # Map combination of hasAC and isHP into coolingSystem return.
        # NOTE: attempt to use Series.combine didn't work because mapping ints
        # to strings failed.
        coolingSystem = pd.Series(index=hasAC.index)
        for index, hS in hasAC.iteritems():
            # Grab pointers to heatpump value
            HP = isHP.iloc[index]
            
            # Assign type accordingly
            if not hS:
                # no cooling
                c = 'none'
            elif HP:
                c = 'heatPump'
            else:
                c = 'electric'
                
            coolingSystem.iloc[index] = c
        
        # Draw cooling setpoint.
        coolingSetpoint = self.drawFromDist(\
            pmf=data['TEMPHOMEAC']['pmf'],
            bin_edges=data['TEMPHOMEAC']['bin_edges'], num=n)
        
        # Make all the coolingSetpoints nan if there's no coolingSystem
        coolingSetpoint[coolingSystem == 'none'] = np.nan
        
        return coolingSystem, coolingSetpoint
    
    def typeAndSQFTForLoad(self, magS):
        """Randomly draw a housing type, then choose number of housing units.
        
        INPUTS:
        magS: Magnitude of peak apparent power for load in question (VA).
        
        OUTPUTS:
        housingType: Selected housing type string.
        housingCode: index from TYPEHUQ that matches up with the housingType
            string
        floorArea: pandas Series of square footages for houses to be added.
        """
        # Grab the denominator needed to normalize self.housing.
        denom = self.housing.sum()
        
        if denom > 0:
            # Convert the housing to probabilities
            p = self.housing / denom
        else:
            # If all our housing has been zeroed out, we'll need to default to the
            # distribution we started with.
            if not self.warnFlag:
                self.log.warn('All houses from self.housing have been '
                              + 'depleted, falling back on the initial '
                              + 'distribution, which will cause deviations '
                              + 'from the distribution since.')
                self.log.info(('{} loads were accounted for, totaling {} '
                               + 'housing units').format(self.loadCount,
                                                         self.houseCount.sum())
                               )
                # Set the warn flag so we don't throw this for the rest of the
                # houses.
                self.warnFlag = True
            
            # Grab the standard housing distribution.
            p = self.data['TYPEHUQ']
        
        # Draw a housing type.
        housingType = self.rand.choice(self.housing.index, p=p)
        
        # Initialize array for tracking square footage (use PNNL's house CIM
        # extension verbage).
        floorArea = []
        totalSqft = 0
        
        # Draw squarefootages for this housing type until we've "filled" the
        # transformer.
        #
        # TODO: I'd like to find a better method here: in reality, housing
        # units attached to the same transformer will be of a similiar size.
        # However, if we were to pick a single square footage than draw the
        # rest from, say, a Gaussian around that square footage, we wouldn't be
        # following the overall square footage distribution found in the data.
        # For consistency, I've decided to always follow distributions from the
        # EIA RECS data, but it doesn't have to be that way.
        while totalSqft * VA_PER_SQFT < magS:
            # Draw squarefootage and append.
            s = self.drawFromDist(\
                pmf=self.data[housingType]['TOTSQFT_EN']['pmf'],
                bin_edges=self.data[housingType]['TOTSQFT_EN']['bin_edges'],
                num=1)
            
            # Note: return comes back as a numpy array, but we just want the
            # value.
            floorArea.append(s[0])
            
            # Increment the total.
            totalSqft += s
        
        # Grab the number of housing units for convenience.
        numUnits = len(floorArea)
        
        # For now, warn if we didn't "pick enough" or picked "too many"
        # TODO: If things are way off, we should probably consider trying again
        cond1 = (housingType == 'Apartment in a building with 2 to 4 units') \
            and ((numUnits < 2) or (numUnits > 4))
        cond2 = (housingType == 'Apartment in a building with 5 or more units') \
            and (numUnits < 5)
        cond3 = (housingType == 'Single-family attached house') \
            and (numUnits < 2)
            
        if cond1 or cond2 or cond3:
            # Warn.
            self.log.warning(("Housing type '{}' chosen, but {} "
                              + "units generated.").format(housingType, numUnits))
        
        if denom > 0:
            # Subtract the number of houses generated from the housing to
            # ensure we follow the distribution for the whole model.
            self.housing.loc[housingType] = self.housing.loc[housingType] \
                - numUnits
                
            # Zero out negatives.
            self.housing[self.housing < 0] = 0
        
        # Done!
        return housingType, pd.Series(floorArea)
    
    def drawFromDist(self, pmf, bin_edges, num=1):
        """Select a discrete bin from a distribution, then use the uniform
            distribution to choose a value within that bin.
            
        INPUTS:
        pmf: probability mass function (sums to 1) for the distribution
        bin_edges: edges for binned data. These should have originated from a
            call to numpy's histogram function.
        """
        # Draw a bin. Note that bin_edges has one more element than pmf.
        leftInd = self.rand.choice(np.arange(0, len(pmf)), p=pmf)
        rightInd = leftInd + 1
        
        # Grab left and right bin edges.
        leftEdge = bin_edges[leftInd]
        rightEdge = bin_edges[rightInd]
        
        # Note: The numpy histogram documentation indicates all bins are helf-
        # open except for the last (righthand-most) bin. To be precise, 
        # if the right edge is the last bin, increment the right edge slightly
        # so that the true value is included in the draw from the uniform
        # distribution.
        if rightInd == len(bin_edges) - 1:
            rightEdge += np.nextafter(rightEdge, rightEdge + 1)
        
        # Draw value from the bin.
        value = self.rand.uniform(low=leftEdge, high=rightEdge, size=num)
        
        return value
    
    def estimateTotalHouses(self, magS):
        """Estimate total number of housing units that will be generated.
        
        This is used to help ensure we're doing a good job tracking the housing
            type distribution.
            
        Rough flow:
            1) Compute mean square footage by housing type
            2) Use factor (VA_PER_SQFT) to estimate peak power by housing type.
            3) Given distribution of housing types and associated average
                power, estimate how many housing units will be generated.
        
        INPUTS:
        magS: Magnitude of apparent power for all the loads in the system.
        
        OUTPUTS:
        num: Estimate of total number of housing units that will be generated.
        """
        
        # Initialize pandas series for holding mean square footages.
        meanSqft = np.zeros(len(self.data['TYPEHUQ']))
        
        # Loop over the housing types and compute the mean square footage.
        for housingInd, housingType in enumerate(self.data['TYPEHUQ'].index):
            # Grab bin_edges.
            bin_edges = np.array(\
                self.data[housingType]['TOTSQFT_EN']['bin_edges'])
            
            # Bin centers are left edge + half of the distance between bins.
            # We're grabbing centers because the uniform distribution is used
            # to pick a value within a bin.
            bin_centers = bin_edges[0:-1] \
                + ((bin_edges[1:] - bin_edges[0:-1]) / 2)
                
            # Mean square footage is the sum of the probabilities times the 
            # values.
            pmf = np.array(self.data[housingType]['TOTSQFT_EN']['pmf'])
            meanSqft[housingInd] = np.sum((bin_centers * pmf))
            
        # Use our (maybe trash) constant to convert square footages to power.
        meanPower = meanSqft * VA_PER_SQFT
        
        # Compute the mean power for all housing types.
        totalMean = np.sum(meanPower * self.data['TYPEHUQ'])
        
        # Estimate the nubmer of houses.
        num = magS / totalMean
        
        # Done!
        return num
            
    
if __name__ == '__main__':
    obj = createHouses(region=1)
    houseGuess = obj.estimateTotalHouses(6e6)
    '''
    for _ in range(20):
        housingType, sqftArray = obj.typeAndSQFTForLoad(magS=12000)
        print(housingType, sqftArray)
    pass
    '''