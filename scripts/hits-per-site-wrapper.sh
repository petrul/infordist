#! /bin/sh

TERM=$1
SITE=$2

cd /home/dadi/work/scripts/ && ruby /home/dadi/work/scripts/hits-per-site.rb $TERM $SITE >> /home/dadi/events-diachronic-hits/$TERM-$SITE.log
