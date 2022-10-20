package eu.europa.ec.leos.ui.component.contributions;

import com.google.common.eventbus.EventBus;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.web.support.user.UserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringComponent
@ViewScope
@DesignRoot("ContributionsTabDesign.html")
public class ContributionsTab<D extends XmlDocument> extends VerticalLayout {
    private static final Logger LOG = LoggerFactory.getLogger(ContributionsTab.class);
    private static final long serialVersionUID = -2540336182761979302L;

    private EventBus eventBus;
    private MessageHelper messageHelper;
    private UserHelper userHelper;

    private VerticalLayout contributionsCardsHolder;
    private Button searchButton;
    private FormLayout filterHolder;
    private TextField authorTextField;

    private List<ContributionVO> allContributions;
    private List<ContributionCard<D>> allContributionCards = new ArrayList<>();
    private Set<ContributionCard.ContributionRow> allContributionRows = new HashSet<>();

    @Autowired
    public ContributionsTab(MessageHelper messageHelper, EventBus eventBus, UserHelper userHelper) {
        this.messageHelper = messageHelper;
        this.eventBus = eventBus;
        this.userHelper = userHelper;

        Design.read(this);
        initView();
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }

    @Override
    public void detach() {
        super.detach();
        eventBus.unregister(this);
    }

    private void initView() {
        searchButton.setIcon(VaadinIcons.SEARCH);
        searchButton.addClickListener(this::showHideFilter);

        filterHolder.setVisible(false);

        authorTextField.setCaption(messageHelper.getMessage("document.versions.filter.author.caption"));
        authorTextField.addValueChangeListener(this::filterContributionCards);

        setFilterDefaults();
    }

    private void setFilterDefaults() {
        authorTextField.setValue("");
    }

    private void showHideFilter(Button.ClickEvent clickEvent) {
        boolean visible = filterHolder.isVisible();
        if (visible) {
            setFilterDefaults();
        }
        filterHolder.setVisible(!visible);
    }

    private boolean getContributionRowVisibilityByAuthor(ContributionCard.ContributionRow contributionRow) {
        String value = authorTextField.getValue();
        if (value == null || "".equals(value)) {
            return true;
        }
        return contributionRow.getUserName().contains(value.toLowerCase());
    }

    private boolean getContributionRowVisibility(ContributionCard.ContributionRow contributionRow) {
        return getContributionRowVisibilityByAuthor(contributionRow);
    }

    private void filterContributionCards(HasValue.ValueChangeEvent<String> valueChangeEvent) {
        allContributionRows.forEach(ContributionCard.ContributionRow::applyMatcher);
    }

    public void populateContributionsData(List<ContributionVO> contributions) {
        this.allContributions = contributions;
        buildContributionCards();
    }
    
    private void buildContributionCards() {
        contributionsCardsHolder.removeAllComponents();
        ListDataProvider<ContributionVO> dataProvider = DataProvider.ofCollection(allContributions);

        for (ContributionVO contributionVO : allContributions) {
            ContributionCard<D> card = new ContributionCard<D>(contributionVO,
                    messageHelper, eventBus, userHelper, dataProvider,
                    allContributionRows,
                    this::getContributionRowVisibility);
            contributionsCardsHolder.addComponent(card);
            allContributionCards.add(card);
        }
    }
}
