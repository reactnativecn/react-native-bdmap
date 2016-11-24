/**
 * Created by tdzl2003 on 4/13/16.
 */

import React, {
  NativeModules,
  NativeAppEventEmitter,
} from 'react-native';

const NativeBDMap = NativeModules.BaiduMap;

Object.assign(exports, NativeBDMap);

let subscriptions = [];
let watcherCount = 0;

exports.watchPosition = function (callback, error, options) {
  if (options) {
    NativeBDMap.setOptions(options);
  }
  const watchId = subscriptions.length;
  subscriptions.push(
    [
      NativeAppEventEmitter.addListener(
        'BDMapLocation',
        callback
      ),
      error ? NativeAppEventEmitter.addListener(
        'BDMapLocationError',
        error
      ) : null
    ]
  );
  if (watcherCount++ == 0) {
    NativeBDMap.startWatch();
  }
  return watchId;
}

exports.clearWatch = function (watchId) {
  var sub = subscriptions[watchId];
  if (!sub) {
    return;
  }
  sub[0].remove();
  sub[1] && sub[1].remove();
  delete subscriptions[watchId];
  if (--watcherCount == 0) {
    NativeBDMap.stopWatch();
  }
}

exports.stopObserving = function() {
  subscriptions.forEach(v=>v.remove());
  subscriptions=[];
  watcherCount = 0;
  NativeBDMap.stopWatch();
}

exports.getLocation = async function(options) {
  let watchId;
  try {
    return await new Promise((resolve,reject) => {
      watchId = exports.watchPosition(resolve, reject, options);
    });
  } finally {
    exports.clearWatch(watchId);
  }
}

exports.geoReverse = NativeBDMap.geoReverse;
exports.poiSearch = NativeBDMap.poiSearch;

exports.BDMapView = require('./BDMapView');
