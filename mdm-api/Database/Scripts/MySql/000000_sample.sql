INSERT INTO app_permission_mapping_master (ERM_CODE, APP_ID)  values ('STD_APP_TILE', 29);
INSERT INTO app_permission_mapping_master (ERM_CODE, APP_ID)  values ('DEFAULT_APP_VIEW', 29);
--//@UNDO
delete from app_permission_mapping_master where app_id = 29;
