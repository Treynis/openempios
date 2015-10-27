INSERT INTO report
(report_id, "name", name_displayed, description, template_name, data_generator) VALUES
(409, 'event-activity', 'Event activity', 'Event activity', 'event_activity', 'jdbcReportGenerator');

INSERT INTO report_parameter
(report_parameter_id, "name", name_displayed, description, report_id, parameter_datatype) VALUES
(418, 'start-date', 'Start Date', NULL, 409, 0),
(419, 'end-date', 'End Date', NULL, 409, 0);

INSERT INTO report_query (report_query_id, "name", query, report_id) VALUES
(452, 'add-identifier-count', 'select count(*) "addIdentifierCount" from person_identifier p', 409),
(453, 'link-count', 'select count(*) "linkCount" from person_link p ', 409),
(454, 'delete-count', 'select count(*) "deleteCount" from person p where date_voided is not null', 409),
(455, 'delete-identifier-count', 'select count(*) "deleteIdentifierCount" from person_identifier p where date_voided is not null', 409),
(456, 'add-count', 'select count(*) "addCount" from person p', 409),
(457, 'update-count', 'select count(*) "updateCount" from person p where date_voided is null and p.date_created <> p.date_changed', 409),
(458, 'person-count', 'select count(*) "count" from person where date_voided is null', 409),
(459, 'identifier-count', 'select count(*) "identifierCount" from person_identifier where date_voided is null', 409);

INSERT INTO report_query_parameter (report_query_parameter_id, report_parameter_id, parameter_name, report_query_id, required) VALUES
(418, 419, 'p.date_created <', 452, 'y'),
(419, 418, 'p.date_created >', 452, 'y'),
(420, 418, 'p.date_created >', 453, 'y'),
(421, 419, 'p.date_created <', 453, 'y'),
(422, 418, 'p.date_created >', 454, 'y'),
(423, 419, 'p.date_created <', 454, 'y'),
(424, 419, 'p.date_created <', 455, 'y'),
(425, 418, 'p.date_created >', 455, 'y'),
(426, 418, 'p.date_created >', 456, 'y'),
(427, 419, 'p.date_created <', 456, 'y'),
(428, 419, 'p.date_changed <', 457, 'y'),
(429, 418, 'p.date_changed >', 457, 'y');

INSERT INTO report
(report_id, "name", name_displayed, description, template_name, data_generator) VALUES
(850, 'potential-match-review-summary', 'potential match review summary', 'potential match review summary', 'potential_match_review_summary', 'jdbcReportGenerator');

INSERT INTO report_parameter
(report_parameter_id, "name", name_displayed, description, report_id, parameter_datatype) VALUES
(850, 'start-date', 'Start Date', NULL, 850, 0);

