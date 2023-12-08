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

import React, { Component, MouseEvent, FormEvent, ChangeEvent } from 'react';

import './App.scss';
import { Button, Form, TextInput, Dropdown } from 'carbon-components-react';
import * as API from './tfuiFieldApi';

type Dictionary = {
	[key: string]: any;
}

type State = {
	formKey: number;
    fieldValues: Dictionary;
}

// only push these fields back to open pages
const UPDATE_KEYS_SET = new Set(['System Fields:Name']);
export class SampleForm extends Component<object, State> {

	resourceId?: string;

	state = {
		formKey: 0,
        fieldValues: {} as Dictionary
	}

	constructor(props: any) {
		super(props);

		// get resource id (recommend use router like react-router)
		const params = window.location.search.replace('?', '').split('&');
		const param = params.find((p) => p.indexOf('resourceId') >= 0);
		this.resourceId = param?.split('=')[1];

		if (this.resourceId) {
			API.listen(this.onMessage.bind(this));
		}
	}

	// process incoming message from OpenPages
	onMessage(message: API.TaskViewMessage) {
		if (message.type === API.TaskViewMessageType.taskViewFieldValues) {
			this.onFieldValues(message as API.TaskViewFieldValuesMessage);
		}
	}
    // setting state from incoming field values.
	onFieldValues(message: API.TaskViewFieldValuesMessage) {
		const fieldValues = { ...this.state.fieldValues };
		for (var i = 0; i < message.fields.length; i++) {
            var field = message.fields[i];
			fieldValues[field.name] = field.value;
		}
		this.setState({ formKey: this.state.formKey + 1, fieldValues })
	}

	onGetFieldsClick(e: MouseEvent) {
		e.preventDefault();
		if (this.resourceId) {
			API.getFields(this.resourceId);
		}
	}

	onSubmit(e: FormEvent) {
		console.log('submit')
		e.preventDefault();
		if (this.resourceId) {
			const message: API.SetTaskViewFieldsMessage = {
				type: API.TaskViewMessageType.setTaskViewFields,
				resourceId: this.resourceId,
				fields: []
			}
			const keys = Array.from(UPDATE_KEYS_SET);
			for (const key of keys) {
				console.log('set, key=' + key + ' value=' + this.state.fieldValues[key]);
				message.fields.push({
					name: key,
					value: this.state.fieldValues[key]
				});
			}
			API.sendMessage(message);
		}
	}

	onFieldChange = (e: ChangeEvent) => {
        const fieldValues = { ...this.state.fieldValues };
		fieldValues[e.target.id] = (e.target as any).value;
        this.setState({ fieldValues })
        UPDATE_KEYS_SET.add(e.target.id)
    }

    initialSelectedItem = (id: string) => {
        return this.getItems().find((x: any) => x.id === id)
    }
    // This is the sample method to populate dropdown items with the value of the field.

    getItems() {
        return [
            {
                id: '1693',
                value: '1693',
                text: 'Effective'
            },
            {
                id: '1694',
                value: '1694',
                text: 'Ineffective'
            },
            {
                id: '1695',
                value: '1695',
                text: 'Not Determined'
            }
        ]
    }

    getDescriptionItems() {
        return ['Description 1', 'Description 2'];
    }

	render() {
		const onSubmit = this.onSubmit.bind(this);
		const onFieldChange = this.onFieldChange.bind(this);
        const onGetFieldsClick = this.onGetFieldsClick.bind(this);
        const items = this.getItems();
        const descriptionItems = this.getDescriptionItems();

		return (
			<div>
				<p>
					Resource Id: {this.resourceId}
				</p>

				<br />
				<br />

				<Form id="sampleForm" key={this.state.formKey} onSubmit={onSubmit}>
					<TextInput
						labelText="Name"
						id="System Fields:Name"
						defaultValue={this.state.fieldValues['System Fields:Name']}
						onChange={onFieldChange}
					/>
                    <Dropdown
                        id="System Fields:Description"
                        titleText=""
                        ariaLabel="Description Options"
                        label="Description Options"
                        onChange={({ selectedItem}) => {
                            const fieldValues = { ...this.state.fieldValues };
                            fieldValues['System Fields:Description'] = selectedItem;
                            this.setState({ fieldValues })
                            UPDATE_KEYS_SET.add('System Fields:Description')
                        }}
                        items={descriptionItems}
                        selectedItem={this.state.fieldValues['System Fields:Description']}
                    />
                    <Dropdown
                        id="OPSS-Ctl:Design Effectiveness"
                        titleText=""
                        ariaLabel="Design Effectiveness"
                        label="Design Effectiveness"
                        onChange={({ selectedItem }) => {
                            const fieldValues = { ...this.state.fieldValues };
                            fieldValues['OPSS-Ctl:Design Effectiveness'] = [selectedItem!.id];
                            this.setState({ fieldValues })
                            UPDATE_KEYS_SET.add('OPSS-Ctl:Design Effectiveness')

                        }}
                        items={items}
                        itemToString={item => (item ? item.text:"")}
                        selectedItem={this.initialSelectedItem(this.state.fieldValues['OPSS-Ctl:Design Effectiveness'])}
                    />
					<Button type="button" onClick={onGetFieldsClick}>
						Get Task View Fields
					</Button>
					<Button type="submit">
						Set Task View Fields
					</Button>
				</Form>
			</div>
		)
	}
}
