#!/bin/bash

node delegator.js &
sleep 0.2
curl -d "{\"script\": \"console.log('a is '+a); \na=33; \nbuzzz$%^&*  \nvar b='ignoreme'; exports.c='dontignoreme';\", \"variables\" : {\"a\":25} }" -X POST http://localhost:8081/execute
killall node
