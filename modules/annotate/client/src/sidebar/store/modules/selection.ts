/**
 * This module handles state related to the current sort, search and filter
 * settings in the UI, including:
 *
 * - The set of annotations that are currently focused (hovered) or selected
 * - The selected tab
 * - The current sort order
 * - The current filter query
 */

'use strict';

var immutable = require('seamless-immutable');

var toSet = require('../../util/array-util').toSet;
var uiConstants = require('../../ui-constants');
var tabs = require('../../tabs');

var util = require('../util');

const annotationMetadata = require('../../annotation-metadata');

/**
* Default starting tab.
*/
var TAB_DEFAULT = uiConstants.TAB_ANNOTATIONS;

 /**
  * Default sort keys for each tab.
  */
var TAB_SORTKEY_DEFAULT = {};
TAB_SORTKEY_DEFAULT[uiConstants.TAB_ANNOTATIONS] = 'Location';
TAB_SORTKEY_DEFAULT[uiConstants.TAB_NOTES] = 'Oldest';
TAB_SORTKEY_DEFAULT[uiConstants.TAB_ORPHANS] = 'Location';

/**
 * Available sort keys for each tab.
 */
var TAB_SORTKEYS_AVAILABLE = {};
TAB_SORTKEYS_AVAILABLE[uiConstants.TAB_ANNOTATIONS] = ['Newest', 'Oldest', 'Location'];
TAB_SORTKEYS_AVAILABLE[uiConstants.TAB_NOTES] = ['Newest', 'Oldest'];
TAB_SORTKEYS_AVAILABLE[uiConstants.TAB_ORPHANS] = ['Newest', 'Oldest', 'Location'];


function initialSelection(settings) {
  var selection = {};
  if (settings.annotations && !settings.query) {
    selection[settings.annotations] = true;
  }
  return freeze(selection);
}

function freeze(selection) {
  if (Object.keys(selection).length) {
    return immutable(selection);
  } else {
    return null;
  }
}

function init(settings) {
  return {
    // Contains a map of annotation tag:true pairs.
    focusedAnnotationMap: null,

    // Contains a map of annotation id:true pairs.
    selectedAnnotationMap: initialSelection(settings),

    // Map of annotation IDs to expanded/collapsed state. For annotations not
    // present in the map, the default state is used which depends on whether
    // the annotation is a top-level annotation or a reply, whether it is
    // selected and whether it matches the current filter.
    expanded: initialSelection(settings) || {},

    // Set of IDs of annotations that have been explicitly shown
    // by the user even if they do not match the current search filter
    forceVisible: {},

    // IDs of annotations that should be highlighted
    highlighted: [],

    filterQuery: settings.query || null,

    filterReseted : true,

    selectedTab: TAB_DEFAULT,

    // Key by which annotations are currently sorted.
    sortKey: TAB_SORTKEY_DEFAULT[TAB_DEFAULT],
    // Keys by which annotations can be sorted.
    sortKeysAvailable: TAB_SORTKEYS_AVAILABLE[TAB_DEFAULT],
  };
}

var update = {
  CLEAR_SEARCH_FILTER: function () {
    return {filterQuery: null};
  },

  CLEAR_SELECTION: function () {
    return {filterQuery: null, selectedAnnotationMap: null};
  },

  DESELECT_ALL_ANNOTATIONS: function () {
    return {selectedAnnotationMap: null};
  },

  SELECT_ANNOTATIONS: function (state, action) {
    return {selectedAnnotationMap: action.selection};
  },

  FOCUS_ANNOTATIONS: function (state, action) {
    return {focusedAnnotationMap: action.focused};
  },

  SET_FORCE_VISIBLE: function (state, action) {
    return {forceVisible: action.forceVisible};
  },

  SET_EXPANDED: function (state, action) {
    return {expanded: action.expanded};
  },

  HIGHLIGHT_ANNOTATIONS: function (state, action) {
    return {highlighted: action.highlighted};
  },

  SELECT_TAB: function (state, action) {
    // Do nothing if the "new tab" is not a valid tab.
    if ([uiConstants.TAB_ANNOTATIONS,
         uiConstants.TAB_NOTES,
         uiConstants.TAB_ORPHANS].indexOf(action.tab) === -1) {
      return {};
    }
    // Shortcut if the tab is already correct, to avoid resetting the sortKey
    // unnecessarily.
    if (state.selectedTab === action.tab) {
      return {};
    }
    return {
      selectedTab: action.tab,
      sortKey: TAB_SORTKEY_DEFAULT[action.tab],
      sortKeysAvailable: TAB_SORTKEYS_AVAILABLE[action.tab],
    };
  },

  ADD_ANNOTATIONS(state, action) {
    var counts = tabs.counts(action.annotations);
    // If there are no annotations at all, ADD_ANNOTATIONS will not be called.
    var haveOnlyPageNotes = counts.notes === action.annotations.length;
    // If this is the init phase and there are only page notes, select the page notes tab.
    if (state.annotations.length === 0 && haveOnlyPageNotes){
      return {selectedTab: uiConstants.TAB_NOTES};
    }
    return {};
  },

  SET_FILTER_QUERY: function (state, action) {
    return {
      filterQuery: action.query,
      forceVisible: {},
      expanded: {},
    };
  },

  SET_SORT_KEY: function (state, action) {
    return {sortKey: action.key};
  },

  SET_FILTER_RESETED: function (stat, action) {
    return {filterReseted: action.filterReseted};
  }
};

