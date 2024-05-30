-- Create the topics table with the new structure
CREATE TABLE topics (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(255) UNIQUE NOT NULL
);

-- Create the tags table with a unique constraint on (label, theme)
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    label VARCHAR(255) NOT NULL,
    theme VARCHAR(255) NOT NULL,
    UNIQUE (label, theme)
);

-- Create the messages table with the new structure
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    content TEXT,
    title VARCHAR(400) NOT NULL,
    link VARCHAR(400) NOT NULL,
    topic_id INT NOT NULL,
    FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
);

CREATE TABLE message_tags (
    messageId INT NOT NULL,
    tagId INT NOT NULL,
    FOREIGN KEY (messageId) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (tagId) REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (messageId, tagId)
);

INSERT INTO topics (id, name, url) VALUES (1,'4sysops','http://4sysops.com/feed/');
INSERT INTO topics (id, name, url) VALUES (2,'theguardian','https://www.theguardian.com/us/technology/rss');
INSERT INTO topics (id, name, url) VALUES (3,'reddittech','https://www.reddit.com/r/technology/top.rss?t=day');
INSERT INTO topics (id, name, url) VALUES (4,'developpez','https://cloud-computing.developpez.com/rss.php');
INSERT INTO topics (id, name, url) VALUES (5,'wsj','https://feeds.a.dj.com/rss/RSSWSJD.xml');
INSERT INTO topics (id, name, url) VALUES (6,'slashdot','http://rss.slashdot.org/Slashdot/slashdotMain');

INSERT INTO tags (id, label, theme) VALUES (1,'Tag 1', 'Theme A');
INSERT INTO tags (id, label, theme) VALUES (2,'Tag 2', 'Theme B' );
INSERT INTO tags (id, label, theme) VALUES (3,'Tag 3', 'Theme C');

INSERT INTO messages (id, date, content, title, link, topic_id) 
VALUES (
  1, 
  '2024-05-10T15:17:50Z', 
  'Microsoft has recently announced that Basic Authentication in Exchange Online SMTP AUTH will be disabled in September 2025. In this post, I will discuss the implications of this announcement and provide guidance on what steps you can take to ensure your email applications and devices remain functional.',
  'Disable Basic Authentication for SMTP AUTH in Exchange Online',
  'https://4sysops.com/archives/disable-basic-authentication-for-smtp-auth-in-exchange-online/', 
  1
);

INSERT INTO messages (id, date, content, title, link, topic_id) 
VALUES (
    2, 
    '2024-05-02T16:29:12Z', 
    'When organizations implement a lockout policy, it is common for users to lock themselves out and require assistance from the helpdesk. In such cases, helpdesk personnel without administrator rights require permission to unlock user accounts. This can be achieved through delegation.', 
    'Delegate permission to unlock Active Directory accounts', 
    'https://4sysops.com/archives/delegate-permission-to-unlock-active-directory-accounts/', 
    1
);

INSERT INTO messages (id, date, content, title, link, topic_id) 
VALUES (
    3, 
    '2024-05-10T21:06:52Z', 
    '<p>Protesters opposed to expansion of the US electric vehicle maker Tesla xbcnheide near Berlin clashed with police as some of them attempted to storm the facility. More than 800 people took part in the protest, according to the organising group Disrupt Tesla, which claims the expansion would damage the environment. Footage shows people wearing blue caps and masks coming from a nearby wooded area and attempting to storm the company\s premises with police officers trying to prevent them</p><p></p><ul><li><p><a href=\"https://www.theguardian.com/technology/article/2024/may/10/tesla-protest-germany-factory\">Eight hundred protesters attempt to storm German Tesla factory</a></p></li></ul> <a href=\"https://www.theguardian.com/technology/video/2024/may/10/germany-police-clash-with-hundreds-of-climate-protesters-trying-to-storm-tesla-plant-video\">Continue reading...</a>', 
    'Germany: police clash with hundreds of climate protesters trying to storm Tesla plant – video', 
    'https://www.theguardian.com/technology/video/2024/may/10/germany-police-clash-with-hundreds-of-climate-protesters-trying-to-storm-tesla-plant-video', 
    2
);

