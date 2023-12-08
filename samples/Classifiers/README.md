# Classifier Field Set Up

These are the steps you will need to perform in order to set up a classifier field and see it in an Add New view.

### Make sure you have json enabled as a file type
1.  Log in to OpenPages as OpenPagesAdministrator.
2.  In the Standard UI, go to Admin > Object Types and select SOXDocument
3.  Make sure the "json" appears in the File Types Information table.
4.  If it doesn't, click "Include..." and add it.

### Create the classifier instance
1.  There are 3 json files in this folder that correspond to different classifier types.  Choose the one you want to set up and add the API Key inside the quotes following the *apiKey* attribute in the JSON.  These keys have been omitted for security reasons.  Contact Christophe Delaure to get the latest keys.
2.  Log in to OpenPages as OpenPagesAdministrator.
3.  In the Standard UI, switch to the "OpenPages Platform 3" profile (from OpenPagesAdministrator drop-down menu in header).
4.  From Administration menu select Manage System Files > SysXMLDocument
5.  Open the folder "End User Applications Config".
6.  Check the box next to the "classifiers" folder and then click "Add New..."
7.  Click "Choose File", select the JSON file from this folder for the classifier that you want to configure, and click Create.
    - Basel II Classification: *classifier-basel_config.json*
    - Child Control Association: *classifier-assoc-controls_config.json*
    - Parent Control Association: *classifier-parent-controls_config.json*

You can verify that the classifier instance was created by going to Admin > Cognitive Services > Natural Language Classifiers in the Standard UI.  You should see the files you loaded listed in the table.  Click on one to see its properties in the configuration UI.  You can also create classifier instances manually this way, using the values from the JSON files for input.

### Create the classifier field
1.  Log in to OpenPages as OpenPagesAdministrator.
2.  Go to Admin > Field Groups and create a new field group (or select an existing one)
3.  Click "Add..." and provide the following data:
    - Name:  Can be anything you want
    - Data Type:  Classifier
    - Classifier Name:  Must correspond to the name of the classifier loaded above (e.g., "classifier-basel"), which also MUST match the name in the file (e.g., "classifier-basel_config.json")
    - Classifying Field:  Must be in fieldGroup.field format (e.g., "System Fields.Description")
    - Save

### Add the field to the profile
1.  Add the field group to the object type
    - Go to Admin > Object Types and select the object type you want
    - Make sure the field group you used above is listed under Included Field Groups.
    - For Basel II Classification, you'll also want to make sure that the OPSS-Shared-Basel field group is present.
    - For Control Association, you'll want to make sure that the object type selected allows child/parent Control association.
2.  Add the field to the profile
    -  Go to Admin > Profiles and select the profile you want.
    -  Select the object type you want to add the field to.
    -  Under Object Fields, click "Include..." to add the classifier field
    -  Display Type will default to the right thing.  You can't change this.
    -  For Basel II Classification, make sure that the Risk Category, Risk Subcategory, and Risk Example fields are also in the profile.

### Load the Control objects (association classifiers only)
The Watson suggestions for the Control Association classifiers included here reference a specific set of Control objects.  You will need to load these into the system.  Otherwise you will get no suggestions.  Note that the server code will validate the existence of each suggested object before including in the suggestions list.
1.  In the Standard UI, go to Reporting > FastMap Import
2.  Click "Browse..." and select the file *ISO 27002 Fastmap Controls.xls* included in this folder.
3.  Click "Import Data".

### Standard UI: Add the field to a view
1.  Add a new Creation or Detail View and give it any name.
2.  On the Field Settings page add the classifier field you created above.
3.  Make sure the Read-Only checkbox is NOT checked (it shouldn't be).
4.  Make sure the classifying field is there as well (Description in the steps above).
5.  Add other fields and sections/pages as desired.

### Task-Focused UI: Add the field to the view
1.  In the TFUI, create or edit a view for the object type used above.
2.  Drag the classifier field into a section in the view.
3.  Drag the classifier input field (e.g., Description) into the view.
4.  For Basel II Classification, you are done.
    - The field will appear as text with a link that will launch the suggestions flyover.
	- You may place the field where you want the link to appear in the view (e.g., under the Description field or grouped with the risk category fields).
5.  For Control Association, you must also create a Relationship field with the correct object type and relationship type.
    - Set the object type (e.g., Control) and relationship type (e.g., Children).
	- Add an action of type "Get Watson Suggestion".
	- Select the name of the classifier field in the flyover and give the action a name.  If you do not have a classifier field with the right properties, the flyover will show you a message explaining what needs to be done.
	- A button will appear in the relationship field that will launch the suggestions flyover.  The classifier field itself will not appear in the UI (so it doesn't matter where you put it).

# Classifier Field Code

Here are some pointers on where to find code for various parts of the classifier implementation.

### Administration Code
- Validation for the "Get Watson Suggestion" action is in *ViewValidateService* (*validateSuggestAssociationsAction*).

- The classifier field flyover code is in the View Designer is in *VDSuggestAssocFlyoverStore* and *VDFieldFlyoverStore*.

- Code for managing the configuration of the classifier instance is in *com.openpages.apps.common.classifier*
    - *ClassifierUtil.java* also contains a number of functions used for processing and synchronizing results

- The REST end points for the configuration page are in *ClassifierController*.

- The REST end points for the Natural Language Classifiers page (i.e., the list of classifiers) is in *ClassifierViewController*.

- Code for updating and validating the classifier field itself (in the Standard UI) is in *FieldDefinitionAction* and *FieldDefinitionActionForm*.

- Classifier fields have special code to handle the audit trail (change history).  This is in *ProfileManager*.

- Beans for serializing and deserializing the data from the Watson NLC service is grouped in *com.openpages.sdk.repository.classifier*

- SDK tests for the classifier data type are in *ClassifierDataTypeTest*.

### End User Code
- Classifier fields are handled similarly to Computed fields in many respects, since they query another service (Watson NLC instead of Cognos) to get their values. They are distinguished sometimes by their data type (PropertyTypeConstants.CLASSIFIER_TYPE) and sometimes by their display type (DisplayType.LINK, a.k.a. "On Demand").

- The REST end points for fetching and synchronizing Watson suggestions for the Standard UI are in WatsonNLCController.

- Standard UI code to update the enumeration fields based on the selected classifier suggestion is in PropertyFormAction

- Standard UI code for adding/removing associations based on the selected classifier suggestion is in SosaPropertyFormAction (processClassifierField).

- The REST end point for fetching Watson suggestions in the TFUI is in WatsonClassifierController.  The corresponding service is WatsonClassifierService.

- The platform code for processing and persisting classifier fields is in com.openpages.aurora.classifier

- TFUI code to update the enumeration fields based on the classifier suggestion is in ObjectConverter (updateClassifierRelatedFields)

- TFUI model for classifier fields is in classifierModel.ts

- Because classifiers involve multiple fields in a view (classifier field, input field, enumeration fields, relationship field) there is a store at the task view level for managing all this.  It's called TaskViewClassifierStore.

- The code for the classifier field itself (including the suggestions flyover, input field guidance, and store) is in react-ui/src/fields/classifierField.
   - Except for the code for managing the interaction with the relationship field.  This is in eact-ui/src/fields/relationshipField/actions/suggestAssociationAction.tsx

- Test code for the NLC controller (end user classifier interactions) is in WatsonNLCControllerTest.

- Test code for the TFUI classifier store is in taskViewClassifierStore.spec.tsx

- There is some pretty robust test code for the serialization/deserialization of data between the NLC service and the OpenPages code in ClassifierJsonTest.
