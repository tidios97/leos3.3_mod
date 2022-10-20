/*
 * decaffeinate suggestions:
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
'use strict';

let RulesEngine;

module.exports = (RulesEngine = (function() {
  let _getNodeExplicitType;
  let _applyRules;
  RulesEngine = class RulesEngine {
    static initClass() {
      _getNodeExplicitType = function(nodeType) {
        switch (nodeType) {
        case Node.TEXT_NODE:
          return 'text';
        case Node.ELEMENT_NODE:
          return 'element';
        default:
          return 'other';
        }
      };
  
      _applyRules= function(engineRules, element, ...args) {
        const rules = engineRules[_getNodeExplicitType(element.nodeType)];
        for (let rule in (rules || [])) { 
          try {
            // using rule name as selector
            if ((rule !== '$') && element.matches(rule)) {
              rules[rule].apply(element, args);
            }
          } catch (e) {
            console.log(`Error in rule: ${rule.toString()} - ${e}`);
          }
          // calling default rule $
          if (rules.$ != null) {
            rules.$.apply(element, args);
          }
        }
      };
    }

    processElement(engineRules, element, ...args) {
      this.processChildren(engineRules, element, ...args);
      return _applyRules(engineRules, element, ...args);
    }

    processChildren(engineRules, element, ...args) {
      const childNodes = Array.prototype.slice.call(element.childNodes);
      if (childNodes && (childNodes.length > 0)) {
        return childNodes.forEach((function(node) {
          this.processElement(engineRules, node, ...args);
        }), this);
      }
    }
  };
  RulesEngine.initClass();
  return RulesEngine;
})());
