
db.getCollection('reference_Data').insert({
    "propertyValue" : "http://192.168.1.225:9090",
    "propertyLevel" : "app",
    "type" : "property",
    "refTypeId" : "84d7b167-1d9f-406d-b974-bea406a25f9a",
    "propertyName" : "eka_ctrm_host"
});//
db.getCollection('reference_Data').insert({
    "propertyValue" : "http://192.168.1.225:3334",
    "propertyLevel" : "app",
    "type" : "property",
    "refTypeId" : "84d7b167-1d9f-406d-b974-bea406a25f9a",
    "propertyName" : "eka_utility_host"
});//
db.getCollection('reference_Data').insert({
    "propertyValue" : "http://manuchar.integ.ekaanalytics.com:99",
    "propertyLevel" : "app",
    "type" : "property",
    "refTypeId" : "84d7b167-1d9f-406d-b974-bea406a25f9a",
    "propertyName" : "eka_platform_host"
});//
db.getCollection('reference_Data').insert({
    "propertyValue" : "http://manuchar.integ.ekaanalytics.com:99",
    "propertyLevel" : "app",
    "type" : "property",
    "refTypeId" : "84d7b167-1d9f-406d-b974-bea406a25f9a",
    "propertyName" : "eka_auth_server_host"
});//
db.getCollection('reference_Data').insert({
    "propertyValue" : "192.168.1.225",
    "propertyLevel" : "app",
    "type" : "property",
    "refTypeId" : "84d7b167-1d9f-406d-b974-bea406a25f9a",
    "propertyName" : "eka_redis_host"
});//
db.getCollection('reference_Data').insert({
    "propertyValue" : "6379",
    "propertyLevel" : "app",
    "type" : "property",
    "refTypeId" : "84d7b167-1d9f-406d-b974-bea406a25f9a",
    "propertyName" : "eka_redis_port"
});//
db.getCollection('reference_Data').insert({
    "propertyValue" : "Y",
    "propertyLevel" : "app",
    "type" : "property",
    "refTypeId" : "84d7b167-1d9f-406d-b974-bea406a25f9a",
    "propertyName" : "eka_mdm_isCacheEnabled"
});//

--//@UNDO
db.getCollection('reference_Data').remove({"type": "property","name": "eka_ctrm_host"});//
db.getCollection('reference_Data').remove({"type": "property","name": "eka_utility_host"});//
db.getCollection('reference_Data').remove({"type": "property","name": "eka_platform_host"});//
db.getCollection('reference_Data').remove({"type": "property","name": "eka_auth_server_host"});//
db.getCollection('reference_Data').remove({"type": "property","name": "eka_redis_host"});//
db.getCollection('reference_Data').remove({"type": "property","name": "eka_redis_port"});//
db.getCollection('reference_Data').remove({"type": "property","name": "eka_mdm_isCacheEnabled"});//
