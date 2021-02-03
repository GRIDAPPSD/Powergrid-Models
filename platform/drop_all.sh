declare -r DB_URL="http://localhost:8889/bigdata/namespace/kb/sparql"

curl -D- -X POST $DB_URL --data-urlencode "update=drop all"
