#!/usr/bin/env python

from flask import Flask, request
import pymongo

EXPECTED_ATTRS = ('x', 'y', 'z', 'v', 'ts')
app = Flask(__name__)
watch_data = pymongo.MongoClient()['watch_data']

@app.route('/', methods=['POST'])
def collect_accel():
    readings = request.get_json()

    if not readings:
        abort(400)

    good_readings = [r for r in readings if all(k in r for k in EXPECTED_ATTRS)]
    tslist = [r['ts'] for r in good_readings]

    start_ts = min(tslist)
    end_ts = max(tslist)
    watch_data.accel_data.insert({'start_ts': start_ts,
                                  'end_ts': end_ts,
                                  'readings': good_readings})
    return "ok"


if __name__ == '__main__':
    watch_data.accel_data.ensure_index('start_ts')
    app.run(host='0.0.0.0', port=5000, debug=True, processes=2)
