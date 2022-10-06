package fr.totetmatt.gephi.twitter;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.AddOrDeleteRulesRequest;
import com.twitter.clientlib.model.AddOrDeleteRulesResponse;
import com.twitter.clientlib.model.AddRulesRequest;
import com.twitter.clientlib.model.DeleteRulesRequest;
import com.twitter.clientlib.model.DeleteRulesRequestDelete;
import com.twitter.clientlib.model.Rule;
import com.twitter.clientlib.model.RuleNoId;
import com.twitter.clientlib.model.RulesLookupResponse;
import fr.totetmatt.gephi.twitter.networklogics.Networklogic;
import fr.totetmatt.gephi.twitter.utils.listener.filtered.TweetsStreamListenersExecutor;
import fr.totetmatt.gephi.twitter.utils.TwitterApiFields;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.project.api.Project;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TwitterStreamerV2.class)
public final class TwitterStreamerV2 {

    private CredentialProperty credentialProperty = new CredentialProperty();
    private TwitterApi apiInstance;
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
        

        this.loadCredentialProperty();
        apiInstance = new TwitterApi(new TwitterCredentialsBearer(this.getCredentialProperty().getBearerToken()));

    }

    public CredentialProperty getCredentialProperty() {
        return this.credentialProperty;
    }

    public void setCredentialProperty(CredentialProperty credentialProperty) {
        this.credentialProperty = credentialProperty;
        apiInstance = new TwitterApi(new TwitterCredentialsBearer(this.getCredentialProperty().getBearerToken()));
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
           streamResult = apiInstance.tweets().sampleStream()
                    .backfillMinutes(0)
                    .tweetFields(TwitterApiFields.tweetFields)
                    .expansions(TwitterApiFields.expansionsFields)
                    .userFields(TwitterApiFields.userFields)
                    .mediaFields(TwitterApiFields.mediaFields).executeWithHttpInfo();
        } else {
                   streamResult = apiInstance.tweets().searchStream()
                    .backfillMinutes(0)
                    .tweetFields(TwitterApiFields.tweetFields)
                    .expansions(TwitterApiFields.expansionsFields)
                    .userFields(TwitterApiFields.userFields)
                    .mediaFields(TwitterApiFields.mediaFields).executeWithHttpInfo();
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
                RulesLookupResponse getRules = apiInstance.tweets().getRules().execute();
                
                if(getRules!=null){
                    rules = getRules.getData();
                    if(currentNetworkLogic!=null){
                        currentNetworkLogic.refreshRulesNodeColumn(rules);
                    } 
                    return rules;
                }
            }
        } catch(ApiException e){
             Exceptions.printStackTrace(e);
            // The sdk don't parse correctly if there is no rules.
            // So this catch is needed
        }
        return rules=new ArrayList<>();
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
            AddOrDeleteRulesResponse response = apiInstance.tweets().addOrDeleteRules(addOrDeleteRulesRequest).execute();
            if(response.getErrors()!=null) {
                response.getErrors().forEach(error -> {
                    Exceptions.printStackTrace(new Exception(error.toJson()));
                });
            }
            return response.getData();
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
        AddOrDeleteRulesResponse response = apiInstance.tweets().addOrDeleteRules(addRequest).execute();
        if(response.getErrors()!=null) {
            response.getErrors().forEach(error -> {
                String msg = error.toJson()+
                        "\n\n" +
                        "Check also that your application have the right access level to use the Advanced Operators \n"+
                        "More info at https://developer.twitter.com/en/docs/twitter-api/tweets/filtered-stream/integrate/build-a-rule \n\n";
                Exceptions.printStackTrace(new Exception(msg));
            });
        }
        return response.getData();
    }

}
