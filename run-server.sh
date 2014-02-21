#!/usr/bin/env bash

# Usage
# run_server.sh <lt_port> <lt_client_id>


cd groovy-src
LT_GROOVY_LOG=true groovy -cp ../classes:. LTServer.groovy $@
