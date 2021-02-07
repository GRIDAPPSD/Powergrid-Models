source envars.sh

curl -D- -X POST $DB_URL --data-urlencode "update=drop all"
