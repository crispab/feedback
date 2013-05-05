Feedback
======

General
-------

This is a happiness index tool for our customers to use for giving us feedback on our performance as Crisp consultants

 - It is written in Scala and based on the Play Framework.
 - Anorm is used for SQL Database access*
 - Heroku is used for deployment

*) H2 when running locally, PostgreSQL on Heroku


### Play Framework ###

 - Play 2.1.0 - http://www.playframework.org/
 - Scala 2.10.0 (part of Play) - http://www.scala-lang.org/
 - Anorm (part of Play) - http://www.playframework.com/documentation/2.1.0/ScalaAnorm

### Presentation ###

 - Twitter Bootstrap 2.2.1 - http://twitter.github.com/bootstrap
 - jQuery 1.9.0 (used by Twitter Bootstrap) - http://jquery.com
 - Flotr2 2013-03-22 - http://www.humblesoftware.com/flotr2

### Services ###
 - Play2.x module for Authentication and Authorization 0.9 - http://github.com/t2v/play20-auth


License, credits and stuff
--------------------------

The Twitter Bootstrap LESS files that are included in the source code
are Copyright 2012 Twitter, Inc and licensed under the Apache
License v2.0.
Modifications have been made by us to create a project specific look & feel.

The rest of the code is Copyright 2012 by Mats Strandberg and Jan Grape and
licensed under the Apache License v2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


Setting Up Development Environment
----------------------------------

### Basics ###

In order to set up the development environment you need to:

 - Clone this project from GitHub
 - Install Play 2.0
 - Install Heroku Toolkit - https://toolbelt.heroku.com/ (Optional, only needed to deploy to Heroku)
 - ```play eclipsify``` or ```play idea```

### "Persistent Database" - Postgres ###

When running Feedback as described above, the H2 database is used.
The H2 database is running in-memory, in-process. This is handy,
because of the simple setup, but each time you stop Play your database
will be gone. It would be nice to have a persistent database,
e.g. a disk based database. When running on Heroku, Postgres will be used.
For these reasons, a local installation of Postgres is good.

To install Postgres on MacOS X:

 - Download Postgres from [postgres.org](http://www.postgresql.org/)
 - Install a standard Postgres (You may have to restart your Mac as the installation fiddles with shared memory)
 - Set the password for the DATABASE (super)user postgres when prompted
 - Finish the installation
 - On MacOS X: A UNIX user is created: 'PostgreSQL'
   This user seem to get some password that you cannot find out.
   Reset the password of the UNIX user 'PostgreSQL' to something you know, and something safe, as this is a real MacOS X user.
 - Do some stuff from the command prompt to create a database and a database user

```
$ su - PostgreSQL
Password: <Enter the password of your UNIX user 'PostgreSQL'>
$ createdb feedback
Password: <Enter the password of your DATABASE user 'postgres'>
$ psql -s feedback
Password: <Enter the password of your DATABASE user 'postgres'>
feedback=# create user feedback password 's7p2+';
feedback=# grant all privileges on database feedback to feedback;
```

Now you can start Feedback with Postgres using

./playrunpostgres.sh

Run Feedback
----------

Once you have the development environment set up you should be able to do

```play run```

And the direct your browser to
[http://localhost:9000](http://localhost:9000)

If you have a local Postgres (installed as described above) you may start Feedback using

./playrunpostgres.sh

Deploy
------

Deployment is done to Heroku.

It runs at [http://crisp-feedback.herokuapp.com](http://feedback4.herokuapp.com)

To be able to deploy to Heroku you must:

* Install Heroku toolbelt.
* Do a 'heroku login'.
* cd into the root of this application
* git remote add production git@heroku.com:crisp-feedback.git
* git push production master

Sometimes you have to do a 'heroku keys:add ~/.ssh/id_rsa.pub' if you get
'Permission denied (publickey).' when trying to 'git push production master'

