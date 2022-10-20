

/**
 * Initializes the document with the sidebar, the client url, and the
 * akn document which is given as an argument by aknHtml.
 * @param {string} aknHtml 
 * @returns {HTMLElement} The container to which aknHtml has been added
 * to as inner html.
 */
function initializeDummyHtml(aknHtml) {
    let sidebarUrl = document.createElement('link');
    sidebarUrl.rel = 'sidebar';
    sidebarUrl.href = 'test/sidebar';
    sidebarUrl.type = 'application/annotator+html';

    let clientUrl = document.createElement('link');
    clientUrl.rel = 'hypothesis-client';
    clientUrl.href = 'test/boot.js';
    clientUrl.type = 'application/annotator+javascript';

    let container = document.createElement('section');
    container.innerHTML = aknHtml;
    document.head.appendChild(sidebarUrl);
    document.head.appendChild(clientUrl);
    document.body.appendChild(container);

    let dummyElements = {
      container : container,
      sidebarUrl : sidebarUrl,
      clientUrl : clientUrl,
    }

    return dummyElements;
}

function removeDummyHtml(dummyElements) {
  document.head.removeChild(dummyElements.sidebarUrl);
  document.head.removeChild(dummyElements.clientUrl);
  document.body.removeChild(dummyElements.container);
}

module.exports = {
    initializeDummyHtml: initializeDummyHtml,
    removeDummyHtml: removeDummyHtml,
}