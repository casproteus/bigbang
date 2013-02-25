use:
1/goto: http://localhost/useraccounts/ -> generate admin user. -> then login as admin -> then added tags for default display.
2/generate a user administrator. -> then login as administrator -> then create a "unclassified tag with this administrator. (it will be the default one when creating content).

dev:
1/I used ... as other tag (for not classified content., and have to find a way to replace ... with I18n. because this field can not be null or empty, and how to use a string to match it our so I can make it not display in personal page.

test:
1/when run the selenium, set the speed, make it not the fastest.
2/make sure tao:1234 admin:2ert are available, and tao has tag of "Simon&Nicholas"
3/selenium can not record selection action in drop box, so have to manuly add some date for test:
	1)authoritized contens: guy is lady's team -> guy has two tag, one private, one for team. in team tag, there's 2 content, one is private, one is for team.
	2)can see nothing if not login as lady nor guy.
	3)login as guy, can see everything in guy space, and because guy is lady's team, so in other space can see all contents which of 0 and 2
		while, if some content is 2, but the othor is not lady but some one else

know bugs:
1/
2/

