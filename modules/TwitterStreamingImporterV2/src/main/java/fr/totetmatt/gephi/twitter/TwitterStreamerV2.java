package fr.totetmatt.gephi.twitter;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.AddOrDeleteRulesRequest;
import com.twitter.clientlib.model.AddRulesRequest;
import com.twitter.clientlib.model.DeleteRulesRequest;
import com.twitter.clientlib.model.DeleteRulesRequestDelete;
import com.twitter.clientlib.model.GetRulesResponse;
import com.twitter.clientlib.model.Rule;
import com.twitter.clientlib.model.RuleNoId;
import fr.totetmatt.gephi.twitter.networklogics.Networklogic;
import fr.totetmatt.gephi.twitter.utils.listener.filtered.TweetsStreamListenersExecutor;
import fr.totetmatt.gephi.twitter.utils.TwitterApiFields;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.project.api.Project;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TwitterStreamerV2.class)
public final class TwitterStreamerV2 {

    private CredentialProperty credentialProperty = new CredentialProperty();
    private final TwitterApi apiInstance;
    private InputStream streamResult;
    private TweetsStreamListenersExecutor tsle;
    private List<Rule> rules;
    private Networklogic currentNetworkLogic = null;
    private void initProjectAndWorkspace() {
        ProjectControllerUI  projectController = Lookup.getDefault().lookup(ProjectControllerUI.class);
        Project currentProject = projectController.getCurrentProject();
        if (currentProject == null) {
            projectController.newProject();
        }
        
        
    }

    public TwitterStreamerV2() {
        initProjectAndWorkspace();
        apiInstance = new TwitterApi();

        this.loadCredentialProperty();
        apiInstance.setTwitterCredentials(new TwitterCredentialsBearer(this.getCredentialProperty().getBearerToken()));

    }

    public CredentialProperty getCredentialProperty() {
        return this.credentialProperty;
    }

    public void setCredentialProperty(CredentialProperty credentialProperty) {
        this.credentialProperty = credentialProperty;
        apiInstance.setTwitterCredentials(new TwitterCredentialsBearer(this.getCredentialProperty().getBearerToken()));
    }

    public void loadCredentialProperty() {
        credentialProperty.setBearerToken(NbPreferences.forModule(TwitterStreamerV2.class).get("twitter.bearerToken", ""));
    }

    public void saveCredentialProperty() {
        NbPreferences.forModule(TwitterStreamerV2.class).put("twitter.bearerToken", credentialProperty.getBearerToken());
    }

    public void streamStart(Networklogic networklogic, boolean useSampleStream) throws ApiException {
        currentNetworkLogic = networklogic;
        currentNetworkLogic.refreshGraphModel(this.rules);
        if (useSampleStream) {
            streamResult = apiInstance.tweets().sampleStream(TwitterApiFields.expansionsFields, TwitterApiFields.tweetFields, TwitterApiFields.userFields, TwitterApiFields.mediaFields, null, null, 0);
        } else {
            streamResult = apiInstance.tweets().searchStream(TwitterApiFields.expansionsFields, TwitterApiFields.tweetFields, TwitterApiFields.userFields, TwitterApiFields.mediaFields, null, null, 0);
        }
        tsle = new TweetsStreamListenersExecutor(streamResult, currentNetworkLogic.getGraphModel().getGraph());
        tsle.addListener(currentNetworkLogic);
        tsle.executeListeners();
    }

    public void streamStop() {
        currentNetworkLogic = null;
        tsle.shutdown();
    }

    public List<Rule> refreshRules() throws ApiException {
        try {
            if (!this.credentialProperty.getBearerToken().isEmpty()) {
                GetRulesResponse getRules = apiInstance.tweets().getRules(null, 100, null);
                if(getRules!=null){
                rules = getRules.getData();
                if(currentNetworkLogic!=null){
                    currentNetworkLogic.refreshRulesNodeColumn(rules);
                }
                return rules;
                }
            }
        } catch(Exception e){
            // Exceptions.printStackTrace(e);
            // The sdk don't parse correctly if there is no rules.
            // So this catch is needed
        }
        return rules=new ArrayList<Rule>();
    }

    public List<Rule> getRules() throws ApiException {
        return rules;
    }

    public List<Rule> deleteRule(List<String> deleteRuleIds) throws ApiException {
        if (!deleteRuleIds.isEmpty()) {
            final DeleteRulesRequestDelete deleteRequestDelete = new DeleteRulesRequestDelete();
            deleteRequestDelete.setIds(deleteRuleIds);
            final DeleteRulesRequest deleteRuleRequest = new DeleteRulesRequest();
            deleteRuleRequest.setDelete(deleteRequestDelete);
            final AddOrDeleteRulesRequest addOrDeleteRulesRequest = new AddOrDeleteRulesRequest(deleteRuleRequest);
            return apiInstance.tweets().addOrDeleteRules(1, addOrDeleteRulesRequest, false).getData();
        }
        return this.rules;

    }

    public List<Rule> addRule(String tag, String value) throws ApiException {
        final RuleNoId rule = new RuleNoId();
        rule.setTag(tag);
        rule.setValue(value);
        final AddRulesRequest addRule = new AddRulesRequest();
        addRule.addAddItem(rule);
        final AddOrDeleteRulesRequest addRequest = new AddOrDeleteRulesRequest(addRule);
        return apiInstance.tweets().addOrDeleteRules(1, addRequest, false).getData();
    }

}
