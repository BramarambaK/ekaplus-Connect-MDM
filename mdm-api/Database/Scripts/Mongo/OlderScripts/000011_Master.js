
db.reference_Data.update({object:'offsetType', key:'offsetType-003'}, {$addToSet:{event:"null"}});//

--//@UNDO
db.reference_Data.update({object:'offsetType', key:'offsetType-003'}, {$pull:{event:"null"}});//