/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages GRC Platform (PID: 5725-D51)
 *
 * © Copyright IBM Corporation  2020 - 2020. All Rights Reserved.
 *
 * US Government Users Restricted Rights- Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 * 
 * INTERNAL USE ONLY
 **/

export enum TaskViewMessageType {
	getTaskViewFields = 'getTaskViewFields',
	taskViewFieldValues = 'taskViewFieldValues',
	setTaskViewFields = 'setTaskViewFields'
}

export interface TaskViewMessage {
	type: TaskViewMessageType
}

/**
 * Request the current values of all fields on the specified TFUI task view
 */
export interface GetFieldsMessage extends TaskViewMessage {
	resourceId: string
}

/**
 * The value of a single field
 */
export interface TaskViewFieldValue {
	name: string;
	value: any;
	modified?: boolean;
	readOnly?: boolean;
}

/**
 * The value of all fields of the specified TFUI task view
 */
export interface TaskViewFieldValuesMessage extends TaskViewMessage {
	resourceId: string;
	fields: TaskViewFieldValue[];
}

/**
 * Set the specified task view field values
 */
export interface SetTaskViewFieldsMessage extends TaskViewMessage {
	resourceId: string;
	fields: TaskViewFieldValue[];
}

// callback listener
type TaskViewMessageListener = (message: TaskViewMessage) => void;


export function getFields(resourceId: string) {
	const message: GetFieldsMessage = {
		type: TaskViewMessageType.getTaskViewFields,
		resourceId: resourceId
	}
	sendMessage(message)
}

export function sendMessage(message: TaskViewMessage) {
	window.opener.postMessage(message, "*");
}

let listener: TaskViewMessageListener;
export function listen(callback: TaskViewMessageListener) {
	listener = callback;
}

// handle messages from parent window
function handleMessage(event: any) {
	const message: TaskViewMessage = event.data;
	if (listener) {
		listener(message);
	}
}
window.addEventListener("message", handleMessage, false);




