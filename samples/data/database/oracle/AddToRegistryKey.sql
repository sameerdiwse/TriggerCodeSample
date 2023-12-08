-------------------------------------------------------------------------------
-- Licensed Materials - Property of IBM
--
--
-- OpenPages GRC Platform (PID: 5725-D51)
--
--  (c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.
--
-- US Government Users Restricted Rights- Use, duplication or
-- disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
--
-------------------------------------------------------------------------------
--
-- Parameters:
--	1 Registry Path of the setting to be updated
--	2 The value to add if not already there
--

declare
	lv_newValue		VARCHAR2(4000) :='';
	lv_regPath		VARCHAR2(4000) := '&&1';
	lv_parPath		VARCHAR2(4000) := substr(lv_regPath,1,instr(lv_regPath,'/',-1)-1);
	lv_newEntryValue	VARCHAR2(4000) := '&&2';
	
	lv_record				registryentries%rowtype;
	lv_actor				actorinfo%rowtype;

begin

	-- get the system user
	op_actor_mgr.get_actorinfo(OP_Actor_Mgr.gc_System_User, lv_actor);

	--get the current registry value  
	op_registry_mgr.get_registry_entry
	(
	  	p_entry_path       => lv_regPath,
	  	p_entry_rw         => lv_record,
	  	p_raise_not_found  => OP_Globals.sc_False
	  );
	  	
  	--Copy over current value if it is not the empty value string or null
	if  (lv_record.value is not null) and (lv_record.value != op_registry_mgr.g_empty_registry_value)
	then
 		DBMS_OUTPUT.PUT_LINE(concat('Old Value: ',lv_record.value));
		
		if ((instr(lv_record.value,lv_newEntryValue) = 0) and (instr(lv_record.value,concat(substr(lv_newEntryValue,2),',')) = 0))
		then
	 		DBMS_OUTPUT.PUT_LINE('Saving new Value');

			lv_newValue:= concat(lv_record.value, lv_newEntryValue);

			DBMS_OUTPUT.PUT_LINE(concat('Parent Path: ',lv_parPath));
	 		DBMS_OUTPUT.PUT_LINE(concat('lv_record.name: ',lv_record.name));
	 		DBMS_OUTPUT.PUT_LINE(concat('lv_record.description: ',lv_record.description));
	 		DBMS_OUTPUT.PUT_LINE(concat('New value: ',lv_newValue));

	 		op_registry_mgr.set_registry_entry
 			(
 				p_parent_path            => lv_parPath,
 				p_name                   => lv_record.name,
 				p_description            =>lv_record.description,
 				p_value                  => lv_newValue,
 				p_is_hidden              => lv_record.is_hidden,
 				p_is_encrypted           =>  lv_record.is_encrypted,
 				p_is_protected           => lv_record.is_protected,
 				p_actor_id               => lv_actor.actorid,
 				p_is_done_by_vendor      => OP_Globals.sc_true,
 				p_behavior               => OP_Registry_Mgr.OPT_RE_Set_Defaults
			);
			commit;
		else
	 		DBMS_OUTPUT.PUT_LINE('Value already set');

		end if;
	else
		DBMS_OUTPUT.PUT_LINE('null or empty');

		DBMS_OUTPUT.PUT_LINE('Saving new Value');

			lv_newValue:= lv_newEntryValue;

			if (substr(lv_newValue,1,1) = ',')
			then
				lv_newValue:= substr(lv_newValue,2);
			end if;

			DBMS_OUTPUT.PUT_LINE(concat('Parent Path: ',lv_parPath));
	 		DBMS_OUTPUT.PUT_LINE(concat('lv_record.name: ',lv_record.name));
	 		DBMS_OUTPUT.PUT_LINE(concat('lv_record.description: ',lv_record.description));
	 		DBMS_OUTPUT.PUT_LINE(concat('New value: ',lv_newValue));

	 		op_registry_mgr.set_registry_entry
 			(
 				p_parent_path            => lv_parPath,
 				p_name                   => lv_record.name,
 				p_description            =>lv_record.description,
 				p_value                  => lv_newValue,
 				p_is_hidden              => lv_record.is_hidden,
 				p_is_encrypted           =>  lv_record.is_encrypted,
 				p_is_protected           => lv_record.is_protected,
 				p_actor_id               => lv_actor.actorid,
 				p_is_done_by_vendor      => OP_Globals.sc_true,
 				p_behavior               => OP_Registry_Mgr.OPT_RE_Set_Defaults
			);
			commit;
	end if;
end;
/