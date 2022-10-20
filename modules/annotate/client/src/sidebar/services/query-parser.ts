/*
 * decaffeinate suggestions:
 * DS205: Consider reworking code to avoid use of IIFEs
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
// This class will process the results of search and generate the correct filter
// It expects the following dict format as rules
// { facet_name : {
//      formatter: to format the value (optional)
//      path: json path mapping to the annotation field
//      case_sensitive: true|false (default: false)
//      and_or: and|or for multiple values should it threat them as 'or' or 'and' (def: or)
//      operator: if given it'll use this operator regardless of other circumstances
//
//      options: backend specific options
//      options.es: elasticsearch specific options
//      options.es.query_type : can be: simple (term), query_string, match, multi_match
//         defaults to: simple, determines which es query type to use
//      options.es.cutoff_frequency: if set, the query will be given a cutoff_frequency for this facet
//      options.es.and_or: match and multi_match queries can use this, defaults to and
//      options.es.match_type: multi_match query type
//      options.es.fields: fields to search for in multi-match query
// }
// The models is the direct output from visualsearch
let QueryParser;
export = (QueryParser = (function() {
  QueryParser = class QueryParser {

    private rules;

    constructor() {
      this.populateFilter = this.populateFilter.bind(this);
    }

    static initClass() {
      this.prototype.rules = {
        user: {
          path: '/user',
          and_or: 'or',
        },
        text: {
          path: '/text',
          and_or: 'and',
        },
        tag: {
          path: '/tags',
          and_or: 'and',
        },
        quote: {
          path: '/quote',
          and_or: 'and',
        },
        uri: {
          formatter(uri) {
            return uri.toLowerCase();
          },
          path: '/uri',
          and_or: 'or',
          options: {
            es: {
              query_type: 'match',
              cutoff_frequency: 0.001,
              and_or: 'and',
            },
          },
        },
        since: {
          formatter(past) {
            let seconds;
            switch (past) {
            case '5 min': seconds = 5*60; break;
            case '30 min': seconds = 30*60; break;
            case '1 hour': seconds = 60*60; break;
            case '12 hours': seconds = 12*60*60; break;
            case '1 day': seconds = 24*60*60; break;
            case '1 week': seconds = 7*24*60*60; break;
            case '1 month': seconds = 30*24*60*60; break;
            case '1 year': seconds = 365*24*60*60; break;
            }
            return new Date(new Date().valueOf() - (seconds*1000));
          },
          path: '/created',
          and_or: 'and',
          operator: 'ge',
        },
        any: {
          and_or: 'and',
          path:   ['/quote', '/tags', '/text', '/uri', '/user'],
          options: {
            es: {
              query_type: 'multi_match',
              match_type: 'cross_fields',
              and_or: 'and',
              fields:   ['quote', 'tags', 'text', 'uri.parts', 'user'],
            },
          },
        },
      };
    }

    populateFilter(filter, query) {
      // Populate a filter with a query object
      for (let category in query) {
        var oper_part, 
          value_part;
        const value = query[category];
        if (this.rules[category] === null) { continue; }
        var {terms} = value;
        if (!terms.length) { continue; }
        var rule = this.rules[category];

        // Now generate the clause with the help of the rule
        var case_sensitive = (rule.case_sensitive !== null) ? rule.case_sensitive : false;
        const and_or = rule.and_or || 'or';
        var mapped_field = rule.path || '/'+category;

        if (and_or === 'or') {
          oper_part = rule.operator || 'match_of';

          value_part = [];
          for (let term of terms) {
            const t = rule.formatter ? rule.formatter(term) : term;
            value_part.push(t);
          }

          filter.addClause(mapped_field, oper_part, value_part, case_sensitive, rule.options);
        } else {
          oper_part = rule.operator || 'matches';
          for (let val of terms) {
            value_part = rule.formatter ? rule.formatter(val) : val;
            filter.addClause(mapped_field, oper_part, value_part, case_sensitive, rule.options);
          }
        }
      }
    }
  };
  QueryParser.initClass();
  return QueryParser;
})());
