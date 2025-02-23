MSAF Spec Version 1

Written by Charlie Santana

15-02-2025



MNI (Micro-Assembly Native Interface) Connector and Custom Functions



Assessment

This document seeks to provide and request information based on a Spec for MNI and ways for

plugins to implement extended functionality for Micro-Assembly based programs

and to include Custom functions through class path manipulation.

## 1\.0: What is MNI

MNI is a proposed name for a Micro-Assembly "Native interface", similar to how CSharp has native DLL importing, being able to interactively use libraries ether in the java programming language, or other programming languages that support runtime loading of libraries.



## 1\.1: what features does this give us?

the use case of MNI would allow you (the user) to implement things such as bindings to NCurses or being able to link other java libraries such as being able to write unit tests inside Micro-Assembly, or even using the interpreter directly inside Micro-Assembly.

MNI should be a internal function only really useful through writing libraries or if needed, through direct access through calling MNI directly.



Take this for example, say you create a java function that takes in a object, ether string or int and turns it to a string, and then prepends hello to it.

```java
/*
    * This is a test file for the MNI spec, it is not a real file
    * This file is used to test the MNI spec

    MNIMethodObjects contain all the infomation about what the interpreter is doing.
    context, states of all variables and everything else.
*/
@MNIClass("functest")
public class functest {
    @MNIFunction("functest","test",int);
    public static void test(MNIMethodObject obj) {
        int start = obj.args[0];
        int outputdest = obj.args[1];
        String output = "";
        int i = start;
        int mem = 0;
        // read memory through while, if the memory is not 0, add it to the output
        while (mem != 0) {
            mem = obj.readMemory(i); // links to context and common.readMemory();
            output += (char)mem; // add the memory to the output
            i++; // increment the memory
        }
        // prepend hello to the output
        output = "hello " + output;
        // write the output to the memory
        obj.writeMemory(outputdest, output);
    }
 }
```

as you can see we are setting up "annotations" which are java's version of [attributes].

first, we annotate that the class "functest" is a MNIClass that can be accessed or is to hold functions that Micro-Assembly can read and use.



if we implement how we could access this from Micro-Assembly then it would look something like

```wasm
lbl main
  mov R0 60
  mov R2 100
  mov R1 1 ; setup for printing to stdout
  DB $R0 "cat" ;; put a string in memory for the function
  MNI functest.test R0 R2 ;; ask MNI to call functest.test with address 50 as-
  ;; in and 60 as out
  out R1 $R2 ;; print the modified string "Hello cat"
  hlt
```

using MNI should be as straight forward as possible.



any class or function that you do not annotate should not show up in the list of functions that the user has access to, think of it like public and private methods.

## 1\.2 why not just reflect to find them?

using annotations is easier on us as we develop the JMASM project and also on the developers.

if we went with reflection without annotations then we would be getting functions and classes we do not want to share with the other users or just junk things that shouldn't be used outside java.



## 1\.3: creating custom functions



As people write Micro-Assembly libraries, Developers might want to create a custom library that they want users to access through their plugin.



a simple way to do that should be using a custom Class-Path for java or some other languages for people to use with the `#import "&lt;lib name here&gt;"` statement you can add to the top of the function.

this would give developers of these plugins, the ability to create custom files that users could access through Micro-Assembly without having to use MNI.



take for example, we have a file in a jar called `myprint.jar`

```wasm
lbl printf
  out RAX $RBX
  ret
```

developers would want to provide that file to the other developers or "users" that could benefit from the function or "label"



## 2\.0: why not just put the files in the Working dir?

importing files is a big part of how programming languages work, and how we can create big apps while also having a codebase that does not look half garbage to anyone who wants to help us.



it's also the main reason that jimmy can provide you some libraries and allow you (the developer) to interact with his cool ncurses wrapper that has cool cat pictures inside it.



without importing files, languages would just sort of die or more better, break into peices.

because now you have to make your "compiler" be one file instead of multiple files and that would also harm development of libraries too.



### 3\.0: Full copyright statement

```
Copyright (C) Charlie-san (2025). All Rights reserved


   This document and translations of it may be copied and furnished to
   others, and derivative works that comment on or otherwise explain it
   or assist in its implementation may be prepared, copied, published
   and distributed, in whole or in part, without restriction of any
   kind, provided that the above copyright notice and this paragraph are
   included on all such copies and derivative works.  However, this
   document itself may not be modified in any way, such as by removing
   the copyright notice or references to the charlie-san/Finite or other
   organizations, except as needed for the purpose of
   developing MSF standards in which case the procedures for
   copyrights defined in the MSF process must be
   followed, or as required to translate it into languages other than
   English.

   The limited permissions granted above are perpetual and will not be
   revoked by Finite or its successors or assigns.

   This document and the information contained herein is provided on an
   "AS IS" basis and FINITE DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
   BUT NOT LIMITED TO ANY WARRANTY THAT THE USE OF THE INFORMATION
   HEREIN WILL NOT INFRINGE ANY RIGHTS OR ANY IMPLIED WARRANTIES OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
```