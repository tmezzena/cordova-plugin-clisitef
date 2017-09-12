
var exec = require('cordova/exec');

var PLUGIN_NAME = 'clisitef';

var clisitef = {
  echo: function(phrase, cb) {
    exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
  },
  getDate: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'getDate', []);
  },
  test: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'test', []);
  },
  vende: function(cb, config, venda) {
    exec(cb, null, PLUGIN_NAME, 'vende', [config, venda]);
  }    
};

module.exports = clisitef;
