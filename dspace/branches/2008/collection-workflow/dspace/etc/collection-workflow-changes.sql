CREATE SEQUENCE workflowroles_seq INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;

CREATE TABLE workflowroles (
workflowroles_id integer DEFAULT nextval('workflowroles_seq'::regclass) NOT NULL,
role_id Text,
collection_id integer REFERENCES collection(collection_id),
group_id integer REFERENCES epersongroup(eperson_group_id));

ALTER TABLE ONLY workflowroles
ADD CONSTRAINT workflowroles_pkey PRIMARY KEY (workflosroles_id);
ALTER TABLE ONLY workflowroles
ADD CONSTRAINT workflowroles_unique UNIQUE (role_id, collection_id, group_id);

CREATE SEQUENCE workflowassignment_seq INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;

CREATE TABLE WorkflowAssignment (
workflow_assignment_id integer DEFAULT nextval('workflowassignment_seq'::regclass) NOT NULL,
role_id Text,
group_id integer REFERENCES epersongroup(eperson_group_id),
collection_id integer REFERENCES collection(collection_id));

ALTER TABLE ONLY WorkflowAssignment
ADD CONSTRAINT workflow_assignment_pkey PRIMARY KEY (workflow_assignment_id);
ALTER TABLE ONLY WorkflowAssignment
ADD CONSTRAINT workflow_assignment_unique UNIQUE (role_id, collection_id);

ALTER TABLE tasklistitem ADD text step_id

CREATE SEQUENCE taskowner_seq INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;

CREATE TABLE taskowner (
taskowner_id integer DEFAULT nextval('taskowner_seq'::regclass) NOT NULL,
workflow_item_id integer REFERENCES workflowitem(workflow_id),
step_id Text,
action_id Text,
owner_id integer REFERENCES eperson(eperson_id));

ALTER TABLE ONLY taskowner
ADD CONSTRAINT taskowner_pkey PRIMARY KEY (taskowner_id);
ALTER TABLE ONLY taskowner
ADD CONSTRAINT taskowner_unique UNIQUE (step_id, workflow_item_id, owner_id, action_id);