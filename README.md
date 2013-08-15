# Varstar

Deployer webservice for uploading R to Vertica.

## Prerequisites

R file restrictions:
  File name is used for library name.
  Function name is used for function name.
  Note that vertica could care less, but it keeps maintenance much simpler.

Needs Capistrano, Vertica driver installed.

Needs ssh key access to all vertica nodes

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2013 One Kings Lane
This project is covered under the [EPL](LICENCE)
