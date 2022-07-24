
db.getCollection('reference_Meta').insert({
	"type" : "property",
    "name" : "eka_ctrm_host",
    "propertyLevel" : "tenant,app,user",
    "dataType" : "String",
    "fieldtype" : "textbox",
    "length" : "120"
});//
db.getCollection('reference_Meta').insert({
	"type" : "property",
    "name" : "eka_utility_host",
    "propertyLevel" : "tenant,app,user",
    "dataType" : "String",
    "fieldtype" : "textbox",
    "length" : "120"
});//
db.getCollection('reference_Meta').insert({
	"type" : "property",
    "name" : "eka_platform_host",
    "propertyLevel" : "tenant,app,user",
    "dataType" : "String",
    "fieldtype" : "textbox",
    "length" : "120"
});//
db.getCollection('reference_Meta').insert({
	"type" : "property",
    "name" : "eka_auth_server_host",
    "propertyLevel" : "tenant,app,user",
    "dataType" : "String",
    "fieldtype" : "textbox",
    "length" : "120"
});//
db.getCollection('reference_Meta').insert({
	"type" : "property",
    "name" : "eka_redis_host",
    "propertyLevel" : "tenant,app,user",
    "dataType" : "String",
    "fieldtype" : "textbox",
    "length" : "120"
});//
db.getCollection('reference_Meta').insert({
	"type" : "property",
    "name" : "eka_redis_port",
    "propertyLevel" : "tenant,app,user",
    "dataType" : "String",
    "fieldtype" : "textbox",
    "length" : "120"
});//
db.getCollection('reference_Meta').insert({
	"type" : "property",
    "name" : "eka_mdm_isCacheEnabled",
    "propertyLevel" : "tenant,app,user",
    "dataType" : "String",
    "fieldtype" : "textbox",
    "length" : "120"
});//

--//@UNDO
db.getCollection('reference_Meta').remove({"type": "property","name": "eka_ctrm_host"});//
db.getCollection('reference_Meta').remove({"type": "property","name": "eka_utility_host"});//
db.getCollection('reference_Meta').remove({"type": "property","name": "eka_platform_host"});//
db.getCollection('reference_Meta').remove({"type": "property","name": "eka_auth_server_host"});//
db.getCollection('reference_Meta').remove({"type": "property","name": "eka_redis_host"});//
db.getCollection('reference_Meta').remove({"type": "property","name": "eka_redis_port"});//
db.getCollection('reference_Meta').remove({"type": "property","name": "eka_mdm_isCacheEnabled"});//
