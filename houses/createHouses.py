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
from eia_recs.housingData import OUTFILE, VARCAT, VARVALUE, CLIMATE_REGION_PUB

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
        loadDf will have a 'houses' column, which contains a dataframe of 
            housing characteristics for each load (row) in loadDf.
        """
        # First, estimate how many houses we'll be placing.
        guess = self.estimateTotalHouses(magS=magS)
        
        # Create a pandas Series to be used for updating the distribution with
        # each housing type selection.
        housing = self.data['TYPEHUQ'].copy(deep=True)
        
        self.housing = housing * guess
        
        count = 0
        # Loop over each load and add houses.
        for loadName, data in loadDf.iterrows():
            
            # Draw a housing type from the distribution and then draw square
            # footages for each house that will be added to the load/xfmr.
            housingType, sqftArray = self.typeAndSQFTForLoad(data.loc['magS'])
            
            # Initialize a DataFrame for these houses. We'll be mapping 
            # EIA RECS codes into the codes used for the house model in 
            # PNNL's CIM extension.
            houseDf = pd.DataFrame(data=sqftArray, columns=['floorArea'])
            
            # TODO: draw all the other house characteristics, add to the
            # houseDf.
            
            # Add this DataFrame to the larger loadDf
            loadDf.loc[loadName, 'houses'] = houseDf
            
            count += 1
            pass
            
            # 
        
        # Done!   
        return loadDf
    
    def drawAC(self, n):
        """Draw all AC related parameters for 'n' houses
        """
        # First, determine whether or not the object has AC
    
    def typeAndSQFTForLoad(self, magS):
        """Randomly draw a housing type, then choose number of housing units.
        
        INPUTS:
        magS: Magnitude of peak apparent power for load in question (VA).
        
        OUTPUTS:
        housingType: Selected housing type string.
        sqftArray: numpy array of square footages for houses to be added.
        """
        # Grab the denominator needed to normalize self.housing.
        denom = self.housing.sum()
        
        if denom > 0:
            # Convert the housing to probabilities
            p = self.housing / denom
        else:
            # If all our housing has been zeroed out, we'll need to default to the
            # distribution we started with.
            self.log.warn('All houses from self.housing have been depleted, '
                           + 'falling back on initial distribution.')
            p = self.data['TYPEHUQ']
        
        # Draw a housing type.
        housingType = self.rand.choice(self.housing.index, p=p)
        
        # Initialize array for tracking square footage.
        sqftArray = []
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
            sqftArray.append(s[0])
            
            # Increment the total.
            totalSqft += s
        
        # Grab the number of housing units for convenience.
        numUnits = len(sqftArray)
        
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
        return housingType, np.array(sqftArray)
    
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