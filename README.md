# Varstar

Deployer webservice for uploading R to Vertica.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above, 
and [Capistrano][2] 2.15.4 installed.

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/capistrano/capistrano

Additionally, you will need Vertica set up.
Each Vertica node needs to have an account for uploading 
with ssh-key access from the computer this deployer will be put on.

## Installation

Note that to install Vertica driver, some Lein plugin is needed. TODO ask Alex
what he used.

First, install all prerequisites, then
    
    lein deps
    TODO generate-default-conf-yml
    
Then fill in the default conf.yml.

## Running

To start a web server for the application, run:

    lein deps
    lein ring server
    
The server should bring up a website. If not, then navigate to
[http://localhost:3000](http://localhost:3000).

## Notes

R file restrictions:

- File name is used for library name.
- Function name is used for function name.
- Note that vertica could care less, but it keeps maintenance much simpler.

## License

Copyright © 2013 One Kings Lane.

This project is covered under the [EPL](LICENSE).
