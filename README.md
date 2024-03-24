# Server

A skeletal server to provide services or output

## Building and Running

This project uses "make" commands to build and execute the server, and to clean up build artifacts from the working directory.

### Project Contents

The project consists of a primary Java source, a src subdirectory with shared resources, a Makefile, and this README.md.

```
$ ls
Makefile	README.md	Server.java	src
```

### Build

To build the server, issue:

```bash
$ make
```
Alternately, `make build` starts compilation.

### Run the Server

Start up the server by executing:

```bash
$ make run
```

To keep your server running, use `nohup` execution along with your `make run` command:

```bash
$ nohup make run &
```

Using `nohup` makes the server immune to hangups. It sets the execution to ignore the SIGHUP signal (terminate process and/or terminal line hangup). Hangup immunity provides continuous execution even after ending your shell or SSH session or closing your terminal window. The server continues to run when you log out.

### Nohup and Output

During a nohup run, standard output and standard error are normally redirected to the working directory's "nohup.out" file. This server logs its address, port, and all connections to standard out. View the most recent connections by issuing:

```
$ tail nohup.out
```

### Resolving SSH hang-ups

Occasionally, you may encounter issues with some SSH clients when using nohup. If the session hangs, try this approach:

```
$ nohup make run & > nohup.out 2> nohup.err < /dev/null &
```

The source of the issue is when an SSH session refuses to log off because it's protecting the data going to or from the background job. This resolution approach redirects all three streams (stdin, stdout, stderr).

### Cleaning Up

To remove any generated files or artifacts, use the following command:

```bash
$ make clean
```

This deletes the `build` folder in your working directory.

