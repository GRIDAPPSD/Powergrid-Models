from cimgraph.databases import XMLFile
from cimgraph.models import FeederModel

from cimloader.databases import ConnectionParameters, BlazegraphConnection

# from cimbuilder.feeder_builder.insert_measurements import create_all_discrete_measurements
# from cimbuilder.feeder_builder.insert_measurements import create_all_analog_measurements
# from cimbuilder.feeder_builder import insert_measurements
# from cimbuilder.feeder_builder import insert_houses
# from cimbuiler.feeder_builder import dss_to_cim


# # for filename in directtory:
# params = ConnectionParameters(filename=filename, cim_profile='cimhub_2023', iec61970_301=8)
# xml_file = XMLFile(params)
# feeder_model = FeederModel(connection=xml_file, container=cim.Feeder(), distributed=False)

# discretes = create_all_discrete_measurements(feeder_model)
# utils.write_xml(discretes, 'discretes.xml')

# analogs = create_all_analog_measurements(feeder_model)
# utils.write_xml(analogs, 'analogs.xml')

# params = ConnectionParameters(url = "http://localhost:8889/bigdata/namespace/kb/sparql")
# blazegraph = BlazegraphConnection(params)
# loader = BlazegraphUploader(params)

# blazegraph.drop_all()
# loader.upload_from_file(filepath="/home/ande188/CIM-Graph/tests/test_models", filename="ieee13.xml")

# loader.upload_from_file(filepath='./', filename='analogs.xml')
# loader.upload_from_file(filepath='./', filename='discretes.xml')


