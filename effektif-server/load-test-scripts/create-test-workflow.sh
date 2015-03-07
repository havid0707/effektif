#/bin/sh

# creates the load test workflow

# param 1 is hostname
# param 2 is port

curl -H "Content-Type: application/json" \
  -d '{"activities":[{"type":"startEvent","id":"One","outgoingTransitions":[{"isToNext":true}]},{"type":"noneTask","id":"Two","outgoingTransitions":[{"isToNext":true}]},{"type":"receiveTask","id":"Three","outgoingTransitions":[{"isToNext":true}]},{"type":"noneTask","id":"Four","outgoingTransitions":[{"isToNext":true}]},{"type":"endEvent","id":"Five"}],"sourceWorkflowId":"Server test workflow"}' \
  http://$1:$2/deploy