'use strict';

const SYSTEM_ID = require('../shared/system-id');
const serviceConfig = require('./service-config');

function isISC(settings):boolean {
  const svc = serviceConfig(settings);
  return svc !== undefined && svc !== null && svc.authority === SYSTEM_ID.ISC;
}

export = {
  isISC,
};
