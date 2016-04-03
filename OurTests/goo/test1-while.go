package main

import "fmt"

func main() {
    var a int = 0

    for a < 5 {
        fmt.Printf("value of a: %d\n", a)
        a += 1
    }
}
