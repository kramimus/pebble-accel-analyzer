#!/usr/bin/env python

from argparse import ArgumentParser
from datetime import datetime, timedelta
import logging
import pandas as pd
import pymongo

EPOCH_START = datetime(1970, 1, 1)

db = pymongo.MongoClient().watch_data
log = logging.getLogger('aggregate_accel')

def _get_readings_in_range(start, end):
    readings = []
    start_epoch = (start - EPOCH_START).total_seconds() * 1000
    end_epoch = (end - EPOCH_START).total_seconds() * 1000
    for r in db.accel_data.find({'start_ts' : {'$gte' : start_epoch}, 'end_ts' : {'$lte' : end_epoch}},
                                sort=[('start_ts', pymongo.ASCENDING)]):
        readings += r['readings']
    log.info('Loaded %d readings', len(readings))

    df = pd.DataFrame(readings)
    idx = pd.to_datetime(df['ts'] / 1000, unit='s')
    return df.set_index(idx)

def get_activity_for_night(day_start):
    """ very crude metric for activity

    cannot just sum readings because there is always gravity producing "acceleration"

    diffing captures changes in the orientation with respect to gravity and
    thus means the watch is moving around
    """
    readings = _get_readings_in_range(day_start, day_start + timedelta(hours=8))
    diffed = readings[['x', 'y', 'z']].diff()
    return sum(diffed.abs().sum())

def options():
    parser = ArgumentParser(description="aggregate accelerometer activity over a day/night")
    parser.add_argument("--day", help="YYYY-MM-DD to process")
    return parser.parse_args()

def main():
    opts = options()

    day = pd.to_datetime(opts.day)
    print("%s: %d" % (day.strftime("%Y-%m-%d"), get_activity_for_night(day)))

if __name__ == '__main__':
    main()
