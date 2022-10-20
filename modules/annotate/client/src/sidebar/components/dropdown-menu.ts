const authorityChecker = require('../authority-checker');

//  @ngInject
function DropdownMenuController($element) {

    const domElement = $element[0];
    const threadList = domElement.closest('thread-list');

    this.$onInit = function () {
        computeLayout();
        if (threadList) {
            threadList.addEventListener('scroll', computeLayout);
        }
    };

    this.$onDestroy = function () {
        if (threadList) {
            threadList.removeEventListener('scroll', computeLayout);
        }
    };

    this.isAuthorityVisible = function () {
        var isVisible = true;
        if (authorityChecker.isISC(this.settings)) {
            isVisible = false;
        }
        return isVisible;
    };

    this.setPrivacy = function (level) {
        this.onSetPrivacy({ level: level });
    };

    this.groupCategory = function (group) {
        return group.type === 'open' ? 'public' : 'group';
    };

    this.updateSelectedGroup = function (group) {
        this.onUpdateSelectedGroup(group);

    }
 


    /**
     * Determines whether the popup should appear above or below the button.
     * In addition the appropriate height is computed in order to prevent the popup from stretching beyond the
     * visible area.
     */
    const computeLayout = function () {
        const menuContainer = domElement.querySelector('.publish-annotation-btn__dropdown-container');

        const anchorElement = menuContainer.closest('.publish-annotation-btn__dropdown-anchor');
        const threadList = menuContainer.closest('thread-list');

        if (!anchorElement || !threadList) {
            return;
        }

        const anchorRect = anchorElement.getBoundingClientRect();
        const threadListRect = threadList.getBoundingClientRect();

        const spaceToTop = anchorRect.top - threadListRect.top;
        const spaceToBottom = threadListRect.bottom - anchorRect.bottom;


        if (spaceToBottom > spaceToTop) {
            applyLayoutBelowButton(menuContainer, spaceToBottom);
        } else {
            applyLayoutAboveButton(menuContainer, spaceToTop);
        }

    };

    /**
     * Applies the changes to the element in order to display it above the anchor.
     * @param {HTMLElement} menuElement 
     * @param {number} space
     */
    const applyLayoutAboveButton = function (menuElement, space) {

        const menuScrollContainer = menuElement.querySelector('.publish-annotation-btn__dropdown-scrollcontainer');
        const publishButton = menuElement.closest('.publish-annotation-btn');

        const buttonRect = publishButton.getBoundingClientRect();

        let height = space;
        height -= buttonRect.height; // account for the publish button over which the menu is being moved
        height -= 7; // account for arrow size
        height -= 30; // add some margin

        const style = `
            max-height: ${height}px;
        `;

        const menuStyle = `
            ${style}
            bottom: ${buttonRect.height + 7}px;
        `;

        menuScrollContainer.setAttribute('style', style);
        menuElement.setAttribute('style', menuStyle);

        const arrow = menuElement.parentElement.querySelector('.publish-annotation-btn__dropdown-arrow');
        arrow.setAttribute('style', `bottom: ${buttonRect.height + 7 + 1}px`)

        arrow.classList.remove('arrow-at-top');
        arrow.classList.add('arrow-at-bottom');

    };

    /**
     * Applies the changes to the element in order to display it below the anchor.
     * @param {HTMLElement} menuElement 
     * @param {number} space
     */
    const applyLayoutBelowButton = function (menuElement, space) {

        const menuScrollContainer = menuElement.querySelector('.publish-annotation-btn__dropdown-scrollcontainer');

        let height = space;
        height -= 7; // account for arrow size
        height -= 30; // add some margin

        const style = `
            max-height: ${height}px;
        `;

        menuScrollContainer.setAttribute('style', style);
        menuElement.setAttribute('style', style);

        const arrow = menuElement.parentElement.querySelector('.publish-annotation-btn__dropdown-arrow');
        arrow.setAttribute('style', `top: ${7 + 1}px`)

        arrow.classList.remove('arrow-at-bottom');
        arrow.classList.add('arrow-at-top');
    };

}

export = {
    controller: DropdownMenuController,
    controllerAs: 'vm',
    bindings: {
        groups: '<',
        privateLabel: '<',
        settings: '<',
        onUpdateSelectedGroup: '&',
        onSetPrivacy: '&',
        group: '<',
        isforward: '<',
        originGroup:'<',
    },
    template: require('../templates/dropdown-menu.html'),

};
