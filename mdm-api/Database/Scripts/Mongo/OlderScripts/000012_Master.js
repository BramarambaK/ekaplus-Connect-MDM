db.getCollection('reference_Data').insert({"key":"creditLimitSource-001","value":"Credendo","object":"creditLimitSource","app":"mdm"});//
db.getCollection('reference_Data').insert({"key":"creditLimitSource-002","value":"equinox","object":"creditLimitSource","app":"mdm"});//
db.getCollection('reference_Data').insert({"key":"creditLimitSource-003","value":"Own Risk","object":"creditLimitSource","app":"mdm"});//


db.getCollection('reference_Data').insert({"key":"creditLimitType-001","value":"Contract(Full Term)","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-001"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-002","value":"Contract(Partial Term)","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-001"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-003","value":"Credit Limit","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-001"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-004","value":"Temporary","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-001"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-005","value":"Prepayment Contract(Full Term)","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-001"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-006","value":"Prepayment Credit Limit","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-001"]});//


db.getCollection('reference_Data').insert({"key":"creditLimitType-007","value":"Top Up Contract(Full Term)","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-002"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-008","value":"Top Up Credit Limit","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-002"]});//


db.getCollection('reference_Data').insert({"key":"creditLimitType-009","value":"Limit","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-003"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-010","value":"Contract(Full term)","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-003"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-011","value":"Contract(Partial Term)","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-003"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-012","value":"Prepayment Limit","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-003"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-013","value":"Prepayment Contract(Full Term)","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-003"]});//
db.getCollection('reference_Data').insert({"key":"creditLimitType-014","value":"Prepayment Contract(Partial term)","object":"creditLimitType","app":"mdm", 
    "creditLimitSource":["creditLimitSource-003"]});//
    

db.getCollection('reference_Data').insert({"key":"limitStatus-001","value":"Active","object":"limitStatus","app":"mdm"});//
db.getCollection('reference_Data').insert({"key":"limitStatus-002","value":"Inactive","object":"limitStatus","app":"mdm"});//

--//@UNDO
db.getCollection('reference_Data').remove({"app": "mdm","object": "creditLimitSource"});//
db.getCollection('reference_Data').remove({"app": "mdm","object": "creditLimitType"});//
db.getCollection('reference_Data').remove({"app": "mdm","object": "limitStatus"});//

