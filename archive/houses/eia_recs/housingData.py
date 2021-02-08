'''
Module to map relevant EIA RECS data into climate regions.

For now, we're creating a very crude house model. In the future, RECS has a
wealth of additional data we can leverage to augment this.

Created on Jun 21, 2018

@author: thay838
'''
#******************************************************************************
# IMPORTS
#******************************************************************************
# Standard library
import json

# Installed packages:
import numpy as np
import pandas as pd
# import matplotlib.pyplot as plt

#******************************************************************************
# CONSTANTS
#******************************************************************************
OUTFILE = 'housing_data.json'

#******************************************************************************
# EIA RECS DEFINITIONS AND MAPPINGS
#******************************************************************************
# Define columns we care about in recs2015_public_v3.csv. At first I tried to
# keep this in order of occurence in the 'codebook2015_public_v3.xlsx' file,
# but that got difficult and annoying very quickly.
#
cols = ['TYPEHUQ', 'YEARMADERANGE', 'ADQINSUL', 'CLIMATE_REGION_PUB',
        'TOTSQFT_EN', 'NWEIGHT', 'AIRCOND', 'CENACHP', 'TEMPHOMEAC',
        'TEMPHOME', 'HEATHOME', 'EQUIPM', 'STORIES', 'DRAFTY']

# Notes on columns which aren't described in more depth later:
# 
# TOTSQFT_EN: "Total square footage (used for publication)." Note that RECS has
# data for total uncooled square footage and total unheated square footage that
# could be used for more sophisticated modeling.
#
# NWEIGHT: "Final sample weight"
#
# TEMPHOMEAC: "Summer temperature when someone is home during the day." 50-90,
# -2 --> not applicable. NOTE: There are also temperatures for "when no on is
# home" and "at night." Rather than try to do a weird weighted average, I'll
# just be using the "when someone is home" as the cooling setpoint.
#
# TEMPHOME: "Winter temperature when someone is home during the day." 50-90, -2
# --> not applicable. See note on "TEMPHOMEAC" about home vs. away vs. night.


# Map variable codes to their values. Using dicts because we can't use simple
# indexing for everything due to the EIA's data.

# Variables for categorical data that will be tracked for each home.
VARCAT = {
    # "Air conditioning equipment used."
    'AIRCOND': {
        1: 'Yes',
        0: 'No'
    },
    
    # "Central air conditioner is a heat pump."
    'CENACHP': {
        1: 'Yes',
        0: 'No',
        -2: 'Not applicable'
    },
    
    # "Space heating used."
    'HEATHOME': {
        1: 'Yes',
        0: 'No'
    },
    
    # "Range when housing unit was built"
    'YEARMADERANGE': {
        1: 'Before 1950',
        2: '1950 to 1959',
        3: '1960 to 1969',
        4: '1970 to 1979',
        5: '1980 to 1989',
        6: '1990 to 1999',
        7: '2000 to 2009',
        8: '2010 to 2015'
    },
    
    # "Level of insulation"
    'ADQINSUL': {
        1: 'Well insulated',
        2: 'Adequately insulated',
        3: 'Poorly insulated',
        4: 'Not insulated'
    },
    
    # "Frequency of draft" (on website "Home is too drafty during the winter"
    'DRAFTY': {
        1: 'All the time',
        2: 'Most of the time',
        3: 'Some of the time',
        4: 'Never'
    },
    
    # "Main space heating equipment type"
    'EQUIPM': {
        2: 'Steam/hot water system with radiators or pipes',
        3: 'Central furnace',
        4: 'Heat pump',
        5: ('Built-in electric units installed in walls, ceilings, ' 
            + 'baseboards, or floors'),
        6: 'Built-in floor/wall pipeless furnace',
        7: 'Built-in room heater burning gas, oil, or kerosene',
        8: 'Wood-burning stove',
        9: 'Fireplace',
        10: 'Portable electric heaters',
        21: 'Some other equipment',
        -2: 'Not applicable'
    },
    
    # "Number of stories in a single-family home"
    'STORIES': {
        10: "One story",
        20: "Two stories",
        31: "Three stories",
        32: "Four or more stories",
        40: "Split-level",
        -2: "Not applicable"
    }
}

# Non-categorical data tracked for a distribution.
VARVALUE = ['NWEIGHT', 'TOTSQFT_EN', 'TEMPHOMEAC', 'TEMPHOME']

# Define list of variables we'll be binning. Note that we won't be using the
# weights for the temperature set-point responses. This is a judgement call:
# it seems that behavioral traits should not be weighted. While the details of
# their housing may be representative of a population, their behavior may not
# be.
HISTVAR = {'TOTSQFT_EN': 'weighted', 'TEMPHOMEAC': 'unweighted',
           'TEMPHOME': 'unweighted'}

