;; migrations/20140521001318415-insert-entities.clj

(defn up []
  [

   "INSERT INTO users(usr_name, usr_bio, created, created_by, modified, modified_by)
   VALUES 
   ('admin', '', current_timestamp(0), 'admin', current_timestamp(0), 'admin');"

   "INSERT INTO entity_types(ety_name, ety_description, ety_short_name, created, created_by, modified, modified_by)
   VALUES 
   ('Person', 'An identified individual.', 'PERSON', current_timestamp(0), 'admin', current_timestamp(0), 'admin'),
   ('Organisation', 'An organisation including both commericial and not-for-profit ventures.', 'ORG', current_timestamp(0), 'admin', current_timestamp(0), 'admin'),
   ('Asset', 'A facility, plant, vehicle, or other technology.','ASSET', current_timestamp(0), 'admin', current_timestamp(0), 'admin');"

   "INSERT INTO relation_types(rty_name, rty_description, rty_short_name, created, created_by, modified, modified_by) 
   VALUES 
   ('Operator', 'operator', 'OPERATOR', current_timestamp(0), 'admin', current_timestamp(0), 'admin'),
   ('Investor', 'investor', 'INVESTOR', current_timestamp(0), 'admin', current_timestamp(0), 'admin'),
   ('Employer', 'employer', 'EMPLOYER', current_timestamp(0), 'admin', current_timestamp(0), 'admin'),
   ('Executive', 'executive', 'EXECUTIVE', current_timestamp(0), 'admin', current_timestamp(0), 'admin'),
   ('Director', 'director', 'DIRECTOR', current_timestamp(0), 'admin', current_timestamp(0), 'admin');"

   ])

(defn down []
  [

   "DELETE FROM relation_types;"
   "DELETE FROM entity_types;"
   "DELETE FROM users;"

   ])
