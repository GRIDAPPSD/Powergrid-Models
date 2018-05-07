'''
Created on Apr 24, 2018

@author: thay838
'''
#******************************************************************************
# URL for blazegraph

# Using the default blazegraph installation as a standalone
blazegraph_url = "http://localhost:9999/blazegraph/namespace/kb/sparql"

# When running the platform in the docker, the blazegraph URL can be found in
# /gridappsd/conf/pnnl.goss.gridappsd.cfg. At the time of writing (04/24/18),
# there are two URLs. One for calling from inside the docker container, and one
# for calling from outside the docker container.

# URL from inside the docker container:
# blazegraph_url = "http://blazegraph:8080/bigdata/sparql"

# URL from outside the docker container:
# blazegraph_url = "http://localhost:8889/bigdata/sparql"

#******************************************************************************
# Prefix for blazegraph queries.

# cim17 is used in InsertMeasurements.py. Notice the lack of "greater than" at
# the end.
cim17 = '<http://iec.ch/TC57/2012/CIM-schema-cim17#'
# Prefix for all queries.
prefix = """PREFIX r: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX c: {cimURL}>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
""".format(cimURL=cim17)
#******************************************************************************