# "Type of housing unit"
TYPEHUQ = {
    1: 'Mobile home',
    2: 'Single-family detached house',
    3: 'Single-family attached house',
    4: 'Apartment in a building with 2 to 4 units',
    5: 'Apartment in a building with 5 or more units'
}

# "Building America Climate Zone"
# NOTE: These are in order according to the old region numbering scheme. DO NOT
# change the order!
CLIMATE_REGION_PUB = [x.lower() for x in ['Marine', 'Cold/Very Cold',
                                        'Hot-Dry/Mixed-Dry', 'Mixed-Humid',
                                        'Hot-Humid']]

#******************************************************************************
# HELPERS
#******************************************************************************
def buildHouseDist():
    """Build a pd.Series object which will hold housing distribution data.
    
    This helper is necessary, because if we don't call this for each houseDist
    object we need, we'll get pointers and end up incidentally modifying data
    we didn't want to. I tried all sorts of copy/deepcopy stuff before landing
    here.
    """
    # Get dictionaries for each categorical variable.
    houseDist = pd.Series()
    for k, v in VARCAT.items():
        houseDist[k] = pd.Series(np.zeros(len(v)), index=v.keys())
    
    # Initialize columns for tracking weighted samples and unweighted samples
    houseDist['num_weighted'] = 0
    houseDist['num_samples'] = 0
    
    # Initialize columns for each non-categorical variable. This doesn't feel
    # efficient, but otherwise we would have to do a LOT of work to pre-initialize.
    for v in VARVALUE:
        houseDist[v] = []
        
    return houseDist

#******************************************************************************
# RUN
#******************************************************************************
if __name__ == '__main__':
    #**************************************************************************
    # READ EIA DATA
    #**************************************************************************
    print('Reading RECS data...', end='', flush=True)
    recs = pd.read_csv('recs2015_public_v3.csv', sep=',', header=0,
                       usecols=cols)
    print('Done!', flush=True)
    
    #**************************************************************************
    # INITIALIZE
    #**************************************************************************
    # Create top-level dataframe to store data. Start by building a dictionary.
    dataDict = {}
    for _, v in TYPEHUQ.items():
        # Build a list of houseDists.
        distList = [buildHouseDist() for x in range(len(CLIMATE_REGION_PUB))]
        # Assign to the larger dictionary.
        dataDict[v] = distList
        
    # Track total housing units by climate region.
    dataDict['total_housing'] = np.zeros(len(CLIMATE_REGION_PUB))
    
    # Put into a pandas DataFrame
    data = pd.DataFrame(dataDict, index=CLIMATE_REGION_PUB)
    
    # Count entries which don't have a climate zone.
    noRegion = 0
    
    #**************************************************************************
    # MAP DATA
    #**************************************************************************
    print('Mapping and aggregating RECS data into climate regions...', end='',
          flush=True)
    # Map data by climate zone and by housing type. NOTE: pandas has a lot of
    # powerful indexing and binning options, but since we have to weight
    # everything by NWEIGHT, we lose the ability to use those features. It
    # makes the most sense to just loop over each row, add the qualities to the
    # appropriate distribution, then normalize at the end.
    for row in recs.itertuples():
        # Grab climate region.
        region = getattr(row, 'CLIMATE_REGION_PUB')
        
        # Skip null regions. If not null, cast to lowercase.
        if pd.isnull(region):
            noRegion += 1
            continue
        else:
            region = region.lower()
        
        # Grab housing type.
        housingType = getattr(row, 'TYPEHUQ')
        
        # Grab weight for this house.
        weight = getattr(row, 'NWEIGHT')
        
        # Grab view into housing distribution for this climate region and
        # housing type. Pull out view into this climate region's data.
        hDist = data.loc[region, TYPEHUQ[housingType]]
        
        # Loop over categorical variables, add weight to the category count.
        for v in VARCAT.keys():
            value = getattr(row, v)
            # Since hDist is a view into data, data gets updated by this call.
            # This adds this housing unit's weight to the correct category for
            # this variable.
            hDist.loc[v].loc[value] += weight
            
        # Increment weighted and unweighted number of houses.
        hDist.loc['num_weighted'] += weight
        hDist.loc['num_samples'] += 1
        
        # Increment counter for the region.
        data.loc[region, 'total_housing'] += weight
        
        # Loop over non-categorical data and append.
        for v in VARVALUE:
            value = getattr(row, v)
            hDist.loc[v].append(value)
    
    print('Done!', flush=True)
    print('{} rows without climate regions were skipped'.format(noRegion))
    
    #**************************************************************************
    # BIN AND NORMALIZE DATA
    #**************************************************************************
    print('Normalizing and binning data...', end='', flush=True)
    # Loop over data and create binned distributions. We'll use a regular dict
    # for the output so it can simply be saved to json.
    out = {}
    
    for j in range(data.shape[0]):
        # Extract the region name, initialize field.
        region = data.index[j]
        out[region] = {}
        
        # Initialize dict to hold relevant distributions.
        out[region]['TYPEHUQ'] = {}
        
        # Loop over the housing types.
        for h, hName in TYPEHUQ.items():
            # Grab view into housing distribution for this climate region and 
            # housing type. Pull out view into this climate region's data
            hDist = data.loc[region, hName]
        
            # Add fraction of total.
            out[region]['TYPEHUQ'][hName] = hDist.loc['num_weighted'] \
                / data.loc[region, 'total_housing']
                
            # Initialize.
            out[region][hName] = {}
            
            # Compute percentages for the categorical data.
            for v in VARCAT.keys():
                # Drop any "Not applicable" rows (keyed with -2).
                try:
                    s = hDist.loc[v].drop(-2)
                except KeyError:
                    s = hDist.loc[v]
                
                # If all are 0, move along (eg "stories" for apartment/mobile)
                if not s.any():
                    continue
                
                # Get the sum of this series.
                ss = s.sum()
                
                # Normalize.
                n = s.divide(ss)
                
                # Assign to output.
                out[region][hName][v] = n.to_dict()
                
            # Compute distributions for non-categorical data.
            for v, w in HISTVAR.items():
                # Grab data
                x = pd.Series(hDist.loc[v])
                # Filter out -2's
                notNeg2 = x != -2
                x = x[notNeg2]
                
                # Determine whether or not to use weights.
                if w == 'weighted':
                    weights = pd.Series(hDist.loc['NWEIGHT'])
                    weights = weights[notNeg2]
                elif w == 'unweighted':
                    weights = None
                else:
                    UserWarning('Unexpected weight string, {}'.format(w))
                    
                # Use a square root rule to get number of bins.
                bins = np.ceil(np.sqrt(len(x))).astype('int')
                
                '''
                # If we have more than 10 bins, cut the number of bins in half.
                if bins > 10:
                    bins = np.ceil(bins / 2).astype('int')
                '''
                
                # Bin the data
                hist, bin_edges = np.histogram(x, bins=bins, weights=weights)
                
                # Convert to discrete probability mass function.
                pmf = hist / hist.sum()
                
                # Assign. Convert numpy arrays to list so we can save result.
                out[region][hName][v] = {}
                out[region][hName][v]['pmf'] = pmf.tolist()
                out[region][hName][v]['bin_edges'] = bin_edges.tolist()
                
    print('Done!', flush=True)
    #**************************************************************************
    # SAVE OUTPUT
    #**************************************************************************
    print('Saving data to file: {}...'.format(OUTFILE), end='', flush=True)
    
    with open(OUTFILE, 'w') as outfile:
        json.dump(out, outfile)
        
    print('Done!')

