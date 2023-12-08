/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages GRC Platform (PID: 5725-D51)
 *
 * © Copyright IBM Corporation  2020 - 2020. All Rights Reserved.
 *
 * US Government Users Restricted Rights- Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *******************************************************************************/


This demonstrates how to build a React app using Carbon that interacts with the task view.  
The app fetches the taskview field values , modifies it and sets the values back in the task view.  
All the relevant field definition and values can be found in OPS_FieldApiHelper.jsp.  

This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

In the project directory, you can run:

### `npm start`

Runs the app in the development mode.<br />
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.<br />
You will also see any lint errors in the console.

### Steps to run this demo

```cd <API_SAMPLES_DIR>/TaskViewFieldsHelper/react```  
```npm i```   
```PUBLIC_URL=/helper-sdk/ npm run build```  ( build command for this project)

Copy build files to <OP_HOME>/wlp-usr/shared/apps/op-apps.ear/sosa.war/helper-sdk
