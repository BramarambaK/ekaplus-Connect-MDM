db.getCollection('reference_Data').insert({"key":"creditRiskStatus-001","value":"Active","object":"creditRiskStatus","app":"mdm"});//
db.getCollection('reference_Data').insert({"key":"creditRiskStatus-002","value":"Automatic  Suspension","object":"creditRiskStatus","app":"mdm"});//
db.getCollection('reference_Data').insert({"key":"creditRiskStatus-003","value":"Delivery Stop","object":"creditRiskStatus","app":"mdm"});//
db.getCollection('reference_Data').insert({"key":"creditRiskStatus-004","value":"Prepayment Stop","object":"creditRiskStatus","app":"mdm"});//

--//@UNDO
db.getCollection('reference_Data').remove({"app": "mdm","object": "creditRiskStatus"});//