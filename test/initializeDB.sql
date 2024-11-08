# insert roleTypes into database
INSERT INTO roleTypes
VALUES (0, 'Patient');
INSERT INTO roleTypes
VALUES (1, 'Staff');
INSERT INTO roleTypes
VALUES (2, 'Admin');

# insert sample users into database
INSERT INTO users
VALUES (UNHEX(SHA2('JohnDoe@email.com', 256)),
                          'JohnDoe@email.com',
              UNHEX(SHA2('password123', 256)),
                                       'John',
                                        'Doe',
                                          'M',
                                    948614400,
                           '123 Address Lane',
                                 '1234567890',
                                            0,
                                          '');
INSERT INTO users
VALUES (UNHEX(SHA2('JaneDoe@email.com', 256)),
                          'JaneDoe@email.com',
              UNHEX(SHA2('password456', 256)),
                                       'John',
                                        'Doe',
                                          'M',
								                   1016092800,
						               '123 Address Lane',
								                 '3141592653',
										                        0,
										                      '');
INSERT INTO users
VALUES (UNHEX(SHA2('DonneAught@scheduledsmiles.com', 256)),
                           'DawnAught@scheduledsmiles.com',
                           UNHEX(SHA2('password314', 256)),
                                                    'Dawn',
                                                   'Aught',
                                                       'F',
                                                 948614400,
                                         '456 Address Ave',
                                              '9169678121',
                                                         1,
                                                       '');
INSERT INTO users
VALUES (UNHEX(SHA2('AdamMinh@scheduledsmiles.com', 256)),
                          'AdamMinh@scheduledsmiles.com',
                         UNHEX(SHA2('password159', 256)),
                                                  'Adam',
                                                  'Minh',
                                                     'M',
                                               948614400,
                                       '789 Address Way',
                                            '9166534124',
                                                       2,
                                                     '');
