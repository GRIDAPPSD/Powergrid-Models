# Simple script to update the GridLAB-D taxonomy feeders directly from github.

import urllib.request
import urllib.parse
import os

# List the taxonomy of prototypical feeders.
feeders = [
    'GC-12.47-1',
    'R1-12.47-1',
    'R1-12.47-2',
    'R1-12.47-3',
    'R1-12.47-4',
    'R1-25.00-1',
    'R2-12.47-1',
    'R2-12.47-2',
    'R2-12.47-3',
    'R2-25.00-1',
    'R2-35.00-1',
    'R3-12.47-1',
    'R3-12.47-2',
    'R3-12.47-3',
    'R4-12.47-1',
    'R4-12.47-2',
    'R4-25.00-1',
    'R5-12.47-1',
    'R5-12.47-2',
    'R5-12.47-3',
    'R5-12.47-4',
    'R5-12.47-5',
    'R5-25.00-1',
    'R5-35.00-1'
]

# Github url:
url = 'https://raw.githubusercontent.com/gridlab-d/Taxonomy_Feeders/master/'

# Directory to save files in:
d = 'base_taxonomy'

# Loop, download, and save. 
for f in feeders:
    with urllib.request.urlopen(url + f + '.glm') as glm:
        glmStr = glm.read().decode('utf-8')
        with open(d + os.sep + 'orig_' + f + '.glm', 'w') as glmFile:
            glmFile.write(glmStr)