#******************************************************************************
# VISUALIZE DATA
#******************************************************************************
'''
# Plot histograms of data to aid in final binning and normalization decisions.

# Define list of variables we'll be binning.
HISTVAR = ['TOTSQFT_EN', 'TEMPHOMEAC', 'TEMPHOME']

# Loop over climate regions. We definitely want data seperated by climate
# regions, so we won't bother comparing distributions across regions.
for j in range(data.shape[0]):
    region = data.index[j]
    # Loop over housing types
    for housingType in data.iloc[j].index:
        for var in HISTVAR:
            # Grab data, filter out -2.
            x = pd.Series(data.iloc[j].loc[housingType].loc[var])
            # Filter out -2's.
            notNeg2 = x != -2
            x = x[notNeg2]
            # Grab weights, filter out -2
            weights = pd.Series(data.iloc[j].loc[housingType].loc['NWEIGHT'])
            weights = weights[notNeg2]
            
            # Use the square root rule of thumb to compute number of bins.
            bins = round(np.sqrt(len(x)).astype('int'))
            
            # Sort and scatter-plot the raw data. We'll have the y-values
            # increment by one so we can visually see density better.
            plt.figure()
            plt.scatter(x=np.sort(x), y=np.arange(1, len(x)+1))
            plt.title('{} for {} in region {}'.format(var,housingType,region))
            
            # Plot weighted and unweighted histograms. We'll use the square 
            # root rule of thumb for bin size, because the automatic binning
            # methods don't allow weights.
            plt.figure()
            plt.hist(x=x, bins=bins,
                     weights=weights)
            plt.title(('Weighted {} distribution for {} '
                       + 'in region {}').format(var,housingType, region))
            
            # Now unweighted.
            plt.figure()
            plt.hist(x=x, bins=bins)
            plt.title(('Unweighted {} distribution for {} '
                       + 'in region {}').format(var,housingType, region))
            
            # If we have a ton of bins (>=10), try plotting with half.
            if bins >= 10:
                plt.figure()
                plt.hist(x=x, bins=np.ceil(bins/2).astype('int'),
                         weights=weights)
                plt.title(('Half-bins, weighted {} distribution for {} '
                           + 'in region {}').format(var,housingType, region))
            
            print('Close all figures to continue.')
            plt.show()
            plt.close('all')
        
'''