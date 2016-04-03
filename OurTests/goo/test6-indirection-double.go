package main

import "fmt"

func main() {
    var i int = 5

    var p *int = &i

    var p2 **int = &p

    **p2 = 6

    fmt.Printf("%d\n", **p2);
}
