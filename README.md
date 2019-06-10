# ExceptionAnalyzer

This is exception analyzer for one problem of coding championship.

Imagine a language with this syntax:

* ```func f() {...}``` — function declaration

* ```maybethrow exc1``` — instruction where exception can be thrown

* ```try {...} suppress exc1``` — try block with types of suppressed exceptions in it

* ```f()``` — function call

All instructions, except function declarations, can be located in the body of a function only. Functions cannot be declared inside other functions. The function can be called before its declaration, as well as in its own body.

The names of functions and exceptions in the language must match the regular expression ```[a-zA-Z0-9_]+```, be unique and not match the keywords ```[func,try,suppress,maybethrow]```.

The text of a program can contain an arbitrary number of spaces and blank lines.

The first line of input contains a single integer — the number of function declarations in the code.

It is guaranteed that the program contains the declaration of the ```main``` function.

The analyzer gets the text of a program and prints exceptions thrown from ```main``` function.

The first line of the output must contain the number of exceptions that the main function can throw. In subsequent lines, the names of these exceptions should be printed one by one.

## Example 1

**Input:**
```
2
func func1() {
maybethrow exc1
try {
maybethrow exc2
maybethrow exc3

} suppress exc1, exc2, exc3
maybethrow exc4

}

func main() {
try {
func1()
} suppress exc1
}
```

**Output:**
```
1
exc4
```

## Example 2

**Input:**
```
6

  func func1(){maybethrow exc1 func1()}

 func func2(  )  {
    func3   ()
  }

func   func3   (  )  {
  func2 ( )
     maybethrow    exc3
}


func func4(      ) {
  func3      ()
  try      {
    maybethrow     exc4
    func5  ()
  } suppress    exc3
}



func func5(   )    {
  maybethrow   exc4
  maybethrow    exc3
    try   {
    maybethrow    exc5
  } suppress        exc3
}

   func    main   (   ) {
  func1(   )
  func2(  )
  func3(      )
  func4 ()
  try    {
      maybethrow exc2
      
      maybethrow    exc3      
    maybethrow exc4
          try{maybethrow exc6 maybethrow exc7 maybethrow exc8}suppress exc8
       maybethrow exc5
     }    suppress     exc3     ,    exc5
}
```

**Output:**
```
7
exc7
exc6
exc5
exc4
exc3
exc2
exc1
```
