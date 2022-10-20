'use strict';

/* global Uint8Array */

function byteToHex(val) {
  var str = val.toString(16);
  return str.length === 1 ? '0' + str : str;
}

/**
 * Generate a random hex string of `len` chars.
 *
 * @param {number} - An even-numbered length string to generate.
 * @return {string}
 */
function generateHexString(len) {

  var ieWindow = window as Window & typeof globalThis & { msCrypto: any };

  var crypto = window.crypto || ieWindow.msCrypto /* IE 11 */;
  var bytes = new Uint8Array(len / 2);
  crypto.getRandomValues(bytes);
  return Array.from(bytes).map(byteToHex).join('');
}

export = {
  generateHexString,
};