INSERT INTO report_query (report_query_id, "name", query, report_id) VALUES
(900, 'link-list', 'SELECT pl.person_link_id "linkId", 
lp.given_name "lGivenName", 
lp.family_name "lFamilyName", 
lp.date_of_birth "lDateBirth", 
lp.ssn "lSsn",
rp.given_name "rGivenName",
 rp.family_name "rFamilyName", 
rp.date_of_birth "rDateBirth", 
rp.ssn "rSsn",
lg.gender_name "lGender",
rg.gender_name "rGender"
FROM person_link pl
	INNER JOIN person lp
       		ON pl.lh_person_id = lp.person_id
	INNER JOIN person rp
       		ON pl.rh_person_id = rp.person_id
	left outer join gender lg on lp.gender_cd = lg.gender_cd
	left outer join gender rg on rp.gender_cd = rg.gender_cd', 850);

INSERT INTO report_query_parameter (report_query_parameter_id, report_parameter_id, parameter_name, report_query_id, required) VALUES
(850, 850, 'pl.date_created >', 900, 'y');


INSERT INTO report
(report_id, "name", name_displayed, description, template_name, data_generator) VALUES
(705, 'potential-match-review-detail', 'Potential Match Review Detail', 'Potential Match Review Detail', 'potential_match_review_detail', 'jdbcReportGenerator');

INSERT INTO report_parameter
(report_parameter_id, "name", name_displayed, description, report_id, parameter_datatype) VALUES
(710, 'start-date', 'Start Date', NULL, 705, 0),
(711, 'end-date', 'End Date', NULL, 705, 0);


INSERT INTO report_query (report_query_id, "name", query, report_id) VALUES
(755, 'link-list', 'SELECT pl.person_link_id "linkId", pl.date_created "dateCreated", 
lp.given_name "lGivenName", rp.given_name "rGivenName",
lp.middle_name "lMiddleName", rp.middle_name "rMiddleName",
lp.family_name "lFamilyName", rp.family_name "rFamilyName",  
lp.date_of_birth "lDateBirth",  rp.date_of_birth "rDateBirth",
lp.birth_place "lBirthPlace",  rp.birth_place "rBirthPlace",
lp.multiple_birth_ind "lBirthInd",  rp.multiple_birth_ind "rBirthInd",
lp.address1 "lAddress",rp.address1 "rAddress",
lp.city "lCity",rp.city "rCity",
lp.state "lState",rp.state "rState",
lp.postal_code "lZipCode",rp.postal_code "rZipCode",
lp.country "lCountry",rp.country "rCountry",
lg.gender_name "lGender", rg.gender_name "rGender",
lp.marital_status_code "lMaritalStatus",rp.marital_status_code "rMaritalStatus",
lp.mothers_maiden_name "lMaidenName",rp.mothers_maiden_name "rMaidenName",
lp.phone_area_code "lPhoneArea", rp.phone_area_code "rPhoneArea",
lp.phone_number "lPhoneNumber", rp.phone_number "rPhoneNumber",
lp.phone_ext "lPhoneExt", rp.phone_ext "rPhoneExt",
lp.email "lEmail", rp.email "rEmail",
lp.ssn "lSsn",rp.ssn "rSsn"
FROM person_link pl
	INNER JOIN person lp
       		ON pl.lh_person_id = lp.person_id
	INNER JOIN person rp
       		ON pl.rh_person_id = rp.person_id
	left outer join gender lg on lp.gender_cd = lg.gender_cd
	left outer join gender rg on rp.gender_cd = rg.gender_cd', 705);

INSERT INTO report_query_parameter (report_query_parameter_id, report_parameter_id, parameter_name, report_query_id, required) VALUES
(710, 711, 'pl.date_created <', 755, 'y'),
(711, 710, 'pl.date_created >', 755, 'y');


INSERT INTO report
(report_id, "name", name_displayed, description, template_name, data_generator) VALUES
(307, 'duplicate-summary-statistics', 'Duplicate Summary Statistics', 'Duplicate Summary Statistics', 'duplicate_summary_statistics', 'jdbcReportGenerator');

INSERT INTO report_parameter
(report_parameter_id, "name", name_displayed, description, report_id, parameter_datatype) VALUES
(314, 'start-date', 'Start Date', NULL, 307, 0),
(315, 'end-date', 'End Date', NULL, 307, 0);

INSERT INTO report_query (report_query_id, "name", query, report_id) VALUES
(313, 'person-count', 'select count(*) "count" from person where date_voided is null', 307),
(314, 'unique-count', 'select count(*) "count"
from (
    select cluster_id
    from person_link l
    inner join person pl on pl.person_id = l.rh_person_id
    inner join person pr on pr.person_id = l.lh_person_id
    where pl.date_voided is null
      and pr.date_voided is null
      and $lStartDate
      and $lEndDate
      and cluster_id is not null
    group by cluster_id
    union
    select person_id
    from person p
    where not exists (select pl.person_link_id from person_link pl
                where pl.lh_person_id = p.person_id
                   or pl.rh_person_id = p.person_id)
      and p.date_voided is null
      and $pStartDate
      and $pEndDate
) clusters', 307),
(315, 'match-count', 'select count(*) "count" from person_link_review pl', 307),
(316, 'resolve-count', 'select count(*) "count" from person_link_review pl', 307);

INSERT INTO report_query_parameter (report_query_parameter_id, report_parameter_id, parameter_name, report_query_id, required) VALUES
(204, 314, 'pl.date_created >', 315, 'y'),
(205, 315, 'pl.date_created <', 315, 'y'),
(206, 314, 'pl.date_reviewed >', 316, 'y'),
(207, 315, 'pl.date_reviewed <', 316, 'y');

INSERT INTO report_query_parameter (report_query_parameter_id, report_parameter_id, parameter_name, report_query_id, required, substitution_key) VALUES
(208, 314, 'l.date_created >', 314, 'y', 'lStartDate'),
(209, 315, 'l.date_created <', 314, 'y', 'lEndDate'),
(210, 314, 'p.date_created >', 314, 'y', 'pStartDate'),
(211, 315, 'p.date_created <', 314, 'y', 'pEndDate');

INSERT INTO report
(report_id, "name", name_displayed, description, template_name, data_generator) VALUES
(1000, 'data-profile-summary', 'Data Profile Summary', 'Data Profile Summary', 'data_profile_summary', 'jdbcReportGenerator');


INSERT INTO report_query (report_query_id, "name", query, report_id) VALUES
(1000, 'list', 'SELECT dp.attribute_name "AttributeName", 
dp.datatype_id "DataType",
dp.row_count "RowCount", 
dp.distinct_count "DistinctCount", 
dp.duplicate_count "DuplicateCount", 
dp.unique_count "UniqueCount",
dp.null_count "NullCount",
dp.average_length "AverageLength",
dp.minimum_length "MinimumLength",
dp.maximum_length "MaximumLength",
dp.average_value "AverageValue",
dp.minimum_value "MinimumValue",
dp.maximum_value "MaximumValue",
dp.variance "Variance",
dp.standard_deviation "StandardDeviation",
dp.null_rate "NullRate",
dp.entropy "Entropy",
dp.maximum_entropy "MaximumEntropy",
dp.u_value "UValue",
dp.average_token_frequency "AverageTokenFrequency",
dp.blocking_pairs "BlockingPairs"
FROM data_profile_attribute dp', 1000);


INSERT INTO report
(report_id, "name", name_displayed, description, template_name, data_generator) VALUES
(1085, 'data-profile-detail', 'Data Profile Detail', 'Data Profile Detail', 'data_profile_detail', 'jdbcReportGenerator');


INSERT INTO report_query (report_query_id, "name", query, report_id) VALUES
(1085, 'list', 'SELECT dpa.attribute_name "AttributeName", 
dpav.attribute_value "AttributeValue", 
dpav.frequency "AttributeFrequency"
FROM data_profile_attribute_value dpav 
	INNER JOIN data_profile_attribute dpa
       		ON  dpa.attribute_id = dpav.attribute_id
      		AND dpa.data_source_id = 0
       		AND dpav.attribute_value IN ( SELECT attribute_value FROM data_profile_attribute_value WHERE attribute_id = dpav.attribute_id ORDER BY frequency desc limit 10 )
       		order by attribute_name, frequency desc', 1085);





