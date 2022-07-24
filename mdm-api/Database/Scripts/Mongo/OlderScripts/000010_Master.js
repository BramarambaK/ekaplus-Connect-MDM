
db.getCollection('reference_Data').insert({"key":"modeOfTransport-001","value":"Rail","object":"modeOfTransport","app":"mdm", 

});//
db.getCollection('reference_Data').insert({"key":"modeOfTransport-002","value":"Truck","object":"modeOfTransport","app":"mdm", 

});//
db.getCollection('reference_Data').insert({"key":"modeOfTransport-003","value":"Ship","object":"modeOfTransport","app":"mdm", 

});//

db.getCollection('reference_Data').insert({"key":"loose-001","value":"Yes","object":"loose","app":"mdm", 

});//
db.getCollection('reference_Data').insert({"key":"loose-002","value":"No","object":"loose","app":"mdm", 

});//

db.getCollection('reference_Data').insert({"key":"containerSize-001","value":"20 Ft","object":"containerSize","app":"mdm", 

});//

db.getCollection('reference_Data').insert({"key":"containerSize-002","value":"40 Ft","object":"containerSize","app":"mdm", 

});//

db.getCollection('reference_Data').insert({"key":"containerSize-003","value":"60 Ft","object":"containerSize","app":"mdm", 

});//

--//@UNDO
db.getCollection('reference_Data').remove({"app": "mdm","object": "modeOfTransport"});//
db.getCollection('reference_Data').remove({"app": "mdm","object": "loose"});//
db.getCollection('reference_Data').remove({"app": "mdm","object": "containerSize"});//

