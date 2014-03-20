#!/usr/bin/env python

from datetime import datetime
import logging
import pandas as pd
import pymongo

db = pymongo.MongoClient().watch_data
log = logging.getLogger('aggregate_accel')

def get_readings_in_range(start, end):
    readings = []
    for r in db.accel_data.find({'start_ts' : {'$gte' : start}, 'end_ts' : {'$lte' : end}},
                                sort=[('start_ts', pymongo.ASCENDING)]):
        readings += r['readings']
    log.info('Loaded %d readings', len(readings))

    df = pd.DataFrame(readings)
    idx = pd.to_datetime(df['ts'] / 1000, unit='s')
    return df.set_index(idx)

