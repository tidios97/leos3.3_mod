/*
 * decaffeinate suggestions:
 * DS206: Consider reworking classes to avoid initClass
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
let StreamFilter;
export = (StreamFilter = (function() {
  StreamFilter = class StreamFilter {

      private strategies = ['include_any', 'include_all', 'exclude_any', 'exclude_all'];
  
      private filter = {
          match_policy :  'include_any',
          clauses : [],
          actions : {
            create: true,
            update: true,
            delete: true
          }
        };

    constructor() {}

    getFilter() { return this.filter; }
    getMatchPolicy() { return this.filter.match_policy; }
    getClauses() { return this.filter.clauses; }
    getActions() { return this.filter.actions; }
    getActionCreate() { return this.filter.actions.create; }
    getActionUpdate() { return this.filter.actions.update; }
    getActionDelete() { return this.filter.actions.delete; }

    setMatchPolicy(policy) {
      this.filter.match_policy = policy;
      return this;
    }

    setMatchPolicyIncludeAny() {
      this.filter.match_policy = 'include_any';
      return this;
    }

    setMatchPolicyIncludeAll() {
      this.filter.match_policy = 'include_all';
      return this;
    }

    setMatchPolicyExcludeAny() {
      this.filter.match_policy = 'exclude_any';
      return this;
    }

    setMatchPolicyExcludeAll() {
      this.filter.match_policy = 'exclude_all';
      return this;
    }

    setActions(actions) {
      this.filter.actions = actions;
      return this;
    }

    setActionCreate(action) {
      this.filter.actions.create = action;
      return this;
    }

    setActionUpdate(action) {
      this.filter.actions.update = action;
      return this;
    }

    setActionDelete(action) {
      this.filter.actions.delete = action;
      return this;
    }

    noClauses() {
      this.filter.clauses = [];
      return this;
    }

    addClause(field, operator, value, case_sensitive, options) {
      if (case_sensitive == null) { case_sensitive = false; }
      options = options || {};
      this.filter.clauses.push({
        field,
        operator,
        value,
        case_sensitive,
        options
      });
      return this;
    }

    resetFilter() {
      this.setMatchPolicyIncludeAny();
      this.setActionCreate(true);
      this.setActionUpdate(true);
      this.setActionDelete(true);
      this.noClauses();
      return this;
    }
  };
  return StreamFilter;
})());