INSERT INTO messages (id, date, content, title, link, topic_id) 
VALUES (
    4, 
    '2024-05-12T15:42:42Z', 
    NULL, 
    'Cheap Catalyst Made Out of Sugar Has the Power To Destroy CO2', 
    'https://www.reddit.com/r/technology/comments/1cqapx8/cheap_catalyst_made_out_of_sugar_has_the_power_to/', 
    3
);

INSERT INTO messages (id, date, content, title, link, topic_id) 
VALUES (
    5, 
    '2024-05-12T13:04:46Z', 
    NULL, 
    'FDA recalls faulty iOS app that injured hundreds of insulin pump users', 
    'https://www.reddit.com/r/technology/comments/1cq7f9f/fda_recalls_faulty_ios_app_that_injured_hundreds/', 
    3
);

INSERT INTO messages (id, date, content, title, link, topic_id) 
VALUES (
    6, 
    '2024-05-08T03:05:00Z', 
    '<b>Microsoft fait l\objet d\une plainte de 700 startups auprès des autorités antitrust concernant le monopole du cloud Azure, Microsoft forcerait l\utilisation de son cloud Azure et limite la concurrence loyale.</b> <br/><br/><b>Microsoft fait l\objet d\une plainte de la part d\entreprises espagnoles concernant ses pratiques en matière de cloud computing. La plainte indique que Microsoft pourrait utiliser sa position dominante sur le marché des logiciels pour imposer l\utilisation de ses services cloud, en imposant...</b>', 
    'Microsoft fait l\objet d\une plainte de 700 startups auprès des autorités antitrust concernant le monopole du cloud Azure, Microsoft forcerait l\utilisation de son cloud Azure et limite la concurrence loyale', 
    'https://windows-azure.developpez.com/actu/357449/Microsoft-fait-l-objet-d-une-plainte-de-700-startups-aupres-des-autorites-antitrust-concernant-le-monopole-du-cloud-Azure-Microsoft-forcerait-l-utilisation-de-son-cloud-Azure-et-limite-la-concurrence-loyale/', 
    4
);

INSERT INTO messages (id, date, content, title, link, topic_id) 
VALUES (
    7, 
    '2024-04-26T04:58:00Z', 
    '<b>La proposition américaine qui s\apparente aux processus "Know Your Customer" mettra fin à l\anonymat des utilisateurs du cloud.</b><br />\\n<b>Prétextant la lutte contre la cybercriminalité, elle suscite la controverse    </b><br/><br/><b>En janvier dernier, le Département du Commerce des États-Unis a publié un avis de proposition de réglementation visant à établir de nouvelles exigences pour les fournisseurs d\Infrastructure en tant que Service (IaaS). Cette proposition s\articule autour d\un régime « Know Your Customer » (KYC)...</b>', 
    'La proposition américaine qui s\apparente aux processus "Know Your Customer" mettra fin à l\anonymat des utilisateurs du cloud. Prétextant la lutte contre la cybercriminalité, elle suscite la controverse', 
    'https://droit.developpez.com/actu/357012/La-proposition-americaine-qui-s-apparente-aux-processus-Know-Your-Customer-mettra-fin-a-l-anonymat-des-utilisateurs-du-cloud-Pretextant-la-lutte-contre-la-cybercriminalite-elle-suscite-la-controverse/', 
    4
);

INSERT INTO message_tags (messageId, tagId) VALUES (1, 1);
INSERT INTO message_tags (messageId, tagId) VALUES (2, 2);
INSERT INTO message_tags (messageId, tagId) VALUES (3, 3);
INSERT INTO message_tags (messageId, tagId) VALUES (4, 2);
INSERT INTO message_tags (messageId, tagId) VALUES (5, 3);  
INSERT INTO message_tags (messageId, tagId) VALUES (6, 3);
INSERT INTO message_tags (messageId, tagId) VALUES (7, 3);

