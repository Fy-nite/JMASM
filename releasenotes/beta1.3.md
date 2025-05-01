Hello everyone!
i've got great news and even better news if you use MNI in your projects!

This release comprises multiple weeks worth of programming efforts by me and my fellow developers into one giant release for today!

this release includes Scala and kotlin now being supported and used inside the codebase, aswell as a new module called IO.

IO currently has 2 functions plus a helper method for any of you j/s/k coders out there.

```nasm
MNI IO.write <FD 1 : 0> <addr without $ for now>
```

this allows users to print to standard out or error with extra support for Terminal colors and formatting.

in order to use colors or formatting, just write the string with the following format:

```nasm
[<contents>]
```

where contents is the supported color or formatting code.

the supported codes are:

- red
- green
- yellow
- blue
- magenta
- cyan
- white
- black
- bg_red
- bg_green
- bg_yellow
- bg_blue
- bg_magenta
- bg_cyan
- bg_white
- bg_black
- bold
- underline
- italic
- strike
- dim
- hidden
- reset
- resetall
- clear
- reverse
- blink

```nasm
db $100 "Hello, [red]World![reset]"
MNI IO.write 1 100
```

this will print "Hello, World!" in red to standard out.

the second function is:

```nasm
MNI IO.flush <FD 1 : 0>
```

this function flushes the buffer for the specified file descriptor.

the helper method is:

```java
Parsing.INSTANCE.parseAnsiTerminal(String)
```

this method will parse the string and return a string with the ansi codes replaced with the actual ansi codes.

that's all i have for now, but i hope you enjoy this release and the new features it brings to the table!