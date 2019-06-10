# ExceptionAnalyzer

Exception Analyzer for one problem of coding championship.

Language syntax:

* ```func f() {...}``` — function declaration

* ```maybethrow exc1``` — instruction where exception can be thrown

* ```try {...} suppress exc1``` — try block with types of suppressed exceptions in it

* ```f()``` — function call

All instructions, except function declarations, can be located in the body of a function only. Functions cannot be declared inside other functions. The function can be called before its declaration, as well as in its own body.

The names of functions and exceptions in the language must match the regular expression ```[a-zA-Z0-9_]+```, be unique and not match the keywords ```[func,try,suppress,maybethrow]```.

The text of a program can contain an arbitrary number of spaces and blank lines.

The first line of input contains a single integer — the number of function declarations in the code.

The analyzer gets the text of a program and prints exceptions thrown from ```main``` function.

In the first line of the output must be the number of exceptions that the main function can throw. In subsequent lines, the names of these exceptions should be printed one by one.

## Example

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
