set session my.number_of_members = '1000';

INSERT INTO public."member" (id, status, first_name, last_name, date_of_birth, gender, address_line_1, address_line_2, city, state, zipcode, country, sponsor_id)
SELECT
    id,
    (array['active', 'canceled'])[floor(random() * 2 + 1)],
    (array['Jill', 'Joe', 'Jack', 'Greg', 'Silvester', 'Tom', 'Toby', 'Scott', 'Alex'])[floor(random() * 9 + 1)],
    (array['Smith', 'Johnson', 'Williams', 'Jones', 'Brown', 'Davis', 'Milller', 'Wilson', 'Taylor'])[floor(random() * 9 + 1)],
    cast( now() - '30 year'::interval * random()  as date ),
    (array['m', 'f'])[floor(random() * 2 + 1)],
    (array['1 maple st', '2 wildflower st', '3 hunter ln'])[floor(random() * 3 + 1)],
    (array['aprtment 1', 'unit 1', 'suite 1'])[floor(random() * 3 + 1)],
    (array['Decatur', 'Atlanta', 'Plymouth'])[floor(random() * 3 + 1)],
    (array['New  York', 'Minnesota', 'Georgia'])[floor(random() * 3 + 1)],
    (array['10001', '92067', '90209'])[floor(random() * 3 + 1)],
    (array['United States', 'India', 'Spain'])[floor(random() * 3 + 1)],
    (array[1,2,3])[floor(random() * 3 + 1)]
FROM GENERATE_SERIES(1, current_setting('my.number_of_members')::int) as id;
