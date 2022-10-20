// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//
// This is a modified copy of
// https://github.com/openannotation/annotator/blob/v1.2.x/src/range.coffee

const $ = require('jquery');

const Util = require('./util');

const Range = {};

// Public: Determines the type of Range of the provided object and returns
// a suitable Range instance.
//
// r - A range Object.
//
// Examples
//
//   selection = window.getSelection()
//   Range.sniff(selection.getRangeAt(0))
//   # => Returns a BrowserRange instance.
//
// Returns a Range object or false.
Range.sniff = function(r) {
  if (r.commonAncestorContainer != null) {
    return new Range.BrowserRange(r);
  } else if (typeof r.start === 'string') {
    return new Range.SerializedRange(r);
  } else if (r.start && (typeof r.start === 'object')) {
    return new Range.NormalizedRange(r);
  } else {
    console.error('Could not sniff range type');
    return false;
  }
};


// Public: Finds an Element Node using an XPath relative to the document root.
//
// If the document is served as application/xhtml+xml it will try and resolve
// any namespaces within the XPath.
//
// xpath - An XPath String to query.
//
// Examples
//
//   node = Range.nodeFromXPath('/html/body/div/p[2]')
//   if node
//     # Do something with the node.
//
// Returns the Node if found otherwise null.
Range.nodeFromXPath = function(xpath, root) {
  if (root == null) { root = document; }
  const evaluateXPath = function(xp, nsResolver=null) {
    try {
      return document.evaluate('.' + xp, root, nsResolver, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    } catch (exception) {
      // There are cases when the evaluation fails, because the
      // HTML documents contains nodes with invalid names,
      // for example tags with equal signs in them, or something like that.
      // In these cases, the XPath expressions will have these abominations,
      // too, and then they can not be evaluated.
      // In these cases, we get an XPathException, with error code 52.
      // See http://www.w3.org/TR/DOM-Level-3-XPath/xpath.html#XPathException
      // This does not necessarily make any sense, but this what we see
      // happening.
      console.log('XPath evaluation failed.');
      console.log('Trying fallback...');
      // We have a an 'evaluator' for the really simple expressions that
      // should work for the simple expressions we generate.
      return Util.nodeFromXPath(xp, root);
    }
  };

  if (!$.isXMLDoc(document.documentElement)) {
    return evaluateXPath(xpath);
  } else {
    // We're in an XML document, create a namespace resolver function to try
    // and resolve any namespaces in the current document.
    // https://developer.mozilla.org/en/DOM/document.createNSResolver
    let customResolver = document.createNSResolver(
      document.ownerDocument === null ?
        document.documentElement
        :
        document.ownerDocument.documentElement
    );
    let node = evaluateXPath(xpath, customResolver);

    if (!node) {
      // If the previous search failed to find a node then we must try to
      // provide a custom namespace resolver to take into account the default
      // namespace. We also prefix all node names with a custom xhtml namespace
      // eg. 'div' => 'xhtml:div'.
      xpath = (xpath.split('/').map((segment) =>
        segment && (segment.indexOf(':') === -1) ?
          segment.replace(/^([a-z]+)/, 'xhtml:$1')
          : segment)
      ).join('/');

      // Find the default document namespace.
      const namespace = document.lookupNamespaceURI(null);

      // Try and resolve the namespace, first seeing if it is an xhtml node
      // otherwise check the head attributes.
      customResolver  = function(ns) {
        if (ns === 'xhtml') { return namespace;
        } else { return document.documentElement.getAttribute('xmlns:' + ns); }
      };

      node = evaluateXPath(xpath, customResolver);
    }
    return node;
  }
};

Range.RangeError = class RangeError extends Error {
  constructor(type, message, parent=null) {
    // this cannot be used before calling super, cannot use super(@type, @message,...)
    // see http://coffeescript.org/#breaking-changes-classes
    super(message);
    this.type = type;
    this.message = message;
    this.parent = parent;
  }
};

// Public: Creates a wrapper around a range object obtained from a DOMSelection.
Range.BrowserRange = class BrowserRange {

  // Public: Creates an instance of BrowserRange.
  //
  // object - A range object obtained via DOMSelection#getRangeAt().
  //
  // Examples
  //
  //   selection = window.getSelection()
  //   range = new Range.BrowserRange(selection.getRangeAt(0))
  //
  // Returns an instance of BrowserRange.
  constructor(obj) {
    this.commonAncestorContainer = obj.commonAncestorContainer;
    this.startContainer          = obj.startContainer;
    this.startOffset             = obj.startOffset;
    this.endContainer            = obj.endContainer;
    this.endOffset               = obj.endOffset;
  }

  // Public: normalize works around the fact that browsers don't generate
  // ranges/selections in a consistent manner. Some (Safari) will create
  // ranges that have (say) a textNode startContainer and elementNode
  // endContainer. Others (Firefox) seem to only ever generate
  // textNode/textNode or elementNode/elementNode pairs.
  //
  // Returns an instance of Range.NormalizedRange
  normalize(root) {
    if (this.tainted) {
      console.error('You may only call normalize() once on a BrowserRange!');
      return false;
    } else {
      this.tainted = true;
    }

    const r = {};

    // Look at the start
    if (this.startContainer.nodeType === Node.ELEMENT_NODE) {
      // We are dealing with element nodes
      if (this.startOffset < this.startContainer.childNodes.length) {
        r.start = Util.getFirstTextNodeNotBefore(this.startContainer.childNodes[this.startOffset]);
      } else {
        r.start = Util.getFirstTextNodeNotBefore(this.startContainer);
      }
      r.startOffset = 0;
    } else {
      // We are dealing with simple text nodes
      r.start = this.startContainer;
      r.startOffset = this.startOffset;
    }

    // Look at the end
    if (this.endContainer.nodeType === Node.ELEMENT_NODE) {
      // Get specified node.
      let node = this.endContainer.childNodes[this.endOffset];

      if (node != null) { // Does that node exist?
        // Look for a text node either at the immediate beginning of node
        let n = node;
        while ((n != null) && (n.nodeType !== Node.TEXT_NODE)) {
          n = n.firstChild;
        }
        if (n != null) { // Did we find a text node at the start of this element?
          r.end = n;
          r.endOffset = 0;
        }
      }

      if (r.end == null) {
        // We need to find a text node in the previous sibling of the node at the
        // given offset, if one exists, or in the previous sibling of its container.
        if (this.endOffset) {
          node = this.endContainer.childNodes[this.endOffset - 1];
        } else {
          node = this.endContainer.previousSibling;
        }
        r.end = Util.getLastTextNodeUpTo(node);
        r.endOffset = r.end.nodeValue.length;
      }

    } else { // We are dealing with simple text nodes
      r.end = this.endContainer;
      r.endOffset = this.endOffset;
    }

    // We have collected the initial data.

    // Now let's start to slice & dice the text elements!
    const nr = {};

    if (r.startOffset > 0) {
      // Do we really have to cut?
      if (!r.start.nextSibling || (r.start.nodeValue.length > r.startOffset)) {
        // Yes. Cut.
        nr.start = r.start.splitText(r.startOffset);
      } else {
        // Avoid splitting off zero-length pieces.
        nr.start = r.start.nextSibling;
      }
    } else {
      nr.start = r.start;
    }

    // is the whole selection inside one text element ?
    if (r.start === r.end) {
      if (nr.start.nodeValue.length > (r.endOffset - r.startOffset)) {
        nr.start.splitText(r.endOffset - r.startOffset);
      }
      nr.end = nr.start;
    } else { // no, the end of the selection is in a separate text element
      // does the end need to be cut?
      if (r.end.nodeValue.length > r.endOffset) {
        r.end.splitText(r.endOffset);
      }
      nr.end = r.end;
    }

    // Make sure the common ancestor is an element node.
    nr.commonAncestor = this.commonAncestorContainer;
    while (nr.commonAncestor.nodeType !== Node.ELEMENT_NODE) {
      nr.commonAncestor = nr.commonAncestor.parentNode;
    }

    return new Range.NormalizedRange(nr);
  }

  // Public: Creates a range suitable for storage.
  //
  // root           - A root Element from which to anchor the serialisation.
  // ignoreSelector - A selector String of elements to ignore. For example
  //                  elements injected by the annotator.
  //
  // Returns an instance of SerializedRange.
  serialize(root, ignoreSelector) {
    return this.normalize(root).serialize(root, ignoreSelector);
  }
};

// Public: A normalised range is most commonly used throughout the annotator.
// its the result of a deserialised SerializedRange or a BrowserRange with
// out browser inconsistencies.
Range.NormalizedRange = class NormalizedRange {

  // Public: Creates an instance of a NormalizedRange.
  //
  // This is usually created by calling the .normalize() method on one of the
  // other Range classes rather than manually.
  //
  // obj - An Object literal. Should have the following properties.
  //       commonAncestor: A Element that encompasses both the start and end nodes
  //       start:          The first TextNode in the range.
  //       end             The last TextNode in the range.
  //
  // Returns an instance of NormalizedRange.
  constructor(obj) {
    this.commonAncestor = obj.commonAncestor;
    this.start          = obj.start;
    this.end            = obj.end;
  }

  // Public: For API consistency.
  //
  // Returns itself.
  normalize(root) {
    return this;
  }

  // Public: Limits the nodes within the NormalizedRange to those contained
  // withing the bounds parameter. It returns an updated range with all
  // properties updated. NOTE: Method returns null if all nodes fall outside
  // of the bounds.
  //
  // bounds - An Element to limit the range to.
  //
  // Returns updated self or null.
  limit(bounds) {
    const nodes = $.grep(this.textNodes(), node => (node.parentNode === bounds) || $.contains(bounds, node.parentNode));

    if (!nodes.length) { return null; }

    this.start = nodes[0];
    this.end   = nodes[nodes.length - 1];

    const startParents = $(this.start).parents();
    for (let parent of $(this.end).parents()) {
      if (startParents.index(parent) !== -1) {
        this.commonAncestor = parent;
        break;
      }
    }
    return this;
  }

  // Convert this range into an object consisting of two pairs of (xpath,
  // character offset), which can be easily stored in a database.
  //
  // root -           The root Element relative to which XPaths should be calculated
  // ignoreSelector - A selector String of elements to ignore. For example
  //                  elements injected by the annotator.
  //
  // Returns an instance of SerializedRange.
  serialize(root, ignoreSelector) {

    const serialization = function(node, isEnd) {
      let origParent;
      if (ignoreSelector) {
        origParent = $(node).parents(`:not(${ignoreSelector})`).eq(0);
      } else {
        origParent = $(node).parent();
      }

      const xpath = Util.xpathFromNode(origParent, root)[0];
      const textNodes = Util.getTextNodes(origParent);

      // Calculate real offset as the combined length of all the
      // preceding textNode siblings. We include the length of the
      // node if it's the end node.
      const nodes = textNodes.slice(0, textNodes.index(node));
      let offset = 0;
      for (let n of nodes) {
        offset += n.nodeValue.length;
      }

      if (isEnd) { return [xpath, offset + node.nodeValue.length]; } else { return [xpath, offset]; }
    };

    const start = serialization(this.start);
    const end   = serialization(this.end, true);

    return new Range.SerializedRange({
      // XPath strings
      start: start[0],
      end: end[0],
      // Character offsets (integer)
      startOffset: start[1],
      endOffset: end[1],
    });
  }

  // Public: Creates a concatenated String of the contents of all the text nodes
  // within the range.
  //
  // Returns a String.
  text() {
    return (this.textNodes().map((node) =>
      node.nodeValue)
    ).join('');
  }

  // Public: Fetches only the text nodes within th range.
  //
  // Returns an Array of TextNode instances.
  textNodes() {
    const textNodes = Util.getTextNodes($(this.commonAncestor));
    const [start, end] = [textNodes.index(this.start), textNodes.index(this.end)];
    // Return the textNodes that fall between the start and end indexes.
    return $.makeArray(textNodes.slice(start, +end + 1 || undefined));
  }

  // Public: Converts the Normalized range to a native browser range.
  //
  // See: https://developer.mozilla.org/en/DOM/range
  //
  // Examples
  //
  //   selection = window.getSelection()
  //   selection.removeAllRanges()
  //   selection.addRange(normedRange.toRange())
  //
  // Returns a Range object.
  toRange() {
    const range = document.createRange();
    range.setStartBefore(this.start);
    range.setEndAfter(this.end);
    return range;
  }
};

// Public: A range suitable for storing in local storage or serializing to JSON.
Range.SerializedRange = class SerializedRange {

  // Public: Creates a SerializedRange
  //
  // obj - The stored object. It should have the following properties.
  //       start:       An xpath to the Element containing the first TextNode
  //                    relative to the root Element.
  //       startOffset: The offset to the start of the selection from obj.start.
  //       end:         An xpath to the Element containing the last TextNode
  //                    relative to the root Element.
  //       startOffset: The offset to the end of the selection from obj.end.
  //
  // Returns an instance of SerializedRange
  constructor(obj) {
    this.start       = obj.start;
    this.startOffset = obj.startOffset;
    this.end         = obj.end;
    this.endOffset   = obj.endOffset;
  }

  // Public: Creates a NormalizedRange.
  //
  // root - The root Element from which the XPaths were generated.
  //
  // Returns a NormalizedRange instance.
  normalize(root) {
    const range = {};

    for (let p of ['start', 'end']) {
      var node;
      try {
        node = Range.nodeFromXPath(this[p], root);
      } catch (e) {
        throw new Range.RangeError(p, `Error while finding ${p} node: ${this[p]}: ` + e, e);
      }

      if (!node) {
        throw new Range.RangeError(p, `Couldn't find ${p} node: ${this[p]}`);
      }

      // Unfortunately, we *can't* guarantee only one textNode per
      // elementNode, so we have to walk along the element's textNodes until
      // the combined length of the textNodes to that point exceeds or
      // matches the value of the offset.
      let length = 0;
      let targetOffset = this[p + 'Offset'];

      // Range excludes its endpoint because it describes the boundary position.
      // Target the string index of the last character inside the range.
      if (p === 'end') { targetOffset--; }

      for (let tn of Util.getTextNodes($(node))) {
        if ((length + tn.nodeValue.length) > targetOffset) {
          range[p + 'Container'] = tn;
          range[p + 'Offset'] = this[p + 'Offset'] - length;
          break;
        } else {
          length += tn.nodeValue.length;
        }
      }

      // If we fall off the end of the for loop without having set
      // 'startOffset'/'endOffset', the element has shorter content than when
      // we annotated, so throw an error:
      if ((range[p + 'Offset'] == null)) {
        throw new Range.RangeError(`${p}offset`, `Couldn't find offset ${this[p + 'Offset']} in element ${this[p]}`);
      }
    }

    // Here's an elegant next step...
    //
    //   range.commonAncestorContainer = $(range.startContainer).parents().has(range.endContainer)[0]
    //
    // ...but unfortunately Node.contains() is broken in Safari 5.1.5 (7534.55.3)
    // and presumably other earlier versions of WebKit. In particular, in a
    // document like
    //
    //   <p>Hello</p>
    //
    // the code
    //
    //   p = document.getElementsByTagName('p')[0]
    //   p.contains(p.firstChild)
    //
    // returns `false`. Yay.
    //
    // So instead, we step through the parents from the bottom up and use
    // Node.compareDocumentPosition() to decide when to set the
    // commonAncestorContainer and bail out.

    const contains =
      (document.compareDocumentPosition == null) ?
        // IE
        (a, b) => a.contains(b)
        :
        // Everyone else
        (a, b) => a.compareDocumentPosition(b) & 16;

    $(range.startContainer).parents().each(function() {
      if (contains(this, range.endContainer)) {
        range.commonAncestorContainer = this;
        return false;
      }
    });

    return new Range.BrowserRange(range).normalize(root);
  }

  // Public: Creates a range suitable for storage.
  //
  // root           - A root Element from which to anchor the serialisation.
  // ignoreSelector - A selector String of elements to ignore. For example
  //                  elements injected by the annotator.
  //
  // Returns an instance of SerializedRange.
  serialize(root, ignoreSelector) {
    return this.normalize(root).serialize(root, ignoreSelector);
  }

  // Public: Returns the range as an Object literal.
  toObject() {
    return {
      start: this.start,
      startOffset: this.startOffset,
      end: this.end,
      endOffset: this.endOffset,
    };
  }
};

module.exports = Range;