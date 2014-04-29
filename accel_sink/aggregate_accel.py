#!/usr/bin/env python

from argparse import ArgumentParser
from datetime import datetime, timedelta
import logging
import pandas as pd
import pylab
import pymongo
import pytz

EPOCH_START = datetime(1970, 1, 1, tzinfo=pytz.utc)
DATA_START = datetime(2014, 4, 1, tzinfo=pytz.utc)

db = pymongo.MongoClient("192.168.1.10").watch_data
log = logging.getLogger('')

def _get_readings_in_range(start, end):
    """ get all the readings in range

    slow because readings between chunks need to be resorted together
    """
    readings = []
    start_epoch = (start - EPOCH_START).total_seconds() * 1000
    end_epoch = (end - EPOCH_START).total_seconds() * 1000
    for r in db.accel_data.find({'start_ts' : {'$lte' : end_epoch}, 'end_ts' : {'$gte' : start_epoch}},
                                sort=[('start_ts', pymongo.ASCENDING)]):
        readings += [reading for reading in r['readings'] if reading['ts'] >= start_epoch and reading['ts'] <= end_epoch]
    readings.sort(key=lambda r: r['ts'])
    log.info('Loaded %d readings', len(readings))

    df = pd.DataFrame(readings)
    idx = pd.to_datetime(df['ts'] / 1000, unit='s')
    df = df.set_index(idx)
    return df

def get_activity_for_night(day_start, minutes_start, minutes_end):
    """ very crude metric for activity

    cannot just sum readings because there is always gravity producing "acceleration"

    diffing captures changes in the orientation with respect to gravity and
    thus means the watch is moving around
    """
    readings = _get_readings_in_range(day_start + timedelta(minutes=minutes_start),
                                      day_start + timedelta(minutes=minutes_end))
    diffed = readings[['x', 'y', 'z']].diff()
    day_str = day_start.strftime("%Y-%m-%d")
    readings[['x', 'y', 'z']].plot()
    pylab.savefig(day_str + "_abs.png", bbox_inches="tight")
    readings[['x', 'y', 'z']].diff().plot()
    pylab.savefig(day_str + "_diff.png", bbox_inches="tight")
    return sum(diffed.abs().sum())

def list_readings_holes(start, end):
    """ list holes of more than 10 seconds
    """
    readings = _get_readings_in_range(start, end)
    readings['delta'] = readings['ts'].diff()
    for row in readings[readings['delta'] > 10000].values:
        print(row)

def options():
    parser = ArgumentParser(description="aggregate accelerometer activity over a day/night")
    parser.add_argument("--day", help="YYYY-MM-DD to process")
    parser.add_argument("--minutes-start", help="number of minutes offset from utc midnight to show", type=int)
    parser.add_argument("--minutes-end", help="number of minutes offset from utc midnight to show", type=int)

    return parser.parse_args()

def main():
    opts = options()

    day = pd.to_datetime(opts.day, utc=True)
    print("%s: %d" % (day.strftime("%Y-%m-%d"), get_activity_for_night(day, opts.minutes_start, opts.minutes_end)))

    now = datetime.now(pytz.utc)
    list_readings_holes(DATA_START, now)

if __name__ == '__main__':
    main()
