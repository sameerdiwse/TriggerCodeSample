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
import React from 'react';
import './App.scss';
import { SampleForm } from './SampleForm';

export default class AppContainer extends React.Component<object, object> {

	render() {
		return (
			<div className="App">
				<h3>
					React + TypeScript + Carbon X Sample Helper
				</h3>
				<br />
				<br />
				<SampleForm />
			</div>
		);
	}
}
