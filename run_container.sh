#!/usr/bin/env bash

usage() {
    echo "USAGE : run_container.sh -t amdsrinivas/distributed-file-loader:latest -c CONFIG_FILE -d DB_MOUNT -p PAYLOAD_FILE -o PORT_TO_OPEN"
    exit 1
}

while getopts "t:c:d:p:o:" opt
do
  case $opt in
  t) _CONTAINER_TAG=$OPTARG ;;
  c) _CONFIG_FILE=$OPTARG ;;
  d) _DB_MOUNT=$OPTARG ;;
  p) _PAYLOAD_FILE=$OPTARG;;
  o) _OPEN_PORT=$OPTARG ;;
  *) usage ;;
  esac
done

if [ $# -ne 10 ]; then
  usage
  exit 1
fi

echo "docker run -v ${_PAYLOAD_FILE}:/products.csv -v ${_CONFIG_FILE}:/application.properties -v ${_DB_MOUNT}:/data/ -ti -p ${_OPEN_PORT}:`grep -m 1 server.port ${_CONFIG_FILE} | sed 's/server.port=//'`  ${_CONTAINER_TAG} /products.csv"
docker run -v ${_PAYLOAD_FILE}:/products.csv -v ${_CONFIG_FILE}:/application.properties -v ${_DB_MOUNT}:/data/ -ti -p ${_OPEN_PORT}:`grep -m 1 server.port ${_CONFIG_FILE} | sed 's/server.port=//'`  ${_CONTAINER_TAG} /products.csv