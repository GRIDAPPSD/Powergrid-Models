# EIA RECS Data
US Energy Information Administration (EIA) Residential Energy Consumption Survey (RECS) data is used to get regional housing and appliance data.

[Source.](https://www.eia.gov/consumption/residential/data/2015/ "EIA 2015 RECS Survey Data") Data downloaded 2018-06-21.

Climate regions come from Department of Energy (DOE) Office of Energy Efficiency and Renewable Energy (EERE). A map of the climate regions can be found [here](https://www.energy.gov/eere/buildings/building-america-climate-specific-guidance) and zone defintions can be found [here](https://www.energy.gov/eere/buildings/climate-zones).

There are two files in this directory: codebook2015_public_v3.csv and recs2015_public_v3.csv. The file recs2015_public_v3 contains the source EIA RECS data, and the codebook2015_public_v3.csv describes codes (column headers) used in recs2015_public_v3.csv.

## Note on Git LFS
The data files in this directory are stored using [Git Large File Storage](https://git-lfs.github.com/).

After installing, use the terminal from which you use git to navigate to the head of this repository and enter the command `git lfs install`.

You'll then need to run `git lfs pull` to get the files.