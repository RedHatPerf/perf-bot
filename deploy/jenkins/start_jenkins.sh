#!/bin/bash

restart_jenkins() {
  if [ -z ${JENKINS_PID+x} ];
   then echo "No jenkins instance is running";
   else
        echo "Killing the running jenkins instance ($JENKINS_PID)"
        kill "$JENKINS_PID";
        sleep 5
  fi
  echo '(re)starting jenkins'
  /sbin/tini -s /usr/local/bin/jenkins.sh &
  JENKINS_PID="$!"
}

restart_jenkins

until curl -s --fail http://localhost:8080 > /dev/null; do
  echo 'Jenkins script: Jenkins not ready, waiting 1 sec...'
  sleep 1;
done;
echo "Jenkins script: Jenkins seems to be UP!".

wait $JENKINS_PID