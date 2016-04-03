package main

import "fmt"

func main() {
    var a int = 5

    fmt.Printf("a: %d\n", a)

    a = +a
    fmt.Printf("+a: %d\n", a);

    a = -a
    fmt.Printf("-a: %d\n", a);
}
