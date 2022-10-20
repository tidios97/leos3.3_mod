'use strict';

const Chance = require('chance');
const chance = new Chance();

type Group = { id: any, name: any, links: { html: string }, type: string };

function group() {
  const id = chance.hash({length: 15});
  const name = chance.string();
  const group: Group = {
    id: id,
    name: name,
    links: {
      html: `http://localhost:5000/groups/${id}/${name}`,
    },
    type: 'private',
  };
  return group;
}

type Organization = { id: any, name: string, logo: string };

function organization(options={}) {
  const org: Organization = {
    id: chance.hash({length : 15}),
    name: chance.string(),
    logo: chance.url(),
  };
  return Object.assign(org, options);
}

function defaultOrganization() {
  return {
    id: '__default__',
    name: 'Hypothesis',
    logo: 'http://example.com/hylogo',
  };
}

type ExpandedGroup = Group & { organization: Organization };

function expandedGroup(options={}) {
  var expanded: ExpandedGroup = group() as ExpandedGroup;
  expanded.organization = organization();

  return Object.assign(expanded, options);
}

module.exports = {
  group,
  expandedGroup,
  organization,
  defaultOrganization,
};