var actions = util.actionTypes(update);

function select(annotations) {
  return {
    type: actions.SELECT_ANNOTATIONS,
    selection: freeze(annotations),
  };
}

/**
 * Set the currently selected annotation IDs.
 */
function selectAnnotations(ids) {
  return select(toSet(ids));
}

/** Toggle whether annotations are selected or not. */
function toggleSelectedAnnotations(ids) {
  return function (dispatch, getState) {
    var selection = Object.assign({}, getState().selectedAnnotationMap);
    for (var i = 0; i < ids.length; i++) {
      var id = ids[i];
      if (selection[id]) {
        delete selection[id];
      } else {
        selection[id] = true;
      }
    }
    dispatch(select(selection));
  };
}

/**
 * Sets whether a given annotation should be visible, even if it does not
 * match the current search query.
 *
 * @param {string} id - Annotation ID
 * @param {boolean} visible
 */
function setForceVisible(id, visible) {
  // FIXME: This should be converted to a plain action and accessing the state
  // should happen in the update() function
  return function (dispatch, getState) {
    var forceVisible = Object.assign({}, getState().forceVisible);
    forceVisible[id] = visible;
    dispatch({
      type: actions.SET_FORCE_VISIBLE,
      forceVisible: forceVisible,
    });
  };
}

/**
 * Sets which annotations are currently focused.
 *
 * @param {Array<string>} Tags of annotations to focus
 */
function focusAnnotations(tags) {
  return {
    type: actions.FOCUS_ANNOTATIONS,
    focused: freeze(toSet(tags)),
  };
}

function setCollapsed(id: number,  collapsed: boolean) {
  // FIXME: This should be converted to a plain action and accessing the state
  // should happen in the update() function
  return function (dispatch, getState) {
    var expanded = Object.assign({}, getState().expanded);
    expanded[id] = !collapsed;
    dispatch({
      type: actions.SET_EXPANDED,
      expanded: expanded,
    });
  };
}

/**
 * Highlight annotations with the given `ids`.
 *
 * This is used to indicate the specific annotation in a thread that was
 * linked to for example.
 */
function highlightAnnotations(ids) {
  return {
    type: actions.HIGHLIGHT_ANNOTATIONS,
    highlighted: ids,
  };
}


/** Set the type annotations to be displayed. */
function selectTab(type) {
  return {
    type: actions.SELECT_TAB,
    tab: type,
  };
}

/** Set the query used to filter displayed annotations. */
function setFilterQuery(query) {
  return {
    type: actions.SET_FILTER_QUERY,
    query: query,
  };
}

/** Sets the sort key for the annotation list. */
function setSortKey(key) {
  return {
    type: actions.SET_SORT_KEY,
    key: key,
  };
}

function setFilterReseted(filterReseted) {
  return {
    type: actions.SET_FILTER_RESETED,
    filterReseted: filterReseted
  };
}

/**
 * Returns true if the annotation with the given `id` is selected.
 */
function isAnnotationSelected(state, id) {
  return Object.prototype.hasOwnProperty.call(state.selectedAnnotationMap || {}, id);
}

/**
 * Return true if any annotations are currently selected.
 */
function hasSelectedAnnotations(state) {
  return !!state.selectedAnnotationMap;
}

function getSelectedAnnotationCount(state) {
  if (!state.selectedAnnotationMap)
    return 0;
  return Object.keys(state.selectedAnnotationMap).length;
}

function getSelectedAnnotations(state) {
  if (!state.selectedAnnotationMap)
    return [];
  const selectedAnnotationsIds = Object.keys(state.selectedAnnotationMap);
  const selectedAnnotations = state.annotations.filter(annotation => selectedAnnotationsIds.includes(annotation.id));
  return selectedAnnotations;
}

function getSelectedSuggestions(state) {
  return getSelectedAnnotations(state)
    .filter(annotationMetadata.isSuggestion);
}

function deselectAnnotation(id) {
  // FIXME: This should be converted to a plain action and accessing the state
  // should happen in the update() function
  return function (dispatch, getState) {
    var selection = Object.assign({}, getState().selectedAnnotationMap);
    if (!selection || !id) {
      return;
    }
    delete selection[id];
    dispatch(select(selection));
  };
}

function selectAnnotation(id) {
  return function (dispatch, getState) {
    var selection = Object.assign({}, getState().selectedAnnotationMap);
    if (!selection || !id) {
      return;
    }
    selection[id] = true;
    dispatch(select(selection));
  };
}

function clearFilteredAndSelectedAnnotations() {
  return {type: actions.CLEAR_SELECTION};
}

function clearSearchFilter() {
  return {type: actions.CLEAR_SEARCH_FILTER};
}

function deselectAllAnnotations() {
  return {type: actions.DESELECT_ALL_ANNOTATIONS};
}

export = {
  init: init,
  update: update,

  actions: {
    clearFilteredAndSelectedAnnotations,
    clearSearchFilter,
    deselectAnnotation,
    deselectAllAnnotations,
    focusAnnotations,
    highlightAnnotations,
    selectAnnotation,
    selectAnnotations,
    selectTab,
    setCollapsed,
    setFilterQuery,
    setForceVisible,
    setSortKey,
    setFilterReseted,
    toggleSelectedAnnotations,
  },

  selectors: {
    getSelectedAnnotationCount,
    getSelectedAnnotations,
    getSelectedSuggestions,
    hasSelectedAnnotations,
    isAnnotationSelected,
  },
};
