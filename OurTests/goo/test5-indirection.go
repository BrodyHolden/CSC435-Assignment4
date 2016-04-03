package main

import "fmt"

func main() {
    var i int = 5

    var p *int = &i

    *p = 6

    fmt.Printf("%d\n", *p);
}
