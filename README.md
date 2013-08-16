# Varstar

Deployer webservice for uploading R to Vertica.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above, 
[Localrepo][2] 0.5.2 or above,
and [Capistrano][3] 2.15.4 or above installed.

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/kumarshantanu/lein-localrepo
[3]: https://github.com/capistrano/capistrano

Additionally, you will need [Vertica](http://www.vertica.com/) set up.

Each Vertica node needs an uploader account with uploading privileges for uploading files and executing shell commands (only for installing packages to Vertica's instance of R). The account needs ssh-key access from the computer/server this deployer will be put on.

The uploader account should also ensure that the right file permissions are applied to uploaded files, so that Vertica can read the files.

The Vertica authentication account must have superuser privileges in order to execute create and drop library/function commands.

## Installation

First, install all prerequisite software.

Then, install your suitable Vertica driver by placing a copy of the driver jar in the project, and running the following command:
    
    lein localrepo coords <rel-path-to>/<vertica-driver>.jar | xargs lein localrepo install

## Running

To start a web server for the application, run:

    lein deps
    lein ring server
    
The server should bring up a website. If not, then navigate to
[http://localhost:3000](http://localhost:3000).

To use the web service:

1. Make sure your R files adhere to Vertica's convention, as stated in their programmer's manual. 
2. Upload your selected file to the deployer. 
3. Select which functions you wish to deploy.
4. Click on the deploy button. The server will then upload your file and deploy the functions to Vertica.

## Notes

R/Vertica file restrictions:

- The file name is used for library name.
- The function name is used for function name in Vertica.

Note that Vertica could care less, but it keeps maintenance much simpler.

## License

Copyright © 2013 One Kings Lane.

This project is covered under the [EPL](LICENSE).
