package org.dspace.workflow_new;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.core.ConfigurationManager;
import org.dspace.workflow_new.Actions.Action;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.SQLException;
import java.io.File;

/**
 * @author: Bram De Schouwer
 */
public class WorkflowFactory {

    private static HashMap<String, Workflow> workflowCache;
    private static HashMap<String, String> rolesCache;

    /*
     * Returns the workflow system for a collection and creates it
     * in case it does not exist
     */
    public static Workflow getWorkflow(Context context, int collectionId) throws WorkflowConfigurationException, SQLException {
        String id = getWorkflowId(context, collectionId);
        if (workflowCache == null) {
            workflowCache = new HashMap<String, Workflow>();
            Node top = getTopNode();
            NodeList nodes = top.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals("workflow")) {
                    String wfId = nodes.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeValue();
                    Workflow wf = new Workflow(wfId, nodes.item(i));
                    workflowCache.put(wfId, wf);
                }
            }
            if (workflowCache.get(id) != null) {
                return workflowCache.get(id);
            } else if (workflowCache.get("default") != null) {
                return workflowCache.get("default");
            }
            throw new WorkflowConfigurationException("No workflow defined for this item and no default workflow found.");
        } else {
            if (workflowCache.get(id) != null) {
                return workflowCache.get(id);
            } else if (workflowCache.get("default") != null) {
                return workflowCache.get("default");
            }
            throw new WorkflowConfigurationException("No workflow defined for this item and no default workflow found.");
        }
    }
    /*
     * Return the ID of the workflow system linked to a collection
     */
    private static String getWorkflowId(Context context, int collectionId) throws WorkflowConfigurationException, SQLException {
        Node node = getTopNode();
        NodeList children = node.getChildNodes();
        Node map = null;
        for(int i = 0; i < children.getLength(); i++){
            if(children.item(i).getNodeName().equals("workflow-map")){
                map = children.item(i);
                break;
            }
        }
        if(map ==null){
            throw new WorkflowConfigurationException("No mapping between collection name and workflow name set.");
        }else{
            NodeList nameMaps = map.getChildNodes();
            String defaultWf = null;
            String handle = Collection.find(context,collectionId).getHandle();
            for(int i = 0; i < nameMaps.getLength(); i++){
                if(nameMaps.item(i).getNodeName().equals("name-map")&&nameMaps.item(i).getAttributes().getNamedItem("collection_handle").getFirstChild().getNodeValue().equals(handle)){
                    return nameMaps.item(i).getAttributes().getNamedItem("workflow_id").getFirstChild().getNodeValue();
                }
                if(nameMaps.item(i).getNodeName().equals("name-map")&&nameMaps.item(i).getAttributes().getNamedItem("collection_handle").getFirstChild().getNodeValue().equals("default")){
                    defaultWf = nameMaps.item(i).getAttributes().getNamedItem("workflow_id").getFirstChild().getNodeValue();
                }
            }
            if(defaultWf!=null){
                return defaultWf;
            }
            throw new WorkflowConfigurationException("No mapping between collection name en workflow name set and no default value found.");

        }
    }
    /*
     * Returns a list of action nodes found in a given node
     */
    private static ArrayList<Node> getActionNodes(Node node) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        NodeList actions = node.getChildNodes();
        for (int j = 0; j < actions.getLength(); j++) {
            if (actions.item(j).getNodeName().equals("action")) {
                nodes.add(actions.item(j));
            }
        }
        return nodes;
    }
    /*
     * Creates the actions for a given step
     */
    public static void createActions(Workflow wf, Step step) throws WorkflowConfigurationException {
        Node stepNode = getStepNode(wf.getId(), step.getId());
        ArrayList<Node> actionNodes = getActionNodes(stepNode);
        for (Node node : actionNodes) {
            String actionType = node.getAttributes().getNamedItem("type").getFirstChild().getNodeValue();
            if (step.getAction(node.getAttributes().getNamedItem("id").getFirstChild().getNodeValue()) == null) {
                try {
                    Class action = Class.forName(actionType);
                    Action act = (Action) action.getConstructor(new Class[]{Node.class, Step.class}).newInstance(new Object[]{node, step});
                } catch (Exception e){
                    throw new WorkflowConfigurationException("WorkflowConfigurationException: Error creating actions for step:"+step.getId());
                }
            }
        }
    }
    /*
     * Returns the top node of the workflow configuration xml
     */
    private static Node getTopNode() throws WorkflowConfigurationException {
        String uri = ConfigurationManager.getProperty("dspace.dir")
            + File.separator + "config" + File.separator + "workflow.xml";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = null;
        try {
            db = factory.newDocumentBuilder();
            Document doc = db.parse(uri);
            return doc.getFirstChild();
        } catch (Exception e){
            throw new WorkflowConfigurationException("WorkflowConfigurationException: error finding xml-top node");
        }
    }
    /*
     * Returns the step node inside a workflow system with a given step id
     */
    public static Node getStepNode(String wfId, String id) throws WorkflowConfigurationException {
        Node top = getTopNode();
        NodeList children = top.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals("workflow")&&children.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeValue().equals(wfId)) {
                NodeList steps = children.item(i).getChildNodes();
                for (int k = 0; k < steps.getLength(); k++) {
                    if (steps.item(k).getAttributes() != null && steps.item(k).getAttributes().getNamedItem("id").getFirstChild().getNodeValue().equals(id)) {
                        return steps.item(k);
                    }
                }
            }
        }
        throw new WorkflowConfigurationException("WorkflowConfigurationException: Can not find step:"+id);
    }
    /*
     * Creates an action with a given ID for a given step and workflow system
     */
    public static Action createAction(String workflowId, String actionId, Step step) throws WorkflowConfigurationException {
        Node stepNode = getStepNode(workflowId, step.getId());
        Node node = getActionNode(actionId, stepNode);
        String actionType = node.getAttributes().getNamedItem("type").getFirstChild().getNodeValue();
        if (step.getAction(actionId) == null) {
            try {
                Class action = Class.forName(actionType);
                Action act = (Action) action.getConstructor(new Class[]{Node.class, Step.class}).newInstance(new Object[]{node, step});
                return act;
            } catch (Exception e){
                throw new WorkflowConfigurationException("WorkflowConfigurationException: creating action:"+actionId+" for step:"+step.getId());
            }
        }
        return step.getAction(actionId);
    }
    /*
     * Returns the node of an action found in a given stepNode
     */
    public static Node getActionNode(String id, Node stepNode) throws WorkflowConfigurationException {
        NodeList actions = stepNode.getChildNodes();
        for (int i = 0; i < actions.getLength(); i++) {
            if (actions.item(i).getNodeName().equals("action")) {
                if (actions.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeValue().equals(id)) {
                    return actions.item(i);
                }
            }
        }
        throw new WorkflowConfigurationException("WorkflowConfigurationException: unable to find action node:"+id);
    }
    /*
     * Returns the the roles (id,name) in the workflow
     */
    public static HashMap<String, String> getRoles() throws WorkflowConfigurationException {
        if(rolesCache!=null){
            return rolesCache;
        }else{
            rolesCache = new HashMap<String, String>();
            Node node = getTopNode();
            NodeList children = node.getChildNodes();
            Node roles = null;
            for(int i = 0; i < children.getLength(); i++){
                if(children.item(i).getNodeName().equals("roles")){
                    roles = children.item(i);
                    break;
                }
            }
            NodeList rolesList = roles.getChildNodes();
            for(int i = 0; i < rolesList.getLength(); i++){
                if(rolesList.item(i).getNodeName().equals("role")){
                    rolesCache.put(rolesList.item(i).getAttributes().getNamedItem("id").getFirstChild().getNodeValue(),
                            rolesList.item(i).getAttributes().getNamedItem("name").getFirstChild().getNodeValue());
                }
            }
            return rolesCache;
        }
    }

}
